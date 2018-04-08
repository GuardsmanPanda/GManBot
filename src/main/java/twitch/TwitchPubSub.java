package twitch;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Strings;
import com.neovisionaries.ws.client.*;
import database.BobsDatabaseHelper;
import utility.Extra;
import utility.PrettyPrinter;
import webapi.Twitchv5;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class TwitchPubSub {
    private static WebSocket connection = null;

    public static void connect() {
        try {
            connection = new WebSocketFactory().createSocket("wss://pubsub-edge.twitch.tv")
                    .addListener(new TwitchPubSubListener())
                    .connect();
        } catch (IOException | WebSocketException e) {
            e.printStackTrace();
        }

        // twitch pubsub does not use the build in ping methods, instead requires an explicit ping message every 5 minutes
        // for now we just try to send a ping every 5 minutes, this should be cleaned up later
        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(() -> connection.sendText("{ \"type\": \"PING\" }"), 5, 5, TimeUnit.MINUTES);
    }

    private static class TwitchPubSubListener extends WebSocketAdapter {
        @Override
        public void onConnected(WebSocket websocket, Map<String, List<String>> headers) {
            System.out.println("Connected To Twitch PubSub");
            //Listen for subs and resubs
            ObjectNode dataNode = JsonNodeFactory.instance.objectNode();
            dataNode.putArray("topics").add("channel-subscribe-events-v1." + Twitchv5.BOBSCHANNELID);
            dataNode.put("auth_token", Twitchv5.getAuthTokenForBobsChannel());

            ObjectNode rootNode = JsonNodeFactory.instance.objectNode();
            rootNode.put("type", "LISTEN");
            rootNode.set("data", dataNode);
            websocket.sendText(rootNode.toString());
        }

        //TODO Detect sub tiers and gifted subs! .. sub tier: "sub_plan": (String) number/Prime
        @Override
        public void onTextMessage(WebSocket websocket, String text) {
            try {
                JsonNode root = new ObjectMapper().readTree(text);
                if (root.get("type").asText().equalsIgnoreCase("MESSAGE")) {
                    String topic = root.get("data").get("topic").asText();
                    if (topic.equalsIgnoreCase("channel-subscribe-events-v1." + Twitchv5.BOBSCHANNELID)) {
                        JsonNode messageNode = new ObjectMapper().readTree(root.get("data").get("message").asText());
                        PrettyPrinter.prettyPrintJSonNode(messageNode);

                        String userId = messageNode.get("user_id").asText();
                        int months = messageNode.get("months").asInt();
                        String displayName = messageNode.get("display_name").asText();

                        if (months < 2) TwitchChat.sendMessage("Thanks For Subscribing " + displayName + "! \uD83D\uDD38 My Emotes -> " + Twitchv5.getBobsEmoticonSet().stream().sorted(Extra.randomOrder()).collect(Collectors.joining(" ")));
                        else if (months % 12 == 0) TwitchChat.sendMessage(Strings.repeat("bobCake ", months/12) + " Happy "+getBirthDayOrdinal(months/12)+" Stream Birthday " + displayName + "!" + Strings.repeat(" bobCake", months/12));
                        else TwitchChat.sendMessage("Thank You So Much For Subscribing Again " + displayName + "!" + Strings.repeat(" bobCake", months/12) + Strings.repeat(" bobHype", months%12));

                        BobsDatabaseHelper.setSubscriberMonths(userId, months);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onTextMessageError(WebSocket websocket, WebSocketException cause, byte[] data) {
            System.out.println("***PubSub Error Message: " + new String(data));
        }

        @Override
        public void onFrame(WebSocket websocket, WebSocketFrame frame) {
            if (!frame.getPayloadText().contains("\"type\": \"PONG\"")) {
                System.out.println("PubSub Frame: " + frame.getPayloadText());
            }
            //Ignore pong responses for now, correct behavior would be to time the ping/pong difference and reconnect if no pong response 10 seconds after ping.
        }

        @Override
        public void onPingFrame(WebSocket websocket, WebSocketFrame frame) {
            System.out.println("PubSub Ping Frame" + frame);
        }

        @Override
        public void onError(WebSocket websocket, WebSocketException cause) {
            System.out.println("**PubSub Error: " + cause.toString());
        }

        @Override
        public void onDisconnected(WebSocket websocket, WebSocketFrame serverCloseFrame, WebSocketFrame clientCloseFrame, boolean closedByServer) {
            System.out.println("PubSub disconnected, by server: " + closedByServer);
        }

        private String getBirthDayOrdinal(int year) {
            switch (year) {
                case 1: return "First";
                case 2: return "Second";
                case 3: return "Third";
                case 4: return "Fourth";
                case 5: return "Fifth";
                case 6: return "Sixth";
            }
            return "";
        }
    }
}

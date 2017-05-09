package twitch;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.neovisionaries.ws.client.*;
import database.BobsDatabaseHelper;
import utility.PrettyPrinter;
import webapi.Twitchv5;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

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

        @Override
        public void onTextMessage(WebSocket websocket, String text) {
            try {
                JsonNode root = new ObjectMapper().readTree(text);
                if (root.get("type").asText().equalsIgnoreCase("MESSAGE")) {
                    String topic = root.get("data").get("topic").asText();
                    if (topic.equalsIgnoreCase("channel-subscribe-events-v1." + Twitchv5.BOBSCHANNELID)) {
                        JsonNode messageNode = new ObjectMapper().readTree(root.get("data").get("message").asText());
                        PrettyPrinter.prettyPrintJSonNode(messageNode);
                        System.out.println("SubEvent -> UserID: " + messageNode.get("user_id").asText() + ", DisplayName: " + messageNode.get("display_name").asText() + ", Months: " + messageNode.get("months").asInt());
                        BobsDatabaseHelper.setSubscriberMonths(messageNode.get("user_id").asText(), messageNode.get("months").asInt());
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
            if (frame.getPayloadText().contains("\"type\": \"PONG\"")) {
                //Ignore pong responses for now, correct behavior would be to time the ping/pong difference and reconnect if no pong response 10 seconds after ping.
            } else {
                System.out.println("PubSub Frame: " + frame.getPayloadText());
            }
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
    }
}

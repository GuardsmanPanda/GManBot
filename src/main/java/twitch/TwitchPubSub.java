package twitch;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.neovisionaries.ws.client.*;
import utility.PrettyPrinter;
import webapi.Twitchv5;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class TwitchPubSub {
    private static WebSocket connection = null;

    public static void main(String[] args) throws InterruptedException {
        connect();
        Thread.sleep(10000);
    }

    public static void connect() throws InterruptedException {
        try {
            connection = new WebSocketFactory().createSocket("wss://pubsub-edge.twitch.tv")
                    .addListener(new TwitchPubSubListener())
                    .setPingPayloadGenerator("{ \"type\": \"PING\" }"::getBytes)
                    .setPingInterval(300000)
                    .connect();
        } catch (IOException | WebSocketException e) {
            e.printStackTrace();
        }
    }


    private static class TwitchPubSubListener extends WebSocketAdapter {
        @Override
        public void onConnected(WebSocket websocket, Map<String, List<String>> headers) {
            System.out.println("Connected To Twitch PubSub");
            ObjectNode dataNode = JsonNodeFactory.instance.objectNode();
            dataNode.putArray("topics").add("channel-subscribe-events-v1." + Twitchv5.BOBSCHANNELID);
            dataNode.put("auth_token", Twitchv5.getAuthTokenForBobsChannel());

            ObjectNode rootNode = JsonNodeFactory.instance.objectNode();
            rootNode.put("type", "LISTEN");
            rootNode.set("data", dataNode);
            websocket.sendText(rootNode.toString());
        }

        @Override
        public void onTextMessage(WebSocket websocket, String text)  {
            try {
                System.out.println("PubSub Message: " + text);
                JsonNode root = new ObjectMapper().readTree(text);
                if (root.get("type").asText().equalsIgnoreCase("MESSAGE")) {
                    String topic = root.get("data").get("topic").asText();
                    if (topic.equalsIgnoreCase("channel-subscribe-events-v1." + Twitchv5.BOBSCHANNELID)) {
                        System.out.println("RESUB");
                        PrettyPrinter.prettyPrintJSonNode(root);
                        JsonNode messageNode = root.get("data").get("message");
                        System.out.println("SubEvent -> UserID: " + messageNode.get("user_id").asText() + ", DisplayName: " + messageNode.get("display_name").asText() + ", Months: " + messageNode.get("months").asInt());
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
            System.out.println("PubSub Frame: " + frame.getPayloadText());
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

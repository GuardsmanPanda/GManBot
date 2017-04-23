package core;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import twitch.Twitchv5;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Path;


public class StreamWebOverlay {
    private static final Path OVERLAYFILEPATH = new File("Data/OBS/HTMLStreamOverlay.html").toPath();
    private static final BobsWebSocketServer socketServer = new BobsWebSocketServer(new InetSocketAddress(9102));

    public static void main(String[] args) throws InterruptedException {
        startOverlay();
        Thread.sleep(10000);
        JsonNodeFactory factory = JsonNodeFactory.instance;
        ObjectNode root = factory.objectNode();
        root.set("type", factory.textNode("gameRating"));
        root.set("gameName", factory.textNode(Twitchv5.getGameTitle()));
        sendJsonToOverlay(root);
    }

    public static void startOverlay() {
        try {
            HttpServer httpServer = HttpServer.create(new InetSocketAddress(9101), 0);
            httpServer.createContext("/OBSOverlay", new OverlayContext());
            httpServer.start();
            socketServer.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * This will silently fail if the overlay has not established a connection.
     * @param node
     */
    public synchronized static void sendJsonToOverlay(JsonNode node) {
        socketServer.sendMessage(node.toString());
    }


    private static class OverlayContext implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            byte[] overlayFileBytes = Files.readAllBytes(OVERLAYFILEPATH);
            exchange.sendResponseHeaders(200, overlayFileBytes.length);
            exchange.getResponseBody().write(overlayFileBytes);
            exchange.close();
        }
    }

    private static class BobsWebSocketServer extends WebSocketServer {

        public BobsWebSocketServer(InetSocketAddress inetSocketAddress) {
            super(inetSocketAddress);
        }

        public void sendMessage(String message) {
            connections().forEach(connection -> connection.send(message));
        }

        @Override
        public void onOpen(org.java_websocket.WebSocket webSocket, ClientHandshake clientHandshake) {
            System.out.println("Connection from overlay: " + webSocket.getRemoteSocketAddress().getAddress().getHostAddress());
        }

        @Override
        public void onClose(org.java_websocket.WebSocket webSocket, int i, String s, boolean b) {
            System.out.println("WebSocket closed");
        }

        @Override
        public void onMessage(org.java_websocket.WebSocket webSocket, String s) {
            System.out.println("Message from socket: " + s);
        }

        @Override
        public void onError(org.java_websocket.WebSocket webSocket, Exception e) {
            System.out.println("WebSocket error");
        }
    }
}

package core;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import utility.FinalPair;
import webapi.Quotes;
import webapi.Twitchv5;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;


public class StreamWebOverlay {
    private static final String OVERLAYFOLDER = "Data/OBS/";
    private static final BobsWebSocketServer socketServer = new BobsWebSocketServer(new InetSocketAddress(9102));
    private static boolean displayQuotes = false;

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

    //TODO: implement a queue system that waits 30+ seconds between each headline push
    public synchronized static void sendHeadlineToOverlay(String headline, String htmlData) {
        ObjectNode root = JsonNodeFactory.instance.objectNode();
        root.put("type", "headline");
        root.put("headline", headline);
        root.put("htmlData", htmlData);
        socketServer.sendMessage(root.toString());
    }

    /**
     * This will silently fail if the overlay has not established a connection.
     */
    public synchronized static void sendJsonToOverlay(JsonNode node) {
        //System.out.println("* Sending Message to Socket: " + node.toString());
        socketServer.sendMessage(node.toString());
    }

    public static void streamIntro() {
        startQuoteOverlayService();
        showEmoteImage();
    }

    public static void endStreamIntro() {
        stopQuoteOverlayService();
        hideEmoteImage();
    }


    public static void showEmoteImage() {
        int PADDING = 6;
        ObjectNode root = JsonNodeFactory.instance.objectNode();
        root.put("type", "bobEmotes");
        root.put("showImage", true);

        List<Image> emoteImages = Twitchv5.getBobsEmoticonSet().stream()
                .map(name -> {
                    try {
                        return ImageIO.read(new File("Data/Icons/bobEmotes/" + name + ".png"));
                    } catch (IOException e) {
                        System.out.println("Error reading " + name);
                        e.printStackTrace();
                        throw new RuntimeException("Error loading emoteImages");
                    }
                }).collect(Collectors.toList());

        int imageWidth = emoteImages.get(0).getWidth(null);
        int imageHeight = emoteImages.get(0).getHeight(null);
        int totalWidth = PADDING*2 + (imageWidth + PADDING) * emoteImages.size();

        System.out.println("iconImage width/height/totalWidth " + imageWidth + "/" + imageHeight + "/" + totalWidth);

        BufferedImage iconImage = new BufferedImage(totalWidth, imageHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics iconGraphics = iconImage.createGraphics();

        for (int i = 0; i < emoteImages.size(); i++) {
            iconGraphics.drawImage(emoteImages.get(i), PADDING + i * (imageWidth + PADDING), 0, imageWidth, imageHeight, null);
        }

        try {
            ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
            ImageIO.write(iconImage, "png", byteStream);
            root.put("imageData", byteStream.toByteArray());
        } catch (IOException e) {
            e.printStackTrace();
        }
        sendJsonToOverlay(root);
    }
    public static void hideEmoteImage() {
        ObjectNode root = JsonNodeFactory.instance.objectNode();
        root.put("type", "bobEmotes");
        root.put("showImage", false);
        sendJsonToOverlay(root);
    }
    private static void startQuoteOverlayService() {
        if (displayQuotes) return;

        displayQuotes = true;
        new Thread(() -> {

            while (displayQuotes) {
                FinalPair<String, String> quotePair = Quotes.getRandomQuote();
                ObjectNode root = JsonNodeFactory.instance.objectNode();
                root.put("type", "quote");
                root.put("quoteText", quotePair.first);
                root.put("quoteAuthor", quotePair.second);
                sendJsonToOverlay(root);

                try {
                    Thread.sleep(10000 + quotePair.first.length() * 65);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
    private static void stopQuoteOverlayService() {
        displayQuotes = false;
        new Thread(() -> {
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            ObjectNode root = JsonNodeFactory.instance.objectNode();
            root.put("type", "quote");
            root.put("quoteText", "");
            root.put("quoteAuthor", "");
            sendJsonToOverlay(root);
        }).start();
    }


    private static class OverlayContext implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String req = exchange.getRequestURI().toString();
            System.out.println("request>>> " + req);
            Path pathToResponseFile = Paths.get(OVERLAYFOLDER + req.substring(req.lastIndexOf("/")));
            byte[] overlayFileBytes = Files.readAllBytes(pathToResponseFile);
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

package core;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Path;

public class StreamWebOverLay {
    private static final Path OVERLAYFILEPATH = new File("Data/OBS/HTMLStreamOverlay.html").toPath();

    public static void main(String[] args) {
        startOverlay();
    }

    public static void startOverlay() {
        try {
            HttpServer server = HttpServer.create(new InetSocketAddress(9101), 0);
            server.createContext("/OBSOverlay", new OverlayContext());
            server.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void showGameNameAndRating(String gameName, double gameRating) {
        //TODO something something push data to browser
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
}

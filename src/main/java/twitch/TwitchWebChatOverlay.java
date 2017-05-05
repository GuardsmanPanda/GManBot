package twitch;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import database.BobsDatabaseHelper;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class TwitchWebChatOverlay {
    private static HashMap<String, byte[]> flagCache = new HashMap<>();

    //TODO Consolidate all HTTP servers into one dispatcher.
    public static void startHttpService() {
        try {
            HttpServer server = HttpServer.create(new InetSocketAddress(8000), 0);
            server.setExecutor(new ThreadPoolExecutor(5, 10, 100, TimeUnit.SECONDS, new LinkedBlockingDeque<>()));
            server.createContext("/flags", new flagHandler());
            server.createContext("/", new chatOverlay());
            server.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static class flagHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange httpExchange) throws IOException {
            String requestURI = httpExchange.getRequestURI().toString();
            String twitchUser = requestURI.substring(requestURI.lastIndexOf("/") + 1, requestURI.indexOf("?")).toLowerCase();
            httpExchange.getRequestBody().close();

            //System.out.println(requestURI);

            String flagName = BobsDatabaseHelper.getFlagFromTwitchName(twitchUser);

            flagCache.computeIfAbsent(flagName, flag -> {
                System.out.println("Loading Flag: " + flagName);
                File flagFile = new File("Data/Flags/Images/" + flagName + ".png");
                try {
                    return Files.readAllBytes(flagFile.toPath());
                } catch (IOException e) {
                    e.printStackTrace();
                    throw new RuntimeException();
                }
            });

            httpExchange.sendResponseHeaders(200, flagCache.get(flagName).length);
            httpExchange.getResponseBody().write(flagCache.get(flagName));
            httpExchange.close();
        }
    }

    static class chatOverlay implements HttpHandler {
        @Override
        public void handle(HttpExchange httpExchange) throws IOException {
            String requestURI = httpExchange.getRequestURI().toString();
            StringBuilder responseBuilder = new StringBuilder();

            System.out.println("Request URI: " + requestURI);

            URLConnection connection = new URL("https://streamlabs.com" + requestURI).openConnection();
            connection.setRequestProperty("User-agent", "");

            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            reader.lines().forEachOrdered(responseBuilder::append);
            reader.close();

            String response = responseBuilder.toString().replaceAll("https://streamlabs.com", "http://127.0.0.1:8000");
            response = response.replaceAll("\"Chrome\"", "\"bobHype\"");

            if (requestURI.contains("/widgets/chat-box/v1/")) {
                response = response.replace("</body>", iconScript() + "</body>");
            }

            httpExchange.sendResponseHeaders(200, response.getBytes().length);
            httpExchange.getResponseBody().write(response.getBytes());
            httpExchange.getResponseBody().close();
            httpExchange.getRequestBody().close();
        }

        private String iconScript() {
            return  "<script>" +
                    "var id = 0;" +
                    "document.getElementById('log').appendChild = function(node) {" +
                    "   this.insertAdjacentElement('beforeend', node);" +
                    "   var name = node.getAttribute('data-from');" +
                    "   var chatMessage = node.children[1].textContent;" +
                    "   if (chatMessage.toLowerCase().includes('bob')) {" +
                    "       node.children[1].className += ' bobHighlight';" +
                    "   }" +
                    "   if (name.toLowerCase() === 'gmanbot') {" +
                    "       node.children[1].className += ' botHighlight';" +
                    "   }" +
                    "   if (name.toLowerCase() === 'gmanbot4') {" +
                    "       node.children[1].style.color='#37e1f5';" +
                    "   }" +
                    "   var metaNode = node.firstElementChild;" +
                    "   var flagNode = document.createElement(\"img\");" +
                    "   flagNode.src = 'http://127.0.0.1:8000/flags/' + name + '?id=' + id;" +
                    "   flagNode.className = 'badge flag-icon';" +
                    "   metaNode.insertAdjacentElement('afterbegin',flagNode);" +
                    "   id++;" +
                    "}" +
                    "</script>";
        }
    }
}

package twitch;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import database.BobsDatabaseHelper;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class TwitchWebChatOverlay {
    private static Map<String, Image> flagCache = new HashMap<>();
    private static Map<String, byte[]> iconCache = new ConcurrentHashMap<>();
    private static final byte[] emptyImageBytes;
    private static final int ICONHEIGHT = 22;
    private static final Image heartImage;
    private static final Image bronze;
    private static final Image silver;
    private static final Image gold;

    static {
        BufferedImage newImage = new BufferedImage(76, ICONHEIGHT, BufferedImage.TYPE_INT_ARGB);
        Graphics graphics = newImage.createGraphics();
        graphics.setColor(new Color(0,0,0,0));
        try {
            ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
            ImageIO.write(newImage, "jpg", byteStream);
            emptyImageBytes = byteStream.toByteArray();
            heartImage = ImageIO.read(new File("Data/Icons/heart.png")).getScaledInstance(ICONHEIGHT, ICONHEIGHT, Image.SCALE_SMOOTH);
            bronze = ImageIO.read(new File("Data/Icons/bronzeStar.png")).getScaledInstance(ICONHEIGHT, ICONHEIGHT, Image.SCALE_SMOOTH);
            silver = ImageIO.read(new File("Data/Icons/silverStar.png")).getScaledInstance(ICONHEIGHT, ICONHEIGHT, Image.SCALE_SMOOTH);
            gold = ImageIO.read(new File("Data/Icons/goldStar.png")).getScaledInstance(ICONHEIGHT, ICONHEIGHT, Image.SCALE_SMOOTH);
        } catch (IOException e) {
            throw new RuntimeException("Couldn't create emptyImage");
        }
    }

    public static void invalidateIcon(String twitchUserID) { iconCache.remove(twitchUserID); }

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

    //flag icon 30wx22h, heart icon: 22x22
    static class flagHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange httpExchange) throws IOException {
            String requestURI = httpExchange.getRequestURI().toString();
            String twitchUserName = requestURI.substring(requestURI.lastIndexOf("/") + 1, requestURI.indexOf("?")).toLowerCase();
            twitchUserName = URLDecoder.decode(twitchUserName, "UTF-8");
            String twitchUserID = BobsDatabaseHelper.getTwitchUserID(twitchUserName);

            httpExchange.getRequestBody().close();

            byte[] returnImageBytes = emptyImageBytes;

            if (!twitchUserID.isEmpty()) {
                String flagName = BobsDatabaseHelper.getFlagName(twitchUserID);

                Image flagImage = flagCache.computeIfAbsent(flagName, flag -> {
                    System.out.println("Loading Flag: " + flagName);
                    File flagFile = new File("Data/Flags/Images/" + flagName + ".png");
                    try {
                        return ImageIO.read(flagFile).getScaledInstance(30, ICONHEIGHT, Image.SCALE_SMOOTH);
                    } catch (IOException e) {
                        e.printStackTrace();
                        throw new RuntimeException();
                    }
                });

                returnImageBytes = iconCache.computeIfAbsent(twitchUserID, icon -> {
                    java.util.List<Image> imagesToDraw = new ArrayList<>();
                    int donated = BobsDatabaseHelper.getCentsDonated(twitchUserID);
                    if (donated > 12000) imagesToDraw.add(gold);
                    else if (donated > 2000) imagesToDraw.add(silver);
                    else if (donated > 500) imagesToDraw.add(bronze);

                    if (BobsDatabaseHelper.getHeartsBob(twitchUserID)) imagesToDraw.add(heartImage);

                    imagesToDraw.add(flagImage);

                    BufferedImage newImage = new BufferedImage(76, ICONHEIGHT, BufferedImage.TYPE_INT_ARGB);
                    Graphics graphics = newImage.createGraphics();
                    int startingPixel = (3 - imagesToDraw.size()) * 23;

                    graphics.setColor(new Color(0,0,0,0));
                    for (int i = 0; i < imagesToDraw.size(); i++) {
                        graphics.drawImage(imagesToDraw.get(i), 23 * i + startingPixel, 0, null);
                    }

                    try {
                        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
                        ImageIO.write(newImage, "png", byteStream);
                        return byteStream.toByteArray();
                    } catch (IOException e) {
                        e.printStackTrace();
                        throw new RuntimeException();
                    }
                });
            }

            httpExchange.sendResponseHeaders(200, returnImageBytes.length);
            httpExchange.getResponseBody().write(returnImageBytes);
            httpExchange.close();
        }
    }

    static class chatOverlay implements HttpHandler {
        @Override
        public void handle(HttpExchange httpExchange) throws IOException {
            String requestURI = httpExchange.getRequestURI().toString();
            StringBuilder responseBuilder = new StringBuilder();

            System.out.println("Request URI: " + requestURI);

            HttpURLConnection connection = (HttpURLConnection)new URL("https://streamlabs.com" + requestURI).openConnection();
            connection.setRequestMethod(httpExchange.getRequestMethod());
            connection.setRequestProperty("User-agent", "");
            String referer = httpExchange.getRequestHeaders().getFirst("Referer");
            if (referer != null) {
                connection.setRequestProperty("Referer", referer.replace("http://127.0.0.1:8000", "https://streamlabs.com"));
            }
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            reader.lines().forEachOrdered(responseBuilder::append);
            reader.close();

            String response = responseBuilder.toString().replaceAll("https://streamlabs.com", "http://127.0.0.1:8000");

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
                    "   this.append(node);" +
                    "   var name = node.querySelector('.name').textContent;" +
                    "   var chatMessage = node.querySelector('.message').textContent;" +
                    "   if (chatMessage.toLowerCase().includes('bob')) {" +
                    "       node.querySelector('.message').className += ' bobHighlight';" +
                    "   }" +
                    "   if (name.toLowerCase() === 'gmanbot') {" +
                    "       node.querySelector('.message').className += ' botHighlight';" +
                    "   }" +
                    "   var metaNode = node.querySelector('.meta');" +
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

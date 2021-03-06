package webapi;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jdk.incubator.http.HttpClient;
import jdk.incubator.http.HttpRequest;
import jdk.incubator.http.HttpResponse;
import twitch.TwitchChat;
import utility.PrettyPrinter;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class Twitchv5 {
    public static final String BOBSCHANNELID = "30084132";
    public static final String GMANBOTUSERID = "39837384";
    private static final HttpClient httpClient = HttpClient.newHttpClient();
    private static String AuthTokenForBobsChannel = "";
    private static String twitchApiKey = "";

    //Caches
    private static final Map<String, Set<String>> emoteSetCache = new HashMap<>();

    static {
        try {
            twitchApiKey = Files.readAllLines(Paths.get("Data/twitchapikey.txt")).get(0);
            AuthTokenForBobsChannel = Files.readAllLines(Paths.get("Data/twitchoauthtoken.txt")).get(0);
        } catch (IOException e) {
            System.out.println("Expecting api key");
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        System.out.println(getStreamUpTime("33"));
    }

    public static Duration getStreamUpTime() { return getStreamUpTime(BOBSCHANNELID); }
    public static Duration getStreamUpTime(String channelID) {
        JsonNode root = executeHttpGet("https://api.twitch.tv/kraken/streams/" + channelID);
        if (root != null && root.has("stream") && root.get("stream").has("created_at")) {
            PrettyPrinter.prettyPrintJSonNode(root);
            Instant startTime = Instant.parse(root.get("stream").get("created_at").asText(Instant.now().toString()));
            return Duration.between(startTime, Instant.now());
        }
        return Duration.ZERO;
    }

    public static String getGameName() {
        return getGameName(BOBSCHANNELID);
    }

    public static String getGameName(String channelID) {
        JsonNode rootNode = executeHttpGet("https://api.twitch.tv/kraken/channels/" + channelID);
        if (rootNode != null && rootNode.has("game")) return rootNode.get("game").asText();
        else {
            System.out.println("Could not find game for channel + " + channelID);
            return "";
        }
    }

    public static Set<String> getBobsEmoticonSet() {
        return getEmoticonSet("581");
    }
    public static Set<String> getGlobalTwitchEmoteSet() {
        return getEmoticonSet("0");
    }
    public static Set<String> getEmoticonSet(String emoteSet) {
        return emoteSetCache.computeIfAbsent(emoteSet, emoteSetID -> {
            JsonNode root = executeHttpGet("https://api.twitch.tv/kraken/chat/emoticon_images?emotesets=" + emoteSet);
            if (root != null && root.has("emoticon_sets") && root.get("emoticon_sets").has(emoteSet)) {
                return StreamSupport.stream(root.get("emoticon_sets").get(emoteSet).spliterator(), false)
                        .map(node -> node.get("code").asText())
                        .filter(emoteString -> !emoteString.contains("\\")) //Remove all emotes that are regex
                        .collect(Collectors.toSet());
            } else {
                System.out.println("Could not rating emote set!!! " + emoteSet);
                return Set.of();
            }
        });
    }

    /**
     * Gets the emoticon set for BTTV, please not that this is not a call to the official twitch APi
     */
    public static Set<String> getBTTVEmoteSet() {
        JsonNode root = WebClient.getJSonNodeFromRequest(HttpRequest.newBuilder(URI.create("https://api.betterttv.net/2/emotes")).GET().build());
        if (root.has("emotes")) {
            return StreamSupport.stream(root.get("emotes").spliterator(), false)
                    .map(node -> node.get("code").asText())
                    .collect(Collectors.toSet());
        }
        System.out.println("Something went wrong trying to get BTTV emoticon set");
        return Set.of();
    }

    public static String getDisplayName(String twitchUserID) {
        JsonNode rootNode = executeHttpGet("https://api.twitch.tv/kraken/users/" + twitchUserID);
        if (rootNode.has("display_name")) {
            return rootNode.get("display_name").asText();
        } else {
            System.out.println("Error requesting user ID " + twitchUserID);
            PrettyPrinter.prettyPrintJSonNode(rootNode);
            return "";
        }
    }

    public static LocalDateTime getFollowDateTime(String twitchUserID) {
        JsonNode rootNode = executeHttpGet("https://api.twitch.tv/kraken/users/" + twitchUserID + "/follows/channels/"+BOBSCHANNELID);
        if (rootNode != null && rootNode.has("created_at")) {
            return LocalDateTime.ofInstant(Instant.parse(rootNode.get("created_at").asText()), ZoneId.systemDefault());
        } else {
            System.out.println("Error getting follow date: " + rootNode);
            return null;
        }
    }

    public static LocalDateTime getSubStreakStartDate(String twitchUserID) {
        JsonNode rootNode = executeHttpGet("https://api.twitch.tv/kraken/channels/"+BOBSCHANNELID+"/subscriptions/"+twitchUserID);
        if (rootNode != null && rootNode.has("created_at")) {
            return LocalDateTime.ofInstant(Instant.parse(rootNode.get("created_at").asText()), ZoneId.systemDefault());
        } else {
            System.out.println("Error getting sub streak start: " + rootNode);
            return null;
        }
    }

    public static void setChannelTitle(String newTitle) {
        ObjectNode root = JsonNodeFactory.instance.objectNode();
        ObjectNode channelNode = JsonNodeFactory.instance.objectNode();
        channelNode.put("status", newTitle);

        root.set("channel", channelNode);
        int statusCode = executeHttpPut("https://api.twitch.tv/kraken/channels/"+BOBSCHANNELID, root.toString());
        if (statusCode == 200) TwitchChat.sendMessage("Stream Title Changed!");
        else System.out.println("Error trying to change stream title, code: " + statusCode);
    }
    public static void setChannelGame(String newGame) {
        ObjectNode root = JsonNodeFactory.instance.objectNode();
        ObjectNode channelNode = JsonNodeFactory.instance.objectNode();
        channelNode.put("game", newGame);

        root.set("channel", channelNode);
        int statusCode = executeHttpPut("https://api.twitch.tv/kraken/channels/"+BOBSCHANNELID, root.toString());
        if (statusCode == 200) TwitchChat.sendMessage("Game Changed!");
    }

    public static String getAuthTokenForBobsChannel() { return AuthTokenForBobsChannel; }

    /**
     * Requturns the requests status code.
     * @param requestURIString
     * @param body
     * @return
     */
    private static int executeHttpPut(String requestURIString, String body) {
        HttpRequest request = HttpRequest.newBuilder(URI.create(requestURIString))
                .header("Accept", "application/vnd.twitchtv.v5+json")
                .header("Client-ID", twitchApiKey)
                .header("Authorization", "OAuth " + AuthTokenForBobsChannel)
                .header("Content-Type", "application/json")
                .PUT(HttpRequest.BodyPublisher.fromString(body)).build();
        return WebClient.executeHttpRequest(request);
    }

    public synchronized static JsonNode executeHttpGet(String requestURIString) {
        try {
            HttpRequest getRequest = HttpRequest.newBuilder(new URI(requestURIString))
                    .header("Accept", "application/vnd.twitchtv.v5+json")
                    .header("Client-ID", twitchApiKey)
                    .header("Authorization", "OAuth " + AuthTokenForBobsChannel)
                    //.timeout(Duration.ofSeconds(5))
                    .GET().build();
            HttpResponse<String> response = httpClient.send(getRequest, HttpResponse.BodyHandler.asString());

            if (response.statusCode() == 200) {
                return new ObjectMapper().readTree(response.body());
            } else if (response.statusCode() == 400 || response.statusCode() == 404) {
                //Bad request, for example requesting a user that doesn't exist anymore.
                return new ObjectMapper().readTree(response.body());
            } else {
                System.out.println("Something went wrong when executing GET: " + requestURIString);
                System.out.println(response.statusCode());
                System.out.println(response.body());
                return null;
            }
        } catch (IOException | InterruptedException | URISyntaxException e) {
            e.printStackTrace();
        }
        return null;
    }

}

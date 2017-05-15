package webapi;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jdk.incubator.http.HttpClient;
import jdk.incubator.http.HttpRequest;
import jdk.incubator.http.HttpResponse;
import utility.PrettyPrinter;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class Twitchv5 {
    public static final String BOBSCHANNELID = "30084132";
    public static final String GMANBOTUSERID = "39837384";
    private static final HttpClient httpClient = HttpClient.newHttpClient();
    private static String AuthTokenForBobsChannel = "";
    private static String twitchApiKey = "";

    static {
        try {
            twitchApiKey = Files.readAllLines(Paths.get("Data/twitchapikey.txt")).get(0);
            AuthTokenForBobsChannel = Files.readAllLines(Paths.get("Data/twitchoauthtoken.txt")).get(0);
        } catch (IOException e) {
            System.out.println("Expecting api key");
            e.printStackTrace();
        }
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

    //TODO throw exception if set empty
    public static Set<String> getBobsEmoticonSet() {
        return getEmoticonSet("581");
    }
    public static Set<String> getGlobalTwitchEmoteSet() {
        Set<String> emotes = getEmoticonSet("0");
        emotes.removeIf(emote -> emote.contains("\\"));
        return emotes;
    }
    public static Set<String> getEmoticonSet(String emoteSet) {
        JsonNode root = executeHttpGet("https://api.twitch.tv/kraken/chat/emoticon_images?emotesets=" + emoteSet);
        if (root != null && root.has("emoticon_sets") && root.get("emoticon_sets").has(emoteSet)) {
            return StreamSupport.stream(root.get("emoticon_sets").get(emoteSet).spliterator(), false)
                    .map(node -> node.get("code").asText())
                    .collect(Collectors.toSet());
        } else {
            System.out.println("Something went wrong trying to get emoticon set: " + emoteSet);
        }
        return Set.of();
    }

    /**
     * Gets the emoticon set for BTTV, please not that this is not a call to the official twitch APi
     * @return
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
        PrettyPrinter.prettyPrintJSonNode(rootNode);
        if (rootNode != null && rootNode.has("created_at")) {
            return LocalDateTime.ofInstant(Instant.parse(rootNode.get("created_at").asText()), ZoneId.systemDefault());
        } else {
            System.out.println("Error getting sub streak start: " + rootNode);
            return null;
        }
    }


    public static String getAuthTokenForBobsChannel() { return AuthTokenForBobsChannel; }

    public synchronized static JsonNode executeHttpGet(String requestURIString) {
        try {
            URI requestURI = new URI(requestURIString);
            HttpRequest getRequest = HttpRequest.newBuilder(requestURI)
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

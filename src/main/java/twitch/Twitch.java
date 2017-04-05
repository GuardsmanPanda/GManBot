package twitch;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import core.GBUtility;
import org.apache.hc.client5.http.impl.sync.HttpClientBuilder;
import org.apache.hc.client5.http.methods.HttpGet;
import org.apache.hc.client5.http.sync.HttpClient;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.*;
import java.util.stream.StreamSupport;

/**
 * For interacting with the Twitch API
 */
public class Twitch {
    private static final HttpClient client = HttpClientBuilder.create().build();

    public static void main(String[] args) {
        System.out.println(getTwitchUserID("dark050"));
    }

    public static boolean isStreamOnline(String twitchName, boolean defaultAssumption) {
        JsonNode root = executeHttpGet(new HttpGet("https://api.twitch.tv/kraken/streams/" + twitchName));
        if (root.has("stream") && root.get("stream").isNull()) {
            //stream exists but is not online
            return false;
        } else if (root.has("stream") && !root.get("stream").isNull()) {
            //Stream exists and is online
            return true;
        } else if (root.has("error") && root.get("error").asText().equalsIgnoreCase("not found")) {
            //Stream does not exist
            return false;
        } else if (root.has("error") && root.has("message") && root.get("message").asText().equalsIgnoreCase("channel '" + twitchName + "' is unavailable")) {
            //If the channel is unavailible we treat it as being offline
            return false;
        } else {
            //else we do not understand the reply, and thus we make the assumption that the stream is online
            System.out.println("Unknown reply for stream online status!!");
            System.out.println(root.toString());
            return defaultAssumption;
        }

    }
    public static HashSet<String> getSubscriberEmoticons(String twitchChannelName) {
        HashSet<String> returnSet = new HashSet<>();
        JsonNode rootNode = executeHttpGet(new HttpGet("https://api.twitch.tv/kraken/chat/guardsmanbob/emoticons"));
        StreamSupport.stream(rootNode.get("emoticons").spliterator(), false)
                .filter(node -> node.get("subscriber_only").asBoolean())
                .forEach(node -> returnSet.add(node.get("regex").asText()));
        return returnSet;
    }
    public static String getTwitchUserID(String twitchName) {
        JsonNode rootNode = executeHttpGet(new HttpGet("https://api.twitch.tv/kraken/users/" + twitchName));
        if (rootNode.has("_id")) return rootNode.get("_id").asText();
        else return "";
    }
    public static String getCurrentGame(String twitchName) {
        JsonNode rootNode = executeHttpGet(new HttpGet("https://api.twitch.tv/kraken/channels/" + twitchName));
        return rootNode.get("game").asText();
    }
    public static String getCurrentTitle(String twitchName) {
        JsonNode rootNode = executeHttpGet(new HttpGet("https://api.twitch.tv/kraken/channels/" + twitchName));
        return rootNode.get("status").asText();
    }
    /**
     * Returns the follower count of the given channel, returns 0 if the channel does not exist.
     * @param twitchName Name of the channel on Twitch
     * @return
     */
    public static int getFollowerCount(String twitchName) {
        JsonNode rootNode = executeHttpGet(new HttpGet("https://api.twitch.tv/kraken/channels/" + twitchName));

        if (rootNode.has("followers")) return rootNode.get("followers").asInt();
        else if (rootNode.has("error")) {
            //If the channel does not exist, return 0
            return 0;
        }
        else {
            //Twitch API reply was not what was expected.
            System.out.println("Unexpected API reply when trying to get follower count for: " + twitchName);
            return 0;
        }
    }
    /**
     * Gets top games on twitch starting from offset (exclusive)
     * @param numberOfGames value between 1 and 100
     * @param offset value starting from 0
     * @return
     */
    public static Map<String, Integer> getTopGamesAndNumberOfChannels(int numberOfGames, int offset) {
        HashMap<String, Integer> returnMap = new HashMap<>();
        JsonNode rootNode = executeHttpGet(new HttpGet("https://api.twitch.tv/kraken/games/top?limit=" + numberOfGames + "&offset=" + offset));
        StreamSupport.stream(rootNode.get("top").spliterator(), false)
                .forEach(gameNode -> {
                    returnMap.put(gameNode.get("game").get("name").asText(), gameNode.get("channels").asInt());
                    //System.out.println(gameNode.get("channels").asInt() + " people streaming and " + gameNode.get("viewers") + " people watching " + gameNode.get("game").get("name").asText());
                });
        return returnMap;
    }
    /**
     * Search for streams with a given quarry
     */
    public static List<String> searchStreams(String searchQuarry, int limit, int offset) {
        try {
            String searchString = "https://api.twitch.tv/kraken/search/streams?q=" + URLEncoder.encode(searchQuarry, "UTF-8") + "&limit=" + limit + "&offset=" + offset;
            System.out.println(searchString);
            JsonNode rootNode = executeHttpGet(new HttpGet(searchString));
            List<String> returnList = new ArrayList<>();
            StreamSupport.stream(rootNode.get("streams").spliterator(), false)
                    .forEach(stream -> {
                        returnList.add(stream.get("channel").get("name").asText());
                    });
            if (returnList.isEmpty()) System.out.println("No streams found for game search: " + searchQuarry);
            return returnList;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            throw new RuntimeException("God damn it java");
        }
    }
    public static List<String> getListOfStreamsForGame(String game, int limit, int offset) {
        try {
            String searchString = "https://api.twitch.tv/kraken/streams?game=" + URLEncoder.encode(game, "UTF-8") + "&limit=" + limit + "&offset=" + offset;
            System.out.println(searchString);
            JsonNode rootNode = executeHttpGet(new HttpGet(searchString));
            List<String> returnList = new ArrayList<>();
            StreamSupport.stream(rootNode.get("streams").spliterator(), false)
                    .forEach(stream -> {
                        returnList.add(stream.get("channel").get("name").asText());
                    });
            if (returnList.isEmpty()) System.out.println("No streams found for game: " + game);
            return returnList;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            throw new RuntimeException("God damn it java");
        }
    }


    public static void setStreamTitle() {

    }

    public static void setHost() {

    }

    public static void removeHost() {

    }

    private synchronized static JsonNode executeHttpGet(HttpGet get) {
        get.addHeader("Client-ID", "affm09r2tf22mjgm3pw46wvjhgf9bkz");
        try(InputStream input = client.execute(get).getEntity().getContent()) {
            JsonNode rootNode = new ObjectMapper().readTree(input);
            get.releaseConnection();

            /* ignore errors for now
            if (rootNode.has("error")) {
                System.out.println("Error encountered sending GET request to the twitch api");
                System.out.println(rootNode.toString());
            }
            */
            return rootNode;
        } catch (IOException e) {
            //TODO: in case of IO error we really should return an empty node with a simple error entry instead
            e.printStackTrace();
        }
        return null;
    }
}

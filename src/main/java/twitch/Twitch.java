package twitch;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.hc.client5.http.impl.sync.HttpClientBuilder;
import org.apache.hc.client5.http.methods.HttpGet;
import org.apache.hc.client5.http.sync.HttpClient;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.stream.StreamSupport;

/**
 * For interacting with the Twitch API
 */
public class Twitch {
    private static final HttpClient client = HttpClientBuilder.create().build();

    public static void main(String[] args) {
        //System.out.println(Arrays.toString(getSubscriberEmoticons("guardsmanbob").toArray()));
        System.out.println(getTwitchUserID("gmanbot"));
        //System.out.println(executeHttpGet(new HttpGet("https://api.twitch.tv/kraken/channels/kusieru/stream_key")).toString());
    }

    public synchronized static boolean isStreamOnline(String twitchName, boolean defaultAssumption) {
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

    public synchronized static HashSet<String> getSubscriberEmoticons(String channelName) {
        HashSet<String> returnSet = new HashSet<>();
        JsonNode rootNode = executeHttpGet(new HttpGet("https://api.twitch.tv/kraken/chat/guardsmanbob/emoticons"));
        StreamSupport.stream(rootNode.get("emoticons").spliterator(), false)
                .filter(node -> node.get("subscriber_only").asBoolean())
                .forEach(node -> returnSet.add(node.get("regex").asText()));
        return returnSet;
    }

    public synchronized static String getTwitchUserID(String twitchName) {
        JsonNode rootNode = executeHttpGet(new HttpGet("https://api.twitch.tv/kraken/users/" + twitchName));
        if (rootNode.has("_id")) return rootNode.get("_id").asText();
        else return "";
    }

    public synchronized static String getCurrentGame(String twitchName) {
        JsonNode rootNode = executeHttpGet(new HttpGet("https://api.twitch.tv/kraken/channels/" + twitchName));
        return rootNode.get("game").asText();
    }

    public synchronized static String getCurrentTitle(String twitchName) {
        JsonNode rootNode = executeHttpGet(new HttpGet("https://api.twitch.tv/kraken/channels/" + twitchName));
        return rootNode.get("status").asText();
    }

    /**
     * Returns the follower count of the given channel, returns 0 if the channel does not exist.
     * @param twitchName Name of the channel on Twitch
     * @return
     */
    public synchronized static int getFollowerCount(String twitchName) {
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

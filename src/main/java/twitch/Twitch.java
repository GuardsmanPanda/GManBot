package twitch;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.hc.client5.http.impl.sync.HttpClientBuilder;
import org.apache.hc.client5.http.methods.HttpGet;
import org.apache.hc.client5.http.sync.HttpClient;

import java.io.IOException;
import java.io.InputStream;

/**
 * For interacting with the Twitch API
 */
public class Twitch {
    private static final HttpClient client = HttpClientBuilder.create().build();

    public static void main(String[] args) {
        System.out.println(isStreamOnline("clock888"));
    }

    // {"error":"Not Found","message":"Channel 'thistreamdoesntexist3242rw3rwfsefsefsefsefwefqwerrqwe' does not exist","status":404}
    public synchronized static boolean isStreamOnline(String twitchName) {
        JsonNode root = executeHttpGet(new HttpGet("https://api.twitch.tv/kraken/streams/" + twitchName));
        System.out.println(root.toString());
        if (root.has("stream") && root.get("stream").isNull()) {
            //stream exists but is not online
            return false;
        } else if (root.has("stream" ) && !root.get("stream").isNull()) {
            //Stream exists and is online
            return true;
        } else if (root.has("error") && root.get("error").asText().equalsIgnoreCase("not found")) {
            //Stream does not exist
            return false;
        } else {
            //else we do not understand the reply, and thus we make the assumption that the stream is online
            System.out.println("Unknown reply for stream online status!!");
            System.out.println(root.toString());
            return true;
        }

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

            //check for errors
            if (rootNode.has("error")) {
                if (rootNode.get("error").asText().equalsIgnoreCase("Not Found")) return rootNode;
                System.out.println("Error encountered sending GET request tot he twitch api");
                System.out.println(rootNode.toString());
            }
            return rootNode;
        } catch (IOException e) {
            //TODO: in case of IO error we really should return an empty node with a simple error entry instead
            e.printStackTrace();
        }
        return null;
    }
}

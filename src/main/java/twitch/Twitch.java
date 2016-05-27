package twitch;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Charsets;
import com.google.common.io.CharStreams;
import org.apache.commons.lang3.CharSet;
import org.apache.hc.client5.http.impl.sync.HttpClientBuilder;
import org.apache.hc.client5.http.methods.HttpGet;
import org.apache.hc.client5.http.sync.HttpClient;
import sun.misc.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * For interacting with the Twitch API
 */
public class Twitch {
    private static final HttpClient client = HttpClientBuilder.create().build();

    public static void main(String[] args) {
        getFollowerCount("guardsmanbobbobobo");
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

        if (rootNode.has("error")) {
            System.out.println("No channel found for " + twitchName);
            return 0;
        }

        return rootNode.get("followers").asInt();
    }



    public static void setStreamTitle() {

    }

    public static void setHost() {

    }

    public static void removeHost() {

    }

    private synchronized static JsonNode executeHttpGet(HttpGet get) {
        try(InputStream input = client.execute(get).getEntity().getContent()) {
            JsonNode rootNode = new ObjectMapper().readTree(input);
            get.releaseConnection();
            return rootNode;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}

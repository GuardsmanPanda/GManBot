package twitch;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import core.GBUtility;
import org.apache.hc.client5.http.impl.sync.HttpClientBuilder;
import org.apache.hc.client5.http.methods.HttpGet;
import org.apache.hc.client5.http.sync.HttpClient;
import utility.FinalPair;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * For interacting with the Twitch API
 */
public class Twitch {
    private static final HttpClient client = HttpClientBuilder.create().build();

    public static void main(String[] args) {
        getTwitchUserIDAndDisplayNames(List.of("korgothor", "Lucho_hs", "mindlesszombiehs"));
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

    public static Map<String, String> getTwitchUserIDAndDisplayNames(List<String> names) {
        String nameString = names.stream().collect(Collectors.joining(","));
        System.out.println(nameString);
        GBUtility.prettyPrintJSonNode(executeHttpGet(new HttpGet("https://api.twitch.tv/kraken/channels?channel=" + nameString)));
        Map<String, String> returnMap = new HashMap<>();


        return returnMap;
    }

    public static FinalPair<String, String> getTwitchUserIDAndDisplayName(String twitchName) {
        JsonNode rootNode = executeHttpGet(new HttpGet("https://api.twitch.tv/kraken/users/" + twitchName));
        if (rootNode.has("_id")) {
            if (rootNode.has("display_name")) {
                String displayname = rootNode.get("display_name").asText();
                if (displayname.isEmpty()) {
                    System.out.println("Found empty display name for user " + twitchName);
                    GBUtility.prettyPrintJSonNode(rootNode);
                    return new FinalPair<>(rootNode.get("_id").asText(), twitchName);
                } else {
                    return new FinalPair<>(rootNode.get("_id").asText(), displayname);
                }
            } else {
                System.out.println("Found no display name for user " + twitchName);
                GBUtility.prettyPrintJSonNode(rootNode);
                return new FinalPair<>(rootNode.get("_id").asText(), twitchName);
            }

        } else return new FinalPair<>("", twitchName);
    }

    public static String getTwitchUserID(String twitchName) {
        JsonNode rootNode = executeHttpGet(new HttpGet("https://api.twitch.tv/kraken/users/" + twitchName));
        if (rootNode.has("_id")) return rootNode.get("_id").asText();
        else return "";
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
        } catch (JsonParseException jpe) {
            jpe.printStackTrace();
            System.out.println("****** JSON perse exception on " + get.toString());
            return JsonNodeFactory.instance.objectNode();
        } catch (IOException e) {
            //TODO: in case of IO error we really should return an empty node with a simple error entry instead
            e.printStackTrace();
        }
        return null;
    }
}

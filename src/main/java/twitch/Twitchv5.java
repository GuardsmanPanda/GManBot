package twitch;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import core.GBUtility;
import jdk.incubator.http.HttpClient;
import jdk.incubator.http.HttpRequest;
import jdk.incubator.http.HttpResponse;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Twitchv5 {
    private static final HttpClient httpClient = HttpClient.newHttpClient();
    private static final String CHANNELID = "30084132";
    private static String twitchApiKey = "";
    private static String twitchAccessToken = "";

    static {
        Path apiKeyPath = Paths.get("Data/twitchapikey.txt");
        try {
            twitchApiKey = Files.readAllLines(apiKeyPath).get(0);
            twitchAccessToken = Files.readAllLines(Paths.get("Data/twitchoauthtoken.txt")).get(0);
        } catch (IOException e) {
            System.out.println("Expecting api key as first line in file: " + apiKeyPath.toString());
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws URISyntaxException, IOException, InterruptedException {
        System.out.println(getGameTitle());
    }

    public static String getGameTitle() {
        return getGameTitle(CHANNELID);
    }
    public static String getGameTitle(String channelID) {
        JsonNode rootNode = executeHttpGet("https://api.twitch.tv/kraken/channels/" + channelID);
        if (rootNode != null && rootNode.has("game")) return rootNode.get("game").asText();

        else {
            System.out.println("Could not find game for channel + " + channelID);
            return "";
        }
    }

    public static String getDisplayName(String twitchUserID) {
        JsonNode rootNode = executeHttpGet("https://api.twitch.tv/kraken/users/" + twitchUserID);
        if (rootNode.has("display_name")) {
            return rootNode.get("display_name").asText();
        } else {
            System.out.println("Error requesting user ID " + twitchUserID);
            GBUtility.prettyPrintJSonNode(rootNode);
            return "";
        }
    }
    public static LocalDate getFollowDate(String twitchUserID) {
        JsonNode rootNode = executeHttpGet("https://api.twitch.tv/kraken/users/" + twitchUserID + "/follows/channels/30084132");
        if (rootNode.has("created_at")) {
            return LocalDate.parse(rootNode.get("created_at").asText().split("T")[0]);
        } else if (rootNode.has("error") && rootNode.has("message") && rootNode.get("message").asText().equalsIgnoreCase("follow not found")) {
            //if the twitchUserID is not a stream follower return null
            return null;
        } else {
            System.out.println("Something went wrong getting followDateTime for " + twitchUserID);
            GBUtility.prettyPrintJSonNode(rootNode);
            return LocalDate.now();
        }
    }

    public synchronized static JsonNode executeHttpGet(String requestURIString) {
        try {
            URI requestURI  = new URI(requestURIString);
            HttpRequest getRequest = HttpRequest.newBuilder(requestURI)
                    .header("Accept", "application/vnd.twitchtv.v5+json")
                    .header("Client-ID", twitchApiKey)
                    .header("Authorization", "OAuth " + twitchAccessToken)
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

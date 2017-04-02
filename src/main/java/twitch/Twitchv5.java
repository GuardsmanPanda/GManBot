package twitch;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import core.GBUtility;
import jdk.incubator.http.HttpClient;
import jdk.incubator.http.HttpRequest;
import jdk.incubator.http.HttpResponse;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Twitchv5 {
    private static final HttpClient httpClient = HttpClient.newHttpClient();
    private static String twitchApiKey = "";
    static {
        Path apiKeyPath = Paths.get("Data/twitchapikey.txt");
        try {
            twitchApiKey = Files.readAllLines(apiKeyPath).get(0);
        } catch (IOException e) {
            System.out.println("Expecting api key as first line in file: " + apiKeyPath.toString());
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        System.out.println(getDisplayName("39837384"));
    }

    public static String getDisplayName(String twitchUserID) {
        JsonNode rootNode = executeHttpGet("https://api.twitch.tv/kraken/users/" + twitchUserID);
        if (rootNode.has("error")) {
            System.out.println("Error requesting user ID " + twitchUserID);
            return "";
        } else {
            return rootNode.get("display_name").asText();
        }
    }


    public static JsonNode executeHttpGet(String requestURIString) {
        try {
            URI requestURI  = new URI(requestURIString);
            HttpRequest getRequest = HttpRequest.newBuilder(requestURI)
                    .header("Accept", "application/vnd.twitchtv.v5+json")
                    .header("Client-ID", twitchApiKey)
                    .GET().build();
            HttpResponse<String> response = httpClient.send(getRequest, HttpResponse.BodyHandler.asString());

            if (response.statusCode() == 200) {
                return new ObjectMapper().readTree(response.body());
            } else if (response.statusCode() == 400) {
                //Bad request, for example requesting a user that doesn't exist anymore.
                return new ObjectMapper().readTree(response.body());
            } else  {
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

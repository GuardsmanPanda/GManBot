package webapi;

import com.fasterxml.jackson.databind.JsonNode;
import jdk.incubator.http.HttpRequest;
import twitch.TwitchChat;

import java.net.URI;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class Earthquakes {
    private static final Set<String> quakesPrinted = new HashSet<>();

    public static void startQuakeWatch(double magnitude) {
        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(() -> checkQuakes(magnitude), 3, 10, TimeUnit.MINUTES);
    }

    private static void checkQuakes(double magnitude) {
        getLatestEarthquakes(magnitude)
                .filter(node -> !quakesPrinted.contains(node.get("id").asText()))
                .forEach(node -> {
                    JsonNode prop = node.get("properties");
                    String place = prop.get("place").asText("No where");
                    double mag = prop.get("mag").asDouble(0.0);
                    String twitchMessage = "Large EarthQuake ("+mag+") In ";
                    if (mag > 7.0) twitchMessage = "HUGE EarthQuake ⏩️"+mag+"⏪️ In ";
                    if (mag > 8.0) twitchMessage = "HOLY SHIT ITS A BIIG ONE! ⚠️" + mag + "⚠️ Earth Quake in ";
                    TwitchChat.sendMessage(twitchMessage + place + " \uD83D\uDD38 " + prop.get("url").asText() +" \uD83D\uDD38 Type: " + prop.get("type").asText());
                    quakesPrinted.add(node.get("id").asText());
                });
    }


    private static Stream<JsonNode> getLatestEarthquakes(double magnitude) {
        HttpRequest request = HttpRequest.newBuilder(URI.create("https://earthquake.usgs.gov/fdsnws/event/1/query?format=geojson&limit=10")).GET().build();
        JsonNode root = WebClient.getJSonNodeFromRequest(request);
        if (root.has("features")) return StreamSupport.stream(root.get("features").spliterator(), false)
                .filter(node -> node.get("properties").get("mag").asDouble(0.0) > magnitude);
        else return Stream.of();
    }
}

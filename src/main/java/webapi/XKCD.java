package webapi;

import com.fasterxml.jackson.databind.JsonNode;
import jdk.incubator.http.HttpRequest;
import twitch.TwitchChat;

import java.net.URI;
import java.time.Instant;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class XKCD {
    private static int latestComicNumber = 1835;
    private static Random random = new Random();
    private static Instant nextRequestTime = Instant.now();

    public static void watchForNewComics() {
        JsonNode latestComicNode = getLatestComic();
        latestComicNumber = latestComicNode.get("num").asInt();
        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(() -> {
            JsonNode node = getLatestComic();
            if (node.get("num").asInt() != latestComicNumber) {
                latestComicNumber = node.get("num").asInt();
                TwitchChat.sendMessage("New xkcd! 웃웃 " + node.get("title") + " -> https://m.xkcd.com/" + latestComicNumber + "/");
            }
        }, 30, 60, TimeUnit.MINUTES);
    }

    public static void randomXKCDRequest() {
        synchronized (XKCD.class) {
            if (nextRequestTime.isAfter(Instant.now())) return;
            nextRequestTime = Instant.now().plusSeconds(20);
        }
        int comicNumber = random.nextInt(latestComicNumber) + 1;
        TwitchChat.sendMessage("Random xkcd: " + getComicTitle(comicNumber) + " -> https://m.xkcd.com/" + comicNumber + "/");
    }

    private static String getComicTitle(int comic) {
        return WebClient.getJSonAndMapToType("https://xkcd.com/" + comic + "/info.0.json", node ->
                node.get("title").asText("Title Not Found")
        );
    }
    private static JsonNode getLatestComic() {
        return WebClient.getJSonNodeFromRequest(HttpRequest.newBuilder(URI.create("https://xkcd.com/info.0.json")).GET().build());
    }
}

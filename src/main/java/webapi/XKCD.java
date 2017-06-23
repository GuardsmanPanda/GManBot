package webapi;

import com.fasterxml.jackson.databind.JsonNode;
import jdk.incubator.http.HttpRequest;
import twitch.TwitchChat;
import utility.Extra;

import java.net.URI;
import java.time.Instant;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class XKCD {
    private static int latestComicNumber = 1835;
    private static String latestComicTitle = "";
    private static Instant nextRequestTime = Instant.now();

    public static void watchForNewComics() {
        JsonNode latestComicNode = getLatestComic();
        latestComicNumber = latestComicNode.get("num").asInt();
        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(() -> {
            JsonNode node = getLatestComic();
            if (node.get("num").asInt() != latestComicNumber) {
                latestComicNumber = node.get("num").asInt();
                latestComicTitle = node.get("title").asText("No Title Found");
                TwitchChat.sendMessage("New xkcd! 웃웃 " + node.get("title") + " -> https://m.xkcd.com/" + latestComicNumber + "/");
            }
        }, 1, 90, TimeUnit.MINUTES);
    }

    public static void xkcdRequest(boolean randomComic) {
        synchronized (XKCD.class) {
            if (nextRequestTime.isAfter(Instant.now())) return;
            nextRequestTime = Instant.now().plusSeconds(8);
        }
        int comicNumber = (randomComic) ? Extra.randomInt(latestComicNumber + 1) : latestComicNumber;
        String response = (randomComic) ? "Random xkcd: " + getComicTitle(comicNumber) : "Latest xkcd: " +latestComicTitle;
        TwitchChat.sendMessage(response + " -> https://m.xkcd.com/" + comicNumber + "/");
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

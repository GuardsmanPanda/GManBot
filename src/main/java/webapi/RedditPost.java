package webapi;

import com.fasterxml.jackson.databind.JsonNode;

public class RedditPost {
    public final int score;
    public final String title;
    public final String url;

    public RedditPost(JsonNode node) {
        JsonNode dataNode = node.get("data");
        score = dataNode.get("score").asInt(0);
        title = dataNode.get("title").asText("No Title");
        url = dataNode.get("url").asText("No Url Found");
    }
}

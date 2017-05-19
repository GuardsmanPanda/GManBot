package webapi.dataobjects;

import com.fasterxml.jackson.databind.JsonNode;

public class RedditPost {
    public final int score;
    public final String title;
    public final String url;
    public final String redditUrl;
    public final String subReddit;


    public RedditPost(JsonNode node) {
        JsonNode dataNode = node.get("data");
        score = dataNode.get("score").asInt(0);
        subReddit = dataNode.get("subreddit").asText("none");
        title = dataNode.get("title").asText("No Title");
        url = dataNode.get("url").asText("No Url Found");
        redditUrl = "https://www.reddit.com" + dataNode.get("permalink").asText("/r/failed");
    }

    public String getBestUrl(boolean fromAll) {
        if (fromAll) return redditUrl;
        else if (subReddit.equalsIgnoreCase("aww")) return url;
        else if (subReddit.equalsIgnoreCase("earthporn")) return url;
        else return redditUrl;
    }
}

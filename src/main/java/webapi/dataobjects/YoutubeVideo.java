package webapi.dataobjects;

import com.fasterxml.jackson.databind.JsonNode;
import utility.PrettyPrinter;

import java.text.NumberFormat;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Locale;


public class YoutubeVideo {
    private static final NumberFormat intFormat = NumberFormat.getIntegerInstance(Locale.getDefault());
    public final LocalDateTime uploadTime;
    public final Duration duration;
    public final String title;
    public final int views;
    public final int likes;
    public final int dislikes;


    public YoutubeVideo(JsonNode node) {
        uploadTime = LocalDateTime.ofInstant(Instant.parse(node.get("snippet").get("publishedAt").asText()), ZoneId.systemDefault());
        title = node.get("snippet").get("title").asText("No Title Found");

        views =  node.get("statistics").get("viewCount").asInt(0);
        likes = node.get("statistics").get("likeCount").asInt(0);
        dislikes = node.get("statistics").get("dislikeCount").asInt(0);

        duration = Duration.parse(node.get("contentDetails").get("duration").asText());
    }

    public String getLength() { return PrettyPrinter.shortTimeFromDuration(duration); }
    public String viewsAndLikes() {
        String returnString = intFormat.format(views) + " Views";
        if (likes == 1) returnString += " \uD83D\uDD38 1 Like";
        else if (likes > 1) returnString += " \uD83D\uDD38 " + intFormat.format(likes) + " Likes [" + Math.round(((float)100*likes)/(likes+dislikes)) + "%]";
        return returnString;
    }
}

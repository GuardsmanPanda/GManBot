package webapi;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import twitch.TwitchChat;
import utility.PrettyPrinter;
import webapi.dataobjects.YoutubeVideo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.Period;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Youtube {
    private static String googleAPIKey = "";
    private static final String videoIdRegex = "^((?:https?:)?//)?((?:www|m)\\.)?((?:youtube\\.com|youtu\\.be))(/(?:[\\w\\-]+\\?v=|embed/|v/)?)([\\w\\-]+)(\\S+)?$";
    private static final Pattern idRegexPattern = Pattern.compile(videoIdRegex);

    static {
        try {
            googleAPIKey = Files.readAllLines(Paths.get("Data/GoogleAPI.txt")).get(0);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void sendVideoInformationFromMessage(String message) {
        String[] wordArray = message.split(" ");
        String videoID = "";

        for (String word : wordArray) {
            Matcher matcher = idRegexPattern.matcher(word);
            if (matcher.find()) {
                videoID = matcher.group(5);
            }
        }

        if (!videoID.isEmpty()) {
            YoutubeVideo video = getVideoFromID(videoID);
            if (video != null) {
                String ageString = "Uploaded ";
                Period periodSinceUpload = Period.between(video.uploadTime.toLocalDate(), LocalDate.now());
                if (periodSinceUpload.getDays() == 0) ageString += "Today!";
                else ageString += PrettyPrinter.timeStringFromPeriod(periodSinceUpload) + " Ago";

                TwitchChat.sendMessage("YouTube ⏩ " + video.title + " - " + video.getLength() + " \uD83D\uDD38 " + video.viewsAndLikes() + " \uD83D\uDD38 " + ageString + " (" + videoID + ")");
            }
        }
    }

    private static YoutubeVideo getVideoFromID(String videoID) {
        JsonNode root = getJsonFromURL("https://www.googleapis.com/youtube/v3/videos?part=snippet%2CcontentDetails%2Cstatistics&id=" + videoID + "&key=" + googleAPIKey);
        if (root != null && root.has("kind") && root.get("kind").asText().equalsIgnoreCase("youtube#videoListResponse")) {
            assert (root.get("pageInfo").get("totalResults").asInt() == 1);
            return new YoutubeVideo(root.get("items").get(0));
        }
        return null;
    }

    private static JsonNode getJsonFromURL(String url) {
        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setRequestMethod("GET");
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                return new ObjectMapper().readTree(reader.lines().collect(Collectors.joining()));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}

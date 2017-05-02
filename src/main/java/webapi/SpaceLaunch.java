package webapi;


import com.fasterxml.jackson.databind.JsonNode;
import jdk.incubator.http.HttpRequest;
import twitch.TwitchChat;
import utility.PrettyPrinter;

import java.net.URI;
import java.time.*;

public class SpaceLaunch {
    //TODO store when to fire next
    private static Instant nextChatMessageTime = Instant.now();

    public static void main(String[] args) {
        nextSpaceLaunchRequest();
    }


    public static void nextSpaceLaunchRequest() {
        if (nextChatMessageTime.isBefore(Instant.now())) return;
        nextChatMessageTime = Instant.now().plusSeconds(120);

        JsonNode nextLaunchNode = getNextLaunchNode("any");

        if (nextLaunchNode != null) {
            LocalDateTime launchTime = LocalDateTime.ofInstant(Instant.ofEpochSecond(nextLaunchNode.get("netstamp").asLong()), ZoneId.systemDefault());
            Duration durationToLaunch = Duration.between(LocalDateTime.now(), launchTime);

            String chatString = "Next Space Launch: " + PrettyPrinter.timeStringFromDuration(durationToLaunch) + "!";
            chatString += " \uD83D\uDE80\uD83D\uDE80 " + nextLaunchNode.get("name").asText();

            JsonNode missionNode = nextLaunchNode.get("missions");
            if (missionNode.has(0))  chatString += " \uD83D\uDE80 Mission Type: " + missionNode.get(0).get("typeName").asText();

            TwitchChat.sendMessage(chatString);

            if (durationToLaunch.toHours() < 2) {
                //TODO: print video URL
            }
        }

    }


    private static JsonNode getNextLaunchNode(String agency) {
        HttpRequest request = HttpRequest.newBuilder(URI.create("https://launchlibrary.net/1.2/launch/next/1"))
                .header("User-Agent","Mozilla/5.0 (Windows NT 6.1; WOW64; rv:54.0) Gecko/20100101 Firefox/54.0")
                .GET().build();
        JsonNode rootNode = WebClient.getJSonNodeFromRequest(request);
        if (rootNode.has("launches")) {
            return rootNode.get("launches").elements().next();
        }
        return null;
    }
}

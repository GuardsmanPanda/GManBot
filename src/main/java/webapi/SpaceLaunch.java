package webapi;

import com.fasterxml.jackson.databind.JsonNode;
import jdk.incubator.http.HttpRequest;
import twitch.TwitchChat;
import utility.GBUtility;
import utility.PrettyPrinter;

import java.net.URI;
import java.time.*;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.StreamSupport;


//emotestats  store emote events to db... wait... run stats on db ->
//account name merge from old twitch name
// overlay voting // game vivewer option selection something
public class SpaceLaunch {
    private static LocalDateTime nextLaunchTime = LocalDateTime.MAX;
    private static Instant nextChatMessageTime = Instant.now();
    private static JsonNode nextLaunchNode;

    static {
        updateCurrentLaunchNode();
    }

    public static void main(String[] args) {
        nextSpaceLaunchRequest();
    }

    public static void startLaunchChecker() {
        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(SpaceLaunch::checkNotify, 5, 40, TimeUnit.MINUTES);
    }

    //TODO add notification when launch is ~1hour away.
    public static synchronized void nextSpaceLaunchRequest() {
        if (nextChatMessageTime.isAfter(Instant.now())) {
            System.out.println("Space launch command muffled");
            return;
        }
        nextChatMessageTime = Instant.now().plusSeconds(120);
        printLaunchInformationToTwitchChat(nextLaunchNode);
    }

    private static void checkNotify() {
        Duration timeUntilLaunch = Duration.between(LocalDateTime.now(), nextLaunchTime);
        if (timeUntilLaunch.toHours() < 1) {
            Duration timeSinceLastLaunchRequest = Duration.between(nextChatMessageTime, Instant.now());
            if (timeSinceLastLaunchRequest.toHours() >= 1) {
                printLaunchInformationToTwitchChat(nextLaunchNode);
            }
        }

    }

    private static void printLaunchInformationToTwitchChat(JsonNode launchNode) {
        LocalDateTime launchTime = LocalDateTime.ofInstant(Instant.ofEpochSecond(launchNode.get("netstamp").asLong()), ZoneId.systemDefault());
        Duration durationToLaunch = Duration.between(LocalDateTime.now(), launchTime);

        String chatString = "Next Space Launch: " + PrettyPrinter.timeStringFromDuration(durationToLaunch) + "!";
        chatString += " \uD83D\uDE80\uD83D\uDE80 " + launchNode.get("name").asText();

        JsonNode missionNode = launchNode.get("missions");
        if (missionNode.has(0))  chatString += " \uD83D\uDE80 Mission Type: " + missionNode.get(0).get("typeName").asText();

        if (launchNode.get("status").asInt() == 1) chatString += " \uD83D\uDE80 Launch Is GO!";

        TwitchChat.sendMessage(chatString);

        if (durationToLaunch.toHours() < 1) {
            StreamSupport.stream(launchNode.get("vidURLs").spliterator(), false)
                    .limit(2)
                    .map(JsonNode::asText)
                    .forEach(urlString -> TwitchChat.sendMessage("WebCast: " + urlString));
        }
    }

    private synchronized static void updateCurrentLaunchNode() {
        JsonNode launchNode = getNextLaunchNode("any");
        if (launchNode != null) {
            nextLaunchNode = launchNode;
            nextLaunchTime = LocalDateTime.ofInstant(Instant.ofEpochSecond(nextLaunchNode.get("netstamp").asLong()), ZoneId.systemDefault());
        }
    }

    private static JsonNode getNextLaunchNode(String agency) {
        HttpRequest request = HttpRequest.newBuilder(URI.create("https://launchlibrary.net/1.2/launch/next/1"))
                .header("User-Agent","Mozilla/5.0 (Windows NT 6.1; WOW64; rv:54.0) Gecko/20100101 Firefox/54.0")
                .header("Keep-Alive", "timeout=30")
                .GET().build();
        JsonNode rootNode = WebClient.getJSonNodeFromRequest(request);
        if (rootNode.has("launches")) {
            return rootNode.get("launches").elements().next();
        } else {
            System.out.println("Something weird with root node");
            GBUtility.prettyPrintJSonNode(rootNode);
        }
        return null;
    }
}

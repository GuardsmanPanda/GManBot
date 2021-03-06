package webapi;

import com.fasterxml.jackson.databind.JsonNode;
import core.StreamWebOverlay;
import jdk.incubator.http.HttpRequest;
import twitch.TwitchChat;
import utility.PrettyPrinter;

import java.net.URI;
import java.time.*;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.StreamSupport;


//TODO: implement !lastspacelaunch to lookup how the last launch went.
public class SpaceLaunch {
    private static LocalDateTime nextLaunchTime = LocalDateTime.MAX;
    private static Instant nextChatMessageTime = Instant.now().plusSeconds(8);
    private static JsonNode nextLaunchNode;

    static {
        updateCurrentLaunchNode();
    }

    public static void main(String[] args) {
        //printLaunchSuccess(getPreviousLaunch());
        PrettyPrinter.prettyPrintJSonNode(getNextLaunchNode("any", 2));
    }

    public static void startLaunchChecker() {
        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(SpaceLaunch::checkNotify, 5, 30, TimeUnit.MINUTES);
    }

    public static synchronized void spaceLaunchRequest(String agency) {
        if (nextChatMessageTime.isAfter(Instant.now())) return;
        nextChatMessageTime = Instant.now().plusSeconds(8);

        if (agency.equalsIgnoreCase("spacex")) {
            printLaunchInformationToTwitchChat(getNextLaunchNode("spacex", 1), "Next SpaceX Launch! -> ");
        } else if (agency.equalsIgnoreCase("next") && nextLaunchTime.isBefore(LocalDateTime.now())) {
            printLaunchInformationToTwitchChat(getNextLaunchNode("any", 2), "Space Launch After This! -> ");
        } else {
            updateCurrentLaunchNode();
            printLaunchInformationToTwitchChat(nextLaunchNode, "Next Space Launch! -> ");
        }
    }

    private static void checkNotify() {
        updateCurrentLaunchNode();
        Duration timeUntilLaunch = Duration.between(LocalDateTime.now(), nextLaunchTime);

        //If we are under 5 hours to launch send headline to overlay;
        if (timeUntilLaunch.toHours() < 5) {
            String launchHeadline = "\uD83D\uDE80 Space Launch In: " + PrettyPrinter.timeStringFromDuration(timeUntilLaunch) + " \uD83D\uDE80";
            StreamWebOverlay.sendHeadlineToOverlay(launchHeadline, "");
        }

        if (timeUntilLaunch.toHours() < 1) {
            Duration timeSinceLastLaunchRequest = Duration.between(nextChatMessageTime, Instant.now());
            if (timeSinceLastLaunchRequest.toHours() >= 1) {
                nextChatMessageTime = Instant.now().plusSeconds(8);
                printLaunchInformationToTwitchChat(nextLaunchNode, "Space Launch Warning! -> ");
            }
        }

    }

    private static void printLaunchInformationToTwitchChat(JsonNode launchNode, String chatString) {
        LocalDateTime launchTime = LocalDateTime.ofInstant(Instant.ofEpochSecond(launchNode.get("netstamp").asLong()), ZoneId.systemDefault());
        Duration durationToLaunch = Duration.between(LocalDateTime.now(), launchTime);

        if (launchNode.get("netstamp").asLong() == 0) {
            chatString += "TBD: " + launchNode.get("net").asText().split(",")[0] + ".";
        } else {
            chatString += "T- " + PrettyPrinter.timeStringFromDuration(durationToLaunch) + "!";
        }
        chatString += " \uD83D\uDE80\uD83D\uDE80 " + launchNode.get("name").asText();

        JsonNode missionNode = launchNode.get("missions");
        if (missionNode.has(0))
            chatString += " \uD83D\uDE80 Mission Type: " + missionNode.get(0).get("typeName").asText();

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
        JsonNode launchNode = getNextLaunchNode("any", 1);
        if (launchNode != null) {
            nextLaunchNode = launchNode;
            if (nextLaunchNode.get("netstamp").asLong() != 0) {
                nextLaunchTime = LocalDateTime.ofInstant(Instant.ofEpochSecond(nextLaunchNode.get("netstamp").asLong()), ZoneId.systemDefault());
            }
        }
    }

    /**
     * stream(launchnodes) -> rocket -> list(agencies) -> name -> if nmae= spacex
     *
     * @param agency
     * @param nodeNumber number of the node to get 1 = first node
     * @return
     */
    private static JsonNode getNextLaunchNode(String agency, int nodeNumber) {
        assert (nodeNumber < 2);
        HttpRequest request = HttpRequest.newBuilder(URI.create("https://launchlibrary.net/1.2/launch/next/20"))
                .header("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:54.0) Gecko/20100101 Firefox/54.0")
                .header("Keep-Alive", "timeout=60")
                .GET().build();

        JsonNode rootNode = WebClient.getJSonNodeFromRequest(request);

        if (rootNode != null && agency.equalsIgnoreCase("any") && rootNode.has("launches")) {
            return rootNode.get("launches").get(nodeNumber - 1);
        } else if (rootNode != null && agency.equalsIgnoreCase("spacex") && rootNode.has("launches")) {
            Optional<JsonNode> matchedLaunchNode = StreamSupport.stream(rootNode.get("launches").spliterator(), false)
                    .filter(launchNode -> StreamSupport.stream(launchNode.get("rocket").get("agencies").spliterator(), false)
                            .anyMatch(agencyNode -> agencyNode.get("name").asText().equalsIgnoreCase(agency)))
                    .findFirst();
            if (matchedLaunchNode.isPresent()) return matchedLaunchNode.get();
        }
        System.out.println("Something weird with root node");
        PrettyPrinter.prettyPrintJSonNode(rootNode);
        return null;
    }

    /**
     * Gets previous launch node
     * @return JsonNode of previous launch
     */
    private static JsonNode getPreviousLaunch() {
        LocalDate thisDate = LocalDate.now();
        LocalDate lastDate = thisDate.minusYears(1);

        HttpRequest request = HttpRequest.newBuilder(URI.create("https://launchlibrary.net/1.2/launch/"
                + lastDate.toString() + "/" + thisDate.toString() //From year back to this date
                + "?limit=1"        // How many to return, in this case 1
                + "&sort=desc"))    // In descending order largest (latest) date first
                .header("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:54.0) Gecko/20100101 Firefox/54.0")
                .header("Keep-Alive", "timeout=60")
                .GET().build();

        return WebClient.getJSonNodeFromRequest(request);
    }

    /**
     * Prints information about nodes success
     * @param node JsonNode which information gets printed
     */
    private static void printLaunchSuccess(JsonNode node) {
        if(node.has("launches")) {

            JsonNode launch = node.get("launches").get(0);
            String launchName = launch.get("name").toString().substring(1, launch.get("name").toString().length() - 1);
            String launchLocation = launch.get("location").get("name").toString().substring(1, launch.get("location").get("name").toString().length() - 1);
            String launchDate = launch.get("net").toString().substring(1, launch.get("net").toString().length() - 1);

            // .substring(1,launch.get("location").get("name").toString().length() - 1) removes first and last characters of the string ("")

            if(launch.get("status").asInt() == 3) {
                // Launch succeeded
                System.out.println("Last space launch: " + launchName + " from " + launchLocation + " on "  +  launchDate + " was a success!");

            } else if(launch.get("status").asInt() == 4){
                // Launch failed
                String failReason = launch.get("failreason").toString().substring(1, launch.get("failreason").toString().length() - 1);
                System.out.println("Last space launch: " + launchName + " from " + launchLocation + " on "  +  launchDate + " " + "failed.");
                // Could add "because of " + failReason"
                // But I'm not sure what kind of failreasons there are and if they are needed.

            } else {
                System.out.println("No launch status information.");
            }

        } else {
            System.out.println("No launches during the past year!");
        }
    }
}

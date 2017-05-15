package twitch;

import com.google.common.collect.ListMultimap;
import database.BobsDatabase;
import database.EmoteDatabase;
import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.events.MessageEvent;

import java.time.Duration;
import java.time.Instant;
import java.util.Comparator;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;


public class TwitchChatStats extends ListenerAdapter {
    public enum StatType {
        IDLEHOURS("The Sneakiest Lurkers"), ACTIVEHOURS("Biggest Hour Farmers"), TOTALEMOTES("Top Emote Users"), CHATLINES("Top Chatters"), BOBCOINS("One Percenters");
        final String statMessage;
        StatType(String message) { statMessage = message; }
    }
    Instant nextStatTime = Instant.now();

    //TODO: Implment !stathide command to hide from all stats, to prevent highlight spam
    @Override
    public void onMessage(MessageEvent event) {
        TwitchChatMessage chatMessage = new TwitchChatMessage(event);
        switch (chatMessage.getMessageCommand()) {
            case "!emotestats": sendEmoteStats(chatMessage, false); break;
            case "!allemotestats": sendEmoteStats(chatMessage, true); break;
            case "!myemotestats": sendPersonalEmoteStats(chatMessage); break;

            case "!activehours": sendTopListStats(StatType.ACTIVEHOURS, true); break;
            case "!idlehours": sendTopListStats(StatType.IDLEHOURS, true); break;
            case "!chatlines": sendTopListStats(StatType.CHATLINES, true); break;
            case "!bobcoins": sendTopListStats(StatType.BOBCOINS, true); break;
            case "!emoteusage": sendTopListStats(StatType.TOTALEMOTES, true); break;

            case "!activehoursinchat": sendTopListStats(StatType.ACTIVEHOURS, false); break;
            case "!idlehoursinchat": sendTopListStats(StatType.IDLEHOURS, false); break;
            case "!chatlinesinchat": sendTopListStats(StatType.CHATLINES, false); break;
            case "!bobcoinsinchat": sendTopListStats(StatType.BOBCOINS, false); break;
            case "!emoteusageinchat": sendTopListStats(StatType.TOTALEMOTES, false); break;
        }
    }


    private void sendTopListStats(StatType statType, boolean everyone) {
        if (nextStatTime.isAfter(Instant.now())) return;
        nextStatTime = Instant.now().plusSeconds(20);

        Set<String> peopleInChat = TwitchChat.getLowerCaseNamesInChannel("#guardsmanbob");
        ListMultimap<String, Integer> names = null;
        switch (statType) {
            case ACTIVEHOURS: names = BobsDatabase.getMultiMapFromSQL("SELECT twitchDisplayName, activeHours FROM twitchChatUsers", String.class, Integer.class); break;
            case IDLEHOURS: names = BobsDatabase.getMultiMapFromSQL("SELECT twitchDisplayName, idleHours FROM twitchChatUsers", String.class, Integer.class); break;
            case CHATLINES: names = BobsDatabase.getMultiMapFromSQL("SELECT twitchDisplayName, chatLines FROM twitchChatUsers", String.class, Integer.class); break;
            case BOBCOINS: names = BobsDatabase.getMultiMapFromSQL("SELECT twitchDisplayName, bobCoins FROM twitchChatUsers", String.class, Integer.class); break;
            case TOTALEMOTES: names = BobsDatabase.getMultiMapFromSQL("SELECT TwitchChatUsers.twitchDisplayName, COUNT(emoteName) AS emoteCount FROM EmoteUsage INNER JOIN TwitchChatUsers ON twitchChatUsers.TwitchUserID = EmoteUsage.TwitchUserID GROUP BY twitchChatUsers.twitchDisplayName", String.class, Integer.class); break;
        }
        if (names == null) {
            TwitchChat.sendMessage("Names Is Null");
            return;
        }
        String statString = names.entries().stream()
                .filter(entry -> everyone || peopleInChat.contains(entry.getKey().toLowerCase()))
                .sorted(Comparator.comparingInt(Map.Entry<String, Integer>::getValue).reversed())
                .map(entry -> entry.getKey() + ": " + entry.getValue())
                .limit(15)
                .collect(Collectors.joining(" \uD83D\uDD38 "));

        TwitchChat.sendMessage(statType.statMessage + ((everyone) ? "! -> " : " Currently in Chat! -> ") + statString);
    }

    //TODO consider condensing this down to 1 method
    private void sendPersonalEmoteStats(TwitchChatMessage chatMessage) {
        String outputString = chatMessage.displayName + " emotes";
        int days = 50000;
        try {
            days = Integer.parseInt(chatMessage.getMessageContent());
            outputString += "for the past " + days + " days";
        } catch (NumberFormatException nfe) { /*empty on purpose*/ }
        outputString += "! ";
        outputString += EmoteDatabase.getEmoteUsageFromUserID(chatMessage.userID, Duration.ofDays(days))
                .sorted(Comparator.comparingInt(Map.Entry<String, Integer>::getValue).reversed())
                .limit(10)
                .map(entry -> entry.getKey() + " " + entry.getValue())
                .collect(Collectors.joining(" ▪️ "));
        TwitchChat.sendMessage(outputString);
    }
    private synchronized void sendEmoteStats(TwitchChatMessage chatMessage, boolean allEmotes) {
        if (nextStatTime.isAfter(Instant.now())) return;
        nextStatTime = Instant.now().plusSeconds(20);

        int days = 14;
        try { days = Integer.parseInt(chatMessage.getMessageContent()); } catch (NumberFormatException nfe) { /*empty on purpose*/ }

        String printString = "Emote usage for the past " + days + " days: ";
        printString += EmoteDatabase.getEmoteUsageByEmoteName(Duration.ofDays(days))
                .filter(entry -> (allEmotes || entry.getKey().startsWith("bob")))
                .sorted(Comparator.comparingInt(Map.Entry<String, Integer>::getValue).reversed())
                .limit(20)
                .map(entry -> entry.getKey() + " " + entry.getValue())
                .collect(Collectors.joining(" \uD83D\uDD38 "));
        TwitchChat.sendMessage(printString);
    }
}

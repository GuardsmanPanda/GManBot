package twitch;

import com.google.common.collect.ListMultimap;
import database.BobsDatabase;
import database.BobsDatabaseHelper;
import database.EmoteDatabase;
import database.SongDatabase;
import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.events.MessageEvent;
import twitch.dataobjects.TwitchChatMessage;

import javax.sql.rowset.CachedRowSet;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

public class TwitchChatStats extends ListenerAdapter {
    public enum StatType {
        IDLEHOURS("The Sneakiest Lurkers"), ACTIVEHOURS("Biggest Hour Farmers"), TOTALEMOTES("Top Emote Users"), CHATLINES("Top Chatters"), BOBCOINS("One Percenters");
        final String statMessage;
        StatType(String message) { statMessage = message; }
    }
    private static final NumberFormat intFormat = NumberFormat.getIntegerInstance(Locale.getDefault());
    private static final Set<String> statHidingUsers = new HashSet<>();
    private Instant nextStatTime = Instant.now();

    static {
       statHidingUsers.addAll(BobsDatabase.getListFromSQL("SELECT twitchLowerCaseName FROM TwitchChatUsers WHERE statHide = TRUE", String.class));
    }

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

            case "!stathide": statHide(chatMessage, true);
            case "!statunhide": statHide(chatMessage, false);

            case "!totalstats": sendTotalStats();
        }
    }

    private void statHide(TwitchChatMessage chatMessage, boolean hide) {
        if (hide) statHidingUsers.add(chatMessage.displayName.toLowerCase());
        else statHidingUsers.remove(chatMessage.displayName.toLowerCase());

        BobsDatabaseHelper.setStatHide(chatMessage.userID, chatMessage.displayName, hide);
    }

    private void sendTopListStats(StatType statType, boolean everyone) {
        synchronized (this) {
            if (nextStatTime.isAfter(Instant.now())) return;
            nextStatTime = Instant.now().plusSeconds(12);
        }

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
                .filter(entry -> !statHidingUsers.contains(entry.getKey().toLowerCase()))
                .sorted(Comparator.comparingInt(Map.Entry<String, Integer>::getValue).reversed())
                .map(entry -> entry.getKey() + ": " + entry.getValue())
                .limit(15)
                .collect(Collectors.joining(" \uD83D\uDD38 "));

        TwitchChat.sendMessage(statType.statMessage + ((everyone) ? "! -> " : " Currently in Chat! -> ") + statString);
    }

    private void sendTotalStats() {
        synchronized (this) {
            if (nextStatTime.isAfter(Instant.now())) return;
            nextStatTime = Instant.now().plusSeconds(12);
        }
        try (CachedRowSet cachedRowSet = BobsDatabase.getCachedRowSetFromSQL("SELECT SUM(chatLines), SUM(activeHours), SUM(idleHours), SUM(bobCoins) FROM TwitchChatUsers")) {
            if (cachedRowSet.next()) {
                String response = "Total Stats -> ";
                response += "ChatLines: " + intFormat.format(cachedRowSet.getInt(1));
                response += " \uD83D\uDD38 ActiveHours: " + intFormat.format(cachedRowSet.getInt(2));
                response += " \uD83D\uDD38 IdleHours: " + intFormat.format(cachedRowSet.getInt(3));
                response += " \uD83D\uDD38 BobCoins: " + intFormat.format(cachedRowSet.getInt(4));
                response += " \uD83D\uDD38 SongRatings: " + intFormat.format(SongDatabase.getTotalSongRatings());
                response += " \uD83D\uDD38 SongQuotes: " + intFormat.format(SongDatabase.getTotalSongQuotes());
                TwitchChat.sendMessage(response);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    //TODO consider condensing this down to 1 method
    private void sendPersonalEmoteStats(TwitchChatMessage chatMessage) {
        String outputString = chatMessage.displayName + " emotes";
        int days = 50000;
        try {
            days = Integer.parseInt(chatMessage.getMessageContent());
            outputString += " for the past " + days + " days";
        } catch (NumberFormatException nfe) { /*empty on purpose*/ }
        outputString += "! ";
        String emoteString = EmoteDatabase.getEmoteUsageFromUserID(chatMessage.userID, Duration.ofDays(days))
                .sorted(Comparator.comparingInt(Map.Entry<String, Integer>::getValue).reversed())
                .limit(10)
                .map(entry -> entry.getKey() + " " + entry.getValue())
                .collect(Collectors.joining(" ▪️ "));
        if (emoteString.isEmpty()) TwitchChat.sendMessage("No Emotes For You!");
        else TwitchChat.sendMessage(outputString + emoteString);
    }
    private synchronized void sendEmoteStats(TwitchChatMessage chatMessage, boolean allEmotes) {
        if (nextStatTime.isAfter(Instant.now())) return;
        nextStatTime = Instant.now().plusSeconds(12);

        int days = 30;
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

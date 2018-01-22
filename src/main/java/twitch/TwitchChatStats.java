package twitch;

import com.google.common.collect.ListMultimap;
import database.*;
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
import java.util.stream.Stream;

public class TwitchChatStats extends ListenerAdapter {
    public enum StatType {
        IDLEHOURS("The Sneakiest Lurkers"), ACTIVEHOURS("Biggest Hour Farmers"), TOTALEMOTES("Top Emote Users"),
        CHATLINES("Top Chatters"), BOBCOINS("One Percenters"), SONGSRATED("Top Song Raters");
        public final String statMessage;
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
            case "!emotestats": emoteStats(chatMessage, false, true); break;
            case "!allemotestats": emoteStats(chatMessage, true, true); break;
            case "!emotestatsinchat": emoteStats(chatMessage, false, false); break;
            case "!allemotestatsinchat": emoteStats(chatMessage, true, false); break;

            case "!commandstats": commandStats(true); break;
            case "!commandstatsinchat": commandStats(false); break;

            case "!myemotestats": personalEmoteStats(chatMessage); break;

            case "!flagstats": flagStats(true); break;
            case "!flagstatsinchat": flagStats(false); break;

            case "!activehours": topListStats(StatType.ACTIVEHOURS, true); break;
            case "!idlehours": topListStats(StatType.IDLEHOURS, true); break;
            case "!chatlines": topListStats(StatType.CHATLINES, true); break;
            case "!bobcoins": topListStats(StatType.BOBCOINS, true); break;
            case "!emoteusage": topListStats(StatType.TOTALEMOTES, true); break;
            case "!songratings": topListStats(StatType.SONGSRATED, true); break;

            case "!activehoursinchat": topListStats(StatType.ACTIVEHOURS, false); break;
            case "!idlehoursinchat": topListStats(StatType.IDLEHOURS, false); break;
            case "!chatlinesinchat": topListStats(StatType.CHATLINES, false); break;
            case "!bobcoinsinchat": topListStats(StatType.BOBCOINS, false); break;
            case "!emoteusageinchat": topListStats(StatType.TOTALEMOTES, false); break;
            case "!songratingsinchat": topListStats(StatType.SONGSRATED, false); break;

            case "!stathide": statHide(chatMessage, true); break;
            case "!statunhide": statHide(chatMessage, false); break;
            case "!totalstats": totalStats(); break;
            case "!donationcheck": donationAmount(chatMessage); break;
        }
    }


    private void statHide(TwitchChatMessage chatMessage, boolean hide) {
        if (hide) statHidingUsers.add(chatMessage.displayName.toLowerCase());
        else statHidingUsers.remove(chatMessage.displayName.toLowerCase());

        BobsDatabaseHelper.setStatHide(chatMessage.userID, chatMessage.displayName, hide);
    }

    private void topListStats(StatType statType, boolean everyone) {
        synchronized (this) {
            if (nextStatTime.isAfter(Instant.now())) return;
            nextStatTime = Instant.now().plusSeconds(8);
        }

        //TODO add songrating and song quote toplsits
        Set<String> peopleInChat = TwitchChat.getLowerCaseNamesInChannel("#guardsmanbob");
        ListMultimap<String, Integer> names = null;
        switch (statType) {
            case ACTIVEHOURS: names = BobsDatabase.getMultiMapFromSQL("SELECT twitchDisplayName, activeHours FROM twitchChatUsers", String.class, Integer.class); break;
            case IDLEHOURS: names = BobsDatabase.getMultiMapFromSQL("SELECT twitchDisplayName, idleHours FROM twitchChatUsers", String.class, Integer.class); break;
            case CHATLINES: names = BobsDatabase.getMultiMapFromSQL("SELECT twitchDisplayName, chatLines FROM twitchChatUsers", String.class, Integer.class); break;
            case BOBCOINS: names = BobsDatabase.getMultiMapFromSQL("SELECT twitchDisplayName, bobCoins FROM twitchChatUsers", String.class, Integer.class); break;
            case TOTALEMOTES: names = BobsDatabase.getMultiMapFromSQL("SELECT TwitchChatUsers.twitchDisplayName, COUNT(emoteName) AS emoteCount FROM EmoteUsage INNER JOIN TwitchChatUsers ON twitchChatUsers.TwitchUserID = EmoteUsage.TwitchUserID GROUP BY twitchChatUsers.twitchDisplayName", String.class, Integer.class); break;
            case SONGSRATED: names = BobsDatabase.getMultiMapFromSQL( "SELECT TwitchChatUsers.twitchDisplayName, COUNT(songName) AS songCount FROM SongRatings INNER JOIN TwitchChatUsers ON twitchChatUsers.TwitchUserID = SongRatings.twitchUserID GROUP BY twitchChatUsers.twitchDisplayName", String.class, Integer.class); break;
        }
        if (names == null) {
            TwitchChat.sendMessage("Names Is Null");
            return;
        }

        Stream<Map.Entry<String,Integer>> stream = names.entries().stream()
                .filter(entry -> everyone || peopleInChat.contains(entry.getKey().toLowerCase()))
                .filter(entry -> !statHidingUsers.contains(entry.getKey().toLowerCase()));

        String statString = stringFromMapEntryStream(stream,17);

        TwitchChat.sendMessage(statType.statMessage + ((everyone) ? "! -> " : " Currently in Chat! -> ") + statString);
    }

    private void totalStats() {
        synchronized (this) {
            if (nextStatTime.isAfter(Instant.now())) return;
            nextStatTime = Instant.now().plusSeconds(8);
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
    private void personalEmoteStats(TwitchChatMessage chatMessage) {
        String outputString = chatMessage.displayName + " emotes";
        int days = 50000;
        try {
            days = Integer.parseInt(chatMessage.getMessageContent());
            outputString += " for the past " + days + " days";
        } catch (NumberFormatException nfe) { /*empty on purpose*/ }
        outputString += "! ";
        String emoteString = stringFromMapEntryStream(EmoteDatabase.getEmoteUsageFromUserID(chatMessage.userID, Duration.ofDays(days)), 15);
        if (emoteString.isEmpty()) TwitchChat.sendMessage("No Emotes For You!");
        else TwitchChat.sendMessage(outputString + emoteString);
    }

    private void emoteStats(TwitchChatMessage chatMessage, boolean allEmotes, boolean everyone) {
        synchronized (this) {
            if (nextStatTime.isAfter(Instant.now())) return;
            nextStatTime = Instant.now().plusSeconds(8);
        }
        int days = 60;
        try { days = Integer.parseInt(chatMessage.getMessageContent()); } catch (NumberFormatException nfe) { /*empty on purpose*/ }

        String printString = "Emote usage" +((everyone)?"":", by people in chat,") + " in the past " + days + " days: ";

        Stream<Map.Entry<String,Integer>> stream = EmoteDatabase.getEmoteUsageByEmoteName(Duration.ofDays(days), everyone)
                .filter(entry -> (allEmotes || entry.getKey().startsWith("bob")));

        printString += stringFromMapEntryStream(stream, 20);
        TwitchChat.sendMessage(printString);
    }

    //TODO add in chat to this
    private void commandStats(boolean everyone) {
        synchronized (this) {
            if (nextStatTime.isAfter(Instant.now())) return;
            nextStatTime = Instant.now().plusSeconds(8);
        }
        int days = 50000;
        String outputString = "Command usage";
        if (!everyone) outputString += ", by people in chat";
        outputString +=": ";

        outputString += stringFromMapEntryStreamLong(ChatLines.commandUsageStats(everyone).entrySet().stream(), 16);
        TwitchChat.sendMessage(outputString);
    }

    private void flagStats(boolean everyone) {
        System.out.println("Flag stats: " + everyone);
        synchronized (this) {
            if (nextStatTime.isAfter(Instant.now())) return;
            nextStatTime = Instant.now().plusSeconds(8);
        }
        String outputString = "Most Used Flags";
        if (!everyone) outputString += " by people in chat";
        outputString += ": ";
        outputString += stringFromMapEntryStream(BobsDatabaseHelper.getFlagStats(everyone).entrySet().stream(), 16);
        TwitchChat.sendMessage(outputString);
    }

    private void donationAmount(TwitchChatMessage chatMessage) {
        int donationCents = BobsDatabaseHelper.getCentsDonated(chatMessage.userID);
        TwitchChat.sendMessage(chatMessage.displayName + " -> Amount Donated: " + donationCents / 100 + "$");
    }

    //TODO convert everything to use long
    private String stringFromMapEntryStreamLong(Stream<Map.Entry<String, Long>> stream, int limit) {
        return stream.sorted(Comparator.comparingLong(Map.Entry<String, Long>::getValue).reversed())
                .limit(limit)
                .map(entry -> entry.getKey() + " " + intFormat.format(entry.getValue()))
                .collect(Collectors.joining(" \uD83D\uDD38 "));
    }

    private String stringFromMapEntryStream(Stream<Map.Entry<String, Integer>> stream, int limit) {
        return stream.sorted(Comparator.comparingInt(Map.Entry<String, Integer>::getValue).reversed())
                .limit(limit)
                .map(entry -> entry.getKey() + " " + intFormat.format(entry.getValue()))
                .collect(Collectors.joining(" \uD83D\uDD38 "));
    }
}

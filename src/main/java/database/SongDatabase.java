package database;

import com.google.common.collect.*;
import javafx.util.Pair;
import twitch.TwitchChat;

import javax.sql.rowset.CachedRowSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class SongDatabase {
    private static Instant nextSongStatUpdate = Instant.now();
    private static final Multiset<String> songsQuoted = HashMultiset.create();
    private static final Multiset<String> songsRated = HashMultiset.create();
    private static final List<String> songsQuotedRank = new ArrayList<>();
    private static final List<String> songsRatedRank = new ArrayList<>();
    private static final Random random = new Random();

    public static void addSongPlay(String songName) {
        int rowsUpdated = BobsDatabase.executePreparedSQL("UPDATE Songs SET lastDatePlayed = CURRENT_DATE, timesPlayed = timesPlayed + 1 WHERE songName = ?", songName);
        if (rowsUpdated == 0) {
            System.out.println("New Song Entry -> " + songName);
            BobsDatabase.executePreparedSQL("INSERT INTO Songs(songName) VALUES(?)", songName);
        }
    }

    public static void addSongRating(String twitchUserID, String songName, int songRating, String songQuote) {
        try (CachedRowSet cachedRowSet = BobsDatabase.getCachedRowSetFromSQL("SELECT * FROM SongRatings WHERE twitchUserID = ? AND songName = ?", twitchUserID, songName)) {
            if (cachedRowSet.next()) {
                if (songQuote.equalsIgnoreCase("none")) songQuote = cachedRowSet.getString("songQuote");
                BobsDatabase.executePreparedSQL("UPDATE SongRatings SET songRating = " + songRating + ", songQuote = ?, ratingTimestamp = '" + Timestamp.valueOf(LocalDateTime.now()) + "' WHERE twitchUserID = ? AND songName = ?", songQuote, twitchUserID, songName);
            } else {
                BobsDatabase.executePreparedSQL("INSERT INTO SongRatings(twitchUserID, songName, songRating, songQuote) VALUES(?, ?, " + songRating + ", ?)", twitchUserID, songName, songQuote);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static Map<String, Double> getSongRatingMap(int minNumberRatings, LocalDate notPlayedSinceDate, boolean everyone) {
        Set<String> peopleInChat = (everyone) ? Set.of() : TwitchChat.getUserIDsInChannel();
        Multimap<String, Integer> songRatings = ArrayListMultimap.create();

        try (CachedRowSet cachedRowSet = BobsDatabase.getCachedRowSetFromSQL("SELECT songRating, songName, twitchUserID FROM SongRatings")) {
            System.out.println("Loaded " + cachedRowSet.size() + " Song Ratings, " + peopleInChat.size() + " People in Chat.");
            while (cachedRowSet.next()) {
                if (everyone || peopleInChat.contains(cachedRowSet.getString("twitchUserID"))) {
                    songRatings.put(cachedRowSet.getString("songName"), cachedRowSet.getInt("songRating"));
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }

        List<String> songsPlayed = BobsDatabase.getListFromSQL("SELECT songName FROM Songs WHERE lastDatePlayed >= '"+notPlayedSinceDate.toString()+"'", String.class);
        System.out.println("Found " + songRatings.keySet().size() + " songs and " + songRatings.size() + " ratings, and " + songsPlayed.size() + " recently played songs");
        songsPlayed.forEach(songRatings::removeAll);
        System.out.println("After removal there are now " + songRatings.keySet().size() + " songs and " + songRatings.size() + " ratings");

        return songRatings.keySet().stream()
                .filter(songName -> songRatings.get(songName).size() >= minNumberRatings)
                .collect(Collectors.toMap(songName -> songName, songName -> ((double) songRatings.get(songName).stream().mapToInt(i -> i).sum()) / songRatings.get(songName).size()));
    }


    public static Pair<Float, Integer> getSongRating(String songName) {
        List<Integer> songRatings = BobsDatabase.getListFromSQL("SELECT songRating FROM SongRatings WHERE songName = ?", Integer.class, songName);
        float rating = (float) songRatings.stream().mapToInt(Integer::intValue).sum() / songRatings.size();
        return new Pair<>(rating, songRatings.size());
    }

    /**
     * Select all song quotes and pick a random one, half the time we want to guarentee that we pick a quote from someone in chat
     * @return a random song quote
     */
    public static String getSongQuote(String songName, boolean nameFirst) {
        Set<String> userIDsInChat = TwitchChat.getUserIDsInChannel();

        List<String> quotesFromEveryone = BobsDatabase.getMultiMapFromSQL("SELECT twitchDisplayName, songQuote FROM SongRatings INNER JOIN TwitchChatUsers ON TwitchChatUsers.TwitchUserID = SongRatings.twitchUserID WHERE songName = ? AND songQuote <> 'none'", String.class, String.class, songName)
                .entries().stream()
                .map(entry -> (nameFirst) ? entry.getKey() + ": " + entry.getValue() : entry.getValue() + " - " + entry.getKey())
                .collect(Collectors.toList());
        List<String> quotesFromChat = quotesFromEveryone.stream().filter(userIDsInChat::contains).collect(Collectors.toList());

        if (quotesFromChat.size() > 0 && random.nextBoolean()) return quotesFromChat.get(random.nextInt(quotesFromChat.size()));
        else if (quotesFromEveryone.size() > 0) return quotesFromEveryone.get(random.nextInt(quotesFromEveryone.size()));
        else return "";
    }

    public static synchronized String getSongRatingStatString(String twitchUserID) {
        if (nextSongStatUpdate.isBefore(Instant.now())) {
            nextSongStatUpdate = Instant.now().plus(Duration.ofMinutes(30));
            updateSongRatingStatistics();
        }
        String returnString = "";
        if (songsRated.count(twitchUserID) > 0)
            returnString += " - SongsRated: " + songsRated.count(twitchUserID) + " [" + (songsRatedRank.indexOf(twitchUserID) + 1) + "]";
        if (songsQuoted.count(twitchUserID) > 0)
            returnString += " \uD83D\uDD38 SongsQuoted: " + songsQuoted.count(twitchUserID) + " [" + (songsQuotedRank.indexOf(twitchUserID) + 1) + "]";
        return returnString;
    }

    private static void updateSongRatingStatistics() {
        System.out.println("Updating Song Rating Stats");
        songsQuoted.clear(); songsRated.clear();
        songsQuotedRank.clear(); songsRatedRank.clear();

        BobsDatabase.getMultiMapFromSQL("SELECT twitchUserID, songQuote FROM SongRatings", String.class, String.class).forEach((key, value) -> {
            songsRated.add(key);
            if (!value.equalsIgnoreCase("none")) songsQuoted.add(key);
        });

        songsRatedRank.addAll(Multisets.copyHighestCountFirst(songsRated).elementSet());
        songsQuotedRank.addAll(Multisets.copyHighestCountFirst(songsQuoted).elementSet());
        System.out.println("Updated Song Rating Stats" + songsRated.size() + " ratings, " + songsQuoted.size() + " quotes");
    }
}

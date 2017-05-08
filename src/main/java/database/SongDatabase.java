package database;

import com.google.common.collect.*;
import javafx.util.Pair;
import twitch.SongAnnouncer;
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
            System.out.println("Loaded " + cachedRowSet.size() + "Song Ratings, " + peopleInChat.size() + " People in Chat.");
            while (cachedRowSet.next()) {
                if (everyone || peopleInChat.contains(cachedRowSet.getString("twitchUserID"))) {
                    songRatings.put(cachedRowSet.getString("songName"), cachedRowSet.getInt("songRating"));
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }

        List<String> songsPlayed = BobsDatabase.getListFromSQL("SELECT songName FROM Songs WHERE lastPlayedDate < '"+notPlayedSinceDate.toString()+"'", String.class);
        System.out.println("Found " + songRatings.keySet().size() + " songs and " + songRatings.size() + " ratings, and " + songsPlayed.size() + " recently played songs");
        songsPlayed.forEach(songRatings::removeAll);
        System.out.println("After removal there are now " + songRatings.keySet().size() + " songs and " + songRatings.size() + " ratings");

        return songRatings.keySet().stream()
                .filter(songName -> songRatings.get(songName).size() >= minNumberRatings + 1)
                .collect(Collectors.toMap(songName -> songName, songName -> ((double) songRatings.get(songName).stream().mapToInt(i -> i).sum()) / songRatings.get(songName).size()));
    }

    /**
     * TODO: Rewrite this whole mess
     * Get the top numberOfSongs as rated by the people currently in the chat, ignore songs which have been played after the end date.
     * TODO: there must be a better way but this works for now. ... MORE TODO: work with the new songs table to lookup last play time
     *
     * @param endDateTime do not get any songs that have been played later than this date.
     */
    public static Map<String, Double> getTopRatedSongsByPeopleInChat(int minRatingAmount, LocalDateTime endDateTime, boolean everyone) {
        CachedRowSet cachedRowSet = BobsDatabase.getCachedRowSetFromSQL("SELECT songRating, songName, twitchDisplayName FROM SongRatings INNER JOIN twitchChatUsers ON twitchChatUsers.twitchUserID = SongRatings.twitchUserID");
        //TODO: enable check for active users .. translate names in chat to userID's in chat
        Set<String> namesInChat = TwitchChat.getLowerCaseNamesInChannel("#guardsmanbob");
        Multimap<String, Integer> songRatings = ArrayListMultimap.create();

        System.out.println("Loaded " + cachedRowSet.size() + " song ratings.");
        System.out.println("Found " + namesInChat.size() + " people in the chat: " + namesInChat.stream().collect(Collectors.joining(", ")));

        try {
            while (cachedRowSet.next()) {
                String displayName = cachedRowSet.getString("twitchDisplayName");
                String songName = cachedRowSet.getString("songName");
                int songRating = cachedRowSet.getInt("songRating");
                if (namesInChat.contains(displayName.toLowerCase()) || everyone)
                    songRatings.put(songName, songRating);
            }
            CachedRowSet recentlyPlayedSongs = BobsDatabase.getCachedRowSetFromSQL("SELECT DISTINCT songName from SongRatings WHERE ratingTimestamp > timestamp('" + Timestamp.valueOf(endDateTime) + "')");
            System.out.println("Found " + songRatings.keySet().size() + " songs and " + songRatings.size() + " ratings, and " + recentlyPlayedSongs.size() + " recently played songs");
            while (recentlyPlayedSongs.next()) songRatings.removeAll(recentlyPlayedSongs.getString("songName"));
            System.out.println("After removal there are now " + songRatings.keySet().size() + " songs and " + songRatings.size() + " ratings");

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return songRatings.keySet().stream()
                .filter(songName -> songRatings.get(songName).size() >= minRatingAmount + 1)
                .collect(Collectors.toMap(
                        songName -> songName,
                        songName -> ((double) songRatings.get(songName).stream().mapToInt(i -> i).sum()) / songRatings.get(songName).size()));
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
        List<String> quotesFromEveryone = new ArrayList<>();
        List<String> quotesFromChat = new ArrayList<>();

        Set<String> lowerCasePeopleInChat = TwitchChat.getLowerCaseNamesInChannel("#guardsmanbob");

        try (CachedRowSet cachedRowSet = BobsDatabase.getCachedRowSetFromSQL("SELECT twitchDisplayName, songQuote FROM SongRatings INNER JOIN TwitchChatUsers ON TwitchChatUsers.TwitchUserID = SongRatings.twitchUserID WHERE songName = ? AND songQuote <> 'none'", songName)) {
            while (cachedRowSet.next()) {
                String quote = cachedRowSet.getString("songQuote");
                String name = cachedRowSet.getString("twitchDisplayName");
                String nameQuote = (nameFirst) ? name + ": " + quote : quote + " - " + name;
                if (lowerCasePeopleInChat.contains(name.toLowerCase())) quotesFromChat.add(nameQuote);
                else quotesFromEveryone.add(nameQuote);
            }
            if (quotesFromChat.size() > 0 && random.nextBoolean())
                return quotesFromChat.get(random.nextInt(quotesFromChat.size()));
            if (quotesFromEveryone.size() > 0) return quotesFromEveryone.get(random.nextInt(quotesFromEveryone.size()));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "";
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
            returnString += ", SongsQuoted: " + songsQuoted.count(twitchUserID) + " [" + (songsQuotedRank.indexOf(twitchUserID) + 1) + "]";
        return returnString;
    }

    private static void updateSongRatingStatistics() {
        System.out.println("Updating Song Rating Stats");
        songsQuoted.clear();
        songsRated.clear();
        songsQuotedRank.clear();
        songsRatedRank.clear();

        try (CachedRowSet cachedRowSet = BobsDatabase.getCachedRowSetFromSQL("SELECT twitchUserID, songQuote FROM SongRatings")) {
            while (cachedRowSet.next()) {
                songsRated.add(cachedRowSet.getString("twitchUserID"));
                if (!cachedRowSet.getString("songQuote").equalsIgnoreCase("none")) {
                    songsQuoted.add(cachedRowSet.getString("twitchUserID"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        songsRatedRank.addAll(Multisets.copyHighestCountFirst(songsRated).elementSet());
        songsQuotedRank.addAll(Multisets.copyHighestCountFirst(songsQuoted).elementSet());
        System.out.println("Updated Song Rating Stats");
    }
}

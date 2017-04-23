package twitch;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import core.BobsDatabase;
import core.GBUtility;

import javax.sql.rowset.CachedRowSet;
import java.awt.*;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class SongDatabase {


    public static void addSongRating(String twitchUserID, String twitchDisplayName, String songName, int songRating, String songQuote) {
        try (CachedRowSet cachedRowSet = BobsDatabase.getCachedRowSetFromSQL("SELECT * FROM SongRatings WHERE twitchUserID = ? AND songName = ?", twitchUserID, songName)) {
            if (cachedRowSet.next()) {
                if (songQuote.equalsIgnoreCase("none")) songQuote = cachedRowSet.getString("songQuote");
                BobsDatabase.executePreparedSQL("UPDATE SongRatings SET twitchDisplayName = ?, songRating = "+songRating+", songQuote = ?, ratingTimestamp = '"+Timestamp.valueOf(LocalDateTime.now())+"' WHERE twitchUserID = ? AND songName = ?", twitchDisplayName, songQuote, twitchUserID, songName);
            } else {
                BobsDatabase.executePreparedSQL("INSERT INTO SongRatings(twitchUserID, twitchDisplayName, songName, songRating, songQuote) VALUES(?, ?, ?, "+songRating+", ?)", twitchUserID, twitchDisplayName, songName, songQuote);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Get the top numberOfSongs as rated by the people currently in the chat, ignore songs which have been played after the end date.
     * TODO: rewrite this in SQL and just return a cachedrowset, this allows us to return only the top x songs
     * @param endDateTime
     * @return
     */
    public static Map<String, Double> getTopRatedSongsByPeopleInChat(int minRatingAmount, LocalDateTime endDateTime, boolean everyone) {
        CachedRowSet cachedRowSet = BobsDatabase.getCachedRowSetFromSQL("SELECT songRating, songName, twitchDisplayName FROM SongRatings");
        //TODO: enable check for active users
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
            System.out.println("Found " + songRatings.keySet().size() + " songs and " + songRatings.size() + " ratings, and "  + recentlyPlayedSongs.size() + " recently played songs");
            while (recentlyPlayedSongs.next()) songRatings.removeAll(recentlyPlayedSongs.getString("songName"));
            System.out.println("After removal there are now " + songRatings.keySet().size() + " songs and " + songRatings.size() + " ratings");

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return songRatings.keySet().stream()
                .filter(songName -> songRatings.get(songName).size() >= minRatingAmount + 1)
                .collect(Collectors.toMap(
                        songName -> songName,
                        songName -> ((double)songRatings.get(songName).stream().mapToInt(i->i).sum()) / songRatings.get(songName).size()));
    }
}
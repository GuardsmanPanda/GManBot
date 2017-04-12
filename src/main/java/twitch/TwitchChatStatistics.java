package twitch;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Multimap;
import core.BobsDatabase;
import core.GBUtility;
import javafx.util.Pair;

import javax.sql.rowset.CachedRowSet;
import java.awt.*;
import java.awt.Robot;
import java.awt.event.InputEvent;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class TwitchChatStatistics {

    public static void main(String[] args) throws InterruptedException, AWTException {
        //GBUtility.prettyPrintCachedRowSet(BobsDatabase.getCachedRowSetFromSQL("SELECT * FROM SongRatings WHERE songName NOT IN (SELECT DISTINCT songName FROM SongRatings WHERE ratingTimestamp > timestamp('" + Timestamp.valueOf(LocalDateTime.now().minusDays(0)) +"')) ORDER BY ratingTimestamp DESC"), 100);
        //GBUtility.prettyPrintCachedRowSet(BobsDatabase.getCachedRowSetFromSQL("SELECT * FROM SongRatings s1 WHERE NOT EXISTS (SELECT songName FROM SongRatings s2 WHERE s1.songName = s2.songName AND s2.ratingTimeStamp > timestamp('" + Timestamp.valueOf(LocalDateTime.now()) + "')) ORDER BY ratingTimestamp DESC"), 100);
        GBUtility.prettyPrintCachedRowSet(BobsDatabase.getCachedRowSetFromSQL("SELECT * FROM SongRatings WHERE ratingTimeStamp > timestamp('" + Timestamp.valueOf(LocalDateTime.now()) + "')"),100);
    }

    public static void printTopRatedSongsByPeopleInChat(LocalDateTime endDateTime) {
        // (String)songName -> (Double)songRating  >>> Print top 20 based on rating
        Map<String, Double> songs = getTopRatedSongsByPeopleInChat(true, endDateTime);
        songs.keySet().stream()
                .sorted(Comparator.comparingDouble(songs::get).reversed())
                .limit(30)
                .forEach(songName -> System.out.println(GBUtility.strictFill(songName, 45) + " " + String.format("%.2f",songs.get(songName))));
    }

    /**
     * Get the top numberOfSongs as rated by the people currently in the chat, ignore songs which have been played after the end date.
     * TODO: rewrite this in SQL and just return a cachedrowset, this allows us to return only the top x songs
     * @param activeUsersOnly
     * @param endDate
     * @return
     */
    private static Map<String, Double> getTopRatedSongsByPeopleInChat(boolean activeUsersOnly, LocalDateTime endDateTime) {
        CachedRowSet cachedRowSet = BobsDatabase.getCachedRowSetFromSQL("SELECT * FROM SongRatings WHERE songName NOT EXISTS (SELECT DISTINCT songName FROM SongRatings WHERE songName = SongRatings.songName AND ratingTimeStamp > timestamp('" + Timestamp.valueOf(endDateTime) + "'))");
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
                if (namesInChat.contains(displayName.toLowerCase()))
                    songRatings.put(songName, songRating);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        System.out.println("Found " + songRatings.keySet().size() + " songs and " + songRatings.size() + " ratings.");
        // songName -> List<Integer> {songRating, songRating, etc..} >>> (sorted map) songNAme -> (double)averageRating
        return songRatings.keySet().stream()
                .filter(songName -> songRatings.get(songName).size() > 8)
                .collect(Collectors.toMap(
                        songName -> songName,
                        songName -> ((double)songRatings.get(songName).stream().mapToInt(i->i).sum()) / songRatings.get(songName).size()));
    }
}

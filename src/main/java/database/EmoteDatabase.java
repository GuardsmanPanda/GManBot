package database;

import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

public class EmoteDatabase {

    public static void main(String[] args) {
        System.out.println(Instant.now());
    }
    public static void addEmoteUsage(String twitchUserID, String emoteName) {
        BobsDatabase.executePreparedSQL("INSERT INTO EmoteUsage(twitchUserID, emoteName, timestamp) VALUES(?, ?, '" + Timestamp.from(Instant.now())+"')", twitchUserID, emoteName);
    }

    public static Stream<Map.Entry<String, Integer>> getEmoteUsageFromUserID(String twitchUserID, Duration timeSpan) {
        return BobsDatabase.getMultiMapFromSQL("SELECT emoteName, Count(*) FROM EmoteUsage WHERE twitchUserID = ? AND timestamp > '" + Timestamp.from(Instant.now().minus(timeSpan)) + "' GROUP BY emoteName", String.class, Integer.class, twitchUserID).entries().stream();
    }

    public static Stream<Map.Entry<String, Integer>> getEmoteUsageByEmoteName(Duration timeSpan) {
        return BobsDatabase.getMultiMapFromSQL("SELECT emoteName, Count(*) FROM EmoteUsage WHERE timestamp > '" + Timestamp.from(Instant.now().minus(timeSpan)) + "'GROUP BY emoteName", String.class, Integer.class).entries().stream();
    }
}

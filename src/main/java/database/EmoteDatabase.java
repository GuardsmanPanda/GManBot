package database;

import com.google.common.base.Strings;

import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

public class EmoteDatabase {
    public static void addEmoteUsage(String twitchUserID, String emoteName) {
        BobsDatabase.executePreparedSQL("INSERT INTO EmoteUsage(twitchUserID, emoteName, timestamp) VALUES(?, ?, '" + Timestamp.from(Instant.now())+"')", twitchUserID, emoteName);
    }

    public static Stream<Map.Entry<String, Integer>> getEmoteUsageFromUserID(String twitchUserID, Duration timeSpan) {
        return BobsDatabase.getMultiMapFromSQL("SELECT emoteName, Count(*) FROM EmoteUsage WHERE twitchUserID = ? AND timestamp > '" + Timestamp.from(Instant.now().minus(timeSpan)) + "' GROUP BY emoteName", String.class, Integer.class, twitchUserID).entries().stream();
    }

    public static Stream<Map.Entry<String, Integer>> getEmoteUsageByEmoteName(Duration timeSpan, Set<String> includeOnly) {
        if (includeOnly.isEmpty()) {
            return BobsDatabase.getMultiMapFromSQL("SELECT emoteName, Count(*) FROM EmoteUsage WHERE timestamp > '" + Timestamp.from(Instant.now().minus(timeSpan)) + "'GROUP BY emoteName", String.class, Integer.class).entries().stream();
        } else {
            return BobsDatabase.getMultiMapFromSQL("SELECT emoteName, Count(*) FROM EmoteUsage WHERE twitchUserID IN ("+ Strings.repeat("?, ", includeOnly.size()-1)+" ?) AND timestamp > '" + Timestamp.from(Instant.now().minus(timeSpan)) + "'GROUP BY emoteName", String.class, Integer.class, includeOnly.toArray(new String[0])).entries().stream();
        }
    }
}

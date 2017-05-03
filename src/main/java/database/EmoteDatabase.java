package database;

import java.sql.Timestamp;
import java.time.Instant;

/**
 * Created by Dons on 03-05-2017.
 */
public class EmoteDatabase {

    public static void addEmoteUsage(String twitchUserID, String emoteName) {
        BobsDatabase.executePreparedSQL("INSERT INTO EmoteUsage(twitchUserID, emoteName, timestamp) VALUES(?, ?, '" + Timestamp.from(Instant.now())+"')", twitchUserID, emoteName);
    }
}

package twitch.dataobjects;

import database.BobsDatabase;
import database.BobsDatabaseHelper;
import utility.FinalPair;
import utility.PrettyPrinter;

import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.time.Period;

public class SeenEvent {
    public final String twitchID;
    public final String targetName;
    private Duration notSeenFor = Duration.ZERO;
    private String lastChatLine = "";
    private String displayName = "";

    public SeenEvent(String twitchName) {
        targetName = twitchName;
        String lowerCaseName = twitchName.toLowerCase();
        twitchID = BobsDatabaseHelper.getTwitchUserID(lowerCaseName);

        if (!twitchID.isEmpty()) {
            //TODO collapse to 1 select for id and name
            displayName = BobsDatabaseHelper.getDisplayName(twitchID);

            FinalPair<String, Timestamp> sqlResult = BobsDatabase.getPairFromSQL("SELECT chatLine, timeStamp FROM ChatLines WHERE twitchUserID = ? ORDER BY timeStamp DESC FETCH FIRST ROW ONLY", twitchID);
            if (sqlResult != null) {
                lastChatLine = sqlResult.first;
                notSeenFor = Duration.between(sqlResult.second.toInstant(), Instant.now());
            }
        }
    }

    public String lastSeen() {
        if (notSeenFor.toDays() < 7) return PrettyPrinter.timeStringFromDuration(notSeenFor);
        else return PrettyPrinter.timeStringFromPeriod(Period.from(notSeenFor));
    }
    public String getDisplayName() { return displayName; }
    public String getLastChatLine() { return lastChatLine; }
}

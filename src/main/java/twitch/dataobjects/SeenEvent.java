package twitch.dataobjects;

import database.BobsDatabase;
import database.BobsDatabaseHelper;
import utility.PrettyPrinter;

import javax.sql.rowset.CachedRowSet;
import java.sql.SQLException;
import java.time.Duration;
import java.time.LocalDateTime;
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
            try (CachedRowSet cachedRowSet = BobsDatabase.getCachedRowSetFromSQL("SELECT chatLine, timeStamp FROM ChatLines WHERE twitchUserID = ? ORDER BY timeStamp DESC FETCH FIRST ROW ONLY", twitchID)) {
                if (cachedRowSet.next()) {
                    lastChatLine = cachedRowSet.getString("chatLine");
                    LocalDateTime chatTime = cachedRowSet.getTimestamp("timeStamp").toLocalDateTime();
                    notSeenFor = Duration.between(chatTime, LocalDateTime.now());
                }
            } catch (SQLException e) {
                e.printStackTrace();
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

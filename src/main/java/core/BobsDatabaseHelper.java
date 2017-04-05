package core;

import twitch.Twitchv5;

import javax.sql.rowset.CachedRowSet;
import java.sql.SQLException;
import java.util.HashSet;

/**
 * Created by Dons on 04-04-2017.
 */
public class BobsDatabaseHelper {
    private static final HashSet<String> cachedUserIDs = new HashSet<>();

    public static String getWelcomeMessage(String twitchName) {
        String returnString = "none";
        try {
            CachedRowSet cachedRowSet = BobsDatabase.getCachedRowSetFromSQL("SELECT welcomeMessage FROM TwitchChatUsers WHERE lowerCaseTwitchName = ?", twitchName.toLowerCase());
            if (cachedRowSet.next()) returnString = cachedRowSet.getString("welcomeMessage");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return returnString;
    }

    public static void setWelcomeMessage(String twitchUserID, String welcomeMessage) {
        createUserIfNotExists(twitchUserID);
        BobsDatabase.executePreparedSQL("UPDATE TwitchChatUsers SET welcomeMessage = ? WHERE twitchUserID = ?", welcomeMessage, twitchUserID);
    }
    public static void setHasSubscribed(String twitchUserID) {
        createUserIfNotExists(twitchUserID);
        BobsDatabase.executePreparedSQL("UPDATE TwitchChatUsers SET hasSubscribed = true WHERE twitchUserID = ?", twitchUserID);
    }


    private static void createUserIfNotExists(String twitchUserID) {
        if (cachedUserIDs.contains(twitchUserID)) return;

        CachedRowSet cachedRowSet = BobsDatabase.getCachedRowSetFromSQL("Select twitchUserID, twitchDisplayName FROM TwitchChatUsers WHERE twitchUserID = ?", twitchUserID);
        String currentDisplayName = Twitchv5.getDisplayName(twitchUserID);

        try {
            if (cachedRowSet.next()) {
                if (!cachedRowSet.getString("twitchDisplayName").equals(currentDisplayName)) {
                    System.out.println("Changed display name for user " + currentDisplayName + " old displayname: " + cachedRowSet.getString("twitchDisplayName") + " user id: " + twitchUserID);
                    setTwitchDisplayName(twitchUserID, currentDisplayName);
                }
                cachedUserIDs.add(twitchUserID);
            } else {
                BobsDatabase.executePreparedSQL("INSERT INTO TwitchChatUsers(twitchUserID, twitchDisplayName) VALUES (?, ?)", twitchUserID, currentDisplayName);
                System.out.println("Created new DB entry for " + currentDisplayName);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void setTwitchDisplayName(String twitchUserID, String twitchDisplayName) {
        int rowsChanged = BobsDatabase.executePreparedSQL("UPDATE TwitchChatUsers SET twitchDisplayName = ? WHERE twitchUserID = ?", twitchDisplayName, twitchUserID);
        if (rowsChanged != 1) throw new RuntimeException("Only 1 row should be changed on display name change");
    }
}

package core;

import org.apache.commons.lang3.tuple.MutableTriple;
import org.apache.commons.lang3.tuple.Triple;
import twitch.Twitchv5;

import javax.sql.rowset.CachedRowSet;
import java.sql.SQLException;
import java.util.HashSet;

/**
 * Created by Dons on 04-04-2017.
 */
public class BobsDatabaseHelper {
    private static final HashSet<String> cachedUserIDs = new HashSet<>();

    public static void main(String[] args) {
        //BobsDatabase.executePreparedSQL("INSERT INTO TwitchChatUsers(twitchUserID, twitchDisplayName) VALUES (?, ?)", "3983733384", "gManBot");
        //createUserIfNotExists("39837384");
    }

    public static String getWelcomeMessage(String twitchName) {
        String returnString = "none";
        try {
            CachedRowSet cachedRowSet = BobsDatabase.getCachedRowSetFromSQL("SELECT welcomeMessage FROM TwitchChatUsers WHERE twitchLowerCaseName = ?", twitchName.toLowerCase());
            if (cachedRowSet.next()) returnString = cachedRowSet.getString("welcomeMessage");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return returnString;
    }
    public static void setWelcomeMessage(String twitchUserID, String welcomeMessage) {
        createUserIfNotExists(twitchUserID);
        if (welcomeMessage.isEmpty()) welcomeMessage = "none";
        BobsDatabase.executePreparedSQL("UPDATE TwitchChatUsers SET welcomeMessage = ? WHERE twitchUserID = ?", welcomeMessage, twitchUserID);
    }
    public static void setHasSubscribed(String twitchUserID) {
        createUserIfNotExists(twitchUserID);
        BobsDatabase.executePreparedSQL("UPDATE TwitchChatUsers SET hasSubscribed = true WHERE twitchUserID = ?", twitchUserID);
    }
    public static void setSubscriberMonths(String twitchUserID, int numberOfMonths) {
        createUserIfNotExists(twitchUserID);
        BobsDatabase.executePreparedSQL("UPDATE TwitchChatUsers SET subscriberMonths = " +numberOfMonths + " WHERE twitchUserID = ?", twitchUserID);
    }

    public static Triple<String, String, Boolean> getDisplayNameWelcomeMessageAndHasSubbedStatus(String twitchName) {
        MutableTriple<String, String, Boolean> returnTriple = new MutableTriple<>("", "", false);
        try {
            CachedRowSet cachedRowSet = BobsDatabase.getCachedRowSetFromSQL("Select twitchDisplayName, welcomeMessage, hasSubscribed FROM TwitchChatUsers WHERE twitchLowerCaseName = ?", twitchName.toLowerCase());
            if (cachedRowSet.next()) {
                returnTriple.setLeft(cachedRowSet.getString("twitchDisplayName"));
                returnTriple.setMiddle(cachedRowSet.getString("welcomeMessage"));
                returnTriple.setRight(cachedRowSet.getBoolean("hasSubscribed"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return returnTriple;
    }

    private static void createUserIfNotExists(String twitchUserID) {
        if (cachedUserIDs.contains(twitchUserID)) return;

        CachedRowSet cachedRowSet = BobsDatabase.getCachedRowSetFromSQL("Select twitchUserID, twitchDisplayName FROM TwitchChatUsers WHERE twitchUserID = ?", twitchUserID);
        String currentDisplayName = Twitchv5.getDisplayName(twitchUserID);

        try {
            if (cachedRowSet.next()) {
                if (!cachedRowSet.getString("twitchDisplayName").equals(currentDisplayName) && !currentDisplayName.isEmpty()) {
                    makeRoomInTwitchUserTableForTwitchName(currentDisplayName);
                    System.out.println("Changed display name for user " + currentDisplayName + " old display name: " + cachedRowSet.getString("twitchDisplayName") + " user id: " + twitchUserID);
                    setTwitchDisplayName(twitchUserID, currentDisplayName);
                }
            } else {
                makeRoomInTwitchUserTableForTwitchName(currentDisplayName);
                BobsDatabase.executePreparedSQL("INSERT INTO TwitchChatUsers(twitchUserID, twitchDisplayName) VALUES (?, ?)", twitchUserID, currentDisplayName);
                //TODO: check if another user has current display name and change him if he does.
                System.out.println("Created new DB entry for " + currentDisplayName);
            }
            cachedUserIDs.add(twitchUserID);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    private static void makeRoomInTwitchUserTableForTwitchName(String twitchName) {
        try {
            CachedRowSet cachedRowSet = BobsDatabase.getCachedRowSetFromSQL("SELECT twitchUserID, twitchDisplayName FROM TwitchChatUsers WHERE twitchLowerCaseName = ?", twitchName.toLowerCase());
            if (cachedRowSet.next()) {
                String otherTwitchID = cachedRowSet.getString("twitchUserID");
                String otherDisplayName = cachedRowSet.getString("twitchDisplayName");
                setTwitchDisplayName(otherTwitchID, otherDisplayName + "." + otherTwitchID);
                System.out.println("Changed Account ID: " + otherTwitchID + " from " + otherDisplayName + " to " + otherDisplayName + "." + otherTwitchID);
            } else {
                System.out.println("no display name collision for " + twitchName);
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

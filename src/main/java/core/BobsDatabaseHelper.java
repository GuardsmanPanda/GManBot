package core;

import org.apache.commons.lang3.tuple.MutableTriple;
import org.apache.commons.lang3.tuple.Triple;
import twitch.Twitchv5;

import javax.sql.rowset.CachedRowSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;

/**
 * Created by Dons on 04-04-2017.
 */
public class BobsDatabaseHelper {
    private static final HashMap<String, String> cachedUserIDs = new HashMap<>();

    public static void main(String[] args) {
        //BobsDatabase.executePreparedSQL("INSERT INTO TwitchChatUsers(twitchUserID, twitchDisplayName) VALUES (?, ?)", "3983733384", "gManBot");
        //createUserIfNotExists("39837384");
    }
    public static String getDisplayName(String twitchUserID) {
        createUserIfNotExists(twitchUserID);
        return (cachedUserIDs.getOrDefault(twitchUserID, ""));
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
    public static void setWelcomeMessage(String twitchUserID, String twitchDisplayName, String welcomeMessage) {
        createUserIfNotExists(twitchUserID, twitchDisplayName);
        if (welcomeMessage.isEmpty()) welcomeMessage = "none";
        BobsDatabase.executePreparedSQL("UPDATE TwitchChatUsers SET welcomeMessage = ? WHERE twitchUserID = ?", welcomeMessage, twitchUserID);
    }
    public static void setHasSubscribed(String twitchUserID, String twitchDisplayName) {
        createUserIfNotExists(twitchUserID, twitchDisplayName);
        BobsDatabase.executePreparedSQL("UPDATE TwitchChatUsers SET hasSubscribed = true WHERE twitchUserID = ?", twitchUserID);
    }
    public static void addChatLine(String twitchUserID, String twitchUserName) {

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
        if (cachedUserIDs.containsKey(twitchUserID)) return;
        createUserIfNotExists(twitchUserID, Twitchv5.getDisplayName(twitchUserID));
    }

    public static void createUserIfNotExists(String twitchUserID, String twitchDisplayName) {
        if (cachedUserIDs.containsKey(twitchUserID)) return;

        synchronized (BobsDatabaseHelper.class) {
            try (CachedRowSet cachedRowSet = BobsDatabase.getCachedRowSetFromSQL("Select twitchUserID, twitchDisplayName FROM TwitchChatUsers WHERE twitchUserID = ?", twitchUserID)) {
                if (cachedRowSet.next()) {
                    if (!cachedRowSet.getString("twitchDisplayName").equals(twitchDisplayName) && !twitchDisplayName.isEmpty()) {
                        makeRoomInTwitchUserTableForTwitchName(twitchDisplayName);
                        System.out.println("Changed display name for user " + twitchDisplayName + " old display name: " + cachedRowSet.getString("twitchDisplayName") + " user id: " + twitchUserID);
                        setTwitchDisplayName(twitchUserID, twitchDisplayName);
                    }
                } else {
                    makeRoomInTwitchUserTableForTwitchName(twitchDisplayName);
                    BobsDatabase.executePreparedSQL("INSERT INTO TwitchChatUsers(twitchUserID, twitchDisplayName) VALUES (?, ?)", twitchUserID, twitchDisplayName);
                    System.out.println("Created new DB entry for " + twitchDisplayName);
                }
                cachedUserIDs.put(twitchUserID, twitchDisplayName);
            } catch (SQLException e) {
                e.printStackTrace();
            }
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

package database;

import utility.FinalTriple;
import webapi.Twitchv5;

import javax.sql.rowset.CachedRowSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.HashMap;

public class BobsDatabaseHelper {
    private static final HashMap<String, String> cachedUserIDs = new HashMap<>();


    public static void mergeOldData(String newTwitchUserID, int idleHours, int activeHours, int chatLines, int bobCoins, boolean rawrsBob) {
        createUserIfNotExists(newTwitchUserID);
        BobsDatabase.executePreparedSQL("UPDATE TwitchChatUsers SET idleHours = idleHours + "+idleHours+", activeHours = activeHours +"+activeHours+", bobCoins = bobCoins +"+bobCoins+", heartsBob = "+rawrsBob+", chatLines = chatLines +"+chatLines+" WHERE twitchUserID = ?",  newTwitchUserID);
    }

    public static void setWelcomeMessage(String twitchUserID, String twitchDisplayName, String welcomeMessage) {
        createUserIfNotExists(twitchUserID, twitchDisplayName);
        cachedUserIDs.put(twitchUserID, twitchDisplayName);
        if (welcomeMessage.isEmpty()) welcomeMessage = "none";
        BobsDatabase.executePreparedSQL("UPDATE TwitchChatUsers SET welcomeMessage = ?, twitchDisplayName = ? WHERE twitchUserID = ?", welcomeMessage, twitchDisplayName, twitchUserID);
    }
    public static void setFlag(String twitchUserID, String twitchDisplayName, String flagName) {
        createUserIfNotExists(twitchUserID, twitchDisplayName);
        cachedUserIDs.put(twitchUserID, twitchDisplayName);
        BobsDatabase.executePreparedSQL("UPDATE TwitchChatUsers SET flag = ?, twitchDisplayName = ? WHERE twitchUserID = ?", flagName, twitchDisplayName, twitchUserID);
    }
    public static void setHasSubscribed(String twitchUserID, String twitchDisplayName) {
        createUserIfNotExists(twitchUserID, twitchDisplayName);
        BobsDatabase.executePreparedSQL("UPDATE TwitchChatUsers SET hasSubscribed = true WHERE twitchUserID = ?", twitchUserID);
    }
    public static void setSongRatingReminder(String twitchUserID, String twitchDisplayName, boolean reminderValue) {
        createUserIfNotExists(twitchUserID, twitchDisplayName);
        BobsDatabase.executePreparedSQL("UPDATE TwitchChatUsers SET songRatingReminder = " + reminderValue + " WHERE twitchUserID = ?", twitchUserID);
    }
    public static void setStatHide(String twitchUserID, String twitchDisplayName, boolean hide) {
        createUserIfNotExists(twitchUserID, twitchDisplayName);
        BobsDatabase.executePreparedSQL("UPDATE TwitchChatUsers SET statHide = " + hide + " WHERE twitchUserID = ?", twitchUserID);
    }
    public static void setSubscriberMonths(String twitchUserID, int numberOfMonths) {
        createUserIfNotExists(twitchUserID);
        int previousSubMonths = BobsDatabase.getIntFromSQL("SELECT subscriberMonths FROM twitchChatUsers WHERE twitchUserID = ?", twitchUserID);
        if (numberOfMonths > previousSubMonths) {
            BobsDatabase.executePreparedSQL("UPDATE TwitchChatUsers SET subscriberMonths = " +numberOfMonths + " WHERE twitchUserID = ?", twitchUserID);
        }
    }

    public static void addChatLine(String twitchUserID, String twitchDisplayName, String chatLine) {
        createUserIfNotExists(twitchUserID, twitchDisplayName);
        BobsDatabase.executePreparedSQL("INSERT INTO ChatLines(twitchUserID, chatLine, timestamp) VALUES(?, ?, '" + Timestamp.from(Instant.now())+"')", twitchUserID, chatLine);
        BobsDatabase.executePreparedSQL("UPDATE TwitchChatUsers SET chatLines = chatLines + 1 WHERE twitchUserID = ?", twitchUserID);
    }
    public static void addActiveHour(String twitchUserID) {
        BobsDatabase.executePreparedSQL("UPDATE TwitchChatUsers SET activeHours = activeHours + 1 WHERE twitchUserID = ?", twitchUserID);
    }
    public static void addIdleHour(String twitchUserID) {
        BobsDatabase.executePreparedSQL("UPDATE TwitchChatUsers SET idleHours = idleHours + 1 WHERE twitchUserID = ?", twitchUserID);
    }
    public static void addBobCoins(String twitchUserID, int bobCoinsToAdd) {
        BobsDatabase.executePreparedSQL("UPDATE TwitchChatUsers SET bobCoins = bobCoins + "+bobCoinsToAdd+" WHERE twitchUserID = ?", twitchUserID);
    }


    public static String getDisplayName(String twitchUserID) {
        createUserIfNotExists(twitchUserID);
        return (cachedUserIDs.getOrDefault(twitchUserID, ""));
    }
    public static String getTwitchUserID(String twitchDisplayName) {
        return BobsDatabase.getStringFromSQL("SELECT twitchUserID FROM TwitchChatUsers WHERE twitchLowerCaseName = ?", twitchDisplayName.toLowerCase());
    }
    public static String getFlagFromTwitchName(String twitchName) {
        String flagName = BobsDatabase.getStringFromSQL("SELECT flag FROM twitchChatUsers WHERE twitchLowerCaseName = ?", twitchName.toLowerCase());
        return (flagName.isEmpty()) ? "none" : flagName;
    }
    public static boolean getHasSubscribed(String twitchUserID) {
        return BobsDatabase.getBooleanFromSQL("SELECT hasSubscribed FROM TwitchChatUsers WHERE twitchUserID = ?", twitchUserID);
    }

    public static FinalTriple<String, String, Boolean> getDisplayNameWelcomeMessageAndHasSubbedStatus(String twitchName) {
        String first = "";
        String second = "";
        Boolean third = false;
        try {
            CachedRowSet cachedRowSet = BobsDatabase.getCachedRowSetFromSQL("Select twitchDisplayName, welcomeMessage, hasSubscribed FROM TwitchChatUsers WHERE twitchLowerCaseName = ?", twitchName.toLowerCase());
            if (cachedRowSet.next()) {
                first = cachedRowSet.getString("twitchDisplayName");
                second = cachedRowSet.getString("welcomeMessage");
                third = cachedRowSet.getBoolean("hasSubscribed");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return new FinalTriple<>(first, second, third);
    }


    private static void createUserIfNotExists(String twitchUserID) {
        if (cachedUserIDs.containsKey(twitchUserID)) return;
        createUserIfNotExists(twitchUserID, Twitchv5.getDisplayName(twitchUserID));
    }
    private static void createUserIfNotExists(String twitchUserID, String twitchDisplayName) {
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

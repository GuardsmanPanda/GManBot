package database;

import com.google.common.base.Strings;
import twitch.TwitchChat;
import twitch.TwitchWebChatOverlay;
import utility.FinalPair;
import utility.FinalTriple;
import webapi.Twitchv5;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class BobsDatabaseHelper {
    private static final HashMap<String, String> cachedUserIDs = new HashMap<>();


    public static void mergeOldData(String newTwitchUserID, int idleHours, int activeHours, int chatLines, int bobCoins, boolean rawrsBob) {
        createUserIfNotExists(newTwitchUserID);
        BobsDatabase.executePreparedSQL("UPDATE TwitchChatUsers SET idleHours = idleHours + "+idleHours+", activeHours = activeHours +"+activeHours+", bobCoins = bobCoins +"+bobCoins+", heartsBob = "+rawrsBob+", chatLines = chatLines +"+chatLines+" WHERE twitchUserID = ?",  newTwitchUserID);
    }

    public static String getDisplayName(String twitchUserID) {
        createUserIfNotExists(twitchUserID);
        return (cachedUserIDs.getOrDefault(twitchUserID, ""));
    }
    public static String getTwitchUserID(String twitchDisplayName) {
        return BobsDatabase.getStringFromSQL("SELECT twitchUserID FROM TwitchChatUsers WHERE twitchLowerCaseName = ?", twitchDisplayName.toLowerCase());
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
        TwitchWebChatOverlay.invalidateIcon(twitchUserID);
    }
    public static void setHasSubscribed(String twitchUserID, String twitchDisplayName) {
        createUserIfNotExists(twitchUserID, twitchDisplayName);
        BobsDatabase.executePreparedSQL("UPDATE TwitchChatUsers SET hasSubscribed = true WHERE twitchUserID = ?", twitchUserID);
    }
    public static void setHeartsBob(String twitchUserID, String twitchDisplayName) {
        createUserIfNotExists(twitchUserID, twitchDisplayName);
        BobsDatabase.executePreparedSQL("UPDATE TwitchChatUsers SET heartsBob = true WHERE twitchUserID = ?", twitchUserID);
        TwitchWebChatOverlay.invalidateIcon(twitchUserID);
    }
    public static void setSongRatingReminder(String twitchUserID, String twitchDisplayName, boolean reminderValue) {
        System.out.println("rating reminder update: " + reminderValue);
        createUserIfNotExists(twitchUserID, twitchDisplayName);
        BobsDatabase.executePreparedSQL("UPDATE TwitchChatUsers SET songRatingReminder = " + reminderValue + " WHERE twitchUserID = ?", twitchUserID);
    }
    public static void setSongQuoteReminder(String twitchUserID, String twitchDisplayName, boolean reminderValue) {
        createUserIfNotExists(twitchUserID, twitchDisplayName);
        BobsDatabase.executePreparedSQL("UPDATE TwitchChatUsers SET songQuoteReminder = " + reminderValue + " WHERE twitchUserID = ?", twitchUserID);
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
    public static void addCentsDonated(String twitchUserID, int centsToAdd) {
        BobsDatabase.executePreparedSQL("UPDATE TwitchChatUsers SET centsDonated = centsDonated + "+centsToAdd+" WHERE twitchUserID = ?", twitchUserID);
        TwitchWebChatOverlay.invalidateIcon(twitchUserID);
    }
    public static void addBobCoins(String twitchUserID, int bobCoinsToAdd) {
        BobsDatabase.executePreparedSQL("UPDATE TwitchChatUsers SET bobCoins = bobCoins + "+bobCoinsToAdd+" WHERE twitchUserID = ?", twitchUserID);
    }


    public static String getFlagName(String twitchUserID) {
        return BobsDatabase.getStringFromSQL("SELECT flag FROM TwitchChatUsers WHERE twitchUserID = ?", twitchUserID);
    }
    public static boolean getHeartsBob(String twitchUserID) {
        return BobsDatabase.getBooleanFromSQL("SELECT heartsBob FROM TwitchChatUsers WHERE twitchUserID = ?", twitchUserID);
    }
    public static int getCentsDonated(String twitchUserID) {
        return BobsDatabase.getIntFromSQL("SELECT centsDonated FROM twitchChatUsers WHERE twitchUserID = ?", twitchUserID);
    }
    public static boolean getHasSubscribed(String twitchUserID) {
        return BobsDatabase.getBooleanFromSQL("SELECT hasSubscribed FROM TwitchChatUsers WHERE twitchUserID = ?", twitchUserID);
    }
    public static Map<String, Integer> getFlagStats(boolean everyone) {
        System.out.println("returning flag stats: " + everyone);
        if (everyone) return BobsDatabase.getMapFromSQL("SELECT flag, count(flag) FROM TwitchChatUsers WHERE flag <> 'none' GROUP BY flag");
        else {
            Set<String> chatUserIDs = TwitchChat.getUserIDsInChannel();
            return BobsDatabase.getMapFromSQL("SELECT flag, count(flag) FROM TwitchChatUsers WHERE flag <> 'none' AND twitchUserID IN (?" + Strings.repeat(", ?", chatUserIDs.size() - 1) + ") GROUP BY flag", chatUserIDs.toArray(new String[0]));
        }
    }


    public static FinalTriple<String, String, Boolean> getDisplayNameWelcomeMessageAndHasSubbedStatus(String twitchName) {
        FinalTriple<String, String, Boolean> triple = BobsDatabase.getTripleFromSQL("Select twitchDisplayName, welcomeMessage, hasSubscribed FROM TwitchChatUsers WHERE twitchLowerCaseName = ?", twitchName.toLowerCase());
        if (triple == null) return new FinalTriple<>("", "", false);
        else return triple;
    }


    private static void createUserIfNotExists(String twitchUserID) {
        if (cachedUserIDs.containsKey(twitchUserID)) return;
        createUserIfNotExists(twitchUserID, Twitchv5.getDisplayName(twitchUserID));
    }
    private static void createUserIfNotExists(String twitchUserID, String twitchDisplayName) {
        if (cachedUserIDs.containsKey(twitchUserID)) return;

        synchronized (BobsDatabaseHelper.class) {
            FinalPair<String, String> sqlResult = BobsDatabase.getPairFromSQL("Select twitchUserID, twitchDisplayName FROM TwitchChatUsers WHERE twitchUserID = ?", twitchUserID);
            if (sqlResult != null) {
                if (!sqlResult.second.equals(twitchDisplayName) && !twitchDisplayName.isEmpty()) {
                    makeRoomInTwitchUserTableForTwitchName(twitchDisplayName);
                    System.out.println("Changed display name for user " + twitchDisplayName + " old display name: " + sqlResult.second + " user id: " + twitchUserID);
                    setTwitchDisplayName(twitchUserID, twitchDisplayName);
                }
            } else {
                makeRoomInTwitchUserTableForTwitchName(twitchDisplayName);
                BobsDatabase.executePreparedSQL("INSERT INTO TwitchChatUsers(twitchUserID, twitchDisplayName) VALUES (?, ?)", twitchUserID, twitchDisplayName);
                System.out.println("Created new DB entry for " + twitchDisplayName);
            }
            cachedUserIDs.put(twitchUserID, twitchDisplayName);
        }
    }
    private static void makeRoomInTwitchUserTableForTwitchName(String twitchName) {
        FinalPair<String, String> sqlResult = BobsDatabase.getPairFromSQL("SELECT twitchUserID, twitchDisplayName FROM TwitchChatUsers WHERE twitchLowerCaseName = ?", twitchName.toLowerCase());
        if (sqlResult == null) System.out.println("no display name collision for " + twitchName);
        else {
            setTwitchDisplayName(sqlResult.first, sqlResult.second + "." + sqlResult.first);
            System.out.println("Changed Account ID: " + sqlResult.first + " from " + sqlResult.second + " to " + sqlResult.second + "." + sqlResult.first);
        }
    }
    private static void setTwitchDisplayName(String twitchUserID, String twitchDisplayName) {
        int rowsChanged = BobsDatabase.executePreparedSQL("UPDATE TwitchChatUsers SET twitchDisplayName = ? WHERE twitchUserID = ?", twitchDisplayName, twitchUserID);
        if (rowsChanged != 1) throw new RuntimeException("Only 1 row should be changed on display name change");
    }
}

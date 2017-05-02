package twitch;

import database.BobsDatabase;
import database.BobsDatabaseHelper;
import database.SongDatabase;
import utility.GBUtility;
import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.events.JoinEvent;
import org.pircbotx.hooks.events.MessageEvent;
import utility.FinalTriple;
import utility.PrettyPrinter;
import webapi.SpaceLaunch;
import webapi.Twitchv5;

import javax.sql.rowset.CachedRowSet;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

public class TwitchChatExtras extends ListenerAdapter {
    private static final HashMap<String, LocalDateTime> lastWelcomeMessageTime = new HashMap<>();
    private static final HashMap<String, String> flagTranslationMap = new HashMap<>();
    private static final LocalDateTime startTime = LocalDateTime.now();
    private static final Random random = new Random();

    static {
        fillFlagTranslationMap(flagTranslationMap);
    }

    @Override
    public void onMessage(MessageEvent event)  {
        TwitchChatMessage chatMessage = new TwitchChatMessage(event);
        switch (chatMessage.getMessageCommand()) {
            case "!followage": followAge(chatMessage); break;
            case "!setwelcomemessage": setWelcomeMessage(chatMessage); break;
            case "!setflag": setFlag(chatMessage); break;
            case "!chatstats": chatStats(chatMessage); break;
            case "!spacelaunch": SpaceLaunch.nextSpaceLaunchRequest(); break;
        }
    }

    public void onJoin(JoinEvent event) {
        //ignore join events for the first 3 minutes of a restart to avoid mass channel spam.
        if (startTime.plusMinutes(3).isAfter(LocalDateTime.now())) return;

        FinalTriple<String, String, Boolean> welcomeTriple = BobsDatabaseHelper.getDisplayNameWelcomeMessageAndHasSubbedStatus(event.getUserHostmask().getNick());
        String displayName = welcomeTriple.first;
        String welcomeMessage = welcomeTriple.second;
        Boolean hasSubscribed = welcomeTriple.third;

        if (hasSubscribed && !welcomeMessage.equalsIgnoreCase("none")) {
            if (welcomeMessage.startsWith("/") && !welcomeMessage.toLowerCase().startsWith("/me ")) return;

            if (lastWelcomeMessageTime.containsKey(displayName) && lastWelcomeMessageTime.get(displayName).isAfter(LocalDateTime.now().minus(2, ChronoUnit.HOURS))) {
                //we have recently sent a welcome message to the user
            } else {
                new Thread(() -> {
                    try { Thread.sleep(4000); } catch (InterruptedException e) { e.printStackTrace(); }
                    if (welcomeMessage.toLowerCase().startsWith("/me ")) {
                        TwitchChat.sendAction(welcomeMessage.substring(4));
                    }
                    else {
                        TwitchChat.sendMessage(welcomeMessage);
                    }
                    lastWelcomeMessageTime.put(displayName, LocalDateTime.now());
                }).start();
            }
        }
    }

    private static void followAge(TwitchChatMessage chatMessage) {
        LocalDate followDate = Twitchv5.getFollowDate(chatMessage.userID);
        if (followDate == null) return;
        if (followDate.isEqual(LocalDate.now())) {
            TwitchChat.sendMessage(chatMessage.displayName + ", You just followed the stream today! bobHype");
            return;
        }

        String printString = chatMessage.displayName + ": Followed for " + PrettyPrinter.timeStringFromPeriod(followDate.until(LocalDate.now())) + "!";
        TwitchChat.sendMessage(printString);
    }

    private static void chatStats(TwitchChatMessage chatMessage) {
        StringBuilder statStringBuilder = new StringBuilder(chatMessage.displayName);
        statStringBuilder.append(" - ");

        try (CachedRowSet cachedRowSet = BobsDatabase.getCachedRowSetFromSQL("SELECT chatLines, activeHours, idleHours, bobCoins FROM TwitchChatUsers WHERE twitchUserID = ?", chatMessage.userID)) {
            if (cachedRowSet.next()) {
                int chatLines = cachedRowSet.getInt("chatLines");
                int activeHours = cachedRowSet.getInt("activeHours");
                int idleHours = cachedRowSet.getInt("idleHours");
                int bobCoins = cachedRowSet.getInt("bobCoins");

                statStringBuilder.append("ChatLines: " + chatLines);
                statStringBuilder.append(" [Rank: " + (BobsDatabase.getIntFromSQL("SELECT COUNT(*) AS numberOfEntries FROM twitchChatUsers WHERE chatLines > "+chatLines) + 1) + "]");
                statStringBuilder.append(", BobCoins: " + bobCoins);
                statStringBuilder.append(" [" + (BobsDatabase.getIntFromSQL("SELECT COUNT(*) AS numberOfEntries FROM twitchChatUsers WHERE bobCoins > "+bobCoins) + 1) + "]");
                statStringBuilder.append(", ActiveHours: " + activeHours);
                statStringBuilder.append(" [" + (BobsDatabase.getIntFromSQL("SELECT COUNT(*) AS numberOfEntries FROM twitchChatUsers WHERE activeHours > "+activeHours) + 1) + "]");
                statStringBuilder.append(", IdleHours: " + idleHours);
                statStringBuilder.append(" [" + (BobsDatabase.getIntFromSQL("SELECT COUNT(*) AS numberOfEntries FROM twitchChatUsers WHERE idleHours > "+idleHours) + 1) + "]");
                statStringBuilder.append(SongDatabase.getSongRatingStatString(chatMessage.userID));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        //TODO append song rating stats
        TwitchChat.sendMessage(statStringBuilder.toString());
    }

    private static void setWelcomeMessage(TwitchChatMessage chatMessage) {
        if (chatMessage.getMessageContent().equalsIgnoreCase("!setwelcomemessage")) return;

        if (chatMessage.isSubOrPrime) BobsDatabaseHelper.setHasSubscribed(chatMessage.userID, chatMessage.displayName);
        BobsDatabaseHelper.setWelcomeMessage(chatMessage.userID, chatMessage.displayName, chatMessage.getMessageContent());
    }

    private static void setFlag(TwitchChatMessage chatMessage) {
        String flagRequest = chatMessage.getMessageContent().replaceAll("\\W", "").toLowerCase().trim();
        if (flagTranslationMap.containsKey(flagRequest)) {
            System.out.println("Found flag for " + chatMessage.displayName + " flag name: " + flagTranslationMap.get(flagRequest) + " flagRequest: " + flagRequest + " Message: " + chatMessage.message);
            BobsDatabaseHelper.setFlag(chatMessage.userID, chatMessage.displayName, flagTranslationMap.get(flagRequest));

        } else if (flagRequest.equals("random") || flagRequest.equals("!setflag")) {
            List<String> flagNames = new ArrayList<>(new HashSet<>(flagTranslationMap.values()));
            String randomFlag = flagNames.get(random.nextInt(flagNames.size()));
            System.out.println("Giving random flag to " + chatMessage.displayName + " flagname: " + randomFlag + " Message: " + chatMessage.message);
            BobsDatabaseHelper.setFlag(chatMessage.userID, chatMessage.displayName, randomFlag);

        } else {
            GBUtility.textToBob("could not find flag translation for " + chatMessage.displayName + " Flag translation: " + flagRequest + " Message: " + chatMessage.message);
            System.out.println("could not find flag translation for " + chatMessage.displayName + " Flag translation: " + flagRequest + " Message: " + chatMessage.message);
        }
    }


    private static void fillFlagTranslationMap(Map<String, String> translationMap) {
        try {
            List<String> flagTranslations = Files.readAllLines(Paths.get("Data/Flags/flagNames.txt"));
            for (String flagTranslation : flagTranslations) {
                String[] flagArray = flagTranslation.split(",");
                String trueFlagName = flagArray[0];
                for (String flagTranslationName : flagArray) {
                    flagTranslationName = flagTranslationName.replaceAll("\\W", "").toLowerCase().trim();
                    if (translationMap.containsKey(flagTranslationName)) {
                        System.out.println("Double flag entry for " + trueFlagName + " translation name: " + flagTranslationName);
                    } else {
                        translationMap.put(flagTranslationName, trueFlagName);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Error loading the flag translation map");
        }
    }
}

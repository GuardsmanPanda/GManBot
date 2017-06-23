package twitch;

import database.BobsDatabaseHelper;
import database.EmoteDatabase;
import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.events.MessageEvent;
import twitch.dataobjects.TwitchChatMessage;
import utility.Extra;
import webapi.Twitchv5;

import java.time.Duration;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class TwitchChatInformationGathering extends ListenerAdapter {
    private static final Set<String> emoteSet = new HashSet<>();
    private static int chatLinesLastHour = 0;

    static {
        emoteSet.addAll(Twitchv5.getBobsEmoticonSet());
        emoteSet.addAll(Twitchv5.getGlobalTwitchEmoteSet());
        emoteSet.addAll(Twitchv5.getBTTVEmoteSet());
        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(() -> hourlyUpdate(), 1, 1, TimeUnit.HOURS);
    }

    @Override
    public void onMessage(MessageEvent event) {
        TwitchChatMessage chatMessage = new TwitchChatMessage(event);

        BobsDatabaseHelper.addChatLine(chatMessage.userID, chatMessage.displayName, chatMessage.message);
        chatLinesLastHour++;

        emoteSet.stream()
                .filter(chatMessage.message::contains)
                .forEach(emote -> EmoteDatabase.addEmoteUsage(chatMessage.userID, emote));
    }

    private static void hourlyUpdate() {
        Set<String> activeUserIDs = TwitchChat.getActiveUserIDsInChannel(Duration.ofMinutes(60));
        Set<String> userIDsInChannel = TwitchChat.getUserIDsInChannel();

        if (activeUserIDs.size() > 4) {
            String emotes = Twitchv5.getBobsEmoticonSet().stream().sorted(Extra.randomOrder()).limit(3).collect(Collectors.joining(" "));
            String updateString = emotes + " Last Hour, " + activeUserIDs.size()  + " People Talked (" + (userIDsInChannel.size() - activeUserIDs.size()) + " idle)";
            updateString += " - Using " + chatLinesLastHour + " lines of text!";
            TwitchChat.sendMessage(updateString);
        }
        chatLinesLastHour = 0;

        activeUserIDs.forEach(userID -> {
            BobsDatabaseHelper.addActiveHour(userID);
            int coins = 6;
            if (BobsDatabaseHelper.getHasSubscribed(userID)) coins += 2;
            BobsDatabaseHelper.addBobCoins(userID, coins);
        });

        userIDsInChannel.stream()
                .filter(userID -> !activeUserIDs.contains(userID))
                .forEach(userID -> {
                    BobsDatabaseHelper.addIdleHour(userID);
                    BobsDatabaseHelper.addBobCoins(userID, 4);
                });
    }
}

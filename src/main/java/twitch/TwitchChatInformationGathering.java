package twitch;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import core.BobsDatabaseHelper;
import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.events.MessageEvent;
import org.pircbotx.hooks.events.NoticeEvent;
import org.pircbotx.hooks.events.UnknownEvent;

import java.time.Duration;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class TwitchChatInformationGathering extends ListenerAdapter {
    private static final HashSet<String> emoticons = new HashSet<>();
    private static final Multiset<String> emoticonUsage = HashMultiset.create();

    static {
        emoticons.addAll(Twitch.getSubscriberEmoticons("guardsmanbob"));
        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(() -> hourlyUpdate(), 0, 1, TimeUnit.HOURS);
    }

    @Override
    public void onMessage(MessageEvent event)  {
        TwitchChatMessage chatMessage = new TwitchChatMessage(event);

        BobsDatabaseHelper.addChatLine(chatMessage.userID, chatMessage.displayName);

        emoticons.stream()
                 .filter(chatMessage.message::contains)
                 .forEach(emoticonUsage::add);
    }

    private static void hourlyUpdate() {
        Set<String> activeUserIDs = TwitchChat.getActiveUserIDsInChannel(Duration.ofMinutes(60));
        Set<String> namesInChannel = TwitchChat.getLowerCaseNamesInChannel("#guardsmanbob");

        activeUserIDs.forEach(userID -> {
            BobsDatabaseHelper.addActiveHour(userID);
            int coins = 6;
            if (BobsDatabaseHelper.getHasSubscribed(userID)) coins += 2;
            BobsDatabaseHelper.addbobCoins(userID, coins);
        });

        namesInChannel.stream()
                .map(BobsDatabaseHelper::getTwitchUserID)
                .filter(userID -> !userID.isEmpty() && !activeUserIDs.contains(userID))
                .forEach(userID -> {
                    BobsDatabaseHelper.addIdleHour(userID);
                    BobsDatabaseHelper.addbobCoins(userID, 4);
                });
    }
}

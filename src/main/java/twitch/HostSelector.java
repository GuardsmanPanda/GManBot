package twitch;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMultiset;
import com.google.common.collect.ImmutableSortedMultiset;
import com.google.common.collect.Multiset;
import com.google.common.collect.Multisets;
import core.GManUtility;
import org.apache.commons.lang3.StringUtils;
import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.events.MessageEvent;

import java.util.HashMap;
import java.util.HashSet;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Functionality for selecting a stream to Host during offline hours, should likely be made start and stoppable via the ui and chat commands (from mods+)
 * voting for a stream to host will be done by typing !host twitchNameHere
 */
public class HostSelector extends ListenerAdapter {
    private int MINIMUMFOLLOWERS = 100;

    private static final ConcurrentHashMap<String, Boolean> streamViability = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<String, String> streamVotes = new ConcurrentHashMap<>();
    private static boolean isActive = false;


    public static void main(String[] args) {
        TwitchChat.connect();
        TwitchChat.addListener(new HostSelector());
    }

    public static void startHostSelector() {
        isActive = true;
        new Thread(() -> {
            TwitchChat.sendMessage("Starting the Host Selector");
            TwitchChat.sendMessage("You can know vote on which stream to host by typing !host streamNameHere");

            try { Thread.sleep(60000); } catch (InterruptedException e) { e.printStackTrace(); }

            TwitchChat.sendMessage("bobHype 30 seconds left to vote for a stream to Host, most voted streams:");
            Multiset<String> channels = ImmutableMultiset.copyOf(streamVotes.values());
            TwitchChat.sendMessage(GManUtility.getMultisetLeaderText(channels, 3));

        }).start();
    }

    public static void changeHost() {
        TwitchChat.sendMessage("Starting vote for new Stream to Host");
    }

    public static void stopHostSelector() {
        TwitchChat.sendMessage("Stopping the Host Selector");
        TwitchChat.sendMessage("/unhost");
        isActive = false;
    }

    @Override
    public void onMessage(MessageEvent event) throws Exception {
        String message = event.getMessage().toLowerCase();

        if (event.getTags().containsKey("mod") && event.getTags().get("mod").equalsIgnoreCase("1")) {
            if (message.startsWith("!starthost") && !isActive) startHostSelector();
            else if (message.startsWith("!stophost") && isActive) stopHostSelector();
            else if (message.startsWith("!changehost") && isActive) changeHost();
        }

        if (message.startsWith("!host") && message.contains(" ")) {
            String sender = event.getUser().getNick().toLowerCase();
            String content = StringUtils.substringAfter(message, " ");
            boolean isViable = false;
            System.out.println("stream vote! " + sender + ", " + content);

            if (streamViability.containsKey(content)) isViable = streamViability.get(content);
                //TODO: add stream online check
            else if (Twitch.getFollowerCount(content) > MINIMUMFOLLOWERS) {
                System.out.println("stream found viable by lookup");
                isViable = true;
                streamViability.put(content, true);
            } else {
                streamViability.put(content, false);
            }
            System.out.println("Stream viability: " + isViable);

            if (isViable) {
                streamVotes.put(sender, content);
                if (event.getTags().get("subscriber").equalsIgnoreCase("1")) streamVotes.put(sender + "subvote", content);
            }
        }
    }
}

package twitch;

import com.google.common.collect.ImmutableMultiset;
import com.google.common.collect.Multiset;
import core.GBUtility;
import org.apache.commons.lang3.StringUtils;
import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.events.MessageEvent;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Functionality for selecting a stream to Host during offline hours, should likely be made start and stoppable via the ui and chat commands (from mods+)
 * voting for a stream to host will be done by typing !host twitchNameHere
 */
public class HostSelector extends ListenerAdapter {
    private int MINIMUMFOLLOWERS = 100;

    private static final ConcurrentHashMap<String, Boolean> streamViability = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<String, String> streamVotes = new ConcurrentHashMap<>();
    private static boolean isActivelyVoting = false;
    private static boolean isActive = false;


    public static void main(String[] args) {
        TwitchChat.connect();
        TwitchChat.addListener(new HostSelector());
    }

    public synchronized static void startHostSelector() {
        if (isActivelyVoting) return;
        isActive = true;
        isActivelyVoting = true;
        streamVotes.clear();

        new Thread(() -> {
            TwitchChat.sendMessage("Starting the Host Selector");
            TwitchChat.sendMessage("You can now vote on which stream to host by typing !host streamNameHere");

            try { Thread.sleep(60000); } catch (InterruptedException e) { e.printStackTrace(); }

            if (streamVotes.size() <= 5) {
                TwitchChat.sendMessage("There must be at least 6 vote to select a new stream to Host, current votes: " + streamVotes.size() + ". Type !host streamNameHere to vote for a stream.");
                try { Thread.sleep(30000); } catch (InterruptedException e) { e.printStackTrace(); }
            }

            if (streamVotes.size() > 5) {
                TwitchChat.sendMessage("bobHype 40 seconds left to vote for a stream to Host, most voted streams:");
                Multiset<String> channels = ImmutableMultiset.copyOf(streamVotes.values());
                TwitchChat.sendMessage(GBUtility.getMultisetLeaderText(channels, 3));

                try { Thread.sleep(40000); } catch (InterruptedException e) { e.printStackTrace(); }

                String winningStream = GBUtility.getElementWithHighestCount(channels);
                TwitchChat.sendMessage("bobHype with " + channels.count(winningStream) + " votes " + winningStream + " was chosen to be hosted!");
                TwitchChat.sendMessage("/Host " + winningStream);
            } else {
                TwitchChat.sendMessage("Stream Host Voting ended, not enough votes");
            }
            isActivelyVoting = false;
        }).start();
    }

    public static void changeHost() {
        if (isActivelyVoting) return;
        startHostSelector();
    }

    public static void stopHostSelector() {
        TwitchChat.sendMessage("Stopping the Host Selector");
        TwitchChat.sendMessage("/unhost");
        isActive = false;
    }

    //TODO: introduce !votestarthost.. !votechangehost and !votestophost
    @Override
    public void onMessage(MessageEvent event) throws Exception {
        if (event.getUser().getNick().equalsIgnoreCase("gmanbot")) return;
        String message = event.getMessage().toLowerCase();

        if (event.getTags().containsKey("mod") && event.getTags().get("mod").equalsIgnoreCase("1")) {
            if      (message.startsWith("!starthost") && !isActive) startHostSelector();
            else if (message.startsWith("!stophost") && isActive) stopHostSelector();
            else if (message.startsWith("!changehost") && isActive) changeHost();
        }

        if (message.startsWith("!host") && message.contains(" ")) {
            String sender = event.getUser().getNick().toLowerCase();
            String content = StringUtils.substringAfter(message, " ");
            boolean isViable = false;

            if (streamViability.containsKey(content)) isViable = streamViability.get(content);
            else if (Twitch.getFollowerCount(content) > MINIMUMFOLLOWERS && Twitch.isStreamOnline(content)) {
                System.out.println("stream " + content + " found viable by lookup");
                isViable = true;
                streamViability.put(content, true);
            } else {
                streamViability.put(content, false);
                System.out.println("stream " + content + " found NOT viable by lookup");
            }

            if (isViable) {
                streamVotes.put(sender, content);
                if (event.getTags().get("subscriber").equalsIgnoreCase("1")) streamVotes.put(sender + "subvote", content);
            }
        }
    }
}

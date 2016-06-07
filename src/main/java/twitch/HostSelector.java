package twitch;

import com.google.common.collect.ImmutableMultiset;
import com.google.common.collect.Multiset;
import core.GBUtility;
import org.apache.commons.lang3.StringUtils;
import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.events.MessageEvent;

import java.awt.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Functionality for selecting a stream to Host during offline hours, should likely be made start and stoppable via the ui and chat commands (from mods+)
 * voting for a stream to host will be done by typing !host twitchNameHere
 */
public class HostSelector extends ListenerAdapter {
    private static int MAXTIMETOWAITINSECONDS = 300;
    private static int MINIMUMFOLLOWERS = 100;
    private static int MINVOTESNEEDED = 6;
    private static boolean watchingStream = false;

    private static final ConcurrentHashMap<String, Boolean> streamViability = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<String, String> streamVotes = new ConcurrentHashMap<>();
    private static boolean isActivelyVoting = false;
    private static boolean isActive = false;

    public static void main(String[] args) throws AWTException {
        TwitchChat.connect();
        TwitchChat.addListener(new HostSelector());
    }

    public synchronized static void startHostSelector() {
        if (isActivelyVoting) {
            TwitchChat.sendMessage("Cannot start the host selector while actively voting");
            return;
        }
        isActive = true;
        isActivelyVoting = true;
        streamVotes.clear();
        stopStreamWatch();

        new Thread(() -> {
            TwitchChat.sendMessage("You can now vote on which stream to host by typing !host streamNameHere");
            try { Thread.sleep(20000); } catch (InterruptedException e) { e.printStackTrace(); }

            for (int i = 1; i <= MAXTIMETOWAITINSECONDS; i++) {
                if (streamVotes.size() >= MINVOTESNEEDED) break;
                if (i % 60 == 0) TwitchChat.sendMessage("There must be at least 6 vote to select a new stream to Host, current votes: " + streamVotes.size() + ". Type !host streamNameHere to vote for a stream.");
                try { Thread.sleep(1000); } catch (InterruptedException e) { e.printStackTrace(); }
            }

            if (streamVotes.size() >= MINVOTESNEEDED) {
                TwitchChat.sendMessage("bobHype 40 seconds left to vote for a stream to Host, most voted streams:");
                Multiset<String> channels = ImmutableMultiset.copyOf(streamVotes.values());
                TwitchChat.sendMessage(GBUtility.getMultisetLeaderText(channels, 3));

                try { Thread.sleep(40000); } catch (InterruptedException e) { e.printStackTrace(); }

                channels = ImmutableMultiset.copyOf(streamVotes.values());
                String winningStream = GBUtility.getElementWithHighestCount(channels);
                TwitchChat.sendMessage("bobHype with " + channels.count(winningStream) + " votes " + winningStream + " was chosen to be hosted!");
                TwitchChat.sendMessage("/Host " + winningStream);
                startStreamWatch(winningStream);
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
        TwitchChat.sendMessage("/unhost");
        stopStreamWatch();
        isActive = false;

    }

    //TODO: introduce !votestarthost.. !votechangehost and !votestophost
    @Override
    public void onMessage(MessageEvent event) throws Exception {
        if (event.getUser().getNick().equalsIgnoreCase("gmanbot")) return;
        String message = event.getMessage().toLowerCase();

        if (event.getTags().containsKey("mod") && event.getTags().get("mod").equalsIgnoreCase("1")) {
            if (message.startsWith("!starthost") && !isActive) {
                TwitchChat.sendMessage("Starting the Host Selector");
                startHostSelector();
            }
            else if (message.startsWith("!stophost") && isActive) {
                TwitchChat.sendMessage("Stopping the Host Selector");
                stopHostSelector();
            }
            else if (message.startsWith("!changehost")) changeHost();
        }

        if (message.startsWith("!host") && message.contains(" ")) {
            String sender = event.getUser().getNick().toLowerCase();
            String content = StringUtils.substringAfter(message, " ");
            boolean isViable = false;
            int followerCount = Twitch.getFollowerCount(content);

            if (streamViability.containsKey(content)) isViable = streamViability.get(content);
            else if (followerCount > MINIMUMFOLLOWERS) {
                System.out.println("stream " + content + " found viable by lookup");
                isViable = true;
                streamViability.put(content, true);
            } else {
                if (followerCount < MINIMUMFOLLOWERS && followerCount > 0) streamViability.put(content, false);
                System.out.println("stream " + content + " found NOT viable by lookup");
            }

            if (isViable) {
                if (!Twitch.isStreamOnline(content, true)) {
                    event.respond(content + " appears to be offline, you can only vote for online streams");
                    return;
                }
                streamVotes.put(sender, content);
                if (event.getTags().get("subscriber").equalsIgnoreCase("1")) streamVotes.put(sender + "subvote", content);
            }
        }
    }

    /**
     * Check if the hosted stream goes offline and start vote for new stream to host in this case.
     * @param twitchName
     */
    private static void startStreamWatch(String twitchName) {
        watchingStream = true;
        new Thread(() -> {
            while(watchingStream) {
                if (!Twitch.isStreamOnline(twitchName, true)) {
                    try { Thread.sleep(20000); } catch (InterruptedException e) { e.printStackTrace(); }
                    if (!Twitch.isStreamOnline(twitchName, true)) {
                        if (watchingStream) {
                            TwitchChat.sendMessage("Stream " + twitchName + " appears to be offline, starting vote for new stream to host!");
                            stopHostSelector();
                            startHostSelector();
                        }
                    }
                }
                try { Thread.sleep(15000); } catch (InterruptedException e) { e.printStackTrace(); }
            }
        }).start();
    }

    private static void stopStreamWatch() {
        watchingStream = false;
    }
}

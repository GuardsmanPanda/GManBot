package twitch;

import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.events.MessageEvent;

import java.time.Duration;

public class VoteSystem extends ListenerAdapter {
    private static boolean active = false;

    @Override
    public void onMessage(MessageEvent event) {
        if (!active) return;

        int i = 0;
    }

    private static void startVoting(Duration voteDuration) {

    }

    //prematurly end a wrongly started vote
    private static void endVote() {

    }


}

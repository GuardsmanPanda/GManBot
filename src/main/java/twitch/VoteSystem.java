package twitch;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.events.MessageEvent;

import java.time.Duration;
import java.util.HashMap;

public class VoteSystem extends ListenerAdapter {
    private static final HashMap<String, Integer> voteMap = new HashMap<>();
    private static boolean active = false;
    private static int voteChoices = 0;

    @Override
    public void onMessage(MessageEvent event) {
        if (!active) return;

        int i = 0;
    }

    private static void startVoting(Duration voteDuration, int options) {
        voteChoices = options;
        active = true;

        ObjectNode rootNode = JsonNodeFactory.instance.objectNode();
        rootNode.put("type", "voteStart");
        rootNode.put("options", options);
    }

    //prematurly end a wrongly started vote
    private static void endVote() {

    }


}

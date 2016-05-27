package twitch;

import com.google.common.base.Strings;
import org.apache.commons.lang3.StringUtils;
import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.events.MessageEvent;

import java.util.HashMap;
import java.util.HashSet;

/**
 * Functionality for selecting a stream to Host during offline hours, should likely be made start and stoppable via the UI and chat commands (from mods+)
 */
public class HostSelector extends ListenerAdapter {
    private int MINIMUMFOLLOWERS = 100;

    private HashMap<String, Boolean> streamViability = new HashMap<>();
    private HashMap<String, String> streamVotes = new HashMap<>();

    @Override
    public void onMessage(MessageEvent event) throws Exception {
        String message = event.getMessage().toLowerCase();

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
            }
        }
    }
}

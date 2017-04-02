package twitch;

import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.events.JoinEvent;

/**
 * Created by Dons on 02-04-2017.
 */
public class TwitchChatExtras extends ListenerAdapter {

    @Override
    public void onJoin(JoinEvent event) {
        event.getUser();
    }
}

package twitch;

import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.events.MessageEvent;
import webapi.SpaceLaunch;
import webapi.XKCD;

public class TwitchChatEasterEggs extends ListenerAdapter {

    @Override
    public void onMessage(MessageEvent event)  {
        TwitchChatMessage chatMessage = new TwitchChatMessage(event);
        switch (chatMessage.getMessageCommand()) {
            case "!randomxkcd": XKCD.randomXKCDRequest(); break;
            case "!spacelaunch": SpaceLaunch.spaceLaunchRequest("any"); break;
            case "!spacexlaunch": SpaceLaunch.spaceLaunchRequest("spacex"); break;
            case "!nextspacelaunch": SpaceLaunch.spaceLaunchRequest("next"); break;
        }
    }
}

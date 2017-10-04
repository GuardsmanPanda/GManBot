package twitch;

import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.events.MessageEvent;
import twitch.dataobjects.TwitchChatMessage;
import webapi.Twitchv5;

public class TwitchChatModCommands extends ListenerAdapter {


    //TODO: consider only doing game change if the game has >0 current viewers. or 'if exists'
    //TODO: use twitch search feature to match game on partially correct text
    @Override
    public void onMessage(MessageEvent event) {
        if (!event.getTags().get("user-type").equalsIgnoreCase("mod") || !event.getMessage().startsWith("!")) return;

        TwitchChatMessage message = new TwitchChatMessage(event);
        switch (message.getMessageCommand()) {
            case "!settitle" : Twitchv5.setChannelTitle(message.getMessageContent()); break;
            case "!setgame" : Twitchv5.setChannelGame(message.getMessageContent()); break;
        }
    }
}

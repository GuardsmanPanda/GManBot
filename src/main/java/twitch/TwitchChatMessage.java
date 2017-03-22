package twitch;

import org.pircbotx.hooks.events.MessageEvent;

import java.util.Map;

public class TwitchChatMessage {
    public Boolean isSubOrPrime = false;
    public boolean isMod = false;
    public String displayName = "";
    public String message = "";
    public String userID = "";
    public String color = "";


    public TwitchChatMessage(MessageEvent e) {
        Map<String, String> tags = e.getTags();
        if (tags.get("subscriber").equalsIgnoreCase("1") || tags.get("badges").contains("premium")) isSubOrPrime = true;
        if (tags.get("mod").equalsIgnoreCase("1")) isMod = true;

        displayName = tags.get("display-name");
        if (displayName.isEmpty()) displayName = e.getUser().getNick();

        message = e.getMessage();
        userID = tags.get("user-id");
        color = tags.get("color");
    }

    /**
     * Utility method for bot commands starting with !command
     * @return returns everything after the first space, if no space is present in the message than return the entire message.
     */
    public String getMessageContent() {
        if (message.contains(" ")) return message.substring(message.indexOf(" ")).trim();
        else return message;
    }
}

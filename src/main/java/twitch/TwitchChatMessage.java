package twitch;

import org.pircbotx.hooks.events.MessageEvent;

import java.util.Map;

public class TwitchChatMessage {
    public final Boolean isSubOrPrime;
    public final boolean isMod;
    public final String displayName;
    public final String message;
    public final String userID;
    public final String color;


    public TwitchChatMessage(MessageEvent e) {
        Map<String, String> tags = e.getTags();
        isSubOrPrime = tags.get("subscriber").equalsIgnoreCase("1") || tags.get("badges").contains("premium");
        isMod = tags.get("mod").equalsIgnoreCase("1");

        displayName = (tags.get("display-name").isEmpty()) ? e.getUser().getNick() : tags.get("display-name");

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
        else return "";
    }

    public String getMessageCommand() {
        return message.split(" ")[0].toLowerCase().trim();
    }
}

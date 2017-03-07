package twitch;

import core.GBUtility;
import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.events.MessageEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

/**
 * Created by Dons on 28-11-2016.
 */
public class NameSelector extends ListenerAdapter {
    private static final HashMap<String, String> nameSuggestions = new HashMap<>();
    private static int NAMESUGGESTIONTIME = 60; // In seconds
    private static boolean isRunning = false;
    private static final Random random;

    static {
        random = new Random();
    }


    public static void startNameSelector(int maxLength) {
        nameSuggestions.clear();
        isRunning = true;
        TwitchChat.sendMessage("Suggest a name! .. by typing: !name nameSuggestionHere");
    }

    public static void selectName() {
        ArrayList<String> twitchNames = new ArrayList<>(nameSuggestions.keySet());
        String winner = twitchNames.get(random.nextInt(twitchNames.size()));
        String winningName = nameSuggestions.get(winner);

        TwitchChat.sendMessage("Chosen Name: " + winningName);
        GBUtility.copyAndPasteString(winningName);
        nameSuggestions.remove(winner);
    }

    @Override
    public void onMessage(MessageEvent event) {
        String content = GBUtility.getIRCMessageContent(event.getMessage());
        if (isRunning && event.getMessage().startsWith("!name") && !content.isEmpty()) {
            String twitchName = event.getUser().getNick();
            nameSuggestions.put(twitchName, content);
            //Add extra chance for a subs name to get picked
            if (event.getTags().get("subscriber").equalsIgnoreCase("1")) {
                System.out.println("Found subscriber");
                nameSuggestions.put(twitchName + "subVote", content);
            }
        }
    }
}

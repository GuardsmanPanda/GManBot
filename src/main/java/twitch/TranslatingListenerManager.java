package twitch;

import utility.GBUtility;
import org.pircbotx.hooks.Event;
import org.pircbotx.hooks.events.MessageEvent;
import org.pircbotx.hooks.managers.ThreadedListenerManager;

import java.util.HashMap;
import java.util.concurrent.ExecutorService;


public class TranslatingListenerManager extends ThreadedListenerManager {
    private final HashMap<String, String> commandTranslationMap = new HashMap<>();

    public TranslatingListenerManager() {
        fillTranslationMap();
    }

    public TranslatingListenerManager(ExecutorService pool) {
        super(pool);
        fillTranslationMap();
    }

    @Override
    public void onEvent(Event event) {
        if (event instanceof MessageEvent) {
            String message = ((MessageEvent) event).getMessage();
            String command = message.split(" ")[0].toLowerCase();
            if (command.startsWith("!")) {
                if (commandTranslationMap.containsKey(command)) {
                    String newMessage = message.replace(command, commandTranslationMap.get(command));
                    super.onEvent(new MessageEvent(event.getBot(), ((MessageEvent) event).getChannel(), ((MessageEvent) event).getChannelSource(), ((MessageEvent) event).getUserHostmask(), ((MessageEvent) event).getUser(), newMessage, ((MessageEvent) event).getTags()));
                    return;
                } else {
                    GBUtility.writeTextToFile(command + " not recognised on message: " + message + System.lineSeparator(), "output/UnknownCommands.txt", true);
                }
            }
        }
        super.onEvent(event);
    }

    private void fillTranslationMap() {
        commandTranslationMap.put("!ratereminder", "!ratereminder");
        commandTranslationMap.put("!ratingreminder", "!ratereminder");
        commandTranslationMap.put("!songreminder", "!ratereminder");
        commandTranslationMap.put("!addratereminder", "!ratereminder");
        commandTranslationMap.put("!addsongreminder", "!ratereminder");
        commandTranslationMap.put("!addratingreminder", "!ratereminder");

        commandTranslationMap.put("!removeratereminder", "!removeratereminder");
        commandTranslationMap.put("!removeratingreminder", "!removeratereminder");
        commandTranslationMap.put("!removesongreminder", "!removeratereminder");

        commandTranslationMap.put("!rate", "!rate");
        commandTranslationMap.put("!ratr", "!rate");
        commandTranslationMap.put("!songrate", "!rate");
        commandTranslationMap.put("!ratesong", "!rate");
        commandTranslationMap.put("!rate1", "!rate 1");
        commandTranslationMap.put("!rate2", "!rate 2");
        commandTranslationMap.put("!rate3", "!rate 3");
        commandTranslationMap.put("!rate4", "!rate 4");
        commandTranslationMap.put("!rate5", "!rate 5");
        commandTranslationMap.put("!rate6", "!rate 6");
        commandTranslationMap.put("!rate7", "!rate 7");
        commandTranslationMap.put("!rate8", "!rate 8");
        commandTranslationMap.put("!rate9", "!rate 9");
        commandTranslationMap.put("!rate10", "!rate 10");
        commandTranslationMap.put("!rate11", "!rate 11");

        commandTranslationMap.put("!rategame", "!rategame");
        commandTranslationMap.put("!gamerate", "!rategame");
        commandTranslationMap.put("!gamerating", "!rategame");

        commandTranslationMap.put("!setwelcomemessage","!setwelcomemessage");
        commandTranslationMap.put("!welcomemessage","!setwelcomemessage");

        commandTranslationMap.put("!followage","!followage");
        commandTranslationMap.put("!folloage","!followage");

        commandTranslationMap.put("!setflag","!setflag");
        commandTranslationMap.put("!setflash","!setflag");
        commandTranslationMap.put("!flag", "!setflag");
        commandTranslationMap.put("!steflag", "!setflag");
        commandTranslationMap.put("!addflag", "!setflag");
        commandTranslationMap.put("!setcountry", "!setflag");
        commandTranslationMap.put("!removeflag","!setflag none");

        commandTranslationMap.put("!chatstats", "!chatstats");
        commandTranslationMap.put("!chatstast", "!chatstats");
        commandTranslationMap.put("!chatstatus", "!chatstats");
        commandTranslationMap.put("!chatstas", "!chatstats");
        commandTranslationMap.put("!chatstaterino", "!chatstats");
        commandTranslationMap.put("!chatsta", "!chatstats");
        commandTranslationMap.put("!chatsts", "!chatstats");

        commandTranslationMap.put("!spacelaunch", "!spacelaunch");
        commandTranslationMap.put("!spacexlaunch", "!spacexlaunch");
        commandTranslationMap.put("!nextspacexlaunch", "!spacexlaunch");
        commandTranslationMap.put("!nextspacelaunch", "!nextspacelaunch");

        commandTranslationMap.put("!emotestats", "!emotestats");
        commandTranslationMap.put("!bobemotestats", "!emotestats");

        commandTranslationMap.put("!allemotestats", "!allemotestats");
        commandTranslationMap.put("!topemotestats", "!allemotestats");

        commandTranslationMap.put("!myemotestats", "!myemotestats");
        commandTranslationMap.put("!selfemotestats", "!myemotestats");
    }
}

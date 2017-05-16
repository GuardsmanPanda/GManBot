package twitch;

import org.pircbotx.hooks.Event;
import org.pircbotx.hooks.events.MessageEvent;
import org.pircbotx.hooks.managers.ThreadedListenerManager;
import utility.GBUtility;

import java.util.HashMap;


public class TranslatingListenerManager extends ThreadedListenerManager {
    //TODO comtemplate a 'full translationmap' so message such as '!random xkcd' can be translated to '!randomxkcd'
    private final HashMap<String, String> commandTranslationMap = new HashMap<>();

    public TranslatingListenerManager() {
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
        commandTranslationMap.put("!songreminder", "!ratereminder");
        commandTranslationMap.put("!ratingreminder", "!ratereminder");
        commandTranslationMap.put("!addratereminder", "!ratereminder");
        commandTranslationMap.put("!addsongreminder", "!ratereminder");
        commandTranslationMap.put("!addratingreminder", "!ratereminder");

        commandTranslationMap.put("!removeratereminder", "!removeratereminder");
        commandTranslationMap.put("!removesongreminder", "!removeratereminder");
        commandTranslationMap.put("!removeratingreminder", "!removeratereminder");
        commandTranslationMap.put("!stopsongreminder", "!removeratereminder");

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
        commandTranslationMap.put("!followtime","!followage");
        commandTranslationMap.put("!folloage","!followage");

        commandTranslationMap.put("!setflag","!setflag");
        commandTranslationMap.put("!flag", "!setflag");
        commandTranslationMap.put("!setflash","!setflag");
        commandTranslationMap.put("!steflag", "!setflag");
        commandTranslationMap.put("!addflag", "!setflag");
        commandTranslationMap.put("!setcountry", "!setflag");
        commandTranslationMap.put("!removeflag","!setflag none");

        commandTranslationMap.put("!chatstats", "!chatstats");
        commandTranslationMap.put("!chatsta", "!chatstats");
        commandTranslationMap.put("!chatsts", "!chatstats");
        commandTranslationMap.put("!chatstat", "!chatstats");
        commandTranslationMap.put("!chatstst", "!chatstats");
        commandTranslationMap.put("!chatstas", "!chatstats");
        commandTranslationMap.put("!chatstaty", "!chatstats");
        commandTranslationMap.put("!chatstast", "!chatstats");
        commandTranslationMap.put("!chatstatus", "!chatstats");
        commandTranslationMap.put("!chatstates", "!chatstats");
        commandTranslationMap.put("!chatstaterino", "!chatstats");
        commandTranslationMap.put("!chatstatszino", "!chatstats");

        commandTranslationMap.put("!spacelaunch", "!spacelaunch");

        commandTranslationMap.put("!spacexlaunch", "!spacexlaunch");
        commandTranslationMap.put("!nextspacexlaunch", "!spacexlaunch");

        commandTranslationMap.put("!nextspacelaunch", "!nextspacelaunch");

        commandTranslationMap.put("!emotestats", "!emotestats");
        commandTranslationMap.put("!emotes", "!emotestats");
        commandTranslationMap.put("!bobemotes", "!emotestats");
        commandTranslationMap.put("!bobemotestats", "!emotestats");

        commandTranslationMap.put("!allemotestats", "!allemotestats");
        commandTranslationMap.put("!allemotes", "!allemotestats");
        commandTranslationMap.put("!topemotes", "!allemotestats");
        commandTranslationMap.put("!topemotestats", "!allemotestats");

        commandTranslationMap.put("!myemotestats", "!myemotestats");
        commandTranslationMap.put("!myemotes", "!myemotestats");
        commandTranslationMap.put("!myemotesstats", "!myemotestats");
        commandTranslationMap.put("!selfemotestats", "!myemotestats");
        commandTranslationMap.put("!personalemotestats", "!myemotestats");

        commandTranslationMap.put("!randomxkcd", "!randomxkcd");
        commandTranslationMap.put("!randomxkck", "!randomxkcd");
        commandTranslationMap.put("!randomxked", "!randomxkcd");
        commandTranslationMap.put("!randomxkdc", "!randomxkcd");
        commandTranslationMap.put("!randomkxcd", "!randomxkcd");
        commandTranslationMap.put("!randommxkcd", "!randomxkcd");
        
        commandTranslationMap.put("!activehours", "!activehours");
        commandTranslationMap.put("!topactivehours", "!activehours");
        commandTranslationMap.put("!activehoursinchat", "!activehoursinchat");
        commandTranslationMap.put("!topactivehoursinchat", "!activehoursinchat");

        commandTranslationMap.put("!idlehours", "!idlehours");
        commandTranslationMap.put("!topidlehours", "!idlehours");
        commandTranslationMap.put("!idlehoursinchat", "!idlehoursinchat");
        commandTranslationMap.put("!topidlehoursinchat", "!idlehoursinchat");

        commandTranslationMap.put("!chatlines", "!chatlines");
        commandTranslationMap.put("!topchatlines", "!chatlines");
        commandTranslationMap.put("!chatlinesinchat", "!chatlinesinchat");
        commandTranslationMap.put("!topchatlinesinchat", "!chatlinesinchat");

        commandTranslationMap.put("!bobcoins", "!bobcoins");
        commandTranslationMap.put("!topbobcoins", "!bobcoins");
        commandTranslationMap.put("!bobcoinsinchat", "!bobcoinsinchat");
        commandTranslationMap.put("!topbobcoinsinchat", "!bobcoinsinchat");

        commandTranslationMap.put("!emoteusage", "!emoteusage");
        commandTranslationMap.put("!emoteuseage", "!emoteusage");
        commandTranslationMap.put("!topemoteusage", "!emoteusage");
        commandTranslationMap.put("!topemoteuseage", "!emoteusage");
        commandTranslationMap.put("!emoteusageinchat", "!emoteusageinchat");
        commandTranslationMap.put("!emoteuseageinchat", "!emoteusageinchat");
        commandTranslationMap.put("!topemoteusageinchat", "!emoteusageinchat");
        commandTranslationMap.put("!topemoteuseageinchat", "!emoteusageinchat");

        commandTranslationMap.put("!mystreambirthday", "!mystreambirthday");
        commandTranslationMap.put("!mystreambirthdate", "!mystreambirthday");
        commandTranslationMap.put("!mybirthday", "!mystreambirthday");
        commandTranslationMap.put("!mybirthdate", "!mystreambirthday");

        commandTranslationMap.put("!stathide", "!stathide");
        commandTranslationMap.put("!hidestats", "!stathide");
        commandTranslationMap.put("!statunhide", "!statunhide");
        commandTranslationMap.put("!unhidestats", "!statunhide");
        commandTranslationMap.put("!removestathide", "!statunhide");
    }
}

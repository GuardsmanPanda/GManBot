package twitch;

import org.pircbotx.hooks.Event;
import org.pircbotx.hooks.events.MessageEvent;
import org.pircbotx.hooks.managers.ThreadedListenerManager;
import utility.GBUtility;

import java.util.HashMap;
import java.util.Set;
import java.util.stream.Collectors;

public class TranslatingListenerManager extends ThreadedListenerManager {
    private static final HashMap<String, String> fullTranslationMap = new HashMap<>();
    private static final HashMap<String, String> commandTranslationMap = new HashMap<>();

    static {
        fillFullTranslationMap();
        fillCommandTranslationMap();
    }

    @Override
    public void onEvent(Event event) {
        if (event instanceof MessageEvent) {
            String message = ((MessageEvent) event).getMessage();
             if (message.startsWith("!")) {
                String newMessage = fullTranslationMap.getOrDefault(message.toLowerCase(), message);
                String command = newMessage.split(" ")[0].toLowerCase();
                if (commandTranslationMap.containsKey(command)) {
                    newMessage = commandTranslationMap.get(command) + newMessage.substring(command.length());
                    super.onEvent(new MessageEvent(event.getBot(), ((MessageEvent) event).getChannel(), ((MessageEvent) event).getChannelSource(), ((MessageEvent) event).getUserHostmask(), ((MessageEvent) event).getUser(), newMessage, ((MessageEvent) event).getTags()));
                    return;
                } else {
                    GBUtility.writeTextToFile(command + " not recognised on message: " + message, "output/UnknownCommands.txt", true);
                }
            }
        }
        super.onEvent(event);
    }


    public static Set<String> getTranslatedCommands() {
        return commandTranslationMap.values().stream().filter(command -> !command.contains(" ")).collect(Collectors.toSet());
    }

    private static void fillFullTranslationMap() {
        fullTranslationMap.put("!setflag", "!setflag random");

        fullTranslationMap.put("!random xkcd", "!randomxkcd");
        fullTranslationMap.put("!latest xkcd", "!latestxkcd");
    }

    private static void fillCommandTranslationMap() {
        //mod commands
        commandTranslationMap.put("!settitle", "!settitle");
        commandTranslationMap.put("!settittle", "!settitle");
        commandTranslationMap.put("!changetittle", "!settitle");
        commandTranslationMap.put("!setstreamtitle", "!settitle");
        commandTranslationMap.put("!setgame", "!setgame");
        commandTranslationMap.put("!setstreamgame", "!setgame");


        //other commands
        commandTranslationMap.put("!commands", "!commands");
        commandTranslationMap.put("!commandlist", "!commands");
        commandTranslationMap.put("!commandslist", "!commands");
        commandTranslationMap.put("!help", "!commands");
        commandTranslationMap.put("!playlist", "!playlist");
        commandTranslationMap.put("!music", "!playlist");
        commandTranslationMap.put("!spotify", "!playlist");

        commandTranslationMap.put("!commandstats", "!commandstats");
        commandTranslationMap.put("!commandstatsinchat", "!commandstatsinchat");

        commandTranslationMap.put("!flagstats", "!flagstats");
        commandTranslationMap.put("!flagstatsinchat", "!flagstatsinchat");


        commandTranslationMap.put("!github", "!github");
        commandTranslationMap.put("!githb", "!github");
        commandTranslationMap.put("!git", "!github");

        commandTranslationMap.put("!uptime", "!uptime");

        commandTranslationMap.put("!text", "!text");
        commandTranslationMap.put("!texts", "!text");
        commandTranslationMap.put("!textt", "!text");
        commandTranslationMap.put("!texxt", "!text");
        commandTranslationMap.put("!texty", "!text");
        commandTranslationMap.put("!textr", "!text");
        commandTranslationMap.put("!text1", "!text");
        commandTranslationMap.put("!textx", "!text");
        commandTranslationMap.put("!textr", "!text");
        commandTranslationMap.put("!texter", "!text");
        commandTranslationMap.put("!texterino", "!text");
        commandTranslationMap.put("!texterinos", "!text");

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

        commandTranslationMap.put("!quotereminder", "!quotereminder");
        commandTranslationMap.put("!addquotereminder", "!quotereminder");

        commandTranslationMap.put("!removequotereminder", "!removequotereminder");

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
        commandTranslationMap.put("!setwelcomeomessage","!setwelcomemessage");

        commandTranslationMap.put("!followage","!followage");
        commandTranslationMap.put("!followtime","!followage");
        commandTranslationMap.put("!folloage","!followage");
        commandTranslationMap.put("!followed","!followage");

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
        commandTranslationMap.put("!spacex", "!spacexlaunch");
        commandTranslationMap.put("!nextspacexlaunch", "!spacexlaunch");

        commandTranslationMap.put("!nextspacelaunch", "!nextspacelaunch");

        commandTranslationMap.put("!emotestats", "!emotestats");
        commandTranslationMap.put("!emotes", "!emotestats");
        commandTranslationMap.put("!bobemotes", "!emotestats");
        commandTranslationMap.put("!bobemotestats", "!emotestats");

        commandTranslationMap.put("!emotestatsinchat", "!emotestatsinchat");
        commandTranslationMap.put("!emotesinchat", "!emotestatsinchat");
        commandTranslationMap.put("!bobemotesinchat", "!emotestatsinchat");
        commandTranslationMap.put("!bobemotestatsinchat", "!emotestatsinchat");

        commandTranslationMap.put("!allemotestats", "!allemotestats");
        commandTranslationMap.put("!allemotes", "!allemotestats");
        commandTranslationMap.put("!topemotes", "!allemotestats");
        commandTranslationMap.put("!topemotestats", "!allemotestats");

        commandTranslationMap.put("!allemotestatsinchat", "!allemotestatsinchat");
        commandTranslationMap.put("!allemotesinchat", "!allemotestatsinchat");
        commandTranslationMap.put("!topemotesinchat", "!allemotestatsinchat");
        commandTranslationMap.put("!topemotestatsinchat", "!allemotestatsinchat");

        commandTranslationMap.put("!myemotestats", "!myemotestats");
        commandTranslationMap.put("!myemotes", "!myemotestats");
        commandTranslationMap.put("!myemotesstats", "!myemotestats");
        commandTranslationMap.put("!selfemotestats", "!myemotestats");
        commandTranslationMap.put("!myemoteusage", "!myemotestats");
        commandTranslationMap.put("!personalemotestats", "!myemotestats");

        commandTranslationMap.put("!randomxkcd", "!randomxkcd");
        commandTranslationMap.put("!randomxkck", "!randomxkcd");
        commandTranslationMap.put("!randomxked", "!randomxkcd");
        commandTranslationMap.put("!randomxkdc", "!randomxkcd");
        commandTranslationMap.put("!randomkxcd", "!randomxkcd");
        commandTranslationMap.put("!randommxkcd", "!randomxkcd");
        commandTranslationMap.put("!xkcd", "!randomxkcd");

        commandTranslationMap.put("!latestxkcd", "!latestxkcd");
        commandTranslationMap.put("!latestxkck", "!latestxkcd");
        commandTranslationMap.put("!latestxked", "!latestxkcd");
        commandTranslationMap.put("!latestxkdc", "!latestxkcd");
        commandTranslationMap.put("!latestkxcd", "!latestxkcd");
        commandTranslationMap.put("!latestmxkcd", "!latestxkcd");
        
        commandTranslationMap.put("!activehours", "!activehours");
        commandTranslationMap.put("!tophours", "!activehours");
        commandTranslationMap.put("!topactivehours", "!activehours");
        commandTranslationMap.put("!tophoursinchat", "!activehours");
        commandTranslationMap.put("!activehoursinchat", "!activehoursinchat");
        commandTranslationMap.put("!acivehoursinchat", "!activehoursinchat");
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
        commandTranslationMap.put("!topcoins", "!bobcoins");
        commandTranslationMap.put("!bobcoinsinchat", "!bobcoinsinchat");
        commandTranslationMap.put("!topbobcoinsinchat", "!bobcoinsinchat");

        commandTranslationMap.put("!songratings", "!songratings");
        commandTranslationMap.put("!songsrated", "!songratings");
        commandTranslationMap.put("!songratingsinchat", "!songratingsinchat");
        commandTranslationMap.put("!songsratedinchat", "!songratingsinchat");

        commandTranslationMap.put("!emoteusage", "!emoteusage");
        commandTranslationMap.put("!emoteuseage", "!emoteusage");
        commandTranslationMap.put("!topemoteusage", "!emoteusage");
        commandTranslationMap.put("!topemoteuseage", "!emoteusage");
        commandTranslationMap.put("!peoplewhoemotethemost", "!emoteusage");
        commandTranslationMap.put("!emoteusageinchat", "!emoteusageinchat");
        commandTranslationMap.put("!emoteuseageinchat", "!emoteusageinchat");
        commandTranslationMap.put("!topemoteusageinchat", "!emoteusageinchat");
        commandTranslationMap.put("!topemoteuseageinchat", "!emoteusageinchat");

        commandTranslationMap.put("!mystreambirthday", "!mystreambirthday");
        commandTranslationMap.put("!mystreambirthdate", "!mystreambirthday");
        commandTranslationMap.put("!mybirthday", "!mystreambirthday");
        commandTranslationMap.put("!mybirthdate", "!mystreambirthday");
        commandTranslationMap.put("!chatbirthday", "!mystreambirthday");

        commandTranslationMap.put("!stathide", "!stathide");
        commandTranslationMap.put("!hidestats", "!stathide");
        commandTranslationMap.put("!statunhide", "!statunhide");
        commandTranslationMap.put("!unhidestats", "!statunhide");
        commandTranslationMap.put("!removestathide", "!statunhide");

        commandTranslationMap.put("!totalstats", "!totalstats");
        commandTranslationMap.put("!allstats", "!totalstats");
        commandTranslationMap.put("!alllstats", "!totalstats");
        commandTranslationMap.put("!sendtotalstats", "!totalstats");

        commandTranslationMap.put("!seen", "!seen");
        commandTranslationMap.put("!seem", "!seen");
        commandTranslationMap.put("!lastseen", "!seen");

        commandTranslationMap.put("!quote", "!quote");
        commandTranslationMap.put("!qoute", "!quote");
        commandTranslationMap.put("!quotes", "!quote");
        commandTranslationMap.put("!qote", "!quote");

        commandTranslationMap.put("!douglasadams", "!douglasadams");
        commandTranslationMap.put("!adams", "!douglasadams");

        commandTranslationMap.put("!pratchett", "!pratchett");
        commandTranslationMap.put("!prachett", "!pratchett");
        commandTranslationMap.put("!pratchet", "!pratchett");
        commandTranslationMap.put("!pratchtet", "!pratchett");
        commandTranslationMap.put("!terrypratchett", "!pratchett");

        commandTranslationMap.put("!sanderson", "!sanderson");
        commandTranslationMap.put("!brandonsanderson", "!sanderson");

        commandTranslationMap.put("!rothfuss", "!rothfuss");
        commandTranslationMap.put("!patrickrothfuss", "!rothfuss");

        commandTranslationMap.put("!stephenking","!stephenking");
        commandTranslationMap.put("!king","!stephenking");

        commandTranslationMap.put("!brentweeks", "!brentweeks");
        commandTranslationMap.put("!weeks", "!brentweeks");

        commandTranslationMap.put("!robinhobb", "!robinhobb");
        commandTranslationMap.put("!hobb", "!robinhobb");

        commandTranslationMap.put("!rrmarting", "!rrmartin");
        commandTranslationMap.put("!martin", "!rrmartin");
        commandTranslationMap.put("!georgemartin", "!rrmartin");
        commandTranslationMap.put("!georgerrmartin", "!rrmartin");

        commandTranslationMap.put("!tolkien", "!tolkien");
        commandTranslationMap.put("!carlin", "!carlin");
        commandTranslationMap.put("!gaiman", "!gaiman");
        commandTranslationMap.put("!abercrombie", "!abercrombie");
        commandTranslationMap.put("!joeabercrombie", "!abercrombie");
        commandTranslationMap.put("!scottlynch", "!scottlynch");
        commandTranslationMap.put("!doyle","!doyle");
        commandTranslationMap.put("!arthurconandoyle","!doyle");
        commandTranslationMap.put("!einstein","!einstein");
        commandTranslationMap.put("!herbert","!herbert");
        commandTranslationMap.put("!feynman","!feynman");

        commandTranslationMap.put("!herbert","!herbert");
        commandTranslationMap.put("!frankherbert", "!herbert");

        commandTranslationMap.put("!stevenerikson", "!erikson");
        commandTranslationMap.put("!erikson", "!erikson");

        commandTranslationMap.put("!lawrence", "!lawrence");
        commandTranslationMap.put("!marklawrence", "!lawrence");

        commandTranslationMap.put("!rowling", "!rowling");
        commandTranslationMap.put("!jkrowling", "!rowling");

        commandTranslationMap.put("!orwell", "!orwell");
        commandTranslationMap.put("!georgeorwell", "!orwell");

        commandTranslationMap.put("!heartsbob", "!heartsbob");
        commandTranslationMap.put("!heartbob", "!heartsbob");

        commandTranslationMap.put("!donationcheck", "!donationcheck");
    }
}

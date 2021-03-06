package twitch;

import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.events.MessageEvent;
import twitch.dataobjects.SeenEvent;
import twitch.dataobjects.TwitchChatMessage;
import utility.Extra;
import utility.PrettyPrinter;
import webapi.*;
import webapi.dataobjects.Author;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.Year;
import java.util.ArrayDeque;
import java.util.stream.Collectors;

public class TwitchChatEasterEggs extends ListenerAdapter {

    @Override
    public void onMessage(MessageEvent event) {
        TwitchChatMessage chatMessage = new TwitchChatMessage(event);

        if (chatMessage.message.contains("youtu.be/") || chatMessage.message.contains("youtube.com/")) Youtube.sendVideoInformationFromMessage(chatMessage.message);

        switch (chatMessage.getMessageCommand()) {
            case "!commands": TwitchChat.sendMessage("List of Commands -> https://pastebin.com/fam4TAMg"); break;
            case "!codefights": TwitchChat.sendMessage("https://app.codesignal.com/signup/S2BuQaGDDcxbKMJC4/main"); break;
            case "!codewars": TwitchChat.sendMessage("https://www.codewars.com/r/n8qKWw"); break;
            case "!hackerearth": TwitchChat.sendMessage("http://hck.re/v9t6fx"); break;
            case "!github": TwitchChat.sendMessage("My GitHub -> https://github.com/GuardsmanPanda/GManBot"); break;
            case "!playlist": TwitchChat.sendMessage("Spotify Playlist -> https://open.spotify.com/user/1158619976/playlist/4gYCOPNjjBz9lYneVGE9dK"); break;
            case "!profile": TwitchChat.sendMessage("Poe Profile -> https://www.pathofexile.com/account/view-profile/ChampionBob/characters"); break;
            case "!dicegolf": diceGolf(chatMessage); break;
            case "!vanish": vanish(chatMessage); break;
            case "!roll": rollDice(chatMessage); break;
            case "!seen": seen(chatMessage); break;
            case "!uptime": uptime(); break;
            case "!randomxkcd": XKCD.xkcdRequest(true); break;
            case "!latestxkcd": XKCD.xkcdRequest(false); break;
            case "!spacelaunch": SpaceLaunch.spaceLaunchRequest("any"); break;
            case "!spacexlaunch": SpaceLaunch.spaceLaunchRequest("spacex"); break;
            case "!nextspacelaunch": SpaceLaunch.spaceLaunchRequest("next"); break;
            case "!mystreambirthday": streamBirthday(chatMessage); break;
            case "!quote": Quotes.sendRandomQuote(); break;
            case "!pratchett": Quotes.sendQuote(Author.TERRY_PRATCHETT); break;
            case "!sanderson": Quotes.sendQuote(Author.BRANDON_SANDERSON); break;
            case "!douglasadams": Quotes.sendQuote(Author.DOUGLAS_ADAMS); break;
            case "!rothfuss": Quotes.sendQuote(Author.PATRICK_ROTHFUSS); break;
            case "!tolkien": Quotes.sendQuote(Author.TOLKIEN); break;
            case "!gaiman": Quotes.sendQuote(Author.NEIL_GAIMAN); break;
            case "!abercrombie": Quotes.sendQuote(Author.JOE_ABERCROMBIE); break;
            case "!carlin": Quotes.sendQuote(Author.GEORGE_CARLIN); break;
            case "!doyle": Quotes.sendQuote(Author.ARTHUR_CONAN_DOYLE); break;
            case "!einstein": Quotes.sendQuote(Author.ALBERT_EINSTEIN); break;
            case "!erikson": Quotes.sendQuote(Author.STEVEN_ERIKSON); break;
            case "!lawrence": Quotes.sendQuote(Author.MARK_LAWRENCE); break;
            case "!orwell": Quotes.sendQuote(Author.GEORGE_ORWELL); break;
            case "!stephenking": Quotes.sendQuote(Author.STEPHEN_KING); break;
            case "!scottlynch": Quotes.sendQuote(Author.SCOTT_LYNCH); break;
            case "!rowling": Quotes.sendQuote(Author.J_K_ROWLING); break;
            case "!rrmartin": Quotes.sendQuote(Author.GEORGE_RR_MARTIN); break;
            case "!herbert": Quotes.sendQuote(Author.FRANK_HERBERT); break;
            case "!feynman": Quotes.sendQuote(Author.RICHARD_FEYNMAN); break;
            case "!brentweeks": Quotes.sendQuote(Author.BRENT_WEEKS); break;
            case "!robinhobb": Quotes.sendQuote(Author.ROBIN_HOBB); break;
        }
    }

    private void seen(TwitchChatMessage message) {
        if (!message.message.contains(" ")) return;
        SeenEvent seenEvent = new SeenEvent(message.message.split(" ")[1]);
        if (seenEvent.twitchID.isEmpty()) {
            TwitchChat.sendMessage("I do not know of this " + seenEvent.targetName +" bobSigh");
        } else if (seenEvent.twitchID.equalsIgnoreCase(Twitchv5.GMANBOTUSERID)) {
            TwitchChat.sendMessage("I Am Groo.. err GManBot!");
        } else if (TwitchChat.getUserIDsInChannel().contains(seenEvent.twitchID)) {
            TwitchChat.sendMessage(seenEvent.getDisplayName() + " is currently in the channel, silly.");
        } else if (seenEvent.getLastChatLine().isEmpty()) {
            TwitchChat.sendMessage("I have not seen " + seenEvent.getDisplayName() + " ¯\\_(ツ)_/¯");
        } else {
            TwitchChat.sendMessage("Last Seen " + seenEvent.getDisplayName() + " -> " + seenEvent.lastSeen() + " Ago \uD83D\uDD38 Saying: " + seenEvent.getLastChatLine());
        }
    }

    private void uptime() {
        Duration uptime = Twitchv5.getStreamUpTime();
        if (uptime.isZero()) TwitchChat.sendMessage("The Stream Is Offline You Fool!");
        else TwitchChat.sendMessage("The Stream Has Been Online For " + PrettyPrinter.timeStringFromDuration(uptime) + "!");
    }

    private void streamBirthday(TwitchChatMessage message) {
        LocalDateTime followTime = Twitchv5.getFollowDateTime(message.userID);
        if (followTime == null) {
            TwitchChat.sendMessage(message.displayName + ": You do not follow the stream bobSigh");
            return;
        }
        LocalDateTime now = LocalDateTime.now();
        String responseString = message.displayName + " - ";

        if (message.isSubOrPrime) {
            LocalDateTime subStreakStartTime = Twitchv5.getSubStreakStartDate(message.userID);
            if (subStreakStartTime != null) {
                LocalDateTime subBirthday = subStreakStartTime.withYear(Year.now().getValue());
                if (Duration.between(subBirthday, now).toDays() == 0) responseString += "bobCake bobCake Today is your Sub Birthday! bobCake bobCake ";
                else {
                    if (now.isAfter(subBirthday)) subBirthday = subBirthday.plusYears(1);
                    if (Duration.between(now, subBirthday).toDays() < 14) responseString += PrettyPrinter.timeStringFromDuration(Duration.between(now, subBirthday));
                    else responseString += PrettyPrinter.timeStringFromPeriod(Period.between(now.toLocalDate(), subBirthday.toLocalDate()));

                    responseString += " Until Sub Birthday! bobCake ";
                }
            }
        }

        LocalDateTime followBirthday = followTime.withYear(Year.now().getValue());
        if (Duration.between(followBirthday, now).toDays() == 0) responseString += "bobHype Today is your Follow Birthday! bobHype";
        else {
            if (now.isAfter(followBirthday)) followBirthday = followBirthday.plusYears(1);
            if (Duration.between(now, followBirthday).toDays() < 14) responseString += PrettyPrinter.timeStringFromDuration(Duration.between(now, followBirthday));
            else responseString += PrettyPrinter.timeStringFromPeriod(Period.between(now.toLocalDate(), followBirthday.toLocalDate()));

            responseString += " Until Follow Birthday! bobHype";
        }
        TwitchChat.sendMessage(responseString);
    }

    private void vanish(TwitchChatMessage message) {
        int seconds = 1;
        if (Extra.percentChance(5.0)) {
            TwitchChat.sendMessage("Boom Headshot.. Eat it!");
            seconds = 400;
        } else if (Extra.percentChance(1.0)) {
            TwitchChat.sendMessage("Critical strike! -- " + message.displayName + " is GONE!");
            seconds = 1500;
        }
        TwitchChat.sendMessage("/timeout " + message.displayName + " " + seconds);
    }

    private void rollDice(TwitchChatMessage message) {
        try {
            int size = Integer.parseInt(message.getMessageContent());
            int roll = Extra.randomInt(size)+1;
            TwitchChat.sendMessage("Rolling a d" + size + ": " + roll +"");
        } catch (NumberFormatException ignored) {

        }
    }

    private void diceGolf(TwitchChatMessage message) {
        ArrayDeque<Integer> stack = new ArrayDeque<>();
        try {
            stack.add(Integer.parseInt(message.getMessageContent()));
        } catch (NumberFormatException e) {
            //ignore
        }
        if (stack.size() == 0) stack.addLast(100);
        while (stack.peekLast() > 1) {
            stack.addLast(Extra.randomInt(stack.peekLast())+1);
        }
        String m = stack.stream().map(Object::toString).collect(Collectors.joining(", "));
        TwitchChat.sendMessage("DiceGolf ⛳ " + m + " == " + (stack.size()-1) + " ⛳");
    }
}

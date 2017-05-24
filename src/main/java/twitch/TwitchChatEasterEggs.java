package twitch;

import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.events.MessageEvent;
import twitch.dataobjects.SeenEvent;
import twitch.dataobjects.TwitchChatMessage;
import utility.PrettyPrinter;
import webapi.*;
import webapi.dataobjects.Authors;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.Year;

public class TwitchChatEasterEggs extends ListenerAdapter {

    @Override
    public void onMessage(MessageEvent event) {
        TwitchChatMessage chatMessage = new TwitchChatMessage(event);

        if (chatMessage.message.contains("youtu.be/") || chatMessage.message.contains("youtube.com/")) Youtube.sendVideoInformationFromMessage(chatMessage.message);

        switch (chatMessage.getMessageCommand()) {
            case "!commands": TwitchChat.sendMessage("List of Commands -> https://pastebin.com/fam4TAMg"); break;
            case "!github": TwitchChat.sendMessage("My GitHub -> https://github.com/GuardsmanPanda/GManBot"); break;
            case "!seen": seen(chatMessage); break;
            case "!randomxkcd": XKCD.xkcdRequest(true); break;
            case "!latestxkcd": XKCD.xkcdRequest(false); break;
            case "!spacelaunch": SpaceLaunch.spaceLaunchRequest("any"); break;
            case "!spacexlaunch": SpaceLaunch.spaceLaunchRequest("spacex"); break;
            case "!nextspacelaunch": SpaceLaunch.spaceLaunchRequest("next"); break;
            case "!mystreambirthday": streamBirthday(chatMessage); break;
            case "!pratchettquote": Quotes.sendQuote(Authors.TERRY_PRATCHETT); break;
            case "!sandersonquote": Quotes.sendQuote(Authors.BRANDON_SANDERSON); break;
            case "!douglasadamsquote": Quotes.sendQuote(Authors.DOUGLAS_ADAMS); break;
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
                if (Duration.between(subBirthday, now).toDays() == 0) responseString += "bobCake bobCake Today is your Sub Birthday! bobCake bobCake";
                else {
                    if (now.isAfter(subBirthday)) subBirthday = subBirthday.plusYears(1);
                    if (Duration.between(now, subBirthday).toDays() < 14) responseString += PrettyPrinter.timeStringFromDuration(Duration.between(now, subBirthday));
                    else responseString += PrettyPrinter.timeStringFromPeriod(Period.between(now.toLocalDate(), subBirthday.toLocalDate()));
                }
                responseString += " Until Sub Birthday! bobCake ";
            }
        }

        LocalDateTime followBirthday = followTime.withYear(Year.now().getValue());
        if (Duration.between(followBirthday, now).toDays() == 0) responseString += "bobHype Today is your Follow Birthday! bobHype";
        else {
            if (now.isAfter(followBirthday)) followBirthday = followBirthday.plusYears(1);
            if (Duration.between(now, followBirthday).toDays() < 14) responseString += PrettyPrinter.timeStringFromDuration(Duration.between(now, followBirthday));
            else responseString += PrettyPrinter.timeStringFromPeriod(Period.between(now.toLocalDate(), followBirthday.toLocalDate()));
        }
        responseString += " Until Follow Birthday! bobHype";

        TwitchChat.sendMessage(responseString);
    }
}

package twitch;

import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.events.MessageEvent;
import utility.PrettyPrinter;
import webapi.SpaceLaunch;
import webapi.Twitchv5;
import webapi.XKCD;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.Year;

public class TwitchChatEasterEggs extends ListenerAdapter {

    @Override
    public void onMessage(MessageEvent event) {
        TwitchChatMessage chatMessage = new TwitchChatMessage(event);
        switch (chatMessage.getMessageCommand()) {
            case "!randomxkcd": XKCD.xkcdRequest(true); break;
            case "!latestxkcd": XKCD.xkcdRequest(false); break;
            case "!spacelaunch": SpaceLaunch.spaceLaunchRequest("any"); break;
            case "!spacexlaunch": SpaceLaunch.spaceLaunchRequest("spacex"); break;
            case "!nextspacelaunch": SpaceLaunch.spaceLaunchRequest("next"); break;
            case "!mystreambirthday": streamBirthday(chatMessage); break;
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

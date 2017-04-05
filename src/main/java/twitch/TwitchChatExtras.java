package twitch;

import core.GBUtility;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.events.JoinEvent;
import org.pircbotx.hooks.events.MessageEvent;
import org.pircbotx.hooks.events.SetModeratedEvent;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;

/**
 * Created by Dons on 02-04-2017.
 */
public class TwitchChatExtras extends ListenerAdapter {

    @Override
    public void onMessage(MessageEvent event)  {
        TwitchChatMessage chatMessage = new TwitchChatMessage(event);
        switch (chatMessage.getMessageCommand()) {
            case "!followage": followAge(chatMessage); break;
        }
    }

    @Override
    public void onJoin(JoinEvent event) {
        System.out.println(event.getUser().getRealName());
        System.out.println(event.getUserHostmask().getNick());
        System.out.println(event.getUserHostmask().getHostname());
    }

    private static void followAge(TwitchChatMessage chatMessage) {
        LocalDate followDate = Twitchv5.getFollowDate(chatMessage.userID);
        if (followDate == null) return;
        if (followDate.isEqual(LocalDate.now())) {
            TwitchChat.sendMessage(chatMessage.displayName + ", You just followed the stream today! bobHype");
            return;
        }

        String followPeriodString = followDate.until(LocalDate.now()).toString();

        followPeriodString = followPeriodString.replace("P", "").replace("Y", " Years, ").replace("M", " Months, ").replace("D", " Days,").trim();
        followPeriodString = followPeriodString.replace("1 Years", "1 Year").replace(" 1 Months", "1 Month").replace(" 1 Days", "1 Day");
        followPeriodString = followPeriodString.substring(0, followPeriodString.length() - 1);

        String printString = chatMessage.displayName + ": Followed for " + followPeriodString + ".";
        TwitchChat.sendMessage(printString);
    }
}

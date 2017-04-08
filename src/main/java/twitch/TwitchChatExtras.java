package twitch;

import core.BobsDatabaseHelper;
import org.apache.commons.lang3.tuple.Triple;
import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.events.JoinEvent;
import org.pircbotx.hooks.events.MessageEvent;


import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;

public class TwitchChatExtras extends ListenerAdapter {
    private static HashMap<String, LocalDateTime> lastWelcomeMessageTime = new HashMap<>();
    private static final LocalDateTime startTime = LocalDateTime.now();

    @Override
    public void onMessage(MessageEvent event)  {
        TwitchChatMessage chatMessage = new TwitchChatMessage(event);
        switch (chatMessage.getMessageCommand()) {
            case "!followage": followAge(chatMessage); break;
            case "!setwelcomemessage": setWelcomeMessage(chatMessage); break;
        }
    }

    @Override // todo: add a 5-6 second delay on welcome message
    public void onJoin(JoinEvent event) {
        //ignore join events for the first 2 minutes of a restart to avoid mass channel spam.
        if (startTime.plusMinutes(2).isAfter(LocalDateTime.now())) return;

        Triple<String, String, Boolean> welcomeTriple = BobsDatabaseHelper.getDisplayNameWelcomeMessageAndHasSubbedStatus(event.getUserHostmask().getNick());
        String displayName = welcomeTriple.getLeft();
        String welcomeMessage = welcomeTriple.getMiddle();
        Boolean hasSubscribed = welcomeTriple.getRight();

        if (hasSubscribed && !welcomeMessage.equalsIgnoreCase("none")) {
            System.out.println("join " + displayName + " welcome message: " +welcomeMessage);
            if (welcomeMessage.startsWith("/") && !welcomeMessage.toLowerCase().startsWith("/me ")) return;

            if (lastWelcomeMessageTime.containsKey(displayName) && lastWelcomeMessageTime.get(displayName).isAfter(LocalDateTime.now().minus(2, ChronoUnit.HOURS))) {
                //we have recently sent a welcome message to the user
                System.out.println("Already sent welcome message for " + displayName);
            } else {
                new Thread(() -> {
                    try { Thread.sleep(4000); } catch (InterruptedException e) { e.printStackTrace(); }
                    if (welcomeMessage.toLowerCase().startsWith("/me ")) {
                        TwitchChat.sendAction(welcomeMessage.substring(4));
                    }
                    else {
                        TwitchChat.sendMessage(welcomeMessage);
                    }
                    lastWelcomeMessageTime.put(displayName, LocalDateTime.now());
                }).start();
            }
        }
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
    private static void setWelcomeMessage(TwitchChatMessage chatMessage) {
        BobsDatabaseHelper.setWelcomeMessage(chatMessage.userID, chatMessage.getMessageContent());
        if (chatMessage.isSubOrPrime) BobsDatabaseHelper.setHasSubscribed(chatMessage.userID);
    }
}

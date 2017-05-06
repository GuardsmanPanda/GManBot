package twitch;

import database.EmoteDatabase;
import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.events.MessageEvent;

import java.time.Duration;
import java.time.Instant;
import java.util.Comparator;
import java.util.Map;
import java.util.stream.Collectors;


public class TwitchChatStats extends ListenerAdapter {
    Instant nextEmoteStatTime = Instant.now();

    @Override
    public void onMessage(MessageEvent event) {
        TwitchChatMessage chatMessage = new TwitchChatMessage(event);

        switch (chatMessage.getMessageCommand()) {
            case "!emotestats": sendEmoteStats(chatMessage, false); break;
            case "!allemotestats": sendEmoteStats(chatMessage, true); break;
            case "!myemotestats": sendPersonalEmoteStats(chatMessage); break;
        }
    }



    private void sendPersonalEmoteStats(TwitchChatMessage chatMessage) {
        String outputString = chatMessage.displayName + " emotes";
        int days = 50000;
        try {
            days = Integer.parseInt(chatMessage.getMessageContent());
            outputString += "for the past " + days + " days";
        } catch (NumberFormatException nfe) { /*empty on purpose*/ }
        outputString += "! ";
        outputString += EmoteDatabase.getEmoteUsageFromUserID(chatMessage.userID, Duration.ofDays(days)).entrySet().stream()
                .sorted(Comparator.comparingInt(Map.Entry<String, Integer>::getValue).reversed())
                .limit(10)
                .map(entry -> entry.getKey() + " " + entry.getValue())
                .collect(Collectors.joining(" ▪️ "));
        TwitchChat.sendMessage(outputString);
    }
    private synchronized void sendEmoteStats(TwitchChatMessage chatMessage, boolean allEmotes) {
        if (nextEmoteStatTime.isAfter(Instant.now())) return;
        nextEmoteStatTime = Instant.now().plusSeconds(60);

        int days = 14;
        try { days = Integer.parseInt(chatMessage.getMessageContent()); } catch (NumberFormatException nfe) { /*empty on purpose*/ }

        String printString = "Emote usage for the past " + days + " days: ";
        printString += EmoteDatabase.getEmoteUsageByEmoteName(Duration.ofDays(days)).entrySet().stream()
                .filter(entry -> (allEmotes || entry.getKey().startsWith("bob")))
                .sorted(Comparator.comparingInt(Map.Entry<String, Integer>::getValue).reversed())
                .limit(20)
                .map(entry -> entry.getKey() + " " + entry.getValue())
                .collect(Collectors.joining(" \uD83D\uDD38 "));
        TwitchChat.sendMessage(printString);
    }
}

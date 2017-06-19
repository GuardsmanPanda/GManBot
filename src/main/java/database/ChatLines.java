package database;

import com.google.common.base.Strings;
import twitch.TranslatingListenerManager;
import twitch.TwitchChat;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ChatLines {


    public static Map<String, Long> commandUsageStats(boolean everyone) {
        Set<String> commands = TranslatingListenerManager.getTranslatedCommands();
        List<String> chatLines;

        if (everyone) {
            chatLines = BobsDatabase.getListFromSQL("SELECT chatLine FROM ChatLines WHERE chatLine LIKE '!%'", String.class);
        } else {
            Set<String> chatUserIDs = TwitchChat.getUserIDsInChannel();
            chatLines = BobsDatabase.getListFromSQL("SELECT chatline FROM ChatLines WHERE chatLine LIKE '!%' AND twitchUserID IN (?" + Strings.repeat(", ?", chatUserIDs.size() - 1) +")", String.class, chatUserIDs.toArray(new String[0]));
        }

        return chatLines.stream()
                .map(chatLine -> chatLine.split(" ")[0].toLowerCase())
                .filter(commands::contains)
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
    }
}

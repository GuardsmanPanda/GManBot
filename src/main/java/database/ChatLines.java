package database;

import twitch.TranslatingListenerManager;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ChatLines {


    public static Map<String, Long> commandUsageStats(boolean everyone) {
        Set<String> commands = TranslatingListenerManager.getTranslatedCommands();
        List<String> chatLines = List.of();
        if (everyone) {
            chatLines = BobsDatabase.getListFromSQL("SELECT chatLine FROM ChatLines WHERE chatLine LIKE '!%'", String.class);
        }

        return chatLines.stream()
                .map(chatLine -> chatLine.split(" ")[0].toLowerCase())
                .filter(commands::contains)
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
    }
}

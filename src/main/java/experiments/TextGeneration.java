package experiments;

import com.google.common.collect.ArrayListMultimap;
import database.BobsDatabase;
import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.events.MessageEvent;
import twitch.TwitchChat;

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;


public class TextGeneration extends ListenerAdapter {
    private static final ArrayListMultimap<String, String> textModel = ArrayListMultimap.create();
    private static final Random random = new Random();

    static {
        loadTextModel();
    }

    public static String generateText() {
        StringBuilder output = new StringBuilder();
        String lastWord = textModel.get("START").get(random.nextInt(textModel.get("START").size()));
        output.append(lastWord);

        while (output.length() < 250) {
            if (lastWord.endsWith("END")) {
                output.setLength(output.indexOf("END"));
                break;
            }
            output.append(" ");
            String[] lastWords = lastWord.split(" ");

            boolean oneWord = random.nextInt(6) != 0;
            if (oneWord) {
                List<String> newWords = textModel.get(lastWords[0]).stream()
                        .filter(word -> word.split(" ")[0].equals(lastWords[1]))
                        .collect(Collectors.toList());
                lastWord = newWords.get(random.nextInt(newWords.size()));
                output.append(lastWord.split(" ")[1]);
            } else {
                List<String> newWords = textModel.get(lastWords[1]);
                if (newWords.isEmpty()) {
                    System.out.println("Empty word list, found end of string");
                    break;
                }
                lastWord = newWords.get(random.nextInt(newWords.size()));
                output.append(lastWord);
            }
        }
        return output.toString();
    }

    @Override
    public void onMessage(MessageEvent event) {
        String message = event.getMessage();
        if (message.startsWith("!text")) TwitchChat.sendMessage(generateText());
    }

    private static void loadTextModel() {
        List<String> textList = BobsDatabase.getListFromSQL("SELECT chatLine FROM ChatLines WHERE LENGTH(chatLine) > 20 ORDER BY timeStamp DESC FETCH FIRST 200000 ROWS ONLY", String.class);
        System.out.println("TEXT LIST SIZE: " + textList.size());
        textList.stream()
                .filter(text -> !text.contains("http://") && !text.contains(".com") && !text.contains("https://"))
                .forEach(line -> {
                    String[] wordArray = line.split(" ");
                    String lastWord = "START";
                    for (int i = 1; i < wordArray.length; i++) {
                        textModel.put(lastWord, wordArray[i - 1] + " " + wordArray[i]);
                        lastWord = wordArray[i - 1];
                    }
                    textModel.put(lastWord, wordArray[wordArray.length - 1] + " END");
                });
    }
}

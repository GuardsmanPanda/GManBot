package experiments;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import database.BobsDatabase;
import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.events.MessageEvent;
import twitch.TwitchChat;

import java.util.List;
import java.util.Random;


public class TextGeneration extends ListenerAdapter {
    private static final Multimap<String, String> textModel = ArrayListMultimap.create();
    private static final Random random = new Random();

    static {
        loadTextModel();
    }

    public static String generateText() {
        StringBuffer output = new StringBuffer();
        String lastWord = "START";
         while (output.length() < 250) {
             String newWord = "END";
             if (output.length() < random.nextInt(150) + 100 || !textModel.get(lastWord).contains("END")) {
                 String[] wordArray = textModel.get(lastWord).toArray(new String[0]);
                 newWord = wordArray[random.nextInt(wordArray.length)];
             }

             if (newWord.equals("END")) break;
             if (!lastWord.equals("START")) output.append(" ");
             output.append(newWord);
             lastWord = newWord;
         }
         return output.toString();
    }

    @Override
    public void onMessage(MessageEvent event) {
        String message = event.getMessage();
        if (message.startsWith("!text")) TwitchChat.sendMessage(generateText());
    }

    private static void loadTextModel() {
        List<String> textList = BobsDatabase.getListFromSQL("SELECT chatLine FROM ChatLines WHERE LENGTH(chatLine) > 30 ORDER BY timeStamp DESC FETCH FIRST 100000 ROWS ONLY", String.class);
        textList.forEach(line -> {
            String[] wordArray = line.split(" ");
            String lastWord = "START";
            for (String word : wordArray) {
                if (!word.contains("http://") || !word.contains(".com") || !word.contains("https://")) {
                    textModel.put(lastWord, word);
                    lastWord = word;
                }
            }
            textModel.put(lastWord, "END");
        });
    }
}

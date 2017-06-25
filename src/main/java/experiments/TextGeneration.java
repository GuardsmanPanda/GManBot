package experiments;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ArrayListMultimap;
import database.BobsDatabase;
import jdk.incubator.http.HttpRequest;
import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.events.MessageEvent;
import twitch.TwitchChat;
import utility.Extra;
import utility.GBUtility;
import webapi.WebClient;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;


public class TextGeneration extends ListenerAdapter {
    private static final ArrayListMultimap<String, String> textModel = ArrayListMultimap.create();

    static {
        loadTextModel();
        addHearthstoneCardTextToModel();
    }


    public static String generateText() {
        try {
            StringBuilder output = new StringBuilder();
            String lastWord = Extra.getRandomElement(textModel.get("START"));
            output.append(lastWord);

            while (output.length() < 250) {
                if (lastWord.endsWith("END")) {
                    output.setLength(output.indexOf("END"));
                    break;
                }
                output.append(" ");
                String[] lastWords = lastWord.split(" ");

                boolean oneWord = Extra.percentChance(85);
                if (oneWord) {
                    List<String> newWords = textModel.get(lastWords[0]).stream()
                            .filter(word -> word.split(" ")[0].equals(lastWords[1]))
                            .collect(Collectors.toList());
                    if (newWords.isEmpty()) {
                        System.out.println("empty newwords");
                    }
                    lastWord = Extra.getRandomElement(newWords);
                    output.append(lastWord.split(" ")[1]);
                } else {
                    List<String> newWords = textModel.get(lastWords[1]);
                    if (newWords.isEmpty()) {
                        System.out.println("Empty word list, found end of string");
                        break;
                    }
                    lastWord = Extra.getRandomElement(newWords);
                    output.append(lastWord);
                }
            }
            return output.toString();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException();
        }
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
                .filter(text -> !text.contains("feces"))
                .map(text -> text.replaceAll("\\s+", " "))
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

    private static void addHearthstoneCardTextToModel() {
        File cardData = new File("Data/Hearthstone/CardData.txt");
        if (!cardData.exists()) {
            System.out.println("loading HS data");
            HttpRequest request = HttpRequest.newBuilder(URI.create("https://api.hearthstonejson.com/v1/19632/enUS/cards.collectible.json"))
                    .setHeader("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:54.0) Gecko/20100101 Firefox/54.0")
                    .GET().build();
            GBUtility.writeTextToFile(WebClient.getJSonNodeFromRequest(request).toString(), cardData.getPath(), false);
        }

        try {
            JsonNode root = new ObjectMapper().readTree(cardData);
            List<String> cardText = StreamSupport.stream(root.spliterator(), false)
                    .filter(node -> node.has("text"))
                    .filter(node -> node.has("flavor"))
                    .flatMap(node -> Stream.of(node.get("text").asText(), node.get("flavor").asText()))
                    .map(text -> text.replaceAll("</?b>", "").replaceAll("\\s+", " ").trim())
                    .filter(text -> text.length() > 15)
                    .collect(Collectors.toList());

            cardText.forEach(text -> {
                String[] wordArray = text.split(" ");
                 if (wordArray.length > 2) {
                    for (int i = 2; i < wordArray.length; i++) {
                            textModel.put(wordArray[i-2], wordArray[i - 1] + " " + wordArray[i]);
                    }
                        textModel.put(wordArray[wordArray.length - 2], wordArray[wordArray.length - 1] + " END");
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Could nto read card data");
        }
        System.out.println("loaded HS data");
    }
}

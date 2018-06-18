package webapi;

import com.fasterxml.jackson.databind.JsonNode;
import jdk.incubator.http.HttpRequest;
import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.events.MessageEvent;
import twitch.TwitchChat;

import java.net.URI;
import java.net.URLEncoder;

public class MagicTCG extends ListenerAdapter {
    private static final String APICARD = "https://api.scryfall.com/cards/named?fuzzy=";

    public static void main(String[] args) {
        System.out.println(getCardNodeFromName("twilight prophet").get("oracle_text").asText().replaceAll("\n", ", "));
    }

    @Override
    public void onMessage(MessageEvent event) throws Exception {
        if (event.getMessage().startsWith("!card ")) {
            JsonNode node = getCardNodeFromName(event.getMessage().substring(6));
            if (node.get("object").asText().equals("error")) {
                TwitchChat.sendMessage("Couldn't find card: " + event.getMessage().substring(6));
                return;
            }

            String desc = node.get("name").asText() +
                    " - " +
                    node.get("mana_cost").asText().replaceAll("[{}]", "") +
                    " - " +
                    node.get("type_line").asText() +
                    " - " +
                    node.get("oracle_text").asText().replaceAll("\n", ", ");
            TwitchChat.sendMessage(desc);
        }
    }

    private static JsonNode getCardNodeFromName(String cardName) {
        HttpRequest request = HttpRequest.newBuilder(URI.create(APICARD + URLEncoder.encode(cardName)))
                .header("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:54.0) Gecko/20100101 Firefox/54.0")
                .header("Keep-Alive", "timeout=60")
                .GET().build();
        return WebClient.getJSonNodeFromRequest(request);
    }
}

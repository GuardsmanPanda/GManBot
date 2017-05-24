package webapi;

import database.BobsDatabase;
import jdk.incubator.http.HttpRequest;
import twitch.TwitchChat;
import utility.FinalPair;
import webapi.dataobjects.Authors;

import java.net.URI;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

//todo: consider supporting quote by tag as well (we could technically cheat and make the tag and 'author') .. but then we maybe wanna knowt he quote auther and.. now its complciated.
public class Quotes {
    private static Instant nextQuoteTime = Instant.now();

    public static void sendQuote(Authors author) {
        if (nextQuoteTime.isAfter(Instant.now())) return;
        nextQuoteTime = Instant.now().plusSeconds(12);

        FinalPair<Integer, String> quotePair = BobsDatabase.getPairFromSQL("SELECT quoteID, quote FROM AuthorQuotes WHERE name = ? ORDER BY quoteID FETCH FIRST ROW ONLY", author.name);
        if (quotePair == null) {
            TwitchChat.sendMessage("Finding quotes by " + author.name + ", please wait a minute.");
            updateQuotes(author);
            quotePair = BobsDatabase.getPairFromSQL("SELECT quoteID, quote FROM AuthorQuotes WHERE name = ? ORDER BY quoteID FETCH FIRST ROW ONLY", author.name);
        }

        if (quotePair != null) {
            String quote = quotePair.second;
            //if it fits in 1 line
            if (quote.length() < 260) TwitchChat.sendMessage(quote + " -" + author.name);
            else {
                int index = quote.indexOf(" ", 240);
                TwitchChat.sendMessage(quote.substring(0, index));
                TwitchChat.sendMessage(quote.substring(index + 1, quote.length()) + " -" + author.name);
            }
            BobsDatabase.executePreparedSQL("DELETE FROM AuthorQuotes WHERE quoteID = " + quotePair.first);
        } else {
            TwitchChat.sendMessage("Could not find any quotes from " + author.name + " ¯\\_(ツ)_/¯");
        }
    }

    private static void updateQuotes(Authors author) {
        System.out.println("Updating Quotes From " + author);
        getQuoteList(author).forEach(quote -> BobsDatabase.executePreparedSQL("INSERT INTO AuthorQuotes(name, quote) VALUES(?, ?)", author.name, quote));
        System.out.println("Updated Quotes From " + author);
    }

    /**
     * Returns a list of quotes from the author, the amount is define in the pages in the Author enum.
     * @param author
     * @return a SHUFFLED list of quotes.
     */
    private static List<String> getQuoteList(Authors author) {
        List<String> returnList = new ArrayList<>();
        for (int i = 1; i <= author.pages; i++) {
            HttpRequest request = HttpRequest.newBuilder(URI.create(author.getQuoteURL() + "?page=" + i)).GET().build();
            String webPage = WebClient.getStringFromRequest(request);
            String[] rawQuoteArray = webPage.split("<div class=\"quoteText\">");

            for (String rawQuote : rawQuoteArray) {
                if (rawQuote.contains("<br>  &#8213;\n    <a class=\"authorOrTitle\"")) {
                    String quote = rawQuote.substring(0, rawQuote.indexOf("<br>  &#8213;\n    <a class=\"authorOrTitle\"")).trim();
                    quote = quote.replace("&ldquo;","“");
                    quote = quote.replace("&rdquo;","”");
                    quote = quote.replaceAll("<br />"," ");
                    quote = quote.replaceAll("</?[^>]>", "");
                    //only add quotes shorter than 500 characters
                    if (quote.length() < 500) returnList.add(quote);
                    else System.out.println("discarding quote");
                }
            }
            System.out.println("Got page " + i + ", " + returnList.size() + " quotes found so far");
        }
        System.out.println("Found " + returnList.size() + " Quotes For " + author.name);
        Collections.shuffle(returnList);
        return returnList;
    }
}

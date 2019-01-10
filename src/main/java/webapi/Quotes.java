package webapi;

import database.BobsDatabase;
import twitch.TwitchChat;
import utility.FinalPair;
import webapi.dataobjects.Author;

import java.time.Instant;
import java.util.Collections;
import java.util.List;

//todo: consider supporting quote by tag as well (we could technically cheat and make the tag and 'author') .. but then we maybe wanna knowt he quote auther and.. now its complciated.
public class Quotes {
    private static Instant nextQuoteTime = Instant.now();

    public static void sendQuote(Author author) {
        synchronized (Quotes.class) {
            if (nextQuoteTime.isAfter(Instant.now())) return;
            nextQuoteTime = Instant.now().plusSeconds(2);
        }

        String quote = getQuote(author);

        if (quote.isEmpty()) {
            TwitchChat.sendMessage("Finding quotes by " + author.name + ", please wait a minute.");
            updateQuotes(author);
            quote = getQuote(author);
        }

        if (!quote.isEmpty()) {
            TwitchChat.sendMessage(quote + " -" + author.name);
        } else {
            TwitchChat.sendMessage("Could not find any quotes from " + author.name + " ¯\\_(ツ)_/¯");
        }
    }
    public static void sendRandomQuote() {
        sendQuote(Author.randomAuthor());
    }


    /**
     * Gets a random quote, Warning this method may block if quotes need to be refreshed.
     * @return
     */
    public static FinalPair<String, String> getRandomQuote() {
        Author author = Author.randomAuthor();
        String quote = getQuote(author);
        if (quote.isEmpty()) {
            updateQuotes(author);
            quote = getQuote(author);
        }
        return new FinalPair<>(quote, author.name);
    }

    private static String getQuote(Author author) {
        FinalPair<Integer, String> quotePair = BobsDatabase.getPairFromSQL("SELECT quoteID, quote FROM AuthorQuotes WHERE name = ? ORDER BY quoteID FETCH FIRST ROW ONLY", author.name);
        if (quotePair != null) {
            BobsDatabase.executePreparedSQL("DELETE FROM AuthorQuotes WHERE quoteID = " + quotePair.first);
            return quotePair.second;
        } else {
            return "";
        }
    }

    private static void updateQuotes(Author author) {
        System.out.println("Updating Quotes From " + author);
        getQuoteList(author).forEach(quote -> BobsDatabase.executePreparedSQL("INSERT INTO AuthorQuotes(name, quote) VALUES(?, ?)", author.name, quote));
        System.out.println("Updated Quotes From " + author);
    }

    /**
     * Returns a list of quotes from the author, the amount is define in the pages in the Author enum.
     * @param author
     * @return a SHUFFLED list of quotes.
     */
    private static List<String> getQuoteList(Author author) {
        List<String> returnList =  author.getQuotes(470);
        System.out.println("Found " + returnList.size() + " Quotes For " + author.name);
        Collections.shuffle(returnList);
        return returnList;
    }
}

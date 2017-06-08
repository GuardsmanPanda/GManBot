package webapi.dataobjects;

import jdk.incubator.http.HttpRequest;
import webapi.WebClient;

import java.net.URI;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public enum Author {
    TERRY_PRATCHETT("Terry Pratchett", 12, "1654.Terry_Pratchett"),
    DOUGLAS_ADAMS("Douglas Adams", 6,"4.Douglas_Adams"),
    PATRICK_ROTHFUSS("Patrick Rothfuss", 6,"108424.Patrick_Rothfuss"),
    TOLKIEN("J.R.R. Tolkien", 6,"656983.J_R_R_Tolkien"),
    SCOTT_LYNCH("Scott Lynch", 5,"73149.Scott_Lynch"),
    GEORGE_CARLIN("George Carlin", 6,"22782.George_Carlin"),
    JOE_ABERCROMBIE("Joe Abercrombie", 6, "276660.Joe_Abercrombie"),
    NEIL_GAIMAN("Neil Gaiman", 6, "1221698.Neil_Gaiman"),
    ARTHUR_CONAN_DOYLE("Arthur Conan Doyle", 6, "2448.Arthur_Conan_Doyle"),
    ALBERT_EINSTEIN("Albert Einstein", 6, "9810.Albert_Einstein"),
    WINSTON_CHURCHILL("Winston S. Churchill", 4, "14033.Winston_S_Churchill"),
    STEPHEN_KING("Stephen King", 6, "3389.Stephen_King"),
    GEORGE_RR_MARTIN("George R.R. Martin", 6, "346732.George_R_R_Martin"),
    FRANK_HERBERT("Frank Herbert", 4, "58.Frank_Herbert"),
    BRANDON_SANDERSON("Brandon Sanderson", 6, "38550.Brandon_Sanderson");

    private static final Author[] values = Author.values();
    private static final Random random = new Random();

    public final String name;
    private final int pages;
    private final String urlEnd;

    Author(String authorName, int quotePages, String quoteURLEnd) {
        name = authorName;
        pages = quotePages;
        urlEnd = quoteURLEnd;
    }
    //todo return list of quotes, this allows for use of more sources then just goodreads
    public Stream<String> getQuoteURLs() {
        return IntStream.rangeClosed(1, pages)
                .mapToObj(page -> "https://www.goodreads.com/author/quotes/" + urlEnd + "?page=" + page);
    }

    //TODO implement this
    public List<String> getQuotes(int maxLength) {
        return IntStream.rangeClosed(1, pages)
                .mapToObj(page -> "https://www.goodreads.com/author/quotes/" + urlEnd + "?page=" + page)
                .map(url -> HttpRequest.newBuilder(URI.create(url)).GET().build())
                .map(WebClient::getStringFromRequest)
                .flatMap(rawWebPage -> Stream.of(rawWebPage.split("<div class=\"quoteText\">")))
                .filter(rawQuote -> rawQuote.contains("<br>  &#8213;\n    <a class=\"authorOrTitle\""))
                .map(rawQuote -> rawQuote.substring(0, rawQuote.indexOf("<br>  &#8213;\n    <a class=\"authorOrTitle\"")).trim())
                .map(quote -> quote.replaceAll("&ldquo;", "“"))
                .map(quote -> quote.replaceAll("&rdquo;", "”"))
                .map(quote -> quote.replaceAll("<br />", " "))
                .map(quote -> quote.replaceAll("</?[^>]>", ""))
                .filter(quote -> quote.length() < maxLength) // make sure a quote message fits in 1 line on twitch
                .collect(Collectors.toList());
    }
    public static Author randomAuthor() { return values[random.nextInt(values.length)]; }
}

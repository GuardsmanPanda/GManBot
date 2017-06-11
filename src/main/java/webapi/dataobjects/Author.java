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
    ALBERT_EINSTEIN("Albert Einstein", 6, "9810.Albert_Einstein"),
    ARTHUR_CONAN_DOYLE("Arthur Conan Doyle", 6, "2448.Arthur_Conan_Doyle"),
    BRANDON_SANDERSON("Brandon Sanderson", 6, "38550.Brandon_Sanderson"),
    BRENT_WEEKS("Brent Weeks", 6, "1370283.Brent_Weeks"),
    DOUGLAS_ADAMS("Douglas Adams", 6,"4.Douglas_Adams"),
    FRANK_HERBERT("Frank Herbert", 4, "58.Frank_Herbert"),
    GEORGE_CARLIN("George Carlin", 6,"22782.George_Carlin"),
    GEORGE_RR_MARTIN("George R.R. Martin", 6, "346732.George_R_R_Martin"),
    NEIL_GAIMAN("Neil Gaiman", 6, "1221698.Neil_Gaiman"),
    PATRICK_ROTHFUSS("Patrick Rothfuss", 6,"108424.Patrick_Rothfuss"),
    TOLKIEN("J.R.R. Tolkien", 6,"656983.J_R_R_Tolkien"),
    SCOTT_LYNCH("Scott Lynch", 5,"73149.Scott_Lynch"),
    JOE_ABERCROMBIE("Joe Abercrombie", 6, "276660.Joe_Abercrombie"),
    TERRY_PRATCHETT("Terry Pratchett", 12, "1654.Terry_Pratchett"),
    ROBIN_HOBB("Robin Hobb", 6, "25307.Robin_Hobb"),
    STEPHEN_KING("Stephen King", 6, "3389.Stephen_King"),
    RICHARD_FEYNMAN("Richard Feynman", 6, "1429989.Richard_Feynman"),
    WINSTON_CHURCHILL("Winston S. Churchill", 4, "14033.Winston_S_Churchill");

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

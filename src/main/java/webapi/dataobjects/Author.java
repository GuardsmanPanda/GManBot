package webapi.dataobjects;

import jdk.incubator.http.HttpRequest;
import utility.Extra;
import webapi.WebClient;

import java.net.URI;
import java.util.List;
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
    GEORGE_ORWELL("George Orwell", 6, "3706.George_Orwell"),
    GEORGE_RR_MARTIN("George R.R. Martin", 6, "346732.George_R_R_Martin"),
    J_K_ROWLING("J. K. Rowling", 8, "1077326.J_K_Rowling"),
    JOE_ABERCROMBIE("Joe Abercrombie", 6, "276660.Joe_Abercrombie"),
    MARK_LAWRENCE("Mark Lawrence", 6, "4721536.Mark_Lawrence"),
    NEIL_GAIMAN("Neil Gaiman", 6, "1221698.Neil_Gaiman"),
    PATRICK_ROTHFUSS("Patrick Rothfuss", 6,"108424.Patrick_Rothfuss"),
    RICHARD_FEYNMAN("Richard Feynman", 6, "1429989.Richard_Feynman"),
    ROBIN_HOBB("Robin Hobb", 6, "25307.Robin_Hobb"),
    SCOTT_LYNCH("Scott Lynch", 5,"73149.Scott_Lynch"),
    STEPHEN_KING("Stephen King", 6, "3389.Stephen_King"),
    STEVEN_ERIKSON("Steven Erikson", 6, "31232.Steven_Erikson"),
    TERRY_PRATCHETT("Terry Pratchett", 15, "1654.Terry_Pratchett"),
    TOLKIEN("J.R.R. Tolkien", 6,"656983.J_R_R_Tolkien");

    private static final Author[] values = Author.values();

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
                .filter(rawQuote -> rawQuote.contains("class=\"authorOrTitle\">"))
                .map(rawQuote -> rawQuote.substring(0, rawQuote.indexOf("<span class=\"authorOrTitle\">")).trim())
                .map(rawQuote -> rawQuote.substring(0, rawQuote.lastIndexOf("<br>")).trim())
                .map(quote -> quote.replaceAll("&ldquo;", "“"))
                .map(quote -> quote.replaceAll("&rdquo;", "”"))
                .map(quote -> quote.replaceAll("<br />", " "))
                .map(quote -> quote.replaceAll("</?[^>]>", ""))
                .map(quote -> quote.replaceAll("\\s+", " "))
                .filter(quote -> quote.length() < maxLength) // make sure a quote message fits in 1 line on twitch
                .collect(Collectors.toList());
    }
    public static Author randomAuthor() { return values[Extra.randomInt(values().length)]; }
}

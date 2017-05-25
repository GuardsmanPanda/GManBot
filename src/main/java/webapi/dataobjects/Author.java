package webapi.dataobjects;

import java.util.concurrent.ThreadLocalRandom;

public enum Author {
    TERRY_PRATCHETT("Terry Pratchett", 12, "1654.Terry_Pratchett"),
    DOUGLAS_ADAMS("Douglas Adams", 6,"4.Douglas_Adams"),
    PATRICK_ROTHFUSS("Patrick Rothfuss", 6,"108424.Patrick_Rothfuss"),
    TOLKIEN("J.R.R. Tolkien", 6,"656983.J_R_R_Tolkien"),
    BRANDON_SANDERSON("Brandon Sanderson", 6, "38550.Brandon_Sanderson");
    public final String name;
    public final int pages;
    private final String urlEnd;
    Author(String authorName, int quotePages, String quoteURLEnd) {
        name = authorName;
        pages = quotePages;
        urlEnd = quoteURLEnd;
    }
    public String getQuoteURL() {
        return "https://www.goodreads.com/author/quotes/" + urlEnd;
    }
    public static Author randomAuthor() { return Author.class.getEnumConstants()[ThreadLocalRandom.current().nextInt(Author.class.getEnumConstants().length)]; }
}

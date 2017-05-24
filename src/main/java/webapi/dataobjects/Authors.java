package webapi.dataobjects;

public enum Authors {
    TERRY_PRATCHETT("Terry Pratchett", 10, "1654.Terry_Pratchett"),
    DOUGLAS_ADAMS("Douglas Adams",5,"4.Douglas_Adams"),
    BRANDON_SANDERSON("Brandon Sanderson", 3, "38550.Brandon_Sanderson");
    public final String name;
    public final int pages;
    private final String urlEnd;
    Authors(String authorName, int quotePages, String quoteURLEnd) {
        name = authorName;
        pages = quotePages;
        urlEnd = quoteURLEnd;
    }
    public String getQuoteURL() {
        return "https://www.goodreads.com/author/quotes/" + urlEnd;
    }
}

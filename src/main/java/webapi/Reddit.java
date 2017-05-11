package webapi;


import jdk.incubator.http.HttpRequest;
import utility.PrettyPrinter;

import java.net.URI;

public class Reddit {
    private static final String USERAGENT = "desktop:gmanbot:v4.0 (by /u/guardsmanbob)";
    private static String accessToken = "55MUphSbl7uANbGUxndo4i4AxrU";


    public static void main(String[] args) {
        HttpRequest request = HttpRequest.newBuilder(URI.create("https://reddit.com/r/MachineLearning/top/.json"))
                .header("User-Agent", USERAGENT)
                .GET()
                .build();

        PrettyPrinter.prettyPrintJSonNode(WebClient.getJSonNodeFromRequest(request));
    }

    /**
     * Watches a subreddit and prints to the twitch channel when a new post breaks into the current top posts
     *
     * @param subReddit
     * @param topPostToBeat
     */
    public static void watchSubReddit(String subReddit, int topPostToBeat, int sinceDays) {
        //get score of topPostToBeat sinceDays


        //periodically check if a new post beats this score


        // print post title + url to the channel
    }



}

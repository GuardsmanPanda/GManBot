import twitter4j.*;
import twitter4j.conf.ConfigurationBuilder;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import com.google.common.io.Files;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Map;


/**
 * Class responsible for posting in chat when a new tweet is made
 * Additional functionality for sending tweets directly from chat or UI should be considered
 */
public class TwitterBot {
    //TODO: Make const pkg to contain all constants used by program.
    private static final int MAX_TWIT_COUNT = 140; // Twitter message length
    private static final int OAUTH_ENTRIES = 8; // 4 headers for 4 tokens, 8 lines
    private static Thread twitterThread;
    private static Twitter twitter;
    private static boolean runTwitterBot = false;


    static {
        // TODO: Clean up reading of tokens
        Map<String,String> tokens = getTwitOAuth();
        ConfigurationBuilder cb = new ConfigurationBuilder();
        cb.setDebugEnabled(true)
                .setOAuthConsumerKey(tokens.get("Consumer_Key"))
                .setOAuthConsumerSecret(tokens.get("Consumer_Secret"))
                .setOAuthAccessToken(tokens.get("Access_Token"))
                .setOAuthAccessTokenSecret(tokens.get("Access_Token_Secret"));
        TwitterFactory tf = new TwitterFactory(cb.build());
        twitter = tf.getInstance();
    }

    /**
     * Reads a formatted text file that contains oauth tokens for Twitter API. Hides it from view of other people.
     *
     * TODO: Currently have to read a file in the following format
     * Consumer_Key
     * xxxxxxxxx
     * Consumer_Secret
     * xxxxxxxxx
     * Access_Token
     * xxxxxxxxxx
     * Access_Token_Secret
     * xxxxxxxxx
     *
     * Better way of handling this? Perhaps we will make one large file containing all passwords? Be able to parse list from that.
     *
     * To generate your own Oauth tokens, visit http://twitter.com/oauth_clients/new
     *
     * @return Map that contains all the OAuth Tokens needed by twitter API
     */
    private static Map<String,String> getTwitOAuth(){
        File oAuthTokens = new File("Data/Twitter_OAuth.txt");
        Map<String, String> oAuthTokenMap = new HashMap<>();
        List<String> listOfTokens;

        if (oAuthTokens.exists()) {
            try {
                listOfTokens = Files.readLines(oAuthTokens,Charset.defaultCharset());
                if(listOfTokens.size() == OAUTH_ENTRIES) {
                    for (int index = 0; index < listOfTokens.size(); index = index + 2) {
                        if(listOfTokens.get(index).equals("Consumer_Key")){
                            oAuthTokenMap.put("Consumer_Key",listOfTokens.get(index+1));
                        } else if(listOfTokens.get(index).equals("Consumer_Secret")) {
                            oAuthTokenMap.put("Consumer_Secret",listOfTokens.get(index+1));
                        } else if(listOfTokens.get(index).equals("Access_Token")) {
                            oAuthTokenMap.put("Access_Token",listOfTokens.get(index+1));
                        } else if(listOfTokens.get(index).equals("Access_Token_Secret")){
                            oAuthTokenMap.put("Access_Token_Secret",listOfTokens.get(index+1));
                        }
                    }
                    if(oAuthTokenMap.size()!= 4) {
                        System.out.println("File did not contain 4 needed Twitter OAuth Tokens");
                    }
                } else {
                    System.out.println("File is not formatted correctly");
                }
            } catch (IOException e) {
                System.out.println("Error when attempting to read the Twitter_OAuth text file!");
                e.printStackTrace();
            }
        } else {
            System.out.println("OAuth Tokens must be located in Data/Twitter_OAuth.txt");
        }
        return oAuthTokenMap;
    }


    /**
     * Start watching @guardsmanbob on twitter and alert in chat when new tweets emerge
     */
    public static void startTwitterWatch() {
        // TODO: Thread this up
        twitterThread = new Thread()  {
            public void run() {
                runTwitterBot = true;
                while(runTwitterBot) {
                    try {
                        readTweets();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        twitterThread.start();
    }

    private static void readTweets(){
        // TODO: Do something more than just reading latest tweets sent to Gmanbot
        try{
            List<Status> statuses = twitter.getHomeTimeline();
            System.out.println("Showing home timeline.");
            for (Status status : statuses) {
                System.out.println(status.getUser().getName() + ":" +
                        status.getText());
            }
            twitterThread.sleep(10000);
        } catch (Exception e) {
            System.out.println("Error in retrieving timeline");
        }

    }

    public static void stopTwitterWatch() {
        runTwitterBot = false;
    }

    /**
     * @param tweet
     */
    public static void sendTweet(String tweet) {
        try {
            // TODO: Do some more sanitizing of inputs besides length
            if (tweet.length() < MAX_TWIT_COUNT) {
                twitter.updateStatus(tweet);
                System.out.println("Sent new tweet: " + tweet.toString());
             } else {
                System.out.println("We've got an error, " + tweet.toString() +
                    " is " +(tweet.length()-MAX_TWIT_COUNT) +" characters too long");
            }

        } catch(Exception e){
            System.out.println(e.getMessage());
        }

    }

    /**
     * I'm envisioning this to be called to send private messages to users, winners of something?
     */
    public static void sendDirectMessage(String tweet){

    }

    /**
     * Perhaps a way for users to interact with Gmanbot?
     * @param user
     */
    public static ResponseList<DirectMessage> getDirectMessageFromUser(String user){

        return null;
    }
}

package core;

import twitch.TwitchChat;
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
 * Additional functionality for sending tweets directly from chat or ui should be considered
 */

/**
 * Usage of this class: Upon startup, initialization is started by reading a separate file that contains OAuth tokens.
 *
 * Other classes can interact with Twitter with the following functions:
 *
 * sendTweet(String): Updates the status as provided by initiator
 *
 * startTwitterWatch/endTwitterWatch(): Starts/Stops the thread that monitors any tweet that includes mention of user
 * (i.e. contains the phrase @guardsmanbob)
 *
 * startBobWatch/endBobWatch(): Starts/stops the thread that monitors any tweet that is sent by @guardsmanbob
 */

public class TwitterBot {
    //TODO: Make const pkg to contain all constants used by program.
    private static final int MAX_TWIT_COUNT = 140; // Twitter message length
    private static final int MAX_DM_COUNT = 10000; // Direct message length
    private static final int OAUTH_ENTRIES = 8; // 4 headers for 4 tokens, 8 lines
    private static Twitter twitter;
    private static boolean runMentionCheck = false;
    private static boolean runBobCheck = false;
    private static Status lastTweetToMe = null;
    private static Status lastTweetByMe = null;
    private static final int SECONDS = 1000;
    private static final int MINUTES = SECONDS*60;
    private static final int HOURS = MINUTES*60;

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
     * Allows other methods to generate a tweet
     *
     * @param tweet: 140 character limit of message to post to Twitter
     */
    public static void sendTweet(String tweet) {
        try {
            // TODO: Do some more sanitizing of inputs besides length
            if (tweet.length() < MAX_TWIT_COUNT) {
                lastTweetByMe = twitter.updateStatus(tweet);
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
    public static void sendDirectMessage(String receiver,String tweet){
            try {
                if(tweet.length()<MAX_DM_COUNT) {
                    twitter.sendDirectMessage(receiver, tweet);
                    System.out.println("Sent private message: " + tweet +" to user: "+receiver);
                }else{
                    System.out.println("We've got an error, " + tweet +" is "
                            +(tweet.length()-MAX_TWIT_COUNT) +" characters too long");
                }
            } catch (TwitterException e) {
                System.out.println(e.getMessage());
            }
    }

    /**
     * Perhaps a way for users to interact with Gmanbot?
     * //TODO:
     * @param user
     */
    public static ResponseList<DirectMessage> getDirectMessageFromUser(String user){

        return null;
    }

    /**
     * Start watching @guardsmanbob on twitter and alert in chat when new tweets emerge
     */
    public static void startTwitterWatch() {

        Thread mentionThread = new Thread()  {
            public void run() {
                runMentionCheck = true;
                while(runMentionCheck) {
                    try {
                        readTweetsToUser();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        /**
         * makes sure the Thread isn't already running
         */
        if(!mentionThread.isAlive()) {
            mentionThread.start();
        }
    }

    /**
     * Stop reading mentions of @guardsmanbob.
     */
    public static void stopTwitterWatch() {
        runMentionCheck = false;
    }

    /**
     * Start thread that starts checking for tweets sent by Bob
     */
    public static void startBobWatch() {
        Thread bobThread = new Thread()  {
            public void run() {
                runBobCheck = true;
                while (runBobCheck) {
                    try {
                        readTweetsByUser();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        /**
         * makes sure the Thread isn't already running
         */
        if(!bobThread.isAlive()) {
            bobThread.start();
        }
    }

    /**
     * Stops checking for new tweets by Bob.
     */
    public static void stopBobWatch(){
        runBobCheck = false;
    }



    //--------------------------- Helper Functions ------------------------------------------

    /**
     * Reads a list of tweets that include username, and perform action if a new one has been received.
     *
     * // TODO: Do something more than just reading latest tweets sent to Gmanbot
     */
    private static void readTweetsToUser() {

        try{
            List<Status> statuses = twitter.getUserTimeline();

            checkLatestTweet(statuses.get(0), "mention");

            /** Debug code to show list of last mentions.
            System.out.println("Showing mentions.");
            for (Status status : statuses) {
                System.out.println(status.getUser().getName() + ":" +
                        status.getText());
            }
            */
            Thread.currentThread().sleep(60*SECONDS);
        } catch (Exception e) {
            System.out.println("Error in retrieving mention timeline");
            try {
                Thread.currentThread().sleep(60*SECONDS);
            } catch (InterruptedException e1) {
                e1.printStackTrace();
            }
        }

    }

    /**
     * Helper function to read tweets made by Bob
     */
    private static void readTweetsByUser() {
        try {
            List<Status> status = twitter.getUserTimeline();

            checkLatestTweet(status.get(0),"user");

            Thread.currentThread().sleep(60*SECONDS);

            TwitchChat.sendMessage("New Tweet by " + status.get(0).getUser().getName() + ": "+ status.get(0).getText());

        } catch (Exception e){
            System.out.println("Error in reading user timeline");
            try {
                Thread.currentThread().sleep(60*SECONDS);
            } catch (InterruptedException e1) {
                e1.printStackTrace();
            }
        }
    }

    /**
     * Helper method to determine whether a new tweet has been made.
     * @param newTweet the newest tweet in the list
     * @return true if there is a new tweet, false otherwise.
     */
    private static boolean checkLatestTweet(Status newTweet, String type){
        // If we get a new tweet (i.e. not the same user or same text as last read), perform action
        if(type.equals("mention")){
            if(lastTweetToMe == null){
                lastTweetToMe = newTweet;
                return true;
            }
            else if(!(lastTweetToMe.getText().equals(newTweet.getText())) && (lastTweetToMe.getUser().getName().equals(newTweet.getUser().getName()))) {
                lastTweetToMe = newTweet;
                return true;
            }
        } else if (type.equals("user")){
            if(lastTweetByMe == null){
                lastTweetByMe = newTweet;
                return true;
            }
            else if(!(lastTweetByMe.getText().equals(newTweet.getText())) && (lastTweetByMe.getUser().getName().equals(newTweet.getUser().getName()))) {
                lastTweetByMe = newTweet;
                return true;
            }
        }
        return false;
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

}

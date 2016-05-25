import twitter4j.*;
import twitter4j.conf.ConfigurationBuilder;

import java.util.List;




/**
 * Class responsible for posting in chat when a new tweet is made
 * Additional functionality for sending tweets directly from chat or UI should be considered
 */
public class TwitterBot {

    //TODO: Make const pkg to contain all constants used by program.
    private static final int MAX_TWIT_COUNT = 140;

    private static Twitter twitter;

    static {
        // TODO: Clean up reading of tokens
        ConfigurationBuilder cb = new ConfigurationBuilder();
        cb.setDebugEnabled(true)
                .setOAuthConsumerKey("*****")
                .setOAuthConsumerSecret("******")
                .setOAuthAccessToken("*****")
                .setOAuthAccessTokenSecret("*****");
        TwitterFactory tf = new TwitterFactory(cb.build());
        twitter = tf.getInstance();
    }

    /**
     * Start watching @guardsmanbob on twitter and alert in chat when new tweets emerge
     */
    public static void startTwitterWatch() {
        // TODO: Thread this up
        // TODO: Do something more than just reading latest tweets sent to Gmanbot
        try{
            List<Status> statuses = twitter.getHomeTimeline();
            System.out.println("Showing home timeline.");
            for (Status status : statuses) {
                System.out.println(status.getUser().getName() + ":" +
                        status.getText());
            }
        } catch (Exception e){
            System.out.println("Error in retrieving timeline");
        }

    }

    public static void stopTwitterWatch() {

    }

    /**
     * @param tweet
     */
    public static void sendTweet(String tweet) {
        try {
            // TODO: Do some more sanitizing of inputs
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

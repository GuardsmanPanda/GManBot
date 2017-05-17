package webapi;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import twitch.TwitchChat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class Reddit {
    //TODO: watch for live threads on teh front page (possibly in all subs we follow?)
    public enum TimeSpan {
        HOUR("hour"), DAY("day"), WEEK("week"), MONTH("month"), YEAR("year"), ALL("all");
        private String nameString;
        TimeSpan(String name) {
            nameString = name;
        }
        @Override
        public String toString() {
            return nameString;
        }
    }

    private static final String USERAGENT = "desktop:gmanbot:v4.0 (by /u/guardsmanbob)";
    //private static String accessToken = "55MUphSbl7uANbGUxndo4i4AxrU";
    private static Map<String, SubRedditWatcher> watchers = new HashMap<>();

    /**
     * Watches a subreddit and prints to the twitch channel when a new post breaks into the current top posts
     * //TODO: train a model to predict thread rating early, so we can post threads when they are 1-2hours old or even sooner.
     *
     * @param subReddit     the subreddit to watch
     * @param topPostToBeat the top post to beat
     */
    public static void watchSubReddit(String subReddit, int topPostToBeat, TimeSpan timeSpan, int updateFreqencyInMinutes) {
        watchers.put(subReddit, new SubRedditWatcher(subReddit, getPostScoreFromTopPost(subReddit, topPostToBeat, timeSpan), updateFreqencyInMinutes));
    }


    private static int getPostScoreFromTopPost(String subReddit, int postNumber, TimeSpan timeSpan) {
        assert (postNumber <= 100);
        JsonNode root = getJsonFromURL("https://www.reddit.com/r/" + subReddit + "/top/.json?limit=100&t=" + timeSpan);
        if (root != null && root.has("data")) {
            return root.get("data").get("children").get(postNumber - 1).get("data").get("score").asInt(0);
        }
        return 0;
    }

    private static List<RedditPost> getHotPosts(String subReddit, int numberOfPosts) {
        assert (numberOfPosts <= 100);
        JsonNode root = getJsonFromURL("https://www.reddit.com/r/" + subReddit + "/hot/.json?limit=" + numberOfPosts);
        if (root != null && root.has("data")) {
            return StreamSupport.stream(root.get("data").get("children").spliterator(), false)
                    .map(RedditPost::new)
                    .collect(Collectors.toList());
        }
        throw new RuntimeException("Getting hot posts went wrong");
    }

    private static JsonNode getJsonFromURL(String url) {
        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setRequestProperty("User-Agent", USERAGENT);
            connection.setRequestMethod("GET");
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                return new ObjectMapper().readTree(reader.lines().collect(Collectors.joining()));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }


    private static class SubRedditWatcher {
        private static ConcurrentHashMap.KeySetView<String, Boolean> alreadyPostedInChat = ConcurrentHashMap.newKeySet();
        private int minScoreForPost = 0;
        private String sub = "";

        public SubRedditWatcher(String subReddit, int scoreToBeat, int updateFrequency) {
            minScoreForPost = scoreToBeat;
            sub = subReddit;
            new Thread(() -> {
                try { Thread.sleep(ThreadLocalRandom.current().nextInt(400000)); } catch (InterruptedException e) { e.printStackTrace(); }
                System.out.println("Starting New SubReddit Watcher for r/" + subReddit + ", Score to Beat: " + scoreToBeat);
                fillAlreadyPostedSet();
                Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(this::checkForPost, 1, updateFrequency, TimeUnit.MINUTES);
            }).start();
        }

        private void fillAlreadyPostedSet() {
            getHotPosts(sub, 100).forEach(post -> {
                if (post.score > minScoreForPost) alreadyPostedInChat.add(post.url);
            });
        }

        private void checkForPost() {
            getHotPosts(sub, 10).forEach(post -> {
                if (post.score > minScoreForPost && !alreadyPostedInChat.contains(post.url)) {
                    alreadyPostedInChat.add(post.url);
                    TwitchChat.sendMessage("Top Post In r/" + post.subReddit + ": " + post.title + " -> " + post.getBestUrl(sub.equalsIgnoreCase("all")));
                }
            });
        }
    }
}

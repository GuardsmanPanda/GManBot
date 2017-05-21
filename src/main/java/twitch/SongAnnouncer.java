package twitch;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import core.StreamWebOverlay;
import database.BobsDatabase;
import database.BobsDatabaseHelper;
import database.SongDatabase;
import javafx.util.Pair;
import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.events.MessageEvent;
import twitch.dataobjects.TwitchChatMessage;
import utility.GBUtility;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.*;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class SongAnnouncer extends ListenerAdapter {
    private static final Map<String, String> ratingReminderMap = new HashMap<>();
    private static final Map<String, String> quoteReminderMap = new HashMap<>();
    private static final int STREAMDELAYINSECONDS = 10;
    private static String currentSong = "Guardsman Bob";
    private static String displayOnStreamSong = "Guardsman Bob";

    public SongAnnouncer(Path songFilePath) {
        watchSongFile(songFilePath);
        //Load the rating reminders
        BobsDatabase.getMultiMapFromSQL("SELECT twitchUserID, twitchDisplayName FROM TwitchChatUsers WHERE songRatingReminder = true", String.class, String.class)
                .forEach(ratingReminderMap::put);
    }

    //TODO: consider implementing a !randomsongsuggestion on a long cooldown to suggest youtube link to a song you have previously rated 11 (or havent rated)
    @Override
    public void onMessage(MessageEvent event) {
        TwitchChatMessage tcm = new TwitchChatMessage(event);
        if (tcm.message.toLowerCase().startsWith("!rate ")) {
            String songQuote = "none";
            try {
                int rating = Integer.parseInt(tcm.getMessageContent().split(" ")[0]);
                if (rating < 1 ) rating = 1;
                if (rating > 11) rating = 11;
                if (tcm.getMessageContent().contains(" ")) songQuote = tcm.getMessageContent().substring(tcm.getMessageContent().indexOf(" ")).trim();

                SongDatabase.addSongRating(tcm.userID, currentSong, rating, songQuote);

                //send new rating to overlay
                Pair<Float, Integer> songRatingPair = SongDatabase.getSongRating(displayOnStreamSong);
                ObjectNode rootNode = JsonNodeFactory.instance.objectNode();
                rootNode.put("type", "songRatingUpdate"); rootNode.put("songRating", String.format("%.2f", songRatingPair.getKey()));
                StreamWebOverlay.sendJsonToOverlay(rootNode);

                //displayOnStreamSongRating = songRatingPair.getKey();
            } catch (NumberFormatException nfe) {
                // Silently kill number format exceptions
            }
        } else {
            switch (tcm.getMessageCommand()) {
                case "!ratereminder": ratingReminderMap.put(tcm.userID, tcm.displayName); BobsDatabaseHelper.setSongRatingReminder(tcm.userID, tcm.displayName, true); break;
                case "!removeratereminder": ratingReminderMap.remove(tcm.userID); BobsDatabaseHelper.setSongRatingReminder(tcm.userID, tcm.displayName,false); break;
            }
        }
    }

    private static void songFileChange(String newSongName) {
        if (newSongName.equalsIgnoreCase("Guardsman Bob")) return;

        Pair<Float, Integer> lastSongPair = SongDatabase.getSongRating(displayOnStreamSong);
        String lastSongString = displayOnStreamSong + " ⏩ Rating: " + String.format("%.2f", lastSongPair.getKey()) + " [" + lastSongPair.getValue() + "]";

        Pair<Float, Integer> songRatingPair = SongDatabase.getSongRating(newSongName);
        float newSongRating = songRatingPair.getKey();
        displayOnStreamSong = newSongName;

        if (newSongRating < 7.7f) GBUtility.textToBob("Do you want to remove the song: " + newSongName + " ⏩ rating: " +newSongRating);

        //Create Json and send to overlay
        ObjectNode rootNode = JsonNodeFactory.instance.objectNode();
        rootNode.put("type", "songUpdate");
        rootNode.put("songName", newSongName);
        rootNode.put("songRating", String.format("%.2f", songRatingPair.getKey()));
        rootNode.put("songNumRatings", songRatingPair.getValue());
        rootNode.put("songQuote", SongDatabase.getSongQuote(newSongName, true));
        StreamWebOverlay.sendJsonToOverlay(rootNode);

        new Thread(() -> {
            try { Thread.sleep(1000 * STREAMDELAYINSECONDS); } catch (InterruptedException e) { e.printStackTrace(); }
            //Check if the song to be announced in chat is actually still playing
            if (displayOnStreamSong.equalsIgnoreCase(newSongName)) {
                currentSong = newSongName;
                SongDatabase.addSongPlay(newSongName);
                TwitchChat.sendMessage("\uD83C\uDFB8\uD83C\uDFBB Now Playing: " + newSongName + " \uD83D\uDD37\uD83D\uDD37 Last Song: " + lastSongString);

                //Add long delay before song rating reminder, so allow for people to rate the song and not be reminded.
                try { Thread.sleep(25000); } catch (InterruptedException e) { e.printStackTrace(); }
                Set<String> peopleInChat = TwitchChat.getActiveUserIDsInChannel(Duration.ofMinutes(90));
                String remindString = ratingReminderMap.keySet().stream()
                        .filter(peopleInChat::contains)
                        .filter(twitchID -> getIndividualSongRating(twitchID, newSongName) == 0)
                        .limit(20)
                        .map(ratingReminderMap::get)
                        .collect(Collectors.joining(", "));
                if (!remindString.isEmpty()) TwitchChat.sendMessage("Rate The Song! -> " + remindString);

                //Send out random songquote
                try { Thread.sleep(10000); } catch (InterruptedException e) { e.printStackTrace(); }
                TwitchChat.sendMessage(SongDatabase.getSongQuote(newSongName, false));
            } else {
                System.out.println("ignoring " + newSongName + "Another song is already playing");
            }
        }).start();
    }

    private static int getIndividualSongRating(String twitchUserID, String songName) {
        return BobsDatabase.getIntFromSQL("SELECT songRating FROM SongRatings WHERE twitchUserID = ? AND songName = ?", twitchUserID, songName);
    }

    private static void watchSongFile(Path songFileLocation) {
        new Thread(() -> {
            try {
                WatchService fileWatcher = FileSystems.getDefault().newWatchService();
                songFileLocation.getParent().register(fileWatcher, StandardWatchEventKinds.ENTRY_MODIFY);
                String lastSongNameInFile = "none";
                while (true) {
                    WatchKey key = fileWatcher.take();
                    for (WatchEvent event : key.pollEvents()) {
                        Path eventFilePath = (Path) event.context();
                        if (event.kind() == StandardWatchEventKinds.ENTRY_MODIFY && eventFilePath.endsWith(songFileLocation.getFileName())) {
                            List<String> songFileLineArray = Files.readAllLines(songFileLocation, Charset.forName("windows-1252"));
                            //if for some reason the file is empty just ignore it.
                            if (songFileLineArray.size() == 0) break;
                            String newSongNameInFile = songFileLineArray.get(0);
                            if (!lastSongNameInFile.equalsIgnoreCase(newSongNameInFile)) {
                                songFileChange(newSongNameInFile);
                                lastSongNameInFile = newSongNameInFile;
                            }
                        }
                    }
                    if (key.reset() == false) {
                        System.out.println("Song file Watching went horrible wrong!");
                        break;
                    }
                }
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }
}

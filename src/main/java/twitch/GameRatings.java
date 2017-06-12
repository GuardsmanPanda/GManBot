package twitch;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import core.StreamWebOverlay;
import database.BobsDatabase;
import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.events.MessageEvent;
import twitch.dataobjects.TwitchChatMessage;
import webapi.Twitchv5;

import java.time.Instant;
import java.time.LocalDate;

public class GameRatings extends ListenerAdapter {
    private static final JsonNodeFactory nodeFactory = JsonNodeFactory.instance;
    private static Instant nextOverlayUpdate = Instant.now();
    //TODO cache the current game playing in case twitch api derps


    @Override
    public void onMessage(MessageEvent event) {
        if (event.getMessage().startsWith("!rategame ")) {
            TwitchChatMessage tcm = new TwitchChatMessage(event);
            String gameQuote = "none";
            String gameTitle = Twitchv5.getGameName();
            try {
                int rating = Integer.parseInt(tcm.getMessageContent().split(" ")[0]);
                if (rating < 1 ) rating = 1;
                if (rating > 11) rating = 11;
                if (tcm.getMessageContent().contains(" ")) gameQuote = tcm.getMessageContent().substring(tcm.getMessageContent().indexOf(" ")).trim();
                addGameRatingToDatabase(tcm.userID, gameTitle, rating, gameQuote);
                updateOverlay(gameTitle);
            } catch (NumberFormatException nfe) {
                // Silently kill number format exceptions
            }
        }
    }

    public static void updateOverlay(String gameTitle) {
        if (Instant.now().isBefore(nextOverlayUpdate)) return;

        double rating = BobsDatabase.getDoubleFromSQL("SELECT AVG(CAST(gameRating AS DOUBLE)) AS avgRating FROM GameRatings WHERE gameName = ?", gameTitle);
        int votes = BobsDatabase.getIntFromSQL("SELECT COUNT(gameName) AS gameCount FROM GameRatings WHERE gameName = ?", gameTitle);

        int x = 1220; int y = 75;
        switch (gameTitle) {
            case "Creative": y = 10;
        }

        ObjectNode root = nodeFactory.objectNode();
        root.set("type", nodeFactory.textNode("gameRatingUpdate"));
        root.set("gameName", nodeFactory.textNode(gameTitle));
        root.set("gameRating", nodeFactory.textNode(String.format("%.2f", rating)));
        root.set("gameVotes", nodeFactory.numberNode(votes));
        root.set("x", nodeFactory.numberNode(x));
        root.set("y", nodeFactory.numberNode(y));

        StreamWebOverlay.sendJsonToOverlay(root);
        nextOverlayUpdate = Instant.now().plusSeconds(25);
    }

    private static void addGameRatingToDatabase(String twitchID, String gameName, int gameRating, String gameQuote) {
        System.out.println("game rating from " + twitchID + " " + gameName + " " + gameRating + " " + gameQuote);
        String oldQuote = BobsDatabase.getStringFromSQL("SELECT gameQuote FROM GameRatings WHERE twitchUserID = ? AND gameName = ?", twitchID, gameName);
        if (oldQuote.isEmpty()) {
            BobsDatabase.executePreparedSQL("INSERT INTO GameRatings(twitchUserID, gameName, gameRating, gameQuote) VALUES (?, ?, "+gameRating+", ?)", twitchID, gameName, gameQuote);
        } else {
            if (gameQuote.equalsIgnoreCase("none")) gameQuote = oldQuote;
            BobsDatabase.executePreparedSQL("UPDATE GameRatings SET gameRating = "+gameRating+", gameQuote = ?, ratingDateTime = '"+ LocalDate.now().toString() + "' WHERE twitchUserID = ? AND gameName = ?", gameQuote, twitchID, gameName);
        }
    }
}

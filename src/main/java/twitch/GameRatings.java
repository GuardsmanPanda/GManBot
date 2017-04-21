package twitch;


import core.BobsDatabase;
import core.GBUtility;
import javafx.util.Pair;
import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.events.MessageEvent;

import javax.sql.rowset.CachedRowSet;
import java.awt.*;
import java.sql.SQLException;
import java.time.LocalDate;

public class GameRatings extends ListenerAdapter{


    public static void main(String[] args) throws InterruptedException {
         Thread.sleep(4000);
        System.out.println(MouseInfo.getPointerInfo().getLocation().toString());
    }


    @Override
    public void onMessage(MessageEvent event) {
        if (event.getMessage().startsWith("!rategame")) {
            TwitchChatMessage tcm = new TwitchChatMessage(event);
            String gameQuote = "none";
            try {
                int rating = Integer.parseInt(tcm.getMessageContent().split(" ")[0]);
                if (rating < 1 ) rating = 1;
                if (rating > 11) rating = 11;
                if (tcm.getMessageContent().contains(" ")) gameQuote = tcm.getMessageContent().substring(tcm.getMessageContent().indexOf(" ")).trim();
                addGameRatingToDatabase(tcm.userID, Twitchv5.getGameTitle(), rating, gameQuote);
                //TODO: display game rating on stream
            } catch (NumberFormatException nfe) {
                // Silently kill number format exceptions
            }
        }
    }

    private static void addGameRatingToDatabase(String twitchID, String gameName, int gameRating, String gameQuote) {
        System.out.println("game rating from " + twitchID + " " + gameName + " " + gameRating + " " + gameQuote);
        try (CachedRowSet cachedRowSet = BobsDatabase.getCachedRowSetFromSQL("SELECT * FROM GameRatings WHERE twitchUserID = ? AND gameName = ?", twitchID, gameName)) {
            if (cachedRowSet.next()) {
                if (gameQuote.equalsIgnoreCase("none")) gameQuote = cachedRowSet.getString("gameQuote");
                //TODO Also update game date.
                BobsDatabase.executePreparedSQL("UPDATE GameRatings SET gameRating = "+gameRating+", gameQuote = ?, ratingDateTime = '"+ LocalDate.now().toString() + "' WHERE twitchUserID = ? AND gameName = ?", gameQuote, twitchID, gameName);
            } else {
                // found no game rating for the given userID
                BobsDatabase.executePreparedSQL("INSERT INTO GameRatings(twitchUserID, gameName, gameRating, gameQuote) VALUES (?, ?, "+gameRating+", ?)", twitchID, gameName, gameQuote);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}

package utility;

import core.BobsDatabase;
import core.BobsDatabaseHelper;
import twitch.SongDatabase;
import twitch.Twitch;

import javax.sql.rowset.CachedRowSet;
import javax.sql.rowset.RowSetProvider;
import java.sql.*;
import java.util.HashMap;

public class DataMigration {
    private static HashMap<String, String> twitchNameToIDMap = new HashMap<>();

    public static void main(String[] args) throws Exception {

    }

    public static void importSongRatings() throws Exception {
        HashMap<String, String> twitchNameChangeMap = new HashMap<>();
        twitchNameChangeMap.put("quadias", "Insidious_void_");
        twitchNameChangeMap.put("shadowbourne2929", "scourgiman2381");

        Class.forName("org.apache.derby.jdbc.EmbeddedDriver");
        Connection databaseConnection = DriverManager.getConnection("jdbc:derby:gmanbotdb");

        ResultSet songRatingSet = databaseConnection.createStatement().executeQuery("SELECT * FROM SongRatings");
        CachedRowSet cachedSongRatingSet = RowSetProvider.newFactory().createCachedRowSet();
        cachedSongRatingSet.populate(songRatingSet);

        System.out.println("Found " + cachedSongRatingSet.size() + " song ratings!");
        int entryNumber = 1;
        while (cachedSongRatingSet.next()) {
            String twitchName = cachedSongRatingSet.getString("ratedBy");
            if (twitchNameChangeMap.containsKey(twitchName)) twitchName = twitchNameChangeMap.get(twitchName);
            String songName = cachedSongRatingSet.getString("songName");
            String songQuote = cachedSongRatingSet.getString("ratingQuote");
            int songRating = cachedSongRatingSet.getInt("rating");
            final int currentEntryNumber = entryNumber;

            String twitchID = twitchNameToIDMap.computeIfAbsent(twitchName, sameTwitchName -> {
                System.out.println("Looking up ID for " + sameTwitchName + ", " + (cachedSongRatingSet.size() - currentEntryNumber) + " song entries left.");
                try { Thread.sleep(350); } catch (InterruptedException e) { e.printStackTrace(); }
                String twitchIDForName = Twitch.getTwitchUserID(sameTwitchName);
                if (twitchIDForName.isEmpty()) System.out.println("Could not find ID for " + sameTwitchName);
                return twitchIDForName;
            });

            if (!twitchID.isEmpty()) {
                SongDatabase.addSongRating(twitchID, twitchName, songName, songRating, songQuote);
            }
            System.out.println("Migrated " + entryNumber + "entries to the new song rating database!");
            entryNumber++;
        }
    }
}

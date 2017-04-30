package utility;


import com.fasterxml.jackson.databind.JsonNode;
import database.BobsDatabase;
import database.BobsDatabaseHelper;
import twitch.Twitchv5;

import javax.sql.rowset.CachedRowSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.stream.StreamSupport;

public class DataMaintenance {

    public static void main(String[] args) throws SQLException {

        //addAllCurrentSubsAndPrimeSubstoDB();
    }

    //TODO, version 1 removes songs not rated by gmanbot, version 2 should remove songs not rated by gmanbot since date x
    public static void cleanSongRatingDB() throws SQLException {
        HashSet<String> songsRatedByGmanbot = new HashSet<>();
        HashSet<String> songsToRemove = new HashSet<>();
        CachedRowSet cachedRowSet = BobsDatabase.getCachedRowSetFromSQL("SElECT DISTINCT songName FROM SongRatings WHERE twitchUserID = ?", "39837384");
        while (cachedRowSet.next()) songsRatedByGmanbot.add(cachedRowSet.getString("songName"));

        System.out.println("GManBot rated " + cachedRowSet.size() + " songs");

        CachedRowSet allSongNamesRowSet = BobsDatabase.getCachedRowSetFromSQL("SELECT DISTINCT songName FROM songRatings");
        System.out.println("Total songs in the database: " +allSongNamesRowSet.size());
            while (allSongNamesRowSet.next()) {
            String songName = allSongNamesRowSet.getString("songName");
            if (!songsRatedByGmanbot.contains(songName)) songsToRemove.add(songName);
        }
        System.out.println("Found " + songsToRemove.size() + " song names to remove from the database");
        songsToRemove.forEach(songName -> BobsDatabase.executePreparedSQL("DELETE FROM songRatings WHERE songName = ?", songName));
    }


    public static void addAllCurrentSubsAndPrimeSubstoDB() {
        int total = 1000;
        for (int offset = 0; offset < total; offset +=99) {
            JsonNode node = Twitchv5.executeHttpGet("https://api.twitch.tv/kraken/channels/30084132/subscriptions?offset=" + offset);
            total = node.get("_total").asInt();
            StreamSupport.stream(node.get("subscriptions").spliterator(),false)
                    .forEach(json -> BobsDatabaseHelper.setHasSubscribed(json.get("user").get("_id").asText(), json.get("user").get("display_name").asText()));
        }
    }
}

package utility;


import com.fasterxml.jackson.databind.JsonNode;
import database.BobsDatabaseHelper;
import webapi.Twitchv5;

import java.sql.SQLException;
import java.util.stream.StreamSupport;

public class DataMaintenance {

    public static void main(String[] args) throws SQLException {
        //addAllCurrentSubsAndPrimeSubstoDB();

    }

    public static void printSongRatingsToFile() {

    }

    //replace with list<id> of subs from twitchv5
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

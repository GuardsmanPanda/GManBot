package utility;

import core.BobsDatabase;
import core.BobsDatabaseHelper;
import core.GBUtility;
import twitch.Twitch;

import javax.sql.rowset.CachedRowSet;
import java.sql.*;
import java.util.ArrayDeque;
import java.util.HashMap;

public class DataMigration {
    private static HashMap<String, FinalPair<String, String>> lowerCaseNameToIDAndDisplayName = new HashMap<>();

    public static void main(String[] args) throws Exception {
        GBUtility.prettyPrintCachedRowSet(BobsDatabase.getCachedRowSetFromSQL("SELECT * FROM TwitchChatUsers ORDER BY idleHours DESC FETCH FIRST 200 ROWS ONLY"), 200, 20);
    }

    public static void importtwitchUserData() throws Exception {
        Class.forName("org.apache.derby.jdbc.EmbeddedDriver");
        Connection databaseConnection = DriverManager.getConnection("jdbc:derby:gmanbotdb");

        //load old names
        CachedRowSet cachedRowSet = BobsDatabase.getCachedRowSetFromSQL("SELECT twitchUserID, twitchDisplayName FROM twitchChatUsers");
        System.out.println("Loaded: " + cachedRowSet.size() + " Entries");
        while (cachedRowSet.next()) lowerCaseNameToIDAndDisplayName.put(cachedRowSet.getString("twitchDisplayName").toLowerCase(), new FinalPair<>(cachedRowSet.getString("twitchUserID"), cachedRowSet.getString("twitchDisplayName")));
        System.out.println("Loaded Known Twitch Names, getting twitch names from old DB");
        cachedRowSet.close();


        ResultSet resultSet = databaseConnection.createStatement().executeQuery("SELECT * FROM Chat");
        while(resultSet.next()) {
            String twitchName = resultSet.getString("twitchname").toLowerCase();
            FinalPair<String, String> idNamePair;

            if (lowerCaseNameToIDAndDisplayName.containsKey(twitchName)) {
                idNamePair = lowerCaseNameToIDAndDisplayName.get(twitchName);
                System.out.println("Found twitch name in cache: " + twitchName);
            } else {
                idNamePair = Twitch.getTwitchUserIDAndDisplayName(twitchName);
            }

            if (!idNamePair.first.isEmpty())  {
                BobsDatabaseHelper.migrateData(idNamePair.first, idNamePair.second, resultSet.getInt("idlehoursinchat"), resultSet.getInt("activehoursinchat"), resultSet.getInt("linesinchat"), resultSet.getString("flagname"), resultSet.getInt("currentbobcoins"), resultSet.getBoolean("rawrsbob"));
            } else {
                System.out.println("Could not find user ID for >> " + resultSet.getString("twitchname"));
            }
        }
    }

}

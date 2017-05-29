package utility;

import database.BobsDatabaseHelper;

import javax.sql.rowset.CachedRowSet;
import javax.sql.rowset.RowSetProvider;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;

public class DataMigration {
    private static HashMap<String, FinalPair<String, String>> lowerCaseNameToIDAndDisplayName = new HashMap<>();


    //TODO: merge stats from people who changed name .. mkrh88 -> Eremiter .. (insidious void) ... immaanime -> im2be .. a
    public static void main(String[] args) throws Exception {
        Class.forName("org.apache.derby.jdbc.EmbeddedDriver");
        Connection databaseConnection = DriverManager.getConnection("jdbc:derby:gmanbotdb");
        CachedRowSet cachedRowSet = RowSetProvider.newFactory().createCachedRowSet();

        cachedRowSet.populate(databaseConnection.createStatement().executeQuery("SELECT * FROM Chat WHERE twitchName = '" + "chooseneye" + "'"));
        PrettyPrinter.prettyPrintCachedRowSet(cachedRowSet, 100);
    }

    public static void mergeOldChatName(String oldName, String newName) throws SQLException, ClassNotFoundException {
        Class.forName("org.apache.derby.jdbc.EmbeddedDriver");
        Connection databaseConnection = DriverManager.getConnection("jdbc:derby:gmanbotdb");

        ResultSet resultSet = databaseConnection.createStatement().executeQuery("SELECT * FROM Chat WHERE twitchName = '"+oldName+"'");

        if (resultSet.next()) {
            System.out.println("found old user: " + oldName);
            String newID = BobsDatabaseHelper.getTwitchUserID(newName);
            System.out.println("merging into " + newID);
            BobsDatabaseHelper.mergeOldData(newID, resultSet.getInt("idlehoursinchat"), resultSet.getInt("activehoursinchat"), resultSet.getInt("linesinchat"), resultSet.getInt("currentbobcoins"), resultSet.getBoolean("rawrsbob"));
        }

    }


    /*
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
    }*/

}

package utility;

import database.BobsDatabaseHelper;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;

public class DataMigration {
    private static HashMap<String, FinalPair<String, String>> lowerCaseNameToIDAndDisplayName = new HashMap<>();



    public static void main(String[] args) throws Exception {
        /*
        Class.forName("org.apache.derby.jdbc.EmbeddedDriver");
        Connection databaseConnection = DriverManager.getConnection("jdbc:derby:gmanbotdb");
        ResultSet resultSet = databaseConnection.createStatement().executeQuery("SELECT name, amountInCents FROM Donations");
        while (resultSet.next()) {
            String name = resultSet.getString("name");
            if (name.equalsIgnoreCase("shadowbourne2929")) {
                name = "Scourgiman2381";
                String twitchID = BobsDatabaseHelper.getTwitchUserID(name);
                if (twitchID.isEmpty()) {
                    System.out.println("Could still not find name for " + name);
                } else {
                    int amount = resultSet.getInt("amountInCents");
                    BobsDatabaseHelper.addCentsDonated(twitchID, amount);
                }
            }
        }
        */
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
}

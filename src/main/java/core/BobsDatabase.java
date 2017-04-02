package core;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import javax.sql.rowset.CachedRowSet;
import javax.sql.rowset.RowSetProvider;
import java.sql.*;
import java.time.Instant;

/**
 * In this class I think hard about life's problems and then database code appears.
 */
public class BobsDatabase {
    private static Connection connection;

    static {
        try {
            Class.forName("org.apache.derby.jdbc.EmbeddedDriver");
            connection = DriverManager.getConnection("jdbc:derby:BobsDB;create=true");
            System.out.println("Connected To DataBase");
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }

        try {
            connection.createStatement().execute("CREATE TABLE SongRatings (twitchUserID VARCHAR(255) NOT NULL, twitchDisplayName VARCHAR(255) NOT NULL, songName VARCHAR(255) NOT NULL, songRating INTEGER NOT NULL, songQuote VARCHAR(255) NOT NULL DEFAULT 'none', ratingTimestamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, PRIMARY KEY (twitchUserID, songName))");
            System.out.println("Crated SongRating Table");
        } catch (SQLException e) {
            // Silently ignore if the table already exists, TODO: Probably shouldn't ... Whenever derby supports CREATE TABLE IF NOT EXISTS
        }

        try {
            connection.createStatement().execute("CREATE TABLE TwitchChatUsers (twitchUserID VARCHAR(255) NOT NULL PRIMARY KEY, twitchDisplayName VARCHAR(255) UNIQUE NOT NULL, welcomeMessage VARCHAR(255) NOT NULL DEFAULT 'none')");
            System.out.println("Created TwitchChatUsers Table");
        } catch (SQLException e) {
            // Silently ignore if the table already exists, TODO: Probably shouldn't ... Whenever derby supports CREATE TABLE IF NOT EXISTS
        }
    }

    public static void main(String[] args) {
        printTable("SongRatings");
    }

    /**
     * Execute SQL against BobsDB
     * @param sql The prepared statements to execute.
     * @param arguments arguments for the statement.
     * @return the number of rows which were updated.
     */
    public static int executePreparedSQL(String sql, String... arguments) {
        if (StringUtils.countMatches(sql, "?") != arguments.length) throw new RuntimeException("Your SQL sucks! " + sql);

        int rowsUpdated = 0;

        try (PreparedStatement statement = connection.prepareStatement(sql);) {
            for (int i = 0; i < arguments.length; i++) statement.setString(i + 1, arguments[i]);
            rowsUpdated = statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return rowsUpdated;
    }

    public static CachedRowSet getCachedRowSetFromSQL(String sql, String... arguments) {
        if (StringUtils.countMatches(sql, "?") != arguments.length) throw new RuntimeException("Your SQL sucks! " + sql);

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            for (int i = 0; i < arguments.length; i++) statement.setString(i + 1, arguments[i]);
            try (ResultSet resultSet = statement.executeQuery()) {
                CachedRowSet returnSet = RowSetProvider.newFactory().createCachedRowSet();
                returnSet.populate(resultSet);
                return returnSet;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Something went horribly wrong trying to execute SQL: " + sql + " <> arguments: " + ArrayUtils.toString(arguments));
        }
    }

    /**
     * Adds songRating to the songRating table
     * @return returns true of exactly one entry was added or updated.
     */
    public static boolean addSongRating(String twitchUserID, String twitchDisplayName, String songName, int songRating, String songQuote) {
        boolean songRatingExists = false;
        String oldQuote = "none";

        CachedRowSet songLookupSet = getCachedRowSetFromSQL("SELECT * FROM SongRatings WHERE twitchUserID = ? AND songName = ? ", twitchUserID, songName);

        try {
            if (songLookupSet.next()) {
                songRatingExists = true;
                oldQuote = songLookupSet.getString("songQuote");
                if (songLookupSet.next()) throw new RuntimeException("More than one song rating returned for ID: " + twitchUserID + " name: " + twitchDisplayName + " songname: " + songName);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        if (songQuote.equalsIgnoreCase("none")) songQuote = oldQuote;

        if (songRatingExists) {
            try (PreparedStatement statement = connection.prepareStatement("UPDATE SongRatings SET twitchDisplayName = ?, songRating = ?, songQuote = ?, ratingTimestamp = ? WHERE twitchUserID = ? AND songName = ?")) {
                statement.setString(1, twitchDisplayName);
                statement.setInt(2, songRating);
                statement.setString(3, songQuote);
                statement.setTimestamp(4, Timestamp.from(Instant.now()));
                statement.setString(5, twitchUserID);
                statement.setString(6, songName);

                int result = statement.executeUpdate();
                if (result == 1) return true;
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } else {
            try (PreparedStatement statement = connection.prepareStatement("INSERT INTO SongRatings VALUES (?, ?, ?, ?, ?, ?)")) {
                statement.setString(1, twitchUserID);
                statement.setString(2, twitchDisplayName);
                statement.setString(3, songName);
                statement.setInt(4, songRating);
                statement.setString(5, songQuote);
                statement.setTimestamp(6, Timestamp.from(Instant.now()));
                int result = statement.executeUpdate();
                if (result == 1) return true;
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        throw new RuntimeException("something went wrong when trying to add song rating: " + twitchUserID + " name: " + twitchDisplayName + " songname: " + songName);
    }

    private static void printTable(String tableName) {
        CachedRowSet tableSET = getCachedRowSetFromSQL("SELECT * FROM " + tableName);
        try {
            while (tableSET.next()) {
                String toPrint = "";
                for (int i = 1; i <= tableSET.getMetaData().getColumnCount(); i++) toPrint += tableSET.getString(i) + " ";
                System.out.println(toPrint);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}

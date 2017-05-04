package database;

import com.google.common.base.CharMatcher;

import javax.sql.rowset.CachedRowSet;
import javax.sql.rowset.RowSetProvider;
import java.sql.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;


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
            connection.createStatement().execute("CREATE TABLE EmoteUsage (twitchUserID VARCHAR(50) NOT NULL, emoteName VARCHAR(30) NOT NULL, timeStamp TIMESTAMP NOT NULL PRIMARY KEY)");
            connection.createStatement().execute("CREATE TABLE GameRatings (twitchUserID VARCHAR(50) NOT NULL, gameName VARCHAR(255) NOT NULL, gameRating INTEGER NOT NULL, gameQuote VARCHAR (255) NOT NULL DEFAULT 'none', ratingDateTime DATE NOT NULL DEFAULT CURRENT DATE, PRIMARY KEY (twitchUserID, gameName))");
            connection.createStatement().execute("CREATE TABLE SongRatings (twitchUserID VARCHAR(255) NOT NULL, twitchDisplayName VARCHAR(255) NOT NULL, songName VARCHAR(255) NOT NULL, songRating INTEGER NOT NULL, songQuote VARCHAR(255) NOT NULL DEFAULT 'none', ratingTimestamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, PRIMARY KEY (twitchUserID, songName))");
            connection.createStatement().execute("CREATE TABLE TwitchChatUsers (twitchUserID VARCHAR(25) NOT NULL PRIMARY KEY, twitchDisplayName VARCHAR(30) UNIQUE NOT NULL, twitchLowerCaseName GENERATED ALWAYS AS (LOWER(twitchDisplayName)), hasSubscribed BOOLEAN NOT NULL DEFAULT false, welcomeMessage VARCHAR(255) NOT NULL DEFAULT 'none')");
            connection.createStatement().execute("CREATE INDEX twitchLowerIndex ON TwitchChatUsers(twitchLowerCaseName)");
            System.out.println("Created Tables");
        } catch (SQLException e) {
            // Silently ignore if the table already exists, TODO: Probably shouldn't ... Whenever derby supports CREATE TABLE IF NOT EXISTS
        }

        try {
            connection.createStatement().execute("ALTER TABLE TwitchChatUsers ADD flag VARCHAR(40) NOT NULL DEFAULT 'none'");
            connection.createStatement().execute("ALTER TABLE TwitchChatUsers ADD chatLines INTEGER NOT NULL DEFAULT 0");
            connection.createStatement().execute("ALTER TABLE TwitchChatUsers ADD activeHours  INTEGER NOT NULL DEFAULT 0");
            connection.createStatement().execute("ALTER TABLE TwitchChatUsers ADD idleHours INTEGER NOT NULL DEFAULT 0");
            connection.createStatement().execute("ALTER TABLE TwitchChatUsers ADD bobCoins INTEGER NOT NULL DEFAULT 0");
            connection.createStatement().execute("ALTER TABLE TwitchChatUsers ADD heartsBob BOOLEAN NOT NULL DEFAULT false");
            connection.createStatement().execute("ALTER TABLE TwitchChatUsers ADD songRatingReminder BOOLEAN NOT NULL DEFAULT false");
            connection.createStatement().execute("ALTER TABLE TwitchChatUsers ADD subscriberMonths INTEGER NOT NULL DEFAULT 0");
        } catch (SQLException e) {
            // probably should check for column and only add if not exists, but im lazy
        }
    }

    /**
     * Execute SQL against BobsDB
     *
     * @param sql       The prepared statements to execute.
     * @param arguments arguments for the statement.
     * @return the number of rows which were updated.
     */
    public static int executePreparedSQL(String sql, String... arguments) {
        if (CharMatcher.is('?').countIn(sql) != arguments.length)
            throw new RuntimeException("Your SQL sucks! " + sql + " <> arguments: " + Arrays.toString(arguments));

        int rowsUpdated = 0;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            for (int i = 0; i < arguments.length; i++) statement.setString(i + 1, arguments[i]);
            rowsUpdated = statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return rowsUpdated;
    }

    public static CachedRowSet getCachedRowSetFromSQL(String sql, String... arguments) {
        if (CharMatcher.is('?').countIn(sql) != arguments.length)
            throw new RuntimeException("Your SQL sucks! " + sql + " <> arguments: " + Arrays.toString(arguments));

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            for (int i = 0; i < arguments.length; i++) statement.setString(i + 1, arguments[i]);
            try (ResultSet resultSet = statement.executeQuery()) {
                CachedRowSet returnSet = RowSetProvider.newFactory().createCachedRowSet();
                returnSet.populate(resultSet);
                return returnSet;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Something went horribly wrong trying to execute SQL: " + sql + " <> arguments: " + Arrays.toString(arguments));
        }
    }


    public static String getStringFromSQL(String sql, String... arguments) {
        try {
            return getValueFromSQL(sql, String.class, arguments);
        } catch (IllegalArgumentException e) {
            return "";
        }
    }
    public static int getIntFromSQL(String sql, String... arguments) {
        try {
            return getValueFromSQL(sql, Integer.class, arguments);
        } catch (IllegalArgumentException e) {
            return 0;
        }
    }
    public static double getDoubleFromSQL(String sql, String... arguments) {
        try {
            return getValueFromSQL(sql, Double.class, arguments);
        } catch (IllegalArgumentException e) {
            return 0.0;
        }
    }
    public static boolean getBooleanFromSQL(String sql, String... arguments) {
        try {
            return getValueFromSQL(sql, Boolean.class, arguments);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    public static <E> E getValueFromSQL(String sql, Class<E> returnType, String... arguments) {
        try (CachedRowSet cachedRowSet = getCachedRowSetFromSQL(sql, arguments)) {
            assert (cachedRowSet.getMetaData().getColumnCount() == 1);

            switch (cachedRowSet.size()) {
                case 0:
                    throw new IllegalArgumentException("0 results");
                case 1:
                    cachedRowSet.next();
                    return returnType.cast(cachedRowSet.getObject(1));
                default:
                    throw new RuntimeException("Only 1 result should be returned when asking for a single value");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("The SQL Didn't work! " + sql + " args: " + Arrays.toString(arguments));
        }
    }

    //TODO Consider using a guava typetoken to cause class cast exception fail before returning an invalid map.
    public static <K, V> Map<K, V> getMapFromSQL(String sql, Map<K, V> mapType, String... arguments) {
        try (CachedRowSet cachedRowSet = getCachedRowSetFromSQL(sql, arguments)) {
            assert (cachedRowSet.getMetaData().getColumnCount() == 2);

            Map<Object, Object> returnMap = new HashMap<>();
            while (cachedRowSet.next()) {
                returnMap.put(cachedRowSet.getObject(1), cachedRowSet.getObject(2));
            }
            return mapType.getClass().cast(returnMap);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Map.of();
    }
}

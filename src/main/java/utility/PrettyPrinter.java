package utility;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.sql.rowset.CachedRowSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.time.Duration;
import java.time.Period;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Dons on 02-05-2017.
 */
public class PrettyPrinter {


    public static String timeStringFromPeriod(Period period) {
        List<String> timeStrings = new ArrayList<>();
        int years = period.getYears();
        int months = period.getMonths();
        int days = period.getDays();

        if (years > 0) timeStrings.add(years + ((years > 1) ? " Years": " Year"));
        if (months > 0) timeStrings.add(months + ((months > 1) ? " Months": " Month"));
        if (days > 0) timeStrings.add(days + ((days > 1) ? " Days": " Day"));

        return String.join(", ", timeStrings);
    }
    public static String timeStringFromDuration(Duration duration) {
        List<String> timeStrings = new ArrayList<>();
        long days = duration.toDays();
        int hours = duration.toHoursPart();
        int minutes = duration.toMinutesPart();

        if (days > 0) timeStrings.add(days + ((days > 1) ? " Days": " Day"));
        if (hours > 0) timeStrings.add(hours + ((hours > 1) ? " Hours": " Hour"));
        if (minutes > 0) timeStrings.add(minutes + ((minutes > 1) ? " Minutes": " Minute"));

        return String.join(", ", timeStrings);
    }

     public static void prettyPrintJSonNode(JsonNode node) {
        try {
            System.out.println(new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(node));
        } catch (JsonProcessingException e) {
            System.out.println("Error printing JsonNode");
            e.printStackTrace();
        }
    }

    public static void prettyPrintCachedRowSet(CachedRowSet cachedRowSet, int rowsToPrint) {
        prettyPrintCachedRowSet(cachedRowSet, rowsToPrint, 20);
    }
    public static void prettyPrintCachedRowSet(CachedRowSet cachedRowSet, int rowsToPrint, int rowLength) {
        try {
            ResultSetMetaData metaData = cachedRowSet.getMetaData();
            String columnNames = "";
            for (int i = 1; i <= metaData.getColumnCount(); i++) columnNames += GBUtility.strictFill(metaData.getColumnLabel(i), rowLength) + " ";
            System.out.println(columnNames.trim());

            while (cachedRowSet.next()) {
                String rowString = "";
                for (int i = 1; i <= metaData.getColumnCount(); i++) rowString += GBUtility.strictFill(cachedRowSet.getString(i), rowLength) + " ";
                System.out.println(rowString.trim());
                if (cachedRowSet.getRow() >= rowsToPrint) break;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}

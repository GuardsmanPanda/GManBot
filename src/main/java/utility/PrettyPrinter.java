package utility;

import java.lang.reflect.Array;
import java.time.Duration;
import java.time.Period;
import java.util.*;

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


}

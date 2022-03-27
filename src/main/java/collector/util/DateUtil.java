package collector.util;

import org.javatuples.Pair;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

public class DateUtil {

    public static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter
            .ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");


    public static Instant getDateTime(String utcString){
        return Instant.parse(utcString);
    }

//    public static long getTimeMillis(String utcString){
//        return Instant.parse(utcString).toEpochMilli();
//    }
//
//    public static Instant getDateTime(long millis){
//        return Instant.ofEpochMilli(millis);
//    }
//
//    public static String getFormattedUtcString(long millis){
//        return getFormattedUtcString(Instant.ofEpochMilli(millis));
//    }

    public static String getFormattedUtcString(Instant date){
        ZonedDateTime utc = date.atZone(ZoneOffset.UTC);
        String res = utc.format(DATE_TIME_FORMATTER);
        return res;
    }

    public static Pair<TimeSpan, TimeSpan> divideTimesByTwo(String utcDateFrom, String utcDateTo){
        Instant instDateFrom = DateUtil.getDateTime(utcDateFrom);
        Instant instDateTo = DateUtil.getDateTime(utcDateTo);

        return divideTimesByTwo(instDateFrom, instDateTo);
    }

    public static Pair<TimeSpan, TimeSpan> divideTimesByTwo(Instant dateFrom, Instant dateTo){
        Instant dateALower = dateFrom;
        Instant dateAUpper = dateFrom.plusMillis(ChronoUnit.MILLIS.between(dateFrom, dateTo) / 2);
        Instant dateBLower = dateAUpper.plusMillis(1);
        Instant dateBUpper = dateTo;
        Pair<TimeSpan, TimeSpan> res = Pair.with(new TimeSpan(dateALower, dateAUpper), new TimeSpan(dateBLower, dateBUpper));
        return res;
    }
}

package com.digitaldetective.util;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Date/time formatting and range helpers used by the timeline view and
 * report generators.
 */
public final class DateUtils {

    private DateUtils() {
    }

    public static final DateTimeFormatter DISPLAY_DATE_TIME =
            DateTimeFormatter.ofPattern("dd MMM yyyy — HH:mm").withZone(ZoneId.systemDefault());

    public static final DateTimeFormatter DISPLAY_DATE =
            DateTimeFormatter.ofPattern("dd MMM yyyy").withZone(ZoneId.systemDefault());

    public static String formatDateTime(Instant instant) {
        return instant == null ? "N/A" : DISPLAY_DATE_TIME.format(instant);
    }

    public static String formatDate(Instant instant) {
        return instant == null ? "N/A" : DISPLAY_DATE.format(instant);
    }

    public static boolean isWithinLastHours(Instant instant, int hours) {
        if (instant == null) return false;
        return instant.isAfter(Instant.now().minusSeconds(hours * 3600L));
    }

    public static boolean isWithinLastDays(Instant instant, int days) {
        if (instant == null) return false;
        return instant.isAfter(Instant.now().minusSeconds(days * 86400L));
    }

    public static boolean isToday(Instant instant) {
        if (instant == null) return false;
        ZonedDateTime now = ZonedDateTime.now();
        ZonedDateTime then = instant.atZone(ZoneId.systemDefault());
        return now.toLocalDate().equals(then.toLocalDate());
    }

    public static boolean isBetween(Instant instant, Instant start, Instant end) {
        if (instant == null) return false;
        return !instant.isBefore(start) && !instant.isAfter(end);
    }
}

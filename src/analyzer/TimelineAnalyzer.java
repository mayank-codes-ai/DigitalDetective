package com.digitaldetective.analyzer;

import com.digitaldetective.model.FileRecord;
import com.digitaldetective.util.Constants;
import com.digitaldetective.util.DateUtils;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Builds a chronological "who touched what, when" style timeline from
 * created/modified/accessed timestamps, and provides the filtering logic
 * used by the Timeline view's quick-range buttons.
 */
public final class TimelineAnalyzer {

    /** A single row in the timeline: one file, one type of timestamp event. */
    public static class TimelineEvent {
        public final FileRecord file;
        public final String eventType; // "created", "modified", "accessed"
        public final Instant timestamp;

        public TimelineEvent(FileRecord file, String eventType, Instant timestamp) {
            this.file = file;
            this.eventType = eventType;
            this.timestamp = timestamp;
        }
    }

    public enum RangeFilter {
        ALL, TODAY, LAST_24_HOURS, LAST_7_DAYS, LAST_30_DAYS, CUSTOM
    }

    private TimelineAnalyzer() {
    }

    public static List<TimelineEvent> buildTimeline(List<FileRecord> files) {
        List<TimelineEvent> events = new ArrayList<>();
        for (FileRecord f : files) {
            if (f.isDirectory()) continue;
            if (f.getCreatedTime() != null) {
                events.add(new TimelineEvent(f, "created", f.getCreatedTime()));
            }
            if (f.getModifiedTime() != null) {
                events.add(new TimelineEvent(f, "modified", f.getModifiedTime()));
            }
            if (f.getAccessedTime() != null) {
                events.add(new TimelineEvent(f, "accessed", f.getAccessedTime()));
            }
        }
        events.sort(Comparator.<TimelineEvent, Instant>comparing(e -> e.timestamp).reversed());
        return events;
    }

    public static List<FileRecord> recentlyModified(List<FileRecord> files, int days) {
        List<FileRecord> result = new ArrayList<>();
        for (FileRecord f : files) {
            if (!f.isDirectory() && DateUtils.isWithinLastDays(f.getModifiedTime(), days)) {
                result.add(f);
            }
        }
        result.sort(Comparator.comparing(FileRecord::getModifiedTime, Comparator.nullsLast(Comparator.reverseOrder())));
        return result;
    }

    public static List<TimelineEvent> filter(List<TimelineEvent> events, RangeFilter range,
                                               Instant customStart, Instant customEnd) {
        List<TimelineEvent> result = new ArrayList<>();
        for (TimelineEvent e : events) {
            boolean include;
            switch (range) {
                case TODAY:
                    include = DateUtils.isToday(e.timestamp);
                    break;
                case LAST_24_HOURS:
                    include = DateUtils.isWithinLastHours(e.timestamp, 24);
                    break;
                case LAST_7_DAYS:
                    include = DateUtils.isWithinLastDays(e.timestamp, 7);
                    break;
                case LAST_30_DAYS:
                    include = DateUtils.isWithinLastDays(e.timestamp, 30);
                    break;
                case CUSTOM:
                    include = customStart != null && customEnd != null
                            && DateUtils.isBetween(e.timestamp, customStart, customEnd);
                    break;
                case ALL:
                default:
                    include = true;
            }
            if (include) {
                result.add(e);
            }
        }
        return result;
    }

    public static int defaultRecentWindowDays() {
        return Constants.RECENT_DAYS_WINDOW;
    }
}

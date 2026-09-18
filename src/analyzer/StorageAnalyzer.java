package com.digitaldetective.analyzer;

import com.digitaldetective.model.FileRecord;
import com.digitaldetective.model.FileType;
import com.digitaldetective.util.Constants;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Produces storage- and type-distribution statistics, plus the "top N
 * largest files", hidden files, and empty files lists used across the
 * dashboard, storage analytics page, and reports.
 */
public final class StorageAnalyzer {

    private StorageAnalyzer() {
    }

    public static Map<FileType, Long> typeCountDistribution(List<FileRecord> files) {
        Map<FileType, Long> map = new EnumMap<>(FileType.class);
        for (FileRecord f : files) {
            if (f.isDirectory()) continue;
            map.merge(f.getFileType(), 1L, Long::sum);
        }
        return map;
    }

    public static Map<FileType, Long> typeSizeDistribution(List<FileRecord> files) {
        Map<FileType, Long> map = new EnumMap<>(FileType.class);
        for (FileRecord f : files) {
            if (f.isDirectory()) continue;
            map.merge(f.getFileType(), f.getSizeBytes(), Long::sum);
        }
        return map;
    }

    public static List<FileRecord> topLargestFiles(List<FileRecord> files, int limit) {
        List<FileRecord> sorted = new ArrayList<>();
        for (FileRecord f : files) {
            if (!f.isDirectory()) sorted.add(f);
        }
        sorted.sort(Comparator.comparingLong(FileRecord::getSizeBytes).reversed());
        return sorted.subList(0, Math.min(limit, sorted.size()));
    }

    public static List<FileRecord> topLargestFiles(List<FileRecord> files) {
        return topLargestFiles(files, Constants.TOP_LARGEST_FILE_COUNT);
    }

    public static List<FileRecord> hiddenFiles(List<FileRecord> files) {
        List<FileRecord> result = new ArrayList<>();
        for (FileRecord f : files) {
            if (f.isHidden() && !f.isDirectory()) result.add(f);
        }
        return result;
    }

    public static List<FileRecord> emptyFiles(List<FileRecord> files) {
        List<FileRecord> result = new ArrayList<>();
        for (FileRecord f : files) {
            if (f.isEmpty()) result.add(f);
        }
        return result;
    }

    public static FileType mostCommonType(Map<FileType, Long> distribution) {
        FileType best = FileType.UNKNOWN;
        long bestCount = -1;
        for (Map.Entry<FileType, Long> e : distribution.entrySet()) {
            if (e.getValue() > bestCount) {
                bestCount = e.getValue();
                best = e.getKey();
            }
        }
        return best;
    }
}

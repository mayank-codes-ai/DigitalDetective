package com.digitaldetective.report;

import com.digitaldetective.model.Investigation;
import com.digitaldetective.util.Constants;
import com.digitaldetective.util.FileUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Manages a lightweight, local investigation history. Since the project
 * deliberately avoids a database, previous investigation summaries are
 * stored as a simple CSV file, and a full HTML report "snapshot" is saved
 * alongside it so the user can re-open any previous investigation's report.
 *
 * <p>No copies of the user's actual scanned files are ever stored - only
 * summary statistics and the generated report.</p>
 */
public final class InvestigationHistoryManager {

    /** One row of investigation history. */
    public static class HistoryEntry {
        public final Instant timestamp;
        public final String path;
        public final boolean demo;
        public final int fileCount;
        public final long totalSizeBytes;
        public final int directoryCount;
        public final int duplicateCount;
        public final int suspiciousCount;
        public final String reportFileName;

        public HistoryEntry(Instant timestamp, String path, boolean demo, int fileCount, long totalSizeBytes,
                             int directoryCount, int duplicateCount, int suspiciousCount, String reportFileName) {
            this.timestamp = timestamp;
            this.path = path;
            this.demo = demo;
            this.fileCount = fileCount;
            this.totalSizeBytes = totalSizeBytes;
            this.directoryCount = directoryCount;
            this.duplicateCount = duplicateCount;
            this.suspiciousCount = suspiciousCount;
            this.reportFileName = reportFileName;
        }

        public String getFormattedSize() {
            return FileUtils.formatSize(totalSizeBytes);
        }
    }

    private static final DateTimeFormatter FILE_STAMP = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    private InvestigationHistoryManager() {
    }

    public static Path getDataFolder() {
        return Paths.get(System.getProperty("user.home"), Constants.DATA_FOLDER_NAME);
    }

    public static Path getReportsFolder() {
        return getDataFolder().resolve("reports");
    }

    private static Path getHistoryFile() {
        return getDataFolder().resolve(Constants.HISTORY_FILE_NAME);
    }

    /**
     * Persists a lightweight summary of the investigation and a full HTML
     * report snapshot, then appends a row to the local history file.
     */
    public static HistoryEntry saveInvestigation(Investigation investigation) throws IOException {
        Files.createDirectories(getReportsFolder());

        Instant now = Instant.now();
        String stamp = FILE_STAMP.format(now.atZone(java.time.ZoneId.systemDefault()));
        String reportFileName = "investigation_" + stamp + ".html";
        Path reportPath = getReportsFolder().resolve(reportFileName);

        HtmlReportGenerator.generate(investigation, reportPath);

        HistoryEntry entry = new HistoryEntry(
                now,
                investigation.getInvestigationPath(),
                investigation.isDemo(),
                investigation.getFileCount(),
                investigation.getTotalSizeBytes(),
                investigation.getDirectoryCount(),
                investigation.getDuplicateFileCount(),
                investigation.getSuspiciousCount(),
                reportFileName
        );

        appendRow(entry);
        return entry;
    }

    private static void appendRow(HistoryEntry entry) throws IOException {
        Path historyFile = getHistoryFile();
        boolean needsHeader = !Files.exists(historyFile);

        try (var writer = Files.newBufferedWriter(historyFile, StandardCharsets.UTF_8,
                StandardOpenOption.CREATE, StandardOpenOption.APPEND)) {
            if (needsHeader) {
                writer.write("timestamp,path,demo,fileCount,totalSizeBytes,directoryCount,duplicateCount,"
                        + "suspiciousCount,reportFileName\n");
            }
            writer.write(String.join(",",
                    entry.timestamp.toString(),
                    csvQuote(entry.path),
                    String.valueOf(entry.demo),
                    String.valueOf(entry.fileCount),
                    String.valueOf(entry.totalSizeBytes),
                    String.valueOf(entry.directoryCount),
                    String.valueOf(entry.duplicateCount),
                    String.valueOf(entry.suspiciousCount),
                    csvQuote(entry.reportFileName)
            ));
            writer.write("\n");
        }
    }

    /** Loads all previous investigation history entries, most recent first. */
    public static List<HistoryEntry> loadHistory() {
        List<HistoryEntry> entries = new ArrayList<>();
        Path historyFile = getHistoryFile();
        if (!Files.exists(historyFile)) {
            return entries;
        }
        try {
            List<String> lines = Files.readAllLines(historyFile, StandardCharsets.UTF_8);
            for (int i = 1; i < lines.size(); i++) { // skip header
                String line = lines.get(i);
                if (line.isBlank()) continue;
                try {
                    entries.add(parseRow(line));
                } catch (Exception rowEx) {
                    // A single malformed history row should never break the whole history view.
                }
            }
        } catch (IOException e) {
            // No history yet, or unreadable - return whatever we have (possibly empty).
        }
        java.util.Collections.reverse(entries);
        return entries;
    }

    private static HistoryEntry parseRow(String line) {
        List<String> fields = splitCsvLine(line);
        return new HistoryEntry(
                Instant.parse(fields.get(0)),
                fields.get(1),
                Boolean.parseBoolean(fields.get(2)),
                Integer.parseInt(fields.get(3)),
                Long.parseLong(fields.get(4)),
                Integer.parseInt(fields.get(5)),
                Integer.parseInt(fields.get(6)),
                Integer.parseInt(fields.get(7)),
                fields.get(8)
        );
    }

    private static String csvQuote(String value) {
        if (value == null) return "\"\"";
        String escaped = value.replace("\"", "\"\"");
        return "\"" + escaped + "\"";
    }

    /** Minimal CSV line splitter that understands double-quoted fields (no external dependency needed). */
    private static List<String> splitCsvLine(String line) {
        List<String> result = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (inQuotes) {
                if (c == '"') {
                    if (i + 1 < line.length() && line.charAt(i + 1) == '"') {
                        current.append('"');
                        i++;
                    } else {
                        inQuotes = false;
                    }
                } else {
                    current.append(c);
                }
            } else {
                if (c == '"') {
                    inQuotes = true;
                } else if (c == ',') {
                    result.add(current.toString());
                    current.setLength(0);
                } else {
                    current.append(c);
                }
            }
        }
        result.add(current.toString());
        return result;
    }
}

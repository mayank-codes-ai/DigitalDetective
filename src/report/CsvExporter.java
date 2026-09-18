package com.digitaldetective.report;

import com.digitaldetective.model.FileRecord;
import com.digitaldetective.model.Investigation;

import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Exports the complete file inventory of an {@link Investigation} to a CSV
 * file, suitable for opening in Excel/Sheets or further analysis.
 */
public final class CsvExporter {

    private static final String[] HEADERS = {
            "Name", "Absolute Path", "Type", "Extension", "Size (bytes)", "Size (formatted)",
            "Created", "Modified", "Accessed", "SHA-256", "Hidden", "Read-Only",
            "Symbolic Link", "Duplicate", "Suspicious", "Risk Score"
    };

    private CsvExporter() {
    }

    public static void export(Investigation investigation, Path outputFile) throws IOException {
        List<FileRecord> files = investigation.getAllFiles();

        try (Writer writer = Files.newBufferedWriter(outputFile, StandardCharsets.UTF_8)) {
            writer.write(joinCsvRow(HEADERS));
            writer.write("\n");

            for (FileRecord f : files) {
                if (f.isDirectory()) {
                    continue;
                }
                String[] row = {
                        f.getFileName(),
                        f.getAbsolutePath(),
                        f.getFileType() == null ? "" : f.getFileType().getDisplayName(),
                        f.getExtension(),
                        String.valueOf(f.getSizeBytes()),
                        f.getFormattedSize(),
                        f.getFormattedCreated(),
                        f.getFormattedModified(),
                        f.getFormattedAccessed(),
                        f.getSha256() == null ? "" : f.getSha256(),
                        String.valueOf(f.isHidden()),
                        String.valueOf(f.isReadOnly()),
                        String.valueOf(f.isSymbolicLink()),
                        String.valueOf(f.isDuplicate()),
                        String.valueOf(f.isSuspicious()),
                        f.getSuspiciousFinding() == null ? "0" : String.valueOf(f.getSuspiciousFinding().getScore())
                };
                writer.write(joinCsvRow(row));
                writer.write("\n");
            }
        }
    }

    private static String joinCsvRow(String[] values) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < values.length; i++) {
            if (i > 0) sb.append(',');
            sb.append(escape(values[i]));
        }
        return sb.toString();
    }

    private static String escape(String value) {
        if (value == null) {
            return "";
        }
        boolean needsQuoting = value.contains(",") || value.contains("\"") || value.contains("\n") || value.contains("\r");
        String escaped = value.replace("\"", "\"\"");
        return needsQuoting ? "\"" + escaped + "\"" : escaped;
    }
}

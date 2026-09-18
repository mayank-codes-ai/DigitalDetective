package com.digitaldetective.analyzer;

import com.digitaldetective.model.FileRecord;
import com.digitaldetective.model.SuspiciousFinding;
import com.digitaldetective.util.Constants;
import com.digitaldetective.util.DateUtils;
import com.digitaldetective.util.FileUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/**
 * A transparent, rule-based analyzer that flags files which deserve a
 * closer look. It deliberately never asserts that a file <em>is</em>
 * malicious - every score is a heuristic combination of simple, explainable
 * signals, and every finding carries a "review manually" style
 * recommendation rather than a verdict.
 */
public final class SuspiciousAnalyzer {

    /** Minimum score required before a file is surfaced as a finding at all. */
    private static final int MIN_SCORE_TO_FLAG = 20;

    private static final String[] COMMONLY_ABUSED_FOLDER_HINTS = {
            "temp", "tmp", "downloads", "appdata\\local\\temp", "app data", "cache"
    };

    private SuspiciousAnalyzer() {
    }

    public static List<SuspiciousFinding> analyze(List<FileRecord> files) {
        List<SuspiciousFinding> findings = new ArrayList<>();

        for (FileRecord record : files) {
            if (record.isDirectory()) {
                continue;
            }
            SuspiciousFinding finding = evaluate(record);
            if (finding != null) {
                record.setSuspiciousFinding(finding);
                findings.add(finding);
            }
        }

        findings.sort(Comparator.comparingInt(SuspiciousFinding::getScore).reversed());
        return findings;
    }

    /** Evaluates a single file and returns a finding, or {@code null} if it is not flagged. */
    public static SuspiciousFinding evaluate(FileRecord record) {
        int score = 0;
        List<String> reasons = new ArrayList<>();

        String fileName = record.getFileName() == null ? "" : record.getFileName();
        String lowerName = fileName.toLowerCase(Locale.ROOT);
        boolean executable = record.isExecutable();

        if (FileUtils.hasDoubleExtension(fileName)) {
            score += 35;
            reasons.add("Double extension (e.g. \"name.pdf.exe\" style naming)");
        }

        if (executable) {
            score += 25;
            reasons.add("Executable file extension (." + record.getExtension() + ")");
        }

        int keywordHits = 0;
        for (String keyword : Constants.SUSPICIOUS_KEYWORDS) {
            if (lowerName.contains(keyword)) {
                keywordHits++;
                reasons.add("File name contains a commonly suspicious keyword pattern");
            }
        }
        if (keywordHits > 0) {
            score += Math.min(40, keywordHits * 20);
        }

        if (executable && record.isHidden()) {
            score += 15;
            reasons.add("Hidden executable file");
        }

        if (executable && DateUtils.isWithinLastHours(record.getCreatedTime(), 24)) {
            score += 10;
            reasons.add("Executable file created very recently");
        }

        String lowerPath = record.getAbsolutePath() == null ? "" : record.getAbsolutePath().toLowerCase(Locale.ROOT);
        if (executable) {
            for (String hint : COMMONLY_ABUSED_FOLDER_HINTS) {
                if (lowerPath.contains(hint)) {
                    score += 10;
                    reasons.add("Located in a directory commonly used to stage unwanted software");
                    break;
                }
            }
        }

        if (record.isReadOnly() && executable && record.isHidden()) {
            score += 5;
            reasons.add("Hidden, read-only executable - unusual combination for a normal user file");
        }

        score = Math.min(100, score);

        if (score < MIN_SCORE_TO_FLAG || reasons.isEmpty()) {
            return null;
        }

        // De-duplicate identical reason strings (e.g. multiple keyword hits collapse to one line).
        List<String> distinctReasons = new ArrayList<>();
        for (String r : reasons) {
            if (!distinctReasons.contains(r)) {
                distinctReasons.add(r);
            }
        }

        return new SuspiciousFinding(record.getAbsolutePath(), fileName, distinctReasons, score);
    }
}

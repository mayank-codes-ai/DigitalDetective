package com.digitaldetective.analyzer;

import com.digitaldetective.model.Investigation;
import com.digitaldetective.model.Severity;
import com.digitaldetective.model.SuspiciousFinding;

/**
 * Computes the "File Health Score" shown on the Overview page.
 *
 * <p><b>This is a heuristic for educational analysis, not a malware or
 * security verdict.</b> It simply combines a handful of transparent,
 * explainable factors (see {@link #explain(Investigation)}) into a single
 * 0-100 number that gives a quick, at-a-glance impression of an
 * investigation, exactly the way a checklist would.</p>
 */
public final class ForensicScoreCalculator {

    private ForensicScoreCalculator() {
    }

    public static int calculate(Investigation investigation) {
        int score = 100;
        int totalFiles = Math.max(1, investigation.getFileCount());

        int suspiciousCount = investigation.getSuspiciousCount();
        int highSeverity = 0;
        for (SuspiciousFinding f : investigation.getSuspiciousFindings()) {
            if (f.getSeverity() == Severity.HIGH) highSeverity++;
        }

        int duplicateGroups = investigation.getDuplicateGroups().size();
        int hiddenCount = investigation.getHiddenFiles().size();
        int emptyCount = investigation.getEmptyFiles().size();
        int executableCount = investigation.getExecutableCount();

        score -= Math.min(30, suspiciousCount * 3);
        score -= Math.min(20, highSeverity * 5);
        score -= Math.min(15, duplicateGroups);
        score -= Math.min(10, hiddenCount / 2);
        score -= Math.min(5, emptyCount / 5);

        double execRatio = executableCount / (double) totalFiles;
        if (execRatio > 0.10) {
            score -= 10;
        } else if (execRatio > 0.05) {
            score -= 5;
        }

        return Math.max(0, Math.min(100, score));
    }

    /** Human-readable breakdown of how the score was derived, for transparency. */
    public static String explain(Investigation investigation) {
        StringBuilder sb = new StringBuilder();
        sb.append("File Health Score is a heuristic for educational analysis only - ")
          .append("it is NOT a malware or security verdict. It is derived from: ")
          .append("potentially suspicious files, their severity, duplicate file groups, ")
          .append("hidden files, empty files, and the proportion of executable files found.");
        return sb.toString();
    }
}

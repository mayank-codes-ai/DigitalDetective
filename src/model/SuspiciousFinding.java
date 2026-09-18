package com.digitaldetective.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents the result of the rule-based suspicious file analyzer for a
 * single file. This is a heuristic, educational classification - it never
 * claims a file is definitely malicious.
 */
public class SuspiciousFinding {

    private final String filePath;
    private final String fileName;
    private final List<String> reasons;
    private final int score; // 0-100
    private final Severity severity;
    private final String recommendation;

    public SuspiciousFinding(String filePath, String fileName, List<String> reasons, int score) {
        this.filePath = filePath;
        this.fileName = fileName;
        this.reasons = Collections.unmodifiableList(new ArrayList<>(reasons));
        this.score = Math.max(0, Math.min(100, score));
        this.severity = Severity.fromScore(this.score);
        this.recommendation = buildRecommendation(this.severity);
    }

    private static String buildRecommendation(Severity severity) {
        switch (severity) {
            case HIGH:
                return "Review this file manually before opening it. Verify its source and consider "
                        + "scanning it with dedicated antivirus/anti-malware software.";
            case MEDIUM:
                return "Review this file manually before opening it.";
            default:
                return "Low-risk indicator only - no immediate action required, but keep in mind during review.";
        }
    }

    public String getFilePath() {
        return filePath;
    }

    public String getFileName() {
        return fileName;
    }

    public List<String> getReasons() {
        return reasons;
    }

    public int getScore() {
        return score;
    }

    public Severity getSeverity() {
        return severity;
    }

    public String getRecommendation() {
        return recommendation;
    }

    public String getReasonsJoined() {
        return String.join(", ", reasons);
    }

    @Override
    public String toString() {
        return "SuspiciousFinding{" + fileName + ", score=" + score + ", severity=" + severity + '}';
    }
}

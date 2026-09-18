package com.digitaldetective.model;

/**
 * Severity bucket for a {@link SuspiciousFinding}. This is a heuristic
 * classification for educational review purposes only - it is not a
 * malware verdict.
 */
public enum Severity {
    LOW,
    MEDIUM,
    HIGH;

    public static Severity fromScore(int score) {
        if (score >= com.digitaldetective.util.Constants.SEVERITY_HIGH_THRESHOLD) {
            return HIGH;
        } else if (score >= com.digitaldetective.util.Constants.SEVERITY_MEDIUM_THRESHOLD) {
            return MEDIUM;
        }
        return LOW;
    }
}

package com.digitaldetective.util;

import java.util.Set;

/**
 * Central place for application-wide constant values.
 * Keeping these in one place avoids magic numbers/strings scattered
 * throughout the codebase and makes tuning behaviour easy.
 */
public final class Constants {

    private Constants() {
        // utility class, no instances
    }

    public static final String APP_NAME = "Digital Detective";
    public static final String APP_TAGLINE = "File Forensics & Investigation Tool";
    public static final String APP_MOTTO = "Analyze. Investigate. Understand.";
    public static final String APP_VERSION = "1.0.0";

    /** Folder (inside the user's home directory) used to store investigation history. */
    public static final String DATA_FOLDER_NAME = "DigitalDetectiveData";
    public static final String HISTORY_FILE_NAME = "investigation_history.csv";

    /** Number of worker threads used for hashing during a scan. */
    public static final int HASH_THREAD_POOL_SIZE =
            Math.max(2, Runtime.getRuntime().availableProcessors());

    /** Files larger than this are still hashed, but we avoid hashing huge files twice. */
    public static final long LARGE_FILE_WARNING_BYTES = 500L * 1024 * 1024; // 500 MB

    /** How many "recently modified" days count as recent for quick insights. */
    public static final int RECENT_DAYS_WINDOW = 7;

    /** How many top largest files to keep track of. */
    public static final int TOP_LARGEST_FILE_COUNT = 10;

    /** Suspicious score threshold buckets. */
    public static final int SEVERITY_HIGH_THRESHOLD = 70;
    public static final int SEVERITY_MEDIUM_THRESHOLD = 35;

    /** Extensions considered executable / potentially dangerous. */
    public static final Set<String> EXECUTABLE_EXTENSIONS = Set.of(
            "exe", "bat", "cmd", "ps1", "vbs", "js", "jar", "msi", "sh", "scr", "com", "pif"
    );

    /** Keywords that, when found in a file name, raise suspicion. */
    public static final Set<String> SUSPICIOUS_KEYWORDS = Set.of(
            "crack", "keygen", "password", "stealer", "payload", "hack",
            "exploit", "autorun", "backdoor", "trojan", "inject"
    );

    /** Archive extensions, used both for type detection and double-extension checks. */
    public static final Set<String> ARCHIVE_EXTENSIONS = Set.of(
            "zip", "rar", "7z", "tar", "gz", "bz2", "xz", "iso"
    );

    /**
     * Common, everyday "innocent looking" extensions that scammers disguise
     * malware behind (e.g. "invoice.pdf.exe" wants to look like a PDF).
     * Used together with {@link #EXECUTABLE_EXTENSIONS} to detect the classic
     * double-extension trick precisely, instead of flagging every file with
     * more than one dot in its name (which would wrongly flag entirely
     * legitimate compound extensions like "archive.tar.gz" or "app.min.js").
     */
    public static final Set<String> DECOY_EXTENSIONS = Set.of(
            "pdf", "doc", "docx", "rtf", "xls", "xlsx", "ppt", "pptx",
            "jpg", "jpeg", "png", "gif", "bmp", "txt", "csv",
            "mp3", "mp4", "wav", "zip", "rar"
    );

    public static final int PREVIEW_BYTES_FOR_TYPE_SNIFF = 32;
}

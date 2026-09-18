package com.digitaldetective.model;

import java.util.Map;
import java.util.Set;

/**
 * Broad categories that every scanned file is classified into.
 * Detection is extension-based first, then refined by a lightweight
 * magic-byte sniff (see {@code MetadataExtractor}) for the most common
 * binary formats so the tool does not rely on extensions alone.
 */
public enum FileType {
    DOCUMENT("Document"),
    IMAGE("Image"),
    VIDEO("Video"),
    AUDIO("Audio"),
    ARCHIVE("Archive"),
    EXECUTABLE("Executable"),
    SOURCE_CODE("Source Code"),
    SPREADSHEET("Spreadsheet"),
    PRESENTATION("Presentation"),
    TEXT("Text"),
    DATABASE("Database"),
    UNKNOWN("Unknown");

    private final String displayName;

    FileType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    /** Extension -> FileType lookup table. Kept modular so it is easy to extend later. */
    private static final Map<String, FileType> EXTENSION_MAP = buildExtensionMap();

    private static Map<String, FileType> buildExtensionMap() {
        Map<String, FileType> m = new java.util.HashMap<>();

        for (String e : Set.of("pdf", "doc", "docx", "odt", "rtf", "wps")) m.put(e, DOCUMENT);
        for (String e : Set.of("jpg", "jpeg", "png", "gif", "bmp", "svg", "webp", "tiff", "ico", "heic"))
            m.put(e, IMAGE);
        for (String e : Set.of("mp4", "mkv", "avi", "mov", "wmv", "flv", "webm", "m4v"))
            m.put(e, VIDEO);
        for (String e : Set.of("mp3", "wav", "flac", "aac", "ogg", "wma", "m4a"))
            m.put(e, AUDIO);
        for (String e : Set.of("zip", "rar", "7z", "tar", "gz", "bz2", "xz", "iso"))
            m.put(e, ARCHIVE);
        for (String e : Set.of("exe", "bat", "cmd", "ps1", "vbs", "js", "jar", "msi", "sh", "scr", "com", "pif"))
            m.put(e, EXECUTABLE);
        for (String e : Set.of("java", "py", "c", "cpp", "h", "hpp", "cs", "rb", "go", "rs",
                "ts", "tsx", "jsx", "php", "swift", "kt", "html", "css", "sql", "xml", "json", "yml", "yaml"))
            m.put(e, SOURCE_CODE);
        for (String e : Set.of("xls", "xlsx", "csv", "ods")) m.put(e, SPREADSHEET);
        for (String e : Set.of("ppt", "pptx", "odp")) m.put(e, PRESENTATION);
        for (String e : Set.of("txt", "md", "log", "ini", "cfg", "conf")) m.put(e, TEXT);
        for (String e : Set.of("db", "sqlite", "mdb", "accdb", "sql", "dat")) m.put(e, DATABASE);

        return java.util.Collections.unmodifiableMap(m);
    }

    /**
     * Resolve a {@link FileType} from a lower-case extension (without the dot).
     * Never returns null - falls back to {@link #UNKNOWN}.
     */
    public static FileType fromExtension(String extension) {
        if (extension == null || extension.isBlank()) {
            return UNKNOWN;
        }
        return EXTENSION_MAP.getOrDefault(extension.toLowerCase(), UNKNOWN);
    }
}

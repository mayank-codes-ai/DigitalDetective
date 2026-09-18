package com.digitaldetective.util;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

/**
 * Small static helpers for formatting and OS interaction. Kept separate
 * from the model classes so formatting rules can change in one place.
 */
public final class FileUtils {

    private FileUtils() {
    }

    private static final String[] UNITS = {"B", "KB", "MB", "GB", "TB", "PB"};

    /** Format a byte count as a human readable string, e.g. "8.7 GB". */
    public static String formatSize(long bytes) {
        if (bytes < 0) return "0 B";
        if (bytes < 1024) return bytes + " B";
        double size = bytes;
        int unitIndex = 0;
        while (size >= 1024 && unitIndex < UNITS.length - 1) {
            size /= 1024;
            unitIndex++;
        }
        return String.format("%.2f %s", size, UNITS[unitIndex]);
    }

    /** Extract the lower-case extension (without dot) from a file name, or "" if none. */
    public static String getExtension(String fileName) {
        if (fileName == null) return "";
        int dot = fileName.lastIndexOf('.');
        if (dot <= 0 || dot == fileName.length() - 1) {
            return "";
        }
        return fileName.substring(dot + 1).toLowerCase();
    }

    /**
     * Detects the classic "double extension" disguise trick, e.g.
     * "invoice.pdf.exe" or "photo.jpg.scr" - a common, innocent-looking
     * extension (see {@link Constants#DECOY_EXTENSIONS}) immediately
     * followed by an executable one (see {@link Constants#EXECUTABLE_EXTENSIONS}).
     *
     * <p>Deliberately <b>not</b> a simple "more than one dot" check: that
     * naive approach would wrongly flag entirely legitimate compound
     * extensions such as "archive.tar.gz", "app.min.js" or "styles.min.css".
     * Only the specific decoy-extension-then-executable-extension pattern is
     * considered suspicious.</p>
     */
    public static boolean hasDoubleExtension(String fileName) {
        if (fileName == null) return false;
        String[] parts = fileName.split("\\.");
        if (parts.length < 3) {
            return false;
        }
        String finalExtension = parts[parts.length - 1].toLowerCase();
        String decoyExtension = parts[parts.length - 2].toLowerCase();

        if (!Constants.EXECUTABLE_EXTENSIONS.contains(finalExtension)) {
            return false;
        }
        return Constants.DECOY_EXTENSIONS.contains(decoyExtension);
    }

    /** Best-effort "open containing folder" using the desktop integration APIs. */
    public static void openContainingFolder(String absolutePath) {
        try {
            File file = new File(absolutePath);
            File parent = file.getParentFile();
            if (parent != null && Desktop.isDesktopSupported()) {
                Desktop.getDesktop().open(parent);
            }
        } catch (IOException | UnsupportedOperationException ex) {
            // Best effort only - silently ignore on unsupported platforms/headless envs.
        }
    }

    /** Copies the given text to the system clipboard. */
    public static void copyToClipboard(String text) {
        java.awt.datatransfer.StringSelection selection = new java.awt.datatransfer.StringSelection(text);
        java.awt.Toolkit.getDefaultToolkit().getSystemClipboard().setContents(selection, null);
    }

    /** Safely resolve a relative path against a root, defaulting to the absolute path on failure. */
    public static String safeRelativize(Path root, Path target) {
        try {
            return root.relativize(target).toString();
        } catch (IllegalArgumentException ex) {
            return target.toString();
        }
    }
}

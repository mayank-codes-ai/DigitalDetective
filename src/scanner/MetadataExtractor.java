package com.digitaldetective.scanner;

import com.digitaldetective.model.FileRecord;
import com.digitaldetective.model.FileType;
import com.digitaldetective.util.FileUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.DosFileAttributeView;
import java.nio.file.attribute.DosFileAttributes;
import java.time.Instant;

/**
 * Reads file system metadata (size, timestamps, hidden/read-only flags,
 * symbolic link status) using Java NIO and produces a partially populated
 * {@link FileRecord}. The SHA-256 hash is intentionally left for the
 * caller to fill in, since that is a much more expensive operation that
 * {@code FileScanner} schedules separately on the hashing thread pool.
 */
public final class MetadataExtractor {

    private MetadataExtractor() {
    }

    public static FileRecord extract(Path root, Path path) throws IOException {
        BasicFileAttributes attrs = Files.readAttributes(path, BasicFileAttributes.class,
                LinkOption.NOFOLLOW_LINKS);

        FileRecord record = new FileRecord();
        String fileName = path.getFileName() != null ? path.getFileName().toString() : path.toString();
        record.setFileName(fileName);
        record.setAbsolutePath(path.toAbsolutePath().toString());
        record.setRelativePath(FileUtils.safeRelativize(root, path));

        String ext = FileUtils.getExtension(fileName);
        record.setExtension(ext);
        record.setFileType(FileType.fromExtension(ext));

        record.setSizeBytes(attrs.isDirectory() ? 0 : attrs.size());
        record.setDirectory(attrs.isDirectory());
        record.setSymbolicLink(attrs.isSymbolicLink());

        record.setCreatedTime(safeInstant(attrs.creationTime() == null ? null : attrs.creationTime().toInstant()));
        record.setModifiedTime(safeInstant(attrs.lastModifiedTime() == null ? null : attrs.lastModifiedTime().toInstant()));
        record.setAccessedTime(safeInstant(attrs.lastAccessTime() == null ? null : attrs.lastAccessTime().toInstant()));

        record.setHidden(detectHidden(path));
        record.setReadOnly(detectReadOnly(path));

        return record;
    }

    private static Instant safeInstant(Instant instant) {
        // Some file systems report the epoch (1970) when a timestamp is genuinely
        // unavailable rather than throwing - treat that as "unknown" for display purposes.
        if (instant == null) return null;
        return instant.getEpochSecond() <= 0 ? null : instant;
    }

    private static boolean detectHidden(Path path) {
        try {
            // Works cross-platform: on POSIX systems this reflects the dot-file
            // convention; on Windows it reflects the DOS hidden attribute.
            return Files.isHidden(path);
        } catch (IOException e) {
            String name = path.getFileName() == null ? "" : path.getFileName().toString();
            return name.startsWith(".");
        }
    }

    private static boolean detectReadOnly(Path path) {
        try {
            DosFileAttributeView dosView = Files.getFileAttributeView(path, DosFileAttributeView.class);
            if (dosView != null) {
                DosFileAttributes dosAttrs = dosView.readAttributes();
                return dosAttrs.isReadOnly();
            }
        } catch (IOException ignored) {
            // fall through to the POSIX-based check below
        }
        return !Files.isWritable(path);
    }
}

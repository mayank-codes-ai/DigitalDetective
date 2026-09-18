package com.digitaldetective.model;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

/**
 * Immutable-ish data holder representing everything the scanner learned
 * about a single file on disk. Instances are created by
 * {@code com.digitaldetective.scanner.FileScanner} and then consumed by
 * every analyzer and UI view in the application.
 */
public class FileRecord {

    private static final DateTimeFormatter DISPLAY_FORMAT =
            DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm:ss").withZone(ZoneId.systemDefault());

    private String fileName;
    private String absolutePath;
    private String relativePath;
    private String extension;
    private FileType fileType;
    private long sizeBytes;

    private Instant createdTime;   // may be null if unsupported
    private Instant modifiedTime;
    private Instant accessedTime;  // may be null if unsupported

    private String sha256;
    private boolean hidden;
    private boolean readOnly;
    private boolean symbolicLink;
    private boolean directory;

    // Populated lazily by analyzers - not part of raw scan metadata.
    private boolean duplicate;
    private SuspiciousFinding suspiciousFinding; // null if not suspicious

    public FileRecord() {
        // default constructor for incremental building by the scanner
    }

    public FileRecord(String fileName, String absolutePath, String relativePath, String extension,
                       FileType fileType, long sizeBytes, Instant createdTime, Instant modifiedTime,
                       Instant accessedTime, String sha256, boolean hidden, boolean readOnly,
                       boolean symbolicLink, boolean directory) {
        this.fileName = fileName;
        this.absolutePath = absolutePath;
        this.relativePath = relativePath;
        this.extension = extension;
        this.fileType = fileType;
        this.sizeBytes = sizeBytes;
        this.createdTime = createdTime;
        this.modifiedTime = modifiedTime;
        this.accessedTime = accessedTime;
        this.sha256 = sha256;
        this.hidden = hidden;
        this.readOnly = readOnly;
        this.symbolicLink = symbolicLink;
        this.directory = directory;
    }

    // ---- Getters / Setters -------------------------------------------------

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getAbsolutePath() {
        return absolutePath;
    }

    public void setAbsolutePath(String absolutePath) {
        this.absolutePath = absolutePath;
    }

    public String getRelativePath() {
        return relativePath;
    }

    public void setRelativePath(String relativePath) {
        this.relativePath = relativePath;
    }

    public String getExtension() {
        return extension;
    }

    public void setExtension(String extension) {
        this.extension = extension;
    }

    public FileType getFileType() {
        return fileType;
    }

    public void setFileType(FileType fileType) {
        this.fileType = fileType;
    }

    public long getSizeBytes() {
        return sizeBytes;
    }

    public void setSizeBytes(long sizeBytes) {
        this.sizeBytes = sizeBytes;
    }

    public Instant getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(Instant createdTime) {
        this.createdTime = createdTime;
    }

    public Instant getModifiedTime() {
        return modifiedTime;
    }

    public void setModifiedTime(Instant modifiedTime) {
        this.modifiedTime = modifiedTime;
    }

    public Instant getAccessedTime() {
        return accessedTime;
    }

    public void setAccessedTime(Instant accessedTime) {
        this.accessedTime = accessedTime;
    }

    public String getSha256() {
        return sha256;
    }

    public void setSha256(String sha256) {
        this.sha256 = sha256;
    }

    public boolean isHidden() {
        return hidden;
    }

    public void setHidden(boolean hidden) {
        this.hidden = hidden;
    }

    public boolean isReadOnly() {
        return readOnly;
    }

    public void setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
    }

    public boolean isSymbolicLink() {
        return symbolicLink;
    }

    public void setSymbolicLink(boolean symbolicLink) {
        this.symbolicLink = symbolicLink;
    }

    public boolean isDirectory() {
        return directory;
    }

    public void setDirectory(boolean directory) {
        this.directory = directory;
    }

    public boolean isDuplicate() {
        return duplicate;
    }

    public void setDuplicate(boolean duplicate) {
        this.duplicate = duplicate;
    }

    public SuspiciousFinding getSuspiciousFinding() {
        return suspiciousFinding;
    }

    public void setSuspiciousFinding(SuspiciousFinding suspiciousFinding) {
        this.suspiciousFinding = suspiciousFinding;
    }

    public boolean isSuspicious() {
        return suspiciousFinding != null;
    }

    public boolean isEmpty() {
        return !directory && sizeBytes == 0;
    }

    public boolean isExecutable() {
        return extension != null
                && com.digitaldetective.util.Constants.EXECUTABLE_EXTENSIONS.contains(extension.toLowerCase());
    }

    // ---- Convenience / display helpers -------------------------------------

    public String getFormattedCreated() {
        return createdTime == null ? "N/A" : DISPLAY_FORMAT.format(createdTime);
    }

    public String getFormattedModified() {
        return modifiedTime == null ? "N/A" : DISPLAY_FORMAT.format(modifiedTime);
    }

    public String getFormattedAccessed() {
        return accessedTime == null ? "N/A" : DISPLAY_FORMAT.format(accessedTime);
    }

    public String getFormattedSize() {
        return com.digitaldetective.util.FileUtils.formatSize(sizeBytes);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FileRecord)) return false;
        FileRecord that = (FileRecord) o;
        return Objects.equals(absolutePath, that.absolutePath);
    }

    @Override
    public int hashCode() {
        return Objects.hash(absolutePath);
    }

    @Override
    public String toString() {
        return "FileRecord{" + fileName + ", " + getFormattedSize() + ", " + fileType + '}';
    }
}

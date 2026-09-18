package com.digitaldetective.model;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Aggregate root holding everything gathered and derived from a single
 * scan: the raw file records plus every analyzer's output. A single
 * instance of this class flows from the scanner, through the analyzers,
 * into the UI and the report generators.
 */
public class Investigation {

    private String investigationPath;
    private Instant scanStarted;
    private Instant scanCompleted;
    private boolean demo;
    private String notes = "";

    // Wrapped with Collections.synchronizedList because FileScanner adds
    // records concurrently from multiple hashing worker threads during a scan.
    private final List<FileRecord> allFiles = java.util.Collections.synchronizedList(new ArrayList<>());
    private final ConcurrentLinkedQueue<String> accessErrors = new ConcurrentLinkedQueue<>();

    // Derived / analyzer outputs - populated after scanning completes.
    private List<List<FileRecord>> duplicateGroups = new ArrayList<>();
    private List<SuspiciousFinding> suspiciousFindings = new ArrayList<>();
    private Map<FileType, Long> typeCountDistribution = Map.of();
    private Map<FileType, Long> typeSizeDistribution = Map.of();
    private List<FileRecord> topLargestFiles = new ArrayList<>();
    private List<FileRecord> hiddenFiles = new ArrayList<>();
    private List<FileRecord> emptyFiles = new ArrayList<>();
    private List<FileRecord> recentlyModifiedFiles = new ArrayList<>();
    private int directoryCount;
    private int forensicScore = 100;

    public String getInvestigationPath() {
        return investigationPath;
    }

    public void setInvestigationPath(String investigationPath) {
        this.investigationPath = investigationPath;
    }

    public Instant getScanStarted() {
        return scanStarted;
    }

    public void setScanStarted(Instant scanStarted) {
        this.scanStarted = scanStarted;
    }

    public Instant getScanCompleted() {
        return scanCompleted;
    }

    public void setScanCompleted(Instant scanCompleted) {
        this.scanCompleted = scanCompleted;
    }

    public boolean isDemo() {
        return demo;
    }

    public void setDemo(boolean demo) {
        this.demo = demo;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes == null ? "" : notes;
    }

    public List<FileRecord> getAllFiles() {
        return allFiles;
    }

    public void addFile(FileRecord record) {
        allFiles.add(record);
    }

    public ConcurrentLinkedQueue<String> getAccessErrors() {
        return accessErrors;
    }

    public void addAccessError(String message) {
        accessErrors.add(message);
    }

    public List<List<FileRecord>> getDuplicateGroups() {
        return duplicateGroups;
    }

    public void setDuplicateGroups(List<List<FileRecord>> duplicateGroups) {
        this.duplicateGroups = duplicateGroups;
    }

    public List<SuspiciousFinding> getSuspiciousFindings() {
        return suspiciousFindings;
    }

    public void setSuspiciousFindings(List<SuspiciousFinding> suspiciousFindings) {
        this.suspiciousFindings = suspiciousFindings;
    }

    public Map<FileType, Long> getTypeCountDistribution() {
        return typeCountDistribution;
    }

    public void setTypeCountDistribution(Map<FileType, Long> typeCountDistribution) {
        this.typeCountDistribution = typeCountDistribution;
    }

    public Map<FileType, Long> getTypeSizeDistribution() {
        return typeSizeDistribution;
    }

    public void setTypeSizeDistribution(Map<FileType, Long> typeSizeDistribution) {
        this.typeSizeDistribution = typeSizeDistribution;
    }

    public List<FileRecord> getTopLargestFiles() {
        return topLargestFiles;
    }

    public void setTopLargestFiles(List<FileRecord> topLargestFiles) {
        this.topLargestFiles = topLargestFiles;
    }

    public List<FileRecord> getHiddenFiles() {
        return hiddenFiles;
    }

    public void setHiddenFiles(List<FileRecord> hiddenFiles) {
        this.hiddenFiles = hiddenFiles;
    }

    public List<FileRecord> getEmptyFiles() {
        return emptyFiles;
    }

    public void setEmptyFiles(List<FileRecord> emptyFiles) {
        this.emptyFiles = emptyFiles;
    }

    public List<FileRecord> getRecentlyModifiedFiles() {
        return recentlyModifiedFiles;
    }

    public void setRecentlyModifiedFiles(List<FileRecord> recentlyModifiedFiles) {
        this.recentlyModifiedFiles = recentlyModifiedFiles;
    }

    public int getDirectoryCount() {
        return directoryCount;
    }

    public void setDirectoryCount(int directoryCount) {
        this.directoryCount = directoryCount;
    }

    public int getForensicScore() {
        return forensicScore;
    }

    public void setForensicScore(int forensicScore) {
        this.forensicScore = forensicScore;
    }

    // ---- Derived convenience stats -----------------------------------------

    public int getFileCount() {
        int n = 0;
        for (FileRecord r : allFiles) {
            if (!r.isDirectory()) n++;
        }
        return n;
    }

    public long getTotalSizeBytes() {
        long total = 0;
        for (FileRecord r : allFiles) {
            if (!r.isDirectory()) total += r.getSizeBytes();
        }
        return total;
    }

    public int getDuplicateFileCount() {
        int n = 0;
        for (List<FileRecord> group : duplicateGroups) {
            n += group.size();
        }
        return n;
    }

    public long getWastedDuplicateBytes() {
        long total = 0;
        for (List<FileRecord> group : duplicateGroups) {
            if (group.isEmpty()) continue;
            long each = group.get(0).getSizeBytes();
            total += each * (group.size() - 1L);
        }
        return total;
    }

    public int getSuspiciousCount() {
        return suspiciousFindings.size();
    }

    public int getExecutableCount() {
        int n = 0;
        for (FileRecord r : allFiles) {
            if (r.isExecutable()) n++;
        }
        return n;
    }

    public FileRecord getLargestFile() {
        return topLargestFiles.isEmpty() ? null : topLargestFiles.get(0);
    }
}

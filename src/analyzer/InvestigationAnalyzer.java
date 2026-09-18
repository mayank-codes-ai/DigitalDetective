package com.digitaldetective.analyzer;

import com.digitaldetective.model.FileRecord;
import com.digitaldetective.model.Investigation;
import com.digitaldetective.util.Constants;

import java.util.List;

/**
 * Orchestrates the individual analyzers over a freshly scanned
 * {@link Investigation}, populating every derived field
 * (duplicates, suspicious findings, distributions, top files, etc.).
 * Kept as a single entry point so the UI layer only needs one call
 * after a scan finishes.
 */
public final class InvestigationAnalyzer {

    private InvestigationAnalyzer() {
    }

    public static void runAll(Investigation investigation) {
        List<FileRecord> files = investigation.getAllFiles();

        investigation.setDuplicateGroups(DuplicateAnalyzer.findDuplicateGroups(files));
        investigation.setSuspiciousFindings(SuspiciousAnalyzer.analyze(files));
        investigation.setTypeCountDistribution(StorageAnalyzer.typeCountDistribution(files));
        investigation.setTypeSizeDistribution(StorageAnalyzer.typeSizeDistribution(files));
        investigation.setTopLargestFiles(StorageAnalyzer.topLargestFiles(files, Constants.TOP_LARGEST_FILE_COUNT));
        investigation.setHiddenFiles(StorageAnalyzer.hiddenFiles(files));
        investigation.setEmptyFiles(StorageAnalyzer.emptyFiles(files));
        investigation.setRecentlyModifiedFiles(
                TimelineAnalyzer.recentlyModified(files, Constants.RECENT_DAYS_WINDOW));

        investigation.setForensicScore(ForensicScoreCalculator.calculate(investigation));
    }
}

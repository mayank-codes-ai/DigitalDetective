package com.digitaldetective.analyzer;

import com.digitaldetective.model.FileRecord;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Detects duplicate files using a two-stage grouping strategy:
 *
 * <ol>
 *   <li><b>Stage 1 - group by size.</b> Two files can only be byte-identical
 *       if they have the same size, so this is a cheap O(n) pass that throws
 *       away the vast majority of candidates immediately.</li>
 *   <li><b>Stage 2 - within each size bucket that has more than one file,
 *       group by SHA-256 hash.</b> Only buckets that still have 2+ files
 *       after this step are real duplicate groups.</li>
 * </ol>
 *
 * <p>Note: {@code FileScanner} already computes the SHA-256 hash for every
 * file once, up front, because the hash is also needed elsewhere in the
 * application (Hash Explorer search, file details, CSV/HTML reports). This
 * analyzer therefore does not recompute any hashes - it reuses them - but it
 * still performs the size-first grouping so that duplicate detection itself
 * runs in O(n) instead of comparing every file's hash against every other
 * file's hash (which would be O(n^2)).</p>
 */
public final class DuplicateAnalyzer {

    private DuplicateAnalyzer() {
    }

    public static List<List<FileRecord>> findDuplicateGroups(List<FileRecord> files) {
        // Stage 1: group by size (skip directories and empty files - an "empty"
        // duplicate group of zero-byte files is not forensically interesting and
        // is already covered by the dedicated Empty Files view).
        Map<Long, List<FileRecord>> bySize = new HashMap<>();
        for (FileRecord record : files) {
            if (record.isDirectory() || record.getSizeBytes() <= 0) {
                continue;
            }
            bySize.computeIfAbsent(record.getSizeBytes(), k -> new ArrayList<>()).add(record);
        }

        List<List<FileRecord>> duplicateGroups = new ArrayList<>();

        // Stage 2: within same-size buckets, group by hash.
        for (List<FileRecord> sameSizeFiles : bySize.values()) {
            if (sameSizeFiles.size() < 2) {
                continue;
            }
            Map<String, List<FileRecord>> byHash = new HashMap<>();
            for (FileRecord record : sameSizeFiles) {
                String hash = record.getSha256();
                if (hash == null || hash.isEmpty()) {
                    continue; // hashing failed for this file - can't confirm duplication
                }
                byHash.computeIfAbsent(hash, k -> new ArrayList<>()).add(record);
            }
            for (List<FileRecord> group : byHash.values()) {
                if (group.size() >= 2) {
                    group.forEach(r -> r.setDuplicate(true));
                    duplicateGroups.add(group);
                }
            }
        }

        duplicateGroups.sort(Comparator.<List<FileRecord>>comparingLong(
                g -> g.get(0).getSizeBytes() * (long) (g.size() - 1)).reversed());

        return duplicateGroups;
    }

    public static long wastedBytes(List<FileRecord> group) {
        if (group.isEmpty()) return 0;
        return group.get(0).getSizeBytes() * (long) (group.size() - 1);
    }
}

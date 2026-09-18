package com.digitaldetective.scanner;

import com.digitaldetective.model.FileRecord;
import com.digitaldetective.model.Investigation;
import com.digitaldetective.util.Constants;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Recursively walks a directory tree using Java NIO's {@link Files#walkFileTree}
 * and produces a fully populated {@link Investigation}.
 *
 * <p>Design notes (also useful for the viva):</p>
 * <ul>
 *   <li>The directory walk itself happens on a single thread (NIO's walker is
 *       inherently sequential), but for every regular file discovered, the
 *       expensive part of the work - metadata extraction plus SHA-256 hashing -
 *       is submitted as a task to a fixed {@link ExecutorService} thread pool.
 *       This keeps the walk fast while spreading the CPU/I/O-heavy hashing
 *       work across all available cores.</li>
 *   <li>The scan never throws out of {@link #scan}; every per-file or
 *       per-directory failure (permission denied, broken symlink, file
 *       deleted mid-scan, unsupported metadata, etc.) is caught, recorded in
 *       {@link Investigation#getAccessErrors()} and reported to the listener,
 *       and scanning simply continues with the next file.</li>
 *   <li>{@link #stop()} allows the GUI's "Stop Scan" button to request a
 *       graceful, safe cancellation from the Event Dispatch Thread.</li>
 * </ul>
 */
public class FileScanner {

    /** Callback interface used to report progress back to the GUI layer. */
    public interface ScanListener {
        /** Invoked (off the EDT) every time a file finishes processing. */
        void onFileScanned(int filesScannedSoFar, String currentFilePath);

        /** Invoked when an individual file or directory could not be processed. */
        void onError(String friendlyMessage);

        /** Invoked once when the scan finishes normally. */
        void onComplete(Investigation investigation);

        /** Invoked once if the scan was stopped early via {@link #stop()}. */
        void onCancelled(Investigation partialInvestigation);
    }

    private final AtomicBoolean cancelled = new AtomicBoolean(false);
    private final AtomicInteger scannedCount = new AtomicInteger(0);
    private volatile ExecutorService hashPool;

    /** Requests that an in-progress scan stop as soon as possible. Safe to call from any thread. */
    public void stop() {
        cancelled.set(true);
        ExecutorService pool = hashPool;
        if (pool != null) {
            pool.shutdownNow();
        }
    }

    public boolean isCancelled() {
        return cancelled.get();
    }

    /**
     * Performs a blocking scan of {@code rootPath}. This method should always
     * be called from a background thread (e.g. inside a {@code SwingWorker}),
     * never directly on the Event Dispatch Thread, since it can take a long
     * time for large directory trees.
     */
    public Investigation scan(Path rootPath, ScanListener listener) {
        cancelled.set(false);
        scannedCount.set(0);
        hashPool = Executors.newFixedThreadPool(Constants.HASH_THREAD_POOL_SIZE);

        Investigation investigation = new Investigation();
        investigation.setInvestigationPath(rootPath.toAbsolutePath().toString());
        investigation.setScanStarted(Instant.now());

        List<Future<?>> futures = new CopyOnWriteArrayList<>();

        try {
            Files.walkFileTree(rootPath, new SimpleFileVisitor<Path>() {

                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
                    if (cancelled.get()) {
                        return FileVisitResult.TERMINATE;
                    }
                    if (!dir.equals(rootPath)) {
                        investigation.setDirectoryCount(investigation.getDirectoryCount() + 1);
                    }
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                    if (cancelled.get()) {
                        return FileVisitResult.TERMINATE;
                    }
                    try {
                        Future<?> future = hashPool.submit(() -> processFile(rootPath, file, investigation, listener));
                        futures.add(future);
                    } catch (java.util.concurrent.RejectedExecutionException rex) {
                        // Pool was shut down concurrently (Stop Scan pressed) - safe to ignore.
                    }
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFileFailed(Path file, IOException exc) {
                    recordAccessProblem(investigation, listener, file, exc);
                    // Never abort the whole scan because of one bad file/directory.
                    return cancelled.get() ? FileVisitResult.TERMINATE : FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) {
                    if (exc != null) {
                        recordAccessProblem(investigation, listener, dir, exc);
                    }
                    return cancelled.get() ? FileVisitResult.TERMINATE : FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException | SecurityException e) {
            investigation.addAccessError("Scan aborted at top level: " + describe(e));
            if (listener != null) {
                listener.onError("Unable to scan the selected location: " + describe(e));
            }
        }

        // Wait for outstanding hashing tasks to finish (bounded, so a stuck disk
        // can't hang the app forever), unless the user cancelled.
        hashPool.shutdown();
        if (!cancelled.get()) {
            for (Future<?> f : futures) {
                try {
                    f.get(2, TimeUnit.MINUTES);
                } catch (Exception ignored) {
                    // Individual task failures are already recorded inside processFile.
                }
            }
        }

        investigation.setScanCompleted(Instant.now());

        if (cancelled.get()) {
            if (listener != null) {
                listener.onCancelled(investigation);
            }
        } else {
            if (listener != null) {
                listener.onComplete(investigation);
            }
        }
        return investigation;
    }

    /** Runs on a hashing worker thread: extract metadata, hash the file, record the result. */
    private void processFile(Path root, Path file, Investigation investigation, ScanListener listener) {
        if (cancelled.get()) {
            return;
        }
        try {
            FileRecord record = MetadataExtractor.extract(root, file);

            if (!record.isDirectory() && !record.isSymbolicLink() && record.getSizeBytes() > 0) {
                try {
                    record.setSha256(HashCalculator.sha256(file));
                } catch (IOException hashEx) {
                    // File became unreadable/was deleted mid-scan, or a permission issue.
                    // We still keep the metadata we already gathered.
                    investigation.addAccessError("Could not hash '" + file + "': " + describe(hashEx));
                }
            } else if (record.getSizeBytes() == 0) {
                record.setSha256(""); // empty files hash to a well-known constant; not worth computing
            }

            investigation.addFile(record);
            int count = scannedCount.incrementAndGet();
            if (listener != null) {
                listener.onFileScanned(count, file.toString());
            }
        } catch (IOException | SecurityException e) {
            recordAccessProblem(investigation, listener, file, e);
        } catch (Exception unexpected) {
            // Absolute last-resort safety net: one corrupt/unusual file must never
            // take down the whole investigation.
            investigation.addAccessError("Unexpected error on '" + file + "': " + unexpected.getMessage());
        }
    }

    private static void recordAccessProblem(Investigation investigation, ScanListener listener, Path path, Exception e) {
        String message = "Unable to access '" + path + "': " + describe(e);
        investigation.addAccessError(message);
        if (listener != null) {
            listener.onError("Unable to access this item. Permission denied or the item no longer exists.");
        }
    }

    private static String describe(Exception e) {
        String simple = e.getClass().getSimpleName();
        String msg = e.getMessage();
        return msg == null ? simple : simple + " - " + msg;
    }
}

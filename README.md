# 🕵️ Digital Detective

### File Forensics & Investigation Tool

> *"Analyze. Investigate. Understand."*

A polished, fully-working **Core Java + Swing** desktop application that
performs a forensic-style analysis of a folder or drive: file inventory,
SHA-256 hashing, duplicate detection, suspicious-file heuristics, a
storage/timeline dashboard, and professional HTML/CSV report generation -
all running **100% locally**, with **no database, no internet connection,
and no external services**.

Built as a college project demonstrating Java NIO, multithreading,
collections, SHA-256 hashing, Swing GUI development, and clean
object-oriented design.

---

## Table of Contents

- [Features](#features)
- [Technology Used](#technology-used)
- [Requirements](#requirements)
- [How to Run](#how-to-run)
- [How to Use](#how-to-use)
- [Project Architecture](#project-architecture)
- [Folder Structure](#folder-structure)
- [Screenshots](#screenshots)
- [Limitations](#limitations)
- [Future Enhancements](#future-enhancements)
- [Educational Disclaimer](#educational-disclaimer)

---

## Features

- **Recursive folder scanning** using Java NIO (`Files.walkFileTree`), with
  graceful handling of permission-denied folders, broken symbolic links,
  files deleted mid-scan, and unsupported metadata - the scan never
  crashes because of a single bad file.
- **Multithreaded scanning** - the directory walk stays fast while a fixed
  thread pool (`ExecutorService`) hashes files in parallel, keeping the GUI
  fully responsive with a live progress dialog and a working **Stop Scan**
  button.
- **SHA-256 hashing** of every file, shown in the UI and searchable from a
  dedicated **Hash Explorer**.
- **Duplicate file detection** using an efficient two-stage strategy:
  group by size first, then compare hashes only within matching-size
  groups (see [ARCHITECTURE.md](ARCHITECTURE.md) for the full algorithm
  discussion).
- **Rule-based suspicious file analyzer** - double extensions
  (`invoice.pdf.exe`), executable extensions, suspicious keywords, hidden
  executables, and more, each combined into a transparent 0-100 risk score
  and LOW/MEDIUM/HIGH severity. **This is a heuristic educational tool, not
  an antivirus** - it never claims a file is definitely malicious.
- **File type detection** across 12 categories (Documents, Images, Videos,
  Audio, Archives, Executables, Source Code, Spreadsheets, Presentations,
  Text, Database, Unknown).
- **Timeline view** of created/modified/accessed events with quick range
  filters (Today, Last 24 Hours, Last 7 Days, Last 30 Days).
- **Storage analytics** with hand-drawn Java2D pie and bar charts (no
  external charting library needed) plus a Top 10 Largest Files list.
- **Hidden file** and **empty file** detection.
- **Searchable, filterable File Explorer** table (by name, extension, path,
  hash, or type).
- **File Health Score** - a transparent, explainable 0-100 heuristic score
  combining suspicious files, duplicates, hidden files, empty files, and
  executable ratio. Clearly **not** a malware/security verdict.
- **Professional HTML report generation** and **CSV inventory export**,
  entirely offline, with a dark cybersecurity-inspired theme.
- **Investigation history** - lightweight local history (CSV + saved HTML
  snapshots) since the project deliberately avoids a database.
- **Investigation notes** that get embedded into the generated report.
- **Demo Mode** - loads a realistic, entirely synthetic investigation so
  you can explore or present every screen without scanning a real folder.
- **Dark, professional, cybersecurity-inspired UI** built with Java Swing.

## Technology Used

- **Java 17+** (developed and tested against Java 17 and Java 21)
- **Java Swing** for the GUI (see [why Swing instead of JavaFX](#why-swing-instead-of-javafx))
- **Java NIO** (`java.nio.file`) for all file system access
- **`java.security.MessageDigest`** for SHA-256 hashing
- **`java.util.concurrent`** (`ExecutorService`, `SwingWorker`,
  `AtomicBoolean`/`AtomicInteger`) for multithreaded, non-blocking scanning
- **`java.time`** for timestamps
- **Java2D** (`Graphics2D`) for hand-drawn pie/bar charts
- **JUnit 5** for unit and integration tests
- **Maven** for build/dependency management

No database. No internet connection required at runtime. No API keys. No
paid software. No cloud services.

### Why Swing instead of JavaFX?

JavaFX was seriously considered, but as of Java 11+ it is no longer bundled
with the JDK and requires either a separate JavaFX SDK download plus
`--module-path`/`--add-modules` VM arguments, or a `javafx-maven-plugin`
that pulls platform-specific native binaries from Maven Central. That adds
real friction for "open in VS Code and run" on a machine that may have
limited or restricted internet access - exactly the situation this project
is designed to avoid. **Java Swing ships inside every standard JDK**, so
`mvn exec:java` or a single `java -jar` works immediately, everywhere,
with zero extra setup. The trade-off is documented here transparently, as
requested, and the UI is still built to a professional, cohesive dark
theme (see `Theme.java`).

## Requirements

- **JDK 17 or newer** installed and on your `PATH`
- **Maven 3.6+** (only needed to build/test with `mvn`; you can also
  compile directly with `javac` - see below)
- **VS Code** with the "Extension Pack for Java" (recommended, optional)
- A graphical desktop environment (this is a desktop GUI application)

## How to Run

### Option A - Maven (recommended)

```bash
cd DigitalDetective
mvn clean compile
mvn exec:java
```

or build a runnable jar:

```bash
mvn clean package
java -jar target/digital-detective.jar
```

Run the test suite:

```bash
mvn test
```

### Option B - Plain `javac` / `java` (no Maven required)

This project's main application code has **zero third-party runtime
dependencies** - only the JDK standard library - so it can be compiled and
run without Maven at all:

```bash
cd DigitalDetective
find src/main/java -name "*.java" > sources.txt
javac -d out @sources.txt
java -cp out com.digitaldetective.Main
```

(JUnit is only required if you want to compile/run the tests in
`src/test/java`; the application itself does not need it.)

### Option C - VS Code

1. Open the `DigitalDetective` folder in VS Code.
2. Install the **"Extension Pack for Java"** extension if prompted (or
   from the Extensions panel) - it bundles Maven support, a test runner,
   and run/debug buttons.
3. Open `src/main/java/com/digitaldetective/Main.java`.
4. Click the **Run** button that appears above the `main` method (or press
   `F5`).
5. VS Code will compile and launch the application automatically.

If your VS Code Java extension doesn't pick up the Maven project
automatically, run **"Java: Clean Java Language Server Workspace"** from
the Command Palette, or simply use Option B above - it needs nothing but
the JDK.

## How to Use

1. **Launch** the app - you'll see the Digital Detective welcome screen.
2. Click **START INVESTIGATION** to open the dashboard.
3. Either:
   - Click **Select Folder**, choose a directory, then **Start Scan**, or
   - Click **Demo Mode** to instantly load a synthetic sample investigation.
4. Explore the sidebar sections: **Overview, Files, Duplicates,
   Suspicious, Timeline, Storage, Hash Explorer, Reports, History**.
5. Double-click any file row anywhere in the app to open its full **File
   Details** window (copy path, copy hash, open containing folder).
6. Go to **Reports** to generate an HTML forensic report, export the full
   file inventory as CSV, export a plain-text summary, or save
   investigation notes.
7. Previously completed scans are saved automatically and can be revisited
   under **History**.

See the **[5-minute presentation script](#example-demo-flow)** below for a
ready-made walkthrough for a college presentation.

## Project Architecture

See **[ARCHITECTURE.md](ARCHITECTURE.md)** for:

- Mermaid architecture/class/sequence/activity/data-flow diagrams
- Class responsibilities
- The scanning pipeline (NIO walk + thread pool hashing)
- The duplicate-detection algorithm (size-then-hash grouping)
- The suspicious-file scoring algorithm
- The report-generation process

See **[VIVA.md](VIVA.md)** for 30+ likely viva questions with concise
answers, covering OOP, collections, multithreading, SHA-256, NIO,
exception handling, hash maps, duplicate detection, time complexity,
security, GUI threading, and CSV/HTML generation.

## Folder Structure

```
DigitalDetective/
├── src/
│   ├── main/java/com/digitaldetective/
│   │   ├── Main.java                     - application entry point
│   │   ├── model/                        - FileRecord, FileType, Severity,
│   │   │                                    SuspiciousFinding, Investigation
│   │   ├── scanner/                      - FileScanner, HashCalculator,
│   │   │                                    MetadataExtractor
│   │   ├── analyzer/                     - DuplicateAnalyzer, SuspiciousAnalyzer,
│   │   │                                    TimelineAnalyzer, StorageAnalyzer,
│   │   │                                    ForensicScoreCalculator,
│   │   │                                    InvestigationAnalyzer (orchestrator)
│   │   ├── report/                       - HtmlReportGenerator, CsvExporter,
│   │   │                                    InvestigationHistoryManager
│   │   ├── ui/                           - WelcomeScreen, MainFrame, Theme, and
│   │   │                                    one panel per dashboard section
│   │   └── util/                         - Constants, FileUtils, DateUtils,
│   │                                        DemoDataGenerator
│   └── test/java/com/digitaldetective/   - JUnit 5 tests
├── sample/test-data/                     - instructions for building practice data
├── reports/                              - (created at runtime) sample report output
├── pom.xml
├── README.md
├── ARCHITECTURE.md
└── VIVA.md
```

## Screenshots

*(Add screenshots here after running the app - e.g.
`docs/screenshots/dashboard.png`, `docs/screenshots/duplicates.png`,
`docs/screenshots/report.png`.)*

| Screen | Screenshot |
|---|---|
| Welcome Screen | `docs/screenshots/welcome.png` |
| Dashboard / Overview | `docs/screenshots/dashboard.png` |
| File Explorer | `docs/screenshots/files.png` |
| Duplicates | `docs/screenshots/duplicates.png` |
| Suspicious Findings | `docs/screenshots/suspicious.png` |
| Storage Analytics | `docs/screenshots/storage.png` |
| HTML Report | `docs/screenshots/report.png` |

## Limitations

- **Heuristic, not a security product.** The suspicious-file analyzer and
  File Health Score are transparent, rule-based heuristics intended for
  educational review - they do not inspect file contents for actual
  malicious code and must never be treated as a malware verdict.
- **Progress bar is indeterminate.** Because computing an exact total file
  count ahead of time would require a full extra directory pass, the scan
  progress dialog shows a live "files scanned" counter and current file
  rather than an exact percentage.
- **No database.** Investigation history is stored as local CSV + HTML
  snapshots rather than a queryable database - intentional, per the
  project brief, but it means history search is limited.
- **Windows-specific metadata** (e.g. the DOS read-only attribute) falls
  back to POSIX-style checks on macOS/Linux, so "Read Only" detection is
  best-effort cross-platform.
- **Very large trees** (millions of files) will use proportionally more
  memory, since the current file inventory is kept in memory for
  interactivity; see Future Enhancements for a streaming alternative.

## Future Enhancements

- Optional two-pass scanning to show an exact percentage-based progress bar.
- Pluggable "magic byte" content sniffing to catch mismatched
  extension-vs-content files (e.g. a renamed `.exe` with a `.pdf`
  extension).
- Exportable/paginated history search and JSON-based history storage.
- A pluggable rules engine so suspicious-file heuristics can be tuned or
  extended without recompiling.
- Optional JavaFX build variant behind a Maven profile for those who do
  have the JavaFX SDK set up.

## Educational Disclaimer

**This tool is intended for educational and authorized forensic analysis
only.** It is a college project demonstrating Java file handling,
multithreading, hashing, and GUI development. It is not an antivirus
product, does not identify malware with certainty, and must never be used
to analyze systems or files without proper authorization. The application
never uploads, transmits, automatically deletes, modifies, or executes any
scanned file.

---

## Example Demo Flow

A ready-to-use **5-minute presentation script** for demonstrating this
project to a professor:

1. **Launch Digital Detective** - show the welcome screen and read the
   motto/disclaimer.
2. Click **START INVESTIGATION**, then click **Demo Mode** to instantly
   load a realistic synthetic dataset (no need to scan a real folder live).
3. Walk through the **Dashboard** - point out Files Scanned, Total Size,
   Duplicates, Suspicious Files, and the File Health Score.
4. Open **Files** and demonstrate the search box and category filters.
5. Open **Duplicates** and show a duplicate group with its shared SHA-256
   hash and wasted storage calculation.
6. Open **Suspicious** and show `invoice.pdf.exe` - explain the
   double-extension + executable + risk-score logic, and point out the
   "review manually" wording (never a certainty claim).
7. Open **Hash Explorer**, copy a hash from any file's details window, and
   search for it.
8. Open **Timeline** and demonstrate a quick range filter (e.g. "Last 7 Days").
9. Open **Storage** and show the pie/bar charts and Top 10 Largest Files.
10. Open **Reports**, click **Generate HTML Report**, and open the
    generated file to show the professional dark-themed report.
11. Briefly explain the architecture: NIO for scanning, `ExecutorService`
    for parallel hashing so the GUI never freezes, `MessageDigest` for
    SHA-256, and the size-then-hash duplicate algorithm.
12. Close by re-emphasizing: **local-first, no uploads, heuristic
    findings only, nothing is ever auto-deleted or auto-executed.**

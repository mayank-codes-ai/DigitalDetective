package com.digitaldetective.report;

import com.digitaldetective.analyzer.TimelineAnalyzer;
import com.digitaldetective.model.FileRecord;
import com.digitaldetective.model.FileType;
import com.digitaldetective.model.Investigation;
import com.digitaldetective.model.SuspiciousFinding;
import com.digitaldetective.util.Constants;
import com.digitaldetective.util.DateUtils;
import com.digitaldetective.util.FileUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

/**
 * Builds a self-contained, professionally styled HTML forensic report for
 * an {@link Investigation}. The report embeds its own CSS (dark,
 * cybersecurity-inspired theme) so the single .html file can be opened,
 * emailed, or archived without any external assets.
 */
public final class HtmlReportGenerator {

    private static final int MAX_TIMELINE_ROWS = 100;
    private static final int MAX_TABLE_ROWS = 500;

    private HtmlReportGenerator() {
    }

    public static void generate(Investigation investigation, Path outputFile) throws IOException {
        String html = buildHtml(investigation);
        Files.writeString(outputFile, html, StandardCharsets.UTF_8);
    }

    public static String buildHtml(Investigation investigation) {
        StringBuilder h = new StringBuilder();
        h.append("<!DOCTYPE html><html lang=\"en\"><head><meta charset=\"UTF-8\">");
        h.append("<title>Digital Detective - Forensic Report</title>");
        h.append("<style>").append(css()).append("</style></head><body>");

        h.append("<div class=\"container\">");
        header(h, investigation);
        summaryCards(h, investigation);
        notesSection(h, investigation);
        typeDistribution(h, investigation);
        topLargest(h, investigation);
        duplicates(h, investigation);
        suspicious(h, investigation);
        hiddenAndEmpty(h, investigation);
        timeline(h, investigation);
        recommendations(h, investigation);
        footer(h);
        h.append("</div></body></html>");
        return h.toString();
    }

    // ---- Sections -----------------------------------------------------------

    private static void header(StringBuilder h, Investigation inv) {
        h.append("<header class=\"report-header\">");
        h.append("<div class=\"badge\">DIGITAL FORENSICS SUMMARY</div>");
        h.append("<h1>").append(Constants.APP_NAME).append("</h1>");
        h.append("<p class=\"tagline\">").append(Constants.APP_TAGLINE).append("</p>");
        h.append("<table class=\"meta-table\">");
        metaRow(h, "Investigation Path", esc(inv.getInvestigationPath()));
        metaRow(h, "Scan Started", DateUtils.formatDateTime(inv.getScanStarted()));
        metaRow(h, "Scan Completed", DateUtils.formatDateTime(inv.getScanCompleted()));
        metaRow(h, "Mode", inv.isDemo() ? "Demo Mode (synthetic data)" : "Live Scan");
        h.append("</table></header>");
    }

    private static void metaRow(StringBuilder h, String label, String value) {
        h.append("<tr><td class=\"meta-label\">").append(label).append("</td><td>").append(value).append("</td></tr>");
    }

    private static void summaryCards(StringBuilder h, Investigation inv) {
        h.append("<section><h2>Scan Statistics</h2><div class=\"cards\">");
        card(h, "Files Scanned", String.format("%,d", inv.getFileCount()));
        card(h, "Total Size", FileUtils.formatSize(inv.getTotalSizeBytes()));
        card(h, "Directories", String.format("%,d", inv.getDirectoryCount()));
        card(h, "Duplicate Files", String.format("%,d", inv.getDuplicateFileCount()));
        card(h, "Potentially Suspicious", String.format("%,d", inv.getSuspiciousCount()));
        card(h, "Hidden Files", String.format("%,d", inv.getHiddenFiles().size()));
        card(h, "Empty Files", String.format("%,d", inv.getEmptyFiles().size()));
        card(h, "Executable Files", String.format("%,d", inv.getExecutableCount()));
        card(h, "File Health Score", inv.getForensicScore() + " / 100");
        h.append("</div>");
        h.append("<p class=\"disclaimer-inline\">File Health Score is a heuristic for educational analysis only - "
                + "it is <strong>not</strong> a malware or security verdict.</p>");
        h.append("</section>");
    }

    private static void card(StringBuilder h, String label, String value) {
        h.append("<div class=\"card\"><div class=\"card-value\">").append(esc(value))
                .append("</div><div class=\"card-label\">").append(esc(label)).append("</div></div>");
    }

    private static void notesSection(StringBuilder h, Investigation inv) {
        if (inv.getNotes() == null || inv.getNotes().isBlank()) {
            return;
        }
        h.append("<section><h2>Investigation Notes</h2><div class=\"notes-box\">")
                .append(esc(inv.getNotes()).replace("\n", "<br>"))
                .append("</div></section>");
    }

    private static void typeDistribution(StringBuilder h, Investigation inv) {
        h.append("<section><h2>File Type Distribution</h2>");
        h.append("<table class=\"data-table\"><tr><th>Category</th><th>Files</th><th>Total Size</th></tr>");
        Map<FileType, Long> counts = inv.getTypeCountDistribution();
        Map<FileType, Long> sizes = inv.getTypeSizeDistribution();
        for (FileType type : FileType.values()) {
            long count = counts.getOrDefault(type, 0L);
            if (count == 0) continue;
            long size = sizes.getOrDefault(type, 0L);
            h.append("<tr><td>").append(type.getDisplayName()).append("</td><td>")
                    .append(String.format("%,d", count)).append("</td><td>")
                    .append(FileUtils.formatSize(size)).append("</td></tr>");
        }
        h.append("</table></section>");
    }

    private static void topLargest(StringBuilder h, Investigation inv) {
        h.append("<section><h2>Top Largest Files</h2>");
        h.append("<table class=\"data-table\"><tr><th>#</th><th>Name</th><th>Path</th><th>Size</th></tr>");
        int i = 1;
        for (FileRecord f : inv.getTopLargestFiles()) {
            h.append("<tr><td>").append(i++).append("</td><td>").append(esc(f.getFileName()))
                    .append("</td><td class=\"path-cell\">").append(esc(f.getAbsolutePath()))
                    .append("</td><td>").append(f.getFormattedSize()).append("</td></tr>");
        }
        h.append("</table></section>");
    }

    private static void duplicates(StringBuilder h, Investigation inv) {
        h.append("<section><h2>Duplicate Files (").append(inv.getDuplicateGroups().size()).append(" groups)</h2>");
        h.append("<p class=\"muted\">Wasted storage from duplicates: ")
                .append(FileUtils.formatSize(inv.getWastedDuplicateBytes())).append("</p>");
        int groupNum = 1;
        for (List<FileRecord> group : inv.getDuplicateGroups()) {
            if (groupNum > 50) {
                h.append("<p class=\"muted\">... additional duplicate groups omitted from the report for brevity; "
                        + "see the CSV export for the full inventory.</p>");
                break;
            }
            h.append("<div class=\"finding-box\"><strong>Duplicate Group #").append(groupNum++).append("</strong> - ")
                    .append(group.size()).append(" identical files, SHA-256: <code>")
                    .append(esc(group.get(0).getSha256())).append("</code><ul>");
            for (FileRecord f : group) {
                h.append("<li class=\"path-cell\">").append(esc(f.getAbsolutePath())).append("</li>");
            }
            h.append("</ul></div>");
        }
        h.append("</section>");
    }

    private static void suspicious(StringBuilder h, Investigation inv) {
        h.append("<section><h2>Potentially Suspicious Files (").append(inv.getSuspiciousFindings().size())
                .append(")</h2>");
        h.append("<p class=\"muted\">These are heuristic indicators for manual review, not confirmed threats.</p>");
        int shown = 0;
        for (SuspiciousFinding f : inv.getSuspiciousFindings()) {
            if (shown++ >= MAX_TABLE_ROWS) {
                h.append("<p class=\"muted\">... additional findings omitted; see the CSV export.</p>");
                break;
            }
            String sevClass = "sev-" + f.getSeverity().name().toLowerCase();
            h.append("<div class=\"finding-box ").append(sevClass).append("\">");
            h.append("<span class=\"severity-badge ").append(sevClass).append("\">")
                    .append(f.getSeverity()).append("</span> ");
            h.append("<strong>").append(esc(f.getFileName())).append("</strong> - Risk Score: ")
                    .append(f.getScore()).append("/100");
            h.append("<div class=\"path-cell\">").append(esc(f.getFilePath())).append("</div>");
            h.append("<div><em>Reasons:</em> ").append(esc(f.getReasonsJoined())).append("</div>");
            h.append("<div><em>Recommendation:</em> ").append(esc(f.getRecommendation())).append("</div>");
            h.append("</div>");
        }
        h.append("</section>");
    }

    private static void hiddenAndEmpty(StringBuilder h, Investigation inv) {
        h.append("<section><h2>Hidden Files (").append(inv.getHiddenFiles().size()).append(")</h2>");
        h.append("<table class=\"data-table\"><tr><th>Name</th><th>Path</th><th>Size</th><th>Modified</th></tr>");
        int i = 0;
        for (FileRecord f : inv.getHiddenFiles()) {
            if (i++ >= 100) {
                h.append("<tr><td colspan=4 class=\"muted\">... additional hidden files omitted.</td></tr>");
                break;
            }
            h.append("<tr><td>").append(esc(f.getFileName())).append("</td><td class=\"path-cell\">")
                    .append(esc(f.getAbsolutePath())).append("</td><td>").append(f.getFormattedSize())
                    .append("</td><td>").append(f.getFormattedModified()).append("</td></tr>");
        }
        h.append("</table></section>");

        h.append("<section><h2>Empty Files (").append(inv.getEmptyFiles().size()).append(")</h2>");
        h.append("<p class=\"muted\">Zero-byte files found during the scan. Not automatically deleted.</p>");
        h.append("</section>");
    }

    private static void timeline(StringBuilder h, Investigation inv) {
        h.append("<section><h2>Recent File Activity Timeline</h2>");
        h.append("<table class=\"data-table\"><tr><th>Date</th><th>Event</th><th>File</th></tr>");
        List<TimelineAnalyzer.TimelineEvent> events = TimelineAnalyzer.buildTimeline(inv.getAllFiles());
        int i = 0;
        for (TimelineAnalyzer.TimelineEvent e : events) {
            if (i++ >= MAX_TIMELINE_ROWS) {
                h.append("<tr><td colspan=3 class=\"muted\">... additional events omitted for brevity.</td></tr>");
                break;
            }
            h.append("<tr><td>").append(DateUtils.formatDateTime(e.timestamp)).append("</td><td>")
                    .append(e.eventType).append("</td><td>").append(esc(e.file.getFileName())).append("</td></tr>");
        }
        h.append("</table></section>");
    }

    private static void recommendations(StringBuilder h, Investigation inv) {
        h.append("<section><h2>Recommendations</h2><ul class=\"recommend-list\">");
        if (inv.getSuspiciousCount() > 0) {
            h.append("<li>Manually review the ").append(inv.getSuspiciousCount())
                    .append(" file(s) flagged as potentially suspicious before opening them.</li>");
        }
        if (!inv.getDuplicateGroups().isEmpty()) {
            h.append("<li>Consider reviewing duplicate files to reclaim ")
                    .append(FileUtils.formatSize(inv.getWastedDuplicateBytes())).append(" of storage.</li>");
        }
        if (!inv.getEmptyFiles().isEmpty()) {
            h.append("<li>").append(inv.getEmptyFiles().size())
                    .append(" empty file(s) were found and may be safe to clean up after review.</li>");
        }
        if (inv.getHiddenFiles().size() > 0) {
            h.append("<li>").append(inv.getHiddenFiles().size())
                    .append(" hidden file(s) were found - confirm they are expected.</li>");
        }
        h.append("<li>This report is generated entirely offline. No file contents, hashes, or metadata were "
                + "transmitted anywhere.</li>");
        h.append("</ul></section>");
    }

    private static void footer(StringBuilder h) {
        h.append("<footer><p>Generated by ").append(Constants.APP_NAME).append(" v").append(Constants.APP_VERSION)
                .append(" — ").append(Constants.APP_MOTTO).append("</p>");
        h.append("<p class=\"disclaimer-inline\">This tool is intended for educational and authorized forensic "
                + "analysis only. It is not an antivirus product and does not identify malware with certainty.</p>");
        h.append("</footer>");
    }

    private static String esc(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
                .replace("\"", "&quot;").replace("'", "&#39;");
    }

    private static String css() {
        return "" +
                ":root{--bg:#0d1117;--panel:#161b22;--panel2:#1c2330;--text:#e6edf3;--muted:#8b949e;" +
                "--accent:#39d0ff;--accent2:#7ee787;--danger:#ff6b6b;--warn:#ffd166;--ok:#7ee787;--border:#30363d;}" +
                "*{box-sizing:border-box;}" +
                "body{background:var(--bg);color:var(--text);font-family:'Segoe UI',Roboto,Arial,sans-serif;margin:0;padding:0;}" +
                ".container{max-width:1100px;margin:0 auto;padding:32px 24px 64px;}" +
                ".report-header{border-bottom:1px solid var(--border);padding-bottom:24px;margin-bottom:24px;}" +
                ".badge{display:inline-block;background:var(--panel2);color:var(--accent);letter-spacing:2px;" +
                "font-size:12px;padding:4px 10px;border-radius:4px;border:1px solid var(--border);}" +
                "h1{font-size:32px;margin:12px 0 0;color:var(--accent);}" +
                ".tagline{color:var(--muted);margin-top:4px;}" +
                ".meta-table{margin-top:16px;border-collapse:collapse;width:100%;}" +
                ".meta-table td{padding:6px 10px;border-bottom:1px solid var(--border);font-size:14px;}" +
                ".meta-label{color:var(--muted);width:220px;}" +
                "section{margin-top:36px;}" +
                "h2{font-size:20px;border-left:4px solid var(--accent);padding-left:10px;color:var(--text);}" +
                ".cards{display:grid;grid-template-columns:repeat(auto-fit,minmax(160px,1fr));gap:12px;margin-top:16px;}" +
                ".card{background:var(--panel);border:1px solid var(--border);border-radius:8px;padding:16px;text-align:center;}" +
                ".card-value{font-size:24px;font-weight:700;color:var(--accent2);}" +
                ".card-label{font-size:12px;color:var(--muted);margin-top:6px;text-transform:uppercase;letter-spacing:1px;}" +
                ".disclaimer-inline{color:var(--muted);font-size:12px;margin-top:10px;}" +
                ".notes-box{background:var(--panel);border:1px solid var(--border);border-radius:8px;padding:16px;}" +
                "table.data-table{width:100%;border-collapse:collapse;margin-top:12px;font-size:13px;}" +
                ".data-table th{text-align:left;color:var(--muted);border-bottom:1px solid var(--border);padding:8px;" +
                "text-transform:uppercase;font-size:11px;letter-spacing:1px;}" +
                ".data-table td{padding:8px;border-bottom:1px solid var(--border);}" +
                ".path-cell{color:var(--muted);font-family:Consolas,monospace;font-size:12px;word-break:break-all;}" +
                ".finding-box{background:var(--panel);border:1px solid var(--border);border-left:4px solid var(--warn);" +
                "border-radius:6px;padding:14px;margin-top:10px;font-size:13px;}" +
                ".finding-box.sev-high{border-left-color:var(--danger);}" +
                ".finding-box.sev-medium{border-left-color:var(--warn);}" +
                ".finding-box.sev-low{border-left-color:var(--ok);}" +
                ".severity-badge{display:inline-block;font-size:11px;padding:2px 8px;border-radius:10px;font-weight:700;}" +
                ".severity-badge.sev-high{background:rgba(255,107,107,.15);color:var(--danger);}" +
                ".severity-badge.sev-medium{background:rgba(255,209,102,.15);color:var(--warn);}" +
                ".severity-badge.sev-low{background:rgba(126,231,135,.15);color:var(--ok);}" +
                ".muted{color:var(--muted);font-size:13px;}" +
                ".recommend-list li{margin:6px 0;font-size:14px;}" +
                "code{color:var(--accent);}" +
                "footer{margin-top:48px;border-top:1px solid var(--border);padding-top:16px;color:var(--muted);font-size:12px;}";
    }
}

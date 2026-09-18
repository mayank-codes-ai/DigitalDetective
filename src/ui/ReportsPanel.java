package com.digitaldetective.ui;

import com.digitaldetective.model.Investigation;
import com.digitaldetective.report.CsvExporter;
import com.digitaldetective.report.HtmlReportGenerator;
import com.digitaldetective.util.DateUtils;
import com.digitaldetective.util.FileUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.IOException;
import java.nio.file.Path;
import java.time.format.DateTimeFormatter;

/**
 * The "Reports" section: generate the professional HTML forensic report,
 * export the full file inventory as CSV, export a plain-text investigation
 * summary, and manage investigator notes (which are embedded into the HTML
 * report).
 */
public class ReportsPanel extends JPanel {

    private final MainFrame owner;
    private Investigation investigation = new Investigation();
    private final JTextArea notesArea = new JTextArea();
    private final JLabel statusLabel = Theme.muted(" ");

    public ReportsPanel(MainFrame owner) {
        this.owner = owner;
        setOpaque(false);
        setLayout(new BorderLayout());
        setBorder(Theme.sectionPadding());
    }

    public void refresh(Investigation investigation) {
        this.investigation = investigation;
        removeAll();

        JPanel column = new JPanel();
        column.setOpaque(false);
        column.setLayout(new BoxLayout(column, BoxLayout.Y_AXIS));

        JLabel title = Theme.heading("Reports");
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel subtitle = Theme.muted("Generate a professional forensic report, or export the raw data for "
                + "further analysis. Everything is generated locally - nothing is uploaded.");
        subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        subtitle.setBorder(new EmptyBorder(2, 0, 18, 0));

        column.add(title);
        column.add(subtitle);
        column.add(buildSummaryCard());
        column.add(Box.createVerticalStrut(16));
        column.add(buildActionsCard());
        column.add(Box.createVerticalStrut(16));
        column.add(buildNotesCard());
        column.add(Box.createVerticalStrut(10));

        statusLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        column.add(statusLabel);

        add(Theme.scrollable(column), BorderLayout.CENTER);
        revalidate();
        repaint();
    }

    private JPanel buildSummaryCard() {
        JPanel card = Theme.card();
        card.setLayout(new GridLayout(0, 2, 8, 4));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.setMaximumSize(new Dimension(2000, 220));

        addSummaryRow(card, "Investigation Path", investigation.getInvestigationPath());
        addSummaryRow(card, "Scan Started", DateUtils.formatDateTime(investigation.getScanStarted()));
        addSummaryRow(card, "Scan Completed", DateUtils.formatDateTime(investigation.getScanCompleted()));
        addSummaryRow(card, "Files Scanned", String.format("%,d", investigation.getFileCount()));
        addSummaryRow(card, "Total Size", FileUtils.formatSize(investigation.getTotalSizeBytes()));
        addSummaryRow(card, "Directories", String.format("%,d", investigation.getDirectoryCount()));
        addSummaryRow(card, "Duplicate Files", String.format("%,d", investigation.getDuplicateFileCount()));
        addSummaryRow(card, "Potentially Suspicious", String.format("%,d", investigation.getSuspiciousCount()));
        addSummaryRow(card, "Hidden Files", String.format("%,d", investigation.getHiddenFiles().size()));
        addSummaryRow(card, "Empty Files", String.format("%,d", investigation.getEmptyFiles().size()));
        addSummaryRow(card, "Executable Files", String.format("%,d", investigation.getExecutableCount()));
        addSummaryRow(card, "Largest File", investigation.getLargestFile() == null ? "N/A"
                : investigation.getLargestFile().getFileName());

        return card;
    }

    private void addSummaryRow(JPanel card, String label, String value) {
        JLabel l = new JLabel(label);
        l.setFont(Theme.FONT_SECTION);
        l.setForeground(Theme.TEXT_MUTED);
        JLabel v = new JLabel(value == null ? "N/A" : value);
        v.setFont(Theme.FONT_BODY);
        v.setForeground(Theme.TEXT);
        card.add(l);
        card.add(v);
    }

    private JPanel buildActionsCard() {
        JPanel card = Theme.card();
        card.setLayout(new FlowLayout(FlowLayout.LEFT, 12, 8));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.setMaximumSize(new Dimension(2000, 80));

        JButton htmlButton = Theme.primaryButton("Generate HTML Report");
        htmlButton.addActionListener(e -> generateHtmlReport());

        JButton csvButton = Theme.secondaryButton("Export CSV");
        csvButton.addActionListener(e -> exportCsv());

        JButton summaryButton = Theme.secondaryButton("Export Investigation Summary");
        summaryButton.addActionListener(e -> exportSummary());

        card.add(htmlButton);
        card.add(csvButton);
        card.add(summaryButton);
        return card;
    }

    private JPanel buildNotesCard() {
        JPanel card = Theme.card();
        card.setLayout(new BorderLayout(0, 10));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.setMaximumSize(new Dimension(2000, 220));

        JLabel title = new JLabel("Investigation Notes");
        title.setFont(Theme.FONT_SECTION);
        title.setForeground(Theme.TEXT_MUTED);

        notesArea.setText(investigation.getNotes());
        notesArea.setLineWrap(true);
        notesArea.setWrapStyleWord(true);
        notesArea.setBackground(Theme.PANEL_LIGHT);
        notesArea.setForeground(Theme.TEXT);
        notesArea.setCaretColor(Theme.TEXT);
        notesArea.setFont(Theme.FONT_BODY);
        notesArea.setBorder(new EmptyBorder(10, 10, 10, 10));
        JScrollPane notesScroll = new JScrollPane(notesArea);
        notesScroll.setPreferredSize(new Dimension(100, 100));

        JButton saveNotes = Theme.secondaryButton("Save Notes");
        saveNotes.addActionListener(e -> {
            investigation.setNotes(notesArea.getText());
            statusLabel.setText("Notes saved. They will be included in the next generated HTML report.");
        });

        JPanel notesButtons = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        notesButtons.setOpaque(false);
        notesButtons.add(saveNotes);

        card.add(title, BorderLayout.NORTH);
        card.add(notesScroll, BorderLayout.CENTER);
        card.add(notesButtons, BorderLayout.SOUTH);
        return card;
    }

    // ---- Actions --------------------------------------------------------------

    private void generateHtmlReport() {
        Path target = chooseSaveLocation("digital_detective_report_" + timestamp() + ".html", "HTML Report (*.html)");
        if (target == null) return;
        try {
            HtmlReportGenerator.generate(investigation, target);
            statusLabel.setText("HTML report saved to: " + target);
            offerToOpen(target);
        } catch (IOException ex) {
            statusLabel.setText("Failed to generate HTML report: " + ex.getMessage());
        }
    }

    private void exportCsv() {
        Path target = chooseSaveLocation("digital_detective_inventory_" + timestamp() + ".csv", "CSV File (*.csv)");
        if (target == null) return;
        try {
            CsvExporter.export(investigation, target);
            statusLabel.setText("CSV export saved to: " + target);
            offerToOpen(target);
        } catch (IOException ex) {
            statusLabel.setText("Failed to export CSV: " + ex.getMessage());
        }
    }

    private void exportSummary() {
        Path target = chooseSaveLocation("digital_detective_summary_" + timestamp() + ".txt", "Text Summary (*.txt)");
        if (target == null) return;
        try {
            String summary = buildPlainTextSummary();
            java.nio.file.Files.writeString(target, summary);
            statusLabel.setText("Investigation summary saved to: " + target);
            offerToOpen(target);
        } catch (IOException ex) {
            statusLabel.setText("Failed to export summary: " + ex.getMessage());
        }
    }

    private String buildPlainTextSummary() {
        StringBuilder sb = new StringBuilder();
        sb.append("DIGITAL FORENSICS SUMMARY\n");
        sb.append("==========================\n\n");
        sb.append("Investigation Path: ").append(investigation.getInvestigationPath()).append('\n');
        sb.append("Scan Started: ").append(DateUtils.formatDateTime(investigation.getScanStarted())).append('\n');
        sb.append("Scan Completed: ").append(DateUtils.formatDateTime(investigation.getScanCompleted())).append('\n');
        sb.append('\n');
        sb.append("Files Scanned: ").append(investigation.getFileCount()).append('\n');
        sb.append("Total Size: ").append(FileUtils.formatSize(investigation.getTotalSizeBytes())).append('\n');
        sb.append("Directories: ").append(investigation.getDirectoryCount()).append('\n');
        sb.append("Duplicate Files: ").append(investigation.getDuplicateFileCount()).append('\n');
        sb.append("Potentially Suspicious: ").append(investigation.getSuspiciousCount()).append('\n');
        sb.append("Hidden: ").append(investigation.getHiddenFiles().size()).append('\n');
        sb.append("Empty: ").append(investigation.getEmptyFiles().size()).append('\n');
        sb.append("Executable: ").append(investigation.getExecutableCount()).append('\n');
        sb.append("Largest File: ").append(investigation.getLargestFile() == null ? "N/A"
                : investigation.getLargestFile().getFileName()).append('\n');
        sb.append("File Health Score: ").append(investigation.getForensicScore()).append("/100\n");
        sb.append('\n');
        if (!investigation.getNotes().isBlank()) {
            sb.append("Investigator Notes:\n").append(investigation.getNotes()).append("\n\n");
        }
        sb.append("Generated by Digital Detective - for educational and authorized forensic analysis only.\n");
        return sb.toString();
    }

    private Path chooseSaveLocation(String suggestedName, String description) {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Save Report");
        chooser.setSelectedFile(new java.io.File(suggestedName));
        int result = chooser.showSaveDialog(this);
        if (result != JFileChooser.APPROVE_OPTION) {
            return null;
        }
        return chooser.getSelectedFile().toPath();
    }

    private void offerToOpen(Path file) {
        int choice = JOptionPane.showConfirmDialog(this,
                "Saved successfully. Open it now?", "Report Generated", JOptionPane.YES_NO_OPTION);
        if (choice == JOptionPane.YES_OPTION) {
            try {
                if (Desktop.isDesktopSupported()) {
                    Desktop.getDesktop().open(file.toFile());
                }
            } catch (IOException ignored) {
                // best effort only
            }
        }
    }

    private String timestamp() {
        return DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")
                .format(java.time.LocalDateTime.now());
    }
}

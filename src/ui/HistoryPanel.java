package com.digitaldetective.ui;

import com.digitaldetective.report.InvestigationHistoryManager;
import com.digitaldetective.util.DateUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.Desktop;
import java.io.IOException;
import java.util.List;

/**
 * The "History" section: previous investigation summaries, persisted
 * locally (no database, no cloud) as described in the project brief. Each
 * entry can be re-opened as its saved HTML report snapshot.
 */
public class HistoryPanel extends JPanel {

    private final MainFrame owner;

    public HistoryPanel(MainFrame owner) {
        this.owner = owner;
        setOpaque(false);
        setLayout(new BorderLayout());
        setBorder(Theme.sectionPadding());
        refresh();
    }

    public void refresh() {
        removeAll();

        JPanel column = new JPanel();
        column.setOpaque(false);
        column.setLayout(new BoxLayout(column, BoxLayout.Y_AXIS));

        JLabel title = Theme.heading("Previous Investigations");
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        List<InvestigationHistoryManager.HistoryEntry> entries = InvestigationHistoryManager.loadHistory();

        JLabel subtitle = Theme.muted(entries.isEmpty()
                ? "No previous investigations yet. Completed scans are saved automatically."
                : String.format("%d saved investigation(s), stored locally at: %s",
                entries.size(), InvestigationHistoryManager.getDataFolder()));
        subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        subtitle.setBorder(new EmptyBorder(2, 0, 16, 0));

        column.add(title);
        column.add(subtitle);

        int number = entries.size();
        for (InvestigationHistoryManager.HistoryEntry entry : entries) {
            column.add(buildEntryCard(number--, entry));
            column.add(Box.createVerticalStrut(10));
        }

        add(Theme.scrollable(column), BorderLayout.CENTER);
        revalidate();
        repaint();
    }

    private JPanel buildEntryCard(int number, InvestigationHistoryManager.HistoryEntry entry) {
        JPanel card = Theme.card();
        card.setLayout(new BorderLayout(10, 0));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.setMaximumSize(new Dimension(2000, 100));

        JPanel info = new JPanel();
        info.setOpaque(false);
        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));

        JLabel heading = new JLabel("Investigation #" + number + (entry.demo ? "  (Demo Mode)" : ""));
        heading.setFont(Theme.FONT_SECTION);
        heading.setForeground(Theme.ACCENT);

        JLabel pathLabel = new JLabel(entry.path);
        pathLabel.setFont(Theme.FONT_MONO);
        pathLabel.setForeground(Theme.TEXT_MUTED);

        JLabel statsLabel = new JLabel(String.format("%s   •   %,d files   •   %s   •   %,d duplicates   •   %,d suspicious",
                DateUtils.formatDate(entry.timestamp), entry.fileCount, entry.getFormattedSize(),
                entry.duplicateCount, entry.suspiciousCount));
        statsLabel.setFont(Theme.FONT_BODY);
        statsLabel.setForeground(Theme.TEXT);

        info.add(heading);
        info.add(pathLabel);
        info.add(statsLabel);

        JButton openButton = Theme.secondaryButton("Open Report");
        openButton.addActionListener(e -> openReport(entry));

        card.add(info, BorderLayout.CENTER);
        card.add(openButton, BorderLayout.EAST);
        return card;
    }

    private void openReport(InvestigationHistoryManager.HistoryEntry entry) {
        try {
            var reportFile = InvestigationHistoryManager.getReportsFolder().resolve(entry.reportFileName);
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().open(reportFile.toFile());
            } else {
                JOptionPane.showMessageDialog(this, "Report saved at: " + reportFile);
            }
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Could not open the report: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}

package com.digitaldetective.ui;

import com.digitaldetective.analyzer.ForensicScoreCalculator;
import com.digitaldetective.analyzer.StorageAnalyzer;
import com.digitaldetective.model.FileRecord;
import com.digitaldetective.model.FileType;
import com.digitaldetective.model.Investigation;
import com.digitaldetective.util.FileUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.Map;

/**
 * The default landing screen: the "Investigation Dashboard" described in
 * the project brief - headline statistics, quick insight cards, the file
 * health score, and the Quick Investigation workflow strip.
 */
public class OverviewPanel extends JPanel {

    private final MainFrame owner;

    public OverviewPanel(MainFrame owner) {
        this.owner = owner;
        setOpaque(false);
        setLayout(new BorderLayout());
        setBorder(Theme.sectionPadding());
    }

    public void refresh(Investigation inv) {
        removeAll();

        JPanel column = new JPanel();
        column.setOpaque(false);
        column.setLayout(new BoxLayout(column, BoxLayout.Y_AXIS));

        JLabel title = Theme.heading("Investigation Dashboard");
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel subtitle = Theme.muted(inv.isDemo()
                ? "Showing synthetic Demo Mode data."
                : (inv.getFileCount() == 0
                ? "Select a folder and start a scan, or load Demo Mode, to begin."
                : "Investigation of: " + inv.getInvestigationPath()));
        subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        subtitle.setBorder(new EmptyBorder(2, 0, 18, 0));

        column.add(title);
        column.add(subtitle);
        column.add(buildQuickWorkflow());
        column.add(Box.createVerticalStrut(20));
        column.add(buildStatCards(inv));
        column.add(Box.createVerticalStrut(24));
        column.add(buildScoreAndInsights(inv));

        add(Theme.scrollable(column), BorderLayout.CENTER);
        revalidate();
        repaint();
    }

    private JComponent buildQuickWorkflow() {
        JPanel row = new JPanel(new GridLayout(1, 5, 10, 0));
        row.setOpaque(false);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(2000, 60));
        String[] steps = {"1. Select Folder", "2. Scan", "3. Analyze", "4. Review Findings", "5. Generate Report"};
        for (String step : steps) {
            JLabel l = new JLabel(step, SwingConstants.CENTER);
            l.setOpaque(true);
            l.setBackground(Theme.PANEL_LIGHT);
            l.setForeground(Theme.ACCENT);
            l.setFont(Theme.FONT_SECTION);
            l.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(Theme.BORDER), new EmptyBorder(10, 6, 10, 6)));
            row.add(l);
        }
        return row;
    }

    private JComponent buildStatCards(Investigation inv) {
        JPanel grid = new JPanel(new GridLayout(1, 5, 12, 0));
        grid.setOpaque(false);
        grid.setAlignmentX(Component.LEFT_ALIGNMENT);
        grid.setMaximumSize(new Dimension(2000, 110));

        grid.add(statCard("Files Scanned", String.format("%,d", inv.getFileCount()), Theme.ACCENT));
        grid.add(statCard("Total Size", FileUtils.formatSize(inv.getTotalSizeBytes()), Theme.ACCENT));
        grid.add(statCard("Directories", String.format("%,d", inv.getDirectoryCount()), Theme.ACCENT));
        grid.add(statCard("Duplicates", String.format("%,d", inv.getDuplicateFileCount()), Theme.WARNING));
        grid.add(statCard("Suspicious Files", String.format("%,d", inv.getSuspiciousCount()), Theme.DANGER));
        return grid;
    }

    private JPanel statCard(String label, String value, Color accent) {
        JPanel card = Theme.card();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        JLabel v = new JLabel(value);
        v.setFont(Theme.FONT_CARD_VALUE);
        v.setForeground(accent);
        JLabel l = new JLabel(label.toUpperCase());
        l.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        l.setForeground(Theme.TEXT_MUTED);
        l.setBorder(new EmptyBorder(6, 0, 0, 0));
        card.add(v);
        card.add(l);
        return card;
    }

    private JComponent buildScoreAndInsights(Investigation inv) {
        JPanel row = new JPanel(new GridLayout(1, 2, 16, 0));
        row.setOpaque(false);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(2000, 260));

        row.add(buildScorePanel(inv));
        row.add(buildInsightsPanel(inv));
        return row;
    }

    private JPanel buildScorePanel(Investigation inv) {
        JPanel p = Theme.card();
        p.setLayout(new BorderLayout());
        JLabel title = new JLabel("INVESTIGATION OVERVIEW");
        title.setFont(Theme.FONT_SECTION);
        title.setForeground(Theme.TEXT_MUTED);

        int score = inv.getForensicScore();
        Color color = score >= 75 ? Theme.ACCENT_GREEN : score >= 45 ? Theme.WARNING : Theme.DANGER;

        JLabel scoreLabel = new JLabel(score + " / 100", SwingConstants.CENTER);
        scoreLabel.setFont(new Font("Segoe UI", Font.BOLD, 48));
        scoreLabel.setForeground(color);

        JLabel caption = new JLabel("File Health Score", SwingConstants.CENTER);
        caption.setFont(Theme.FONT_BODY);
        caption.setForeground(Theme.TEXT_MUTED);

        JTextArea note = new JTextArea(ForensicScoreCalculator.explain(inv));
        note.setEditable(false);
        note.setFocusable(false);
        note.setOpaque(false);
        note.setLineWrap(true);
        note.setWrapStyleWord(true);
        note.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        note.setForeground(Theme.TEXT_MUTED);
        note.setBorder(new EmptyBorder(10, 0, 0, 0));

        JPanel center = new JPanel();
        center.setOpaque(false);
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        scoreLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        caption.setAlignmentX(Component.CENTER_ALIGNMENT);
        center.add(Box.createVerticalGlue());
        center.add(scoreLabel);
        center.add(caption);
        center.add(Box.createVerticalGlue());

        p.add(title, BorderLayout.NORTH);
        p.add(center, BorderLayout.CENTER);
        p.add(note, BorderLayout.SOUTH);
        return p;
    }

    private JPanel buildInsightsPanel(Investigation inv) {
        JPanel p = Theme.card();
        p.setLayout(new GridLayout(2, 2, 10, 10));

        FileRecord largest = inv.getLargestFile();
        Map<FileType, Long> counts = inv.getTypeCountDistribution();
        FileType mostCommon = StorageAnalyzer.mostCommonType(counts);
        long mostCommonCount = counts.getOrDefault(mostCommon, 0L);

        p.add(insightTile("Largest File",
                largest == null ? "N/A" : largest.getFileName(),
                largest == null ? "" : largest.getFormattedSize()));
        p.add(insightTile("Most Common Type",
                mostCommon == null ? "N/A" : mostCommon.getDisplayName(),
                mostCommonCount + " files"));
        p.add(insightTile("Recently Modified", String.valueOf(inv.getRecentlyModifiedFiles().size()),
                "in the last 7 days"));
        p.add(insightTile("Hidden Files", String.valueOf(inv.getHiddenFiles().size()), "detected"));

        return p;
    }

    private JPanel insightTile(String label, String value, String caption) {
        JPanel p = new JPanel();
        p.setOpaque(false);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        JLabel l = new JLabel(label.toUpperCase());
        l.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        l.setForeground(Theme.TEXT_MUTED);
        JLabel v = new JLabel(value);
        v.setFont(new Font("Segoe UI", Font.BOLD, 15));
        v.setForeground(Theme.TEXT);
        JLabel c = new JLabel(caption);
        c.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        c.setForeground(Theme.ACCENT);
        p.add(l);
        p.add(v);
        p.add(c);
        return p;
    }
}

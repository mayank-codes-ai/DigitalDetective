package com.digitaldetective.ui;

import com.digitaldetective.model.FileRecord;
import com.digitaldetective.model.Investigation;
import com.digitaldetective.model.SuspiciousFinding;
import com.digitaldetective.util.FileUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;
import java.util.Map;

/**
 * The "Suspicious" section: every potentially suspicious file, its risk
 * score, severity, reasons, and a manual-review recommendation. This tool
 * never claims a file is definitely malicious.
 */
public class SuspiciousPanel extends JPanel {

    private final MainFrame owner;
    private Investigation investigation = new Investigation();

    public SuspiciousPanel(MainFrame owner) {
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

        JLabel title = Theme.heading("Potentially Suspicious Files");
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        List<SuspiciousFinding> findings = investigation.getSuspiciousFindings();
        JLabel subtitle = Theme.muted(findings.isEmpty()
                ? "No files were flagged. Remember: this is a heuristic review aid, not a malware scanner."
                : String.format("%,d file(s) flagged for manual review. These are heuristic indicators only.",
                findings.size()));
        subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        subtitle.setBorder(new EmptyBorder(2, 0, 16, 0));

        column.add(title);
        column.add(subtitle);

        if (!findings.isEmpty()) {
            column.add(buildSeveritySummary(findings));
            column.add(Box.createVerticalStrut(14));
        }

        for (SuspiciousFinding finding : findings) {
            column.add(buildFindingCard(finding));
            column.add(Box.createVerticalStrut(10));
        }

        add(Theme.scrollable(column), BorderLayout.CENTER);
        revalidate();
        repaint();
    }

    private JComponent buildSeveritySummary(List<SuspiciousFinding> findings) {
        int high = 0, medium = 0, low = 0;
        for (SuspiciousFinding f : findings) {
            switch (f.getSeverity()) {
                case HIGH: high++; break;
                case MEDIUM: medium++; break;
                default: low++;
            }
        }
        JPanel row = new JPanel(new GridLayout(1, 3, 10, 0));
        row.setOpaque(false);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(2000, 70));
        row.add(severityTile("HIGH", high, Theme.DANGER));
        row.add(severityTile("MEDIUM", medium, Theme.WARNING));
        row.add(severityTile("LOW", low, Theme.ACCENT_GREEN));
        return row;
    }

    private JPanel severityTile(String label, int count, Color color) {
        JPanel p = Theme.card();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        JLabel v = new JLabel(String.valueOf(count));
        v.setFont(new Font("Segoe UI", Font.BOLD, 22));
        v.setForeground(color);
        JLabel l = new JLabel(label);
        l.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        l.setForeground(Theme.TEXT_MUTED);
        p.add(v);
        p.add(l);
        return p;
    }

    private JPanel buildFindingCard(SuspiciousFinding finding) {
        Color severityColor = Theme.severityColor(finding.getSeverity().name());

        JPanel card = new JPanel(new BorderLayout(0, 6));
        card.setBackground(Theme.PANEL);
        card.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.setMaximumSize(new Dimension(2000, 160));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 4, 0, 0, severityColor),
                new EmptyBorder(12, 14, 12, 14)));

        JPanel headerRow = new JPanel(new BorderLayout());
        headerRow.setOpaque(false);

        JLabel badge = new JLabel(finding.getSeverity().name());
        badge.setOpaque(true);
        badge.setBackground(severityColor);
        badge.setForeground(Theme.BACKGROUND);
        badge.setFont(new Font("Segoe UI", Font.BOLD, 11));
        badge.setBorder(new EmptyBorder(3, 8, 3, 8));

        JLabel fileName = new JLabel("  " + finding.getFileName() + "   —   Risk Score: " + finding.getScore() + "/100");
        fileName.setFont(Theme.FONT_SECTION);
        fileName.setForeground(Theme.TEXT);

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        left.setOpaque(false);
        left.add(badge);
        left.add(fileName);

        JButton details = new JButton("View File Details");
        details.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        details.setFocusPainted(false);
        details.setBackground(Theme.PANEL_LIGHT);
        details.setForeground(Theme.ACCENT);
        details.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        headerRow.add(left, BorderLayout.WEST);
        headerRow.add(details, BorderLayout.EAST);

        JLabel pathLabel = new JLabel(finding.getFilePath());
        pathLabel.setFont(Theme.FONT_MONO);
        pathLabel.setForeground(Theme.TEXT_MUTED);

        JTextArea reasons = new JTextArea("Reasons: " + finding.getReasonsJoined());
        styleNote(reasons, Theme.TEXT);

        JTextArea recommendation = new JTextArea("Recommendation: " + finding.getRecommendation());
        styleNote(recommendation, Theme.ACCENT_GREEN);

        JPanel body = new JPanel();
        body.setOpaque(false);
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.add(pathLabel);
        body.add(Box.createVerticalStrut(6));
        body.add(reasons);
        body.add(recommendation);

        card.add(headerRow, BorderLayout.NORTH);
        card.add(body, BorderLayout.CENTER);

        details.addActionListener(e -> {
            FileRecord record = findRecord(finding.getFilePath());
            if (record != null) {
                owner.openFileDetails(record);
            }
        });

        return card;
    }

    private void styleNote(JTextArea area, Color color) {
        area.setEditable(false);
        area.setFocusable(false);
        area.setOpaque(false);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        area.setForeground(color);
        area.setAlignmentX(Component.LEFT_ALIGNMENT);
    }

    private FileRecord findRecord(String absolutePath) {
        for (FileRecord f : investigation.getAllFiles()) {
            if (f.getAbsolutePath() != null && f.getAbsolutePath().equals(absolutePath)) {
                return f;
            }
        }
        return null;
    }
}

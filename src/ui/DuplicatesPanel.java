package com.digitaldetective.ui;

import com.digitaldetective.analyzer.DuplicateAnalyzer;
import com.digitaldetective.model.FileRecord;
import com.digitaldetective.model.Investigation;
import com.digitaldetective.util.FileUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;

/**
 * The "Duplicates" section: one card per duplicate group, each listing the
 * identical files and offering quick "Open Location" / "Copy Path" actions.
 * Nothing is ever deleted automatically.
 */
public class DuplicatesPanel extends JPanel {

    private final MainFrame owner;

    public DuplicatesPanel(MainFrame owner) {
        this.owner = owner;
        setOpaque(false);
        setLayout(new BorderLayout());
        setBorder(Theme.sectionPadding());
    }

    public void refresh(Investigation investigation) {
        removeAll();

        JPanel column = new JPanel();
        column.setOpaque(false);
        column.setLayout(new BoxLayout(column, BoxLayout.Y_AXIS));

        JLabel title = Theme.heading("Duplicate Files");
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        List<List<FileRecord>> groups = investigation.getDuplicateGroups();
        JLabel subtitle = Theme.muted(groups.isEmpty()
                ? "No duplicate files detected."
                : String.format("%,d duplicate group(s) found - %s of storage could potentially be reclaimed.",
                groups.size(), FileUtils.formatSize(investigation.getWastedDuplicateBytes())));
        subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        subtitle.setBorder(new EmptyBorder(2, 0, 16, 0));

        column.add(title);
        column.add(subtitle);

        int groupNumber = 1;
        for (List<FileRecord> group : groups) {
            column.add(buildGroupCard(groupNumber++, group));
            column.add(Box.createVerticalStrut(12));
        }

        add(Theme.scrollable(column), BorderLayout.CENTER);
        revalidate();
        repaint();
    }

    private JPanel buildGroupCard(int number, List<FileRecord> group) {
        JPanel card = Theme.card();
        card.setLayout(new BorderLayout(0, 10));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.setMaximumSize(new Dimension(2000, 40 + group.size() * 34));

        FileRecord first = group.get(0);
        JLabel headline = new JLabel(String.format("Duplicate Group #%d — %d identical files — Wasted storage: %s",
                number, group.size(), FileUtils.formatSize(DuplicateAnalyzer.wastedBytes(group))));
        headline.setFont(Theme.FONT_SECTION);
        headline.setForeground(Theme.WARNING);

        JLabel hashLabel = new JLabel("SHA-256: " + first.getSha256());
        hashLabel.setFont(Theme.FONT_MONO);
        hashLabel.setForeground(Theme.TEXT_MUTED);

        JPanel headerBox = new JPanel();
        headerBox.setOpaque(false);
        headerBox.setLayout(new BoxLayout(headerBox, BoxLayout.Y_AXIS));
        headline.setAlignmentX(Component.LEFT_ALIGNMENT);
        hashLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        headerBox.add(headline);
        headerBox.add(hashLabel);

        JPanel filesBox = new JPanel();
        filesBox.setOpaque(false);
        filesBox.setLayout(new BoxLayout(filesBox, BoxLayout.Y_AXIS));

        int i = 1;
        for (FileRecord f : group) {
            filesBox.add(buildFileRow(i++, f));
        }

        card.add(headerBox, BorderLayout.NORTH);
        card.add(filesBox, BorderLayout.CENTER);
        return card;
    }

    private JComponent buildFileRow(int index, FileRecord f) {
        JPanel row = new JPanel(new BorderLayout(10, 0));
        row.setOpaque(false);
        row.setBorder(new EmptyBorder(4, 0, 4, 0));

        JLabel pathLabel = new JLabel(index + ". " + f.getAbsolutePath());
        pathLabel.setFont(Theme.FONT_MONO);
        pathLabel.setForeground(Theme.TEXT);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        actions.setOpaque(false);

        JButton openLocation = smallButton("Open Location");
        openLocation.addActionListener(e -> FileUtils.openContainingFolder(f.getAbsolutePath()));

        JButton copyPath = smallButton("Copy Path");
        copyPath.addActionListener(e -> FileUtils.copyToClipboard(f.getAbsolutePath()));

        JButton details = smallButton("Details");
        details.addActionListener(e -> owner.openFileDetails(f));

        actions.add(openLocation);
        actions.add(copyPath);
        actions.add(details);

        row.add(pathLabel, BorderLayout.CENTER);
        row.add(actions, BorderLayout.EAST);
        return row;
    }

    private JButton smallButton(String text) {
        JButton b = new JButton(text);
        b.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        b.setFocusPainted(false);
        b.setBackground(Theme.PANEL_LIGHT);
        b.setForeground(Theme.ACCENT);
        b.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Theme.BORDER), new EmptyBorder(3, 8, 3, 8)));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }
}

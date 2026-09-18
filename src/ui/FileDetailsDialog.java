package com.digitaldetective.ui;

import com.digitaldetective.model.FileRecord;
import com.digitaldetective.model.SuspiciousFinding;
import com.digitaldetective.util.FileUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Modal dialog shown when the user double-clicks a file in any table:
 * displays every collected forensic attribute plus quick actions.
 */
public class FileDetailsDialog extends JDialog {

    public FileDetailsDialog(Window owner, FileRecord record) {
        super(owner, "File Details", ModalityType.APPLICATION_MODAL);
        setSize(560, 560);
        setLocationRelativeTo(owner);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(Theme.PANEL);
        root.setBorder(new EmptyBorder(22, 24, 18, 24));

        JLabel title = Theme.heading("FILE DETAILS");
        title.setBorder(new EmptyBorder(0, 0, 14, 0));

        JPanel fields = new JPanel(new GridBagLayout());
        fields.setOpaque(false);
        GridBagConstraints gc = new GridBagConstraints();
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.weightx = 1;
        gc.gridx = 0;
        gc.insets = new Insets(6, 0, 6, 0);
        int row = 0;

        gc.gridy = row++;
        fields.add(field("Name", record.getFileName()), gc);
        gc.gridy = row++;
        fields.add(field("Location", record.getAbsolutePath()), gc);
        gc.gridy = row++;
        fields.add(field("Type", record.getFileType() == null ? "Unknown" : record.getFileType().getDisplayName()
                + (record.getExtension() == null || record.getExtension().isEmpty() ? "" : " (." + record.getExtension() + ")")), gc);
        gc.gridy = row++;
        fields.add(field("Size", record.getFormattedSize()), gc);
        gc.gridy = row++;
        fields.add(field("Created", record.getFormattedCreated()), gc);
        gc.gridy = row++;
        fields.add(field("Modified", record.getFormattedModified()), gc);
        gc.gridy = row++;
        fields.add(field("Last Accessed", record.getFormattedAccessed()), gc);
        gc.gridy = row++;
        fields.add(field("Hidden", record.isHidden() ? "Yes" : "No"), gc);
        gc.gridy = row++;
        fields.add(field("Read Only", record.isReadOnly() ? "Yes" : "No"), gc);
        gc.gridy = row++;
        fields.add(field("Symbolic Link", record.isSymbolicLink() ? "Yes" : "No"), gc);
        gc.gridy = row++;
        fields.add(field("SHA-256", record.getSha256() == null || record.getSha256().isEmpty()
                ? "(not computed - empty or unreadable file)" : record.getSha256()), gc);

        String status = "NORMAL";
        Color statusColor = Theme.ACCENT_GREEN;
        SuspiciousFinding finding = record.getSuspiciousFinding();
        if (finding != null) {
            status = "POTENTIALLY SUSPICIOUS (" + finding.getSeverity() + ", score " + finding.getScore() + "/100)";
            statusColor = Theme.severityColor(finding.getSeverity().name());
        } else if (record.isDuplicate()) {
            status = "DUPLICATE DETECTED";
            statusColor = Theme.WARNING;
        }
        gc.gridy = row++;
        JPanel statusField = field("Status", status);
        ((JLabel) statusField.getComponent(1)).setForeground(statusColor);
        fields.add(statusField, gc);

        if (finding != null) {
            gc.gridy = row++;
            fields.add(field("Reasons", finding.getReasonsJoined()), gc);
            gc.gridy = row++;
            fields.add(field("Recommendation", finding.getRecommendation()), gc);
        }

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        buttons.setOpaque(false);
        buttons.setBorder(new EmptyBorder(18, 0, 0, 0));

        JButton copyPath = Theme.secondaryButton("Copy Path");
        copyPath.addActionListener(e -> FileUtils.copyToClipboard(record.getAbsolutePath()));

        JButton copyHash = Theme.secondaryButton("Copy Hash");
        copyHash.addActionListener(e -> FileUtils.copyToClipboard(
                record.getSha256() == null ? "" : record.getSha256()));

        JButton openFolder = Theme.secondaryButton("Open Containing Folder");
        openFolder.addActionListener(e -> FileUtils.openContainingFolder(record.getAbsolutePath()));

        JButton close = Theme.primaryButton("Close");
        close.addActionListener(e -> dispose());

        buttons.add(copyPath);
        buttons.add(copyHash);
        buttons.add(openFolder);
        buttons.add(close);

        root.add(title, BorderLayout.NORTH);
        root.add(Theme.scrollable(fields), BorderLayout.CENTER);
        root.add(buttons, BorderLayout.SOUTH);

        setContentPane(root);
    }

    private JPanel field(String label, String value) {
        JPanel p = new JPanel(new GridLayout(2, 1, 0, 2));
        p.setOpaque(false);
        JLabel l = new JLabel(label.toUpperCase());
        l.setFont(new Font("Segoe UI", Font.BOLD, 10));
        l.setForeground(Theme.TEXT_MUTED);
        JLabel v = new JLabel("<html><body style='width: 460px'>" + escapeForHtml(value) + "</body></html>");
        v.setFont(Theme.FONT_BODY);
        v.setForeground(Theme.TEXT);
        p.add(l);
        p.add(v);
        return p;
    }

    private String escapeForHtml(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
}

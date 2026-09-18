package com.digitaldetective.ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Modal-ish progress dialog shown while {@code FileScanner} works on a
 * background thread. Because the total file count is not known ahead of
 * time without a costly pre-pass, the progress bar runs in indeterminate
 * mode while a live "files scanned" counter and the current file path give
 * the user concrete feedback that the scan is actively making progress.
 */
public class ScanProgressDialog extends JDialog {

    private final JLabel countLabel;
    private final JLabel currentFileLabel;
    private final JProgressBar progressBar;
    private Runnable onStop;

    public ScanProgressDialog(Frame owner) {
        super(owner, "Scanning...", false);
        setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        setSize(520, 220);
        setLocationRelativeTo(owner);
        setResizable(false);

        JPanel panel = new JPanel();
        panel.setBackground(Theme.PANEL);
        panel.setBorder(new EmptyBorder(24, 24, 20, 24));
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        JLabel title = Theme.heading("Scanning in progress...");
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);
        progressBar.setForeground(Theme.ACCENT);
        progressBar.setBackground(Theme.PANEL_LIGHT);
        progressBar.setBorder(BorderFactory.createEmptyBorder());
        progressBar.setAlignmentX(Component.LEFT_ALIGNMENT);
        progressBar.setMaximumSize(new Dimension(2000, 18));
        progressBar.setPreferredSize(new Dimension(460, 18));

        countLabel = new JLabel("Files scanned: 0");
        countLabel.setFont(Theme.FONT_SECTION);
        countLabel.setForeground(Theme.ACCENT_GREEN);
        countLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        currentFileLabel = new JLabel("Preparing scan...");
        currentFileLabel.setFont(Theme.FONT_MONO);
        currentFileLabel.setForeground(Theme.TEXT_MUTED);
        currentFileLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton stopButton = Theme.dangerButton("STOP SCAN");
        stopButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        stopButton.addActionListener(e -> {
            stopButton.setEnabled(false);
            stopButton.setText("STOPPING...");
            if (onStop != null) {
                onStop.run();
            }
        });

        panel.add(title);
        panel.add(Box.createVerticalStrut(16));
        panel.add(progressBar);
        panel.add(Box.createVerticalStrut(14));
        panel.add(countLabel);
        panel.add(Box.createVerticalStrut(4));
        panel.add(currentFileLabel);
        panel.add(Box.createVerticalStrut(18));
        panel.add(stopButton);

        setContentPane(panel);
    }

    public void setOnStop(Runnable onStop) {
        this.onStop = onStop;
    }

    /** Must be called on the EDT. */
    public void updateProgress(int filesScanned, String currentFilePath) {
        countLabel.setText(String.format("Files scanned: %,d", filesScanned));
        String display = currentFilePath == null ? "" : currentFilePath;
        if (display.length() > 70) {
            display = "..." + display.substring(display.length() - 67);
        }
        currentFileLabel.setText("Current file: " + display);
    }
}

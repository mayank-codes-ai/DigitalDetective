package com.digitaldetective.ui;

import com.digitaldetective.util.Constants;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * The very first screen shown when the application launches: a professional
 * welcome/splash screen with the app identity, motto, an educational-use
 * disclaimer, and two entry points (Start Investigation / Exit).
 */
public class WelcomeScreen extends JFrame {

    public WelcomeScreen() {
        super(Constants.APP_NAME);
        Theme.applyGlobalDefaults();

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(920, 620);
        setMinimumSize(new Dimension(760, 560));
        setLocationRelativeTo(null);
        getContentPane().setBackground(Theme.BACKGROUND);
        setLayout(new GridBagLayout());

        JPanel content = new JPanel();
        content.setOpaque(false);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));

        JLabel icon = new JLabel("\uD83D\uDD75\uFE0F"); // detective emoji
        icon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 64));
        icon.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel title = new JLabel(Constants.APP_NAME.toUpperCase());
        title.setFont(new Font("Segoe UI", Font.BOLD, 42));
        title.setForeground(Theme.ACCENT);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitle = new JLabel(Constants.APP_TAGLINE);
        subtitle.setFont(Theme.FONT_SUBTITLE);
        subtitle.setForeground(Theme.TEXT_MUTED);
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel motto = new JLabel("\u201C" + Constants.APP_MOTTO + "\u201D");
        motto.setFont(new Font("Segoe UI", Font.ITALIC, 16));
        motto.setForeground(Theme.ACCENT_GREEN);
        motto.setAlignmentX(Component.CENTER_ALIGNMENT);
        motto.setBorder(new EmptyBorder(14, 0, 30, 0));

        JButton startButton = Theme.primaryButton("  \u25B6  START INVESTIGATION  ");
        startButton.setFont(startButton.getFont().deriveFont(15f));
        startButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        startButton.setMaximumSize(new Dimension(320, 46));
        startButton.setPreferredSize(new Dimension(320, 46));

        JButton exitButton = Theme.secondaryButton("EXIT");
        exitButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        exitButton.setMaximumSize(new Dimension(320, 40));
        exitButton.setPreferredSize(new Dimension(320, 40));
        exitButton.addActionListener(e -> System.exit(0));

        startButton.addActionListener(e -> {
            dispose();
            SwingUtilities.invokeLater(() -> new MainFrame().setVisible(true));
        });

        JTextArea disclaimer = new JTextArea(
                "This tool is intended for educational and authorized forensic analysis.\n"
                        + "It never uploads, transmits, deletes, or modifies your files.");
        disclaimer.setEditable(false);
        disclaimer.setFocusable(false);
        disclaimer.setOpaque(false);
        disclaimer.setLineWrap(true);
        disclaimer.setWrapStyleWord(true);
        disclaimer.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        disclaimer.setForeground(Theme.TEXT_MUTED);
        disclaimer.setAlignmentX(Component.CENTER_ALIGNMENT);
        disclaimer.setMaximumSize(new Dimension(460, 60));

        JLabel version = new JLabel("v" + Constants.APP_VERSION);
        version.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        version.setForeground(Theme.TEXT_MUTED);
        version.setAlignmentX(Component.CENTER_ALIGNMENT);
        version.setBorder(new EmptyBorder(24, 0, 0, 0));

        content.add(icon);
        content.add(Box.createVerticalStrut(10));
        content.add(title);
        content.add(Box.createVerticalStrut(6));
        content.add(subtitle);
        content.add(motto);
        content.add(startButton);
        content.add(Box.createVerticalStrut(12));
        content.add(exitButton);
        content.add(Box.createVerticalStrut(28));
        content.add(disclaimer);
        content.add(version);

        add(content);
    }
}

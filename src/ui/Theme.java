package com.digitaldetective.ui;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Central place for the application's dark, cybersecurity-inspired visual
 * theme: colors, fonts, and a handful of small factory methods for
 * consistently styled buttons/panels/labels used across every screen.
 */
public final class Theme {

    private Theme() {
    }

    public static final Color BACKGROUND = new Color(0x0d, 0x11, 0x17);
    public static final Color PANEL = new Color(0x16, 0x1b, 0x22);
    public static final Color PANEL_LIGHT = new Color(0x1c, 0x23, 0x30);
    public static final Color BORDER = new Color(0x30, 0x36, 0x3d);
    public static final Color TEXT = new Color(0xe6, 0xed, 0xf3);
    public static final Color TEXT_MUTED = new Color(0x8b, 0x94, 0x9e);
    public static final Color ACCENT = new Color(0x39, 0xd0, 0xff);
    public static final Color ACCENT_GREEN = new Color(0x7e, 0xe7, 0x87);
    public static final Color WARNING = new Color(0xff, 0xd1, 0x66);
    public static final Color DANGER = new Color(0xff, 0x6b, 0x6b);

    public static final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 30);
    public static final Font FONT_SUBTITLE = new Font("Segoe UI", Font.PLAIN, 15);
    public static final Font FONT_HEADING = new Font("Segoe UI", Font.BOLD, 18);
    public static final Font FONT_SECTION = new Font("Segoe UI", Font.BOLD, 13);
    public static final Font FONT_BODY = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_MONO = new Font("Consolas", Font.PLAIN, 12);
    public static final Font FONT_CARD_VALUE = new Font("Segoe UI", Font.BOLD, 22);

    public static void applyGlobalDefaults() {
        UIManager.put("Panel.background", BACKGROUND);
        UIManager.put("OptionPane.background", PANEL);
        UIManager.put("control", PANEL);
    }

    public static JButton primaryButton(String text) {
        JButton b = new JButton(text);
        styleButton(b, ACCENT, BACKGROUND);
        return b;
    }

    public static JButton secondaryButton(String text) {
        JButton b = new JButton(text);
        styleButton(b, PANEL_LIGHT, TEXT);
        b.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER, 1, true),
                new EmptyBorder(8, 18, 8, 18)));
        return b;
    }

    public static JButton dangerButton(String text) {
        JButton b = new JButton(text);
        styleButton(b, DANGER, BACKGROUND);
        return b;
    }

    private static void styleButton(JButton b, Color bg, Color fg) {
        b.setBackground(bg);
        b.setForeground(fg);
        b.setFocusPainted(false);
        b.setFont(FONT_SECTION);
        b.setBorder(new EmptyBorder(9, 20, 9, 20));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setOpaque(true);
        b.setBorderPainted(false);
    }

    public static JPanel card() {
        JPanel p = new JPanel();
        p.setBackground(PANEL);
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER, 1, true),
                new EmptyBorder(16, 18, 16, 18)));
        return p;
    }

    public static Border sectionPadding() {
        return new EmptyBorder(18, 24, 18, 24);
    }

    public static JLabel heading(String text) {
        JLabel l = new JLabel(text);
        l.setFont(FONT_HEADING);
        l.setForeground(TEXT);
        return l;
    }

    public static JLabel muted(String text) {
        JLabel l = new JLabel(text);
        l.setFont(FONT_BODY);
        l.setForeground(TEXT_MUTED);
        return l;
    }

    public static JScrollPane scrollable(Component content) {
        JScrollPane sp = new JScrollPane(content);
        sp.setBorder(BorderFactory.createEmptyBorder());
        sp.getVerticalScrollBar().setUnitIncrement(16);
        sp.setBackground(BACKGROUND);
        sp.getViewport().setBackground(BACKGROUND);
        return sp;
    }

    public static Color severityColor(String severityName) {
        switch (severityName) {
            case "HIGH": return DANGER;
            case "MEDIUM": return WARNING;
            default: return ACCENT_GREEN;
        }
    }
}

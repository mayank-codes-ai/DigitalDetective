package com.digitaldetective.ui;

import javax.swing.*;
import java.awt.*;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * A small, self-contained horizontal bar chart rendered with Java2D - used
 * for "largest file types by storage" and similar breakdowns without
 * requiring any external charting dependency.
 */
public class BarChartPanel extends JPanel {

    private Map<String, Long> data = new LinkedHashMap<>();
    private java.util.function.LongFunction<String> valueFormatter = String::valueOf;

    public BarChartPanel() {
        setOpaque(false);
        setPreferredSize(new Dimension(360, 260));
    }

    public void setData(Map<String, Long> data) {
        this.data = data == null ? new LinkedHashMap<>() : data;
        repaint();
    }

    public void setValueFormatter(java.util.function.LongFunction<String> formatter) {
        this.valueFormatter = formatter;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setFont(Theme.FONT_BODY);

        if (data.isEmpty()) {
            g2.setColor(Theme.TEXT_MUTED);
            g2.drawString("No data to display yet.", 10, 20);
            g2.dispose();
            return;
        }

        long max = data.values().stream().mapToLong(Long::longValue).max().orElse(1);
        max = Math.max(max, 1);

        int labelWidth = 130;
        int valueWidth = 90;
        int rowHeight = 28;
        int chartWidth = Math.max(getWidth() - labelWidth - valueWidth - 20, 60);
        int y = 10;

        for (Map.Entry<String, Long> entry : data.entrySet()) {
            g2.setColor(Theme.TEXT);
            g2.drawString(truncate(entry.getKey(), 16), 0, y + 18);

            double ratio = entry.getValue() / (double) max;
            int barWidth = (int) Math.round(chartWidth * ratio);

            g2.setColor(Theme.PANEL_LIGHT);
            g2.fillRoundRect(labelWidth, y + 4, chartWidth, 16, 6, 6);

            g2.setColor(Theme.ACCENT);
            g2.fillRoundRect(labelWidth, y + 4, Math.max(barWidth, 3), 16, 6, 6);

            g2.setColor(Theme.TEXT_MUTED);
            g2.drawString(valueFormatter.apply(entry.getValue()), labelWidth + chartWidth + 10, y + 18);

            y += rowHeight;
        }

        g2.dispose();
    }

    private String truncate(String s, int max) {
        return s.length() <= max ? s : s.substring(0, max - 1) + "\u2026";
    }
}

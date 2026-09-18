package com.digitaldetective.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Arc2D;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * A small, self-contained pie chart rendered with Java2D. Built by hand
 * (rather than pulling in a charting library) to keep the project free of
 * extra Maven dependencies and network requirements, as required by the
 * project brief.
 */
public class PieChartPanel extends JPanel {

    private static final Color[] PALETTE = {
            Theme.ACCENT, Theme.ACCENT_GREEN, Theme.WARNING, Theme.DANGER,
            new Color(0xa8, 0x7c, 0xff), new Color(0xff, 0x9f, 0x40),
            new Color(0x4d, 0xd4, 0xac), new Color(0xff, 0x6f, 0xa1),
            new Color(0x6e, 0xc6, 0xff), new Color(0xc9, 0xcb, 0xcf), new Color(0x8b, 0x94, 0x9e)
    };

    private Map<String, Long> data = new LinkedHashMap<>();

    public PieChartPanel() {
        setOpaque(false);
        setPreferredSize(new Dimension(360, 300));
    }

    public void setData(Map<String, Long> data) {
        this.data = data == null ? new LinkedHashMap<>() : data;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        long total = data.values().stream().mapToLong(Long::longValue).sum();
        int size = Math.min(getWidth() - 160, getHeight() - 20);
        size = Math.max(size, 60);
        int x = 20;
        int y = (getHeight() - size) / 2;

        if (total <= 0) {
            g2.setColor(Theme.TEXT_MUTED);
            g2.setFont(Theme.FONT_BODY);
            g2.drawString("No data to display yet.", x, getHeight() / 2);
            g2.dispose();
            return;
        }

        double startAngle = 90;
        int colorIndex = 0;
        int legendX = x + size + 30;
        int legendY = y + 4;

        for (Map.Entry<String, Long> entry : data.entrySet()) {
            double sweep = 360.0 * entry.getValue() / total;
            Color color = PALETTE[colorIndex % PALETTE.length];
            g2.setColor(color);
            Arc2D arc = new Arc2D.Double(x, y, size, size, startAngle, -sweep, Arc2D.PIE);
            g2.fill(arc);
            startAngle -= sweep;

            g2.setColor(color);
            g2.fillRect(legendX, legendY, 10, 10);
            g2.setColor(Theme.TEXT);
            g2.setFont(Theme.FONT_BODY);
            double pct = 100.0 * entry.getValue() / total;
            g2.drawString(String.format("%s (%.1f%%)", entry.getKey(), pct), legendX + 16, legendY + 10);
            legendY += 22;
            colorIndex++;
        }

        g2.setColor(Theme.BORDER);
        g2.drawOval(x, y, size, size);

        g2.dispose();
    }
}

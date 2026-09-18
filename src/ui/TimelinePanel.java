package com.digitaldetective.ui;

import com.digitaldetective.analyzer.TimelineAnalyzer;
import com.digitaldetective.model.Investigation;
import com.digitaldetective.util.DateUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;

/**
 * The "Timeline" section: a chronological feed of created/modified/accessed
 * events across every file, with quick date-range filter buttons.
 */
public class TimelinePanel extends JPanel {

    private final MainFrame owner;
    private Investigation investigation = new Investigation();
    private TimelineAnalyzer.RangeFilter activeRange = TimelineAnalyzer.RangeFilter.ALL;

    private final JPanel eventsColumn = new JPanel();
    private final JLabel countLabel = Theme.muted("0 events");
    private final JPanel filterBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));

    public TimelinePanel(MainFrame owner) {
        this.owner = owner;
        setOpaque(false);
        setLayout(new BorderLayout(0, 12));
        setBorder(Theme.sectionPadding());

        JLabel title = Theme.heading("File Timeline");

        filterBar.setOpaque(false);
        buildFilterButtons();

        JPanel header = new JPanel(new BorderLayout(0, 8));
        header.setOpaque(false);
        header.add(title, BorderLayout.NORTH);
        header.add(filterBar, BorderLayout.SOUTH);

        eventsColumn.setOpaque(false);
        eventsColumn.setLayout(new BoxLayout(eventsColumn, BoxLayout.Y_AXIS));

        JPanel south = new JPanel(new BorderLayout());
        south.setOpaque(false);
        south.add(countLabel, BorderLayout.WEST);

        add(header, BorderLayout.NORTH);
        add(Theme.scrollable(eventsColumn), BorderLayout.CENTER);
        add(south, BorderLayout.SOUTH);
    }

    private void buildFilterButtons() {
        addFilterButton("All", TimelineAnalyzer.RangeFilter.ALL);
        addFilterButton("Today", TimelineAnalyzer.RangeFilter.TODAY);
        addFilterButton("Last 24 Hours", TimelineAnalyzer.RangeFilter.LAST_24_HOURS);
        addFilterButton("Last 7 Days", TimelineAnalyzer.RangeFilter.LAST_7_DAYS);
        addFilterButton("Last 30 Days", TimelineAnalyzer.RangeFilter.LAST_30_DAYS);
    }

    private void addFilterButton(String label, TimelineAnalyzer.RangeFilter range) {
        JButton b = Theme.secondaryButton(label);
        b.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        b.addActionListener(e -> {
            activeRange = range;
            rebuild();
        });
        filterBar.add(b);
    }

    public void refresh(Investigation investigation) {
        this.investigation = investigation;
        activeRange = TimelineAnalyzer.RangeFilter.ALL;
        rebuild();
    }

    private void rebuild() {
        eventsColumn.removeAll();

        List<TimelineAnalyzer.TimelineEvent> events = TimelineAnalyzer.buildTimeline(investigation.getAllFiles());
        List<TimelineAnalyzer.TimelineEvent> filtered = TimelineAnalyzer.filter(events, activeRange, null, null);

        int shown = 0;
        for (TimelineAnalyzer.TimelineEvent event : filtered) {
            if (shown++ >= 300) {
                JLabel more = Theme.muted("... " + (filtered.size() - shown + 1) + " more events not shown. "
                        + "Use the File Explorer search for full detail.");
                more.setBorder(new EmptyBorder(10, 0, 10, 0));
                eventsColumn.add(more);
                break;
            }
            eventsColumn.add(buildEventRow(event));
        }

        countLabel.setText(String.format("%,d event(s) shown", Math.min(shown, filtered.size())));

        eventsColumn.revalidate();
        eventsColumn.repaint();
    }

    private JComponent buildEventRow(TimelineAnalyzer.TimelineEvent event) {
        JPanel row = new JPanel(new BorderLayout(14, 0));
        row.setOpaque(true);
        row.setBackground(Theme.PANEL);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(2000, 52));
        row.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, Theme.BORDER), new EmptyBorder(8, 12, 8, 12)));

        JLabel dateLabel = new JLabel(DateUtils.formatDateTime(event.timestamp));
        dateLabel.setFont(Theme.FONT_MONO);
        dateLabel.setForeground(Theme.TEXT_MUTED);
        dateLabel.setPreferredSize(new Dimension(190, 20));

        JLabel eventLabel = new JLabel(event.eventType.toUpperCase());
        eventLabel.setFont(new Font("Segoe UI", Font.BOLD, 11));
        eventLabel.setForeground(eventColor(event.eventType));
        eventLabel.setPreferredSize(new Dimension(90, 20));

        JLabel nameLabel = new JLabel(event.file.getFileName());
        nameLabel.setFont(Theme.FONT_BODY);
        nameLabel.setForeground(Theme.TEXT);

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 14, 0));
        left.setOpaque(false);
        left.add(dateLabel);
        left.add(eventLabel);
        left.add(nameLabel);

        row.add(left, BorderLayout.CENTER);

        row.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) {
                    owner.openFileDetails(event.file);
                }
            }
        });
        row.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        return row;
    }

    private Color eventColor(String eventType) {
        switch (eventType) {
            case "created": return Theme.ACCENT_GREEN;
            case "modified": return Theme.ACCENT;
            default: return Theme.WARNING;
        }
    }
}

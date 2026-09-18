package com.digitaldetective.ui;

import com.digitaldetective.model.FileRecord;
import com.digitaldetective.model.FileType;
import com.digitaldetective.model.Investigation;
import com.digitaldetective.util.FileUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The "Storage" section: total storage scanned, a file-category pie chart,
 * a bar chart of the largest categories by size, and the Top 10 Largest
 * Files list.
 */
public class StoragePanel extends JPanel {

    private final MainFrame owner;
    private final PieChartPanel pieChart = new PieChartPanel();
    private final BarChartPanel barChart = new BarChartPanel();
    private final JLabel totalLabel = new JLabel("0 B");
    private JTable largestTable;

    public StoragePanel(MainFrame owner) {
        this.owner = owner;
        setOpaque(false);
        setLayout(new BorderLayout(0, 16));
        setBorder(Theme.sectionPadding());

        barChart.setValueFormatter(FileUtils::formatSize);

        JPanel column = new JPanel();
        column.setOpaque(false);
        column.setLayout(new BoxLayout(column, BoxLayout.Y_AXIS));

        JLabel title = Theme.heading("Storage Analytics");
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        totalLabel.setFont(new Font("Segoe UI", Font.BOLD, 30));
        totalLabel.setForeground(Theme.ACCENT);
        totalLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel totalCaption = Theme.muted("Total Storage Scanned");
        totalCaption.setAlignmentX(Component.LEFT_ALIGNMENT);
        totalCaption.setBorder(new EmptyBorder(0, 0, 16, 0));

        JPanel chartsRow = new JPanel(new GridLayout(1, 2, 16, 0));
        chartsRow.setOpaque(false);
        chartsRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        chartsRow.setMaximumSize(new Dimension(2000, 320));

        JPanel pieCard = Theme.card();
        pieCard.setLayout(new BorderLayout());
        JLabel pieTitle = new JLabel("File Category Distribution");
        pieTitle.setFont(Theme.FONT_SECTION);
        pieTitle.setForeground(Theme.TEXT_MUTED);
        pieCard.add(pieTitle, BorderLayout.NORTH);
        pieCard.add(pieChart, BorderLayout.CENTER);

        JPanel barCard = Theme.card();
        barCard.setLayout(new BorderLayout());
        JLabel barTitle = new JLabel("Storage by Category");
        barTitle.setFont(Theme.FONT_SECTION);
        barTitle.setForeground(Theme.TEXT_MUTED);
        barCard.add(barTitle, BorderLayout.NORTH);
        barCard.add(barChart, BorderLayout.CENTER);

        chartsRow.add(pieCard);
        chartsRow.add(barCard);

        JLabel topTitle = Theme.heading("Top 10 Largest Files");
        topTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        topTitle.setBorder(new EmptyBorder(24, 0, 10, 0));

        largestTable = UiUtils.buildFileTable(owner, java.util.Collections.emptyList());
        JScrollPane tableScroll = new JScrollPane(largestTable);
        tableScroll.setAlignmentX(Component.LEFT_ALIGNMENT);
        tableScroll.setPreferredSize(new Dimension(100, 300));
        tableScroll.setMaximumSize(new Dimension(3000, 320));

        column.add(title);
        column.add(totalLabel);
        column.add(totalCaption);
        column.add(chartsRow);
        column.add(topTitle);
        column.add(tableScroll);

        add(Theme.scrollable(column), BorderLayout.CENTER);
    }

    public void refresh(Investigation investigation) {
        totalLabel.setText(FileUtils.formatSize(investigation.getTotalSizeBytes()));

        Map<String, Long> pieData = new LinkedHashMap<>();
        Map<FileType, Long> sizes = investigation.getTypeSizeDistribution();
        for (FileType type : FileType.values()) {
            long size = sizes.getOrDefault(type, 0L);
            if (size > 0) {
                pieData.put(type.getDisplayName(), size);
            }
        }
        pieChart.setData(pieData);
        barChart.setData(pieData);

        UiUtils.refreshTable(largestTable, investigation.getTopLargestFiles());
    }
}

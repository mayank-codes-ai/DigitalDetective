package com.digitaldetective.ui;

import com.digitaldetective.model.FileRecord;
import com.digitaldetective.util.DateUtils;
import com.digitaldetective.util.FileUtils;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.Instant;
import java.util.List;

/**
 * Shared Swing helpers so every table in the application looks and behaves
 * consistently: dark styling, correctly-typed sortable columns (numeric
 * size, chronological dates), and double-click-to-open-details.
 */
public final class UiUtils {

    private UiUtils() {
    }

    /**
     * Builds a fully wired, styled JTable bound to a fresh {@link FileTableModel}
     * for the given records, with double-click opening {@link FileDetailsDialog}.
     */
    public static JTable buildFileTable(MainFrame owner, List<FileRecord> records) {
        FileTableModel model = new FileTableModel();
        model.setRecords(records);

        JTable table = new JTable(model);
        styleTable(table);
        table.setRowSorter(new TableRowSorter<>(model));

        table.getColumnModel().getColumn(2).setCellRenderer(sizeRenderer());
        table.getColumnModel().getColumn(3).setCellRenderer(dateRenderer());
        table.getColumnModel().getColumn(4).setCellRenderer(dateRenderer());
        table.getColumnModel().getColumn(7).setCellRenderer(statusRenderer());

        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int viewRow = table.getSelectedRow();
                    if (viewRow < 0) return;
                    int modelRow = table.convertRowIndexToModel(viewRow);
                    FileRecord record = model.getRecordAt(modelRow);
                    if (record != null) {
                        owner.openFileDetails(record);
                    }
                }
            }
        });

        return table;
    }

    public static void refreshTable(JTable table, List<FileRecord> records) {
        ((FileTableModel) table.getModel()).setRecords(records);
    }

    private static javax.swing.table.TableCellRenderer sizeRenderer() {
        return zebraRenderer((value, row) -> {
            long bytes = (value instanceof Long) ? (Long) value : 0L;
            return FileUtils.formatSize(bytes);
        });
    }

    private static javax.swing.table.TableCellRenderer dateRenderer() {
        return zebraRenderer((value, row) -> {
            long millis = (value instanceof Long) ? (Long) value : 0L;
            if (millis <= 0) return "N/A";
            return DateUtils.formatDateTime(Instant.ofEpochMilli(millis));
        });
    }

    private interface CellFormatter {
        String format(Object value, int row);
    }

    private static javax.swing.table.TableCellRenderer zebraRenderer(CellFormatter formatter) {
        return new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                             boolean hasFocus, int row, int column) {
                String formatted = formatter.format(value, row);
                Component c = super.getTableCellRendererComponent(table, formatted, isSelected, hasFocus, row, column);
                if (!isSelected) {
                    setBackground(row % 2 == 0 ? Theme.PANEL : Theme.PANEL_LIGHT);
                    setForeground(Theme.TEXT);
                }
                return c;
            }
        };
    }

    private static javax.swing.table.TableCellRenderer statusRenderer() {
        return new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                             boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                String text = String.valueOf(value);
                if (!isSelected) {
                    if (text.startsWith("SUSPICIOUS")) {
                        setForeground(Theme.DANGER);
                    } else if (text.equals("DUPLICATE")) {
                        setForeground(Theme.WARNING);
                    } else if (text.equals("EMPTY")) {
                        setForeground(Theme.TEXT_MUTED);
                    } else {
                        setForeground(Theme.ACCENT_GREEN);
                    }
                    setBackground(row % 2 == 0 ? Theme.PANEL : Theme.PANEL_LIGHT);
                }
                return c;
            }
        };
    }

    public static void styleTable(JTable table) {
        table.setBackground(Theme.PANEL);
        table.setForeground(Theme.TEXT);
        table.setGridColor(Theme.BORDER);
        table.setRowHeight(26);
        table.setFont(Theme.FONT_BODY);
        table.setSelectionBackground(Theme.PANEL_LIGHT);
        table.setSelectionForeground(Theme.ACCENT);
        table.setFillsViewportHeight(true);
        table.getTableHeader().setBackground(Theme.PANEL_LIGHT);
        table.getTableHeader().setForeground(Theme.TEXT_MUTED);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 11));
        table.getTableHeader().setReorderingAllowed(false);

        DefaultTableCellRenderer zebra = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object value, boolean isSelected,
                                                             boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(t, value, isSelected, hasFocus, row, column);
                if (!isSelected) {
                    setBackground(row % 2 == 0 ? Theme.PANEL : Theme.PANEL_LIGHT);
                    setForeground(Theme.TEXT);
                }
                return c;
            }
        };
        for (int i = 0; i < table.getColumnCount(); i++) {
            if (i != 2 && i != 3 && i != 4 && i != 7) {
                table.getColumnModel().getColumn(i).setCellRenderer(zebra);
            }
        }
    }
}

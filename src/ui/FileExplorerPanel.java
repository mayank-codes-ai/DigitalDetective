package com.digitaldetective.ui;

import com.digitaldetective.model.FileRecord;
import com.digitaldetective.model.FileType;
import com.digitaldetective.model.Investigation;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * The "Files" section: a searchable, filterable table of every scanned
 * file, matching the File Explorer described in the project brief.
 */
public class FileExplorerPanel extends JPanel {

    private final MainFrame owner;
    private Investigation investigation = new Investigation();

    private final JTextField searchField = new JTextField();
    private final JComboBox<String> filterCombo = new JComboBox<>(new String[]{
            "All", "Documents", "Images", "Videos", "Audio", "Archives", "Executables",
            "Source Code", "Spreadsheets", "Presentations", "Suspicious", "Hidden", "Duplicates", "Empty"
    });
    private final JLabel resultCountLabel = Theme.muted("0 files");
    private JTable table;

    public FileExplorerPanel(MainFrame owner) {
        this.owner = owner;
        setOpaque(false);
        setLayout(new BorderLayout(0, 12));
        setBorder(Theme.sectionPadding());

        add(buildHeader(), BorderLayout.NORTH);

        table = UiUtils.buildFileTable(owner, new ArrayList<>());
        add(new JScrollPane(table), BorderLayout.CENTER);
        add(resultCountLabel, BorderLayout.SOUTH);

        searchField.getDocument().addDocumentListener(new SimpleDocumentListener(this::applyFilters));
        filterCombo.addActionListener(e -> applyFilters());
    }

    private JComponent buildHeader() {
        JPanel header = new JPanel(new BorderLayout(0, 10));
        header.setOpaque(false);

        JLabel title = Theme.heading("File Explorer");

        JPanel controls = new JPanel(new BorderLayout(10, 0));
        controls.setOpaque(false);

        searchField.setToolTipText("Search by filename, extension, path, hash, or type");
        searchField.setBackground(Theme.PANEL_LIGHT);
        searchField.setForeground(Theme.TEXT);
        searchField.setCaretColor(Theme.TEXT);
        searchField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Theme.BORDER), new EmptyBorder(8, 10, 8, 10)));

        filterCombo.setBackground(Theme.PANEL_LIGHT);
        filterCombo.setForeground(Theme.TEXT);

        controls.add(searchField, BorderLayout.CENTER);
        controls.add(filterCombo, BorderLayout.EAST);

        header.add(title, BorderLayout.NORTH);
        header.add(controls, BorderLayout.CENTER);
        return header;
    }

    public void refresh(Investigation investigation) {
        this.investigation = investigation;
        applyFilters();
    }

    private void applyFilters() {
        String query = searchField.getText().trim().toLowerCase(Locale.ROOT);
        String filter = (String) filterCombo.getSelectedItem();

        List<FileRecord> filtered = new ArrayList<>();
        for (FileRecord f : investigation.getAllFiles()) {
            if (f.isDirectory()) continue;
            if (!matchesFilter(f, filter)) continue;
            if (!matchesQuery(f, query)) continue;
            filtered.add(f);
        }

        UiUtils.refreshTable(table, filtered);
        resultCountLabel.setText(String.format("%,d file(s) shown out of %,d total", filtered.size(),
                investigation.getFileCount()));
    }

    private boolean matchesFilter(FileRecord f, String filter) {
        if (filter == null || filter.equals("All")) return true;
        switch (filter) {
            case "Documents": return f.getFileType() == FileType.DOCUMENT;
            case "Images": return f.getFileType() == FileType.IMAGE;
            case "Videos": return f.getFileType() == FileType.VIDEO;
            case "Audio": return f.getFileType() == FileType.AUDIO;
            case "Archives": return f.getFileType() == FileType.ARCHIVE;
            case "Executables": return f.isExecutable();
            case "Source Code": return f.getFileType() == FileType.SOURCE_CODE;
            case "Spreadsheets": return f.getFileType() == FileType.SPREADSHEET;
            case "Presentations": return f.getFileType() == FileType.PRESENTATION;
            case "Suspicious": return f.isSuspicious();
            case "Hidden": return f.isHidden();
            case "Duplicates": return f.isDuplicate();
            case "Empty": return f.isEmpty();
            default: return true;
        }
    }

    private boolean matchesQuery(FileRecord f, String query) {
        if (query.isEmpty()) return true;
        return contains(f.getFileName(), query)
                || contains(f.getExtension(), query)
                || contains(f.getAbsolutePath(), query)
                || contains(f.getSha256(), query)
                || (f.getFileType() != null && contains(f.getFileType().getDisplayName(), query));
    }

    private boolean contains(String haystack, String needle) {
        return haystack != null && haystack.toLowerCase(Locale.ROOT).contains(needle);
    }

    /** Minimal document listener adapter so all three change events map to one callback. */
    private static class SimpleDocumentListener implements javax.swing.event.DocumentListener {
        private final Runnable callback;

        SimpleDocumentListener(Runnable callback) {
            this.callback = callback;
        }

        @Override
        public void insertUpdate(javax.swing.event.DocumentEvent e) {
            callback.run();
        }

        @Override
        public void removeUpdate(javax.swing.event.DocumentEvent e) {
            callback.run();
        }

        @Override
        public void changedUpdate(javax.swing.event.DocumentEvent e) {
            callback.run();
        }
    }
}

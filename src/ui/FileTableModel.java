package com.digitaldetective.ui;

import com.digitaldetective.model.FileRecord;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;

/**
 * A reusable table model backing the "File Explorer" style tables used
 * across several screens (File Explorer, Duplicates, Suspicious, Storage
 * top-largest, Hash Explorer results, etc). Columns follow the file table
 * described in the project brief: Name, Type, Size, Modified, Created,
 * SHA-256, Hidden, Status.
 */
public class FileTableModel extends AbstractTableModel {

    public static final String[] COLUMNS = {
            "Name", "Type", "Size", "Modified", "Created", "SHA-256", "Hidden", "Status"
    };

    private List<FileRecord> records = new ArrayList<>();

    public void setRecords(List<FileRecord> records) {
        this.records = records == null ? new ArrayList<>() : records;
        fireTableDataChanged();
    }

    public FileRecord getRecordAt(int row) {
        if (row < 0 || row >= records.size()) return null;
        return records.get(row);
    }

    @Override
    public int getRowCount() {
        return records.size();
    }

    @Override
    public int getColumnCount() {
        return COLUMNS.length;
    }

    @Override
    public String getColumnName(int column) {
        return COLUMNS[column];
    }

    /**
     * Columns 2 (Size), 3 (Modified) and 4 (Created) return raw, naturally
     * comparable values (Long) rather than formatted strings, so that a
     * {@link javax.swing.table.TableRowSorter} sorts them numerically /
     * chronologically instead of alphabetically. Dedicated cell renderers
     * (see {@link UiUtils}) format these back into human-readable text.
     */
    @Override
    public Class<?> getColumnClass(int columnIndex) {
        switch (columnIndex) {
            case 2: // Size
            case 3: // Modified
            case 4: // Created
                return Long.class;
            default:
                return String.class;
        }
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return false;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        FileRecord r = records.get(rowIndex);
        switch (columnIndex) {
            case 0: return r.getFileName();
            case 1: return r.getFileType() == null ? "Unknown" : r.getFileType().getDisplayName();
            case 2: return r.getSizeBytes();
            case 3: return r.getModifiedTime() == null ? 0L : r.getModifiedTime().toEpochMilli();
            case 4: return r.getCreatedTime() == null ? 0L : r.getCreatedTime().toEpochMilli();
            case 5: return shortHash(r.getSha256());
            case 6: return r.isHidden() ? "Yes" : "No";
            case 7: return status(r);
            default: return "";
        }
    }

    private String shortHash(String hash) {
        if (hash == null || hash.isEmpty()) return "N/A";
        return hash.length() > 16 ? hash.substring(0, 16) + "..." : hash;
    }

    private String status(FileRecord r) {
        if (r.isSuspicious()) return "SUSPICIOUS (" + r.getSuspiciousFinding().getSeverity() + ")";
        if (r.isDuplicate()) return "DUPLICATE";
        if (r.isEmpty()) return "EMPTY";
        return "NORMAL";
    }
}

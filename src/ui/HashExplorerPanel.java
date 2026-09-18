package com.digitaldetective.ui;

import com.digitaldetective.model.FileRecord;
import com.digitaldetective.model.Investigation;
import com.digitaldetective.util.FileUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * The "Hash Explorer" section: lets the investigator paste a SHA-256 hash
 * and check whether it exists anywhere in the current investigation, with
 * a one-click copy for any file's hash. Nothing is ever uploaded anywhere -
 * the search runs entirely against the in-memory investigation data.
 */
public class HashExplorerPanel extends JPanel {

    private final MainFrame owner;
    private Investigation investigation = new Investigation();

    private final JTextField hashField = new JTextField();
    private final JLabel resultSummary = Theme.muted("Enter a SHA-256 hash above and click Search.");
    private JTable resultsTable;

    public HashExplorerPanel(MainFrame owner) {
        this.owner = owner;
        setOpaque(false);
        setLayout(new BorderLayout(0, 16));
        setBorder(Theme.sectionPadding());

        JLabel title = Theme.heading("Hash Explorer");

        JPanel searchRow = new JPanel(new BorderLayout(10, 0));
        searchRow.setOpaque(false);
        searchRow.setBorder(new EmptyBorder(14, 0, 0, 0));

        hashField.setToolTipText("Paste a SHA-256 hash, e.g. 8f14e45fceea167a5a36dedd4bea2543...");
        hashField.setBackground(Theme.PANEL_LIGHT);
        hashField.setForeground(Theme.TEXT);
        hashField.setCaretColor(Theme.TEXT);
        hashField.setFont(Theme.FONT_MONO);
        hashField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Theme.BORDER), new EmptyBorder(10, 10, 10, 10)));

        JButton searchButton = Theme.primaryButton("Search Hash");
        searchButton.addActionListener(e -> performSearch());
        hashField.addActionListener(e -> performSearch());

        searchRow.add(hashField, BorderLayout.CENTER);
        searchRow.add(searchButton, BorderLayout.EAST);

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.add(title, BorderLayout.NORTH);
        header.add(searchRow, BorderLayout.SOUTH);

        resultsTable = UiUtils.buildFileTable(owner, new ArrayList<>());

        add(header, BorderLayout.NORTH);
        add(new JScrollPane(resultsTable), BorderLayout.CENTER);
        add(resultSummary, BorderLayout.SOUTH);
    }

    public void refresh(Investigation investigation) {
        this.investigation = investigation;
        UiUtils.refreshTable(resultsTable, new ArrayList<>());
        resultSummary.setText("Enter a SHA-256 hash above and click Search.");
    }

    private void performSearch() {
        String query = hashField.getText().trim().toLowerCase();
        if (query.isEmpty()) {
            resultSummary.setText("Please enter a hash to search for.");
            return;
        }

        List<FileRecord> matches = new ArrayList<>();
        for (FileRecord f : investigation.getAllFiles()) {
            if (f.getSha256() != null && f.getSha256().equalsIgnoreCase(query)) {
                matches.add(f);
            }
        }

        UiUtils.refreshTable(resultsTable, matches);

        if (matches.isEmpty()) {
            resultSummary.setText("No files in this investigation match that hash.");
        } else {
            resultSummary.setText(String.format("%d file(s) found with this exact SHA-256 hash. "
                    + "Double-click a row for full details.", matches.size()));
        }
    }
}

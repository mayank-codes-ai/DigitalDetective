package com.digitaldetective.ui;

import com.digitaldetective.analyzer.InvestigationAnalyzer;
import com.digitaldetective.model.Investigation;
import com.digitaldetective.report.InvestigationHistoryManager;
import com.digitaldetective.scanner.FileScanner;
import com.digitaldetective.util.Constants;
import com.digitaldetective.util.DemoDataGenerator;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The main application window shown after "Start Investigation". Hosts the
 * top toolbar (folder selection / scan controls), the left-hand section
 * navigation, and a {@link CardLayout} content area with one panel per
 * section of the tool.
 */
public class MainFrame extends JFrame {

    private final FileScanner scanner = new FileScanner();
    private Investigation investigation = new Investigation(); // empty until a scan/demo runs
    private Path selectedFolder;
    private ScanProgressDialog progressDialog;

    private final JLabel locationLabel = new JLabel("No location selected");
    private final JButton selectFolderButton = Theme.secondaryButton("Select Folder");
    private final JButton startScanButton = Theme.primaryButton("Start Scan");
    private final JButton stopScanButton = Theme.dangerButton("Stop Scan");
    private final JButton demoModeButton = Theme.secondaryButton("Demo Mode");

    private final CardLayout cardLayout = new CardLayout();
    private final JPanel contentPanel = new JPanel();

    private final Map<String, JButton> navButtons = new LinkedHashMap<>();

    private OverviewPanel overviewPanel;
    private FileExplorerPanel fileExplorerPanel;
    private DuplicatesPanel duplicatesPanel;
    private SuspiciousPanel suspiciousPanel;
    private TimelinePanel timelinePanel;
    private StoragePanel storagePanel;
    private HashExplorerPanel hashExplorerPanel;
    private ReportsPanel reportsPanel;
    private HistoryPanel historyPanel;

    public MainFrame() {
        super(Constants.APP_NAME + " - Investigation Dashboard");
        Theme.applyGlobalDefaults();
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1280, 820);
        setMinimumSize(new Dimension(1024, 680));
        setLocationRelativeTo(null);
        getContentPane().setBackground(Theme.BACKGROUND);
        setLayout(new BorderLayout());

        add(buildToolbar(), BorderLayout.NORTH);
        add(buildSidebar(), BorderLayout.WEST);
        add(buildContentArea(), BorderLayout.CENTER);

        updateScanButtonsState(false);
        refreshAll();
    }

    // ---- Toolbar --------------------------------------------------------------

    private JComponent buildToolbar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(Theme.PANEL);
        bar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, Theme.BORDER),
                new EmptyBorder(12, 20, 12, 20)));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        left.setOpaque(false);
        JLabel brand = new JLabel("\uD83D\uDD75\uFE0F  " + Constants.APP_NAME.toUpperCase());
        brand.setFont(new Font("Segoe UI", Font.BOLD, 16));
        brand.setForeground(Theme.ACCENT);
        left.add(brand);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        right.setOpaque(false);
        locationLabel.setForeground(Theme.TEXT_MUTED);
        locationLabel.setFont(Theme.FONT_BODY);

        selectFolderButton.addActionListener(e -> onSelectFolder());
        startScanButton.addActionListener(e -> onStartScan());
        stopScanButton.addActionListener(e -> onStopScan());
        demoModeButton.addActionListener(e -> onDemoMode());

        right.add(locationLabel);
        right.add(selectFolderButton);
        right.add(startScanButton);
        right.add(stopScanButton);
        right.add(new JSeparator(SwingConstants.VERTICAL));
        right.add(demoModeButton);

        bar.add(left, BorderLayout.WEST);
        bar.add(right, BorderLayout.EAST);
        return bar;
    }

    private void onSelectFolder() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setDialogTitle("Select Investigation Location");
        int result = chooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            selectedFolder = chooser.getSelectedFile().toPath();
            locationLabel.setText("Selected: " + selectedFolder);
            startScanButton.setEnabled(true);
        }
    }

    private void onStartScan() {
        if (selectedFolder == null) {
            JOptionPane.showMessageDialog(this, "Please select a folder first.", "No Folder Selected",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        updateScanButtonsState(true);

        progressDialog = new ScanProgressDialog(this);
        progressDialog.setOnStop(this::onStopScan);

        ScanWorker worker = new ScanWorker(selectedFolder);
        worker.execute();

        progressDialog.setVisible(true);
    }

    private void onStopScan() {
        scanner.stop();
    }

    private void onDemoMode() {
        investigation = DemoDataGenerator.generate();
        locationLabel.setText("DEMO MODE - synthetic sample data");
        refreshAll();
        showCard("Overview");
        JOptionPane.showMessageDialog(this,
                "Demo Mode loaded a realistic, synthetic investigation so you can explore every screen "
                        + "without scanning a real folder.",
                "Demo Mode", JOptionPane.INFORMATION_MESSAGE);
    }

    private void updateScanButtonsState(boolean scanning) {
        selectFolderButton.setEnabled(!scanning);
        startScanButton.setEnabled(!scanning && selectedFolder != null);
        stopScanButton.setEnabled(scanning);
        demoModeButton.setEnabled(!scanning);
    }

    /** Background scan task; publishes progress chunks back to the EDT. */
    private class ScanWorker extends SwingWorker<Investigation, ScanProgress> {
        private final Path root;

        ScanWorker(Path root) {
            this.root = root;
        }

        @Override
        protected Investigation doInBackground() {
            return scanner.scan(root, new FileScanner.ScanListener() {
                @Override
                public void onFileScanned(int filesScannedSoFar, String currentFilePath) {
                    publish(new ScanProgress(filesScannedSoFar, currentFilePath));
                }

                @Override
                public void onError(String friendlyMessage) {
                    // Individual file errors are recorded on the Investigation itself;
                    // we avoid popping a dialog per-error so scanning huge trees stays usable.
                }

                @Override
                public void onComplete(Investigation completed) {
                    // Final handling happens in done(), after get().
                }

                @Override
                public void onCancelled(Investigation partial) {
                    // Final handling happens in done(), after get().
                }
            });
        }

        @Override
        protected void process(List<ScanProgress> chunks) {
            if (chunks.isEmpty() || progressDialog == null) return;
            ScanProgress last = chunks.get(chunks.size() - 1);
            progressDialog.updateProgress(last.count, last.path);
        }

        @Override
        protected void done() {
            if (progressDialog != null) {
                progressDialog.dispose();
                progressDialog = null;
            }
            updateScanButtonsState(false);

            try {
                Investigation result = get();
                InvestigationAnalyzer.runAll(result);
                investigation = result;
                refreshAll();

                boolean wasCancelled = scanner.isCancelled();
                if (wasCancelled) {
                    JOptionPane.showMessageDialog(MainFrame.this,
                            "Scan stopped early. Showing partial results for "
                                    + result.getFileCount() + " file(s) scanned so far.",
                            "Scan Stopped", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    try {
                        InvestigationHistoryManager.saveInvestigation(result);
                        if (historyPanel != null) historyPanel.refresh();
                    } catch (Exception historyEx) {
                        // History is a convenience feature - never block the main flow on it.
                    }
                }
                showCard("Overview");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(MainFrame.this,
                        "The scan could not be completed: " + ex.getMessage(),
                        "Scan Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private static class ScanProgress {
        final int count;
        final String path;

        ScanProgress(int count, String path) {
            this.count = count;
            this.path = path;
        }
    }

    // ---- Sidebar navigation -----------------------------------------------------

    private JComponent buildSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setBackground(Theme.PANEL);
        sidebar.setPreferredSize(new Dimension(200, 0));
        sidebar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 0, 1, Theme.BORDER),
                new EmptyBorder(18, 12, 18, 12)));
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));

        JLabel navTitle = new JLabel("NAVIGATION");
        navTitle.setFont(new Font("Segoe UI", Font.BOLD, 11));
        navTitle.setForeground(Theme.TEXT_MUTED);
        navTitle.setBorder(new EmptyBorder(0, 8, 10, 0));
        navTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        sidebar.add(navTitle);

        String[] sections = {
                "Overview", "Files", "Duplicates", "Suspicious", "Timeline",
                "Storage", "Hash Explorer", "Reports", "History"
        };
        for (String section : sections) {
            JButton navButton = new JButton(section);
            navButton.setAlignmentX(Component.LEFT_ALIGNMENT);
            navButton.setMaximumSize(new Dimension(2000, 38));
            navButton.setHorizontalAlignment(SwingConstants.LEFT);
            navButton.setFocusPainted(false);
            navButton.setBorderPainted(false);
            navButton.setContentAreaFilled(true);
            navButton.setBackground(Theme.PANEL);
            navButton.setForeground(Theme.TEXT);
            navButton.setFont(Theme.FONT_BODY);
            navButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            navButton.addActionListener(e -> showCard(section));
            sidebar.add(navButton);
            sidebar.add(Box.createVerticalStrut(2));
            navButtons.put(section, navButton);
        }

        sidebar.add(Box.createVerticalGlue());
        return sidebar;
    }

    private void showCard(String section) {
        cardLayout.show(contentPanel, section);
        for (Map.Entry<String, JButton> e : navButtons.entrySet()) {
            boolean active = e.getKey().equals(section);
            e.getValue().setBackground(active ? Theme.PANEL_LIGHT : Theme.PANEL);
            e.getValue().setForeground(active ? Theme.ACCENT : Theme.TEXT);
        }
    }

    // ---- Content area ------------------------------------------------------------

    private JComponent buildContentArea() {
        contentPanel.setLayout(cardLayout);
        contentPanel.setBackground(Theme.BACKGROUND);

        overviewPanel = new OverviewPanel(this);
        fileExplorerPanel = new FileExplorerPanel(this);
        duplicatesPanel = new DuplicatesPanel(this);
        suspiciousPanel = new SuspiciousPanel(this);
        timelinePanel = new TimelinePanel(this);
        storagePanel = new StoragePanel(this);
        hashExplorerPanel = new HashExplorerPanel(this);
        reportsPanel = new ReportsPanel(this);
        historyPanel = new HistoryPanel(this);

        contentPanel.add(Theme.scrollable(overviewPanel), "Overview");
        contentPanel.add(Theme.scrollable(fileExplorerPanel), "Files");
        contentPanel.add(Theme.scrollable(duplicatesPanel), "Duplicates");
        contentPanel.add(Theme.scrollable(suspiciousPanel), "Suspicious");
        contentPanel.add(Theme.scrollable(timelinePanel), "Timeline");
        contentPanel.add(Theme.scrollable(storagePanel), "Storage");
        contentPanel.add(Theme.scrollable(hashExplorerPanel), "Hash Explorer");
        contentPanel.add(Theme.scrollable(reportsPanel), "Reports");
        contentPanel.add(Theme.scrollable(historyPanel), "History");

        showCard("Overview");
        return contentPanel;
    }

    /** Pushes the current investigation into every section panel. Called after scan/demo load. */
    public void refreshAll() {
        overviewPanel.refresh(investigation);
        fileExplorerPanel.refresh(investigation);
        duplicatesPanel.refresh(investigation);
        suspiciousPanel.refresh(investigation);
        timelinePanel.refresh(investigation);
        storagePanel.refresh(investigation);
        hashExplorerPanel.refresh(investigation);
        reportsPanel.refresh(investigation);
        if (historyPanel != null) historyPanel.refresh();
    }

    public Investigation getInvestigation() {
        return investigation;
    }

    public void openFileDetails(com.digitaldetective.model.FileRecord record) {
        new FileDetailsDialog(this, record).setVisible(true);
    }

    public void navigateTo(String section) {
        showCard(section);
    }
}

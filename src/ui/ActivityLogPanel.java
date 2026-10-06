package ui;

import dao.ActivityLogDAO;
import model.ActivityLog;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.List;

/**
 * ActivityLogPanel - Audit Trail Viewer for Administrators.
 * Displays system-wide activity logs with search and filtering capabilities.
 */
public class ActivityLogPanel extends JPanel {

    private final ActivityLogDAO logDAO;

    // Filter controls
    private JTextField txtSearch;
    private JComboBox<String> cmbActionFilter;
    private JComboBox<String> cmbUserFilter;
    private JTextField txtDateFilter;
    private JButton btnFilter;
    private JButton btnReset;
    private JButton btnRefresh;

    // Table
    private JTable logTable;
    private DefaultTableModel tableModel;
    private JLabel lblTotalLogs;
    private JTextArea txtSelectedDetails;

    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public ActivityLogPanel() {
        this.logDAO = new ActivityLogDAO();
        initComponents();
        loadDropdownOptions();
        loadLogData();
    }

    private void initComponents() {
        setLayout(new BorderLayout(15, 15));
        setBackground(UIUtils.COLOR_BG);
        setBorder(new EmptyBorder(15, 20, 15, 20));

        // Top Header
        JPanel topHeader = new JPanel(new BorderLayout());
        topHeader.setOpaque(false);

        JPanel titleBox = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        titleBox.setOpaque(false);
        JLabel lblTitle = UIUtils.createHeaderLabel("System Activity Logs (Audit Trail)");
        titleBox.add(lblTitle);

        btnRefresh = UIUtils.createPrimaryButton("Refresh Logs");
        btnRefresh.addActionListener(e -> {
            loadDropdownOptions();
            loadLogData();
        });

        topHeader.add(titleBox, BorderLayout.WEST);
        topHeader.add(btnRefresh, BorderLayout.EAST);
        add(topHeader, BorderLayout.NORTH);

        // Center Panel: Filter Bar + Table + Detail Card
        JPanel centerPanel = new JPanel(new BorderLayout(0, 12));
        centerPanel.setOpaque(false);

        // Filter Bar Card
        JPanel filterCard = UIUtils.createCardPanel();
        filterCard.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 6, 4, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Keyword Search
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.05;
        filterCard.add(new JLabel("Keyword:"), gbc);
        gbc.gridx = 1; gbc.gridy = 0; gbc.weightx = 0.25;
        txtSearch = new JTextField(12);
        txtSearch.setToolTipText("Search username, role, action, or description");
        filterCard.add(txtSearch, gbc);

        // Action Filter
        gbc.gridx = 2; gbc.gridy = 0; gbc.weightx = 0.05;
        filterCard.add(new JLabel("Action:"), gbc);
        gbc.gridx = 3; gbc.gridy = 0; gbc.weightx = 0.20;
        cmbActionFilter = new JComboBox<>(new String[]{"All Actions"});
        filterCard.add(cmbActionFilter, gbc);

        // User Filter
        gbc.gridx = 4; gbc.gridy = 0; gbc.weightx = 0.05;
        filterCard.add(new JLabel("User:"), gbc);
        gbc.gridx = 5; gbc.gridy = 0; gbc.weightx = 0.15;
        cmbUserFilter = new JComboBox<>(new String[]{"All Users"});
        filterCard.add(cmbUserFilter, gbc);

        // Date Filter
        gbc.gridx = 6; gbc.gridy = 0; gbc.weightx = 0.05;
        filterCard.add(new JLabel("Date:"), gbc);
        gbc.gridx = 7; gbc.gridy = 0; gbc.weightx = 0.15;
        txtDateFilter = new JTextField(8);
        txtDateFilter.setToolTipText("Format: YYYY-MM-DD");
        filterCard.add(txtDateFilter, gbc);

        // Buttons
        gbc.gridx = 8; gbc.gridy = 0; gbc.weightx = 0.10;
        JPanel btnBox = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 0));
        btnBox.setOpaque(false);
        btnFilter = UIUtils.createPrimaryButton("Filter");
        btnReset = UIUtils.createSecondaryButton("Reset");
        btnBox.add(btnFilter);
        btnBox.add(btnReset);
        filterCard.add(btnBox, gbc);

        centerPanel.add(filterCard, BorderLayout.NORTH);

        // Main Table Card
        JPanel tableCard = UIUtils.createCardPanel();
        tableCard.setLayout(new BorderLayout(0, 8));

        // Subheader inside table card with counter
        JPanel tableHeaderBar = new JPanel(new BorderLayout());
        tableHeaderBar.setOpaque(false);
        JLabel lblAuditTitle = new JLabel("Audit Log History");
        lblAuditTitle.setFont(UIUtils.FONT_SUBTITLE);
        lblAuditTitle.setForeground(UIUtils.COLOR_PRIMARY);

        lblTotalLogs = new JLabel("Showing: 0 records");
        lblTotalLogs.setFont(UIUtils.FONT_SMALL);
        lblTotalLogs.setForeground(UIUtils.COLOR_TEXT_MUTED);

        tableHeaderBar.add(lblAuditTitle, BorderLayout.WEST);
        tableHeaderBar.add(lblTotalLogs, BorderLayout.EAST);
        tableCard.add(tableHeaderBar, BorderLayout.NORTH);

        // Table
        String[] columns = {"ID", "Timestamp", "Username", "Role", "Action Type", "Description"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        logTable = new JTable(tableModel);
        UIUtils.styleTable(logTable);
        logTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Adjust column widths
        TableColumnModel colModel = logTable.getColumnModel();
        colModel.getColumn(0).setPreferredWidth(50);
        colModel.getColumn(0).setMaxWidth(70);
        colModel.getColumn(1).setPreferredWidth(150);
        colModel.getColumn(1).setMaxWidth(170);
        colModel.getColumn(2).setPreferredWidth(90);
        colModel.getColumn(2).setMaxWidth(120);
        colModel.getColumn(3).setPreferredWidth(70);
        colModel.getColumn(3).setMaxWidth(90);
        colModel.getColumn(4).setPreferredWidth(140);
        colModel.getColumn(4).setMaxWidth(170);
        colModel.getColumn(5).setPreferredWidth(450);

        tableCard.add(new JScrollPane(logTable), BorderLayout.CENTER);

        // Selected log description preview at bottom
        JPanel detailsPanel = new JPanel(new BorderLayout(5, 5));
        detailsPanel.setOpaque(false);
        detailsPanel.setBorder(new EmptyBorder(6, 0, 0, 0));

        JLabel lblDetailHeader = new JLabel("Selected Log Entry Details:");
        lblDetailHeader.setFont(UIUtils.FONT_BOLD);
        lblDetailHeader.setForeground(UIUtils.COLOR_TEXT_PRIMARY);

        txtSelectedDetails = new JTextArea(2, 20);
        txtSelectedDetails.setEditable(false);
        txtSelectedDetails.setLineWrap(true);
        txtSelectedDetails.setWrapStyleWord(true);
        txtSelectedDetails.setFont(UIUtils.FONT_REGULAR);
        txtSelectedDetails.setBackground(new Color(248, 250, 252));
        txtSelectedDetails.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UIUtils.COLOR_BORDER, 1),
            BorderFactory.createEmptyBorder(6, 8, 6, 8)
        ));

        detailsPanel.add(lblDetailHeader, BorderLayout.NORTH);
        detailsPanel.add(txtSelectedDetails, BorderLayout.CENTER);
        tableCard.add(detailsPanel, BorderLayout.SOUTH);

        centerPanel.add(tableCard, BorderLayout.CENTER);
        add(centerPanel, BorderLayout.CENTER);

        // Selection Listener
        logTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && logTable.getSelectedRow() != -1) {
                int r = logTable.getSelectedRow();
                String id = tableModel.getValueAt(r, 0).toString();
                String time = tableModel.getValueAt(r, 1).toString();
                String user = tableModel.getValueAt(r, 2).toString();
                String role = tableModel.getValueAt(r, 3).toString();
                String action = tableModel.getValueAt(r, 4).toString();
                String desc = tableModel.getValueAt(r, 5).toString();

                txtSelectedDetails.setText(String.format("[%s] Action: %s | User: %s (%s) | Log ID #%s\nDetails: %s",
                        time, action, user, role, id, desc));
            }
        });

        // Action Listeners
        btnFilter.addActionListener(e -> handleFilter());
        btnReset.addActionListener(e -> handleReset());
        txtSearch.addActionListener(e -> handleFilter());
        txtDateFilter.addActionListener(e -> handleFilter());
    }

    /**
     * Loads distinct actions and usernames into the filter dropdowns.
     */
    public void loadDropdownOptions() {
        String currentAction = (String) cmbActionFilter.getSelectedItem();
        String currentUser = (String) cmbUserFilter.getSelectedItem();

        cmbActionFilter.removeAllItems();
        cmbActionFilter.addItem("All Actions");
        List<String> actions = logDAO.getDistinctActions();
        for (String a : actions) {
            cmbActionFilter.addItem(a);
        }
        if (currentAction != null) cmbActionFilter.setSelectedItem(currentAction);

        cmbUserFilter.removeAllItems();
        cmbUserFilter.addItem("All Users");
        List<String> users = logDAO.getDistinctUsers();
        for (String u : users) {
            cmbUserFilter.addItem(u);
        }
        if (currentUser != null) cmbUserFilter.setSelectedItem(currentUser);
    }

    /**
     * Loads all logs into the table.
     */
    public void loadLogData() {
        tableModel.setRowCount(0);
        List<ActivityLog> logs = logDAO.getAllLogs();
        populateTable(logs);
    }

    private void handleFilter() {
        String keyword = txtSearch.getText().trim();
        String action = (String) cmbActionFilter.getSelectedItem();
        String user = (String) cmbUserFilter.getSelectedItem();
        String dateStr = txtDateFilter.getText().trim();

        List<ActivityLog> logs;

        // If search keyword is given and other filters are default, use searchLogs
        if (!keyword.isEmpty() && ("All Actions".equals(action)) && ("All Users".equals(user)) && dateStr.isEmpty()) {
            logs = logDAO.searchLogs(keyword);
        } else {
            logs = logDAO.filterLogs(action, user, dateStr);
            // In addition, if keyword is provided, filter the results in memory
            if (!keyword.isEmpty()) {
                String lower = keyword.toLowerCase();
                logs.removeIf(l -> !l.getUsername().toLowerCase().contains(lower) &&
                                   !l.getRole().toLowerCase().contains(lower) &&
                                   !l.getAction().toLowerCase().contains(lower) &&
                                   !l.getDescription().toLowerCase().contains(lower));
            }
        }

        tableModel.setRowCount(0);
        populateTable(logs);
    }

    private void handleReset() {
        txtSearch.setText("");
        cmbActionFilter.setSelectedIndex(0);
        cmbUserFilter.setSelectedIndex(0);
        txtDateFilter.setText("");
        txtSelectedDetails.setText("");
        loadLogData();
    }

    private void populateTable(List<ActivityLog> logs) {
        for (ActivityLog l : logs) {
            String formattedTime = (l.getTimestamp() != null) ? dateFormat.format(l.getTimestamp()) : "";
            tableModel.addRow(new Object[]{
                l.getLogId(),
                formattedTime,
                l.getUsername(),
                l.getRole(),
                l.getAction(),
                l.getDescription()
            });
        }
        lblTotalLogs.setText("Showing: " + logs.size() + " record" + (logs.size() == 1 ? "" : "s"));
    }
}

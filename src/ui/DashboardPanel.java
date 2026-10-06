package ui;

import dao.DashboardDAO;
import dao.AllocationDAO;
import dao.ComplaintDAO;
import model.Allocation;
import model.Complaint;
import model.User;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.util.Map;

/**
 * DashboardPanel - Overview & Live System Statistics.
 */
public class DashboardPanel extends JPanel {

    private final DashboardDAO dashboardDAO;
    private final AllocationDAO allocationDAO;
    private final ComplaintDAO complaintDAO;
    private final User currentUser;

    private JLabel lblTotalStudentsVal;
    private JLabel lblTotalRoomsVal;
    private JLabel lblOccupiedRoomsVal;
    private JLabel lblAvailableRoomsVal;
    private JLabel lblPendingFeesVal;
    private JLabel lblPendingComplaintsVal;

    private DefaultTableModel allocationsTableModel;
    private DefaultTableModel complaintsTableModel;

    public DashboardPanel(User user) {
        this.currentUser = user;
        this.dashboardDAO = new DashboardDAO();
        this.allocationDAO = new AllocationDAO();
        this.complaintDAO = new ComplaintDAO();

        initComponents();
        refreshData();
    }

    private void initComponents() {
        setLayout(new BorderLayout(15, 15));
        setBackground(UIUtils.COLOR_BG);
        setBorder(new EmptyBorder(15, 20, 15, 20));

        // Top Header inside Panel
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);

        JLabel lblWelcome = new JLabel("Welcome back, " + currentUser.getFullName());
        lblWelcome.setFont(UIUtils.FONT_TITLE);
        lblWelcome.setForeground(UIUtils.COLOR_HEADER);

        JButton btnRefresh = UIUtils.createPrimaryButton("Refresh Stats");
        btnRefresh.addActionListener(e -> refreshData());

        topPanel.add(lblWelcome, BorderLayout.WEST);
        topPanel.add(btnRefresh, BorderLayout.EAST);
        add(topPanel, BorderLayout.NORTH);

        // Center Content: Stats Cards + Quick Summaries
        JPanel centerPanel = new JPanel(new BorderLayout(0, 15));
        centerPanel.setOpaque(false);

        // 6 Statistics Cards in a 2x3 or 1x6 grid
        JPanel statsGrid = new JPanel(new GridLayout(2, 3, 15, 15));
        statsGrid.setOpaque(false);

        // Create labels for card values
        lblTotalStudentsVal = new JLabel("0", SwingConstants.CENTER);
        lblTotalRoomsVal = new JLabel("0", SwingConstants.CENTER);
        lblOccupiedRoomsVal = new JLabel("0", SwingConstants.CENTER);
        lblAvailableRoomsVal = new JLabel("0", SwingConstants.CENTER);
        lblPendingFeesVal = new JLabel("0", SwingConstants.CENTER);
        lblPendingComplaintsVal = new JLabel("0", SwingConstants.CENTER);

        statsGrid.add(createStatCard("Total Students", lblTotalStudentsVal, UIUtils.COLOR_PRIMARY, ""));
        statsGrid.add(createStatCard("Total Rooms", lblTotalRoomsVal, new Color(79, 70, 229), ""));
        statsGrid.add(createStatCard("Occupied Rooms", lblOccupiedRoomsVal, UIUtils.COLOR_WARNING, ""));
        statsGrid.add(createStatCard("Available Rooms", lblAvailableRoomsVal, UIUtils.COLOR_SUCCESS, ""));
        statsGrid.add(createStatCard("Pending Fee Payments", lblPendingFeesVal, UIUtils.COLOR_DANGER, ""));
        statsGrid.add(createStatCard("Pending Complaints", lblPendingComplaintsVal, new Color(225, 29, 72), ""));

        centerPanel.add(statsGrid, BorderLayout.NORTH);

        // Bottom split: Recent Active Allocations + Recent Pending Complaints
        JPanel tablesPanel = new JPanel(new GridLayout(1, 2, 15, 0));
        tablesPanel.setOpaque(false);

        // Recent Allocations
        JPanel allocCard = UIUtils.createCardPanel();
        allocCard.setLayout(new BorderLayout(5, 8));
        JLabel lblAllocHeader = new JLabel("Recent Active Allocations");
        lblAllocHeader.setFont(UIUtils.FONT_SUBTITLE);
        lblAllocHeader.setForeground(UIUtils.COLOR_PRIMARY);
        allocCard.add(lblAllocHeader, BorderLayout.NORTH);

        String[] allocCols = { "Roll No", "Student Name", "Room", "Date" };
        allocationsTableModel = new DefaultTableModel(allocCols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable allocTable = new JTable(allocationsTableModel);
        UIUtils.styleTable(allocTable);
        allocCard.add(new JScrollPane(allocTable), BorderLayout.CENTER);

        // Recent Complaints
        JPanel compCard = UIUtils.createCardPanel();
        compCard.setLayout(new BorderLayout(5, 8));
        JLabel lblCompHeader = new JLabel("Pending / In-Progress Complaints");
        lblCompHeader.setFont(UIUtils.FONT_SUBTITLE);
        lblCompHeader.setForeground(UIUtils.COLOR_PRIMARY);
        compCard.add(lblCompHeader, BorderLayout.NORTH);

        String[] compCols = { "Student", "Category", "Description", "Status" };
        complaintsTableModel = new DefaultTableModel(compCols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable compTable = new JTable(complaintsTableModel);
        UIUtils.styleTable(compTable);
        compCard.add(new JScrollPane(compTable), BorderLayout.CENTER);

        tablesPanel.add(allocCard);
        tablesPanel.add(compCard);

        centerPanel.add(tablesPanel, BorderLayout.CENTER);
        add(centerPanel, BorderLayout.CENTER);
    }

    private JPanel createStatCard(String title, JLabel valueLabel, Color accentColor, String icon) {
        JPanel card = UIUtils.createCardPanel();
        card.setLayout(new BorderLayout(10, 5));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 5, 0, 0, accentColor),
                BorderFactory.createEmptyBorder(12, 15, 12, 15)));

        JPanel textPanel = new JPanel(new GridLayout(2, 1, 0, 4));
        textPanel.setOpaque(false);

        JLabel lblTitle = new JLabel(icon + "  " + title);
        lblTitle.setFont(UIUtils.FONT_REGULAR);
        lblTitle.setForeground(UIUtils.COLOR_TEXT_MUTED);

        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 26));
        valueLabel.setForeground(accentColor);
        valueLabel.setHorizontalAlignment(SwingConstants.LEFT);

        textPanel.add(lblTitle);
        textPanel.add(valueLabel);

        card.add(textPanel, BorderLayout.CENTER);
        return card;
    }

    /**
     * Refreshes stats and table previews from MySQL.
     */
    public void refreshData() {
        // Query live statistics from MySQL
        Map<String, Integer> stats = dashboardDAO.getDashboardStats();
        lblTotalStudentsVal.setText(String.valueOf(stats.getOrDefault("totalStudents", 0)));
        lblTotalRoomsVal.setText(String.valueOf(stats.getOrDefault("totalRooms", 0)));
        lblOccupiedRoomsVal.setText(String.valueOf(stats.getOrDefault("occupiedRooms", 0)));
        lblAvailableRoomsVal.setText(String.valueOf(stats.getOrDefault("availableRooms", 0)));
        lblPendingFeesVal.setText(String.valueOf(stats.getOrDefault("pendingFees", 0)));
        lblPendingComplaintsVal.setText(String.valueOf(stats.getOrDefault("pendingComplaints", 0)));

        // Load active allocations preview
        allocationsTableModel.setRowCount(0);
        List<Allocation> activeAllocs = allocationDAO.getActiveAllocations();
        int count = 0;
        for (Allocation a : activeAllocs) {
            allocationsTableModel.addRow(new Object[] {
                    a.getStudentRollNo(),
                    a.getStudentName(),
                    a.getRoomNumber(),
                    a.getAllocationDate()
            });
            if (++count >= 10)
                break; // preview top 10
        }

        // Load complaints preview
        complaintsTableModel.setRowCount(0);
        List<Complaint> complaints = complaintDAO.getAllComplaints();
        count = 0;
        for (Complaint c : complaints) {
            if (!"Resolved".equalsIgnoreCase(c.getStatus())) {
                complaintsTableModel.addRow(new Object[] {
                        c.getStudentName(),
                        c.getComplaintType(),
                        c.getDescription(),
                        c.getStatus()
                });
                if (++count >= 10)
                    break;
            }
        }
    }
}

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
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * Modern, Minimalist & Elegant Overview & Live System Statistics Panel.
 */
public class DashboardPanel extends JPanel {

    private static class DashboardData {
        private final Map<String, Integer> stats;
        private final List<Allocation> allocations;
        private final List<Complaint> complaints;

        private DashboardData(Map<String, Integer> stats, List<Allocation> allocations,
                              List<Complaint> complaints) {
            this.stats = stats;
            this.allocations = allocations;
            this.complaints = complaints;
        }
    }

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
    }

    private void initComponents() {
        setLayout(new BorderLayout(18, 18));
        setBackground(UIUtils.COLOR_BG);
        setBorder(new EmptyBorder(20, 24, 20, 24));

        // Dashboard heading and refresh action
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);

        JPanel heading = new JPanel();
        heading.setOpaque(false);
        heading.setLayout(new BoxLayout(heading, BoxLayout.Y_AXIS));

        JLabel lblWelcome = new JLabel("Welcome back, " + currentUser.getFullName());
        lblWelcome.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblWelcome.setForeground(UIUtils.COLOR_TEXT_PRIMARY);

        String roleBadge = "Admin".equalsIgnoreCase(currentUser.getRole()) ? "Administrator" : "Staff Warden";
        JLabel lblSubtitle = new JLabel(roleBadge + " Portal  •  Live campus residence metrics  •  " +
                LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, dd MMMM yyyy")));
        lblSubtitle.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblSubtitle.setForeground(UIUtils.COLOR_TEXT_MUTED);

        heading.add(lblWelcome);
        heading.add(Box.createVerticalStrut(4));
        heading.add(lblSubtitle);

        JButton btnRefresh = UIUtils.createPrimaryButton("Refresh Stats");
        btnRefresh.setToolTipText("Reload dashboard statistics and recent activity");
        btnRefresh.addActionListener(e -> refreshData());

        topPanel.add(heading, BorderLayout.WEST);
        topPanel.add(btnRefresh, BorderLayout.EAST);
        add(topPanel, BorderLayout.NORTH);

        // Center Content: Stats Cards + Quick Summaries
        JPanel centerPanel = new JPanel(new BorderLayout(0, 18));
        centerPanel.setOpaque(false);

        // 6 Statistics Cards in a 2x3 grid
        JPanel statsGrid = new JPanel(new GridLayout(2, 3, 16, 16));
        statsGrid.setOpaque(false);

        // Create labels for card values
        lblTotalStudentsVal = new JLabel("0", SwingConstants.LEFT);
        lblTotalRoomsVal = new JLabel("0", SwingConstants.LEFT);
        lblOccupiedRoomsVal = new JLabel("0", SwingConstants.LEFT);
        lblAvailableRoomsVal = new JLabel("0", SwingConstants.LEFT);
        lblPendingFeesVal = new JLabel("0", SwingConstants.LEFT);
        lblPendingComplaintsVal = new JLabel("0", SwingConstants.LEFT);

        statsGrid.add(createStatCard("Total Students", lblTotalStudentsVal, UIUtils.COLOR_PRIMARY));
        statsGrid.add(createStatCard("Total Rooms", lblTotalRoomsVal, new Color(99, 102, 241)));
        statsGrid.add(createStatCard("Occupied Rooms", lblOccupiedRoomsVal, UIUtils.COLOR_WARNING));
        statsGrid.add(createStatCard("Available Rooms", lblAvailableRoomsVal, UIUtils.COLOR_SUCCESS));
        statsGrid.add(createStatCard("Pending Fee Payments", lblPendingFeesVal, UIUtils.COLOR_DANGER));
        statsGrid.add(createStatCard("Pending Complaints", lblPendingComplaintsVal, new Color(225, 29, 72)));

        centerPanel.add(statsGrid, BorderLayout.NORTH);

        // Bottom split: Recent Active Allocations + Recent Pending Complaints
        JPanel tablesPanel = new JPanel(new GridLayout(1, 2, 16, 0));
        tablesPanel.setOpaque(false);

        // Recent Allocations
        JPanel allocCard = UIUtils.createCardPanel();
        allocCard.setLayout(new BorderLayout(5, 10));

        JLabel lblAllocHeader = new JLabel("Recent Active Allocations");
        lblAllocHeader.setFont(UIUtils.FONT_SUBTITLE);
        lblAllocHeader.setForeground(UIUtils.COLOR_TEXT_PRIMARY);
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
        JScrollPane allocScrollPane = new JScrollPane(allocTable);
        allocScrollPane.setBorder(BorderFactory.createLineBorder(UIUtils.COLOR_BORDER));
        allocCard.add(allocScrollPane, BorderLayout.CENTER);

        // Recent Complaints
        JPanel compCard = UIUtils.createCardPanel();
        compCard.setLayout(new BorderLayout(5, 10));

        JLabel lblCompHeader = new JLabel("Pending & In-Progress Complaints");
        lblCompHeader.setFont(UIUtils.FONT_SUBTITLE);
        lblCompHeader.setForeground(UIUtils.COLOR_TEXT_PRIMARY);
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
        JScrollPane complaintScrollPane = new JScrollPane(compTable);
        complaintScrollPane.setBorder(BorderFactory.createLineBorder(UIUtils.COLOR_BORDER));
        compCard.add(complaintScrollPane, BorderLayout.CENTER);

        tablesPanel.add(allocCard);
        tablesPanel.add(compCard);

        centerPanel.add(tablesPanel, BorderLayout.CENTER);
        add(centerPanel, BorderLayout.CENTER);
    }

    private JPanel createStatCard(String title, JLabel valueLabel, Color accentColor) {
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int w = getWidth();
                int h = getHeight();
                int arc = 12;

                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, w - 1, h - 1, arc, arc);

                g2.setColor(UIUtils.COLOR_BORDER);
                g2.setStroke(new BasicStroke(1.0f));
                g2.drawRoundRect(0, 0, w - 1, h - 1, arc, arc);

                // Top accent strip
                g2.setColor(accentColor);
                g2.fillRoundRect(0, 0, w - 1, 4, 4, 4);

                g2.dispose();
                super.paintComponent(g);
            }
        };
        card.setOpaque(false);
        card.setLayout(new BorderLayout(12, 6));
        card.setBorder(new EmptyBorder(14, 18, 14, 18));

        JPanel textPanel = new JPanel();
        textPanel.setOpaque(false);
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblTitle.setForeground(UIUtils.COLOR_TEXT_MUTED);

        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        valueLabel.setForeground(accentColor);
        valueLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        textPanel.add(lblTitle);
        textPanel.add(Box.createVerticalStrut(4));
        textPanel.add(valueLabel);

        card.add(textPanel, BorderLayout.CENTER);
        return card;
    }

    /**
     * Refreshes stats and table previews from MySQL.
     */
    public void refreshData() {
        UIUtils.loadInBackground(this, "dashboard", "load dashboard data", () ->
                new DashboardData(
                        dashboardDAO.getDashboardStats(),
                        allocationDAO.getActiveAllocations(),
                        complaintDAO.getAllComplaints()), data -> {
                    Map<String, Integer> stats = data.stats;
                    lblTotalStudentsVal.setText(String.valueOf(stats.getOrDefault("totalStudents", 0)));
                    lblTotalRoomsVal.setText(String.valueOf(stats.getOrDefault("totalRooms", 0)));
                    lblOccupiedRoomsVal.setText(String.valueOf(stats.getOrDefault("occupiedRooms", 0)));
                    lblAvailableRoomsVal.setText(String.valueOf(stats.getOrDefault("availableRooms", 0)));
                    lblPendingFeesVal.setText(String.valueOf(stats.getOrDefault("pendingFees", 0)));
                    lblPendingComplaintsVal.setText(String.valueOf(stats.getOrDefault("pendingComplaints", 0)));

                    allocationsTableModel.setRowCount(0);
                    for (int i = 0; i < Math.min(10, data.allocations.size()); i++) {
                        Allocation allocation = data.allocations.get(i);
                        allocationsTableModel.addRow(new Object[] {
                                allocation.getStudentRollNo(),
                                allocation.getStudentName(),
                                allocation.getRoomNumber(),
                                allocation.getAllocationDate()
                        });
                    }

                    complaintsTableModel.setRowCount(0);
                    int count = 0;
                    for (Complaint complaint : data.complaints) {
                        if (!"Resolved".equalsIgnoreCase(complaint.getStatus())) {
                            complaintsTableModel.addRow(new Object[] {
                                    complaint.getStudentName(),
                                    complaint.getComplaintType(),
                                    complaint.getDescription(),
                                    complaint.getStatus()
                            });
                            if (++count >= 10) {
                                break;
                            }
                        }
                    }
                });
    }
}

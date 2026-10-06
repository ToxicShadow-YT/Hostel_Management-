package ui;

import model.User;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * DashboardFrame - Main Container Window for Hostel Management System.
 * Houses the sidebar navigation and CardLayout workspace.
 */
public class DashboardFrame extends JFrame {

    private final User currentUser;

    // Navigation and Cards
    private JPanel mainContentCards;
    private CardLayout cardLayout;
    private final Map<String, JButton> navButtons = new HashMap<>();

    // Sub-panels
    private DashboardPanel dashboardPanel;
    private StudentPanel studentPanel;
    private RoomPanel roomPanel;
    private AllocationPanel allocationPanel;
    private FeePanel feePanel;
    private VisitorPanel visitorPanel;
    private ComplaintPanel complaintPanel;
    private ActivityLogPanel activityLogPanel;

    // Constants for card names
    private static final String CARD_DASHBOARD = "DASHBOARD";
    private static final String CARD_STUDENTS = "STUDENTS";
    private static final String CARD_ROOMS = "ROOMS";
    private static final String CARD_ALLOCATIONS = "ALLOCATIONS";
    private static final String CARD_FEES = "FEES";
    private static final String CARD_VISITORS = "VISITORS";
    private static final String CARD_COMPLAINTS = "COMPLAINTS";
    private static final String CARD_LOGS = "LOGS";

    public DashboardFrame(User user) {
        this.currentUser = user;
        initComponents();
    }

    private void initComponents() {
        setTitle("Hostel Management System - " + currentUser.getFullName() + " (" + currentUser.getRole() + ")");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1220, 800);
        setMinimumSize(new Dimension(1050, 680));
        setLocationRelativeTo(null);
        getContentPane().setBackground(UIUtils.COLOR_BG);
        setLayout(new BorderLayout());

        // 1. Top Header Bar
        add(createTopHeader(), BorderLayout.NORTH);

        // 2. Left Sidebar Navigation
        add(createSidebar(), BorderLayout.WEST);

        // 3. Center Workspace with CardLayout
        cardLayout = new CardLayout();
        mainContentCards = new JPanel(cardLayout);
        mainContentCards.setBackground(UIUtils.COLOR_BG);

        // Instantiate sub-panels passing currentUser for audit logging
        dashboardPanel = new DashboardPanel(currentUser);
        studentPanel = new StudentPanel(currentUser);
        roomPanel = new RoomPanel(currentUser);
        allocationPanel = new AllocationPanel(currentUser);
        feePanel = new FeePanel(currentUser);
        visitorPanel = new VisitorPanel(currentUser);
        complaintPanel = new ComplaintPanel(currentUser);

        // Add to CardLayout
        mainContentCards.add(dashboardPanel, CARD_DASHBOARD);
        mainContentCards.add(studentPanel, CARD_STUDENTS);
        mainContentCards.add(roomPanel, CARD_ROOMS);
        mainContentCards.add(allocationPanel, CARD_ALLOCATIONS);
        mainContentCards.add(feePanel, CARD_FEES);
        mainContentCards.add(visitorPanel, CARD_VISITORS);
        mainContentCards.add(complaintPanel, CARD_COMPLAINTS);

        // RBAC: Only instantiate and register ActivityLogPanel for Admin
        if (isAdmin()) {
            activityLogPanel = new ActivityLogPanel();
            mainContentCards.add(activityLogPanel, CARD_LOGS);
        }

        add(mainContentCards, BorderLayout.CENTER);

        // Default to Dashboard
        setActiveCard(CARD_DASHBOARD);
    }

    private JPanel createTopHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(UIUtils.COLOR_HEADER);
        header.setBorder(new EmptyBorder(12, 20, 12, 20));

        // Left Branding
        JPanel brandPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        brandPanel.setOpaque(false);

        JLabel lblTitle = new JLabel("HOSTEL MANAGEMENT SYSTEM");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 17));
        lblTitle.setForeground(Color.WHITE);

        JLabel lblTag = new JLabel("|  Campus Residence Portal");
        lblTag.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblTag.setForeground(new Color(148, 163, 184));

        brandPanel.add(lblTitle);
        brandPanel.add(lblTag);
        header.add(brandPanel, BorderLayout.WEST);

        // Right User Info & Actions
        JPanel rightInfo = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        rightInfo.setOpaque(false);

        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, dd MMM yyyy"));
        JLabel lblDate = new JLabel(today);
        lblDate.setFont(UIUtils.FONT_REGULAR);
        lblDate.setForeground(new Color(203, 213, 225));

        JLabel lblUser = new JLabel(currentUser.getFullName() + " (" + currentUser.getRole() + ")");
        lblUser.setFont(UIUtils.FONT_BOLD);
        lblUser.setForeground(Color.WHITE);

        JButton btnLogout = UIUtils.createDangerButton("Logout");
        btnLogout.addActionListener(e -> handleLogout());

        rightInfo.add(lblDate);
        rightInfo.add(lblUser);
        rightInfo.add(btnLogout);

        header.add(rightInfo, BorderLayout.EAST);
        return header;
    }

    private JPanel createSidebar() {
        JPanel sidebar = new JPanel(new BorderLayout());
        sidebar.setPreferredSize(new Dimension(220, 0));
        sidebar.setBackground(new Color(15, 23, 42)); // Dark Slate
        sidebar.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, new Color(30, 41, 59)));

        JPanel menuItemsPanel = new JPanel();
        menuItemsPanel.setOpaque(false);
        menuItemsPanel.setLayout(new BoxLayout(menuItemsPanel, BoxLayout.Y_AXIS));
        menuItemsPanel.setBorder(new EmptyBorder(15, 10, 15, 10));

        JLabel lblMenu = new JLabel("MAIN MENU");
        lblMenu.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblMenu.setForeground(new Color(100, 116, 139));
        lblMenu.setBorder(new EmptyBorder(0, 10, 10, 0));
        lblMenu.setAlignmentX(Component.LEFT_ALIGNMENT);
        menuItemsPanel.add(lblMenu);

        // Nav Buttons
        addNavButton(menuItemsPanel, "Dashboard", CARD_DASHBOARD);
        addNavButton(menuItemsPanel, "Students", CARD_STUDENTS);
        addNavButton(menuItemsPanel, "Rooms", CARD_ROOMS);
        addNavButton(menuItemsPanel, "Room Allocation", CARD_ALLOCATIONS);
        addNavButton(menuItemsPanel, "Fees & Payments", CARD_FEES);
        addNavButton(menuItemsPanel, "Visitor Log", CARD_VISITORS);
        addNavButton(menuItemsPanel, "Complaints", CARD_COMPLAINTS);

        // RBAC: Activity Logs is strictly Admin-only
        if (isAdmin()) {
            addNavButton(menuItemsPanel, "Activity Logs", CARD_LOGS);
        }

        sidebar.add(menuItemsPanel, BorderLayout.NORTH);

        // Bottom Exit Button
        JPanel bottomPanel = new JPanel();
        bottomPanel.setOpaque(false);
        bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.Y_AXIS));
        bottomPanel.setBorder(new EmptyBorder(10, 10, 15, 10));

        JButton btnExit = new JButton("Exit System");
        btnExit.setFont(UIUtils.FONT_BOLD);
        btnExit.setForeground(new Color(248, 113, 113));
        btnExit.setBackground(new Color(30, 41, 59));
        btnExit.setFocusPainted(false);
        btnExit.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
        btnExit.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnExit.setMaximumSize(new Dimension(200, 36));
        btnExit.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnExit.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(this,
                    "Are you sure you want to exit the application?",
                    "Exit Confirmation",
                    JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                System.exit(0);
            }
        });
        bottomPanel.add(btnExit);

        sidebar.add(bottomPanel, BorderLayout.SOUTH);
        return sidebar;
    }

    private void addNavButton(JPanel container, String text, String cardName) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        btn.setForeground(new Color(203, 213, 225));
        btn.setBackground(new Color(15, 23, 42));
        btn.setFocusPainted(false);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setBorder(BorderFactory.createEmptyBorder(9, 14, 9, 14));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setMaximumSize(new Dimension(200, 38));
        btn.setAlignmentX(Component.LEFT_ALIGNMENT);

        btn.addActionListener(e -> setActiveCard(cardName));

        // Hover Effect
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                if (!btn.getBackground().equals(UIUtils.COLOR_PRIMARY)) {
                    btn.setBackground(new Color(30, 41, 59));
                }
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                if (!btn.getBackground().equals(UIUtils.COLOR_PRIMARY)) {
                    btn.setBackground(new Color(15, 23, 42));
                }
            }
        });

        navButtons.put(cardName, btn);
        container.add(btn);
        container.add(Box.createVerticalStrut(4));
    }

    private void setActiveCard(String cardName) {
        // Reset all buttons style
        for (Map.Entry<String, JButton> entry : navButtons.entrySet()) {
            if (entry.getKey().equals(cardName)) {
                entry.getValue().setBackground(UIUtils.COLOR_PRIMARY);
                entry.getValue().setForeground(Color.WHITE);
                entry.getValue().setFont(new Font("Segoe UI", Font.BOLD, 13));
            } else {
                entry.getValue().setBackground(new Color(15, 23, 42));
                entry.getValue().setForeground(new Color(203, 213, 225));
                entry.getValue().setFont(new Font("Segoe UI", Font.PLAIN, 13));
            }
        }

        // Refresh dependent data when switching screens
        switch (cardName) {
            case CARD_DASHBOARD:
                dashboardPanel.refreshData();
                break;
            case CARD_STUDENTS:
                studentPanel.loadStudentData();
                break;
            case CARD_ROOMS:
                roomPanel.loadRoomData();
                break;
            case CARD_ALLOCATIONS:
                allocationPanel.reloadDropdowns();
                allocationPanel.loadAllocationData();
                break;
            case CARD_FEES:
                feePanel.reloadStudentDropdown();
                feePanel.loadFeeData();
                break;
            case CARD_VISITORS:
                visitorPanel.reloadStudentDropdown();
                visitorPanel.loadVisitorData();
                break;
            case CARD_COMPLAINTS:
                complaintPanel.reloadStudentDropdown();
                complaintPanel.loadComplaintData();
                break;
            case CARD_LOGS:
                if (activityLogPanel != null) {
                    activityLogPanel.loadDropdownOptions();
                    activityLogPanel.loadLogData();
                }
                break;
        }

        cardLayout.show(mainContentCards, cardName);
    }

    private boolean isAdmin() {
        return currentUser != null && "Admin".equalsIgnoreCase(currentUser.getRole());
    }

    private void handleLogout() {
        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to log out?",
                "Confirm Logout",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            new LoginFrame().setVisible(true);
            this.dispose();
        }
    }
}

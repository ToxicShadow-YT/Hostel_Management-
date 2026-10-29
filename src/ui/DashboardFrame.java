package ui;

import model.User;
import model.RolePermissions;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * Modern, Minimalist & Elegant Application Shell for Administrators and Wardens.
 */
public class DashboardFrame extends JFrame {

    private final User currentUser;
    private JPanel mainContentCards;
    private CardLayout cardLayout;
    private final Map<String, JButton> navButtons = new HashMap<>();

    private DashboardPanel dashboardPanel;
    private StudentPanel studentPanel;
    private RoomPanel roomPanel;
    private AllocationPanel allocationPanel;
    private FeePanel feePanel;
    private VisitorPanel visitorPanel;
    private ComplaintPanel complaintPanel;
    private ActivityLogPanel activityLogPanel;

    private static final String CARD_DASHBOARD = "DASHBOARD";
    private static final String CARD_STUDENTS = "STUDENTS";
    private static final String CARD_ROOMS = "ROOMS";
    private static final String CARD_ALLOCATIONS = "ALLOCATIONS";
    private static final String CARD_FEES = "FEES";
    private static final String CARD_VISITORS = "VISITORS";
    private static final String CARD_COMPLAINTS = "COMPLAINTS";
    private static final String CARD_LOGS = "LOGS";

    public DashboardFrame(User user) {
        if (user == null || !RolePermissions.canAccessStaffPortal(user.getRole())) {
            throw new IllegalArgumentException("Only administrators and wardens can open the staff dashboard.");
        }
        this.currentUser = user;
        initComponents();
    }

    private void initComponents() {
        setTitle("Hostel Management System - " + currentUser.getFullName() + " (" + currentUser.getRole() + ")");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1360, 850);
        setMinimumSize(new Dimension(1020, 700));
        setLocationRelativeTo(null);
        setIconImage(createAppIcon());
        getContentPane().setBackground(UIUtils.COLOR_BG);
        setLayout(new BorderLayout());

        JPanel appHeader = new JPanel(new BorderLayout());
        appHeader.add(createTopHeader(), BorderLayout.NORTH);
        appHeader.add(createNavigationBar(), BorderLayout.SOUTH);
        add(appHeader, BorderLayout.NORTH);

        cardLayout = new CardLayout();
        mainContentCards = new JPanel(cardLayout);
        mainContentCards.setBackground(UIUtils.COLOR_BG);

        dashboardPanel = new DashboardPanel(currentUser);
        studentPanel = new StudentPanel(currentUser);
        roomPanel = new RoomPanel(currentUser);
        allocationPanel = new AllocationPanel(currentUser);
        feePanel = new FeePanel(currentUser);
        visitorPanel = new VisitorPanel(currentUser);
        complaintPanel = new ComplaintPanel(currentUser);

        mainContentCards.add(dashboardPanel, CARD_DASHBOARD);
        mainContentCards.add(studentPanel, CARD_STUDENTS);
        mainContentCards.add(roomPanel, CARD_ROOMS);
        mainContentCards.add(allocationPanel, CARD_ALLOCATIONS);
        mainContentCards.add(feePanel, CARD_FEES);
        mainContentCards.add(visitorPanel, CARD_VISITORS);
        mainContentCards.add(complaintPanel, CARD_COMPLAINTS);

        if (isAdmin()) {
            activityLogPanel = new ActivityLogPanel();
            mainContentCards.add(activityLogPanel, CARD_LOGS);
        }

        add(mainContentCards, BorderLayout.CENTER);
        setActiveCard(CARD_DASHBOARD);
    }

    private Image createAppIcon() {
        int size = 32;
        BufferedImage img = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = img.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        GradientPaint gradient = new GradientPaint(
                0, 0, new Color(99, 102, 241),
                0, size, new Color(67, 56, 202)
        );
        g2.setPaint(gradient);
        g2.fillRoundRect(0, 0, size, size, 10, 10);

        g2.setColor(Color.WHITE);
        g2.setStroke(new BasicStroke(2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        int cx = size / 2;
        int cy = size / 2;
        int[] rx = {cx - 9, cx, cx + 9};
        int[] ry = {cy, cy - 8, cy};
        g2.drawPolyline(rx, ry, 3);
        g2.drawLine(cx - 7, cy, cx - 7, cy + 8);
        g2.drawLine(cx + 7, cy, cx + 7, cy + 8);
        g2.drawLine(cx - 7, cy + 8, cx + 7, cy + 8);
        g2.drawRoundRect(cx - 3, cy + 2, 6, 6, 2, 2);

        g2.dispose();
        return img;
    }

    private JPanel createTopHeader() {
        JPanel header = new JPanel(new BorderLayout(18, 0));
        header.setBackground(UIUtils.COLOR_HEADER);
        header.setBorder(new EmptyBorder(12, 24, 12, 24));

        // Left Branding
        JPanel branding = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        branding.setOpaque(false);

        // Modern 36x36 brand badge
        JComponent brandBadge = new JComponent() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int size = 36;
                int arc = 10;
                GradientPaint gp = new GradientPaint(0, 0, new Color(99, 102, 241), 0, size, new Color(67, 56, 202));
                g2.setPaint(gp);
                g2.fillRoundRect(0, 0, size, size, arc, arc);

                g2.setColor(Color.WHITE);
                g2.setStroke(new BasicStroke(2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                int cx = size / 2;
                int cy = size / 2;
                int[] rx = {cx - 8, cx, cx + 8};
                int[] ry = {cy, cy - 7, cy};
                g2.drawPolyline(rx, ry, 3);
                g2.drawLine(cx - 6, cy, cx - 6, cy + 7);
                g2.drawLine(cx + 6, cy, cx + 6, cy + 7);
                g2.drawLine(cx - 6, cy + 7, cx + 6, cy + 7);
                g2.drawRoundRect(cx - 3, cy + 2, 6, 5, 2, 2);

                g2.dispose();
            }
        };
        brandBadge.setPreferredSize(new Dimension(36, 36));
        brandBadge.setOpaque(false);

        JPanel titleBlock = new JPanel();
        titleBlock.setOpaque(false);
        titleBlock.setLayout(new BoxLayout(titleBlock, BoxLayout.Y_AXIS));
        titleBlock.setPreferredSize(new Dimension(280, 38));

        JLabel title = new JLabel("Hostel Management System");
        title.setFont(new Font("Segoe UI", Font.BOLD, 16));
        title.setForeground(Color.WHITE);

        JLabel subtitle = new JLabel("Campus Administration Suite");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        subtitle.setForeground(new Color(148, 163, 184)); // Slate 400

        titleBlock.add(title);
        titleBlock.add(Box.createVerticalStrut(2));
        titleBlock.add(subtitle);

        branding.add(brandBadge);
        branding.add(titleBlock);

        // Right Account Panel
        JPanel account = new JPanel(new FlowLayout(FlowLayout.RIGHT, 14, 0));
        account.setOpaque(false);

        // Date Display
        JLabel date = new JLabel(LocalDate.now().format(DateTimeFormatter.ofPattern("EEE, dd MMM yyyy")));
        date.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        date.setForeground(new Color(148, 163, 184)); // Slate 400

        // User Avatar Circle
        String initial = (currentUser.getFullName() != null && !currentUser.getFullName().isEmpty())
                ? currentUser.getFullName().substring(0, 1).toUpperCase()
                : "U";

        JComponent avatar = new JComponent() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);

                int size = 32;
                if (isAdmin()) {
                    g2.setPaint(new GradientPaint(0, 0, new Color(139, 92, 246), 0, size, new Color(109, 40, 217)));
                } else {
                    g2.setPaint(new GradientPaint(0, 0, new Color(14, 165, 233), 0, size, new Color(2, 132, 199)));
                }
                g2.fillOval(0, 0, size, size);

                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 13));
                FontMetrics fm = g2.getFontMetrics();
                int tx = (size - fm.stringWidth(initial)) / 2;
                int ty = (size - fm.getHeight()) / 2 + fm.getAscent();
                g2.drawString(initial, tx, ty);
                g2.dispose();
            }
        };
        avatar.setPreferredSize(new Dimension(32, 32));
        avatar.setOpaque(false);

        // User Name & Role Pill
        JPanel userBlock = new JPanel();
        userBlock.setOpaque(false);
        userBlock.setLayout(new BoxLayout(userBlock, BoxLayout.Y_AXIS));

        JLabel userName = new JLabel(currentUser.getFullName());
        userName.setFont(new Font("Segoe UI", Font.BOLD, 13));
        userName.setForeground(Color.WHITE);

        // Role Pill Badge
        String roleText = isAdmin() ? "ADMIN" : "WARDEN";
        JLabel userRole = new JLabel(roleText) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);

                int w = getWidth();
                int h = getHeight();
                int arc = 8;

                if (isAdmin()) {
                    g2.setColor(new Color(168, 85, 247, 45));
                    g2.fillRoundRect(0, 0, w, h, arc, arc);
                    g2.setColor(new Color(168, 85, 247, 100));
                    g2.drawRoundRect(0, 0, w - 1, h - 1, arc, arc);
                } else {
                    g2.setColor(new Color(56, 189, 248, 45));
                    g2.fillRoundRect(0, 0, w, h, arc, arc);
                    g2.setColor(new Color(56, 189, 248, 100));
                    g2.drawRoundRect(0, 0, w - 1, h - 1, arc, arc);
                }
                g2.dispose();
                super.paintComponent(g);
            }
        };
        userRole.setFont(new Font("Segoe UI", Font.BOLD, 10));
        userRole.setForeground(isAdmin() ? new Color(216, 180, 254) : new Color(186, 230, 253));
        userRole.setBorder(new EmptyBorder(1, 8, 1, 8));
        userRole.setOpaque(false);

        userBlock.add(userName);
        userBlock.add(Box.createVerticalStrut(2));
        userBlock.add(userRole);

        // Modern Ghost Logout Button
        JButton logout = new JButton("Log out") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);

                int w = getWidth();
                int h = getHeight();
                int arc = 8;

                if (getModel().isRollover()) {
                    g2.setColor(new Color(239, 68, 68, 50));
                    g2.fillRoundRect(0, 0, w, h, arc, arc);
                    g2.setColor(new Color(239, 68, 68));
                    g2.drawRoundRect(0, 0, w - 1, h - 1, arc, arc);
                    g2.setColor(new Color(254, 202, 202));
                } else {
                    g2.setColor(new Color(255, 255, 255, 15));
                    g2.fillRoundRect(0, 0, w, h, arc, arc);
                    g2.setColor(new Color(255, 255, 255, 40));
                    g2.drawRoundRect(0, 0, w - 1, h - 1, arc, arc);
                    g2.setColor(new Color(226, 232, 240));
                }

                FontMetrics fm = g2.getFontMetrics(getFont());
                int tx = (w - fm.stringWidth(getText())) / 2;
                int ty = (h - fm.getHeight()) / 2 + fm.getAscent();
                g2.setFont(getFont());
                g2.drawString(getText(), tx, ty);

                g2.dispose();
            }
        };
        logout.setFont(new Font("Segoe UI", Font.BOLD, 12));
        logout.setPreferredSize(new Dimension(80, 32));
        logout.setFocusPainted(false);
        logout.setBorderPainted(false);
        logout.setContentAreaFilled(false);
        logout.setOpaque(false);
        logout.setCursor(new Cursor(Cursor.HAND_CURSOR));
        logout.addActionListener(e -> handleLogout());

        account.add(date);
        account.add(avatar);
        account.add(userBlock);
        account.add(logout);

        header.add(branding, BorderLayout.WEST);
        header.add(account, BorderLayout.EAST);
        return header;
    }

    private JComponent createNavigationBar() {
        JPanel navItems = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 6));
        navItems.setBackground(UIUtils.COLOR_NAV_BG);
        navItems.setBorder(new EmptyBorder(2, 18, 6, 18));

        addNavButton(navItems, "Dashboard", "Dashboard", CARD_DASHBOARD);
        addNavButton(navItems, "Students", "Students", CARD_STUDENTS);
        addNavButton(navItems, "Rooms", "Rooms", CARD_ROOMS);
        addNavButton(navItems, "Allocations", "Allocations", CARD_ALLOCATIONS);
        addNavButton(navItems, "Fees & Payments", "Fees & Payments", CARD_FEES);
        addNavButton(navItems, "Visitors", "Visitors", CARD_VISITORS);
        addNavButton(navItems, "Complaints", "Complaints", CARD_COMPLAINTS);
        if (isAdmin()) {
            addNavButton(navItems, "Activity Logs", "Activity Logs", CARD_LOGS);
        }

        JScrollPane navScroll = new JScrollPane(navItems,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        navScroll.setBorder(BorderFactory.createMatteBorder(1, 0, 1, 0, new Color(30, 41, 59)));
        navScroll.getViewport().setBackground(UIUtils.COLOR_NAV_BG);
        navScroll.setPreferredSize(new Dimension(100, 48));
        navScroll.getHorizontalScrollBar().setUnitIncrement(24);
        return navScroll;
    }

    private void addNavButton(JPanel container, String baseName, String displayText, String cardName) {
        JButton button = new JButton(displayText) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);

                int w = getWidth();
                int h = getHeight();
                int arc = 8;
                boolean active = cardName.equals(getActiveCard());

                if (active) {
                    GradientPaint gp = new GradientPaint(0, 0, UIUtils.COLOR_PRIMARY, 0, h, new Color(67, 56, 202));
                    g2.setPaint(gp);
                    g2.fillRoundRect(0, 0, w, h, arc, arc);

                    g2.setColor(new Color(99, 102, 241));
                    g2.setStroke(new BasicStroke(1.0f));
                    g2.drawRoundRect(0, 0, w - 1, h - 1, arc, arc);

                    g2.setColor(Color.WHITE);
                } else if (getModel().isRollover()) {
                    g2.setColor(new Color(30, 41, 59));
                    g2.fillRoundRect(0, 0, w, h, arc, arc);
                    g2.setColor(Color.WHITE);
                } else {
                    g2.setColor(new Color(148, 163, 184)); // Slate 400
                }

                FontMetrics fm = g2.getFontMetrics(getFont());
                int tx = (w - fm.stringWidth(getText())) / 2;
                int ty = (h - fm.getHeight()) / 2 + fm.getAscent();
                g2.setFont(getFont());
                g2.drawString(getText(), tx, ty);

                g2.dispose();
            }
        };

        button.setFont(new Font("Segoe UI", Font.BOLD, 12));
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setOpaque(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setPreferredSize(new Dimension(displayText.length() > 16 ? 156 : 132, 34));
        button.addActionListener(e -> setActiveCard(cardName));

        navButtons.put(cardName, button);
        container.add(button);
    }

    private String activeCard = CARD_DASHBOARD;

    private String getActiveCard() {
        return activeCard;
    }

    private void setActiveCard(String cardName) {
        if (CARD_LOGS.equals(cardName) && !RolePermissions.canViewActivityLogs(currentUser.getRole())) {
            throw new SecurityException("Activity logs are available to administrators only.");
        }
        this.activeCard = cardName;
        for (JButton btn : navButtons.values()) {
            btn.repaint();
        }

        switch (cardName) {
            case CARD_DASHBOARD -> dashboardPanel.refreshData();
            case CARD_STUDENTS -> studentPanel.loadStudentData();
            case CARD_ROOMS -> roomPanel.loadRoomData();
            case CARD_ALLOCATIONS -> {
                allocationPanel.reloadDropdowns();
                allocationPanel.loadAllocationData();
            }
            case CARD_FEES -> {
                feePanel.reloadStudentDropdown();
                feePanel.loadFeeData();
            }
            case CARD_VISITORS -> {
                visitorPanel.reloadStudentDropdown();
                visitorPanel.loadVisitorData();
            }
            case CARD_COMPLAINTS -> {
                complaintPanel.reloadStudentDropdown();
                complaintPanel.loadComplaintData();
            }
            case CARD_LOGS -> {
                if (activityLogPanel != null) {
                    activityLogPanel.loadDropdownOptions();
                    activityLogPanel.loadLogData();
                }
            }
            default -> throw new IllegalArgumentException("Unknown application screen: " + cardName);
        }
        cardLayout.show(mainContentCards, cardName);
    }

    private boolean isAdmin() {
        return currentUser != null && RolePermissions.canViewActivityLogs(currentUser.getRole());
    }

    private void handleLogout() {
        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to log out of your session?",
                "Confirm Logout",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);
        if (confirm == JOptionPane.YES_OPTION) {
            new LoginFrame().setVisible(true);
            dispose();
        }
    }
}

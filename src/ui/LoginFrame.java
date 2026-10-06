package ui;

import dao.UserDAO;
import database.DBConnection;
import model.User;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

/**
 * LoginFrame - Authentication window for Hostel Management System.
 */
public class LoginFrame extends JFrame {

    private JTextField txtUsername;
    private JPasswordField txtPassword;
    private JButton btnLogin;
    private JButton btnExit;
    private final UserDAO userDAO;

    public LoginFrame() {
        this.userDAO = new UserDAO();
        initComponents();
    }

    private void initComponents() {
        setTitle("Hostel Management System - Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(460, 440);
        setLocationRelativeTo(null);
        setResizable(false);
        getContentPane().setBackground(UIUtils.COLOR_BG);
        setLayout(new BorderLayout());

        // Header Panel
        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(UIUtils.COLOR_HEADER);
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
        headerPanel.setBorder(new EmptyBorder(22, 20, 20, 20));

        JLabel lblTitle = new JLabel("HOSTEL MANAGEMENT SYSTEM");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTitle.setForeground(Color.WHITE);
        lblTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblSub = new JLabel("Staff & Warden Portal");
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblSub.setForeground(new Color(148, 163, 184));
        lblSub.setAlignmentX(Component.CENTER_ALIGNMENT);

        headerPanel.add(lblTitle);
        headerPanel.add(Box.createVerticalStrut(5));
        headerPanel.add(lblSub);
        add(headerPanel, BorderLayout.NORTH);

        // Center Card Panel
        JPanel centerWrapper = new JPanel(new GridBagLayout());
        centerWrapper.setBackground(UIUtils.COLOR_BG);

        JPanel cardPanel = UIUtils.createCardPanel();
        cardPanel.setPreferredSize(new Dimension(380, 250));
        cardPanel.setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 10, 8, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Username
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.3;
        JLabel lblUser = UIUtils.createFormLabel("Username:");
        cardPanel.add(lblUser, gbc);

        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.weightx = 0.7;
        txtUsername = new JTextField(15);
        txtUsername.setFont(UIUtils.FONT_REGULAR);
        txtUsername.setText("admin"); // Default convenience
        cardPanel.add(txtUsername, gbc);

        // Password
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0.3;
        JLabel lblPass = UIUtils.createFormLabel("Password:");
        cardPanel.add(lblPass, gbc);

        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.weightx = 0.7;
        txtPassword = new JPasswordField(15);
        txtPassword.setFont(UIUtils.FONT_REGULAR);
        txtPassword.setText("admin123"); // Default convenience
        cardPanel.add(txtPassword, gbc);

        // Buttons Panel
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(18, 10, 8, 10);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        btnPanel.setOpaque(false);

        btnLogin = UIUtils.createPrimaryButton("  Login  ");
        btnExit = UIUtils.createSecondaryButton("  Exit  ");

        btnPanel.add(btnLogin);
        btnPanel.add(btnExit);
        cardPanel.add(btnPanel, gbc);

        // Demo Hint
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(10, 10, 0, 10);
        JLabel lblHint = new JLabel("Default Demo: admin / admin123", SwingConstants.CENTER);
        lblHint.setFont(UIUtils.FONT_SMALL);
        lblHint.setForeground(UIUtils.COLOR_TEXT_MUTED);
        cardPanel.add(lblHint, gbc);

        centerWrapper.add(cardPanel);
        add(centerWrapper, BorderLayout.CENTER);

        // Footer status info
        JPanel footerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        footerPanel.setBackground(UIUtils.COLOR_BG);
        JLabel lblFooter = new JLabel("Connected DB: " + DBConnection.getDbName());
        lblFooter.setFont(UIUtils.FONT_SMALL);
        lblFooter.setForeground(UIUtils.COLOR_TEXT_MUTED);
        footerPanel.add(lblFooter);
        add(footerPanel, BorderLayout.SOUTH);

        // Event Listeners
        btnLogin.addActionListener(this::handleLogin);
        btnExit.addActionListener(e -> System.exit(0));

        // Enter key listeners
        KeyAdapter enterListener = new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    handleLogin(null);
                }
            }
        };
        txtUsername.addKeyListener(enterListener);
        txtPassword.addKeyListener(enterListener);
    }

    private void handleLogin(ActionEvent e) {
        String username = txtUsername.getText().trim();
        String password = new String(txtPassword.getPassword()).trim();

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "Please enter both Username and Password.", 
                "Validation Error", 
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Test DB connection first to give clear feedback if MySQL is not reachable
        if (!DBConnection.testConnection()) {
            JOptionPane.showMessageDialog(this,
                "Unable to connect to MySQL database!\n\n" +
                "Please ensure that:\n" +
                "1. MySQL Server is running on port 3306.\n" +
                "2. The database 'hostel_management' was created using database.sql.\n" +
                "3. Username & Password in DBConnection.java match your MySQL configuration.",
                "Database Connection Error",
                JOptionPane.ERROR_MESSAGE);
            return;
        }

        User user = userDAO.authenticate(username, password);
        if (user != null) {
            JOptionPane.showMessageDialog(this, 
                "Welcome, " + user.getFullName() + "!", 
                "Login Successful", 
                JOptionPane.INFORMATION_MESSAGE);
            
            // Launch main dashboard
            DashboardFrame dashboard = new DashboardFrame(user);
            dashboard.setVisible(true);
            this.dispose();
        } else {
            JOptionPane.showMessageDialog(this, 
                "Invalid Username or Password. Please try again.", 
                "Login Failed", 
                JOptionPane.ERROR_MESSAGE);
            txtPassword.setText("");
            txtPassword.requestFocus();
        }
    }
}

package ui;

import dao.StudentAccountDAO;
import database.DBConnection;
import model.Student;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.security.GeneralSecurityException;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;

/**
 * Student account sign-in and first-time account activation.
 */
public class StudentLoginFrame extends JFrame {

    private final StudentAccountDAO accountDAO = new StudentAccountDAO();
    private final JTextField loginRollNo = new JTextField();
    private final JPasswordField loginPassword = new JPasswordField();
    private final JTextField setupRollNo = new JTextField();
    private final JTextField setupPhone = new JTextField();
    private final JPasswordField setupPassword = new JPasswordField();
    private final JPasswordField confirmPassword = new JPasswordField();
    private final JLabel loginStatus = new JLabel(" ");
    private final JLabel setupStatus = new JLabel(" ");
    private JButton signInButton;
    private JButton createAccountButton;

    public StudentLoginFrame() {
        setTitle("Hostel Management System - Student Portal");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(520, 510);
        setMinimumSize(new Dimension(480, 470));
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(16, 16));
        getContentPane().setBackground(UIUtils.COLOR_BG);

        JLabel heading = new JLabel("Student Portal", SwingConstants.CENTER);
        heading.setFont(UIUtils.FONT_TITLE);
        heading.setForeground(UIUtils.COLOR_HEADER);
        heading.setBorder(new EmptyBorder(20, 16, 0, 16));
        add(heading, BorderLayout.NORTH);

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Sign in", createLoginPanel());
        tabs.addTab("Create account", createSetupPanel());
        add(tabs, BorderLayout.CENTER);

        JLabel footer = new JLabel("Students can access only their own hostel records.", SwingConstants.CENTER);
        footer.setFont(UIUtils.FONT_SMALL);
        footer.setForeground(UIUtils.COLOR_TEXT_MUTED);
        footer.setBorder(new EmptyBorder(0, 12, 14, 12));
        add(footer, BorderLayout.SOUTH);
        getRootPane().setDefaultButton(signInButton);
    }

    private JPanel createLoginPanel() {
        JPanel panel = formPanel();
        addField(panel, "Roll number", loginRollNo);
        addField(panel, "Password", loginPassword);
        loginStatus.setForeground(UIUtils.COLOR_DANGER);
        panel.add(loginStatus);
        signInButton = UIUtils.createPrimaryButton("Sign in");
        signInButton.addActionListener(event -> signIn());
        panel.add(signInButton);
        loginRollNo.addActionListener(event -> loginPassword.requestFocusInWindow());
        loginPassword.addActionListener(event -> signIn());
        return panel;
    }

    private JPanel createSetupPanel() {
        JPanel panel = formPanel();
        addField(panel, "Enrolled roll number", setupRollNo);
        addField(panel, "Phone number on student record", setupPhone);
        addField(panel, "Create password (at least 8 characters)", setupPassword);
        addField(panel, "Confirm password", confirmPassword);
        setupStatus.setForeground(UIUtils.COLOR_DANGER);
        panel.add(setupStatus);
        createAccountButton = UIUtils.createPrimaryButton("Create student account");
        createAccountButton.addActionListener(event -> createAccount());
        panel.add(createAccountButton);
        return panel;
    }

    private JPanel formPanel() {
        JPanel panel = new JPanel(new GridLayout(0, 1, 0, 8));
        panel.setBackground(UIUtils.COLOR_BG);
        panel.setBorder(new EmptyBorder(20, 32, 24, 32));
        return panel;
    }

    private void addField(JPanel panel, String labelText, JComponent field) {
        JLabel label = new JLabel(labelText);
        label.setFont(UIUtils.FONT_BOLD);
        label.setForeground(UIUtils.COLOR_TEXT_PRIMARY);
        panel.add(label);
        field.setPreferredSize(new Dimension(300, 36));
        panel.add(field);
    }

    private void signIn() {
        String rollNo = loginRollNo.getText().trim();
        char[] password = loginPassword.getPassword();
        if (rollNo.isEmpty() || password.length == 0) {
            Arrays.fill(password, '\0');
            loginStatus.setText("Enter your roll number and password.");
            return;
        }
        setBusy(signInButton, true, "Signing in...");
        loginStatus.setText(" ");
        new SwingWorker<Student, Void>() {
            @Override
            protected Student doInBackground() throws SQLException, GeneralSecurityException {
                try {
                    if (!DBConnection.testConnection()) {
                        throw new SQLException("The database server is unreachable.");
                    }
                    return accountDAO.authenticate(rollNo, password);
                } finally {
                    Arrays.fill(password, '\0');
                }
            }

            @Override
            protected void done() {
                Arrays.fill(password, '\0');
                loginPassword.setText("");
                setBusy(signInButton, false, "Sign in");
                try {
                    Student student = get();
                    if (student == null) {
                        loginStatus.setText("Invalid credentials or no student account exists.");
                        return;
                    }
                    new StudentPortalFrame(student).setVisible(true);
                    dispose();
                } catch (InterruptedException error) {
                    Thread.currentThread().interrupt();
                    loginStatus.setText("Sign in was interrupted. Please try again.");
                } catch (ExecutionException error) {
                    showFailure(loginStatus, error.getCause());
                }
            }
        }.execute();
    }

    private void createAccount() {
        String rollNo = setupRollNo.getText().trim();
        String phone = setupPhone.getText().trim();
        char[] password = setupPassword.getPassword();
        char[] confirmation = confirmPassword.getPassword();
        if (rollNo.isEmpty() || phone.isEmpty() || password.length == 0) {
            setupStatus.setText("Complete all fields.");
            clear(password, confirmation);
            return;
        }
        if (password.length < 8) {
            setupStatus.setText("Use a password with at least 8 characters.");
            clear(password, confirmation);
            return;
        }
        if (!Arrays.equals(password, confirmation)) {
            setupStatus.setText("The passwords do not match.");
            clear(password, confirmation);
            return;
        }
        clear(confirmation);
        setBusy(createAccountButton, true, "Creating account...");
        setupStatus.setText(" ");
        new SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() throws SQLException, GeneralSecurityException {
                try {
                    if (!DBConnection.testConnection()) {
                        throw new SQLException("The database server is unreachable.");
                    }
                    return accountDAO.createAccount(rollNo, phone, password);
                } finally {
                    clear(password);
                }
            }

            @Override
            protected void done() {
                clear(password);
                setupPassword.setText("");
                confirmPassword.setText("");
                setBusy(createAccountButton, false, "Create student account");
                try {
                    if (get()) {
                        setupStatus.setForeground(UIUtils.COLOR_SUCCESS);
                        setupStatus.setText("Account created. You can now sign in.");
                    } else {
                        setupStatus.setForeground(UIUtils.COLOR_DANGER);
                        setupStatus.setText("Details do not match an enrolled student or account exists.");
                    }
                } catch (InterruptedException error) {
                    Thread.currentThread().interrupt();
                    showFailure(setupStatus, error);
                } catch (ExecutionException error) {
                    showFailure(setupStatus, error.getCause());
                }
            }
        }.execute();
    }

    private void showFailure(JLabel label, Throwable error) {
        System.err.println("Student portal authentication failed: " + error.getMessage());
        label.setForeground(UIUtils.COLOR_DANGER);
        label.setText("Unable to connect. Check the database and try again.");
    }

    private void setBusy(JButton button, boolean busy, String text) {
        button.setEnabled(!busy);
        button.setText(text);
    }

    private void clear(char[]... values) {
        for (char[] value : values) {
            Arrays.fill(value, '\0');
        }
    }

}

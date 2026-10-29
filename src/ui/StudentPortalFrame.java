package ui;

import dao.ComplaintDAO;
import dao.StudentAccountDAO;
import dao.StudentPortalDAO;
import dao.StudentPortalDAO.PortalData;
import model.Complaint;
import model.Fee;
import model.Student;
import model.Visitor;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.security.GeneralSecurityException;
import java.sql.Date;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;

/**
 * Read-only personal records and complaint submission for a single authenticated student.
 */
public class StudentPortalFrame extends JFrame {

    private final Student student;
    private final StudentPortalDAO portalDAO = new StudentPortalDAO();
    private final ComplaintDAO complaintDAO = new ComplaintDAO();
    private final StudentAccountDAO accountDAO = new StudentAccountDAO();
    private final JLabel status = new JLabel(" ");
    private final JLabel studentDetails = new JLabel();
    private final DefaultTableModel feeModel = tableModel("Date", "Amount", "Status", "Mode", "Remarks");
    private final DefaultTableModel complaintModel = tableModel("Date", "Category", "Description", "Status");
    private final DefaultTableModel visitorModel = tableModel(
            "Date", "Visitor", "Relation", "Phone", "In time", "Out time");
    private final JComboBox<String> complaintType = new JComboBox<>(new String[]{
            "Electrical", "Plumbing", "Cleanliness", "Food/Mess", "Furniture", "Internet", "Other"
    });
    private final JTextArea complaintDescription = new JTextArea(3, 32);
    private JButton refreshButton;
    private JButton submitComplaintButton;

    public StudentPortalFrame(Student student) {
        this.student = student;
        setTitle("Hostel Student Portal - " + student.getName());
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1100, 760);
        setMinimumSize(new java.awt.Dimension(850, 600));
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(0, 12));
        getContentPane().setBackground(UIUtils.COLOR_BG);

        add(createHeader(), BorderLayout.NORTH);
        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("My details", createDetailsPanel());
        tabs.addTab("Fee records", createTablePanel(feeModel));
        tabs.addTab("My complaints", createComplaintsPanel());
        tabs.addTab("Visitor records", createTablePanel(visitorModel));
        add(tabs, BorderLayout.CENTER);

        status.setForeground(UIUtils.COLOR_TEXT_MUTED);
        status.setBorder(new EmptyBorder(0, 16, 8, 16));
        add(status, BorderLayout.SOUTH);
        refreshData();
    }

    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(UIUtils.COLOR_HEADER);
        header.setBorder(new EmptyBorder(16, 22, 16, 22));

        JPanel titleBlock = new JPanel(new GridLayout(0, 1, 0, 4));
        titleBlock.setOpaque(false);
        JLabel title = new JLabel("STUDENT PORTAL");
        title.setFont(UIUtils.FONT_SUBTITLE);
        title.setForeground(java.awt.Color.WHITE);
        JLabel welcome = new JLabel("Welcome, " + student.getName() + "  |  " + student.getRollNo());
        welcome.setFont(UIUtils.FONT_REGULAR);
        welcome.setForeground(new java.awt.Color(203, 213, 225));
        titleBlock.add(title);
        titleBlock.add(welcome);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actions.setOpaque(false);
        refreshButton = UIUtils.createPrimaryButton("Refresh");
        refreshButton.addActionListener(event -> refreshData());
        JButton changePassword = UIUtils.createSecondaryButton("Change password");
        changePassword.addActionListener(event -> changePassword());
        JButton logout = UIUtils.createSecondaryButton("Log out");
        logout.addActionListener(event -> {
            new LoginFrame().setVisible(true);
            dispose();
        });
        actions.add(refreshButton);
        actions.add(changePassword);
        actions.add(logout);
        header.add(titleBlock, BorderLayout.WEST);
        header.add(actions, BorderLayout.EAST);
        return header;
    }

    private JPanel createDetailsPanel() {
        JPanel panel = UIUtils.createCardPanel();
        panel.setLayout(new BorderLayout(12, 12));
        panel.setBorder(new EmptyBorder(24, 28, 24, 28));
        JLabel heading = new JLabel("Your enrolled details");
        heading.setFont(UIUtils.FONT_TITLE);
        heading.setForeground(UIUtils.COLOR_PRIMARY);
        studentDetails.setFont(UIUtils.FONT_REGULAR);
        studentDetails.setForeground(UIUtils.COLOR_TEXT_PRIMARY);
        panel.add(heading, BorderLayout.NORTH);
        panel.add(studentDetails, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createComplaintsPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(UIUtils.COLOR_BG);
        panel.setBorder(new EmptyBorder(16, 16, 16, 16));

        JPanel form = UIUtils.createCardPanel();
        form.setLayout(new BorderLayout(10, 8));
        form.setBorder(new EmptyBorder(14, 16, 14, 16));
        JPanel formTop = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        formTop.setOpaque(false);
        JLabel categoryLabel = new JLabel("Category:");
        categoryLabel.setFont(UIUtils.FONT_BOLD);
        formTop.add(categoryLabel);
        formTop.add(complaintType);
        form.add(formTop, BorderLayout.NORTH);
        complaintDescription.setLineWrap(true);
        complaintDescription.setWrapStyleWord(true);
        complaintDescription.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UIUtils.COLOR_BORDER),
                new EmptyBorder(7, 8, 7, 8)));
        form.add(new JScrollPane(complaintDescription), BorderLayout.CENTER);
        submitComplaintButton = UIUtils.createPrimaryButton("Submit complaint");
        submitComplaintButton.addActionListener(event -> submitComplaint());
        form.add(submitComplaintButton, BorderLayout.EAST);
        panel.add(form, BorderLayout.NORTH);
        panel.add(createTablePanel(complaintModel), BorderLayout.CENTER);
        return panel;
    }

    private JPanel createTablePanel(DefaultTableModel model) {
        JPanel panel = UIUtils.createCardPanel();
        panel.setLayout(new BorderLayout());
        panel.setBorder(new EmptyBorder(12, 14, 12, 14));
        JTable table = new JTable(model);
        table.setAutoCreateRowSorter(true);
        table.setFillsViewportHeight(true);
        table.setRowHeight(28);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        return panel;
    }

    private static DefaultTableModel tableModel(String... columns) {
        return new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
    }

    private void refreshData() {
        refreshButton.setEnabled(false);
        status.setForeground(UIUtils.COLOR_TEXT_MUTED);
        status.setText("Loading your records...");
        new SwingWorker<PortalData, Void>() {
            @Override
            protected PortalData doInBackground() throws SQLException {
                return portalDAO.loadPortalData(student.getStudentId());
            }

            @Override
            protected void done() {
                refreshButton.setEnabled(true);
                try {
                    applyPortalData(get());
                    status.setText("Your records are up to date.");
                } catch (InterruptedException error) {
                    Thread.currentThread().interrupt();
                    showFailure(error);
                } catch (ExecutionException error) {
                    showFailure(error.getCause());
                }
            }
        }.execute();
    }

    private void applyPortalData(PortalData data) {
        Student details = data.student();
        studentDetails.setText("<html><div style='line-height:1.8'>"
                + "<b>Name:</b> " + escape(details.getName()) + "<br>"
                + "<b>Roll number:</b> " + escape(details.getRollNo()) + "<br>"
                + "<b>Course:</b> " + escape(details.getCourse()) + "<br>"
                + "<b>Academic year:</b> " + escape(details.getYear()) + "<br>"
                + "<b>Gender:</b> " + escape(details.getGender()) + "<br>"
                + "<b>Phone:</b> " + escape(details.getPhone()) + "<br>"
                + "<b>Email:</b> " + escape(details.getEmail()) + "<br>"
                + "<b>Room:</b> " + escape(data.roomNumber()) + "</div></html>");

        feeModel.setRowCount(0);
        for (Fee fee : data.fees()) {
            feeModel.addRow(new Object[]{fee.getPaymentDate(), UIUtils.formatCurrency(fee.getAmount()),
                    fee.getPaymentStatus(), fee.getPaymentMode(), fee.getRemarks()});
        }
        complaintModel.setRowCount(0);
        for (Complaint complaint : data.complaints()) {
            complaintModel.addRow(new Object[]{complaint.getComplaintDate(), complaint.getComplaintType(),
                    complaint.getDescription(), complaint.getStatus()});
        }
        visitorModel.setRowCount(0);
        for (Visitor visitor : data.visitors()) {
            visitorModel.addRow(new Object[]{visitor.getVisitDate(), visitor.getVisitorName(),
                    visitor.getRelation(), visitor.getPhone(), visitor.getInTime(),
                    visitor.getOutTime() == null ? "In hostel" : visitor.getOutTime()});
        }
    }

    private void submitComplaint() {
        String description = complaintDescription.getText().trim();
        if (description.isEmpty()) {
            status.setForeground(UIUtils.COLOR_DANGER);
            status.setText("Enter a description before submitting your complaint.");
            return;
        }
        submitComplaintButton.setEnabled(false);
        status.setForeground(UIUtils.COLOR_TEXT_MUTED);
        status.setText("Submitting your complaint...");
        Complaint complaint = new Complaint(student.getStudentId(),
                (String) complaintType.getSelectedItem(), description,
                Date.valueOf(LocalDate.now()), "Pending");
        new SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() throws SQLException {
                return complaintDAO.addComplaint(complaint);
            }

            @Override
            protected void done() {
                submitComplaintButton.setEnabled(true);
                try {
                    if (!get()) {
                        status.setForeground(UIUtils.COLOR_DANGER);
                        status.setText("The complaint was not saved. Please try again.");
                        return;
                    }
                    complaintDescription.setText("");
                    status.setForeground(UIUtils.COLOR_SUCCESS);
                    status.setText("Complaint submitted.");
                    refreshData();
                } catch (InterruptedException error) {
                    Thread.currentThread().interrupt();
                    showFailure(error);
                } catch (ExecutionException error) {
                    showFailure(error.getCause());
                }
            }
        }.execute();
    }

    private void changePassword() {
        JPasswordField current = new JPasswordField();
        JPasswordField next = new JPasswordField();
        JPasswordField confirmation = new JPasswordField();
        JPanel fields = new JPanel(new GridLayout(0, 1, 0, 6));
        fields.add(new JLabel("Current password"));
        fields.add(current);
        fields.add(new JLabel("New password (at least 8 characters)"));
        fields.add(next);
        fields.add(new JLabel("Confirm new password"));
        fields.add(confirmation);
        int result = JOptionPane.showConfirmDialog(this, fields, "Change student password",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result != JOptionPane.OK_OPTION) {
            clear(current.getPassword(), next.getPassword(), confirmation.getPassword());
            return;
        }

        char[] oldPassword = current.getPassword();
        char[] newPassword = next.getPassword();
        char[] confirmPassword = confirmation.getPassword();
        if (newPassword.length < 8 || !Arrays.equals(newPassword, confirmPassword)) {
            clear(oldPassword, newPassword, confirmPassword);
            JOptionPane.showMessageDialog(this, "Use at least 8 characters and make sure passwords match.",
                    "Invalid password", JOptionPane.ERROR_MESSAGE);
            return;
        }
        clear(confirmPassword);
        setPasswordBusy(true);
        new SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() throws SQLException, GeneralSecurityException {
                try {
                    return accountDAO.changePassword(student.getStudentId(), oldPassword, newPassword);
                } finally {
                    clear(oldPassword, newPassword);
                }
            }

            @Override
            protected void done() {
                clear(oldPassword, newPassword);
                setPasswordBusy(false);
                try {
                    if (get()) {
                        JOptionPane.showMessageDialog(StudentPortalFrame.this,
                                "Your password was changed.", "Password updated",
                                JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        JOptionPane.showMessageDialog(StudentPortalFrame.this,
                                "The current password is incorrect.", "Password not changed",
                                JOptionPane.ERROR_MESSAGE);
                    }
                } catch (InterruptedException error) {
                    Thread.currentThread().interrupt();
                    showFailure(error);
                } catch (ExecutionException error) {
                    showFailure(error.getCause());
                }
            }
        }.execute();
    }

    private void setPasswordBusy(boolean busy) {
        refreshButton.setEnabled(!busy);
        submitComplaintButton.setEnabled(!busy);
    }

    private void showFailure(Throwable error) {
        System.err.println("Student portal request failed: " + error.getMessage());
        status.setForeground(UIUtils.COLOR_DANGER);
        status.setText("Unable to load or save data. Check the database and try again.");
    }

    private String escape(String text) {
        return text == null ? "" : text.replace("&", "&amp;")
                .replace("<", "&lt;").replace(">", "&gt;");
    }

    private void clear(char[]... values) {
        for (char[] value : values) {
            Arrays.fill(value, '\0');
        }
    }
}

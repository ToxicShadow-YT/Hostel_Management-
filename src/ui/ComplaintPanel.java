package ui;

import dao.ActivityLogDAO;
import dao.ComplaintDAO;
import dao.StudentDAO;
import model.Complaint;
import model.Student;
import model.User;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Date;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

/**
 * ComplaintPanel - Student Grievance & Maintenance Tracking.
 */
public class ComplaintPanel extends JPanel {

    private final ComplaintDAO complaintDAO;
    private final StudentDAO studentDAO;
    private final ActivityLogDAO logDAO;
    private final User currentUser;

    // Form
    private JTextField txtComplaintId;
    private JComboBox<StudentItem> cmbStudents;
    private JComboBox<String> cmbComplaintType;
    private JTextArea txtDescription;
    private JTextField txtComplaintDate;
    private JComboBox<String> cmbStatus;

    // Buttons
    private JButton btnAddComplaint;
    private JButton btnUpdateStatus;
    private JButton btnDeleteComplaint;
    private JButton btnClear;

    // Search and Table
    private JTextField txtSearch;
    private JButton btnSearch;
    private JButton btnResetSearch;
    private JComboBox<String> cmbFilterStatus;
    private JTable complaintTable;
    private DefaultTableModel tableModel;

    public static class StudentItem {
        private final int id;
        private final String rollNo;
        private final String name;

        public StudentItem(int id, String rollNo, String name) {
            this.id = id;
            this.rollNo = rollNo;
            this.name = name;
        }

        public int getId() { return id; }
        public String getRollNo() { return rollNo; }
        public String getName() { return name; }

        @Override
        public String toString() {
            return rollNo + " - " + name;
        }
    }

    public ComplaintPanel(User currentUser) {
        this.currentUser = currentUser;
        this.complaintDAO = new ComplaintDAO();
        this.studentDAO = new StudentDAO();
        this.logDAO = new ActivityLogDAO();

        initComponents();
        reloadStudentDropdown();
        loadComplaintData();
    }

    public ComplaintPanel() {
        this(null);
    }

    private void initComponents() {
        setLayout(new BorderLayout(15, 15));
        setBackground(UIUtils.COLOR_BG);
        setBorder(new EmptyBorder(15, 20, 15, 20));

        // Top Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        headerPanel.add(UIUtils.createHeaderLabel("Hostel Grievance & Complaint Management"), BorderLayout.WEST);
        add(headerPanel, BorderLayout.NORTH);

        // Split: Left = Form, Right = Table + Search
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setDividerLocation(380);
        splitPane.setContinuousLayout(true);
        splitPane.setOpaque(false);
        splitPane.setBorder(null);

        // Left Form Panel
        JPanel formCard = UIUtils.createCardPanel();
        formCard.setLayout(new BorderLayout(0, 10));

        JLabel lblFormTitle = new JLabel("Complaint Registration Form");
        lblFormTitle.setFont(UIUtils.FONT_SUBTITLE);
        lblFormTitle.setForeground(UIUtils.COLOR_PRIMARY);
        formCard.add(lblFormTitle, BorderLayout.NORTH);

        JPanel fieldsPanel = new JPanel(new GridBagLayout());
        fieldsPanel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 5, 4, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        int row = 0;

        // Complaint ID
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.35;
        fieldsPanel.add(UIUtils.createFormLabel("Complaint ID:"), gbc);
        gbc.gridx = 1; gbc.gridy = row++; gbc.weightx = 0.65;
        txtComplaintId = new JTextField();
        txtComplaintId.setEditable(false);
        txtComplaintId.setBackground(new Color(241, 245, 249));
        fieldsPanel.add(txtComplaintId, gbc);

        // Student Dropdown
        gbc.gridx = 0; gbc.gridy = row;
        fieldsPanel.add(UIUtils.createFormLabel("Student * :"), gbc);
        gbc.gridx = 1; gbc.gridy = row++;
        cmbStudents = new JComboBox<>();
        fieldsPanel.add(cmbStudents, gbc);

        // Complaint Type
        gbc.gridx = 0; gbc.gridy = row;
        fieldsPanel.add(UIUtils.createFormLabel("Category * :"), gbc);
        gbc.gridx = 1; gbc.gridy = row++;
        cmbComplaintType = new JComboBox<>(new String[]{
            "Electrical",
            "Plumbing",
            "Cleanliness",
            "Food/Mess",
            "Furniture",
            "Internet",
            "Security",
            "Other"
        });
        fieldsPanel.add(cmbComplaintType, gbc);

        // Description
        gbc.gridx = 0; gbc.gridy = row;
        fieldsPanel.add(UIUtils.createFormLabel("Description * :"), gbc);
        gbc.gridx = 1; gbc.gridy = row++;
        txtDescription = new JTextArea(4, 15);
        txtDescription.setLineWrap(true);
        txtDescription.setWrapStyleWord(true);
        txtDescription.setFont(UIUtils.FONT_REGULAR);
        JScrollPane scrollDesc = new JScrollPane(txtDescription);
        fieldsPanel.add(scrollDesc, gbc);

        // Date
        gbc.gridx = 0; gbc.gridy = row;
        fieldsPanel.add(UIUtils.createFormLabel("Date * :"), gbc);
        gbc.gridx = 1; gbc.gridy = row++;
        txtComplaintDate = new JTextField(LocalDate.now().toString());
        fieldsPanel.add(txtComplaintDate, gbc);

        // Status
        gbc.gridx = 0; gbc.gridy = row;
        fieldsPanel.add(UIUtils.createFormLabel("Status * :"), gbc);
        gbc.gridx = 1; gbc.gridy = row++;
        cmbStatus = new JComboBox<>(new String[]{"Pending", "In Progress", "Resolved"});
        fieldsPanel.add(cmbStatus, gbc);

        formCard.add(new JScrollPane(fieldsPanel), BorderLayout.CENTER);

        // Form Buttons
        JPanel actionBtnPanel = new JPanel(new GridLayout(2, 2, 8, 8));
        actionBtnPanel.setOpaque(false);
        actionBtnPanel.setBorder(new EmptyBorder(8, 0, 0, 0));

        btnAddComplaint = UIUtils.createPrimaryButton("Register");
        btnUpdateStatus = UIUtils.createSuccessButton("Update Status");
        btnDeleteComplaint = UIUtils.createDangerButton("Delete");
        btnClear = UIUtils.createSecondaryButton("Clear Form");

        actionBtnPanel.add(btnAddComplaint);
        actionBtnPanel.add(btnUpdateStatus);
        actionBtnPanel.add(btnDeleteComplaint);
        actionBtnPanel.add(btnClear);

        formCard.add(actionBtnPanel, BorderLayout.SOUTH);
        splitPane.setLeftComponent(formCard);

        // Right Table & Search
        JPanel rightPanel = UIUtils.createCardPanel();
        rightPanel.setLayout(new BorderLayout(0, 10));

        // Search Bar & Filter
        JPanel topFilterBar = new JPanel(new BorderLayout(8, 0));
        topFilterBar.setOpaque(false);
        topFilterBar.setBorder(new EmptyBorder(0, 0, 8, 0));

        JPanel searchBox = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        searchBox.setOpaque(false);
        searchBox.add(new JLabel("Search:"));
        txtSearch = new JTextField(12);
        searchBox.add(txtSearch);
        btnSearch = UIUtils.createPrimaryButton("Search");
        btnResetSearch = UIUtils.createSecondaryButton("Reset");
        searchBox.add(btnSearch);
        searchBox.add(btnResetSearch);
        topFilterBar.add(searchBox, BorderLayout.WEST);

        JPanel filterBox = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        filterBox.setOpaque(false);
        filterBox.add(new JLabel("Filter:"));
        cmbFilterStatus = new JComboBox<>(new String[]{"All", "Pending", "In Progress", "Resolved"});
        cmbFilterStatus.addActionListener(e -> filterTableByStatus());
        filterBox.add(cmbFilterStatus);
        topFilterBar.add(filterBox, BorderLayout.EAST);

        rightPanel.add(topFilterBar, BorderLayout.NORTH);

        // Table
        String[] columns = {"ID", "Student Roll", "Student Name", "Category", "Description", "Date", "Status"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };

        complaintTable = new JTable(tableModel);
        UIUtils.styleTable(complaintTable);
        complaintTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Table Selection listener
        complaintTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && complaintTable.getSelectedRow() != -1) {
                int r = complaintTable.getSelectedRow();
                txtComplaintId.setText(tableModel.getValueAt(r, 0).toString());
                String roll = tableModel.getValueAt(r, 1).toString();
                selectStudentByRoll(roll);
                cmbComplaintType.setSelectedItem(tableModel.getValueAt(r, 3).toString());
                txtDescription.setText(tableModel.getValueAt(r, 4).toString());
                txtComplaintDate.setText(tableModel.getValueAt(r, 5).toString());
                cmbStatus.setSelectedItem(tableModel.getValueAt(r, 6).toString());
            }
        });

        rightPanel.add(new JScrollPane(complaintTable), BorderLayout.CENTER);
        splitPane.setRightComponent(rightPanel);

        add(splitPane, BorderLayout.CENTER);

        // Action Listeners
        btnAddComplaint.addActionListener(e -> handleAddComplaint());
        btnUpdateStatus.addActionListener(e -> handleUpdateStatus());
        btnDeleteComplaint.addActionListener(e -> handleDeleteComplaint());
        btnClear.addActionListener(e -> clearForm());
        btnSearch.addActionListener(e -> handleSearch());
        btnResetSearch.addActionListener(e -> {
            txtSearch.setText("");
            cmbFilterStatus.setSelectedIndex(0);
            loadComplaintData();
        });
        txtSearch.addActionListener(e -> handleSearch());
    }

    public void reloadStudentDropdown() {
        cmbStudents.removeAllItems();
        List<Student> students = studentDAO.getAllStudents();
        for (Student s : students) {
            cmbStudents.addItem(new StudentItem(s.getStudentId(), s.getRollNo(), s.getName()));
        }
    }

    private void selectStudentByRoll(String roll) {
        for (int i = 0; i < cmbStudents.getItemCount(); i++) {
            StudentItem item = cmbStudents.getItemAt(i);
            if (item.getRollNo().equalsIgnoreCase(roll)) {
                cmbStudents.setSelectedIndex(i);
                break;
            }
        }
    }

    public void loadComplaintData() {
        tableModel.setRowCount(0);
        List<Complaint> list = complaintDAO.getAllComplaints();
        for (Complaint c : list) {
            tableModel.addRow(new Object[]{
                c.getComplaintId(),
                c.getStudentRollNo(),
                c.getStudentName(),
                c.getComplaintType(),
                c.getDescription(),
                c.getComplaintDate(),
                c.getStatus()
            });
        }
    }

    private void filterTableByStatus() {
        String filter = cmbFilterStatus.getSelectedItem().toString();
        tableModel.setRowCount(0);
        List<Complaint> list = complaintDAO.getAllComplaints();
        for (Complaint c : list) {
            if (!"All".equals(filter) && !filter.equalsIgnoreCase(c.getStatus())) {
                continue;
            }
            tableModel.addRow(new Object[]{
                c.getComplaintId(),
                c.getStudentRollNo(),
                c.getStudentName(),
                c.getComplaintType(),
                c.getDescription(),
                c.getComplaintDate(),
                c.getStatus()
            });
        }
    }

    private void handleAddComplaint() {
        StudentItem student = (StudentItem) cmbStudents.getSelectedItem();
        if (student == null) {
            JOptionPane.showMessageDialog(this, "Please select a student lodging the complaint.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String desc = txtDescription.getText().trim();
        if (desc.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Complaint Description cannot be empty.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            txtDescription.requestFocus();
            return;
        }

        Date date;
        try {
            date = Date.valueOf(txtComplaintDate.getText().trim());
        } catch (IllegalArgumentException e) {
            JOptionPane.showMessageDialog(this, "Please enter a valid date in YYYY-MM-DD format.", "Invalid Date", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Complaint c = new Complaint(
            student.getId(),
            cmbComplaintType.getSelectedItem().toString(),
            desc,
            date,
            cmbStatus.getSelectedItem().toString()
        );

        try {
            if (complaintDAO.addComplaint(c)) {
                logDAO.log(currentUser, "COMPLAINT_ADD", 
                    "Registered " + c.getComplaintType() + " complaint for student " + student.getName() + " (" + student.getRollNo() + "): " + desc);
                JOptionPane.showMessageDialog(this, "Complaint registered successfully! (ID: " + c.getComplaintId() + ")", "Success", JOptionPane.INFORMATION_MESSAGE);
                loadComplaintData();
                clearForm();
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Database Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleUpdateStatus() {
        if (txtComplaintId.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please select a complaint from the table to update status.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int complaintId = Integer.parseInt(txtComplaintId.getText().trim());
        String newStatus = cmbStatus.getSelectedItem().toString();

        try {
            if (complaintDAO.updateComplaintStatus(complaintId, newStatus)) {
                logDAO.log(currentUser, "COMPLAINT_UPDATE", 
                    "Updated complaint #" + complaintId + " status to " + newStatus + ".");
                JOptionPane.showMessageDialog(this, "Complaint status updated to: " + newStatus, "Status Updated", JOptionPane.INFORMATION_MESSAGE);
                loadComplaintData();
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Database Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleDeleteComplaint() {
        if (txtComplaintId.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please select a complaint to delete.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int complaintId = Integer.parseInt(txtComplaintId.getText().trim());
        int confirm = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to delete Complaint #" + complaintId + "?",
            "Confirm Delete",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                if (complaintDAO.deleteComplaint(complaintId)) {
                    logDAO.log(currentUser, "COMPLAINT_DELETE", "Deleted complaint #" + complaintId + ".");
                    JOptionPane.showMessageDialog(this, "Complaint deleted.", "Deleted", JOptionPane.INFORMATION_MESSAGE);
                    loadComplaintData();
                    clearForm();
                }
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Database Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void handleSearch() {
        String keyword = txtSearch.getText().trim();
        if (keyword.isEmpty()) {
            loadComplaintData();
            return;
        }

        tableModel.setRowCount(0);
        List<Complaint> list = complaintDAO.searchComplaints(keyword);
        for (Complaint c : list) {
            tableModel.addRow(new Object[]{
                c.getComplaintId(),
                c.getStudentRollNo(),
                c.getStudentName(),
                c.getComplaintType(),
                c.getDescription(),
                c.getComplaintDate(),
                c.getStatus()
            });
        }
    }

    private void clearForm() {
        txtComplaintId.setText("");
        if (cmbStudents.getItemCount() > 0) cmbStudents.setSelectedIndex(0);
        cmbComplaintType.setSelectedIndex(0);
        txtDescription.setText("");
        txtComplaintDate.setText(LocalDate.now().toString());
        cmbStatus.setSelectedIndex(0);
        complaintTable.clearSelection();
    }
}

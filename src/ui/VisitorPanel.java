package ui;

import dao.ActivityLogDAO;
import dao.StudentDAO;
import dao.VisitorDAO;
import model.Student;
import model.User;
import model.Visitor;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Date;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * VisitorPanel - Hostel Visitor Entry/Exit Management.
 */
public class VisitorPanel extends JPanel {

    private final VisitorDAO visitorDAO;
    private final StudentDAO studentDAO;
    private final ActivityLogDAO logDAO;
    private final User currentUser;

    // Form
    private JTextField txtVisitorId;
    private JComboBox<StudentItem> cmbStudents;
    private JTextField txtVisitorName;
    private JComboBox<String> cmbRelation;
    private JTextField txtPhone;
    private JTextField txtVisitDate;
    private JTextField txtInTime;
    private JTextField txtOutTime;

    // Buttons
    private JButton btnAddVisitor;
    private JButton btnSetOutTime;
    private JButton btnDeleteVisitor;
    private JButton btnClear;

    // Search and Table
    private JTextField txtSearch;
    private JButton btnSearch;
    private JButton btnResetSearch;
    private JTable visitorTable;
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

    public VisitorPanel(User currentUser) {
        this.currentUser = currentUser;
        this.visitorDAO = new VisitorDAO();
        this.studentDAO = new StudentDAO();
        this.logDAO = new ActivityLogDAO();

        initComponents();
        reloadStudentDropdown();
        loadVisitorData();
    }

    public VisitorPanel() {
        this(null);
    }

    private void initComponents() {
        setLayout(new BorderLayout(15, 15));
        setBackground(UIUtils.COLOR_BG);
        setBorder(new EmptyBorder(15, 20, 15, 20));

        // Top Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        headerPanel.add(UIUtils.createHeaderLabel("Hostel Visitor Log Management"), BorderLayout.WEST);
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

        JLabel lblFormTitle = new JLabel("Visitor Entry Form");
        lblFormTitle.setFont(UIUtils.FONT_SUBTITLE);
        lblFormTitle.setForeground(UIUtils.COLOR_PRIMARY);
        formCard.add(lblFormTitle, BorderLayout.NORTH);

        JPanel fieldsPanel = new JPanel(new GridBagLayout());
        fieldsPanel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 5, 4, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        int row = 0;

        // Visitor ID
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.35;
        fieldsPanel.add(UIUtils.createFormLabel("Visitor ID:"), gbc);
        gbc.gridx = 1; gbc.gridy = row++; gbc.weightx = 0.65;
        txtVisitorId = new JTextField();
        txtVisitorId.setEditable(false);
        txtVisitorId.setBackground(new Color(241, 245, 249));
        fieldsPanel.add(txtVisitorId, gbc);

        // Student Dropdown
        gbc.gridx = 0; gbc.gridy = row;
        fieldsPanel.add(UIUtils.createFormLabel("Student Visited * :"), gbc);
        gbc.gridx = 1; gbc.gridy = row++;
        cmbStudents = new JComboBox<>();
        fieldsPanel.add(cmbStudents, gbc);

        // Visitor Name
        gbc.gridx = 0; gbc.gridy = row;
        fieldsPanel.add(UIUtils.createFormLabel("Visitor Name * :"), gbc);
        gbc.gridx = 1; gbc.gridy = row++;
        txtVisitorName = new JTextField();
        fieldsPanel.add(txtVisitorName, gbc);

        // Relation
        gbc.gridx = 0; gbc.gridy = row;
        fieldsPanel.add(UIUtils.createFormLabel("Relation * :"), gbc);
        gbc.gridx = 1; gbc.gridy = row++;
        cmbRelation = new JComboBox<>(new String[]{"Parent", "Guardian", "Sibling", "Friend", "Relative", "Other"});
        cmbRelation.setEditable(true);
        fieldsPanel.add(cmbRelation, gbc);

        // Phone
        gbc.gridx = 0; gbc.gridy = row;
        fieldsPanel.add(UIUtils.createFormLabel("Phone (10 Digits) * :"), gbc);
        gbc.gridx = 1; gbc.gridy = row++;
        txtPhone = new JTextField();
        fieldsPanel.add(txtPhone, gbc);

        // Visit Date
        gbc.gridx = 0; gbc.gridy = row;
        fieldsPanel.add(UIUtils.createFormLabel("Visit Date * :"), gbc);
        gbc.gridx = 1; gbc.gridy = row++;
        txtVisitDate = new JTextField(LocalDate.now().toString());
        fieldsPanel.add(txtVisitDate, gbc);

        // In Time
        gbc.gridx = 0; gbc.gridy = row;
        fieldsPanel.add(UIUtils.createFormLabel("In Time * :"), gbc);
        gbc.gridx = 1; gbc.gridy = row++;
        txtInTime = new JTextField(getCurrentFormattedTime());
        fieldsPanel.add(txtInTime, gbc);

        // Out Time
        gbc.gridx = 0; gbc.gridy = row;
        fieldsPanel.add(UIUtils.createFormLabel("Out Time:"), gbc);
        gbc.gridx = 1; gbc.gridy = row++;
        txtOutTime = new JTextField();
        txtOutTime.setToolTipText("Leave blank if still visiting");
        fieldsPanel.add(txtOutTime, gbc);

        formCard.add(new JScrollPane(fieldsPanel), BorderLayout.CENTER);

        // Form Buttons
        JPanel actionBtnPanel = new JPanel(new GridLayout(2, 2, 8, 8));
        actionBtnPanel.setOpaque(false);
        actionBtnPanel.setBorder(new EmptyBorder(8, 0, 0, 0));

        btnAddVisitor = UIUtils.createPrimaryButton("Record Entry");
        btnSetOutTime = UIUtils.createSuccessButton("Record Exit Now");
        btnDeleteVisitor = UIUtils.createDangerButton("Delete Entry");
        btnClear = UIUtils.createSecondaryButton("Clear Form");

        actionBtnPanel.add(btnAddVisitor);
        actionBtnPanel.add(btnSetOutTime);
        actionBtnPanel.add(btnDeleteVisitor);
        actionBtnPanel.add(btnClear);

        formCard.add(actionBtnPanel, BorderLayout.SOUTH);
        splitPane.setLeftComponent(formCard);

        // Right Table & Search
        JPanel rightPanel = UIUtils.createCardPanel();
        rightPanel.setLayout(new BorderLayout(0, 10));

        // Search Bar
        JPanel searchBar = new JPanel(new BorderLayout(8, 0));
        searchBar.setOpaque(false);
        searchBar.setBorder(new EmptyBorder(0, 0, 8, 0));

        JLabel lblSearch = new JLabel("Search Visitors: ");
        lblSearch.setFont(UIUtils.FONT_BOLD);
        searchBar.add(lblSearch, BorderLayout.WEST);

        txtSearch = new JTextField();
        txtSearch.setFont(UIUtils.FONT_REGULAR);
        searchBar.add(txtSearch, BorderLayout.CENTER);

        JPanel searchBtns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        searchBtns.setOpaque(false);
        btnSearch = UIUtils.createPrimaryButton("Search");
        btnResetSearch = UIUtils.createSecondaryButton("Reset");
        searchBtns.add(btnSearch);
        searchBtns.add(btnResetSearch);
        searchBar.add(searchBtns, BorderLayout.EAST);

        rightPanel.add(searchBar, BorderLayout.NORTH);

        // Table
        String[] columns = {"ID", "Student Roll", "Student Name", "Visitor Name", "Relation", "Phone", "Date", "In Time", "Out Time"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };

        visitorTable = new JTable(tableModel);
        UIUtils.styleTable(visitorTable);
        visitorTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Table Selection listener
        visitorTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && visitorTable.getSelectedRow() != -1) {
                int r = visitorTable.getSelectedRow();
                txtVisitorId.setText(tableModel.getValueAt(r, 0).toString());
                String roll = tableModel.getValueAt(r, 1).toString();
                selectStudentByRoll(roll);
                txtVisitorName.setText(tableModel.getValueAt(r, 3).toString());
                cmbRelation.setSelectedItem(tableModel.getValueAt(r, 4).toString());
                txtPhone.setText(tableModel.getValueAt(r, 5).toString());
                txtVisitDate.setText(tableModel.getValueAt(r, 6).toString());
                txtInTime.setText(tableModel.getValueAt(r, 7).toString());
                Object outVal = tableModel.getValueAt(r, 8);
                txtOutTime.setText(outVal != null ? outVal.toString() : "");
            }
        });

        rightPanel.add(new JScrollPane(visitorTable), BorderLayout.CENTER);
        splitPane.setRightComponent(rightPanel);

        add(splitPane, BorderLayout.CENTER);

        // Action Listeners
        btnAddVisitor.addActionListener(e -> handleAddVisitor());
        btnSetOutTime.addActionListener(e -> handleRecordExitTime());
        btnDeleteVisitor.addActionListener(e -> handleDeleteVisitor());
        btnClear.addActionListener(e -> clearForm());
        btnSearch.addActionListener(e -> handleSearch());
        btnResetSearch.addActionListener(e -> {
            txtSearch.setText("");
            loadVisitorData();
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

    public void loadVisitorData() {
        tableModel.setRowCount(0);
        List<Visitor> list = visitorDAO.getAllVisitors();
        for (Visitor v : list) {
            tableModel.addRow(new Object[]{
                v.getVisitorId(),
                v.getStudentRollNo(),
                v.getStudentName(),
                v.getVisitorName(),
                v.getRelation(),
                v.getPhone(),
                v.getVisitDate(),
                v.getInTime(),
                v.getOutTime() != null ? v.getOutTime() : "(Active)"
            });
        }
    }

    private void handleAddVisitor() {
        StudentItem student = (StudentItem) cmbStudents.getSelectedItem();
        if (student == null) {
            JOptionPane.showMessageDialog(this, "Please select a student being visited.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String visitorName = txtVisitorName.getText().trim();
        if (visitorName.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Visitor Name is required.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            txtVisitorName.requestFocus();
            return;
        }

        String phone = txtPhone.getText().trim();
        if (!phone.matches("\\d{10}")) {
            JOptionPane.showMessageDialog(this, "Phone Number must be 10 digits.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            txtPhone.requestFocus();
            return;
        }

        String inTime = txtInTime.getText().trim();
        if (inTime.isEmpty()) {
            inTime = getCurrentFormattedTime();
        }

        Date visitDate;
        try {
            visitDate = Date.valueOf(txtVisitDate.getText().trim());
        } catch (IllegalArgumentException e) {
            JOptionPane.showMessageDialog(this, "Please enter a valid visit date in YYYY-MM-DD format.", "Invalid Date", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String outTime = txtOutTime.getText().trim();
        if (outTime.isEmpty()) {
            outTime = null;
        }

        Visitor v = new Visitor(
            student.getId(),
            visitorName,
            cmbRelation.getSelectedItem().toString().trim(),
            phone,
            visitDate,
            inTime,
            outTime
        );

        try {
            if (visitorDAO.addVisitor(v)) {
                logDAO.log(currentUser, "VISITOR_CHECKIN", 
                    "Recorded visitor entry: " + visitorName + " (" + cmbRelation.getSelectedItem() + ") visiting student " + student.getName() + " (" + student.getRollNo() + ").");
                JOptionPane.showMessageDialog(this, "Visitor entry recorded successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                loadVisitorData();
                clearForm();
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Database Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleRecordExitTime() {
        if (txtVisitorId.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please select an active visitor record from the table to record exit time.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int visitorId = Integer.parseInt(txtVisitorId.getText().trim());
        String currentOut = getCurrentFormattedTime();
        txtOutTime.setText(currentOut);

        try {
            if (visitorDAO.recordOutTime(visitorId, currentOut)) {
                logDAO.log(currentUser, "VISITOR_CHECKOUT", 
                    "Recorded visitor exit for log #" + visitorId + " at " + currentOut + ".");
                JOptionPane.showMessageDialog(this, "Exit time recorded as: " + currentOut, "Exit Recorded", JOptionPane.INFORMATION_MESSAGE);
                loadVisitorData();
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Database Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleDeleteVisitor() {
        if (txtVisitorId.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please select a visitor record to delete.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int visitorId = Integer.parseInt(txtVisitorId.getText().trim());
        int confirm = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to delete Visitor log #" + visitorId + "?",
            "Confirm Delete",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                if (visitorDAO.deleteVisitor(visitorId)) {
                    logDAO.log(currentUser, "VISITOR_DELETE", "Deleted visitor log #" + visitorId + ".");
                    JOptionPane.showMessageDialog(this, "Visitor entry deleted.", "Deleted", JOptionPane.INFORMATION_MESSAGE);
                    loadVisitorData();
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
            loadVisitorData();
            return;
        }

        tableModel.setRowCount(0);
        List<Visitor> list = visitorDAO.searchVisitors(keyword);
        for (Visitor v : list) {
            tableModel.addRow(new Object[]{
                v.getVisitorId(),
                v.getStudentRollNo(),
                v.getStudentName(),
                v.getVisitorName(),
                v.getRelation(),
                v.getPhone(),
                v.getVisitDate(),
                v.getInTime(),
                v.getOutTime() != null ? v.getOutTime() : "(Active)"
            });
        }
    }

    private String getCurrentFormattedTime() {
        return LocalTime.now().format(DateTimeFormatter.ofPattern("hh:mm a"));
    }

    private void clearForm() {
        txtVisitorId.setText("");
        if (cmbStudents.getItemCount() > 0) cmbStudents.setSelectedIndex(0);
        txtVisitorName.setText("");
        cmbRelation.setSelectedIndex(0);
        txtPhone.setText("");
        txtVisitDate.setText(LocalDate.now().toString());
        txtInTime.setText(getCurrentFormattedTime());
        txtOutTime.setText("");
        visitorTable.clearSelection();
    }
}

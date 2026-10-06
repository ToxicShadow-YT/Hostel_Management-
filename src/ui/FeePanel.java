package ui;

import dao.ActivityLogDAO;
import dao.FeeDAO;
import dao.StudentDAO;
import model.Fee;
import model.Student;
import model.User;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.sql.Date;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

/**
 * FeePanel - Hostel Fee Records and Payment Management.
 */
public class FeePanel extends JPanel {

    private final FeeDAO feeDAO;
    private final StudentDAO studentDAO;
    private final ActivityLogDAO logDAO;
    private final User currentUser;

    // Form
    private JTextField txtFeeId;
    private JComboBox<StudentComboItem> cmbStudents;
    private JTextField txtAmount;
    private JTextField txtPaymentDate;
    private JComboBox<String> cmbPaymentStatus;
    private JComboBox<String> cmbPaymentMode;
    private JTextField txtRemarks;

    // Buttons
    private JButton btnAddFee;
    private JButton btnUpdateFee;
    private JButton btnMarkPaid;
    private JButton btnDeleteFee;
    private JButton btnClear;

    // Search and Table
    private JTextField txtSearch;
    private JButton btnSearch;
    private JButton btnResetSearch;
    private JTable feeTable;
    private DefaultTableModel tableModel;

    public static class StudentComboItem {
        private final int id;
        private final String rollNo;
        private final String name;

        public StudentComboItem(int id, String rollNo, String name) {
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

    public FeePanel(User currentUser) {
        this.currentUser = currentUser;
        this.feeDAO = new FeeDAO();
        this.studentDAO = new StudentDAO();
        this.logDAO = new ActivityLogDAO();

        initComponents();
        reloadStudentDropdown();
        loadFeeData();
    }

    public FeePanel() {
        this(null);
    }

    private void initComponents() {
        setLayout(new BorderLayout(15, 15));
        setBackground(UIUtils.COLOR_BG);
        setBorder(new EmptyBorder(15, 20, 15, 20));

        // Top Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        headerPanel.add(UIUtils.createHeaderLabel("Fees & Payment Management"), BorderLayout.WEST);
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

        JLabel lblFormTitle = new JLabel("Fee Record Form");
        lblFormTitle.setFont(UIUtils.FONT_SUBTITLE);
        lblFormTitle.setForeground(UIUtils.COLOR_PRIMARY);
        formCard.add(lblFormTitle, BorderLayout.NORTH);

        JPanel fieldsPanel = new JPanel(new GridBagLayout());
        fieldsPanel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        int row = 0;

        // Fee ID
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.35;
        fieldsPanel.add(UIUtils.createFormLabel("Fee ID:"), gbc);
        gbc.gridx = 1; gbc.gridy = row++; gbc.weightx = 0.65;
        txtFeeId = new JTextField();
        txtFeeId.setEditable(false);
        txtFeeId.setBackground(new Color(241, 245, 249));
        fieldsPanel.add(txtFeeId, gbc);

        // Student Dropdown
        gbc.gridx = 0; gbc.gridy = row;
        fieldsPanel.add(UIUtils.createFormLabel("Student * :"), gbc);
        gbc.gridx = 1; gbc.gridy = row++;
        cmbStudents = new JComboBox<>();
        fieldsPanel.add(cmbStudents, gbc);

        // Amount
        gbc.gridx = 0; gbc.gridy = row;
        fieldsPanel.add(UIUtils.createFormLabel("Amount (₹) * :"), gbc);
        gbc.gridx = 1; gbc.gridy = row++;
        txtAmount = new JTextField();
        txtAmount.setToolTipText("e.g. 35000.00");
        fieldsPanel.add(txtAmount, gbc);

        // Payment Date
        gbc.gridx = 0; gbc.gridy = row;
        fieldsPanel.add(UIUtils.createFormLabel("Payment Date * :"), gbc);
        gbc.gridx = 1; gbc.gridy = row++;
        txtPaymentDate = new JTextField(LocalDate.now().toString());
        fieldsPanel.add(txtPaymentDate, gbc);

        // Payment Status
        gbc.gridx = 0; gbc.gridy = row;
        fieldsPanel.add(UIUtils.createFormLabel("Status * :"), gbc);
        gbc.gridx = 1; gbc.gridy = row++;
        cmbPaymentStatus = new JComboBox<>(new String[]{"Paid", "Pending"});
        fieldsPanel.add(cmbPaymentStatus, gbc);

        // Payment Mode
        gbc.gridx = 0; gbc.gridy = row;
        fieldsPanel.add(UIUtils.createFormLabel("Payment Mode * :"), gbc);
        gbc.gridx = 1; gbc.gridy = row++;
        cmbPaymentMode = new JComboBox<>(new String[]{"Cash", "UPI", "Card", "Bank Transfer"});
        fieldsPanel.add(cmbPaymentMode, gbc);

        // Remarks
        gbc.gridx = 0; gbc.gridy = row;
        fieldsPanel.add(UIUtils.createFormLabel("Remarks:"), gbc);
        gbc.gridx = 1; gbc.gridy = row++;
        txtRemarks = new JTextField();
        fieldsPanel.add(txtRemarks, gbc);

        formCard.add(new JScrollPane(fieldsPanel), BorderLayout.CENTER);

        // Form Buttons
        JPanel actionBtnPanel = new JPanel(new GridLayout(3, 2, 8, 8));
        actionBtnPanel.setOpaque(false);
        actionBtnPanel.setBorder(new EmptyBorder(8, 0, 0, 0));

        btnAddFee = UIUtils.createPrimaryButton("Add Fee");
        btnUpdateFee = UIUtils.createSuccessButton("Update Fee");
        btnMarkPaid = UIUtils.createButton("Mark Paid", new Color(13, 148, 136), Color.WHITE);
        btnDeleteFee = UIUtils.createDangerButton("Delete Fee");
        btnClear = UIUtils.createSecondaryButton("Clear Form");

        actionBtnPanel.add(btnAddFee);
        actionBtnPanel.add(btnUpdateFee);
        actionBtnPanel.add(btnMarkPaid);
        actionBtnPanel.add(btnDeleteFee);
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

        JLabel lblSearch = new JLabel("Search Fees: ");
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
        String[] columns = {"Fee ID", "Roll No", "Student Name", "Amount (₹)", "Date", "Status", "Mode", "Remarks"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };

        feeTable = new JTable(tableModel);
        UIUtils.styleTable(feeTable);
        feeTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Table Selection listener: populate form fields
        feeTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && feeTable.getSelectedRow() != -1) {
                int r = feeTable.getSelectedRow();
                txtFeeId.setText(tableModel.getValueAt(r, 0).toString());
                String roll = tableModel.getValueAt(r, 1).toString();
                selectStudentByRoll(roll);
                txtAmount.setText(tableModel.getValueAt(r, 3).toString());
                txtPaymentDate.setText(tableModel.getValueAt(r, 4).toString());
                cmbPaymentStatus.setSelectedItem(tableModel.getValueAt(r, 5).toString());
                cmbPaymentMode.setSelectedItem(tableModel.getValueAt(r, 6).toString());
                Object remarks = tableModel.getValueAt(r, 7);
                txtRemarks.setText(remarks != null ? remarks.toString() : "");
            }
        });

        rightPanel.add(new JScrollPane(feeTable), BorderLayout.CENTER);
        splitPane.setRightComponent(rightPanel);

        add(splitPane, BorderLayout.CENTER);

        // Action Listeners
        btnAddFee.addActionListener(e -> handleAddFee());
        btnUpdateFee.addActionListener(e -> handleUpdateFee());
        btnMarkPaid.addActionListener(e -> handleMarkPaid());
        btnDeleteFee.addActionListener(e -> handleDeleteFee());
        btnClear.addActionListener(e -> clearForm());
        btnSearch.addActionListener(e -> handleSearch());
        btnResetSearch.addActionListener(e -> {
            txtSearch.setText("");
            loadFeeData();
        });
        txtSearch.addActionListener(e -> handleSearch());
    }

    public void reloadStudentDropdown() {
        cmbStudents.removeAllItems();
        List<Student> students = studentDAO.getAllStudents();
        for (Student s : students) {
            cmbStudents.addItem(new StudentComboItem(s.getStudentId(), s.getRollNo(), s.getName()));
        }
    }

    private void selectStudentByRoll(String roll) {
        for (int i = 0; i < cmbStudents.getItemCount(); i++) {
            StudentComboItem item = cmbStudents.getItemAt(i);
            if (item.getRollNo().equalsIgnoreCase(roll)) {
                cmbStudents.setSelectedIndex(i);
                break;
            }
        }
    }

    public void loadFeeData() {
        tableModel.setRowCount(0);
        List<Fee> fees = feeDAO.getAllFees();
        for (Fee f : fees) {
            tableModel.addRow(new Object[]{
                f.getFeeId(),
                f.getStudentRollNo(),
                f.getStudentName(),
                f.getAmount(),
                f.getPaymentDate(),
                f.getPaymentStatus(),
                f.getPaymentMode(),
                f.getRemarks()
            });
        }
    }

    private void handleAddFee() {
        StudentComboItem student = (StudentComboItem) cmbStudents.getSelectedItem();
        if (student == null) {
            JOptionPane.showMessageDialog(this, "Please select a student.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        BigDecimal amount = parseAmount();
        if (amount == null) return;

        Date paymentDate = parseDate();
        if (paymentDate == null) return;

        Fee f = new Fee(
            student.getId(),
            amount,
            paymentDate,
            cmbPaymentStatus.getSelectedItem().toString(),
            cmbPaymentMode.getSelectedItem().toString(),
            txtRemarks.getText().trim()
        );

        try {
            if (feeDAO.addFee(f)) {
                logDAO.log(currentUser, "FEE_ADD", 
                    "Recorded fee of Rs. " + f.getAmount() + " (" + f.getPaymentStatus() + ", Mode: " + f.getPaymentMode() + ") for student " + student.getName() + " (" + student.getRollNo() + ").");
                JOptionPane.showMessageDialog(this, "Fee record created successfully! (ID: " + f.getFeeId() + ")", "Success", JOptionPane.INFORMATION_MESSAGE);
                loadFeeData();
                clearForm();
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Database Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleUpdateFee() {
        if (txtFeeId.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please select a fee record from the table to update.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int feeId = Integer.parseInt(txtFeeId.getText().trim());
        StudentComboItem student = (StudentComboItem) cmbStudents.getSelectedItem();
        if (student == null) return;

        BigDecimal amount = parseAmount();
        if (amount == null) return;

        Date paymentDate = parseDate();
        if (paymentDate == null) return;

        Fee f = new Fee(
            feeId,
            student.getId(),
            student.getRollNo(),
            student.getName(),
            amount,
            paymentDate,
            cmbPaymentStatus.getSelectedItem().toString(),
            cmbPaymentMode.getSelectedItem().toString(),
            txtRemarks.getText().trim()
        );

        try {
            if (feeDAO.updateFee(f)) {
                logDAO.log(currentUser, "FEE_UPDATE", 
                    "Updated fee record #" + feeId + " (Amount: Rs. " + f.getAmount() + ", Status: " + f.getPaymentStatus() + ") for student " + student.getName() + ".");
                JOptionPane.showMessageDialog(this, "Fee record updated successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                loadFeeData();
                clearForm();
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Database Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleMarkPaid() {
        if (txtFeeId.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please select a fee record from the table.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int feeId = Integer.parseInt(txtFeeId.getText().trim());
        try {
            if (feeDAO.updateStatus(feeId, "Paid")) {
                logDAO.log(currentUser, "FEE_PAID", "Marked fee record #" + feeId + " as Paid.");
                JOptionPane.showMessageDialog(this, "Fee marked as Paid!", "Success", JOptionPane.INFORMATION_MESSAGE);
                loadFeeData();
                cmbPaymentStatus.setSelectedItem("Paid");
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Database Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleDeleteFee() {
        if (txtFeeId.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please select a fee record to delete.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int feeId = Integer.parseInt(txtFeeId.getText().trim());
        int confirm = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to delete Fee record #" + feeId + "?",
            "Confirm Delete",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                if (feeDAO.deleteFee(feeId)) {
                    logDAO.log(currentUser, "FEE_DELETE", "Deleted fee record #" + feeId + ".");
                    JOptionPane.showMessageDialog(this, "Fee record deleted.", "Deleted", JOptionPane.INFORMATION_MESSAGE);
                    loadFeeData();
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
            loadFeeData();
            return;
        }

        tableModel.setRowCount(0);
        List<Fee> results = feeDAO.searchFees(keyword);
        for (Fee f : results) {
            tableModel.addRow(new Object[]{
                f.getFeeId(),
                f.getStudentRollNo(),
                f.getStudentName(),
                f.getAmount(),
                f.getPaymentDate(),
                f.getPaymentStatus(),
                f.getPaymentMode(),
                f.getRemarks()
            });
        }
    }

    private BigDecimal parseAmount() {
        String text = txtAmount.getText().trim();
        if (text.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Fee Amount is required.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            txtAmount.requestFocus();
            return null;
        }
        try {
            BigDecimal amt = new BigDecimal(text);
            if (amt.compareTo(BigDecimal.ZERO) <= 0) {
                JOptionPane.showMessageDialog(this, "Amount must be greater than 0.", "Validation Error", JOptionPane.WARNING_MESSAGE);
                return null;
            }
            return amt;
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Please enter a valid numeric amount (e.g. 35000.00).", "Invalid Amount", JOptionPane.WARNING_MESSAGE);
            txtAmount.requestFocus();
            return null;
        }
    }

    private Date parseDate() {
        String text = txtPaymentDate.getText().trim();
        try {
            return Date.valueOf(text);
        } catch (IllegalArgumentException e) {
            JOptionPane.showMessageDialog(this, "Please enter a valid date in YYYY-MM-DD format.", "Invalid Date", JOptionPane.WARNING_MESSAGE);
            txtPaymentDate.requestFocus();
            return null;
        }
    }

    private void clearForm() {
        txtFeeId.setText("");
        if (cmbStudents.getItemCount() > 0) cmbStudents.setSelectedIndex(0);
        txtAmount.setText("");
        txtPaymentDate.setText(LocalDate.now().toString());
        cmbPaymentStatus.setSelectedIndex(0);
        cmbPaymentMode.setSelectedIndex(0);
        txtRemarks.setText("");
        feeTable.clearSelection();
    }
}

package ui;

import dao.ActivityLogDAO;
import dao.AllocationDAO;
import dao.RoomDAO;
import dao.StudentDAO;
import model.Allocation;
import model.Room;
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
 * AllocationPanel - Manage Student Room Allocations and Vacating.
 */
public class AllocationPanel extends JPanel {

    private final AllocationDAO allocationDAO;
    private final StudentDAO studentDAO;
    private final RoomDAO roomDAO;
    private final ActivityLogDAO logDAO;
    private final User currentUser;

    // Allocation Form
    private JComboBox<StudentItem> cmbStudents;
    private JComboBox<RoomItem> cmbRooms;
    private JTextField txtAllocDate;
    private JButton btnAllocate;
    private JButton btnRefreshCombos;

    // Vacate Form
    private JTextField txtVacateAllocId;
    private JTextField txtVacateStudentName;
    private JTextField txtVacateRoomNumber;
    private JTextField txtVacateDate;
    private JButton btnVacate;

    // Table & Filters
    private JTextField txtSearch;
    private JButton btnSearch;
    private JButton btnResetSearch;
    private JComboBox<String> cmbFilterStatus;
    private JTable allocTable;
    private DefaultTableModel tableModel;

    // Helper item classes for JComboBox
    public static class StudentItem {
        private final int id;
        private final String rollNo;
        private final String name;

        public StudentItem(int id, String rollNo, String name) {
            this.id = id;
            this.rollNo = rollNo;
            this.name = name;
        }

        public int getId() {
            return id;
        }

        @Override
        public String toString() {
            return rollNo + " - " + name;
        }
    }

    public static class RoomItem {
        private final int id;
        private final String roomNumber;
        private final String type;
        private final int available;

        public RoomItem(int id, String roomNumber, String type, int available) {
            this.id = id;
            this.roomNumber = roomNumber;
            this.type = type;
            this.available = available;
        }

        public int getId() {
            return id;
        }

        @Override
        public String toString() {
            return roomNumber + " (" + type + " - " + available + " bed(s) free)";
        }
    }

    public AllocationPanel(User currentUser) {
        this.currentUser = currentUser;
        this.allocationDAO = new AllocationDAO();
        this.studentDAO = new StudentDAO();
        this.roomDAO = new RoomDAO();
        this.logDAO = new ActivityLogDAO();

        initComponents();
        reloadDropdowns();
        loadAllocationData();
    }

    public AllocationPanel() {
        this(null);
    }

    private void initComponents() {
        setLayout(new BorderLayout(15, 15));
        setBackground(UIUtils.COLOR_BG);
        setBorder(new EmptyBorder(15, 20, 15, 20));

        // Top Title
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        headerPanel.add(UIUtils.createHeaderLabel("Room Allocation & Vacate Management"), BorderLayout.WEST);
        add(headerPanel, BorderLayout.NORTH);

        // Center Split: Left = Forms (Allocate & Vacate), Right = Table + Filters
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setDividerLocation(400);
        splitPane.setContinuousLayout(true);
        splitPane.setOpaque(false);
        splitPane.setBorder(null);

        // Left Container (Two Cards: Allocate Card on top, Vacate Card on bottom)
        JPanel leftFormsPanel = new JPanel(new BorderLayout(0, 12));
        leftFormsPanel.setOpaque(false);

        // --- 1. Allocate Room Card ---
        JPanel allocCard = UIUtils.createCardPanel();
        allocCard.setLayout(new BorderLayout(0, 8));

        JPanel allocHeader = new JPanel(new BorderLayout());
        allocHeader.setOpaque(false);
        JLabel lblAllocTitle = new JLabel("Allocate Room to Student");
        lblAllocTitle.setFont(UIUtils.FONT_SUBTITLE);
        lblAllocTitle.setForeground(UIUtils.COLOR_PRIMARY);
        btnRefreshCombos = UIUtils.createSecondaryButton("");
        btnRefreshCombos.setToolTipText("Refresh Student and Room dropdowns");
        btnRefreshCombos.addActionListener(e -> reloadDropdowns());
        allocHeader.add(lblAllocTitle, BorderLayout.WEST);
        allocHeader.add(btnRefreshCombos, BorderLayout.EAST);
        allocCard.add(allocHeader, BorderLayout.NORTH);

        JPanel allocFields = new JPanel(new GridBagLayout());
        allocFields.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Student dropdown
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.35;
        allocFields.add(UIUtils.createFormLabel("Select Student *:"), gbc);
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.weightx = 0.65;
        cmbStudents = new JComboBox<>();
        allocFields.add(cmbStudents, gbc);

        // Room dropdown
        gbc.gridx = 0;
        gbc.gridy = 1;
        allocFields.add(UIUtils.createFormLabel("Select Room *:"), gbc);
        gbc.gridx = 1;
        gbc.gridy = 1;
        cmbRooms = new JComboBox<>();
        allocFields.add(cmbRooms, gbc);

        // Allocation Date
        gbc.gridx = 0;
        gbc.gridy = 2;
        allocFields.add(UIUtils.createFormLabel("Date (YYYY-MM-DD):"), gbc);
        gbc.gridx = 1;
        gbc.gridy = 2;
        txtAllocDate = new JTextField(LocalDate.now().toString());
        allocFields.add(txtAllocDate, gbc);

        allocCard.add(allocFields, BorderLayout.CENTER);

        btnAllocate = UIUtils.createPrimaryButton("Confirm Room Allocation");
        btnAllocate.addActionListener(e -> handleAllocate());
        allocCard.add(btnAllocate, BorderLayout.SOUTH);

        leftFormsPanel.add(allocCard, BorderLayout.NORTH);

        // --- 2. Vacate Room Card ---
        JPanel vacateCard = UIUtils.createCardPanel();
        vacateCard.setLayout(new BorderLayout(0, 8));

        JLabel lblVacateTitle = new JLabel("Vacate Student from Room");
        lblVacateTitle.setFont(UIUtils.FONT_SUBTITLE);
        lblVacateTitle.setForeground(UIUtils.COLOR_DANGER);
        vacateCard.add(lblVacateTitle, BorderLayout.NORTH);

        JPanel vacateFields = new JPanel(new GridBagLayout());
        vacateFields.setOpaque(false);

        // Allocation ID
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.35;
        vacateFields.add(UIUtils.createFormLabel("Allocation ID:"), gbc);
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.weightx = 0.65;
        txtVacateAllocId = new JTextField();
        txtVacateAllocId.setEditable(false);
        txtVacateAllocId.setBackground(new Color(241, 245, 249));
        vacateFields.add(txtVacateAllocId, gbc);

        // Student Name
        gbc.gridx = 0;
        gbc.gridy = 1;
        vacateFields.add(UIUtils.createFormLabel("Student:"), gbc);
        gbc.gridx = 1;
        gbc.gridy = 1;
        txtVacateStudentName = new JTextField();
        txtVacateStudentName.setEditable(false);
        txtVacateStudentName.setBackground(new Color(241, 245, 249));
        vacateFields.add(txtVacateStudentName, gbc);

        // Room Number
        gbc.gridx = 0;
        gbc.gridy = 2;
        vacateFields.add(UIUtils.createFormLabel("Room No:"), gbc);
        gbc.gridx = 1;
        gbc.gridy = 2;
        txtVacateRoomNumber = new JTextField();
        txtVacateRoomNumber.setEditable(false);
        txtVacateRoomNumber.setBackground(new Color(241, 245, 249));
        vacateFields.add(txtVacateRoomNumber, gbc);

        // Vacate Date
        gbc.gridx = 0;
        gbc.gridy = 3;
        vacateFields.add(UIUtils.createFormLabel("Vacate Date:"), gbc);
        gbc.gridx = 1;
        gbc.gridy = 3;
        txtVacateDate = new JTextField(LocalDate.now().toString());
        vacateFields.add(txtVacateDate, gbc);

        vacateCard.add(vacateFields, BorderLayout.CENTER);

        btnVacate = UIUtils.createDangerButton("Confirm Vacate");
        btnVacate.addActionListener(e -> handleVacate());
        vacateCard.add(btnVacate, BorderLayout.SOUTH);

        leftFormsPanel.add(vacateCard, BorderLayout.CENTER);
        splitPane.setLeftComponent(leftFormsPanel);

        // Right Panel: Table + Search & Filters
        JPanel rightPanel = UIUtils.createCardPanel();
        rightPanel.setLayout(new BorderLayout(0, 10));

        // Top Filter Bar
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
        filterBox.add(new JLabel("Status:"));
        cmbFilterStatus = new JComboBox<>(new String[] { "All", "Active Only", "Vacated Only" });
        cmbFilterStatus.addActionListener(e -> filterTableByStatus());
        filterBox.add(cmbFilterStatus);

        topFilterBar.add(filterBox, BorderLayout.EAST);
        rightPanel.add(topFilterBar, BorderLayout.NORTH);

        // Table
        String[] columns = { "Alloc ID", "Roll No", "Student Name", "Room No", "Alloc Date", "Vacate Date", "Status" };
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };

        allocTable = new JTable(tableModel);
        UIUtils.styleTable(allocTable);
        allocTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Selection listener to populate vacate form if active allocation selected
        allocTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && allocTable.getSelectedRow() != -1) {
                int r = allocTable.getSelectedRow();
                String status = tableModel.getValueAt(r, 6).toString();
                if ("Active".equalsIgnoreCase(status)) {
                    txtVacateAllocId.setText(tableModel.getValueAt(r, 0).toString());
                    txtVacateStudentName
                            .setText(tableModel.getValueAt(r, 2).toString() + " (" + tableModel.getValueAt(r, 1) + ")");
                    txtVacateRoomNumber.setText(tableModel.getValueAt(r, 3).toString());
                } else {
                    txtVacateAllocId.setText("");
                    txtVacateStudentName.setText("(Already Vacated)");
                    txtVacateRoomNumber.setText("");
                }
            }
        });

        rightPanel.add(new JScrollPane(allocTable), BorderLayout.CENTER);
        splitPane.setRightComponent(rightPanel);

        add(splitPane, BorderLayout.CENTER);

        // Search listeners
        btnSearch.addActionListener(e -> handleSearch());
        btnResetSearch.addActionListener(e -> {
            txtSearch.setText("");
            cmbFilterStatus.setSelectedIndex(0);
            loadAllocationData();
        });
        txtSearch.addActionListener(e -> handleSearch());
    }

    /**
     * Reloads students without active allocations and rooms with available beds
     * into dropdowns.
     */
    public void reloadDropdowns() {
        // Students without active allocations
        cmbStudents.removeAllItems();
        List<Student> availableStudents = studentDAO.getStudentsWithoutActiveAllocation();
        for (Student s : availableStudents) {
            cmbStudents.addItem(new StudentItem(s.getStudentId(), s.getRollNo(), s.getName()));
        }

        // Rooms with remaining capacity
        cmbRooms.removeAllItems();
        List<Room> availableRooms = roomDAO.getAvailableRooms();
        for (Room r : availableRooms) {
            cmbRooms.addItem(new RoomItem(r.getRoomId(), r.getRoomNumber(), r.getRoomType(), r.getAvailableCapacity()));
        }
    }

    /**
     * Loads allocations table from MySQL.
     */
    public void loadAllocationData() {
        tableModel.setRowCount(0);
        List<Allocation> list = allocationDAO.getAllAllocations();
        for (Allocation a : list) {
            tableModel.addRow(new Object[] {
                    a.getAllocationId(),
                    a.getStudentRollNo(),
                    a.getStudentName(),
                    a.getRoomNumber(),
                    a.getAllocationDate(),
                    a.getVacateDate() != null ? a.getVacateDate() : "-",
                    a.getStatus()
            });
        }
    }

    private void filterTableByStatus() {
        String filter = cmbFilterStatus.getSelectedItem().toString();
        tableModel.setRowCount(0);
        List<Allocation> list = allocationDAO.getAllAllocations();
        for (Allocation a : list) {
            if ("Active Only".equals(filter) && !"Active".equalsIgnoreCase(a.getStatus())) {
                continue;
            }
            if ("Vacated Only".equals(filter) && !"Vacated".equalsIgnoreCase(a.getStatus())) {
                continue;
            }
            tableModel.addRow(new Object[] {
                    a.getAllocationId(),
                    a.getStudentRollNo(),
                    a.getStudentName(),
                    a.getRoomNumber(),
                    a.getAllocationDate(),
                    a.getVacateDate() != null ? a.getVacateDate() : "-",
                    a.getStatus()
            });
        }
    }

    private void handleAllocate() {
        StudentItem student = (StudentItem) cmbStudents.getSelectedItem();
        RoomItem room = (RoomItem) cmbRooms.getSelectedItem();

        if (student == null) {
            JOptionPane.showMessageDialog(this,
                    "No unallocated students available. All registered students are currently allocated or none exist.",
                    "Notice", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        if (room == null) {
            JOptionPane.showMessageDialog(this,
                    "No rooms with free capacity available. Please add new rooms or vacate existing ones.",
                    "Notice", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String dateStr = txtAllocDate.getText().trim();
        Date allocDate;
        try {
            allocDate = Date.valueOf(dateStr);
        } catch (IllegalArgumentException e) {
            JOptionPane.showMessageDialog(this, "Please enter a valid date in YYYY-MM-DD format.", "Invalid Date",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            boolean success = allocationDAO.allocateRoom(student.getId(), room.getId(), allocDate);
            if (success) {
                logDAO.log(currentUser, "ROOM_ALLOCATE", 
                    "Allocated Room " + room.roomNumber + " to student " + student.name + " (" + student.rollNo + ").");
                JOptionPane.showMessageDialog(this,
                        "Room " + room.roomNumber + " successfully allocated to " + student.name + "!",
                        "Allocation Success", JOptionPane.INFORMATION_MESSAGE);
                loadAllocationData();
                reloadDropdowns();
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Allocation Failed: " + ex.getMessage(), "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleVacate() {
        String allocIdStr = txtVacateAllocId.getText().trim();
        if (allocIdStr.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Please select an ACTIVE allocation row from the table to vacate.",
                    "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int allocId = Integer.parseInt(allocIdStr);
        String studentInfo = txtVacateStudentName.getText();
        String roomInfo = txtVacateRoomNumber.getText();

        String dateStr = txtVacateDate.getText().trim();
        Date vacateDate;
        try {
            vacateDate = Date.valueOf(dateStr);
        } catch (IllegalArgumentException e) {
            JOptionPane.showMessageDialog(this, "Please enter a valid vacate date in YYYY-MM-DD format.",
                    "Invalid Date", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to vacate " + studentInfo + " from Room "
                        + roomInfo + "?\n" +
                        "This will free up bed capacity in the room.",
                "Confirm Vacate",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                boolean success = allocationDAO.vacateRoom(allocId, vacateDate);
                if (success) {
                    logDAO.log(currentUser, "ROOM_VACATE", 
                        "Vacated student " + studentInfo + " from Room " + roomInfo + " (Allocation #" + allocId + ").");
                    JOptionPane.showMessageDialog(this, "Student vacated successfully. Bed capacity restored.",
                            "Vacate Completed", JOptionPane.INFORMATION_MESSAGE);
                    txtVacateAllocId.setText("");
                    txtVacateStudentName.setText("");
                    txtVacateRoomNumber.setText("");
                    loadAllocationData();
                    reloadDropdowns();
                }
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Vacate Failed: " + ex.getMessage(), "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void handleSearch() {
        String keyword = txtSearch.getText().trim();
        if (keyword.isEmpty()) {
            loadAllocationData();
            return;
        }

        tableModel.setRowCount(0);
        List<Allocation> results = allocationDAO.searchAllocations(keyword);
        for (Allocation a : results) {
            tableModel.addRow(new Object[] {
                    a.getAllocationId(),
                    a.getStudentRollNo(),
                    a.getStudentName(),
                    a.getRoomNumber(),
                    a.getAllocationDate(),
                    a.getVacateDate() != null ? a.getVacateDate() : "-",
                    a.getStatus()
            });
        }
    }
}

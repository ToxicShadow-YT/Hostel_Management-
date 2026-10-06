package ui;

import dao.ActivityLogDAO;
import dao.RoomDAO;
import model.Room;
import model.User;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

/**
 * RoomPanel - Room CRUD and Occupancy/Status Management.
 */
public class RoomPanel extends JPanel {

    private final RoomDAO roomDAO;
    private final ActivityLogDAO logDAO;
    private final User currentUser;

    // Form Components
    private JTextField txtRoomId;
    private JTextField txtRoomNumber;
    private JComboBox<String> cmbBlock;
    private JSpinner spnFloor;
    private JComboBox<String> cmbRoomType;
    private JSpinner spnCapacity;
    private JTextField txtOccupied;
    private JComboBox<String> cmbStatus;

    // Buttons
    private JButton btnAdd;
    private JButton btnUpdate;
    private JButton btnDelete;
    private JButton btnClear;

    // Search & Table
    private JTextField txtSearch;
    private JButton btnSearch;
    private JButton btnResetSearch;
    private JTable roomTable;
    private DefaultTableModel tableModel;

    public RoomPanel(User currentUser) {
        this.currentUser = currentUser;
        this.roomDAO = new RoomDAO();
        this.logDAO = new ActivityLogDAO();
        initComponents();
        loadRoomData();
    }

    public RoomPanel() {
        this(null);
    }

    private void initComponents() {
        setLayout(new BorderLayout(15, 15));
        setBackground(UIUtils.COLOR_BG);
        setBorder(new EmptyBorder(15, 20, 15, 20));

        // Top Title
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        headerPanel.add(UIUtils.createHeaderLabel("Hostel Room Management"), BorderLayout.WEST);
        add(headerPanel, BorderLayout.NORTH);

        // Center Split: Left = Form, Right = Table + Search
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setDividerLocation(380);
        splitPane.setContinuousLayout(true);
        splitPane.setOpaque(false);
        splitPane.setBorder(null);

        // Left Form Panel
        JPanel formCard = UIUtils.createCardPanel();
        formCard.setLayout(new BorderLayout(0, 10));

        JLabel lblFormTitle = new JLabel("Room Information Form");
        lblFormTitle.setFont(UIUtils.FONT_SUBTITLE);
        lblFormTitle.setForeground(UIUtils.COLOR_PRIMARY);
        formCard.add(lblFormTitle, BorderLayout.NORTH);

        JPanel fieldsPanel = new JPanel(new GridBagLayout());
        fieldsPanel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        int row = 0;

        // Room ID
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.35;
        fieldsPanel.add(UIUtils.createFormLabel("Room ID:"), gbc);
        gbc.gridx = 1; gbc.gridy = row++; gbc.weightx = 0.65;
        txtRoomId = new JTextField();
        txtRoomId.setEditable(false);
        txtRoomId.setBackground(new Color(241, 245, 249));
        fieldsPanel.add(txtRoomId, gbc);

        // Room Number
        gbc.gridx = 0; gbc.gridy = row;
        fieldsPanel.add(UIUtils.createFormLabel("Room No * :"), gbc);
        gbc.gridx = 1; gbc.gridy = row++;
        txtRoomNumber = new JTextField();
        txtRoomNumber.setToolTipText("e.g. A-101, B-204");
        fieldsPanel.add(txtRoomNumber, gbc);

        // Block
        gbc.gridx = 0; gbc.gridy = row;
        fieldsPanel.add(UIUtils.createFormLabel("Block * :"), gbc);
        gbc.gridx = 1; gbc.gridy = row++;
        cmbBlock = new JComboBox<>(new String[]{"Block A", "Block B", "Block C", "Block D"});
        cmbBlock.setEditable(true);
        fieldsPanel.add(cmbBlock, gbc);

        // Floor
        gbc.gridx = 0; gbc.gridy = row;
        fieldsPanel.add(UIUtils.createFormLabel("Floor * :"), gbc);
        gbc.gridx = 1; gbc.gridy = row++;
        spnFloor = new JSpinner(new SpinnerNumberModel(1, 0, 10, 1));
        fieldsPanel.add(spnFloor, gbc);

        // Room Type
        gbc.gridx = 0; gbc.gridy = row;
        fieldsPanel.add(UIUtils.createFormLabel("Room Type * :"), gbc);
        gbc.gridx = 1; gbc.gridy = row++;
        cmbRoomType = new JComboBox<>(new String[]{
            "Single Non-AC",
            "Single AC",
            "Double Non-AC",
            "Double AC",
            "Triple Non-AC",
            "Four Sharing"
        });
        cmbRoomType.setEditable(true);
        fieldsPanel.add(cmbRoomType, gbc);

        // Capacity
        gbc.gridx = 0; gbc.gridy = row;
        fieldsPanel.add(UIUtils.createFormLabel("Capacity * :"), gbc);
        gbc.gridx = 1; gbc.gridy = row++;
        spnCapacity = new JSpinner(new SpinnerNumberModel(2, 1, 10, 1));
        fieldsPanel.add(spnCapacity, gbc);

        // Occupied (Read-only count)
        gbc.gridx = 0; gbc.gridy = row;
        fieldsPanel.add(UIUtils.createFormLabel("Occupied:"), gbc);
        gbc.gridx = 1; gbc.gridy = row++;
        txtOccupied = new JTextField("0");
        txtOccupied.setEditable(false);
        txtOccupied.setBackground(new Color(241, 245, 249));
        txtOccupied.setToolTipText("Updated automatically via Room Allocation module");
        fieldsPanel.add(txtOccupied, gbc);

        // Status
        gbc.gridx = 0; gbc.gridy = row;
        fieldsPanel.add(UIUtils.createFormLabel("Status * :"), gbc);
        gbc.gridx = 1; gbc.gridy = row++;
        cmbStatus = new JComboBox<>(new String[]{"Available", "Full", "Maintenance"});
        fieldsPanel.add(cmbStatus, gbc);

        formCard.add(new JScrollPane(fieldsPanel), BorderLayout.CENTER);

        // Buttons
        JPanel actionBtnPanel = new JPanel(new GridLayout(2, 2, 8, 8));
        actionBtnPanel.setOpaque(false);
        actionBtnPanel.setBorder(new EmptyBorder(8, 0, 0, 0));

        btnAdd = UIUtils.createPrimaryButton("Add Room");
        btnUpdate = UIUtils.createSuccessButton("Update");
        btnDelete = UIUtils.createDangerButton("Delete");
        btnClear = UIUtils.createSecondaryButton("Clear Form");

        actionBtnPanel.add(btnAdd);
        actionBtnPanel.add(btnUpdate);
        actionBtnPanel.add(btnDelete);
        actionBtnPanel.add(btnClear);

        formCard.add(actionBtnPanel, BorderLayout.SOUTH);
        splitPane.setLeftComponent(formCard);

        // Right Panel: Table + Search
        JPanel rightPanel = UIUtils.createCardPanel();
        rightPanel.setLayout(new BorderLayout(0, 10));

        // Search Bar
        JPanel searchBar = new JPanel(new BorderLayout(8, 0));
        searchBar.setOpaque(false);
        searchBar.setBorder(new EmptyBorder(0, 0, 8, 0));

        JLabel lblSearch = new JLabel("Search Rooms: ");
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
        String[] columns = {"ID", "Room No", "Block", "Floor", "Room Type", "Capacity", "Occupied", "Available Beds", "Status"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };

        roomTable = new JTable(tableModel);
        UIUtils.styleTable(roomTable);
        roomTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Row selection listener
        roomTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && roomTable.getSelectedRow() != -1) {
                int r = roomTable.getSelectedRow();
                txtRoomId.setText(tableModel.getValueAt(r, 0).toString());
                txtRoomNumber.setText(tableModel.getValueAt(r, 1).toString());
                cmbBlock.setSelectedItem(tableModel.getValueAt(r, 2).toString());
                spnFloor.setValue(Integer.parseInt(tableModel.getValueAt(r, 3).toString()));
                cmbRoomType.setSelectedItem(tableModel.getValueAt(r, 4).toString());
                spnCapacity.setValue(Integer.parseInt(tableModel.getValueAt(r, 5).toString()));
                txtOccupied.setText(tableModel.getValueAt(r, 6).toString());
                cmbStatus.setSelectedItem(tableModel.getValueAt(r, 8).toString());
            }
        });

        rightPanel.add(new JScrollPane(roomTable), BorderLayout.CENTER);
        splitPane.setRightComponent(rightPanel);

        add(splitPane, BorderLayout.CENTER);

        // Action Listeners
        btnAdd.addActionListener(e -> handleAddRoom());
        btnUpdate.addActionListener(e -> handleUpdateRoom());
        btnDelete.addActionListener(e -> handleDeleteRoom());
        btnClear.addActionListener(e -> clearForm());
        btnSearch.addActionListener(e -> handleSearch());
        btnResetSearch.addActionListener(e -> {
            txtSearch.setText("");
            loadRoomData();
        });
        txtSearch.addActionListener(e -> handleSearch());
    }

    /**
     * Loads room data from MySQL.
     */
    public void loadRoomData() {
        tableModel.setRowCount(0);
        List<Room> rooms = roomDAO.getAllRooms();
        for (Room r : rooms) {
            tableModel.addRow(new Object[]{
                r.getRoomId(),
                r.getRoomNumber(),
                r.getBlock(),
                r.getFloor(),
                r.getRoomType(),
                r.getCapacity(),
                r.getOccupied(),
                r.getAvailableCapacity(),
                r.getStatus()
            });
        }
    }

    private void handleAddRoom() {
        if (!validateForm(0)) return;

        int capacity = (int) spnCapacity.getValue();
        Room r = new Room(
            txtRoomNumber.getText().trim(),
            cmbBlock.getSelectedItem().toString().trim(),
            (int) spnFloor.getValue(),
            cmbRoomType.getSelectedItem().toString().trim(),
            capacity,
            0,
            cmbStatus.getSelectedItem().toString()
        );

        try {
            if (roomDAO.addRoom(r)) {
                logDAO.log(currentUser, "ROOM_ADD", 
                    "Added Room " + r.getRoomNumber() + " (" + r.getBlock() + ", " + r.getRoomType() + ", Capacity: " + r.getCapacity() + ").");
                JOptionPane.showMessageDialog(this, 
                    "Room " + r.getRoomNumber() + " added successfully!", 
                    "Success", JOptionPane.INFORMATION_MESSAGE);
                loadRoomData();
                clearForm();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to add room.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Database Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleUpdateRoom() {
        if (txtRoomId.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please select a room from the table to update.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int roomId = Integer.parseInt(txtRoomId.getText().trim());
        if (!validateForm(roomId)) return;

        int occupied = Integer.parseInt(txtOccupied.getText().trim());
        int capacity = (int) spnCapacity.getValue();

        if (capacity < occupied) {
            JOptionPane.showMessageDialog(this, 
                "Capacity (" + capacity + ") cannot be less than currently occupied beds (" + occupied + ").", 
                "Capacity Conflict", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Room r = new Room(
            roomId,
            txtRoomNumber.getText().trim(),
            cmbBlock.getSelectedItem().toString().trim(),
            (int) spnFloor.getValue(),
            cmbRoomType.getSelectedItem().toString().trim(),
            capacity,
            occupied,
            cmbStatus.getSelectedItem().toString()
        );

        try {
            if (roomDAO.updateRoom(r)) {
                logDAO.log(currentUser, "ROOM_UPDATE", 
                    "Updated Room " + r.getRoomNumber() + " (Capacity: " + r.getCapacity() + ", Status: " + r.getStatus() + ").");
                JOptionPane.showMessageDialog(this, "Room updated successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                loadRoomData();
                clearForm();
            } else {
                JOptionPane.showMessageDialog(this, "Room update failed.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Database Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleDeleteRoom() {
        if (txtRoomId.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please select a room from the table to delete.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int roomId = Integer.parseInt(txtRoomId.getText().trim());
        String roomNumber = txtRoomNumber.getText().trim();
        int occupied = Integer.parseInt(txtOccupied.getText().trim());

        if (occupied > 0) {
            JOptionPane.showMessageDialog(this, 
                "Cannot delete Room " + roomNumber + ": " + occupied + " student(s) are currently occupying this room.\nPlease vacate them first.", 
                "Room Occupied", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to delete Room " + roomNumber + "?",
            "Confirm Delete",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                if (roomDAO.deleteRoom(roomId)) {
                    logDAO.log(currentUser, "ROOM_DELETE", 
                        "Deleted Room " + roomNumber + " (ID: " + roomId + ").");
                    JOptionPane.showMessageDialog(this, "Room deleted successfully.", "Deleted", JOptionPane.INFORMATION_MESSAGE);
                    loadRoomData();
                    clearForm();
                } else {
                    JOptionPane.showMessageDialog(this, "Room could not be deleted.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Cannot delete room: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void handleSearch() {
        String keyword = txtSearch.getText().trim();
        if (keyword.isEmpty()) {
            loadRoomData();
            return;
        }

        tableModel.setRowCount(0);
        List<Room> results = roomDAO.searchRooms(keyword);
        for (Room r : results) {
            tableModel.addRow(new Object[]{
                r.getRoomId(),
                r.getRoomNumber(),
                r.getBlock(),
                r.getFloor(),
                r.getRoomType(),
                r.getCapacity(),
                r.getOccupied(),
                r.getAvailableCapacity(),
                r.getStatus()
            });
        }
    }

    private boolean validateForm(int excludeId) {
        String roomNum = txtRoomNumber.getText().trim();
        if (roomNum.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Room Number is required (e.g. A-101).", "Validation Error", JOptionPane.WARNING_MESSAGE);
            txtRoomNumber.requestFocus();
            return false;
        }

        if (roomDAO.isRoomNumberExists(roomNum, excludeId)) {
            JOptionPane.showMessageDialog(this, "Room Number '" + roomNum + "' already exists.", "Duplicate Room Number", JOptionPane.ERROR_MESSAGE);
            txtRoomNumber.requestFocus();
            return false;
        }

        int capacity = (int) spnCapacity.getValue();
        if (capacity < 1) {
            JOptionPane.showMessageDialog(this, "Capacity must be at least 1.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return false;
        }

        return true;
    }

    private void clearForm() {
        txtRoomId.setText("");
        txtRoomNumber.setText("");
        cmbBlock.setSelectedIndex(0);
        spnFloor.setValue(1);
        cmbRoomType.setSelectedIndex(0);
        spnCapacity.setValue(2);
        txtOccupied.setText("0");
        cmbStatus.setSelectedIndex(0);
        roomTable.clearSelection();
    }
}

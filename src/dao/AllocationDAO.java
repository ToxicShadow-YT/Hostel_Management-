package dao;

import database.DBConnection;
import model.Allocation;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Room Allocation & Vacating transactions.
 */
public class AllocationDAO {

    /**
     * Allocates a room to a student using a database transaction.
     * Enforces:
     * 1. Student exists and does not already have an active allocation.
     * 2. Room exists, is not in maintenance, and has remaining capacity.
     * 3. Increments room occupancy and updates room status to 'Full' if capacity reached.
     * 
     * @param studentId ID of the student
     * @param roomId ID of the room
     * @param allocationDate Date of allocation
     * @return true if allocation succeeded
     * @throws SQLException with user-friendly error message if validation fails
     */
    public boolean allocateRoom(int studentId, int roomId, Date allocationDate) throws SQLException {
        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false); // Begin transaction

            // 1. Check if student already has an active allocation
            String checkStudentSql = "SELECT allocation_id FROM allocations WHERE student_id = ? AND status = 'Active'";
            try (PreparedStatement ps = conn.prepareStatement(checkStudentSql)) {
                ps.setInt(1, studentId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        throw new SQLException("This student already has an active room allocation!");
                    }
                }
            }

            // 2. Lock and check room status and capacity
            String checkRoomSql = "SELECT capacity, occupied, status FROM rooms WHERE room_id = ? FOR UPDATE";
            int capacity = 0;
            int occupied = 0;
            String roomStatus = "";

            try (PreparedStatement ps = conn.prepareStatement(checkRoomSql)) {
                ps.setInt(1, roomId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        throw new SQLException("Selected room was not found in database.");
                    }
                    capacity = rs.getInt("capacity");
                    occupied = rs.getInt("occupied");
                    roomStatus = rs.getString("status");
                }
            }

            if ("Maintenance".equalsIgnoreCase(roomStatus)) {
                throw new SQLException("Cannot allocate: Selected room is currently under Maintenance.");
            }

            if (occupied >= capacity) {
                throw new SQLException("Cannot allocate: Selected room is already full (Capacity: " 
                        + capacity + ", Occupied: " + occupied + ").");
            }

            // 3. Create allocation record
            String insertSql = "INSERT INTO allocations (student_id, room_id, allocation_date, status) VALUES (?, ?, ?, 'Active')";
            try (PreparedStatement ps = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, studentId);
                ps.setInt(2, roomId);
                ps.setDate(3, allocationDate);
                ps.executeUpdate();
            }

            // 4. Update room occupancy and status
            int newOccupied = occupied + 1;
            String newStatus = (newOccupied >= capacity) ? "Full" : "Available";

            String updateRoomSql = "UPDATE rooms SET occupied = ?, status = ? WHERE room_id = ?";
            try (PreparedStatement ps = conn.prepareStatement(updateRoomSql)) {
                ps.setInt(1, newOccupied);
                ps.setString(2, newStatus);
                ps.setInt(3, roomId);
                ps.executeUpdate();
            }

            conn.commit(); // Transaction success!
            return true;

        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback(); // Rollback on error
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            throw e;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    /**
     * Vacates a student from an allocated room using a database transaction.
     * Enforces:
     * 1. Allocation exists and is active.
     * 2. Sets vacate_date and updates status to 'Vacated'.
     * 3. Decreases room occupancy and marks room 'Available' if not under maintenance.
     * 
     * @param allocationId ID of the allocation record
     * @param vacateDate Date of vacation
     * @return true if vacating succeeded
     * @throws SQLException with user-friendly error message if validation fails
     */
    public boolean vacateRoom(int allocationId, Date vacateDate) throws SQLException {
        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false); // Begin transaction

            // 1. Check allocation details
            String checkAllocSql = "SELECT room_id, status FROM allocations WHERE allocation_id = ? FOR UPDATE";
            int roomId = 0;
            String allocStatus = "";

            try (PreparedStatement ps = conn.prepareStatement(checkAllocSql)) {
                ps.setInt(1, allocationId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        throw new SQLException("Allocation record not found.");
                    }
                    roomId = rs.getInt("room_id");
                    allocStatus = rs.getString("status");
                }
            }

            if (!"Active".equalsIgnoreCase(allocStatus)) {
                throw new SQLException("Cannot vacate: This allocation is already marked as " + allocStatus + ".");
            }

            // 2. Update allocation record
            String updateAllocSql = "UPDATE allocations SET vacate_date = ?, status = 'Vacated' WHERE allocation_id = ?";
            try (PreparedStatement ps = conn.prepareStatement(updateAllocSql)) {
                ps.setDate(1, vacateDate);
                ps.setInt(2, allocationId);
                ps.executeUpdate();
            }

            // 3. Decrement room occupancy and adjust room status
            String checkRoomSql = "SELECT capacity, occupied, status FROM rooms WHERE room_id = ? FOR UPDATE";
            int capacity = 0;
            int occupied = 0;
            String currentRoomStatus = "";

            try (PreparedStatement ps = conn.prepareStatement(checkRoomSql)) {
                ps.setInt(1, roomId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        capacity = rs.getInt("capacity");
                        occupied = rs.getInt("occupied");
                        currentRoomStatus = rs.getString("status");
                    }
                }
            }

            int newOccupied = Math.max(0, occupied - 1);
            String newRoomStatus = currentRoomStatus;
            if (!"Maintenance".equalsIgnoreCase(currentRoomStatus)) {
                newRoomStatus = (newOccupied >= capacity) ? "Full" : "Available";
            }

            String updateRoomSql = "UPDATE rooms SET occupied = ?, status = ? WHERE room_id = ?";
            try (PreparedStatement ps = conn.prepareStatement(updateRoomSql)) {
                ps.setInt(1, newOccupied);
                ps.setString(2, newRoomStatus);
                ps.setInt(3, roomId);
                ps.executeUpdate();
            }

            conn.commit(); // Transaction success!
            return true;

        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            throw e;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    /**
     * Retrieves all allocation records joined with student and room details.
     */
    public List<Allocation> getAllAllocations() {
        return queryAllocations("SELECT a.allocation_id, a.student_id, s.roll_no, s.name AS student_name, "
                + "a.room_id, r.room_number, a.allocation_date, a.vacate_date, a.status "
                + "FROM allocations a "
                + "JOIN students s ON a.student_id = s.student_id "
                + "JOIN rooms r ON a.room_id = r.room_id "
                + "ORDER BY a.allocation_id DESC");
    }

    /**
     * Retrieves active allocations only.
     */
    public List<Allocation> getActiveAllocations() {
        return queryAllocations("SELECT a.allocation_id, a.student_id, s.roll_no, s.name AS student_name, "
                + "a.room_id, r.room_number, a.allocation_date, a.vacate_date, a.status "
                + "FROM allocations a "
                + "JOIN students s ON a.student_id = s.student_id "
                + "JOIN rooms r ON a.room_id = r.room_id "
                + "WHERE a.status = 'Active' "
                + "ORDER BY a.allocation_id DESC");
    }

    /**
     * Searches allocations by student name, roll number, or room number.
     */
    public List<Allocation> searchAllocations(String keyword) {
        String pattern = "%" + keyword.trim() + "%";
        String sql = "SELECT a.allocation_id, a.student_id, s.roll_no, s.name AS student_name, "
                + "a.room_id, r.room_number, a.allocation_date, a.vacate_date, a.status "
                + "FROM allocations a "
                + "JOIN students s ON a.student_id = s.student_id "
                + "JOIN rooms r ON a.room_id = r.room_id "
                + "WHERE s.name LIKE ? OR s.roll_no LIKE ? OR r.room_number LIKE ? OR a.status LIKE ? "
                + "ORDER BY a.allocation_id DESC";

        List<Allocation> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, pattern);
            ps.setString(2, pattern);
            ps.setString(3, pattern);
            ps.setString(4, pattern);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(extractAllocationFromResultSet(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("AllocationDAO searchAllocations error: " + e.getMessage());
            e.printStackTrace();
        }
        return list;
    }

    private List<Allocation> queryAllocations(String sql) {
        List<Allocation> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(extractAllocationFromResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println("AllocationDAO queryAllocations error: " + e.getMessage());
            e.printStackTrace();
        }
        return list;
    }

    private Allocation extractAllocationFromResultSet(ResultSet rs) throws SQLException {
        return new Allocation(
            rs.getInt("allocation_id"),
            rs.getInt("student_id"),
            rs.getString("roll_no"),
            rs.getString("student_name"),
            rs.getInt("room_id"),
            rs.getString("room_number"),
            rs.getDate("allocation_date"),
            rs.getDate("vacate_date"),
            rs.getString("status")
        );
    }
}

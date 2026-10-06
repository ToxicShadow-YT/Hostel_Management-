package dao;

import database.DBConnection;
import model.Room;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Room CRUD and status operations.
 */
public class RoomDAO {

    /**
     * Adds a new room to MySQL.
     */
    public boolean addRoom(Room room) throws SQLException {
        String sql = "INSERT INTO rooms (room_number, block, floor, room_type, capacity, occupied, status) "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, room.getRoomNumber().trim());
            ps.setString(2, room.getBlock().trim());
            ps.setInt(3, room.getFloor());
            ps.setString(4, room.getRoomType().trim());
            ps.setInt(5, room.getCapacity());
            ps.setInt(6, room.getOccupied());
            ps.setString(7, room.getStatus().trim());

            int affectedRows = ps.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        room.setRoomId(generatedKeys.getInt(1));
                    }
                }
                return true;
            }
        }
        return false;
    }

    /**
     * Updates an existing room in MySQL.
     */
    public boolean updateRoom(Room room) throws SQLException {
        // Automatically determine status if not in maintenance
        String status = room.getStatus();
        if (!"Maintenance".equalsIgnoreCase(status)) {
            if (room.getOccupied() >= room.getCapacity()) {
                status = "Full";
            } else {
                status = "Available";
            }
        }

        String sql = "UPDATE rooms SET room_number = ?, block = ?, floor = ?, room_type = ?, "
                   + "capacity = ?, occupied = ?, status = ? WHERE room_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, room.getRoomNumber().trim());
            ps.setString(2, room.getBlock().trim());
            ps.setInt(3, room.getFloor());
            ps.setString(4, room.getRoomType().trim());
            ps.setInt(5, room.getCapacity());
            ps.setInt(6, room.getOccupied());
            ps.setString(7, status);
            ps.setInt(8, room.getRoomId());

            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Deletes a room if it does not have active allocations or occupants.
     */
    public boolean deleteRoom(int roomId) throws SQLException {
        if (hasActiveAllocations(roomId)) {
            throw new SQLException("Cannot delete room: active student allocations are currently assigned to this room.");
        }

        String sql = "DELETE FROM rooms WHERE room_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, roomId);
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Retrieves all rooms.
     */
    public List<Room> getAllRooms() {
        List<Room> list = new ArrayList<>();
        String sql = "SELECT * FROM rooms ORDER BY room_id ASC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(extractRoomFromResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println("RoomDAO getAllRooms error: " + e.getMessage());
            e.printStackTrace();
        }
        return list;
    }

    /**
     * Retrieves rooms that have available capacity and are not in maintenance.
     */
    public List<Room> getAvailableRooms() {
        List<Room> list = new ArrayList<>();
        String sql = "SELECT * FROM rooms WHERE status != 'Maintenance' AND occupied < capacity ORDER BY room_number ASC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(extractRoomFromResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println("RoomDAO getAvailableRooms error: " + e.getMessage());
            e.printStackTrace();
        }
        return list;
    }

    /**
     * Searches rooms by room_number, block, or room_type.
     */
    public List<Room> searchRooms(String keyword) {
        List<Room> list = new ArrayList<>();
        String sql = "SELECT * FROM rooms WHERE room_number LIKE ? OR block LIKE ? OR room_type LIKE ? OR status LIKE ? "
                   + "ORDER BY room_id ASC";

        String pattern = "%" + keyword.trim() + "%";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, pattern);
            ps.setString(2, pattern);
            ps.setString(3, pattern);
            ps.setString(4, pattern);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(extractRoomFromResultSet(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("RoomDAO searchRooms error: " + e.getMessage());
            e.printStackTrace();
        }
        return list;
    }

    /**
     * Gets a single room by ID.
     */
    public Room getRoomById(int roomId) {
        String sql = "SELECT * FROM rooms WHERE room_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, roomId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return extractRoomFromResultSet(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("RoomDAO getRoomById error: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Checks if a room number already exists.
     */
    public boolean isRoomNumberExists(String roomNumber, int excludeId) {
        String sql = "SELECT room_id FROM rooms WHERE room_number = ? AND room_id != ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, roomNumber.trim());
            ps.setInt(2, excludeId);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            System.err.println("RoomDAO isRoomNumberExists error: " + e.getMessage());
            return false;
        }
    }

    /**
     * Checks if the room has active allocations.
     */
    public boolean hasActiveAllocations(int roomId) {
        String sql = "SELECT allocation_id FROM allocations WHERE room_id = ? AND status = 'Active'";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, roomId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            System.err.println("RoomDAO hasActiveAllocations error: " + e.getMessage());
            return false;
        }
    }

    private Room extractRoomFromResultSet(ResultSet rs) throws SQLException {
        return new Room(
            rs.getInt("room_id"),
            rs.getString("room_number"),
            rs.getString("block"),
            rs.getInt("floor"),
            rs.getString("room_type"),
            rs.getInt("capacity"),
            rs.getInt("occupied"),
            rs.getString("status")
        );
    }
}

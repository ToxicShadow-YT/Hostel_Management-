package dao;

import database.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * Data Access Object for Dashboard Statistics queries.
 */
public class DashboardDAO {

    /**
     * Retrieves all core statistics in a single call for dashboard display.
     * 
     * @return Map containing statistics keys and their counts
     */
    public Map<String, Integer> getDashboardStats() {
        Map<String, Integer> stats = new HashMap<>();
        stats.put("totalStudents", getCount("SELECT COUNT(*) FROM students"));
        stats.put("totalRooms", getCount("SELECT COUNT(*) FROM rooms"));
        stats.put("occupiedRooms", getCount("SELECT COUNT(*) FROM rooms WHERE occupied > 0"));
        stats.put("availableRooms", getCount("SELECT COUNT(*) FROM rooms WHERE status = 'Available' AND occupied < capacity"));
        stats.put("activeAllocations", getCount("SELECT COUNT(*) FROM allocations WHERE status = 'Active'"));
        stats.put("pendingFees", getCount("SELECT COUNT(*) FROM fees WHERE payment_status = 'Pending'"));
        stats.put("pendingComplaints", getCount("SELECT COUNT(*) FROM complaints WHERE status != 'Resolved'"));
        stats.put("totalVisitorsToday", getCount("SELECT COUNT(*) FROM visitors WHERE visit_date = CURRENT_DATE()"));
        return stats;
    }

    public int getTotalStudents() {
        return getCount("SELECT COUNT(*) FROM students");
    }

    public int getTotalRooms() {
        return getCount("SELECT COUNT(*) FROM rooms");
    }

    public int getOccupiedRooms() {
        return getCount("SELECT COUNT(*) FROM rooms WHERE occupied > 0");
    }

    public int getAvailableRooms() {
        return getCount("SELECT COUNT(*) FROM rooms WHERE status = 'Available' AND occupied < capacity");
    }

    public int getActiveAllocations() {
        return getCount("SELECT COUNT(*) FROM allocations WHERE status = 'Active'");
    }

    public int getPendingFees() {
        return getCount("SELECT COUNT(*) FROM fees WHERE payment_status = 'Pending'");
    }

    public int getPendingComplaints() {
        return getCount("SELECT COUNT(*) FROM complaints WHERE status != 'Resolved'");
    }

    private int getCount(String sql) {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("DashboardDAO error executing [" + sql + "]: " + e.getMessage());
        }
        return 0;
    }
}

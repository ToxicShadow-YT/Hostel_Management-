package dao;

import database.DBConnection;
import model.ActivityLog;
import model.User;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Activity Logs Audit Trail.
 */
public class ActivityLogDAO {

    /**
     * Inserts an activity log entry into the database.
     */
    public boolean addLog(ActivityLog log) {
        String sql = "INSERT INTO activity_logs (username, role, action, description) VALUES (?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, log.getUsername() != null ? log.getUsername() : "system");
            ps.setString(2, log.getRole() != null ? log.getRole() : "System");
            ps.setString(3, log.getAction() != null ? log.getAction() : "ACTION");
            ps.setString(4, log.getDescription() != null ? log.getDescription() : "");

            int rows = ps.executeUpdate();
            if (rows > 0) {
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) {
                        log.setLogId(keys.getInt(1));
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            System.err.println("ActivityLogDAO addLog error: " + e.getMessage());
        }
        return false;
    }

    /**
     * Convenience method to log an action with raw parameters.
     */
    public boolean log(String username, String role, String action, String description) {
        return addLog(new ActivityLog(username, role, action, description));
    }

    /**
     * Convenience method to log an action using the current authenticated User.
     */
    public boolean log(User user, String action, String description) {
        String username = (user != null) ? user.getUsername() : "system";
        String role = (user != null) ? user.getRole() : "System";
        return log(username, role, action, description);
    }

    /**
     * Retrieves all activity logs ordered by timestamp descending.
     */
    public List<ActivityLog> getAllLogs() {
        String sql = "SELECT log_id, username, role, action, description, timestamp "
                   + "FROM activity_logs ORDER BY timestamp DESC, log_id DESC";
        return queryLogs(sql);
    }

    /**
     * Filters logs by action type, username, and/or date.
     * Pass null or empty strings to ignore specific filters.
     */
    public List<ActivityLog> filterLogs(String action, String username, String dateStr) {
        StringBuilder sql = new StringBuilder("SELECT log_id, username, role, action, description, timestamp FROM activity_logs WHERE 1=1");
        List<Object> params = new ArrayList<>();

        if (action != null && !action.trim().isEmpty() && !"All Actions".equalsIgnoreCase(action)) {
            sql.append(" AND action = ?");
            params.add(action.trim());
        }

        if (username != null && !username.trim().isEmpty() && !"All Users".equalsIgnoreCase(username)) {
            sql.append(" AND username = ?");
            params.add(username.trim());
        }

        if (dateStr != null && !dateStr.trim().isEmpty()) {
            sql.append(" AND DATE(timestamp) = ?");
            params.add(dateStr.trim());
        }

        sql.append(" ORDER BY timestamp DESC, log_id DESC");

        List<ActivityLog> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(extractLogFromResultSet(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("ActivityLogDAO filterLogs error: " + e.getMessage());
        }
        return list;
    }

    /**
     * Searches logs across username, role, action, or description.
     */
    public List<ActivityLog> searchLogs(String keyword) {
        String pattern = "%" + keyword.trim() + "%";
        String sql = "SELECT log_id, username, role, action, description, timestamp "
                   + "FROM activity_logs "
                   + "WHERE username LIKE ? OR role LIKE ? OR action LIKE ? OR description LIKE ? "
                   + "ORDER BY timestamp DESC, log_id DESC";

        List<ActivityLog> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            for (int i = 1; i <= 4; i++) {
                ps.setString(i, pattern);
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(extractLogFromResultSet(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("ActivityLogDAO searchLogs error: " + e.getMessage());
        }
        return list;
    }

    /**
     * Gets all distinct action names in the log table for filter dropdowns.
     */
    public List<String> getDistinctActions() {
        List<String> list = new ArrayList<>();
        String sql = "SELECT DISTINCT action FROM activity_logs ORDER BY action ASC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(rs.getString("action"));
            }
        } catch (SQLException e) {
            System.err.println("ActivityLogDAO getDistinctActions error: " + e.getMessage());
        }
        return list;
    }

    /**
     * Gets all distinct usernames in the log table for filter dropdowns.
     */
    public List<String> getDistinctUsers() {
        List<String> list = new ArrayList<>();
        String sql = "SELECT DISTINCT username FROM activity_logs ORDER BY username ASC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(rs.getString("username"));
            }
        } catch (SQLException e) {
            System.err.println("ActivityLogDAO getDistinctUsers error: " + e.getMessage());
        }
        return list;
    }

    private List<ActivityLog> queryLogs(String sql) {
        List<ActivityLog> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(extractLogFromResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println("ActivityLogDAO queryLogs error: " + e.getMessage());
        }
        return list;
    }

    private ActivityLog extractLogFromResultSet(ResultSet rs) throws SQLException {
        return new ActivityLog(
            rs.getInt("log_id"),
            rs.getString("username"),
            rs.getString("role"),
            rs.getString("action"),
            rs.getString("description"),
            rs.getTimestamp("timestamp")
        );
    }
}

package dao;

import database.DBConnection;
import model.Complaint;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Student Complaint management.
 */
public class ComplaintDAO {

    /**
     * Registers a new complaint.
     */
    public boolean addComplaint(Complaint complaint) throws SQLException {
        String sql = "INSERT INTO complaints (student_id, complaint_type, description, complaint_date, status) "
                   + "VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, complaint.getStudentId());
            ps.setString(2, complaint.getComplaintType().trim());
            ps.setString(3, complaint.getDescription().trim());
            ps.setDate(4, complaint.getComplaintDate());
            ps.setString(5, complaint.getStatus().trim());

            int affectedRows = ps.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        complaint.setComplaintId(generatedKeys.getInt(1));
                    }
                }
                return true;
            }
        }
        return false;
    }

    /**
     * Updates the status of an existing complaint ('Pending', 'In Progress', 'Resolved').
     */
    public boolean updateComplaintStatus(int complaintId, String status) throws SQLException {
        String sql = "UPDATE complaints SET status = ? WHERE complaint_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, status.trim());
            ps.setInt(2, complaintId);

            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Deletes a complaint.
     */
    public boolean deleteComplaint(int complaintId) throws SQLException {
        String sql = "DELETE FROM complaints WHERE complaint_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, complaintId);
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Retrieves all complaints joined with student details.
     */
    public List<Complaint> getAllComplaints() {
        return queryComplaints("SELECT c.complaint_id, c.student_id, s.roll_no, s.name AS student_name, "
                + "c.complaint_type, c.description, c.complaint_date, c.status "
                + "FROM complaints c "
                + "JOIN students s ON c.student_id = s.student_id "
                + "ORDER BY c.complaint_id DESC");
    }

    /**
     * Searches complaints by complaint_type, description, status, or student name/roll number.
     */
    public List<Complaint> searchComplaints(String keyword) {
        String pattern = "%" + keyword.trim() + "%";
        String sql = "SELECT c.complaint_id, c.student_id, s.roll_no, s.name AS student_name, "
                + "c.complaint_type, c.description, c.complaint_date, c.status "
                + "FROM complaints c "
                + "JOIN students s ON c.student_id = s.student_id "
                + "WHERE c.complaint_type LIKE ? OR c.description LIKE ? OR c.status LIKE ? "
                + "OR s.name LIKE ? OR s.roll_no LIKE ? "
                + "ORDER BY c.complaint_id DESC";

        List<Complaint> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            for (int i = 1; i <= 5; i++) {
                ps.setString(i, pattern);
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(extractComplaintFromResultSet(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("ComplaintDAO searchComplaints error: " + e.getMessage());
            e.printStackTrace();
        }
        return list;
    }

    /**
     * Counts complaints that are still 'Pending' or 'In Progress'.
     */
    public int getPendingComplaintsCount() {
        String sql = "SELECT COUNT(*) FROM complaints WHERE status != 'Resolved'";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("ComplaintDAO getPendingComplaintsCount error: " + e.getMessage());
        }
        return 0;
    }

    private List<Complaint> queryComplaints(String sql) {
        List<Complaint> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(extractComplaintFromResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println("ComplaintDAO queryComplaints error: " + e.getMessage());
            e.printStackTrace();
        }
        return list;
    }

    private Complaint extractComplaintFromResultSet(ResultSet rs) throws SQLException {
        return new Complaint(
            rs.getInt("complaint_id"),
            rs.getInt("student_id"),
            rs.getString("roll_no"),
            rs.getString("student_name"),
            rs.getString("complaint_type"),
            rs.getString("description"),
            rs.getDate("complaint_date"),
            rs.getString("status")
        );
    }
}

package dao;

import database.DBConnection;
import model.Visitor;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Visitor entry and exit tracking.
 */
public class VisitorDAO {

    /**
     * Records a new visitor entry.
     */
    public boolean addVisitor(Visitor visitor) throws SQLException {
        String sql = "INSERT INTO visitors (student_id, visitor_name, relation, phone, visit_date, in_time, out_time) "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, visitor.getStudentId());
            ps.setString(2, visitor.getVisitorName().trim());
            ps.setString(3, visitor.getRelation().trim());
            ps.setString(4, visitor.getPhone().trim());
            ps.setDate(5, visitor.getVisitDate());
            ps.setString(6, visitor.getInTime().trim());
            ps.setString(7, visitor.getOutTime() != null ? visitor.getOutTime().trim() : null);

            int affectedRows = ps.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        visitor.setVisitorId(generatedKeys.getInt(1));
                    }
                }
                return true;
            }
        }
        return false;
    }

    /**
     * Updates the exit time for a visitor.
     */
    public boolean recordOutTime(int visitorId, String outTime) throws SQLException {
        String sql = "UPDATE visitors SET out_time = ? WHERE visitor_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, outTime.trim());
            ps.setInt(2, visitorId);

            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Deletes a visitor log record.
     */
    public boolean deleteVisitor(int visitorId) throws SQLException {
        String sql = "DELETE FROM visitors WHERE visitor_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, visitorId);
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Retrieves all visitors joined with the visited student's details.
     */
    public List<Visitor> getAllVisitors() {
        return queryVisitors("SELECT v.visitor_id, v.student_id, s.roll_no, s.name AS student_name, "
                + "v.visitor_name, v.relation, v.phone, v.visit_date, v.in_time, v.out_time "
                + "FROM visitors v "
                + "JOIN students s ON v.student_id = s.student_id "
                + "ORDER BY v.visitor_id DESC");
    }

    /**
     * Searches visitors by visitor name, phone, relation, or student name/roll number.
     */
    public List<Visitor> searchVisitors(String keyword) {
        String pattern = "%" + keyword.trim() + "%";
        String sql = "SELECT v.visitor_id, v.student_id, s.roll_no, s.name AS student_name, "
                + "v.visitor_name, v.relation, v.phone, v.visit_date, v.in_time, v.out_time "
                + "FROM visitors v "
                + "JOIN students s ON v.student_id = s.student_id "
                + "WHERE v.visitor_name LIKE ? OR s.name LIKE ? OR s.roll_no LIKE ? OR v.relation LIKE ? "
                + "ORDER BY v.visitor_id DESC";

        List<Visitor> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, pattern);
            ps.setString(2, pattern);
            ps.setString(3, pattern);
            ps.setString(4, pattern);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(extractVisitorFromResultSet(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("VisitorDAO searchVisitors error: " + e.getMessage());
            e.printStackTrace();
        }
        return list;
    }

    private List<Visitor> queryVisitors(String sql) {
        List<Visitor> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(extractVisitorFromResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println("VisitorDAO queryVisitors error: " + e.getMessage());
            e.printStackTrace();
        }
        return list;
    }

    private Visitor extractVisitorFromResultSet(ResultSet rs) throws SQLException {
        return new Visitor(
            rs.getInt("visitor_id"),
            rs.getInt("student_id"),
            rs.getString("roll_no"),
            rs.getString("student_name"),
            rs.getString("visitor_name"),
            rs.getString("relation"),
            rs.getString("phone"),
            rs.getDate("visit_date"),
            rs.getString("in_time"),
            rs.getString("out_time")
        );
    }
}

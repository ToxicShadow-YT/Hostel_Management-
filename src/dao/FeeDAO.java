package dao;

import database.DBConnection;
import model.Fee;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Fee management operations.
 */
public class FeeDAO {

    /**
     * Records a new fee payment.
     */
    public boolean addFee(Fee fee) throws SQLException {
        String sql = "INSERT INTO fees (student_id, amount, payment_date, payment_status, payment_mode, remarks) "
                   + "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, fee.getStudentId());
            ps.setBigDecimal(2, fee.getAmount());
            ps.setDate(3, fee.getPaymentDate());
            ps.setString(4, fee.getPaymentStatus());
            ps.setString(5, fee.getPaymentMode());
            ps.setString(6, fee.getRemarks());

            int affectedRows = ps.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        fee.setFeeId(generatedKeys.getInt(1));
                    }
                }
                return true;
            }
        }
        return false;
    }

    /**
     * Updates an existing fee record.
     */
    public boolean updateFee(Fee fee) throws SQLException {
        String sql = "UPDATE fees SET student_id = ?, amount = ?, payment_date = ?, "
                   + "payment_status = ?, payment_mode = ?, remarks = ? WHERE fee_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, fee.getStudentId());
            ps.setBigDecimal(2, fee.getAmount());
            ps.setDate(3, fee.getPaymentDate());
            ps.setString(4, fee.getPaymentStatus());
            ps.setString(5, fee.getPaymentMode());
            ps.setString(6, fee.getRemarks());
            ps.setInt(7, fee.getFeeId());

            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Quick status update (e.g., mark as 'Paid').
     */
    public boolean updateStatus(int feeId, String newStatus) throws SQLException {
        String sql = "UPDATE fees SET payment_status = ? WHERE fee_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, newStatus);
            ps.setInt(2, feeId);

            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Deletes a fee record.
     */
    public boolean deleteFee(int feeId) throws SQLException {
        String sql = "DELETE FROM fees WHERE fee_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, feeId);
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Retrieves all fee records joined with student information.
     */
    public List<Fee> getAllFees() {
        return queryFees("SELECT f.fee_id, f.student_id, s.roll_no, s.name AS student_name, "
                + "f.amount, f.payment_date, f.payment_status, f.payment_mode, f.remarks "
                + "FROM fees f "
                + "JOIN students s ON f.student_id = s.student_id "
                + "ORDER BY f.fee_id DESC");
    }

    /**
     * Searches fees by student name, roll number, payment mode, or status.
     */
    public List<Fee> searchFees(String keyword) {
        String pattern = "%" + keyword.trim() + "%";
        String sql = "SELECT f.fee_id, f.student_id, s.roll_no, s.name AS student_name, "
                + "f.amount, f.payment_date, f.payment_status, f.payment_mode, f.remarks "
                + "FROM fees f "
                + "JOIN students s ON f.student_id = s.student_id "
                + "WHERE s.name LIKE ? OR s.roll_no LIKE ? OR f.payment_mode LIKE ? OR f.payment_status LIKE ? "
                + "ORDER BY f.fee_id DESC";

        List<Fee> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, pattern);
            ps.setString(2, pattern);
            ps.setString(3, pattern);
            ps.setString(4, pattern);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(extractFeeFromResultSet(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("FeeDAO searchFees error: " + e.getMessage());
            e.printStackTrace();
        }
        return list;
    }

    /**
     * Counts fees with 'Pending' status.
     */
    public int getPendingFeesCount() {
        String sql = "SELECT COUNT(*) FROM fees WHERE payment_status = 'Pending'";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("FeeDAO getPendingFeesCount error: " + e.getMessage());
        }
        return 0;
    }

    private List<Fee> queryFees(String sql) {
        List<Fee> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(extractFeeFromResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println("FeeDAO queryFees error: " + e.getMessage());
            e.printStackTrace();
        }
        return list;
    }

    private Fee extractFeeFromResultSet(ResultSet rs) throws SQLException {
        return new Fee(
            rs.getInt("fee_id"),
            rs.getInt("student_id"),
            rs.getString("roll_no"),
            rs.getString("student_name"),
            rs.getBigDecimal("amount"),
            rs.getDate("payment_date"),
            rs.getString("payment_status"),
            rs.getString("payment_mode"),
            rs.getString("remarks")
        );
    }
}

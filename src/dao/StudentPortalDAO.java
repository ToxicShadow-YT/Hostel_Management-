package dao;

import database.DBConnection;
import model.Complaint;
import model.Fee;
import model.Student;
import model.Visitor;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Loads only data belonging to the authenticated student.
 */
public class StudentPortalDAO {

    public PortalData loadPortalData(int studentId) throws SQLException {
        try (Connection connection = DBConnection.getConnection()) {
            Student student = loadStudent(connection, studentId);
            if (student == null) {
                throw new SQLException("The student account is no longer linked to an enrolled student.");
            }
            return new PortalData(student, loadRoom(connection, studentId),
                    loadFees(connection, studentId), loadComplaints(connection, studentId),
                    loadVisitors(connection, studentId));
        }
    }

    private Student loadStudent(Connection connection, int studentId) throws SQLException {
        String sql = "SELECT student_id, roll_no, name, gender, phone, email, course, year, address "
                + "FROM students WHERE student_id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, studentId);
            try (ResultSet result = statement.executeQuery()) {
                if (!result.next()) {
                    return null;
                }
                return new Student(result.getInt("student_id"), result.getString("roll_no"),
                        result.getString("name"), result.getString("gender"), result.getString("phone"),
                        result.getString("email"), result.getString("course"), result.getString("year"),
                        result.getString("address"));
            }
        }
    }

    private String loadRoom(Connection connection, int studentId) throws SQLException {
        String sql = "SELECT r.room_number FROM allocations a "
                + "JOIN rooms r ON r.room_id = a.room_id "
                + "WHERE a.student_id = ? AND a.status = 'Active' "
                + "ORDER BY a.allocation_id DESC LIMIT 1";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, studentId);
            try (ResultSet result = statement.executeQuery()) {
                return result.next() ? result.getString("room_number") : "Not currently allocated";
            }
        }
    }

    private List<Fee> loadFees(Connection connection, int studentId) throws SQLException {
        String sql = "SELECT f.fee_id, f.student_id, s.roll_no, s.name AS student_name, "
                + "f.amount, f.payment_date, f.payment_status, f.payment_mode, f.remarks "
                + "FROM fees f JOIN students s ON s.student_id = f.student_id "
                + "WHERE f.student_id = ? ORDER BY f.payment_date DESC, f.fee_id DESC";
        List<Fee> fees = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, studentId);
            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    fees.add(new Fee(result.getInt("fee_id"), result.getInt("student_id"),
                            result.getString("roll_no"), result.getString("student_name"),
                            result.getBigDecimal("amount"), result.getDate("payment_date"),
                            result.getString("payment_status"), result.getString("payment_mode"),
                            result.getString("remarks")));
                }
            }
        }
        return fees;
    }

    private List<Complaint> loadComplaints(Connection connection, int studentId) throws SQLException {
        String sql = "SELECT c.complaint_id, c.student_id, s.roll_no, s.name AS student_name, "
                + "c.complaint_type, c.description, c.complaint_date, c.status "
                + "FROM complaints c JOIN students s ON s.student_id = c.student_id "
                + "WHERE c.student_id = ? ORDER BY c.complaint_date DESC, c.complaint_id DESC";
        List<Complaint> complaints = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, studentId);
            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    complaints.add(new Complaint(result.getInt("complaint_id"), result.getInt("student_id"),
                            result.getString("roll_no"), result.getString("student_name"),
                            result.getString("complaint_type"), result.getString("description"),
                            result.getDate("complaint_date"), result.getString("status")));
                }
            }
        }
        return complaints;
    }

    private List<Visitor> loadVisitors(Connection connection, int studentId) throws SQLException {
        String sql = "SELECT v.visitor_id, v.student_id, s.roll_no, s.name AS student_name, "
                + "v.visitor_name, v.relation, v.phone, v.visit_date, v.in_time, v.out_time "
                + "FROM visitors v JOIN students s ON s.student_id = v.student_id "
                + "WHERE v.student_id = ? ORDER BY v.visit_date DESC, v.visitor_id DESC";
        List<Visitor> visitors = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, studentId);
            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    visitors.add(new Visitor(result.getInt("visitor_id"), result.getInt("student_id"),
                            result.getString("roll_no"), result.getString("student_name"),
                            result.getString("visitor_name"), result.getString("relation"),
                            result.getString("phone"), result.getDate("visit_date"),
                            result.getString("in_time"), result.getString("out_time")));
                }
            }
        }
        return visitors;
    }

    public record PortalData(Student student, String roomNumber, List<Fee> fees,
                             List<Complaint> complaints, List<Visitor> visitors) {
        public PortalData {
            fees = List.copyOf(fees);
            complaints = List.copyOf(complaints);
            visitors = List.copyOf(visitors);
        }
    }
}

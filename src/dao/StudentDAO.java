package dao;

import database.DBConnection;
import model.Student;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Student CRUD operations.
 */
public class StudentDAO {

    /**
     * Inserts a new student into MySQL.
     */
    public boolean addStudent(Student student) throws SQLException {
        String sql = "INSERT INTO students (roll_no, name, gender, phone, email, course, year, address) "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, student.getRollNo().trim());
            ps.setString(2, student.getName().trim());
            ps.setString(3, student.getGender());
            ps.setString(4, student.getPhone().trim());
            ps.setString(5, student.getEmail().trim());
            ps.setString(6, student.getCourse().trim());
            ps.setString(7, student.getYear().trim());
            ps.setString(8, student.getAddress().trim());

            int affectedRows = ps.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        student.setStudentId(generatedKeys.getInt(1));
                    }
                }
                return true;
            }
        }
        return false;
    }

    /**
     * Updates an existing student record in MySQL.
     */
    public boolean updateStudent(Student student) throws SQLException {
        String sql = "UPDATE students SET roll_no = ?, name = ?, gender = ?, phone = ?, "
                   + "email = ?, course = ?, year = ?, address = ? WHERE student_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, student.getRollNo().trim());
            ps.setString(2, student.getName().trim());
            ps.setString(3, student.getGender());
            ps.setString(4, student.getPhone().trim());
            ps.setString(5, student.getEmail().trim());
            ps.setString(6, student.getCourse().trim());
            ps.setString(7, student.getYear().trim());
            ps.setString(8, student.getAddress().trim());
            ps.setInt(9, student.getStudentId());

            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Deletes a student from MySQL.
     * Throws SQLException if student has active room allocations (foreign key constraint).
     */
    public boolean deleteStudent(int studentId) throws SQLException {
        String sql = "DELETE FROM students WHERE student_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, studentId);
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Retrieves all student records.
     */
    public List<Student> getAllStudents() {
        List<Student> list = new ArrayList<>();
        String sql = "SELECT * FROM students ORDER BY student_id ASC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(extractStudentFromResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println("StudentDAO getAllStudents error: " + e.getMessage());
            e.printStackTrace();
        }
        return list;
    }

    /**
     * Searches students by roll_no, name, phone, course, or year.
     */
    public List<Student> searchStudents(String keyword) {
        List<Student> list = new ArrayList<>();
        String sql = "SELECT * FROM students WHERE roll_no LIKE ? OR name LIKE ? OR phone LIKE ? "
                   + "OR course LIKE ? OR year LIKE ? ORDER BY student_id ASC";

        String pattern = "%" + keyword.trim() + "%";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            for (int i = 1; i <= 5; i++) {
                ps.setString(i, pattern);
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(extractStudentFromResultSet(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("StudentDAO searchStudents error: " + e.getMessage());
            e.printStackTrace();
        }
        return list;
    }

    /**
     * Gets a single student by ID.
     */
    public Student getStudentById(int studentId) {
        String sql = "SELECT * FROM students WHERE student_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return extractStudentFromResultSet(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("StudentDAO getStudentById error: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Checks if a roll number is already used by another student.
     */
    public boolean isRollNoExists(String rollNo, int excludeId) {
        String sql = "SELECT student_id FROM students WHERE roll_no = ? AND student_id != ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, rollNo.trim());
            ps.setInt(2, excludeId);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            System.err.println("StudentDAO isRollNoExists error: " + e.getMessage());
            return false;
        }
    }

    /**
     * Checks if student currently has an active room allocation.
     */
    public boolean hasActiveAllocation(int studentId) {
        String sql = "SELECT allocation_id FROM allocations WHERE student_id = ? AND status = 'Active'";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            System.err.println("StudentDAO hasActiveAllocation error: " + e.getMessage());
            return false;
        }
    }

    /**
     * Retrieves students who do NOT currently have an active room allocation.
     * Useful for populating the room allocation dropdown!
     */
    public List<Student> getStudentsWithoutActiveAllocation() {
        List<Student> list = new ArrayList<>();
        String sql = "SELECT s.* FROM students s "
                   + "WHERE s.student_id NOT IN ("
                   + "    SELECT a.student_id FROM allocations a WHERE a.status = 'Active'"
                   + ") ORDER BY s.name ASC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(extractStudentFromResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println("StudentDAO getStudentsWithoutActiveAllocation error: " + e.getMessage());
            e.printStackTrace();
        }
        return list;
    }

    private Student extractStudentFromResultSet(ResultSet rs) throws SQLException {
        return new Student(
            rs.getInt("student_id"),
            rs.getString("roll_no"),
            rs.getString("name"),
            rs.getString("gender"),
            rs.getString("phone"),
            rs.getString("email"),
            rs.getString("course"),
            rs.getString("year"),
            rs.getString("address")
        );
    }
}

package dao;

import database.DBConnection;
import model.Student;

import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Manages student-only credentials, separate from staff accounts.
 */
public class StudentAccountDAO {

    public Student authenticate(String rollNo, char[] password)
            throws SQLException, GeneralSecurityException {
        String sql = "SELECT s.student_id, s.roll_no, s.name, s.gender, s.phone, s.email, "
                + "s.course, s.year, s.address, sa.password "
                + "FROM students s JOIN student_accounts sa ON sa.student_id = s.student_id "
                + "WHERE s.roll_no = ?";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, rollNo.trim());
            try (ResultSet result = statement.executeQuery()) {
                if (!result.next() || !PasswordHasher.verify(password, result.getString("password"))) {
                    return null;
                }
                return extractStudent(result);
            }
        }
    }

    /**
     * Activates an account once, using the enrolled roll number and phone on record.
     */
    public boolean createAccount(String rollNo, String registeredPhone, char[] password)
            throws SQLException, GeneralSecurityException {
        requireStrongPassword(password);
        String lookupSql = "SELECT student_id, phone FROM students WHERE roll_no = ? FOR UPDATE";
        String accountSql = "SELECT student_id FROM student_accounts WHERE student_id = ?";
        String insertSql = "INSERT INTO student_accounts (student_id, password) VALUES (?, ?)";
        try (Connection connection = DBConnection.getConnection()) {
            connection.setAutoCommit(false);
            try {
                int studentId;
                try (PreparedStatement lookup = connection.prepareStatement(lookupSql)) {
                    lookup.setString(1, rollNo.trim());
                    try (ResultSet result = lookup.executeQuery()) {
                        if (!result.next() || !samePhone(registeredPhone, result.getString("phone"))) {
                            connection.rollback();
                            return false;
                        }
                        studentId = result.getInt("student_id");
                    }
                }

                try (PreparedStatement account = connection.prepareStatement(accountSql)) {
                    account.setInt(1, studentId);
                    try (ResultSet result = account.executeQuery()) {
                        if (result.next()) {
                            connection.rollback();
                            return false;
                        }
                    }
                }

                try (PreparedStatement insert = connection.prepareStatement(insertSql)) {
                    insert.setInt(1, studentId);
                    insert.setString(2, PasswordHasher.hash(password));
                    insert.executeUpdate();
                }
                connection.commit();
                return true;
            } catch (SQLException | GeneralSecurityException | RuntimeException e) {
                connection.rollback();
                throw e;
            } finally {
                connection.setAutoCommit(true);
            }
        }
    }

    public boolean changePassword(int studentId, char[] currentPassword, char[] newPassword)
            throws SQLException, GeneralSecurityException {
        requireStrongPassword(newPassword);
        String selectSql = "SELECT password FROM student_accounts WHERE student_id = ?";
        String updateSql = "UPDATE student_accounts SET password = ? WHERE student_id = ?";
        try (Connection connection = DBConnection.getConnection()) {
            String storedPassword;
            try (PreparedStatement select = connection.prepareStatement(selectSql)) {
                select.setInt(1, studentId);
                try (ResultSet result = select.executeQuery()) {
                    if (!result.next()) {
                        return false;
                    }
                    storedPassword = result.getString("password");
                }
            }
            if (!PasswordHasher.verify(currentPassword, storedPassword)) {
                return false;
            }
            try (PreparedStatement update = connection.prepareStatement(updateSql)) {
                update.setString(1, PasswordHasher.hash(newPassword));
                update.setInt(2, studentId);
                return update.executeUpdate() == 1;
            }
        }
    }

    private boolean samePhone(String suppliedPhone, String registeredPhone) {
        if (suppliedPhone == null || registeredPhone == null) {
            return false;
        }
        return MessageDigest.isEqual(
                suppliedPhone.trim().getBytes(StandardCharsets.UTF_8),
                registeredPhone.trim().getBytes(StandardCharsets.UTF_8));
    }

    private void requireStrongPassword(char[] password) {
        if (password == null || password.length < 8) {
            throw new IllegalArgumentException("Student passwords must contain at least 8 characters.");
        }
    }

    private Student extractStudent(ResultSet result) throws SQLException {
        return new Student(
                result.getInt("student_id"),
                result.getString("roll_no"),
                result.getString("name"),
                result.getString("gender"),
                result.getString("phone"),
                result.getString("email"),
                result.getString("course"),
                result.getString("year"),
                result.getString("address"));
    }
}

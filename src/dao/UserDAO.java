package dao;

import database.DBConnection;
import model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.security.GeneralSecurityException;
import java.util.Arrays;

/**
 * Data Access Object for User authentication and management.
 */
public class UserDAO {

    /**
     * Authenticates a user against credentials in MySQL.
     * 
     * @param username The entered username
     * @param password The entered password
     * @return User object if credentials are valid, null otherwise
     */
    public User authenticate(String username, String password) {
        char[] passwordChars = password == null ? new char[0] : password.toCharArray();
        try {
            return authenticate(username, passwordChars);
        } finally {
            Arrays.fill(passwordChars, '\0');
        }
    }

    /**
     * Authenticates legacy plaintext accounts and upgrades them to PBKDF2 after a valid login.
     */
    public User authenticate(String username, char[] password) {
        String sql = "SELECT user_id, username, password, full_name, role FROM users WHERE username = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String storedPassword = rs.getString("password");
                    if (!PasswordHasher.verify(password, storedPassword)) {
                        return null;
                    }

                    int userId = rs.getInt("user_id");
                    String authenticatedUsername = rs.getString("username");
                    String fullName = rs.getString("full_name");
                    String role = rs.getString("role");
                    if (!PasswordHasher.isHashed(storedPassword)) {
                        upgradeLegacyPassword(conn, userId, storedPassword, password);
                    }
                    return new User(userId, authenticatedUsername, "", fullName, role);
                }
            }
        } catch (SQLException | GeneralSecurityException e) {
            System.err.println("UserDAO authenticate error: " + e.getMessage());
        }
        return null;
    }

    /**
     * Changes a user's password after verifying the current credential.
     */
    public boolean changePassword(int userId, String oldPassword, String newPassword) {
        String verifySql = "SELECT password FROM users WHERE user_id = ?";
        String updateSql = "UPDATE users SET password = ? WHERE user_id = ?";
        try (Connection conn = DBConnection.getConnection()) {
            try (PreparedStatement psVerify = conn.prepareStatement(verifySql)) {
                psVerify.setInt(1, userId);
                try (ResultSet rs = psVerify.executeQuery()) {
                    if (!rs.next()) {
                        return false;
                    }
                    char[] oldPasswordChars = chars(oldPassword);
                    try {
                        if (!PasswordHasher.verify(oldPasswordChars, rs.getString("password"))) {
                            return false;
                        }
                    } finally {
                        Arrays.fill(oldPasswordChars, '\0');
                    }
                }
            }

            try (PreparedStatement psUpdate = conn.prepareStatement(updateSql)) {
                char[] newPasswordChars = chars(newPassword);
                try {
                    psUpdate.setString(1, PasswordHasher.hash(newPasswordChars));
                } finally {
                    Arrays.fill(newPasswordChars, '\0');
                }
                psUpdate.setInt(2, userId);
                return psUpdate.executeUpdate() > 0;
            }
        } catch (SQLException | GeneralSecurityException e) {
            System.err.println("UserDAO changePassword error: " + e.getMessage());
            return false;
        }
    }

    private void upgradeLegacyPassword(Connection conn, int userId, String oldPassword, char[] password) {
        String sql = "UPDATE users SET password = ? WHERE user_id = ? AND password = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, PasswordHasher.hash(password));
            ps.setInt(2, userId);
            ps.setString(3, oldPassword);
            if (ps.executeUpdate() == 0) {
                System.err.println("UserDAO could not upgrade the legacy password for user id " + userId + ".");
            }
        } catch (SQLException | GeneralSecurityException e) {
            System.err.println("UserDAO legacy password upgrade failed for user id " + userId + ": " + e.getMessage());
        }
    }

    private char[] chars(String value) {
        return value == null ? new char[0] : value.toCharArray();
    }
}

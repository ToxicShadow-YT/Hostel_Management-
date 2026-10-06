package dao;

import database.DBConnection;
import model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

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
        String sql = "SELECT user_id, username, password, full_name, role FROM users WHERE username = ? AND password = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, username.trim());
            ps.setString(2, password.trim());
            
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new User(
                        rs.getInt("user_id"),
                        rs.getString("username"),
                        rs.getString("password"),
                        rs.getString("full_name"),
                        rs.getString("role")
                    );
                }
            }
        } catch (SQLException e) {
            System.err.println("UserDAO authenticate error: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Changes password for a user.
     */
    public boolean changePassword(int userId, String oldPassword, String newPassword) {
        String verifySql = "SELECT user_id FROM users WHERE user_id = ? AND password = ?";
        String updateSql = "UPDATE users SET password = ? WHERE user_id = ?";
        
        try (Connection conn = DBConnection.getConnection()) {
            try (PreparedStatement psVerify = conn.prepareStatement(verifySql)) {
                psVerify.setInt(1, userId);
                psVerify.setString(2, oldPassword);
                try (ResultSet rs = psVerify.executeQuery()) {
                    if (!rs.next()) {
                        return false; // Old password doesn't match
                    }
                }
            }
            
            try (PreparedStatement psUpdate = conn.prepareStatement(updateSql)) {
                psUpdate.setString(1, newPassword);
                psUpdate.setInt(2, userId);
                return psUpdate.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            System.err.println("UserDAO changePassword error: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}

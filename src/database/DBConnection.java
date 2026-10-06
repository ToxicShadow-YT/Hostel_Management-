package database;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

/**
 * DBConnection - Centralized Database Connection Manager
 * 
 * Provides reusable JDBC connections to the MySQL database.
 * 
 * Configuration can be set directly in the static variables below OR
 * in the external 'db.properties' file in the project root folder.
 */
public class DBConnection {

    // =========================================================================
    // DATABASE CONFIGURATION - CONFIGURE YOUR MYSQL DETAILS HERE
    // =========================================================================
    private static final String DEFAULT_HOST = "localhost";
    private static final String DEFAULT_PORT = "3306";
    private static final String DEFAULT_DB = "hostel_management";

    // Change these to your MySQL credentials if different
    private static String url = "jdbc:mysql://" + DEFAULT_HOST + ":" + DEFAULT_PORT + "/" + DEFAULT_DB
            + "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    private static String user = "root";
    private static String password = "[PASSWORD]";// replace with your password

    // Driver class name
    private static final String DRIVER_CLASS = "com.mysql.cj.jdbc.Driver";

    static {
        // 1. Register MySQL JDBC Driver
        try {
            Class.forName(DRIVER_CLASS);
        } catch (ClassNotFoundException e) {
            System.err.println("CRITICAL ERROR: MySQL JDBC Driver not found in classpath!");
            System.err.println("Ensure mysql-connector-j jar is present in the lib folder.");
            e.printStackTrace();
        }

        // 2. Attempt to load from db.properties if present in root folder
        loadExternalProperties();
    }

    private static void loadExternalProperties() {
        File propFile = new File("db.properties");
        if (propFile.exists()) {
            try (InputStream in = new FileInputStream(propFile)) {
                Properties props = new Properties();
                props.load(in);
                if (props.containsKey("db.url"))
                    url = props.getProperty("db.url").trim();
                if (props.containsKey("db.user"))
                    user = props.getProperty("db.user").trim();
                if (props.containsKey("db.password"))
                    password = props.getProperty("db.password").trim();
                System.out.println("Loaded database configuration from db.properties file.");
            } catch (Exception e) {
                System.err
                        .println("Notice: Could not read db.properties, using default credentials: " + e.getMessage());
            }
        }
    }

    /**
     * Gets a new database connection.
     * Callers are responsible for closing the connection (typically via
     * try-with-resources).
     * 
     * @return Connection object
     * @throws SQLException if a database access error occurs
     */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, user, password);
    }

    /**
     * Tests if the database connection can be established successfully.
     * 
     * @return true if connection succeeded, false otherwise
     */
    public static boolean testConnection() {
        try (Connection conn = getConnection()) {
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            System.err.println("Database connection test failed: " + e.getMessage());
            return false;
        }
    }

    /**
     * Helper to safely close JDBC resources without cluttering DAO code.
     */
    public static void close(Connection conn, Statement stmt, ResultSet rs) {
        if (rs != null) {
            try {
                rs.close();
            } catch (SQLException ignored) {
            }
        }
        if (stmt != null) {
            try {
                stmt.close();
            } catch (SQLException ignored) {
            }
        }
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException ignored) {
            }
        }
    }

    public static void close(Connection conn, Statement stmt) {
        close(conn, stmt, null);
    }

    public static void close(Statement stmt) {
        close(null, stmt, null);
    }

    // Getters and Setters for configuration values
    public static String getDbUrl() {
        return url;
    }

    public static String getDbUser() {
        return user;
    }

    public static String getDbName() {
        return DEFAULT_DB;
    }

    public static void setCredentials(String newUrl, String newUser, String newPassword) {
        url = newUrl;
        user = newUser;
        password = newPassword;
    }
}

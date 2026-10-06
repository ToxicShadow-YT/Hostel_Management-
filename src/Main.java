import database.DBConnection;
import ui.LoginFrame;

import javax.swing.*;

/**
 * Main Application Launcher for Hostel Management System.
 * 
 * Diploma/Polytechnic Java Microproject
 * Technologies: Java, AWT, Swing, JDBC, MySQL.
 */
public class Main {

    public static void main(String[] args) {
        // Set System Look and Feel for native operating system controls
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
            // Fall back to default Swing look and feel
        }

        System.out.println("=================================================");
        System.out.println("        HOSTEL MANAGEMENT SYSTEM (HMS)           ");
        System.out.println("=================================================");
        System.out.println("Java Version  : " + System.getProperty("java.version"));
        System.out.println("OS Name       : " + System.getProperty("os.name"));
        System.out.println("Database URL  : " + DBConnection.getDbUrl());
        System.out.println("Database User : " + DBConnection.getDbUser());
        System.out.println("-------------------------------------------------");

        // Verify MySQL database connection availability
        boolean dbConnected = DBConnection.testConnection();
        if (dbConnected) {
            System.out.println(">>> Database Connection Status: CONNECTED SUCCESSFULLY! <<<");
        } else {
            System.err.println(">>> WARNING: Could not connect to MySQL database! <<<");
            System.err.println("Ensure MySQL is running, 'hostel_management' database exists, and");
            System.err.println("check credentials in src/database/DBConnection.java if needed.");
        }
        System.out.println("=================================================");
        System.out.println("Launching Login Window...");

        // Launch Login window on the Swing Event Dispatch Thread (EDT)
        SwingUtilities.invokeLater(() -> {
            LoginFrame loginFrame = new LoginFrame();
            loginFrame.setVisible(true);
        });
    }
}

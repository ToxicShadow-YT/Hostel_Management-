import ui.LoginFrame;
import ui.UIUtils;

import javax.swing.*;

/**
 * Main Application Launcher for Hostel Management System.
 * 
 * Diploma/Polytechnic Java Microproject
 * Technologies: Java, AWT, Swing, JDBC, MySQL.
 */
public class Main {

    public static void main(String[] args) {
        // Keep native controls while applying the shared enterprise color and font defaults.
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            System.err.println("Could not load the system look and feel; using the Swing default.");
        }
        UIUtils.installTheme();

        System.out.println("=================================================");
        System.out.println("        HOSTEL MANAGEMENT SYSTEM (HMS)           ");
        System.out.println("=================================================");
        System.out.println("Java Version  : " + System.getProperty("java.version"));
        System.out.println("OS Name       : " + System.getProperty("os.name"));
        System.out.println("=================================================");
        System.out.println("Launching Login Window...");

        // Launch Login window on the Swing Event Dispatch Thread (EDT)
        SwingUtilities.invokeLater(() -> {
            LoginFrame loginFrame = new LoginFrame();
            loginFrame.setVisible(true);
        });
    }
}

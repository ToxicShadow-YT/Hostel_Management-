package test;

import model.User;
import model.Student;
import dao.StudentDAO;
import ui.DashboardFrame;
import ui.LoginFrame;
import ui.StudentLoginFrame;
import ui.StudentPortalFrame;
import ui.UIUtils;

import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

/**
 * Regenerates the README UI screenshots from the running Swing application.
 */
public final class ScreenshotGenerator {

    private ScreenshotGenerator() {
    }

    public static void main(String[] args) throws Exception {
        if (GraphicsEnvironment.isHeadless()) {
            throw new IllegalStateException("Screenshot generation requires a graphical desktop session.");
        }

        UIUtils.installTheme();
        Path screenshotDirectory = Path.of("docs", "screenshots");
        Files.createDirectories(screenshotDirectory);
        Robot robot = new Robot();
        robot.setAutoDelay(120);

        final LoginFrame[] login = new LoginFrame[1];
        SwingUtilities.invokeAndWait(() -> {
            login[0] = new LoginFrame();
            login[0].setVisible(true);
        });
        capture(login[0], screenshotDirectory.resolve("login.png"), robot);
        SwingUtilities.invokeAndWait(login[0]::dispose);

        final StudentLoginFrame[] studentLogin = new StudentLoginFrame[1];
        SwingUtilities.invokeAndWait(() -> {
            studentLogin[0] = new StudentLoginFrame();
            studentLogin[0].setVisible(true);
        });
        capture(studentLogin[0], screenshotDirectory.resolve("student-login.png"), robot);
        SwingUtilities.invokeAndWait(studentLogin[0]::dispose);

        User screenshotUser = new User(0, "admin", "", "Hostel Chief Warden", "Admin");
        final DashboardFrame[] dashboard = new DashboardFrame[1];
        SwingUtilities.invokeAndWait(() -> {
            dashboard[0] = new DashboardFrame(screenshotUser);
            dashboard[0].setSize(1360, 850);
            dashboard[0].setVisible(true);
        });

        Field navigationField = DashboardFrame.class.getDeclaredField("navButtons");
        navigationField.setAccessible(true);
        @SuppressWarnings("unchecked")
        Map<String, JButton> navigation = (Map<String, JButton>) navigationField.get(dashboard[0]);

        capture(dashboard[0], screenshotDirectory.resolve("dashboard.png"), robot);
        captureCard(dashboard[0], navigation, "STUDENTS", "students.png", screenshotDirectory, robot);
        captureCard(dashboard[0], navigation, "ROOMS", "rooms.png", screenshotDirectory, robot);
        captureCard(dashboard[0], navigation, "ALLOCATIONS", "allocation.png", screenshotDirectory, robot);
        captureCard(dashboard[0], navigation, "FEES", "fees.png", screenshotDirectory, robot);
        captureCard(dashboard[0], navigation, "VISITORS", "visitors.png", screenshotDirectory, robot);
        captureCard(dashboard[0], navigation, "COMPLAINTS", "complaints.png", screenshotDirectory, robot);
        captureCard(dashboard[0], navigation, "LOGS", "activity-logs.png", screenshotDirectory, robot);

        SwingUtilities.invokeAndWait(dashboard[0]::dispose);

        Student screenshotStudent = new StudentDAO().searchStudents("CS202401").stream()
                .filter(student -> "CS202401".equals(student.getRollNo()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Demo student CS202401 was not found."));
        final StudentPortalFrame[] studentPortal = new StudentPortalFrame[1];
        SwingUtilities.invokeAndWait(() -> {
            studentPortal[0] = new StudentPortalFrame(screenshotStudent);
            studentPortal[0].setVisible(true);
        });
        Thread.sleep(1000);
        capture(studentPortal[0], screenshotDirectory.resolve("student-portal.png"), robot);
        SwingUtilities.invokeAndWait(studentPortal[0]::dispose);
        System.out.println("Updated login, student portal, and seven staff module screenshots in "
                + screenshotDirectory + ".");
    }

    private static void captureCard(JFrame frame, Map<String, JButton> navigation, String card,
                                    String filename, Path directory, Robot robot) throws Exception {
        JButton button = navigation.get(card);
        if (button == null) {
            throw new IllegalStateException("Navigation item not found: " + card);
        }
        SwingUtilities.invokeAndWait(button::doClick);
        Thread.sleep(900);
        capture(frame, directory.resolve(filename), robot);
    }

    private static void capture(JFrame frame, Path output, Robot robot) throws Exception {
        SwingUtilities.invokeAndWait(frame::toFront);
        Thread.sleep(250);
        Rectangle bounds = new Rectangle(frame.getLocationOnScreen(), frame.getSize());
        Rectangle screen = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());
        Rectangle captureBounds = bounds.intersection(screen);
        if (captureBounds.isEmpty()) {
            throw new IllegalStateException("Application window is outside the visible screen.");
        }
        BufferedImage screenshot = robot.createScreenCapture(captureBounds);
        if (!ImageIO.write(screenshot, "png", output.toFile())) {
            throw new IllegalStateException("No PNG image writer is available.");
        }
        System.out.println("Captured " + output);
    }
}

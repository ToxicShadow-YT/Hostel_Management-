package test;

import dao.*;
import model.*;
import ui.*;

import javax.swing.*;
import java.math.BigDecimal;
import java.awt.Color;
import java.sql.Date;
import java.time.LocalDate;

/**
 * SystemSelfTest - Comprehensive self-test suite.
 * Validates models, DAOs, UI panels, and component bindings.
 */
public class SystemSelfTest {

    public static void main(String[] args) {
        System.out.println(">>> STARTING HOSTEL MANAGEMENT SYSTEM SELF-TEST <<<");
        int passed = 0;
        int failed = 0;

        // Test 1: User Model
        try {
            User user = new User(1, "admin", "admin123", "Chief Warden", "Admin");
            assert "admin".equals(user.getUsername());
            assert "Chief Warden".equals(user.getFullName());
            System.out.println("  [PASS] Test 1: User Model");
            passed++;
        } catch (Throwable t) {
            System.err.println("  [FAIL] Test 1: User Model - " + t.getMessage());
            failed++;
        }

        // Test 2: Role permission boundaries
        try {
            assert RolePermissions.canAccessStaffPortal("Admin");
            assert RolePermissions.canAccessStaffPortal("Staff");
            assert RolePermissions.canAccessStaffPortal("Warden");
            assert !RolePermissions.canAccessStaffPortal("Student");
            assert RolePermissions.canViewActivityLogs("Admin");
            assert !RolePermissions.canViewActivityLogs("Staff");
            assert !RolePermissions.canViewActivityLogs("Warden");
            assert !RolePermissions.canViewActivityLogs("Student");
            System.out.println("  [PASS] Test 2: Role Permission Boundaries");
            passed++;
        } catch (Throwable t) {
            System.err.println("  [FAIL] Test 2: Role Permissions - " + t.getMessage());
            failed++;
        }

        // Test 3: Student Model
        try {
            Student s = new Student(1, "CS202401", "Aarav Sharma", "Male", "9876543210", 
                                    "aarav@example.com", "Diploma in Computer Engg", "2nd Year", "Pune");
            assert "CS202401".equals(s.getRollNo());
            assert "Aarav Sharma".equals(s.getName());
            System.out.println("  [PASS] Test 3: Student Model");
            passed++;
        } catch (Throwable t) {
            System.err.println("  [FAIL] Test 3: Student Model - " + t.getMessage());
            failed++;
        }

        // Test 4: Room Model
        try {
            Room r = new Room(1, "A-101", "Block A", 1, "Double Non-AC", 2, 1, "Available");
            assert r.getAvailableCapacity() == 1;
            r.setOccupied(2);
            assert r.getAvailableCapacity() == 0;
            System.out.println("  [PASS] Test 4: Room Model Capacity Calculation");
            passed++;
        } catch (Throwable t) {
            System.err.println("  [FAIL] Test 4: Room Model - " + t.getMessage());
            failed++;
        }

        // Test 5: Allocation Model
        try {
            Allocation a = new Allocation(1, 10, "CS01", "Test Student", 5, "B-201", 
                                          Date.valueOf(LocalDate.now()), null, "Active");
            assert "Active".equals(a.getStatus());
            assert "B-201".equals(a.getRoomNumber());
            System.out.println("  [PASS] Test 5: Allocation Model");
            passed++;
        } catch (Throwable t) {
            System.err.println("  [FAIL] Test 5: Allocation Model - " + t.getMessage());
            failed++;
        }

        // Test 6: Fee Model
        try {
            Fee fee = new Fee(1, 10, "CS01", "Test Student", new BigDecimal("35000.00"), 
                              Date.valueOf(LocalDate.now()), "Paid", "UPI", "Term 1");
            assert "Paid".equals(fee.getPaymentStatus());
            assert new BigDecimal("35000.00").equals(fee.getAmount());
            System.out.println("  [PASS] Test 6: Fee Model");
            passed++;
        } catch (Throwable t) {
            System.err.println("  [FAIL] Test 6: Fee Model - " + t.getMessage());
            failed++;
        }

        // Test 7: Visitor Model
        try {
            Visitor v = new Visitor(1, 10, "CS01", "Test Student", "Mr. Sharma", "Father", 
                                    "9811223344", Date.valueOf(LocalDate.now()), "10:00 AM", "12:00 PM");
            assert "Mr. Sharma".equals(v.getVisitorName());
            System.out.println("  [PASS] Test 7: Visitor Model");
            passed++;
        } catch (Throwable t) {
            System.err.println("  [FAIL] Test 7: Visitor Model - " + t.getMessage());
            failed++;
        }

        // Test 8: Complaint Model
        try {
            Complaint c = new Complaint(1, 10, "CS01", "Test Student", "Electrical", 
                                        "Fan issue", Date.valueOf(LocalDate.now()), "Pending");
            assert "Electrical".equals(c.getComplaintType());
            System.out.println("  [PASS] Test 8: Complaint Model");
            passed++;
        } catch (Throwable t) {
            System.err.println("  [FAIL] Test 8: Complaint Model - " + t.getMessage());
            failed++;
        }

        // Test 9: UI Styling Helpers
        try {
            JButton btn = UIUtils.createPrimaryButton("Test Button");
            assert btn != null;
            assert btn.getBackground().equals(UIUtils.COLOR_PRIMARY);
            assert contrastRatio(btn.getForeground(), btn.getBackground()) >= 4.5;
            JButton[] actionButtons = {
                UIUtils.createSuccessButton("Save"),
                UIUtils.createDangerButton("Delete"),
                UIUtils.createSecondaryButton("Cancel"),
                UIUtils.createButton("Mark Paid", UIUtils.COLOR_SUCCESS, Color.WHITE)
            };
            for (JButton actionButton : actionButtons) {
                assert contrastRatio(actionButton.getForeground(), actionButton.getBackground()) >= 4.5
                        : actionButton.getText() + " has insufficient label contrast";
                actionButton.setEnabled(false);
                assert contrastRatio(actionButton.getForeground(), actionButton.getBackground()) >= 4.5
                        : actionButton.getText() + " has insufficient disabled-label contrast";
            }
            assert "₹35,000.00".equals(UIUtils.formatCurrency(new BigDecimal("35000.00")));
            System.out.println("  [PASS] Test 9: UIUtils Button Styling");
            passed++;
        } catch (Throwable t) {
            System.err.println("  [FAIL] Test 9: UIUtils - " + t.getMessage());
            failed++;
        }

        // Test 10: Password hashing and legacy account verification
        try {
            char[] password = "admin123".toCharArray();
            String encoded = PasswordHasher.hash(password);
            assert encoded.length() <= 100;
            assert PasswordHasher.isHashed(encoded);
            assert PasswordHasher.verify(password, encoded);
            assert !PasswordHasher.verify("wrong-password".toCharArray(), encoded);
            assert PasswordHasher.verify(password, "admin123");
            assert !PasswordHasher.verify(password, "not-admin123");
            java.util.Arrays.fill(password, '\0');
            System.out.println("  [PASS] Test 10: Password Hashing & Legacy Verification");
            passed++;
        } catch (Throwable t) {
            System.err.println("  [FAIL] Test 10: Password hashing - " + t.getMessage());
            failed++;
        }

        // Test 11: Background data updates return to the Swing EDT
        try {
            java.util.concurrent.CountDownLatch completed = new java.util.concurrent.CountDownLatch(1);
            java.util.concurrent.atomic.AtomicBoolean appliedOnEdt = new java.util.concurrent.atomic.AtomicBoolean();
            UIUtils.loadInBackground(new JPanel(), "self-test", "run self-test load",
                    () -> 7, value -> {
                        assert value == 7;
                        appliedOnEdt.set(SwingUtilities.isEventDispatchThread());
                        completed.countDown();
                    });
            assert completed.await(5, java.util.concurrent.TimeUnit.SECONDS);
            assert appliedOnEdt.get();
            System.out.println("  [PASS] Test 11: Background Loads Update UI on the EDT");
            passed++;
        } catch (Throwable t) {
            System.err.println("  [FAIL] Test 11: Background data loading - " + t.getMessage());
            failed++;
        }

        // Test 12: DAO Instantiation
        try {
            new UserDAO();
            new StudentAccountDAO();
            new StudentPortalDAO();
            new StudentDAO();
            new RoomDAO();
            new AllocationDAO();
            new FeeDAO();
            new VisitorDAO();
            new ComplaintDAO();
            new DashboardDAO();
            System.out.println("  [PASS] Test 12: All DAO Classes Instantiated");
            passed++;
        } catch (Throwable t) {
            System.err.println("  [FAIL] Test 12: DAO Instantiation - " + t.getMessage());
            failed++;
        }

        // Test 13: Validation regex logic check
        try {
            String validPhone = "9876543210";
            String invalidPhone = "12345";
            assert validPhone.matches("\\d{10}");
            assert !invalidPhone.matches("\\d{10}");

            String validEmail = "student@example.com";
            String invalidEmail = "studentexample";
            assert validEmail.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
            assert !invalidEmail.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
            System.out.println("  [PASS] Test 13: Phone & Email Validation Regexes");
            passed++;
        } catch (Throwable t) {
            System.err.println("  [FAIL] Test 13: Validation logic - " + t.getMessage());
            failed++;
        }

        // Test 14: ActivityLog Model and DAO
        try {
            ActivityLog log = new ActivityLog(1, "admin", "Admin", "STUDENT_ADD", "Added student test", null);
            assert "admin".equals(log.getUsername());
            assert "STUDENT_ADD".equals(log.getAction());
            ActivityLogDAO logDAO = new ActivityLogDAO();
            assert logDAO != null;
            System.out.println("  [PASS] Test 14: ActivityLog Model & ActivityLogDAO");
            passed++;
        } catch (Throwable t) {
            System.err.println("  [FAIL] Test 14: ActivityLog - " + t.getMessage());
            failed++;
        }

        System.out.println("-------------------------------------------------");
        System.out.println("Total Tests Run : " + (passed + failed));
        System.out.println("Tests Passed    : " + passed);
        System.out.println("Tests Failed    : " + failed);
        System.out.println("=================================================");

        if (failed > 0) {
            System.exit(1);
        }
    }

    private static double contrastRatio(Color foreground, Color background) {
        double lighter = Math.max(relativeLuminance(foreground), relativeLuminance(background));
        double darker = Math.min(relativeLuminance(foreground), relativeLuminance(background));
        return (lighter + 0.05) / (darker + 0.05);
    }

    private static double relativeLuminance(Color color) {
        double red = linearize(color.getRed() / 255.0);
        double green = linearize(color.getGreen() / 255.0);
        double blue = linearize(color.getBlue() / 255.0);
        return 0.2126 * red + 0.7152 * green + 0.0722 * blue;
    }

    private static double linearize(double channel) {
        return channel <= 0.04045 ? channel / 12.92 : Math.pow((channel + 0.055) / 1.055, 2.4);
    }
}

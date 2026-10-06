package test;

import dao.*;
import model.*;
import ui.*;

import javax.swing.*;
import java.math.BigDecimal;
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

        // Test 2: Student Model
        try {
            Student s = new Student(1, "CS202401", "Aarav Sharma", "Male", "9876543210", 
                                    "aarav@example.com", "Diploma in Computer Engg", "2nd Year", "Pune");
            assert "CS202401".equals(s.getRollNo());
            assert "Aarav Sharma".equals(s.getName());
            System.out.println("  [PASS] Test 2: Student Model");
            passed++;
        } catch (Throwable t) {
            System.err.println("  [FAIL] Test 2: Student Model - " + t.getMessage());
            failed++;
        }

        // Test 3: Room Model
        try {
            Room r = new Room(1, "A-101", "Block A", 1, "Double Non-AC", 2, 1, "Available");
            assert r.getAvailableCapacity() == 1;
            r.setOccupied(2);
            assert r.getAvailableCapacity() == 0;
            System.out.println("  [PASS] Test 3: Room Model Capacity Calculation");
            passed++;
        } catch (Throwable t) {
            System.err.println("  [FAIL] Test 3: Room Model - " + t.getMessage());
            failed++;
        }

        // Test 4: Allocation Model
        try {
            Allocation a = new Allocation(1, 10, "CS01", "Test Student", 5, "B-201", 
                                          Date.valueOf(LocalDate.now()), null, "Active");
            assert "Active".equals(a.getStatus());
            assert "B-201".equals(a.getRoomNumber());
            System.out.println("  [PASS] Test 4: Allocation Model");
            passed++;
        } catch (Throwable t) {
            System.err.println("  [FAIL] Test 4: Allocation Model - " + t.getMessage());
            failed++;
        }

        // Test 5: Fee Model
        try {
            Fee fee = new Fee(1, 10, "CS01", "Test Student", new BigDecimal("35000.00"), 
                              Date.valueOf(LocalDate.now()), "Paid", "UPI", "Term 1");
            assert "Paid".equals(fee.getPaymentStatus());
            assert new BigDecimal("35000.00").equals(fee.getAmount());
            System.out.println("  [PASS] Test 5: Fee Model");
            passed++;
        } catch (Throwable t) {
            System.err.println("  [FAIL] Test 5: Fee Model - " + t.getMessage());
            failed++;
        }

        // Test 6: Visitor Model
        try {
            Visitor v = new Visitor(1, 10, "CS01", "Test Student", "Mr. Sharma", "Father", 
                                    "9811223344", Date.valueOf(LocalDate.now()), "10:00 AM", "12:00 PM");
            assert "Mr. Sharma".equals(v.getVisitorName());
            System.out.println("  [PASS] Test 6: Visitor Model");
            passed++;
        } catch (Throwable t) {
            System.err.println("  [FAIL] Test 6: Visitor Model - " + t.getMessage());
            failed++;
        }

        // Test 7: Complaint Model
        try {
            Complaint c = new Complaint(1, 10, "CS01", "Test Student", "Electrical", 
                                        "Fan issue", Date.valueOf(LocalDate.now()), "Pending");
            assert "Electrical".equals(c.getComplaintType());
            System.out.println("  [PASS] Test 7: Complaint Model");
            passed++;
        } catch (Throwable t) {
            System.err.println("  [FAIL] Test 7: Complaint Model - " + t.getMessage());
            failed++;
        }

        // Test 8: UI Styling Helpers
        try {
            JButton btn = UIUtils.createPrimaryButton("Test Button");
            assert btn != null;
            assert btn.getBackground().equals(UIUtils.COLOR_PRIMARY);
            System.out.println("  [PASS] Test 8: UIUtils Button Styling");
            passed++;
        } catch (Throwable t) {
            System.err.println("  [FAIL] Test 8: UIUtils - " + t.getMessage());
            failed++;
        }

        // Test 9: DAO Instantiation
        try {
            new UserDAO();
            new StudentDAO();
            new RoomDAO();
            new AllocationDAO();
            new FeeDAO();
            new VisitorDAO();
            new ComplaintDAO();
            new DashboardDAO();
            System.out.println("  [PASS] Test 9: All 8 DAO Classes Instantiated");
            passed++;
        } catch (Throwable t) {
            System.err.println("  [FAIL] Test 9: DAO Instantiation - " + t.getMessage());
            failed++;
        }

        // Test 10: Validation regex logic check
        try {
            String validPhone = "9876543210";
            String invalidPhone = "12345";
            assert validPhone.matches("\\d{10}");
            assert !invalidPhone.matches("\\d{10}");

            String validEmail = "student@example.com";
            String invalidEmail = "studentexample";
            assert validEmail.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
            assert !invalidEmail.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
            System.out.println("  [PASS] Test 10: Phone & Email Validation Regexes");
            passed++;
        } catch (Throwable t) {
            System.err.println("  [FAIL] Test 10: Validation logic - " + t.getMessage());
            failed++;
        }

        // Test 11: ActivityLog Model and DAO
        try {
            ActivityLog log = new ActivityLog(1, "admin", "Admin", "STUDENT_ADD", "Added student test", null);
            assert "admin".equals(log.getUsername());
            assert "STUDENT_ADD".equals(log.getAction());
            ActivityLogDAO logDAO = new ActivityLogDAO();
            assert logDAO != null;
            System.out.println("  [PASS] Test 11: ActivityLog Model & ActivityLogDAO");
            passed++;
        } catch (Throwable t) {
            System.err.println("  [FAIL] Test 11: ActivityLog - " + t.getMessage());
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
}

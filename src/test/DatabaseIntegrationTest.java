package test;

import dao.ActivityLogDAO;
import dao.AllocationDAO;
import dao.ComplaintDAO;
import dao.DashboardDAO;
import dao.FeeDAO;
import dao.RoomDAO;
import dao.StudentDAO;
import dao.StudentAccountDAO;
import dao.StudentPortalDAO;
import dao.UserDAO;
import dao.VisitorDAO;
import database.DBConnection;
import model.Complaint;
import model.Fee;
import model.Room;
import model.Student;
import model.Visitor;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Exercises the database-backed module operations against the configured MySQL database.
 * The test creates uniquely named records and removes them when finished.
 */
public final class DatabaseIntegrationTest {

    private static int passed;

    private DatabaseIntegrationTest() {
    }

    public static void main(String[] args) throws Exception {
        require(DBConnection.testConnection(), "MySQL connection");

        UserDAO userDAO = new UserDAO();
        require(userDAO.authenticate("admin", "admin123") != null, "admin authentication");
        require(userDAO.authenticate("warden", "warden123") != null, "warden authentication");
        pass("Authentication and legacy-password upgrade");

        String suffix = UUID.randomUUID().toString().replace("-", "").substring(0, 10).toUpperCase();
        String rollNo = "T" + suffix;
        String roomNumber = "T-" + suffix;
        String logDescription = "Integration test " + suffix;

        StudentDAO studentDAO = new StudentDAO();
        RoomDAO roomDAO = new RoomDAO();
        AllocationDAO allocationDAO = new AllocationDAO();
        FeeDAO feeDAO = new FeeDAO();
        VisitorDAO visitorDAO = new VisitorDAO();
        ComplaintDAO complaintDAO = new ComplaintDAO();
        ActivityLogDAO activityLogDAO = new ActivityLogDAO();

        int studentId = 0;
        int roomId = 0;
        int allocationId = 0;
        int feeId = 0;
        int visitorId = 0;
        int complaintId = 0;
        try {
            Student student = new Student(rollNo, "Integration Test Student", "Other", "9000000000",
                    suffix.toLowerCase() + "@example.test", "Test Course", "1st Year", "Test Address");
            require(studentDAO.addStudent(student), "create student");
            studentId = student.getStudentId();
            require(studentId > 0, "student generated id");
            student.setName("Updated Integration Student");
            require(studentDAO.updateStudent(student), "update student");
            require("Updated Integration Student".equals(studentDAO.getStudentById(studentId).getName()),
                    "read updated student");
            require(!studentDAO.searchStudents(rollNo).isEmpty(), "search student");
            pass("Student create, update, read, and search");

            StudentAccountDAO studentAccountDAO = new StudentAccountDAO();
            char[] studentPassword = "student-demo-password".toCharArray();
            require(!studentAccountDAO.createAccount(rollNo, "incorrect-phone", studentPassword),
                    "student account activation rejects an incorrect phone");
            require(studentAccountDAO.createAccount(rollNo, "9000000000", studentPassword),
                    "student account activation");
            require(!studentAccountDAO.createAccount(rollNo, "9000000000", studentPassword),
                    "student account can only be activated once");
            require(studentAccountDAO.authenticate(rollNo, "wrong-password".toCharArray()) == null,
                    "student authentication rejects an incorrect password");
            Student authenticatedStudent = studentAccountDAO.authenticate(rollNo, studentPassword);
            require(authenticatedStudent != null && authenticatedStudent.getStudentId() == studentId,
                    "student authentication returns the enrolled student");
            require(!studentAccountDAO.changePassword(studentId, "wrong-password".toCharArray(),
                    "new-student-password".toCharArray()), "student password change verifies current password");
            require(studentAccountDAO.changePassword(studentId, studentPassword,
                    "new-student-password".toCharArray()), "student password change");
            require(studentAccountDAO.authenticate(rollNo, "new-student-password".toCharArray()) != null,
                    "student can sign in with the changed password");
            pass("Student account activation, authentication, and password change");

            Room room = new Room(roomNumber, "Block T", 0, "Single Non-AC", 1, 0, "Available");
            require(roomDAO.addRoom(room), "create room");
            roomId = room.getRoomId();
            require(roomId > 0, "room generated id");
            room.setRoomType("Single AC");
            require(roomDAO.updateRoom(room), "update room");
            require("Single AC".equals(roomDAO.getRoomById(roomId).getRoomType()), "read updated room");
            require(!roomDAO.searchRooms(roomNumber).isEmpty(), "search room");
            pass("Room create, update, read, and search");

            Date today = Date.valueOf(LocalDate.now());
            int allocatedStudentId = studentId;
            int allocatedRoomId = roomId;
            require(allocationDAO.allocateRoom(studentId, roomId, today), "allocate room");
            allocationId = allocationDAO.getActiveAllocations().stream()
                    .filter(a -> a.getStudentId() == allocatedStudentId)
                    .mapToInt(a -> a.getAllocationId()).findFirst().orElseThrow();
            require(roomDAO.getRoomById(roomId).getOccupied() == 1, "allocation increments occupancy");
            expectSqlFailure(() -> allocationDAO.allocateRoom(allocatedStudentId, allocatedRoomId, today),
                    "duplicate active allocation rejection");
            require(allocationDAO.vacateRoom(allocationId, today), "vacate room");
            require(roomDAO.getRoomById(roomId).getOccupied() == 0, "vacating decrements occupancy");
            pass("Allocation, duplicate protection, vacating, and room occupancy");

            Fee fee = new Fee(studentId, new BigDecimal("1250.00"), today,
                    "Pending", "UPI", logDescription);
            require(feeDAO.addFee(fee), "create fee");
            feeId = fee.getFeeId();
            int createdFeeId = feeId;
            StudentPortalDAO.PortalData portalData = new StudentPortalDAO().loadPortalData(studentId);
            require(portalData.student().getStudentId() == studentId, "student portal loads its owner's profile");
            require(portalData.fees().size() == 1 && portalData.fees().get(0).getFeeId() == createdFeeId,
                    "student portal limits fee data to its owner");
            fee.setAmount(new BigDecimal("1500.00"));
            require(feeDAO.updateFee(fee), "update fee");
            require(feeDAO.updateStatus(createdFeeId, "Paid"), "mark fee paid");
            require(feeDAO.searchFees(rollNo).stream().anyMatch(f -> f.getFeeId() == createdFeeId
                    && "Paid".equals(f.getPaymentStatus())), "search updated fee");
            require(feeDAO.deleteFee(createdFeeId), "delete fee");
            feeId = 0;
            pass("Fee create, update, payment status, search, and delete");

            Complaint complaint = new Complaint(studentId, "Other", logDescription, today, "Pending");
            require(complaintDAO.addComplaint(complaint), "create complaint");
            complaintId = complaint.getComplaintId();
            int createdComplaintId = complaintId;
            require(new StudentPortalDAO().loadPortalData(studentId).complaints().stream()
                    .anyMatch(c -> c.getComplaintId() == createdComplaintId),
                    "student portal loads its own complaints");
            require(complaintDAO.updateComplaintStatus(createdComplaintId, "In Progress"), "update complaint status");
            require(complaintDAO.searchComplaints(rollNo).stream()
                    .anyMatch(c -> c.getComplaintId() == createdComplaintId
                            && "In Progress".equals(c.getStatus())), "search updated complaint");
            require(complaintDAO.deleteComplaint(createdComplaintId), "delete complaint");
            complaintId = 0;
            pass("Complaint create, status update, search, and delete");

            Visitor visitor = new Visitor(studentId, "Integration Visitor", "Guest", "9000000001",
                    today, "10:00 AM", null);
            require(visitorDAO.addVisitor(visitor), "create visitor entry");
            visitorId = visitor.getVisitorId();
            int createdVisitorId = visitorId;
            require(new StudentPortalDAO().loadPortalData(studentId).visitors().stream()
                    .anyMatch(v -> v.getVisitorId() == createdVisitorId),
                    "student portal loads its own visitor records");
            require(visitorDAO.recordOutTime(createdVisitorId, "11:00 AM"), "record visitor exit");
            require(visitorDAO.searchVisitors("Integration Visitor").stream()
                    .anyMatch(v -> v.getVisitorId() == createdVisitorId && "11:00 AM".equals(v.getOutTime())),
                    "search checked-out visitor");
            require(visitorDAO.deleteVisitor(createdVisitorId), "delete visitor");
            visitorId = 0;
            pass("Visitor check-in, check-out, search, and delete");

            require(activityLogDAO.log("admin", "Admin", "INTEGRATION_TEST", logDescription),
                    "create activity log");
            require(activityLogDAO.searchLogs(logDescription).stream()
                    .anyMatch(log -> logDescription.equals(log.getDescription())), "search activity log");
            require(!activityLogDAO.filterLogs("INTEGRATION_TEST", "admin", "").isEmpty(),
                    "filter activity log");
            pass("Activity log create, search, and filter");

            var stats = new DashboardDAO().getDashboardStats();
            require(stats.getOrDefault("totalStudents", 0) >= 6, "dashboard student metric");
            require(stats.getOrDefault("totalRooms", 0) >= 7, "dashboard room metric");
            require(stats.getOrDefault("activeAllocations", 0) >= 3, "dashboard allocation metric");
            require(stats.getOrDefault("pendingFees", 0) >= 2, "dashboard pending fee metric");
            require(stats.getOrDefault("pendingComplaints", 0) >= 2, "dashboard complaint metric");
            pass("Database-backed dashboard metrics");
        } finally {
            cleanup(studentDAO, roomDAO, studentId, roomId, allocationId, feeId, visitorId,
                    complaintId, logDescription);
        }

        System.out.println("Database integration checks passed: " + passed);
    }

    private static void cleanup(StudentDAO studentDAO, RoomDAO roomDAO, int studentId, int roomId,
                                int allocationId, int feeId, int visitorId, int complaintId,
                                String logDescription) throws SQLException {
        try (Connection connection = DBConnection.getConnection()) {
            delete(connection, "DELETE FROM activity_logs WHERE description = ?", logDescription);
            if (complaintId > 0) {
                delete(connection, "DELETE FROM complaints WHERE complaint_id = ?", complaintId);
            }
            if (visitorId > 0) {
                delete(connection, "DELETE FROM visitors WHERE visitor_id = ?", visitorId);
            }
            if (feeId > 0) {
                delete(connection, "DELETE FROM fees WHERE fee_id = ?", feeId);
            }
            if (allocationId > 0) {
                delete(connection, "DELETE FROM allocations WHERE allocation_id = ?", allocationId);
            }
        }
        if (roomId > 0 && !roomDAO.deleteRoom(roomId)) {
            throw new SQLException("Integration test cleanup could not delete its room.");
        }
        if (studentId > 0 && !studentDAO.deleteStudent(studentId)) {
            throw new SQLException("Integration test cleanup could not delete its student.");
        }
    }

    private static void delete(Connection connection, String sql, Object value) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setObject(1, value);
            statement.executeUpdate();
        }
    }

    private static void expectSqlFailure(SqlOperation operation, String check) throws Exception {
        try {
            operation.run();
        } catch (SQLException expected) {
            pass(check);
            return;
        }
        throw new AssertionError(check + " did not reject the operation");
    }

    private static void require(boolean condition, String check) {
        if (!condition) {
            throw new AssertionError("Failed: " + check);
        }
    }

    private static void pass(String check) {
        passed++;
        System.out.println("[PASS] " + check);
    }

    @FunctionalInterface
    private interface SqlOperation {
        boolean run() throws SQLException;
    }
}

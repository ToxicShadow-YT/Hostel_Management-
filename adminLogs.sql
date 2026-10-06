-- =================================================================
-- HOSTEL MANAGEMENT SYSTEM - ACTIVITY LOGS AUDIT TRAIL
-- Table: activity_logs
-- Use this script to add or reset the activity_logs table
-- =================================================================

USE hostel_management;

-- Create Activity Logs Table
CREATE TABLE IF NOT EXISTS activity_logs (
    log_id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL,
    role VARCHAR(20) NOT NULL,
    action VARCHAR(50) NOT NULL,
    description TEXT NOT NULL,
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Sample Log Data for Demonstration
INSERT INTO activity_logs (username, role, action, description, timestamp) VALUES
('admin', 'Admin', 'SYSTEM_INIT', 'Hostel Management System database and initial records initialized.', '2025-07-15 09:00:00'),
('admin', 'Admin', 'STUDENT_ADD', 'Registered student Aarav Sharma (Roll No: CS202401).', '2025-07-15 09:30:00'),
('admin', 'Admin', 'ROOM_ALLOCATE', 'Allocated Room A-101 to student Aarav Sharma.', '2025-07-15 10:00:00'),
('warden', 'Staff', 'FEE_PAID', 'Recorded fee payment of Rs. 35000.00 (UPI) for student Aarav Sharma.', '2025-07-15 10:15:00'),
('admin', 'Admin', 'STUDENT_ADD', 'Registered student Priya Patil (Roll No: IT202403).', '2025-07-20 11:00:00'),
('admin', 'Admin', 'ROOM_ALLOCATE', 'Allocated Room A-102 to student Priya Patil.', '2025-07-20 11:30:00'),
('warden', 'Staff', 'VISITOR_CHECKIN', 'Recorded visitor entry: Ramesh Sharma visiting Aarav Sharma.', '2025-09-10 10:00:00'),
('warden', 'Staff', 'VISITOR_CHECKOUT', 'Recorded visitor exit: Ramesh Sharma (Out: 12:30 PM).', '2025-09-10 12:30:00'),
('warden', 'Staff', 'COMPLAINT_ADD', 'Registered Electrical complaint for student Aarav Sharma: Ceiling fan issue.', '2025-09-12 14:00:00'),
('admin', 'Admin', 'COMPLAINT_UPDATE', 'Updated complaint #1 status to Resolved.', '2025-09-14 16:30:00'),
('admin', 'Admin', 'ROOM_VACATE', 'Vacated student Aditya Verma from Room A-103.', '2025-09-15 09:45:00');

-- Verify inserted logs
SELECT * FROM activity_logs ORDER BY timestamp DESC;

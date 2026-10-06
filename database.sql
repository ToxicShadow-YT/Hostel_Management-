-- =================================================================
-- HOSTEL MANAGEMENT SYSTEM - DATABASE SCRIPT
-- Database: MySQL
-- Recommended tool: MySQL Workbench or MySQL CLI
-- =================================================================

-- 1. Create Database
CREATE DATABASE IF NOT EXISTS hostel_management;
USE hostel_management;

-- 2. Drop existing tables if re-running (in dependency order)
DROP TABLE IF EXISTS activity_logs;
DROP TABLE IF EXISTS complaints;
DROP TABLE IF EXISTS visitors;
DROP TABLE IF EXISTS fees;
DROP TABLE IF EXISTS allocations;
DROP TABLE IF EXISTS rooms;
DROP TABLE IF EXISTS students;
DROP TABLE IF EXISTS users;

-- 3. Create Users Table (for Admin / Staff Login)
CREATE TABLE users (
    user_id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    role VARCHAR(20) NOT NULL DEFAULT 'Admin',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 4. Create Students Table
CREATE TABLE students (
    student_id INT AUTO_INCREMENT PRIMARY KEY,
    roll_no VARCHAR(20) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    gender VARCHAR(10) NOT NULL,
    phone VARCHAR(15) NOT NULL,
    email VARCHAR(100) NOT NULL,
    course VARCHAR(50) NOT NULL,
    year VARCHAR(20) NOT NULL,
    address VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 5. Create Rooms Table
CREATE TABLE rooms (
    room_id INT AUTO_INCREMENT PRIMARY KEY,
    room_number VARCHAR(20) NOT NULL UNIQUE,
    block VARCHAR(20) NOT NULL,
    floor INT NOT NULL,
    room_type VARCHAR(30) NOT NULL,
    capacity INT NOT NULL,
    occupied INT NOT NULL DEFAULT 0,
    status VARCHAR(20) NOT NULL DEFAULT 'Available'
);

-- 6. Create Allocations Table
CREATE TABLE allocations (
    allocation_id INT AUTO_INCREMENT PRIMARY KEY,
    student_id INT NOT NULL,
    room_id INT NOT NULL,
    allocation_date DATE NOT NULL,
    vacate_date DATE NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'Active',
    CONSTRAINT fk_alloc_student FOREIGN KEY (student_id) REFERENCES students(student_id) ON DELETE RESTRICT,
    CONSTRAINT fk_alloc_room FOREIGN KEY (room_id) REFERENCES rooms(room_id) ON DELETE RESTRICT
);

-- 7. Create Fees Table
CREATE TABLE fees (
    fee_id INT AUTO_INCREMENT PRIMARY KEY,
    student_id INT NOT NULL,
    amount DECIMAL(10,2) NOT NULL,
    payment_date DATE NOT NULL,
    payment_status VARCHAR(20) NOT NULL DEFAULT 'Pending',
    payment_mode VARCHAR(30) NOT NULL,
    remarks VARCHAR(255) NULL,
    CONSTRAINT fk_fee_student FOREIGN KEY (student_id) REFERENCES students(student_id) ON DELETE CASCADE
);

-- 8. Create Visitors Table
CREATE TABLE visitors (
    visitor_id INT AUTO_INCREMENT PRIMARY KEY,
    student_id INT NOT NULL,
    visitor_name VARCHAR(100) NOT NULL,
    relation VARCHAR(50) NOT NULL,
    phone VARCHAR(15) NOT NULL,
    visit_date DATE NOT NULL,
    in_time VARCHAR(20) NOT NULL,
    out_time VARCHAR(20) NULL,
    CONSTRAINT fk_visitor_student FOREIGN KEY (student_id) REFERENCES students(student_id) ON DELETE CASCADE
);

-- 9. Create Complaints Table
CREATE TABLE complaints (
    complaint_id INT AUTO_INCREMENT PRIMARY KEY,
    student_id INT NOT NULL,
    complaint_type VARCHAR(50) NOT NULL,
    description TEXT NOT NULL,
    complaint_date DATE NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'Pending',
    CONSTRAINT fk_complaint_student FOREIGN KEY (student_id) REFERENCES students(student_id) ON DELETE CASCADE
);

-- 10. Create Activity Logs Table (Audit Trail)
CREATE TABLE activity_logs (
    log_id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL,
    role VARCHAR(20) NOT NULL,
    action VARCHAR(50) NOT NULL,
    description TEXT NOT NULL,
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- =================================================================
-- SAMPLE DATA INSERTION FOR DEMONSTRATION
-- =================================================================

-- Default Users (Username / Password)
INSERT INTO users (username, password, full_name, role) VALUES
('admin', 'admin123', 'Hostel Chief Warden', 'Admin'),
('warden', 'warden123', 'Assistant Hostel Warden', 'Staff');

-- Sample Students
INSERT INTO students (roll_no, name, gender, phone, email, course, year, address) VALUES
('CS202401', 'Aarav Sharma', 'Male', '9876543210', 'aarav.sharma@example.com', 'Diploma in Computer Engg', '2nd Year', '45 Shivaji Nagar, Pune'),
('CS202402', 'Rohan Kulkarni', 'Male', '9822114455', 'rohan.k@example.com', 'Diploma in Computer Engg', '2nd Year', '12 Tilak Road, Nashik'),
('IT202403', 'Priya Patil', 'Female', '9765432190', 'priya.patil@example.com', 'Diploma in Information Tech', '3rd Year', '78 Sector 15, Navi Mumbai'),
('ME202404', 'Aditya Verma', 'Male', '9988776655', 'aditya.v@example.com', 'Diploma in Mechanical Engg', '1st Year', '23 Model Town, Nagpur'),
('EE202405', 'Sneha Deshmukh', 'Female', '9123456780', 'sneha.d@example.com', 'Diploma in Electrical Engg', '2nd Year', '104 Deccan Gymkhana, Pune'),
('CE202406', 'Kunal Joshi', 'Male', '9345678123', 'kunal.j@example.com', 'Diploma in Civil Engg', '1st Year', '56 MG Road, Kolhapur');

-- Sample Rooms
-- Room 101: Double room with 2 active students -> Full
-- Room 102: Single room with 1 active student -> Full
-- Room 103: Double room with 0 active students -> Available
-- Room 201: Triple room with 0 active students -> Available
-- Room 202: Double room with 0 active students -> Maintenance
-- Room 301: Single AC room with 0 active students -> Available
-- Room 302: Double AC room with 0 active students -> Available
INSERT INTO rooms (room_number, block, floor, room_type, capacity, occupied, status) VALUES
('A-101', 'Block A', 1, 'Double Non-AC', 2, 2, 'Full'),
('A-102', 'Block A', 1, 'Single Non-AC', 1, 1, 'Full'),
('A-103', 'Block A', 1, 'Double Non-AC', 2, 0, 'Available'),
('B-201', 'Block B', 2, 'Triple Non-AC', 3, 0, 'Available'),
('B-202', 'Block B', 2, 'Double Non-AC', 2, 0, 'Maintenance'),
('B-301', 'Block B', 3, 'Single AC', 1, 0, 'Available'),
('B-302', 'Block B', 3, 'Double AC', 2, 0, 'Available');

-- Sample Allocations (3 Active allocations, 1 Vacated allocation)
-- Aarav Sharma (student_id: 1) -> A-101 (room_id: 1) [Active]
-- Rohan Kulkarni (student_id: 2) -> A-101 (room_id: 1) [Active]
-- Priya Patil (student_id: 3) -> A-102 (room_id: 2) [Active]
-- Aditya Verma (student_id: 4) -> A-103 (room_id: 3) [Vacated earlier]
INSERT INTO allocations (student_id, room_id, allocation_date, vacate_date, status) VALUES
(1, 1, '2025-07-15', NULL, 'Active'),
(2, 1, '2025-07-16', NULL, 'Active'),
(3, 2, '2025-07-20', NULL, 'Active'),
(4, 3, '2025-01-10', '2025-06-30', 'Vacated');

-- Sample Fees
INSERT INTO fees (student_id, amount, payment_date, payment_status, payment_mode, remarks) VALUES
(1, 35000.00, '2025-07-15', 'Paid', 'UPI', 'Annual hostel fee - Term 1'),
(2, 35000.00, '2025-07-16', 'Paid', 'Bank Transfer', 'Annual hostel fee - Term 1'),
(3, 45000.00, '2025-07-20', 'Paid', 'Cash', 'Single room annual fee'),
(4, 35000.00, '2025-08-01', 'Pending', 'UPI', 'Installment 2 pending'),
(5, 35000.00, '2025-08-05', 'Pending', 'Cash', 'Awaiting DD submission');

-- Sample Visitors
INSERT INTO visitors (student_id, visitor_name, relation, phone, visit_date, in_time, out_time) VALUES
(1, 'Ramesh Sharma', 'Father', '9811223344', '2025-09-10', '10:00 AM', '12:30 PM'),
(2, 'Sunita Kulkarni', 'Mother', '9822334455', '2025-09-15', '03:15 PM', '05:00 PM'),
(3, 'Neha Patil', 'Sister', '9833445566', '2025-09-22', '11:00 AM', '01:00 PM'),
(4, 'Suresh Verma', 'Guardian', '9844556677', '2025-10-01', '04:30 PM', NULL);

-- Sample Complaints
INSERT INTO complaints (student_id, complaint_type, description, complaint_date, status) VALUES
(1, 'Electrical', 'Ceiling fan in room A-101 is making loud noise and rotating slowly.', '2025-09-12', 'Resolved'),
(2, 'Plumbing', 'Bathroom tap leaking continuously in Block A 1st floor.', '2025-10-02', 'In Progress'),
(3, 'Internet', 'Wi-Fi router on 2nd floor has very weak signal in corner rooms.', '2025-10-04', 'Pending'),
(4, 'Cleanliness', 'Dustbin in corridor has not been emptied for 2 days.', '2025-10-05', 'Pending');

-- Sample Activity Logs
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

-- Verify tables and counts
SELECT 'users' AS table_name, COUNT(*) AS count FROM users
UNION ALL
SELECT 'students', COUNT(*) FROM students
UNION ALL
SELECT 'rooms', COUNT(*) FROM rooms
UNION ALL
SELECT 'allocations', COUNT(*) FROM allocations
UNION ALL
SELECT 'fees', COUNT(*) FROM fees
UNION ALL
SELECT 'visitors', COUNT(*) FROM visitors
UNION ALL
SELECT 'complaints', COUNT(*) FROM complaints
UNION ALL
SELECT 'activity_logs', COUNT(*) FROM activity_logs;

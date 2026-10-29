-- Non-destructive first-time schema setup for Hostel Management System.
-- Safe to run more than once: existing tables and records are not dropped.
CREATE DATABASE IF NOT EXISTS hostel_management
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;
USE hostel_management;

CREATE TABLE IF NOT EXISTS users (
    user_id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    role VARCHAR(20) NOT NULL DEFAULT 'Admin',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS students (
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
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS student_accounts (
    student_id INT PRIMARY KEY,
    password VARCHAR(100) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_student_account_student FOREIGN KEY (student_id)
        REFERENCES students(student_id) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS rooms (
    room_id INT AUTO_INCREMENT PRIMARY KEY,
    room_number VARCHAR(20) NOT NULL UNIQUE,
    block VARCHAR(20) NOT NULL,
    floor INT NOT NULL,
    room_type VARCHAR(30) NOT NULL,
    capacity INT NOT NULL,
    occupied INT NOT NULL DEFAULT 0,
    status VARCHAR(20) NOT NULL DEFAULT 'Available'
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS allocations (
    allocation_id INT AUTO_INCREMENT PRIMARY KEY,
    student_id INT NOT NULL,
    room_id INT NOT NULL,
    allocation_date DATE NOT NULL,
    vacate_date DATE NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'Active',
    CONSTRAINT fk_alloc_student FOREIGN KEY (student_id)
        REFERENCES students(student_id) ON DELETE RESTRICT,
    CONSTRAINT fk_alloc_room FOREIGN KEY (room_id)
        REFERENCES rooms(room_id) ON DELETE RESTRICT
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS fees (
    fee_id INT AUTO_INCREMENT PRIMARY KEY,
    student_id INT NOT NULL,
    amount DECIMAL(10,2) NOT NULL,
    payment_date DATE NOT NULL,
    payment_status VARCHAR(20) NOT NULL DEFAULT 'Pending',
    payment_mode VARCHAR(30) NOT NULL,
    remarks VARCHAR(255) NULL,
    CONSTRAINT fk_fee_student FOREIGN KEY (student_id)
        REFERENCES students(student_id) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS visitors (
    visitor_id INT AUTO_INCREMENT PRIMARY KEY,
    student_id INT NOT NULL,
    visitor_name VARCHAR(100) NOT NULL,
    relation VARCHAR(50) NOT NULL,
    phone VARCHAR(15) NOT NULL,
    visit_date DATE NOT NULL,
    in_time VARCHAR(20) NOT NULL,
    out_time VARCHAR(20) NULL,
    CONSTRAINT fk_visitor_student FOREIGN KEY (student_id)
        REFERENCES students(student_id) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS complaints (
    complaint_id INT AUTO_INCREMENT PRIMARY KEY,
    student_id INT NOT NULL,
    complaint_type VARCHAR(50) NOT NULL,
    description TEXT NOT NULL,
    complaint_date DATE NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'Pending',
    CONSTRAINT fk_complaint_student FOREIGN KEY (student_id)
        REFERENCES students(student_id) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS activity_logs (
    log_id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL,
    role VARCHAR(20) NOT NULL,
    action VARCHAR(50) NOT NULL,
    description TEXT NOT NULL,
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- Local demonstration accounts; existing accounts and passwords are not changed.
INSERT IGNORE INTO users (username, password, full_name, role) VALUES
    ('admin', 'admin123', 'Hostel Chief Warden', 'Admin'),
    ('warden', 'warden123', 'Assistant Hostel Warden', 'Staff');

SELECT table_name
FROM information_schema.tables
WHERE table_schema = 'hostel_management'
ORDER BY table_name;

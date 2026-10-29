-- Idempotent demonstration records for Hostel Management System.
-- Adds only missing records; it never drops tables or replaces user data.
USE hostel_management;

INSERT IGNORE INTO students (roll_no, name, gender, phone, email, course, year, address) VALUES
    ('CS202401', 'Aarav Sharma', 'Male', '9876543210', 'aarav.sharma@example.com',
     'Diploma in Computer Engg', '2nd Year', '45 Shivaji Nagar, Pune'),
    ('CS202402', 'Rohan Kulkarni', 'Male', '9822114455', 'rohan.k@example.com',
     'Diploma in Computer Engg', '2nd Year', '12 Tilak Road, Nashik'),
    ('IT202403', 'Priya Patil', 'Female', '9765432190', 'priya.patil@example.com',
     'Diploma in Information Tech', '3rd Year', '78 Sector 15, Navi Mumbai'),
    ('ME202404', 'Aditya Verma', 'Male', '9988776655', 'aditya.v@example.com',
     'Diploma in Mechanical Engg', '1st Year', '23 Model Town, Nagpur'),
    ('EE202405', 'Sneha Deshmukh', 'Female', '9123456780', 'sneha.d@example.com',
     'Diploma in Electrical Engg', '2nd Year', '104 Deccan Gymkhana, Pune'),
    ('CE202406', 'Kunal Joshi', 'Male', '9345678123', 'kunal.j@example.com',
     'Diploma in Civil Engg', '1st Year', '56 MG Road, Kolhapur');

INSERT INTO student_accounts (student_id, password)
SELECT s.student_id,
       'pbkdf2-sha256$210000$L1UiC9vIiIAL+P9/nwVw5Q==$ZZzY4r9BLJy43Eog3oITrI4NKypI45SvIxoejhJlFYA='
FROM students s
WHERE s.roll_no = 'CS202401'
  AND NOT EXISTS (
      SELECT 1 FROM student_accounts sa WHERE sa.student_id = s.student_id
  );

INSERT IGNORE INTO rooms (room_number, block, floor, room_type, capacity, occupied, status) VALUES
    ('A-101', 'Block A', 1, 'Double Non-AC', 2, 0, 'Available'),
    ('A-102', 'Block A', 1, 'Single Non-AC', 1, 0, 'Available'),
    ('A-103', 'Block A', 1, 'Double Non-AC', 2, 0, 'Available'),
    ('B-201', 'Block B', 2, 'Triple Non-AC', 3, 0, 'Available'),
    ('B-202', 'Block B', 2, 'Double Non-AC', 2, 0, 'Maintenance'),
    ('B-301', 'Block B', 3, 'Single AC', 1, 0, 'Available'),
    ('B-302', 'Block B', 3, 'Double AC', 2, 0, 'Available');

INSERT INTO allocations (student_id, room_id, allocation_date, vacate_date, status)
SELECT s.student_id, r.room_id, seed.allocation_date, seed.vacate_date, seed.status
FROM (
    SELECT 'CS202401' AS roll_no, 'A-101' AS room_number,
           DATE('2025-07-15') AS allocation_date, NULL AS vacate_date, 'Active' AS status
    UNION ALL SELECT 'CS202402', 'A-101', DATE('2025-07-16'), NULL, 'Active'
    UNION ALL SELECT 'IT202403', 'A-102', DATE('2025-07-20'), NULL, 'Active'
    UNION ALL SELECT 'ME202404', 'A-103', DATE('2025-01-10'), DATE('2025-06-30'), 'Vacated'
) AS seed
JOIN students s ON s.roll_no = seed.roll_no
JOIN rooms r ON r.room_number = seed.room_number
WHERE NOT EXISTS (
    SELECT 1
    FROM allocations existing
    WHERE existing.student_id = s.student_id
      AND existing.room_id = r.room_id
      AND existing.allocation_date = seed.allocation_date
      AND existing.status = seed.status
);

UPDATE rooms r
SET r.occupied = (
        SELECT COUNT(*)
        FROM allocations a
        WHERE a.room_id = r.room_id AND a.status = 'Active'
    ),
    r.status = CASE
        WHEN r.status = 'Maintenance' THEN 'Maintenance'
        WHEN (SELECT COUNT(*) FROM allocations a
              WHERE a.room_id = r.room_id AND a.status = 'Active') >= r.capacity THEN 'Full'
        ELSE 'Available'
    END
WHERE r.room_number IN ('A-101', 'A-102', 'A-103', 'B-201', 'B-202', 'B-301', 'B-302');

INSERT INTO fees (student_id, amount, payment_date, payment_status, payment_mode, remarks)
SELECT s.student_id, seed.amount, seed.payment_date, seed.payment_status, seed.payment_mode, seed.remarks
FROM (
    SELECT 'CS202401' AS roll_no, 35000.00 AS amount, DATE('2025-07-15') AS payment_date,
           'Paid' AS payment_status, 'UPI' AS payment_mode, 'Annual hostel fee - Term 1' AS remarks
    UNION ALL SELECT 'CS202402', 35000.00, DATE('2025-07-16'), 'Paid', 'Bank Transfer', 'Annual hostel fee - Term 1'
    UNION ALL SELECT 'IT202403', 45000.00, DATE('2025-07-20'), 'Paid', 'Cash', 'Single room annual fee'
    UNION ALL SELECT 'ME202404', 35000.00, DATE('2025-08-01'), 'Pending', 'UPI', 'Installment 2 pending'
    UNION ALL SELECT 'EE202405', 35000.00, DATE('2025-08-05'), 'Pending', 'Cash', 'Awaiting DD submission'
) AS seed
JOIN students s ON s.roll_no = seed.roll_no
WHERE NOT EXISTS (
    SELECT 1 FROM fees f
    WHERE f.student_id = s.student_id
      AND f.amount = seed.amount
      AND f.payment_date = seed.payment_date
      AND f.payment_status = seed.payment_status
      AND f.payment_mode = seed.payment_mode
      AND f.remarks = seed.remarks
);

INSERT INTO visitors (student_id, visitor_name, relation, phone, visit_date, in_time, out_time)
SELECT s.student_id, seed.visitor_name, seed.relation, seed.phone, seed.visit_date, seed.in_time, seed.out_time
FROM (
    SELECT 'CS202401' AS roll_no, 'Ramesh Sharma' AS visitor_name, 'Father' AS relation,
           '9811223344' AS phone, DATE('2025-09-10') AS visit_date, '10:00 AM' AS in_time, '12:30 PM' AS out_time
    UNION ALL SELECT 'CS202402', 'Sunita Kulkarni', 'Mother', '9822334455', DATE('2025-09-15'), '03:15 PM', '05:00 PM'
    UNION ALL SELECT 'IT202403', 'Neha Patil', 'Sister', '9833445566', DATE('2025-09-22'), '11:00 AM', '01:00 PM'
    UNION ALL SELECT 'ME202404', 'Suresh Verma', 'Guardian', '9844556677', DATE('2025-10-01'), '04:30 PM', NULL
) AS seed
JOIN students s ON s.roll_no = seed.roll_no
WHERE NOT EXISTS (
    SELECT 1 FROM visitors v
    WHERE v.student_id = s.student_id
      AND v.visitor_name = seed.visitor_name
      AND v.visit_date = seed.visit_date
      AND v.in_time = seed.in_time
);

INSERT INTO complaints (student_id, complaint_type, description, complaint_date, status)
SELECT s.student_id, seed.complaint_type, seed.description, seed.complaint_date, seed.status
FROM (
    SELECT 'CS202401' AS roll_no, 'Electrical' AS complaint_type,
           'Ceiling fan in room A-101 is making loud noise and rotating slowly.' AS description,
           DATE('2025-09-12') AS complaint_date, 'Resolved' AS status
    UNION ALL SELECT 'CS202402', 'Plumbing', 'Bathroom tap leaking continuously in Block A 1st floor.',
           DATE('2025-10-02'), 'In Progress'
    UNION ALL SELECT 'IT202403', 'Internet', 'Wi-Fi router on the third floor disconnects frequently.',
           DATE('2025-10-05'), 'Pending'
) AS seed
JOIN students s ON s.roll_no = seed.roll_no
WHERE NOT EXISTS (
    SELECT 1 FROM complaints c
    WHERE c.student_id = s.student_id
      AND c.complaint_type = seed.complaint_type
      AND c.complaint_date = seed.complaint_date
      AND c.description = seed.description
);

INSERT INTO activity_logs (username, role, action, description, timestamp)
SELECT seed.username, seed.role, seed.action, seed.description, seed.log_timestamp
FROM (
    SELECT 'admin' AS username, 'Admin' AS role, 'SYSTEM_INIT' AS action,
           'Hostel Management System demo records initialized.' AS description,
           TIMESTAMP('2025-07-15 09:00:00') AS log_timestamp
    UNION ALL SELECT 'admin', 'Admin', 'STUDENT_ADD', 'Registered student Aarav Sharma (Roll No: CS202401).',
           TIMESTAMP('2025-07-15 09:30:00')
    UNION ALL SELECT 'admin', 'Admin', 'ROOM_ALLOCATE', 'Allocated Room A-101 to student Aarav Sharma.',
           TIMESTAMP('2025-07-15 10:00:00')
    UNION ALL SELECT 'warden', 'Staff', 'FEE_PAID', 'Recorded fee payment for student Aarav Sharma.',
           TIMESTAMP('2025-07-15 10:15:00')
    UNION ALL SELECT 'admin', 'Admin', 'STUDENT_ADD', 'Registered student Priya Patil (Roll No: IT202403).',
           TIMESTAMP('2025-07-20 11:00:00')
    UNION ALL SELECT 'admin', 'Admin', 'ROOM_ALLOCATE', 'Allocated Room A-102 to student Priya Patil.',
           TIMESTAMP('2025-07-20 11:30:00')
    UNION ALL SELECT 'warden', 'Staff', 'VISITOR_CHECKIN', 'Recorded visitor entry for Aarav Sharma.',
           TIMESTAMP('2025-09-10 10:00:00')
    UNION ALL SELECT 'warden', 'Staff', 'VISITOR_CHECKOUT', 'Recorded visitor exit for Aarav Sharma.',
           TIMESTAMP('2025-09-10 12:30:00')
    UNION ALL SELECT 'warden', 'Staff', 'COMPLAINT_ADD', 'Registered Electrical complaint for Aarav Sharma.',
           TIMESTAMP('2025-09-12 14:00:00')
    UNION ALL SELECT 'admin', 'Admin', 'COMPLAINT_UPDATE', 'Updated complaint #1 status to Resolved.',
           TIMESTAMP('2025-09-14 16:30:00')
    UNION ALL SELECT 'admin', 'Admin', 'ROOM_VACATE', 'Vacated student Aditya Verma from Room A-103.',
           TIMESTAMP('2025-09-15 09:45:00')
) AS seed
WHERE NOT EXISTS (
    SELECT 1 FROM activity_logs l
    WHERE l.action = seed.action
      AND l.username = seed.username
      AND l.description = seed.description
);

SELECT 'users' AS table_name, COUNT(*) AS row_count FROM users
UNION ALL SELECT 'students', COUNT(*) FROM students
UNION ALL SELECT 'rooms', COUNT(*) FROM rooms
UNION ALL SELECT 'allocations', COUNT(*) FROM allocations
UNION ALL SELECT 'fees', COUNT(*) FROM fees
UNION ALL SELECT 'visitors', COUNT(*) FROM visitors
UNION ALL SELECT 'complaints', COUNT(*) FROM complaints
UNION ALL SELECT 'activity_logs', COUNT(*) FROM activity_logs;
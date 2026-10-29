# Hostel Management System Requirements

## 1. Purpose

Provide a local desktop application for managing hostel operations and a restricted portal through which each enrolled student can view their own records.

## 2. Runtime requirements

- Java Development Kit (JDK) 21 or later.
- MySQL Server, with a database account configured in the local, untracked `db.properties` file.
- MySQL Connector/J supplied in `lib/mysql-connector-j-8.3.0.jar`.
- A graphical desktop session for running the Swing user interface.
- MySQL Workbench is optional; a MySQL command-line client can be used to apply SQL scripts.

## 3. Functional requirements

### Staff authentication and permissions

- Administrators and wardens/staff sign in with accounts from the `users` table.
- An administrator can manage student and room records, room allocations, fees, visitors, and complaints, and can view activity/audit logs.
- A warden/staff member can manage student and room records, room allocations, fees, visitors, and complaints, but cannot open the activity/audit-log screen.
- Staff passwords must be verified securely. Existing legacy staff passwords are upgraded to salted PBKDF2 hashes after successful sign-in.

### Student authentication and permissions

- Students use the separate Student Portal sign-in screen and authenticate with their enrolled roll number and a password.
- A student without an account may activate one using their roll number and the phone number recorded on their student record. Account activation is one-time per student.
- Student passwords are stored as salted PBKDF2 hashes, must contain at least eight characters, and can be changed by the signed-in student after verifying the current password.
- Students can view only their own profile, active room allocation, fee records, complaint history, and visitor records.
- Students can submit complaints for themselves.
- Students cannot enter staff screens, view other students' records, edit staff-managed records, or view activity/audit logs.

### Hostel operations

- Staff can create, view, update, search, and delete student and room records, subject to database relationships.
- Staff can allocate a room to an eligible student, prevent duplicate active allocations, and vacate rooms while maintaining room occupancy.
- Staff can record fees and payment status, visitor entry and exit, and complaints and their status.
- The dashboard displays current hostel statistics and recent allocations and complaints.
- Administrative actions are recorded in the activity log.

## 4. Data and setup requirements

- MySQL stores staff users, student profiles, student credentials, rooms, allocations, fees, visitors, complaints, and activity logs.
- `database-setup.sql` must create missing tables without deleting existing data.
- `database-demo-data.sql` must be safe to run more than once and must not overwrite an existing student's password.
- `database.sql` is a destructive reset-and-demo script and must only be used when intentionally replacing all database contents.
- Database credentials must remain in the local, Git-ignored `db.properties` file and must not be committed.

## 5. Quality and security requirements

- Database access must use prepared statements for user-supplied values.
- A student's portal data must be queried using the authenticated student's database ID, not a student ID supplied by the UI.
- Database operations that may take time must not block Swing's Event Dispatch Thread.
- Errors during sign-in or data operations must be shown to the user and reported for diagnosis; failed authentication must not grant access.
- The application must compile with JDK 21 or later and pass the self-tests and MySQL integration tests documented in the README.

package model;

import java.sql.Date;

/**
 * Model representing a Student Complaint.
 */
public class Complaint {
    private int complaintId;
    private int studentId;
    private String studentRollNo;
    private String studentName;
    private String complaintType; // 'Electrical', 'Plumbing', 'Cleanliness', 'Food/Mess', 'Furniture', 'Internet', 'Other'
    private String description;
    private Date complaintDate;
    private String status; // 'Pending', 'In Progress', 'Resolved'

    public Complaint() {
    }

    public Complaint(int complaintId, int studentId, String studentRollNo, String studentName,
                     String complaintType, String description, Date complaintDate, String status) {
        this.complaintId = complaintId;
        this.studentId = studentId;
        this.studentRollNo = studentRollNo;
        this.studentName = studentName;
        this.complaintType = complaintType;
        this.description = description;
        this.complaintDate = complaintDate;
        this.status = status;
    }

    public Complaint(int studentId, String complaintType, String description, Date complaintDate, String status) {
        this(0, studentId, "", "", complaintType, description, complaintDate, status);
    }

    public int getComplaintId() {
        return complaintId;
    }

    public void setComplaintId(int complaintId) {
        this.complaintId = complaintId;
    }

    public int getStudentId() {
        return studentId;
    }

    public void setStudentId(int studentId) {
        this.studentId = studentId;
    }

    public String getStudentRollNo() {
        return studentRollNo;
    }

    public void setStudentRollNo(String studentRollNo) {
        this.studentRollNo = studentRollNo;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public String getComplaintType() {
        return complaintType;
    }

    public void setComplaintType(String complaintType) {
        this.complaintType = complaintType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Date getComplaintDate() {
        return complaintDate;
    }

    public void setComplaintDate(Date complaintDate) {
        this.complaintDate = complaintDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "Complaint #" + complaintId + " [" + complaintType + "] - " + studentName + " (" + status + ")";
    }
}

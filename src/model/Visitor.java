package model;

import java.sql.Date;

/**
 * Model representing a Visitor record.
 */
public class Visitor {
    private int visitorId;
    private int studentId;
    private String studentRollNo;
    private String studentName;
    private String visitorName;
    private String relation;
    private String phone;
    private Date visitDate;
    private String inTime;
    private String outTime;

    public Visitor() {
    }

    public Visitor(int visitorId, int studentId, String studentRollNo, String studentName,
                   String visitorName, String relation, String phone, 
                   Date visitDate, String inTime, String outTime) {
        this.visitorId = visitorId;
        this.studentId = studentId;
        this.studentRollNo = studentRollNo;
        this.studentName = studentName;
        this.visitorName = visitorName;
        this.relation = relation;
        this.phone = phone;
        this.visitDate = visitDate;
        this.inTime = inTime;
        this.outTime = outTime;
    }

    public Visitor(int studentId, String visitorName, String relation, 
                   String phone, Date visitDate, String inTime, String outTime) {
        this(0, studentId, "", "", visitorName, relation, phone, visitDate, inTime, outTime);
    }

    public int getVisitorId() {
        return visitorId;
    }

    public void setVisitorId(int visitorId) {
        this.visitorId = visitorId;
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

    public String getVisitorName() {
        return visitorName;
    }

    public void setVisitorName(String visitorName) {
        this.visitorName = visitorName;
    }

    public String getRelation() {
        return relation;
    }

    public void setRelation(String relation) {
        this.relation = relation;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public Date getVisitDate() {
        return visitDate;
    }

    public void setVisitDate(Date visitDate) {
        this.visitDate = visitDate;
    }

    public String getInTime() {
        return inTime;
    }

    public void setInTime(String inTime) {
        this.inTime = inTime;
    }

    public String getOutTime() {
        return outTime;
    }

    public void setOutTime(String outTime) {
        this.outTime = outTime;
    }

    @Override
    public String toString() {
        return visitorName + " (Visiting: " + studentName + ")";
    }
}

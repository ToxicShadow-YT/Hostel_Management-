package model;

import java.sql.Date;

/**
 * Model representing a Room Allocation to a Student.
 */
public class Allocation {
    private int allocationId;
    private int studentId;
    private String studentRollNo;
    private String studentName;
    private int roomId;
    private String roomNumber;
    private Date allocationDate;
    private Date vacateDate;
    private String status; // 'Active', 'Vacated'

    public Allocation() {
    }

    public Allocation(int allocationId, int studentId, String studentRollNo, String studentName,
                      int roomId, String roomNumber, Date allocationDate, Date vacateDate, String status) {
        this.allocationId = allocationId;
        this.studentId = studentId;
        this.studentRollNo = studentRollNo;
        this.studentName = studentName;
        this.roomId = roomId;
        this.roomNumber = roomNumber;
        this.allocationDate = allocationDate;
        this.vacateDate = vacateDate;
        this.status = status;
    }

    public Allocation(int studentId, int roomId, Date allocationDate, Date vacateDate, String status) {
        this(0, studentId, "", "", roomId, "", allocationDate, vacateDate, status);
    }

    public int getAllocationId() {
        return allocationId;
    }

    public void setAllocationId(int allocationId) {
        this.allocationId = allocationId;
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

    public int getRoomId() {
        return roomId;
    }

    public void setRoomId(int roomId) {
        this.roomId = roomId;
    }

    public String getRoomNumber() {
        return roomNumber;
    }

    public void setRoomNumber(String roomNumber) {
        this.roomNumber = roomNumber;
    }

    public Date getAllocationDate() {
        return allocationDate;
    }

    public void setAllocationDate(Date allocationDate) {
        this.allocationDate = allocationDate;
    }

    public Date getVacateDate() {
        return vacateDate;
    }

    public void setVacateDate(Date vacateDate) {
        this.vacateDate = vacateDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "Allocation #" + allocationId + " - " + studentName + " in Room " + roomNumber + " (" + status + ")";
    }
}

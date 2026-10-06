package model;

import java.math.BigDecimal;
import java.sql.Date;

/**
 * Model representing a Fee payment record.
 */
public class Fee {
    private int feeId;
    private int studentId;
    private String studentRollNo;
    private String studentName;
    private BigDecimal amount;
    private Date paymentDate;
    private String paymentStatus; // 'Paid', 'Pending'
    private String paymentMode;   // 'Cash', 'UPI', 'Card', 'Bank Transfer'
    private String remarks;

    public Fee() {
    }

    public Fee(int feeId, int studentId, String studentRollNo, String studentName,
               BigDecimal amount, Date paymentDate, String paymentStatus, 
               String paymentMode, String remarks) {
        this.feeId = feeId;
        this.studentId = studentId;
        this.studentRollNo = studentRollNo;
        this.studentName = studentName;
        this.amount = amount;
        this.paymentDate = paymentDate;
        this.paymentStatus = paymentStatus;
        this.paymentMode = paymentMode;
        this.remarks = remarks;
    }

    public Fee(int studentId, BigDecimal amount, Date paymentDate, 
               String paymentStatus, String paymentMode, String remarks) {
        this(0, studentId, "", "", amount, paymentDate, paymentStatus, paymentMode, remarks);
    }

    public int getFeeId() {
        return feeId;
    }

    public void setFeeId(int feeId) {
        this.feeId = feeId;
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

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public Date getPaymentDate() {
        return paymentDate;
    }

    public void setPaymentDate(Date paymentDate) {
        this.paymentDate = paymentDate;
    }

    public String getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(String paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public String getPaymentMode() {
        return paymentMode;
    }

    public void setPaymentMode(String paymentMode) {
        this.paymentMode = paymentMode;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    @Override
    public String toString() {
        return "Fee #" + feeId + " - " + studentName + " - ₹" + amount + " (" + paymentStatus + ")";
    }
}

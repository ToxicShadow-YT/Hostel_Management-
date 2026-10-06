package model;

/**
 * Model representing a Student.
 */
public class Student {
    private int studentId;
    private String rollNo;
    private String name;
    private String gender;
    private String phone;
    private String email;
    private String course;
    private String year;
    private String address;

    public Student() {
    }

    public Student(int studentId, String rollNo, String name, String gender, 
                   String phone, String email, String course, String year, String address) {
        this.studentId = studentId;
        this.rollNo = rollNo;
        this.name = name;
        this.gender = gender;
        this.phone = phone;
        this.email = email;
        this.course = course;
        this.year = year;
        this.address = address;
    }

    public Student(String rollNo, String name, String gender, 
                   String phone, String email, String course, String year, String address) {
        this(0, rollNo, name, gender, phone, email, course, year, address);
    }

    public int getStudentId() {
        return studentId;
    }

    public void setStudentId(int studentId) {
        this.studentId = studentId;
    }

    public String getRollNo() {
        return rollNo;
    }

    public void setRollNo(String rollNo) {
        this.rollNo = rollNo;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getCourse() {
        return course;
    }

    public void setCourse(String course) {
        this.course = course;
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    @Override
    public String toString() {
        return rollNo + " - " + name;
    }
}

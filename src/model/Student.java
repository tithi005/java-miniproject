package model;

import java.util.Objects;

/**
 * Plain Java object (POJO) that represents one row of the "students" table.
 *
 * <p>This class only holds data. It knows nothing about SQL or the GUI,
 * which keeps the layers of the application loosely coupled: the UI talks
 * to the DAO, the DAO moves data in and out of {@code Student} objects, and
 * the model stays a simple, reusable value object.</p>
 */
public class Student {

    private String rollNo;   // unique identifier, e.g. "CS101"
    private String name;     // full name, e.g. "Aarav Sharma"
    private String course;   // e.g. "B.Sc Computer Science"
    private int year;        // study year, 1 - 4
    private String email;    // e.g. "aarav@example.com"
    private String phone;    // 10 digits, stored as text to keep leading zeros
    private String gender;   // "Male", "Female", "Other", ...

    /** No-argument constructor required by many frameworks / bean tools. */
    public Student() {
    }

    /**
     * Full constructor used when the DAO reads a row or the UI creates a record.
     */
    public Student(String rollNo, String name, String course, int year,
                   String email, String phone, String gender) {
        this.rollNo = rollNo;
        this.name = name;
        this.course = course;
        this.year = year;
        this.email = email;
        this.phone = phone;
        this.gender = gender;
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

    public String getCourse() {
        return course;
    }

    public void setCourse(String course) {
        this.course = course;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    /**
     * Readable representation, handy for console tests and debugging.
     * The GUI will normally read individual getters instead.
     */
    @Override
    public String toString() {
        return "Student{" +
                "rollNo='" + rollNo + '\'' +
                ", name='" + name + '\'' +
                ", course='" + course + '\'' +
                ", year=" + year +
                ", email='" + email + '\'' +
                ", phone='" + phone + '\'' +
                ", gender='" + gender + '\'' +
                '}';
    }

    /**
     * Two students are the same record when their roll numbers match,
     * because roll_no is the primary key in the database.
     */
    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof Student)) {
            return false;
        }
        Student that = (Student) other;
        return Objects.equals(rollNo, that.rollNo);
    }

    @Override
    public int hashCode() {
        return Objects.hash(rollNo);
    }
}
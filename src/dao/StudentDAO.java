package dao;

import model.Student;
import util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Data Access Object for the {@code students} table.
 *
 * <p>All SQL lives in this class. The rest of the application (console test
 * now, Swing screens later) calls these methods and never touches JDBC
 * directly. Every method opens its own connection and closes it through
 * try-with-resources, so no connections leak even when an exception occurs.</p>
 */
public class StudentDAO {

    private static final String COLUMNS =
            "roll_no, name, course, year, email, phone, gender";

    /**
     * Inserts a new student.
     *
     * @param student the record to insert
     * @throws SQLException if the roll number already exists (clear message),
     *                      or for any other database error
     */
    public void addStudent(Student student) throws SQLException {
        if (student == null) {
            throw new IllegalArgumentException("Student cannot be null.");
        }
        String sql = "INSERT INTO students (" + COLUMNS + ") VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setString(1, student.getRollNo());
            ps.setString(2, student.getName());
            ps.setString(3, student.getCourse());
            ps.setInt(4, student.getYear());
            ps.setString(5, student.getEmail());
            ps.setString(6, student.getPhone());
            ps.setString(7, student.getGender());
            ps.executeUpdate();

        } catch (SQLException e) {
            // SQLite reports a PRIMARY KEY violation as a UNIQUE/constraint error.
            if (isUniqueViolation(e)) {
                throw new SQLException(
                        "Cannot add student: roll number '" + student.getRollNo()
                                + "' already exists.", e);
            }
            throw e;
        }
    }

    /** @return every student, sorted by roll number. */
    public List<Student> getAllStudents() throws SQLException {
        String sql = "SELECT " + COLUMNS + " FROM students ORDER BY roll_no";

        List<Student> students = new ArrayList<>();
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                students.add(mapRow(rs));
            }
        }
        return students;
    }

    /**
     * Updates every editable field of an existing student (the roll number is
     * the primary key and therefore the WHERE clause).
     *
     * @return true if a row was changed, false if no student has that roll no
     */
    public boolean updateStudent(Student student) throws SQLException {
        if (student == null) {
            throw new IllegalArgumentException("Student cannot be null.");
        }
        String sql = "UPDATE students SET name = ?, course = ?, year = ?, "
                + "email = ?, phone = ?, gender = ? WHERE roll_no = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setString(1, student.getName());
            ps.setString(2, student.getCourse());
            ps.setInt(3, student.getYear());
            ps.setString(4, student.getEmail());
            ps.setString(5, student.getPhone());
            ps.setString(6, student.getGender());
            ps.setString(7, student.getRollNo());
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Deletes the student with the given roll number.
     *
     * @return true if a row was deleted, false if it did not exist
     */
    public boolean deleteStudent(String rollNo) throws SQLException {
        if (rollNo == null || rollNo.trim().isEmpty()) {
            throw new IllegalArgumentException("Roll number cannot be empty.");
        }
        String sql = "DELETE FROM students WHERE roll_no = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setString(1, rollNo.trim());
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Partial, case-insensitive search across roll number, name and course.
     * An empty keyword simply returns all students.
     *
     * @param keyword text to look for, e.g. "comp" or "cs1"
     */
    public List<Student> searchStudents(String keyword) throws SQLException {
        if (keyword == null || keyword.trim().isEmpty()) {
            return getAllStudents();
        }
        String sql = "SELECT " + COLUMNS + " FROM students "
                + "WHERE roll_no LIKE ? COLLATE NOCASE "
                + "   OR name    LIKE ? COLLATE NOCASE "
                + "   OR course  LIKE ? COLLATE NOCASE "
                + "ORDER BY roll_no";

        List<Student> students = new ArrayList<>();
        String pattern = "%" + keyword.trim() + "%";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setString(1, pattern);
            ps.setString(2, pattern);
            ps.setString(3, pattern);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    students.add(mapRow(rs));
                }
            }
        }
        return students;
    }

    /** Converts the current result-set row into a Student object. */
    private Student mapRow(ResultSet rs) throws SQLException {
        Student student = new Student();
        student.setRollNo(rs.getString("roll_no"));
        student.setName(rs.getString("name"));
        student.setCourse(rs.getString("course"));
        student.setYear(rs.getInt("year"));
        student.setEmail(rs.getString("email"));
        student.setPhone(rs.getString("phone"));
        student.setGender(rs.getString("gender"));
        return student;
    }

    /** True when SQLite rejected the statement because of a duplicate key. */
    private boolean isUniqueViolation(SQLException e) {
        String message = e.getMessage() == null
                ? ""
                : e.getMessage().toLowerCase(Locale.ROOT);
        return message.contains("unique") || message.contains("constraint failed");
    }
}
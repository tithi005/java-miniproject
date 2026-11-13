package app;

import dao.AdminDAO;
import dao.StudentDAO;
import model.Student;
import util.Validator;

import java.sql.SQLException;
import java.util.List;

/**
 * Console smoke test for the backend (Part 1).
 *
 * <p>Run this class to prove that the database file is created, the tables
 * exist, and every DAO operation works. It adds one student, lists them,
 * searches, updates, deletes, and finally checks the default admin login.
 * The sample uses roll number "CS101"; any leftover copy is removed first so
 * the test can be run repeatedly.</p>
 *
 * <p>There is deliberately no Swing code here yet - the GUI arrives in a
 * later part and will call exactly the same DAO methods.</p>
 */
public class Main {

    public static void main(String[] args) {
        StudentDAO studentDAO = new StudentDAO();
        AdminDAO adminDAO = new AdminDAO();

        try {
            // ---------------------------------------------------------------
            // 0. Validation demo (no database involved)
            // ---------------------------------------------------------------
            System.out.println("== 0. Validator ==");
            printValidation("roll no", Validator.validateRollNo(" "));
            printValidation("name", Validator.validateName("Aarav123"));
            printValidation("email", Validator.validateEmail("not-an-email"));
            printValidation("phone", Validator.validatePhone("98765"));
            printValidation("year", Validator.validateYear(7));
            printValidation("valid record", Validator.validateStudent(
                    "CS101", "Aarav Sharma", "B.Sc Computer Science", 2,
                    "aarav@example.com", "9876543210"));

            // ---------------------------------------------------------------
            // 1. Clean start, then add
            // ---------------------------------------------------------------
            System.out.println("\n== 1. Add student ==");
            studentDAO.deleteStudent("CS101"); // ignore result: may not exist

            Student sample = new Student(
                    "CS101",
                    "Aarav Sharma",
                    "B.Sc Computer Science",
                    2,
                    "aarav@example.com",
                    "9876543210",
                    "Male");

            studentDAO.addStudent(sample);
            System.out.println("Added: " + sample);

            // ---------------------------------------------------------------
            // 2. Duplicate roll number must fail with a clear message
            // ---------------------------------------------------------------
            System.out.println("\n== 2. Duplicate check ==");
            try {
                studentDAO.addStudent(new Student(
                        "CS101", "Duplicate Student", "B.A English", 1,
                        "dup@example.com", "9000000000", "Female"));
                System.out.println("ERROR: duplicate was accepted!");
            } catch (SQLException e) {
                System.out.println("Rejected as expected -> " + e.getMessage());
            }

            // ---------------------------------------------------------------
            // 3. List all
            // ---------------------------------------------------------------
            System.out.println("\n== 3. All students ==");
            printStudents(studentDAO.getAllStudents());

            // ---------------------------------------------------------------
            // 4. Search (partial, case-insensitive)
            // ---------------------------------------------------------------
            System.out.println("\n== 4. Search \"comp\" ==");
            printStudents(studentDAO.searchStudents("comp"));

            // ---------------------------------------------------------------
            // 5. Update
            // ---------------------------------------------------------------
            System.out.println("\n== 5. Update year and phone ==");
            sample.setYear(3);
            sample.setPhone("9123456780");
            boolean updated = studentDAO.updateStudent(sample);
            System.out.println(updated ? "Updated: " + sample : "No row updated!");

            // ---------------------------------------------------------------
            // 6. Delete
            // ---------------------------------------------------------------
            System.out.println("\n== 6. Delete CS101 ==");
            boolean deleted = studentDAO.deleteStudent("CS101");
            System.out.println(deleted ? "Deleted." : "Nothing to delete.");
            System.out.println("Rows left: " + studentDAO.getAllStudents().size());

            // ---------------------------------------------------------------
            // 7. Default admin login
            // ---------------------------------------------------------------
            System.out.println("\n== 7. Admin login ==");
            System.out.println("admin/admin123 -> "
                    + adminDAO.authenticate("admin", "admin123"));
            System.out.println("admin/wrong    -> "
                    + adminDAO.authenticate("admin", "wrong"));

            System.out.println("\nBackend test finished successfully.");

        } catch (SQLException e) {
            // Any database problem ends up here with a readable message.
            System.err.println("Database error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void printStudents(List<Student> students) {
        if (students.isEmpty()) {
            System.out.println("(no students)");
            return;
        }
        for (Student student : students) {
            System.out.println("  " + student);
        }
    }

    private static void printValidation(String label, String error) {
        System.out.println("  " + label + " -> "
                + (error == null ? "valid" : "invalid: " + error));
    }
}
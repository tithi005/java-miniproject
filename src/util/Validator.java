package util;

import java.util.regex.Pattern;

/**
 * Stateless input validation for student data.
 *
 * <p>Every method follows the same contract: it returns {@code null} when the
 * value is valid, or a short, user-friendly error message when it is not.
 * That makes the methods easy to use from both the console and (later) the
 * Swing forms:</p>
 *
 * <pre>{@code
 * String error = Validator.validateEmail(field.getText());
 * if (error != null) { showMessage(error); }
 * }</pre>
 *
 * <p>Validation lives here, not in the DAO, so that business rules are
 * defined in exactly one place.</p>
 */
public final class Validator {

    /** Local part + "@" + domain with a dot + 2+ letter extension. */
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    /** Letters and spaces only, no digits or punctuation. */
    private static final Pattern NAME_PATTERN = Pattern.compile(
            "^[A-Za-z]+(?:\\s+[A-Za-z]+)*$");

    /** Exactly ten digits, nothing else. */
    private static final Pattern PHONE_PATTERN = Pattern.compile("^\\d{10}$");

    /** Utility class: no instances needed. */
    private Validator() {
    }

    /**
     * Roll number must be present. (Its uniqueness is enforced by the
     * database, not here.)
     *
     * @return null if valid, otherwise an error message
     */
    public static String validateRollNo(String rollNo) {
        if (rollNo == null || rollNo.trim().isEmpty()) {
            return "Roll number cannot be empty.";
        }
        if (rollNo.trim().length() > 20) {
            return "Roll number cannot be longer than 20 characters.";
        }
        return null;
    }

    /**
     * Name may contain letters and single spaces between words only
     * (for example "Aarav Sharma"). Empty values are rejected too.
     *
     * @return null if valid, otherwise an error message
     */
    public static String validateName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return "Name cannot be empty.";
        }
        if (!NAME_PATTERN.matcher(name.trim()).matches()) {
            return "Name may contain letters and spaces only.";
        }
        return null;
    }

    /**
     * Checks the email against a practical (not fully RFC-compliant) pattern.
     *
     * @return null if valid, otherwise an error message
     */
    public static String validateEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return "Email cannot be empty.";
        }
        if (!EMAIL_PATTERN.matcher(email.trim()).matches()) {
            return "Enter a valid email address, e.g. name@example.com.";
        }
        return null;
    }

    /**
     * Phone must be exactly 10 digits (no spaces, +91, or dashes).
     *
     * @return null if valid, otherwise an error message
     */
    public static String validatePhone(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            return "Phone number cannot be empty.";
        }
        if (!PHONE_PATTERN.matcher(phone.trim()).matches()) {
            return "Phone number must be exactly 10 digits.";
        }
        return null;
    }

    /**
     * Study year must be between 1 and 4.
     *
     * @return null if valid, otherwise an error message
     */
    public static String validateYear(int year) {
        if (year < 1 || year > 4) {
            return "Year must be between 1 and 4.";
        }
        return null;
    }

    /**
     * Convenience helper for the GUI: validates a whole record and returns
     * the first problem found, or null when everything is fine.
     */
    public static String validateStudent(String rollNo, String name, String course,
                                        int year, String email, String phone) {
        String error = validateRollNo(rollNo);
        if (error == null) error = validateName(name);
        if (error == null && (course == null || course.trim().isEmpty())) {
            error = "Course cannot be empty.";
        }
        if (error == null) error = validateYear(year);
        if (error == null) error = validateEmail(email);
        if (error == null) error = validatePhone(phone);
        return error;
    }
}
package dao;

import util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Data Access Object for the {@code admins} table.
 *
 * <p>The table is seeded with a default account (admin / admin123) when the
 * database is created. Credentials are compared in SQL using a prepared
 * statement, so the input is never concatenated into the query.</p>
 *
 * <p>Note: for a real production system, store salted password hashes
 * instead of plain text. Plain text is acceptable only for this learning
 * project.</p>
 */
public class AdminDAO {

    /**
     * Checks whether the username/password pair exists in the admins table.
     *
     * @return true when the credentials match, false otherwise
     */
    public boolean authenticate(String username, String password) throws SQLException {
        if (username == null || password == null) {
            return false;
        }
        String sql = "SELECT 1 FROM admins WHERE username = ? AND password = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setString(1, username.trim());
            ps.setString(2, password);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next(); // a row means the credentials are valid
            }
        }
    }
}
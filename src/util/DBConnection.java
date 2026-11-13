package util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Central place that knows how to reach the SQLite database.
 *
 * <p>Usage is "singleton-style": the class cannot be instantiated and every
 * caller uses the single static {@link #getConnection()} entry point. A fresh
 * JDBC {@link Connection} is returned per call because JDBC connections are
 * not meant to be shared between threads or UI actions; the shared state we
 * really care about (the database file and its tables) is created only once,
 * the first time a connection is requested.</p>
 *
 * <p>The database file is {@code studentdb.db} in the folder the program is
 * started from. It is created automatically by SQLite on first use. The URL
 * can be overridden with {@code -Dstudentdb.url=jdbc:sqlite:/full/path/db.db}.</p>
 */
public final class DBConnection {

    /** JDBC URL of the database; a relative path keeps the project portable. */
    private static final String URL =
            System.getProperty("studentdb.url", "jdbc:sqlite:studentdb.db");

    /** Guard so the CREATE TABLE statements run only once per JVM. */
    private static volatile boolean schemaReady = false;

    /*
     * Load the driver explicitly. Modern JDBC can auto-discover drivers, but
     * doing it here lets us fail with a helpful message when the sqlite-jdbc
     * jar is missing from the classpath.
     */
    static {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            throw new ExceptionInInitializerError(
                    "SQLite JDBC driver not found. Add sqlite-jdbc-x.y.z.jar "
                            + "to the project's lib folder / module dependencies.");
        }
    }

    /** Private constructor: this is a utility class, never instantiate it. */
    private DBConnection() {
    }

    /**
     * Opens a connection to the SQLite database and guarantees that the
     * schema (tables + default admin) exists.
     *
     * @return a ready-to-use connection; the caller must close it
     *         (preferably with try-with-resources)
     * @throws SQLException if the database cannot be opened or initialised
     */
    public static Connection getConnection() throws SQLException {
        Connection connection = DriverManager.getConnection(URL);
        boolean success = false;
        try {
            // Enforce foreign keys now, so future tables that reference
            // students behave correctly without extra setup.
            try (Statement pragma = connection.createStatement()) {
                pragma.execute("PRAGMA foreign_keys = ON");
            }

            // Double-checked locking: create tables on the first call only.
            if (!schemaReady) {
                synchronized (DBConnection.class) {
                    if (!schemaReady) {
                        initSchema(connection);
                        schemaReady = true;
                    }
                }
            }
            success = true;
            return connection;
        } finally {
            if (!success) {
                connection.close(); // never leak a half-initialised connection
            }
        }
    }

    /**
     * Creates the tables and seeds the default administrator, all with
     * {@code IF NOT EXISTS} / {@code OR IGNORE} so it is safe to run on an
     * existing database.
     */
    private static void initSchema(Connection connection) throws SQLException {
        String createStudents =
                "CREATE TABLE IF NOT EXISTS students (" +
                "  roll_no TEXT PRIMARY KEY," +
                "  name    TEXT NOT NULL," +
                "  course  TEXT NOT NULL," +
                "  year    INTEGER NOT NULL CHECK (year BETWEEN 1 AND 4)," +
                "  email   TEXT," +
                "  phone   TEXT," +
                "  gender  TEXT" +
                ")";

        String createAdmins =
                "CREATE TABLE IF NOT EXISTS admins (" +
                "  username TEXT PRIMARY KEY," +
                "  password TEXT NOT NULL" +
                ")";

        // INSERT OR IGNORE keeps any password the admin may have changed later.
        String seedAdmin =
                "INSERT OR IGNORE INTO admins (username, password) " +
                "VALUES ('admin', 'admin123')";

        try (Statement statement = connection.createStatement()) {
            statement.execute(createStudents);
            statement.execute(createAdmins);
            statement.execute(seedAdmin);
        }
    }
}
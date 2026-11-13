package ui;

import dao.AdminDAO;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.sql.SQLException;

/**
 * Login window shown before the main application.
 *
 * <p>The frame only collects credentials and delegates the real check to
 * {@link AdminDAO#authenticate(String, String)}. On success it opens
 * {@link MainFrame} and disposes itself; on failure it shows a dialog and
 * keeps the user on the login screen.</p>
 *
 * <p>Pressing Enter triggers Login because the button is registered as the
 * frame's default button (and the password field also has its own action
 * listener, which is helpful when focus starts there).</p>
 */
public class LoginFrame extends JFrame {

    private static final long serialVersionUID = 1L;

    private final AdminDAO adminDAO = new AdminDAO();

    private final JTextField usernameField = new JTextField(18);
    private final JPasswordField passwordField = new JPasswordField(18);
    private final JButton loginButton = new JButton("Login");
    private final JButton exitButton = new JButton("Exit");

    /** Builds and shows the login screen. */
    public LoginFrame() {
        super("Admin Login - Student Record Management");
        initUi();
    }

    /** Creates every component and wires the actions. */
    private void initUi() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        setResizable(false);

        JPanel card = new JPanel(new GridBagLayout());
        card.setBorder(BorderFactory.createEmptyBorder(28, 34, 28, 34));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // ---- Heading -------------------------------------------------
        JLabel title = new JLabel("Student Record Management");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 20f));

        JLabel subtitle = new JLabel("Sign in as administrator");
        subtitle.setFont(subtitle.getFont().deriveFont(Font.PLAIN, 12f));

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        card.add(title, gbc);

        gbc.gridy = 1;
        gbc.insets = new Insets(0, 6, 18, 6);
        card.add(subtitle, gbc);

        // ---- Username ------------------------------------------------
        gbc.gridwidth = 1;
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 0;
        card.add(new JLabel("Username:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1;
        card.add(usernameField, gbc);

        // ---- Password ------------------------------------------------
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.weightx = 0;
        card.add(new JLabel("Password:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1;
        card.add(passwordField, gbc);

        // ---- Buttons -------------------------------------------------
        loginButton.setPreferredSize(new Dimension(110, 32));
        exitButton.setPreferredSize(new Dimension(110, 32));

        JPanel buttonPanel = new JPanel(new GridLayout(1, 2, 10, 0));
        buttonPanel.add(loginButton);
        buttonPanel.add(exitButton);

        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        gbc.weightx = 1;
        gbc.insets = new Insets(18, 6, 0, 6);
        card.add(buttonPanel, gbc);

        add(card, BorderLayout.CENTER);

        // ---- Events --------------------------------------------------
        // Enter anywhere in the window (including the password field)
        // activates the default button.
        getRootPane().setDefaultButton(loginButton);
        loginButton.addActionListener(e -> attemptLogin());
        passwordField.addActionListener(e -> attemptLogin());
        usernameField.addActionListener(e -> passwordField.requestFocusInWindow());
        exitButton.addActionListener(e -> exitApplication());

        pack();
        setLocationRelativeTo(null);

        // Focus goes straight to the first field when the window opens.
        usernameField.requestFocusInWindow();
    }

    /**
     * Validates that both fields are filled, asks the DAO, and routes the
     * user to {@link MainFrame} when the credentials are correct.
     */
    private void attemptLogin() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            showError("Please enter both username and password.");
            return;
        }

        try {
            if (adminDAO.authenticate(username, password)) {
                // Open the main window first, then remove the login screen.
                new MainFrame().setVisible(true);
                dispose();
            } else {
                showError("Invalid username or password.");
                passwordField.setText("");
                passwordField.requestFocusInWindow();
            }
        } catch (SQLException ex) {
            // A database problem is different from wrong credentials, so the
            // user gets the real reason instead of "invalid login".
            showError("Database error:\n" + ex.getMessage());
        }
    }

    private void exitApplication() {
        int choice = JOptionPane.showConfirmDialog(
                this, "Exit the application?", "Confirm exit",
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (choice == JOptionPane.YES_OPTION) {
            dispose();
            System.exit(0);
        }
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(
                this, message, "Login failed", JOptionPane.ERROR_MESSAGE);
    }
}
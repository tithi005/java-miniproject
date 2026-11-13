package ui;

import dao.StudentDAO;
import model.Student;
import util.Validator;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.sql.SQLException;
import java.util.List;
import java.util.Locale;

/**
 * Main dashboard: a student form on the left, the records table in the
 * center, and the action buttons at the bottom.
 *
 * <p>Layout summary (BorderLayout on the frame):</p>
 * <pre>
 *   NORTH  - title / subtitle
 *   WEST   - form panel (GridBagLayout)
 *   CENTER - search bar + JTable in a JScrollPane + record count
 *   SOUTH  - Add / Update / Delete / Clear / Exit
 * </pre>
 *
 * <p>Part 3 completes the CRUD cycle: selecting a row loads it into the form
 * and locks the roll number, Update and Delete act on that selection, and
 * Search filters the table through the DAO.</p>
 */
public class MainFrame extends JFrame {

    private static final long serialVersionUID = 1L;

    private static final String TOTAL_PREFIX = "Total Students: ";
    private static final String RESULT_PREFIX = "Search results: ";

    private final StudentDAO studentDAO = new StudentDAO();

    // ---- Form fields --------------------------------------------------
    private final JTextField rollNoField = new JTextField(16);
    private final JTextField nameField = new JTextField(16);
    private final JComboBox<String> courseCombo = new JComboBox<>(
            new String[]{"CSE", "IT", "ECE", "EE", "ME", "Civil"});
    private final JComboBox<Integer> yearCombo = new JComboBox<>(
            new Integer[]{1, 2, 3, 4});
    private final JTextField emailField = new JTextField(16);
    private final JTextField phoneField = new JTextField(16);
    private final JRadioButton maleRadio = new JRadioButton("Male");
    private final JRadioButton femaleRadio = new JRadioButton("Female");
    private final JRadioButton otherRadio = new JRadioButton("Other");
    private final ButtonGroup genderGroup = new ButtonGroup();

    // ---- Search + table ----------------------------------------------
    private final JTextField searchField = new JTextField(20);
    private final JButton searchButton = new JButton("Search");
    private final JButton showAllButton = new JButton("Show All");
    private final StudentTableModel tableModel = new StudentTableModel();
    private final JTable table = new JTable(tableModel);
    private final JLabel countLabel = new JLabel(TOTAL_PREFIX + "0");
    private final JLabel editHint = new JLabel(" ");

    // ---- Buttons -----------------------------------------------------
    private final JButton addButton = new JButton("Add");
    private final JButton updateButton = new JButton("Update");
    private final JButton deleteButton = new JButton("Delete");
    private final JButton clearButton = new JButton("Clear");
    private final JButton exitButton = new JButton("Exit");

    /**
     * Roll number of the row currently loaded into the form, or null when
     * the form is in "new student" mode. Kept separately because the roll
     * number field becomes read-only during editing and is still needed as
     * the WHERE key for update/delete.
     */
    private String selectedRollNo;

    /** Builds the dashboard and loads the existing records. */
    public MainFrame() {
        super("Student Record Management System");
        initUi();
        refreshTable(); // startup load: every row in the database
    }

    /** Creates the menu bar, panels, form, table and button bar. */
    private void initUi() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        setJMenuBar(buildMenuBar());
        add(buildHeader(), BorderLayout.NORTH);
        add(buildFormPanel(), BorderLayout.WEST);
        add(buildTablePanel(), BorderLayout.CENTER);
        add(buildButtonPanel(), BorderLayout.SOUTH);

        // Resizing: the form keeps its width, the table takes the rest, and
        // the initial size adapts to smaller screens.
        setMinimumSize(new Dimension(960, 580));
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        setSize(Math.min(1150, screen.width - 60),
                Math.min(700, screen.height - 60));
        setLocationRelativeTo(null);

        // Put the cursor in the first field once the window is really open.
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowOpened(WindowEvent e) {
                rollNoField.requestFocusInWindow();
            }
        });
    }

    // ==================================================================
    // Layout builders
    // ==================================================================

    /** File (Clear, Logout, Exit) and Help (About) menus. */
    private JMenuBar buildMenuBar() {
        JMenuBar menuBar = new JMenuBar();

        JMenu fileMenu = new JMenu("File");

        JMenuItem clearItem = new JMenuItem("Clear");
        clearItem.addActionListener(e -> clearForm());

        JMenuItem logoutItem = new JMenuItem("Logout");
        logoutItem.addActionListener(e -> logout());

        JMenuItem exitItem = new JMenuItem("Exit");
        exitItem.addActionListener(e -> exitApplication());

        fileMenu.add(clearItem);
        fileMenu.addSeparator();
        fileMenu.add(logoutItem);
        fileMenu.addSeparator();
        fileMenu.add(exitItem);

        JMenu helpMenu = new JMenu("Help");
        JMenuItem aboutItem = new JMenuItem("About");
        aboutItem.addActionListener(e -> showAbout());
        helpMenu.add(aboutItem);

        menuBar.add(fileMenu);
        menuBar.add(helpMenu);
        return menuBar;
    }

    /** Title block shown at the top of the window. */
    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBorder(BorderFactory.createEmptyBorder(16, 20, 10, 20));

        JLabel title = new JLabel("Student Record Management System");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 22f));

        JLabel subtitle = new JLabel("Select a row to edit or delete it, or add a new student");
        subtitle.setFont(subtitle.getFont().deriveFont(Font.PLAIN, 12f));

        JPanel textPanel = new JPanel(new GridLayout(2, 1, 0, 4));
        textPanel.add(title);
        textPanel.add(subtitle);

        header.add(textPanel, BorderLayout.WEST);
        header.add(new JSeparator(), BorderLayout.SOUTH);
        return header;
    }

    /** Left-hand form built with GridBagLayout, one row per field. */
    private JPanel buildFormPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(14, 20, 12, 8),
                BorderFactory.createTitledBorder("Student Details")));
        panel.setPreferredSize(new Dimension(340, 0));
        panel.setMinimumSize(new Dimension(300, 0));

        // One shared ButtonGroup keeps the radio buttons mutually exclusive.
        genderGroup.add(maleRadio);
        genderGroup.add(femaleRadio);
        genderGroup.add(otherRadio);

        courseCombo.setSelectedIndex(0);
        yearCombo.setSelectedIndex(0);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 6, 5, 6);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        int row = 0;
        addFormRow(panel, gbc, row++, "Roll No:", rollNoField);
        addFormRow(panel, gbc, row++, "Name:", nameField);
        addFormRow(panel, gbc, row++, "Course:", courseCombo);
        addFormRow(panel, gbc, row++, "Year:", yearCombo);
        addFormRow(panel, gbc, row++, "Email:", emailField);
        addFormRow(panel, gbc, row++, "Phone:", phoneField);

        // Gender takes a full row because it holds three radio buttons.
        JPanel genderPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        genderPanel.add(maleRadio);
        genderPanel.add(femaleRadio);
        genderPanel.add(otherRadio);

        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 2;
        gbc.weightx = 1;
        gbc.insets = new Insets(5, 6, 5, 6);
        panel.add(new JLabel("Gender:"), gbc);

        gbc.gridy = row + 1;
        gbc.insets = new Insets(0, 6, 5, 6);
        panel.add(genderPanel, gbc);

        // Push the form to the top when the window grows taller.
        gbc.gridx = 0;
        gbc.gridy = row + 2;
        gbc.gridwidth = 2;
        gbc.weighty = 1;
        gbc.fill = GridBagConstraints.BOTH;
        panel.add(new JPanel(), gbc);

        return panel;
    }

    /** Adds "label + field" to one GridBag row. */
    private void addFormRow(JPanel panel, GridBagConstraints gbc, int row,
                            String labelText, Component field) {
        gbc.gridwidth = 1;
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0;
        gbc.weighty = 0;
        gbc.fill = GridBagConstraints.NONE;
        panel.add(new JLabel(labelText), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(field, gbc);
    }

    /** Search bar on top of the scrollable table, count label below it. */
    private JPanel buildTablePanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 8));
        panel.setBorder(BorderFactory.createEmptyBorder(14, 8, 12, 20));

        JPanel searchBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        searchField.setPreferredSize(new Dimension(240, 26));
        searchBar.add(new JLabel("Search:"));
        searchBar.add(searchField);
        searchBar.add(searchButton);
        searchBar.add(showAllButton);

        // Table appearance.
        table.setRowHeight(28);
        table.setFillsViewportHeight(true);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getTableHeader().setReorderingAllowed(false);
        table.setAutoCreateRowSorter(false);

        // Clicking a column header sorts; the Year column uses Integer so it
        // sorts numerically (1, 2, 3, 4) instead of as text.
        TableRowSorter<StudentTableModel> sorter = new TableRowSorter<>(tableModel);
        table.setRowSorter(sorter);

        int[] widths = {90, 170, 80, 55, 200, 110, 80};
        for (int i = 0; i < widths.length; i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }

        // Footer: record count on the left, editing hint on the right.
        countLabel.setFont(countLabel.getFont().deriveFont(Font.BOLD));
        editHint.setFont(editHint.getFont().deriveFont(Font.ITALIC));
        editHint.setForeground(new Color(0x6B6B6B));

        JPanel footer = new JPanel(new BorderLayout());
        footer.setBorder(BorderFactory.createEmptyBorder(4, 2, 0, 2));
        footer.add(countLabel, BorderLayout.WEST);
        footer.add(editHint, BorderLayout.EAST);

        panel.add(searchBar, BorderLayout.NORTH);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        panel.add(footer, BorderLayout.SOUTH);

        // Events.
        searchButton.addActionListener(e -> searchStudents());
        showAllButton.addActionListener(e -> showAllStudents());
        searchField.addActionListener(e -> searchStudents()); // Enter key
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                onRowSelected();
            }
        });

        return panel;
    }

    /** Bottom action bar. */
    private JPanel buildButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(4, 16, 14, 16));

        addButton.setFont(addButton.getFont().deriveFont(Font.BOLD));
        panel.add(addButton);
        panel.add(updateButton);
        panel.add(deleteButton);
        panel.add(clearButton);
        panel.add(exitButton);

        addButton.addActionListener(e -> addStudent());
        updateButton.addActionListener(e -> updateStudent());
        deleteButton.addActionListener(e -> deleteStudent());
        clearButton.addActionListener(e -> clearForm());
        exitButton.addActionListener(e -> exitApplication());

        return panel;
    }

    // ==================================================================
    // Table data
    // ==================================================================

    /** Reloads every student from the database into the table. */
    private void refreshTable() {
        try {
            setTableData(studentDAO.getAllStudents(), false);
        } catch (SQLException ex) {
            showError("Could not load students.\n" + ex.getMessage());
        }
    }

    /**
     * Replaces the table rows and updates the count label.
     *
     * @param students the rows to show
     * @param filtered true when the list is a search result
     */
    private void setTableData(List<Student> students, boolean filtered) {
        tableModel.setRowCount(0); // clear existing rows
        for (Student student : students) {
            tableModel.addRow(new Object[]{
                    student.getRollNo(),
                    student.getName(),
                    student.getCourse(),
                    student.getYear(),      // Integer -> numeric sorting
                    student.getEmail(),
                    student.getPhone(),
                    student.getGender()
            });
        }
        countLabel.setText((filtered ? RESULT_PREFIX : TOTAL_PREFIX)
                + students.size());
    }

    // ==================================================================
    // Actions
    // ==================================================================

    /** Reads the form, validates it, inserts, then refreshes the table. */
    private void addStudent() {
        // While a row is loaded the roll number field is read-only, so the
        // user is expected to Clear before adding somebody new.
        if (selectedRollNo != null) {
            showInfo("You are editing " + selectedRollNo
                    + ". Click Clear first to add a new student.");
            return;
        }

        String rollNo = rollNoField.getText().trim();
        String name = nameField.getText().trim();
        String course = (String) courseCombo.getSelectedItem();
        int year = yearCombo.getSelectedItem() == null
                ? 0
                : (Integer) yearCombo.getSelectedItem();
        String email = emailField.getText().trim();
        String phone = phoneField.getText().trim();
        String gender = selectedGender();

        // Business rules come from the shared Validator.
        String error = Validator.validateStudent(
                rollNo, name, course, year, email, phone);
        if (error == null && gender.isEmpty()) {
            error = "Please select a gender.";
        }
        if (error != null) {
            showError(error);
            return;
        }

        Student student = new Student(
                rollNo, name, course, year, email, phone, gender);

        try {
            studentDAO.addStudent(student); // unique PK check happens in SQLite
            showInfo("Student added successfully.");
            refreshTable();
            clearForm();
        } catch (SQLException ex) {
            if (isDuplicateRollNo(ex)) {
                showError("Roll number '" + rollNo + "' is already in use.\n"
                        + "Please enter a different roll number.");
                rollNoField.requestFocusInWindow();
                rollNoField.selectAll();
            } else {
                showError("Could not add the student.\n" + ex.getMessage());
            }
        }
    }

    /**
     * Updates the record previously loaded by a row click. The roll number
     * is the primary key and stays read-only, so {@link #selectedRollNo} is
     * used as the key even if the form fields were edited.
     */
    private void updateStudent() {
        String key = selectedRollNo;
        if (key == null) {
            showInfo("Select a student row first, edit the details, "
                    + "then click Update.");
            return;
        }

        String name = nameField.getText().trim();
        String course = (String) courseCombo.getSelectedItem();
        int year = yearCombo.getSelectedItem() == null
                ? 0
                : (Integer) yearCombo.getSelectedItem();
        String email = emailField.getText().trim();
        String phone = phoneField.getText().trim();
        String gender = selectedGender();

        String error = Validator.validateStudent(
                key, name, course, year, email, phone);
        if (error == null && gender.isEmpty()) {
            error = "Please select a gender.";
        }
        if (error != null) {
            showError(error);
            return;
        }

        int choice = JOptionPane.showConfirmDialog(
                this,
                "Update the record for roll number '" + key + "'?",
                "Confirm update",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);
        if (choice != JOptionPane.YES_OPTION) {
            return;
        }

        Student student = new Student(
                key, name, course, year, email, phone, gender);

        try {
            if (studentDAO.updateStudent(student)) {
                showInfo("Student updated successfully.");
                refreshTable();
                clearForm();
            } else {
                showError("That student no longer exists. "
                        + "The table has been refreshed.");
                refreshTable();
                clearForm();
            }
        } catch (SQLException ex) {
            showError("Could not update the student.\n" + ex.getMessage());
        }
    }

    /**
     * Deletes the student currently loaded from the table, after a Yes/No
     * confirmation. Deletion is permanent, so the dialog says so.
     */
    private void deleteStudent() {
        String rollNo = selectedRollNo;
        if (rollNo == null) {
            showInfo("Select a student row in the table first.");
            return;
        }

        int choice = JOptionPane.showConfirmDialog(
                this,
                "Delete the record for roll number '" + rollNo + "'?\n"
                        + "This action cannot be undone.",
                "Confirm delete",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);
        if (choice != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            if (studentDAO.deleteStudent(rollNo)) {
                showInfo("Student deleted.");
                refreshTable();
                clearForm();
            } else {
                showError("That student no longer exists. "
                        + "The table has been refreshed.");
                refreshTable();
                clearForm();
            }
        } catch (SQLException ex) {
            showError("Could not delete the student.\n" + ex.getMessage());
        }
    }

    /** Filters the table by roll no, name or course (partial, ignore case). */
    private void searchStudents() {
        String keyword = searchField.getText().trim();
        if (keyword.isEmpty()) {
            refreshTable();
            return;
        }

        try {
            List<Student> matches = studentDAO.searchStudents(keyword);
            setTableData(matches, true);
            clearForm(); // a filtered table invalidates any pending edit

            if (matches.isEmpty()) {
                JOptionPane.showMessageDialog(
                        this,
                        "No records found for \"" + keyword + "\".",
                        "Search",
                        JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (SQLException ex) {
            showError("Could not search students.\n" + ex.getMessage());
        }
    }

    /** Shows every student again and empties the search box. */
    private void showAllStudents() {
        searchField.setText("");
        refreshTable();
        clearForm();
    }

    /** Resets the form, releases the row selection and unlocks Roll No. */
    private void clearForm() {
        rollNoField.setText("");
        nameField.setText("");
        courseCombo.setSelectedIndex(0);
        yearCombo.setSelectedIndex(0);
        emailField.setText("");
        phoneField.setText("");
        genderGroup.clearSelection();
        table.clearSelection();
        setEditingState(null); // selectedRollNo = null, Roll No editable
        rollNoField.requestFocusInWindow();
    }

    /** Returns the selected radio button text, or an empty string. */
    private String selectedGender() {
        if (maleRadio.isSelected()) {
            return "Male";
        }
        if (femaleRadio.isSelected()) {
            return "Female";
        }
        if (otherRadio.isSelected()) {
            return "Other";
        }
        return "";
    }

    /** Checks the radio button that matches the stored gender value. */
    private void selectGender(String gender) {
        genderGroup.clearSelection();
        if ("Male".equalsIgnoreCase(gender)) {
            maleRadio.setSelected(true);
        } else if ("Female".equalsIgnoreCase(gender)) {
            femaleRadio.setSelected(true);
        } else if ("Other".equalsIgnoreCase(gender)) {
            otherRadio.setSelected(true);
        }
    }

    /**
     * Switches between "new student" and "editing" modes:
     * editing locks the roll number (it is the primary key) and disables Add.
     */
    private void setEditingState(String rollNo) {
        selectedRollNo = rollNo;
        boolean editing = rollNo != null;

        rollNoField.setEditable(!editing);
        addButton.setEnabled(!editing);
        addButton.setToolTipText(editing
                ? "Click Clear first to add a new student"
                : null);
        editHint.setText(editing ? "Editing: " + rollNo : " ");
    }

    /**
     * Copies the clicked table row into the form.
     *
     * <p>The view row index is converted to the model index first, because
     * TableRowSorter may be displaying sorted or filtered rows.</p>
     */
    private void onRowSelected() {
        int viewRow = table.getSelectedRow();
        if (viewRow < 0) {
            return; // selection cleared programmatically, keep the form as-is
        }
        int modelRow = table.convertRowIndexToModel(viewRow);

        rollNoField.setText(textAt(modelRow, 0));
        nameField.setText(textAt(modelRow, 1));
        courseCombo.setSelectedItem(textAt(modelRow, 2));
        Object year = tableModel.getValueAt(modelRow, 3);
        if (year != null) {
            yearCombo.setSelectedItem(year);
        }
        emailField.setText(textAt(modelRow, 4));
        phoneField.setText(textAt(modelRow, 5));
        selectGender(textAt(modelRow, 6));

        setEditingState(rollNoField.getText().trim());
        nameField.requestFocusInWindow();
    }

    /** Null-safe cell read for the table model. */
    private String textAt(int modelRow, int column) {
        Object value = tableModel.getValueAt(modelRow, column);
        return value == null ? "" : value.toString();
    }

    /** True when the DAO reported a duplicate primary key. */
    private boolean isDuplicateRollNo(SQLException ex) {
        String message = ex.getMessage() == null
                ? ""
                : ex.getMessage().toLowerCase(Locale.ROOT);
        return message.contains("already exists") || message.contains("unique");
    }

    // ==================================================================
    // Window actions
    // ==================================================================

    private void logout() {
        int choice = JOptionPane.showConfirmDialog(
                this, "Log out and return to the login screen?",
                "Confirm logout", JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);
        if (choice == JOptionPane.YES_OPTION) {
            dispose();
            new LoginFrame().setVisible(true);
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

    private void showAbout() {
        JOptionPane.showMessageDialog(
                this,
                "Student Record Management System\n"
                        + "Java Swing + SQLite (JDBC)\n\n"
                        + "Default login: admin / admin123",
                "About",
                JOptionPane.INFORMATION_MESSAGE);
    }

    private void showInfo(String message) {
        JOptionPane.showMessageDialog(
                this, message, "Information", JOptionPane.INFORMATION_MESSAGE);
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(
                this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    // ==================================================================
    // Table model
    // ==================================================================

    /**
     * Read-only table model. Extending DefaultTableModel gives us the
     * row/column storage for free, and overriding isCellEditable makes every
     * cell non-editable (the form is the only place data can be changed).
     */
    private static class StudentTableModel extends DefaultTableModel {

        private static final long serialVersionUID = 1L;

        private static final String[] COLUMNS = {
                "Roll No", "Name", "Course", "Year", "Email", "Phone", "Gender"
        };

        StudentTableModel() {
            super(COLUMNS, 0);
        }

        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }

        @Override
        public Class<?> getColumnClass(int columnIndex) {
            // Used by TableRowSorter to pick a sensible comparator.
            return columnIndex == 3 ? Integer.class : String.class;
        }
    }
}
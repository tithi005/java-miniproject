# Student Record Management System

A desktop CRUD application for managing student records, built with **Java
Swing** and **SQLite via JDBC**. It has an administrator login, a form for
adding and editing students, a sortable table with live search, and a
file-based database that creates itself on first run.

The project is split into three layers so the UI never talks to SQL directly:
`model` (data), `dao` (all JDBC/SQL), `util` (connection + validation), and
`ui` (Swing screens).

## Features

- **Admin login** with the default account `admin` / `admin123`; Enter submits
  the form, wrong credentials show an inline error dialog.
- **Add student** with validation of roll number, name, email, phone, year and
  gender. Duplicate roll numbers are rejected with a friendly message.
- **Row click to edit**: selecting a row fills the form and locks the Roll No
  field (it is the primary key); the footer shows `Editing: <roll no>`.
- **Update** the selected record after a Yes/No confirmation.
- **Delete** the selected record after a permanent-action confirmation.
- **Search** by roll number, name or course - partial and case-insensitive -
  with a "No records found" dialog when nothing matches.
- **Show All** restores the full list; **Clear** resets the form and unlocks
  Roll No.
- **Sortable table** (click a column header; Year sorts numerically) with a
  read-only `DefaultTableModel`.
- **Record count** label (`Total Students: N` / `Search results: N`).
- **Resizable layout** with a screen-aware initial size and minimum size.
- **Nimbus look and feel** with a consistent font and padded panels.
- **Logout / Exit** both ask for confirmation; every `SQLException` becomes a
  readable dialog instead of a stack trace.
- Database file, tables and default admin are created automatically.

## Tech stack

| Item | Version |
|---|---|
| Java (JDK) | 8 or newer |
| UI | Java Swing (Nimbus L&F) |
| Database | SQLite 3 |
| JDBC driver | `sqlite-jdbc-3.54.1.0.jar` (org.xerial) |
| Logging API | `slf4j-api-2.0.17.jar` (required by the driver) |

## Project structure

```
java miniproject/
├── lib/
│   ├── sqlite-jdbc-3.54.1.0.jar
│   └── slf4j-api-2.0.17.jar
├── src/
│   ├── app/
│   │   ├── Application.java      <- GUI entry point (Nimbus + LoginFrame)
│   │   └── Main.java             <- Part 1 console smoke test
│   ├── model/Student.java        <- POJO: rollNo, name, course, year, ...
│   ├── dao/
│   │   ├── StudentDAO.java       <- add / getAll / update / delete / search
│   │   └── AdminDAO.java         <- authenticate(username, password)
│   ├── util/
│   │   ├── DBConnection.java     <- JDBC URL, schema creation, admin seed
│   │   └── Validator.java        <- field validation rules
│   └── ui/
│       ├── LoginFrame.java       <- login window
│       └── MainFrame.java        <- dashboard (form + table + actions)
├── studentdb.db                  <- created automatically on first run
├── manifest.txt                  <- used when building the JAR
└── StudentRecordSystem.jar       <- build output (optional)
```

## Database schema

```sql
CREATE TABLE students (
    roll_no TEXT PRIMARY KEY,
    name    TEXT NOT NULL,
    course  TEXT NOT NULL,
    year    INTEGER NOT NULL CHECK (year BETWEEN 1 AND 4),
    email   TEXT,
    phone   TEXT,
    gender  TEXT
);

CREATE TABLE admins (
    username TEXT PRIMARY KEY,
    password TEXT NOT NULL
);
```

`admins` is seeded once with `admin` / `admin123` using `INSERT OR IGNORE`.
The database file lives next to the working directory (`studentdb.db`).

## Setup

### 1. Requirements

- JDK 8+ (`java -version` should work).
- The two jars in `lib/` (already included). If you start from scratch, download
  them from Maven Central:
  - <https://repo1.maven.org/maven2/org/xerial/sqlite-jdbc/3.54.1.0/sqlite-jdbc-3.54.1.0.jar>
  - <https://repo1.maven.org/maven2/org/slf4j/slf4j-api/2.0.17/slf4j-api-2.0.17.jar>

### 2. Add the jars in an IDE

- **IntelliJ IDEA:** `File > Project Structure > Modules > Dependencies >
  + > JARs or directories...`, select both jars, scope `Compile`. Mark `src`
  as *Sources Root*.
- **Eclipse:** right-click the project `> Build Path > Configure Build Path...
  > Libraries > Add External JARs...`, add both jars.
- **NetBeans:** right-click the project `> Properties > Libraries > Compile >
  Add JAR/Folder...`, add both jars.
- **VS Code / command line:** no import step; use `-cp "lib/*"`.

### 3. Run

Windows PowerShell, from the project folder:

```powershell
$files = Get-ChildItem -Path src -Recurse -Filter *.java | ForEach-Object { $_.FullName }
javac -d out -cp "lib/*" -encoding UTF-8 $files
java -cp "out;lib/*" app.Application     # GUI
java -cp "out;lib/*" app.Main            # console backend test
```

macOS / Linux (classpath separator is `:`):

```bash
javac -d out -cp "lib/*" -encoding UTF-8 $(find src -name "*.java")
java -cp "out:lib/*" app.Application
```

Log in with **admin / admin123**. Delete `studentdb.db` to start with an empty
database.

## Export a runnable JAR

### Command line (works for any IDE)

```powershell
# 1. compile to out/
$files = Get-ChildItem -Path src -Recurse -Filter *.java | ForEach-Object { $_.FullName }
javac -d out -cp "lib/*" -encoding UTF-8 $files

# 2. manifest - note: ASCII, otherwise a UTF-8 BOM breaks the Main-Class line
@"
Main-Class: app.Application
Class-Path: lib/sqlite-jdbc-3.54.1.0.jar lib/slf4j-api-2.0.17.jar
"@ | Set-Content -Encoding ASCII manifest.txt

# 3. package the compiled classes
jar cfm StudentRecordSystem.jar manifest.txt -C out .

# 4. run it
java -jar StudentRecordSystem.jar
```

The manifest points at the jars relatively, so keep `lib/` beside the JAR (or
edit `Class-Path` to absolute paths). Double-clicking the JAR also works when
`lib/` is beside it. The `studentdb.db` file is created in the folder the JAR is
started from, not inside the JAR.

> If PowerShell says `jar` is not recognized, call it with its full path, e.g.
> `& "C:\Program Files\Java\jdk-24\bin\jar.exe" cfm StudentRecordSystem.jar manifest.txt -C out .`,
> or add `%JAVA_HOME%\bin` to `PATH`.

### IntelliJ IDEA

1. `File > Project Structure > Artifacts > + > JAR > From modules with
   dependencies...`
2. Main class: `app.Application`; choose "extract to the target JAR" if you want
   a single fat JAR.
3. `Build > Build Artifacts > Build`, then run the JAR from `out/artifacts/...`.

### Eclipse

1. `File > Export > Java > Runnable JAR file`.
2. Launch configuration: pick a run configuration whose main class is
   `app.Application`.
3. Choose "Package required libraries into generated JAR" (fat JAR), then
   `Finish`.

### NetBeans

1. `Properties > Run > Main Class` = `app.Application`.
2. `Run > Clean and Build`; the `dist/` folder contains the JAR plus a copy of
   the libraries and a working manifest.

## Test checklist

Run through these in order after a fresh start (`studentdb.db` deleted).

| # | Test case | Steps | Expected result |
|---|---|---|---|
| 1 | Valid add | Fill all fields (e.g. `CS101`, `Aarav Sharma`, CSE, 2, `aarav@example.com`, `9876543210`, Male) and click Add | "Student added successfully"; row appears; count increases; form clears |
| 2 | Duplicate roll no | Add another student with the same `CS101` | Error "Roll number 'CS101' is already in use"; no new row |
| 3 | Empty fields | Leave Roll No, Name, Email and Phone blank, click Add | First validation message appears (e.g. "Roll number cannot be empty"); nothing saved |
| 4 | Invalid email | Email `abc@` | "Enter a valid email address, e.g. name@example.com." |
| 5 | Invalid phone | Phone `98765` | "Phone number must be exactly 10 digits." |
| 6 | Invalid name | Name `Aarav123` | "Name may contain letters and spaces only." |
| 7 | Year limited to 1-4 | Open the Year combo | Only 1, 2, 3, 4 are offered (DB also has a CHECK constraint) |
| 8 | Row click loads form | Click an existing row | All fields fill; Roll No becomes read-only; footer shows `Editing: <roll>` |
| 9 | Clear resets | Click Clear while editing | Fields empty, selection cleared, Roll No editable again, hint hidden |
| 10 | Update success | Click a row, change name/phone/year, click Update, confirm Yes | Success dialog; table shows new values; count unchanged; form clears |
| 11 | Update cancelled | Same as #10 but click No | Nothing changes; form keeps the edited values |
| 12 | Update with no selection | Click Update without selecting a row | Info dialog "Select a student row first..." |
| 13 | Delete success | Click a row, click Delete, confirm Yes | "Student deleted"; row disappears; count decreases |
| 14 | Delete cancelled | Click Delete, confirm No | Row remains |
| 15 | Delete with no selection | Click Delete without selecting a row | Info dialog "Select a student row in the table first." |
| 16 | Search by roll no | Type `CS1`, click Search | Only matching rows; label becomes `Search results: N` |
| 17 | Search by name, mixed case | Type `aarav`, click Search | Partial, case-insensitive match on Name |
| 18 | Search by course | Type `cse`, click Search | All CSE students listed |
| 19 | Search with no match | Type `zzz`, click Search | "No records found for "zzz"." and an empty table |
| 20 | Show All | After a search, click Show All | Search box empties; every record returns; label `Total Students: N` |
| 21 | Sorting | Click the Name header, then Year | Rows sort; Year sorts 1,2,3,4 (not 1,10,2) |
| 22 | Wrong login | `admin` / `wrong` | "Invalid username or password"; password field clears |
| 23 | Correct login | `admin` / `admin123` | Login closes, dashboard opens and loads records |
| 24 | Enter to log in | Type credentials, press Enter | Same as clicking Login |
| 25 | Logout confirmation | File > Logout, choose Yes / No | Yes returns to login; No stays on the dashboard |
| 26 | Exit confirmation | File > Exit or the Exit button | Yes closes the app; No keeps it open |
| 27 | Layout resize | Drag the window edges / maximise | Form keeps its width; table grows and fills; nothing overlaps |
| 28 | DB error handling | Rename `studentdb.db` while the app is closed, then open it read-only and try Add | A readable error dialog appears (no stack trace in the UI) |

## Viva questions and short answers

**1. Why did you separate `model`, `dao`, `util` and `ui`?**
Separation of concerns. The model is a plain data holder, the DAO owns all SQL,
`util` holds shared helpers (connection and validation), and the UI only reacts
to events. The GUI can be redesigned or the console test run without touching
database code, and the DAO can be tested independently.

**2. How does the application connect to SQLite, and why use
`PreparedStatement`?**
`DriverManager.getConnection("jdbc:sqlite:studentdb.db")` opens the file;
`DBConnection` creates the tables and seeds the admin once per JVM. All queries
use `PreparedStatement` with `?` placeholders, which pre-compiles the statement,
binds values safely (no string concatenation, so quotes and SQL injection are
handled) and is faster when a statement is executed repeatedly.

**3. How does a button click reach the database in Swing?**
Each button has an `ActionListener` (a lambda here). Swing delivers the click on
the Event Dispatch Thread, which calls e.g. `addStudent()`: read the widgets,
validate, call the DAO, then refresh the table. Because all UI work happens on
the EDT, no extra synchronisation is needed; long-running work should instead be
moved to a `SwingWorker` so the window does not freeze.

**4. Why use a `DefaultTableModel`, and where do collections fit in?**
`JTable` does not store data itself - it reads it through a `TableModel`.
Extending `DefaultTableModel` gives row/column storage, and overriding
`isCellEditable` makes the grid read-only. The DAO returns a
`List<Student>` (an `ArrayList`), which `setTableData` copies into the model;
generics keep the collection type-safe. The model also reports `Integer` for the
Year column so `TableRowSorter` sorts it numerically.

**5. How are exceptions handled end to end?**
JDBC methods throw the checked `SQLException`. Every DAO method declares it and
closes resources with try-with-resources. `addStudent` catches the SQLite
constraint violation and rethrows a clear message for duplicates. The UI wraps
every DAO call in `try/catch (SQLException)` and shows the message in a
`JOptionPane`, while unexpected errors never dump a stack trace to the user.
Input problems are prevented earlier by `Validator`, which returns an error
string or `null`.

## Notes and limitations

- All database calls run on the Swing event thread. That is fine for a local
  SQLite file and small classes; a production version would use `SwingWorker`.
- Passwords are stored in plain text for teaching purposes only. Real systems
  must store salted hashes (e.g. BCrypt).
- Roll numbers are compared case-sensitively by SQLite (`CS101` and `cs101` are
  different keys).
- The `SLF4J(W): No SLF4J providers were found` line is harmless driver output;
  add `slf4j-nop` to `lib/` to silence it. On JDK 24+ a native-access warning
  from the SQLite driver can be hidden with `--enable-native-access=ALL-UNNAMED`.
- Part 3 changed only `src/ui/MainFrame.java`; the backend and login screen were
  left untouched.
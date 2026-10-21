/*
 * ============================================================================
 *  ICTD IMS - ATTENDANCE STORAGE
 * ============================================================================
 *  What this program does:
 *    Saves and reads the READS Scholar attendance (Morning In / Morning Out)
 *    in a local text file. No database is used.
 *
 *  How records are kept in memory (HASH TABLE):
 *    key   = username (lowercase) + "|" + date
 *    value = the attendance Record
 *    There is only one record for each scholar for each day, so checking
 *    "did this scholar already time in today?" is fast (O(1) on average).
 *
 *  Where records are saved:
 *    data/attendance.txt (created automatically if it does not exist).
 *
 *  File format (one record per line, parts separated by "|"):
 *    username|spn|firstName|lastName|date|morningIn|morningOut
 *    (date is yyyy-MM-dd, times look like 08:05 AM, empty = not yet)
 * ============================================================================
 */
package backend;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class AttendanceStorage {

    private static final String DEFAULT_FILE_PATH = "data/attendance.txt";
    private static final DateTimeFormatter TIME_FORMAT =
            DateTimeFormatter.ofPattern("hh:mm a", Locale.ENGLISH);

    private static final String[] FILE_HEADER = {
            "# ICTD IMS READS attendance. Do not edit by hand.",
            "# Format: username|spn|firstName|lastName|date|morningIn|morningOut"
    };

    // ==================================================================
    // Record (one day of attendance for one scholar)
    // ==================================================================
    public static class Record {
        private final String username;
        private final String spn;
        private final String firstName;
        private final String lastName;
        private final String date;
        private final String morningIn;
        private final String morningOut;

        private Record(String username, String spn, String firstName, String lastName,
                       String date, String morningIn, String morningOut) {
            this.username = username;
            this.spn = spn;
            this.firstName = firstName;
            this.lastName = lastName;
            this.date = date;
            this.morningIn = morningIn;
            this.morningOut = morningOut;
        }

        public String getUsername() { return username; }
        public String getSpn() { return spn; }
        public String getFirstName() { return firstName; }
        public String getLastName() { return lastName; }
        public String getDate() { return date; }
        public String getMorningIn() { return morningIn; }
        public String getMorningOut() { return morningOut; }
    }

    private final Path filePath;

    // The hash table that holds all records (key = username + "|" + date)
    private HashTable<Record> records = new HashTable<>();

    /** Opens the attendance file (creates it if missing) and loads the records. */
    public AttendanceStorage() throws IOException {
        this(DEFAULT_FILE_PATH);
    }

    /** Same as above, but uses a file path of your choice (useful for testing). */
    public AttendanceStorage(String path) throws IOException {
        this.filePath = Paths.get(path);
        createFileIfMissing();
        loadRecords();
    }

    // ==================================================================
    // Public methods
    // ==================================================================

    /** Today's record of this scholar, or null if the scholar has not timed in today. */
    public Record getToday(String username) {
        return records.get(makeKey(username, LocalDate.now().toString()));
    }

    /**
     * Records the Morning In of today.
     * Throws IllegalArgumentException if SPN or a name is empty.
     * Throws IllegalStateException if the scholar already timed in today.
     */
    public Record timeIn(String username, String spn, String firstName, String lastName)
            throws IOException {
        spn = clean(spn);
        firstName = clean(firstName);
        lastName = clean(lastName);

        if (spn.isEmpty()) {
            throw new IllegalArgumentException("SPN is required.");
        }
        if (firstName.isEmpty()) {
            throw new IllegalArgumentException("First Name is required.");
        }
        if (lastName.isEmpty()) {
            throw new IllegalArgumentException("Last Name is required.");
        }

        String today = LocalDate.now().toString();
        String key = makeKey(username, today);
        if (records.containsKey(key)) {
            throw new IllegalStateException("You have already timed in today.");
        }

        Record record = new Record(username, spn, firstName, lastName,
                today, LocalTime.now().format(TIME_FORMAT), "");
        records.put(key, record);
        try {
            saveRecords();
        } catch (IOException ex) {
            records.remove(key);   // saving failed, so undo the change
            throw ex;
        }
        return record;
    }

    /**
     * Records the Morning Out of today.
     * Throws IllegalStateException if the scholar did not time in today
     * or already timed out.
     */
    public Record timeOut(String username) throws IOException {
        String key = makeKey(username, LocalDate.now().toString());
        Record old = records.get(key);

        if (old == null) {
            throw new IllegalStateException("You have not timed in today.");
        }
        if (!old.morningOut.isEmpty()) {
            throw new IllegalStateException("You have already timed out today.");
        }

        Record updated = new Record(old.username, old.spn, old.firstName, old.lastName,
                old.date, old.morningIn, LocalTime.now().format(TIME_FORMAT));
        records.put(key, updated);
        try {
            saveRecords();
        } catch (IOException ex) {
            records.put(key, old);   // saving failed, so bring back the old record
            throw ex;
        }
        return updated;
    }

    /** All records of one scholar, newest day first (never null). */
    public List<Record> getRecordsFor(String username) {
        List<Record> list = new ArrayList<>();
        for (Record record : records.values()) {
            if (record.username.equalsIgnoreCase(username)) {
                list.add(record);
            }
        }
        list.sort((a, b) -> b.date.compareTo(a.date)); // yyyy-MM-dd sorts correctly as text
        return list;
    }

    /** The most recent record of one scholar (to refill the form), or null if none. */
    public Record getLatestRecord(String username) {
        List<Record> list = getRecordsFor(username);
        return list.isEmpty() ? null : list.get(0);
    }

    // ==================================================================
    // Reading and writing the file
    // ==================================================================
    private static String clean(String text) {
        return text == null ? "" : text.trim();
    }

    private static String makeKey(String username, String date) {
        return username.trim().toLowerCase(Locale.ROOT) + "|" + date;
    }

    private void createFileIfMissing() throws IOException {
        Path folder = filePath.toAbsolutePath().getParent();
        if (folder != null) {
            Files.createDirectories(folder);
        }
        if (Files.notExists(filePath)) {
            Files.write(filePath, Arrays.asList(FILE_HEADER), StandardCharsets.UTF_8);
        }
    }

    /** Reads every record from the file. Broken lines are skipped. */
    private void loadRecords() throws IOException {
        HashTable<Record> loaded = new HashTable<>();

        for (String line : Files.readAllLines(filePath, StandardCharsets.UTF_8)) {
            line = line.trim();
            if (line.isEmpty() || line.startsWith("#")) {
                continue;
            }

            String[] p = FileText.split(line);
            if (p.length != 7) {
                continue; // wrong format, skip it
            }
            loaded.put(makeKey(p[0], p[4]), new Record(p[0], p[1], p[2], p[3], p[4], p[5], p[6]));
        }
        records = loaded;
    }

    /** Saves all records to the file (replaces the old content). */
    private void saveRecords() throws IOException {
        List<String> lines = new ArrayList<>(Arrays.asList(FILE_HEADER));

        List<Record> all = records.values();
        all.sort((a, b) -> a.date.compareTo(b.date)); // oldest first in the file
        for (Record r : all) {
            lines.add(FileText.join(r.username, r.spn, r.firstName, r.lastName,
                    r.date, r.morningIn, r.morningOut));
        }
        Files.write(filePath, lines, StandardCharsets.UTF_8);
    }
}
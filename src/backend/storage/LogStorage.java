/*
 * ============================================================================
 *  ICTD IMS - LOG STORAGE
 * ============================================================================
 *  What this program does:
 *    Saves and reads the log entries (equipment, communication, borrow, and
 *    request logs) in local text files. No database is used.
 *
 *  How entries are kept in memory (HASH TABLE):
 *    key   = the entry id (as text)
 *    value = the LogRecord
 *    Finding, editing, or deleting an entry by id is fast (O(1) on average).
 *    Every change is also saved to the file.
 *
 *  Where entries are saved:
 *    data/logs_<type>.txt, one file for each LogType. The file and the "data"
 *    folder are created automatically if they do not exist.
 *
 *  File format (one entry per line, parts separated by "|"):
 *    id|status|field1|field2|...
 *
 *  Who uses it:
 *    LogPanel (the log pages) and the dashboards that show log numbers.
 * ============================================================================
 */
package backend.storage;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import backend.auth.HashTable;
import backend.models.LogRecord;
import backend.models.LogType;

public class LogStorage {

    private static final String DEFAULT_FOLDER = "data";

    private final LogType type;
    private final Path filePath;

    // The hash table that holds all entries (key = id as text)
    private HashTable<LogRecord> records = new HashTable<>();
    private int nextId = 1;

    /** Opens the log file of this type (creates it if missing) and loads the entries. */
    public LogStorage(LogType type) throws IOException {
        this(type, DEFAULT_FOLDER);
    }

    /** Same as above, but uses a folder of your choice (useful for testing). */
    public LogStorage(LogType type, String folder) throws IOException {
        this.type = type;
        this.filePath = Paths.get(folder, "logs_" + type.getFileName() + ".txt");
        createFileIfMissing();
        loadRecords();
    }

    public LogType getType() {
        return type;
    }

    // ==================================================================
    // Public methods
    // ==================================================================

    /** All entries, newest first (never null). */
    public List<LogRecord> getAll() {
        List<LogRecord> list = records.values();
        list.sort((a, b) -> Integer.compare(b.getId(), a.getId()));
        return list;
    }

    /** Finds one entry by id. Returns null if not found. */
    public LogRecord find(int id) {
        return records.get(String.valueOf(id));
    }

    /**
     * Adds a new entry. It starts with the start status of this log type.
     * Throws IllegalArgumentException with a readable message if a value is not valid.
     */
    public LogRecord add(String[] values) throws IOException {
        String[] clean = validate(values);

        String key = String.valueOf(nextId);
        LogRecord record = new LogRecord(nextId, type.getStartStatus(), clean);

        records.put(key, record);
        try {
            saveRecords();
        } catch (IOException ex) {
            records.remove(key);   // saving failed, so undo the change
            throw ex;
        }
        nextId++;
        return record;
    }

    /**
     * Changes the field values of an entry (its status stays the same).
     * Returns false if no entry has this id.
     */
    public boolean update(int id, String[] values) throws IOException {
        LogRecord old = find(id);
        if (old == null) {
            return false;
        }
        String[] clean = validate(values);
        return replace(old, new LogRecord(id, old.getStatus(), clean));
    }

    /** Changes the status of an entry. Returns false if no entry has this id. */
    public boolean setStatus(int id, String status) throws IOException {
        LogRecord old = find(id);
        if (old == null) {
            return false;
        }
        return replace(old, new LogRecord(id, status, old.getValues()));
    }

    /** Deletes an entry. Returns false if no entry has this id. */
    public boolean delete(int id) throws IOException {
        String key = String.valueOf(id);
        LogRecord removed = records.remove(key);
        if (removed == null) {
            return false;
        }

        try {
            saveRecords();
        } catch (IOException ex) {
            records.put(key, removed);   // saving failed, so put it back
            throw ex;
        }
        return true;
    }

    // ==================================================================
    // Checking the input
    // ==================================================================

    /**
     * Checks the values and returns them with extra spaces removed.
     * Throws IllegalArgumentException if a required value is empty
     * or a date is not written as yyyy-MM-dd.
     */
    private String[] validate(String[] values) {
        String[] fields = type.getFields();
        if (values == null || values.length != fields.length) {
            throw new IllegalArgumentException("The form has the wrong number of fields.");
        }

        String[] clean = new String[fields.length];
        for (int i = 0; i < fields.length; i++) {
            clean[i] = values[i] == null ? "" : values[i].trim();

            if (clean[i].isEmpty()) {
                if (!type.isOptional(fields[i])) {
                    throw new IllegalArgumentException(fields[i] + " is required.");
                }
                continue;
            }

            if (type.isDateField(fields[i])) {
                try {
                    LocalDate.parse(clean[i]);
                } catch (DateTimeParseException ex) {
                    throw new IllegalArgumentException(fields[i]
                            + " must be a real date written as yyyy-MM-dd (example: 2026-10-13).");
                }
            }
        }
        return clean;
    }

    // ==================================================================
    // Reading and writing the file
    // ==================================================================

    /** Puts a new version of an entry in the hash table and saves (undo if saving fails). */
    private boolean replace(LogRecord old, LogRecord updated) throws IOException {
        String key = String.valueOf(old.getId());
        records.put(key, updated);
        try {
            saveRecords();
        } catch (IOException ex) {
            records.put(key, old);   // saving failed, so bring back the old entry
            throw ex;
        }
        return true;
    }

    private void createFileIfMissing() throws IOException {
        Path folder = filePath.toAbsolutePath().getParent();
        if (folder != null) {
            Files.createDirectories(folder);
        }
        if (Files.notExists(filePath)) {
            Files.write(filePath, headerLines(), StandardCharsets.UTF_8);
        }
    }

    /** The first lines of the file (lines starting with # are ignored when reading). */
    private List<String> headerLines() {
        return Arrays.asList(
                "# ICTD IMS " + type.getTitle() + ". Do not edit by hand.",
                "# Format: id|status|" + String.join("|", type.getFields()));
    }

    /** Reads every entry from the file. Broken lines are skipped. */
    private void loadRecords() throws IOException {
        HashTable<LogRecord> loaded = new HashTable<>();
        int highestId = 0;
        int partCount = type.getFieldCount() + 2; // id + status + fields

        for (String line : Files.readAllLines(filePath, StandardCharsets.UTF_8)) {
            line = line.trim();
            if (line.isEmpty() || line.startsWith("#")) {
                continue;
            }

            String[] parts = FileText.split(line);
            if (parts.length != partCount) {
                continue; // wrong format, skip it
            }

            int id;
            try {
                id = Integer.parseInt(parts[0]);
            } catch (NumberFormatException ex) {
                continue; // the id is not a number, skip it
            }

            String[] values = Arrays.copyOfRange(parts, 2, parts.length);
            loaded.put(String.valueOf(id), new LogRecord(id, parts[1], values));
            highestId = Math.max(highestId, id);
        }

        records = loaded;
        nextId = highestId + 1;
    }

    /** Saves all entries to the file (replaces the old content). */
    private void saveRecords() throws IOException {
        List<String> lines = new ArrayList<>(headerLines());

        List<LogRecord> all = getAll();
        for (int i = all.size() - 1; i >= 0; i--) { // oldest first in the file
            LogRecord record = all.get(i);

            List<String> parts = new ArrayList<>();
            parts.add(String.valueOf(record.getId()));
            parts.add(record.getStatus());
            parts.addAll(Arrays.asList(record.getValues()));
            lines.add(FileText.join(parts.toArray(new String[0])));
        }
        Files.write(filePath, lines, StandardCharsets.UTF_8);
    }
}
/*
 * ICTD IMS - LOG RECORD
 *
 * One saved log entry: an id, a status, and the values of its fields
 * (in the same order as LogType.getFields()).
 */
package backend;

public class LogRecord {

    private final int id;
    private final String status;
    private final String[] values;

    LogRecord(int id, String status, String[] values) {
        this.id = id;
        this.status = status;
        this.values = values.clone();
    }

    public int getId() {
        return id;
    }

    public String getStatus() {
        return status;
    }

    /** Value of the field at this position. */
    public String getValue(int index) {
        return values[index];
    }

    /** All field values (a copy). */
    public String[] getValues() {
        return values.clone();
    }
}
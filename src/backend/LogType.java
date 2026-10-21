/*
 * ICTD IMS - LOG TYPE
 *
 * The kinds of logs the system keeps. Each type has:
 *   - a title shown on the page
 *   - a file name (data/logs_<fileName>.txt)
 *   - the fields of one log entry (these are also the table columns)
 *   - which fields are optional
 *   - the status a new entry starts with, and the status "Done" sets
 *
 * Every entry also has a Status column, which is added automatically.
 * Fields with "Date" in the name must be written as yyyy-MM-dd.
 */
package backend;

public enum LogType {

    INCOMING_EQUIPMENT("Incoming Equipment Logs", "incoming_equipment",
            Fields.EQUIPMENT, Fields.EQUIPMENT_OPTIONAL,
            "Pending", "Done", "Done"),

    OUTGOING_EQUIPMENT("Outgoing Equipment Logs", "outgoing_equipment",
            Fields.EQUIPMENT, Fields.EQUIPMENT_OPTIONAL,
            "Pending", "Done", "Done"),

    INCOMING_COMMUNICATION("Incoming Communication Logs", "incoming_communication",
            Fields.COMMUNICATION, Fields.NONE,
            "Pending", "Done", "Done"),

    OUTGOING_COMMUNICATION("Outgoing Communication Logs", "outgoing_communication",
            Fields.COMMUNICATION, Fields.NONE,
            "Pending", "Done", "Done"),

    BORROW_EQUIPMENT("Borrow Equipment Logs", "borrow_equipment",
            Fields.BORROW, Fields.BORROW_OPTIONAL,
            "Borrowed", "Returned", "Mark Returned"),

    EQUIPMENT_REQUEST("Equipment Request Logs", "equipment_request",
            Fields.REQUEST, Fields.NONE,
            "Pending", "Done", "Done");

    // The field lists (kept in a small class so the constants above can use them)
    private static class Fields {
        static final String[] NONE = {};

        static final String[] EQUIPMENT = {
                "Date", "Department/Office", "End-user", "Equipment Name",
                "Equipment Model", "Serial Number", "Inclusions", "Received by"};
        static final String[] EQUIPMENT_OPTIONAL = {
                "Equipment Model", "Serial Number", "Inclusions"};

        static final String[] COMMUNICATION = {
                "Date", "Department", "Doc Code", "Description", "Received by"};

        static final String[] BORROW = {
                "Date Borrowed", "Borrower", "Department/Office", "Equipment Name",
                "Serial Number", "Due Date", "Released by"};
        static final String[] BORROW_OPTIONAL = {"Serial Number"};

        static final String[] REQUEST = {
                "Date", "Requested by", "Department/Office", "Equipment", "Purpose"};
    }

    private final String title;
    private final String fileName;
    private final String[] fields;
    private final String[] optionalFields;
    private final String startStatus;
    private final String doneStatus;
    private final String doneButtonLabel;

    LogType(String title, String fileName, String[] fields, String[] optionalFields,
            String startStatus, String doneStatus, String doneButtonLabel) {
        this.title = title;
        this.fileName = fileName;
        this.fields = fields;
        this.optionalFields = optionalFields;
        this.startStatus = startStatus;
        this.doneStatus = doneStatus;
        this.doneButtonLabel = doneButtonLabel;
    }

    public String getTitle() {
        return title;
    }

    public String getFileName() {
        return fileName;
    }

    /** The field names of one entry (a copy, so the original cannot be changed). */
    public String[] getFields() {
        return fields.clone();
    }

    public int getFieldCount() {
        return fields.length;
    }

    /** Position of a field in the list, or -1 if there is no such field. */
    public int indexOfField(String fieldName) {
        for (int i = 0; i < fields.length; i++) {
            if (fields[i].equals(fieldName)) {
                return i;
            }
        }
        return -1;
    }

    /** True if the field can be left empty. */
    public boolean isOptional(String fieldName) {
        for (String optional : optionalFields) {
            if (optional.equals(fieldName)) {
                return true;
            }
        }
        return false;
    }

    /** True if the field holds a date (its name contains "Date"). */
    public boolean isDateField(String fieldName) {
        return fieldName.contains("Date");
    }

    public String getStartStatus() {
        return startStatus;
    }

    public String getDoneStatus() {
        return doneStatus;
    }

    public String getDoneButtonLabel() {
        return doneButtonLabel;
    }
}
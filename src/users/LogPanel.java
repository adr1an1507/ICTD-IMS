/*
 * ============================================================================
 *  ICTD IMS - LOG PANEL
 * ============================================================================
 *  What this program does:
 *    A working log page: a table of saved log entries with the buttons
 *    Add, Edit, Delete, and Done. It is used for the equipment logs, the
 *    communication logs, the borrow log, and the request log. Which log it
 *    shows depends on the LogType given to it.
 *
 *  How it works:
 *    - LogStorage saves the entries in a file (data/logs_<type>.txt).
 *    - Add and Edit open a form (FormDialog). The form is shown again with
 *      the typed text if something is wrong, so nothing has to be retyped.
 *    - Done sets the status of the selected entry (for example Pending ->
 *      Done, or Borrowed -> Returned).
 *    - The table reloads after every change.
 *
 *  Any dashboard can show it:  new LogPanel(LogType.INCOMING_EQUIPMENT, name)
 * ============================================================================
 */
package users;

import backend.LogRecord;
import backend.LogStorage;
import backend.LogType;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class LogPanel extends JPanel {

    // Colors and fonts (same style as the dashboards)
    private static final Color PRIMARY_COLOR = new Color(24, 54, 99);
    private static final Color BACKGROUND_COLOR = new Color(236, 240, 246);
    private static final Color TEXT_COLOR = new Color(33, 37, 41);
    private static final Color BORDER_COLOR = new Color(220, 224, 230);
    private static final Font TITLE_FONT = new Font("SansSerif", Font.BOLD, 22);
    private static final Font BODY_FONT = new Font("SansSerif", Font.PLAIN, 13);

    private final LogType type;
    private final LogStorage storage;
    private final String userName;          // used to fill in "Received by" / "Released by"

    private final DefaultTableModel tableModel;
    private final JTable table;

    // The entries shown in the table, in the same order as the table rows
    private List<LogRecord> shownRecords = new ArrayList<>();

    /**
     * @param type     which log to show
     * @param userName full name of the logged-in user
     * @throws IOException if the log file cannot be created or read
     */
    public LogPanel(LogType type, String userName) throws IOException {
        this.type = type;
        this.userName = userName;
        this.storage = new LogStorage(type); // loads the saved entries

        setLayout(new BorderLayout(0, 15));
        setBackground(BACKGROUND_COLOR);
        setBorder(new EmptyBorder(25, 30, 25, 30));

        // Table columns = the log fields + Status
        String[] fields = type.getFields();
        String[] columns = new String[fields.length + 1];
        System.arraycopy(fields, 0, columns, 0, fields.length);
        columns[fields.length] = "Status";

        // The cells cannot be edited by typing
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table = new JTable(tableModel);
        styleTable();
        setColumnWidths(fields);

        JLabel title = new JLabel(type.getTitle());
        title.setFont(TITLE_FONT);
        title.setForeground(TEXT_COLOR);

        add(title, BorderLayout.NORTH);
        add(createTableScrollPane(), BorderLayout.CENTER);
        add(createButtons(), BorderLayout.SOUTH);

        refreshTable();
    }

    // ==================================================================
    // Buttons
    // ==================================================================
    private JPanel createButtons() {
        JButton addButton = new JButton("Add");
        JButton editButton = new JButton("Edit");
        JButton deleteButton = new JButton("Delete");
        JButton doneButton = new JButton(type.getDoneButtonLabel());

        addButton.addActionListener(e -> handleAdd());
        editButton.addActionListener(e -> handleEdit());
        deleteButton.addActionListener(e -> handleDelete());
        doneButton.addActionListener(e -> handleDone());

        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        panel.setBackground(BACKGROUND_COLOR);
        panel.add(addButton);
        panel.add(editButton);
        panel.add(deleteButton);
        panel.add(doneButton);
        return panel;
    }

    // ==================================================================
    // Add, Edit, Delete, Done
    // ==================================================================
    private void handleAdd() {
        String[] fields = type.getFields();

        // Start the form with useful values (today's date, the user's name)
        String[] values = new String[fields.length];
        for (int i = 0; i < fields.length; i++) {
            values[i] = defaultValue(fields[i]);
        }

        // Show the form again until the entry is valid (or the user cancels)
        while (true) {
            values = Formdialog.show(this, "Add Log Entry", fields, optionalFlags(), values);
            if (values == null) {
                return; // cancelled
            }

            try {
                storage.add(values);
                break;
            } catch (IllegalArgumentException ex) {
                showWarning(ex.getMessage()); // for example: Date is required.
            } catch (IOException ex) {
                showError("The entry could not be saved.\n" + ex.getMessage());
                return;
            }
        }
        refreshTable();
    }

    private void handleEdit() {
        LogRecord record = getSelectedRecord();
        if (record == null) {
            showWarning("Please select a log to edit.");
            return;
        }

        String[] values = record.getValues();
        while (true) {
            values = Formdialog.show(this, "Edit Log Entry", type.getFields(),
                    optionalFlags(), values);
            if (values == null) {
                return; // cancelled
            }

            try {
                if (!storage.update(record.getId(), values)) {
                    showWarning("That entry no longer exists.");
                }
                break;
            } catch (IllegalArgumentException ex) {
                showWarning(ex.getMessage());
            } catch (IOException ex) {
                showError("The entry could not be saved.\n" + ex.getMessage());
                return;
            }
        }
        refreshTable();
    }

    private void handleDelete() {
        LogRecord record = getSelectedRecord();
        if (record == null) {
            showWarning("Please select a log to delete.");
            return;
        }

        int result = JOptionPane.showConfirmDialog(this, "Delete the selected log?",
                "Confirm Delete", JOptionPane.YES_NO_OPTION);
        if (result != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            storage.delete(record.getId());
        } catch (IOException ex) {
            showError("The entry could not be deleted.\n" + ex.getMessage());
            return;
        }
        refreshTable();
    }

    private void handleDone() {
        LogRecord record = getSelectedRecord();
        if (record == null) {
            showWarning("Please select a log first.");
            return;
        }
        if (record.getStatus().equals(type.getDoneStatus())) {
            showWarning("This log is already marked as " + type.getDoneStatus() + ".");
            return;
        }

        try {
            storage.setStatus(record.getId(), type.getDoneStatus());
        } catch (IOException ex) {
            showError("The status could not be saved.\n" + ex.getMessage());
            return;
        }
        refreshTable();
    }

    // ==================================================================
    // Table
    // ==================================================================

    /** Reloads the table from the saved entries. */
    private void refreshTable() {
        shownRecords = storage.getAll();
        tableModel.setRowCount(0); // remove old rows

        for (LogRecord record : shownRecords) {
            Object[] row = new Object[tableModel.getColumnCount()];
            String[] values = record.getValues();
            System.arraycopy(values, 0, row, 0, values.length);
            row[values.length] = record.getStatus();
            tableModel.addRow(row);
        }
    }

    /** The entry of the selected row, or null if no row is selected. */
    private LogRecord getSelectedRecord() {
        int viewRow = table.getSelectedRow();
        if (viewRow == -1) {
            return null;
        }
        // The table can be sorted, so convert to the real row number
        return shownRecords.get(table.convertRowIndexToModel(viewRow));
    }

    // ==================================================================
    // Small helpers
    // ==================================================================

    /** The starting text of a field in the Add form. */
    private String defaultValue(String field) {
        if (field.equals("Due Date")) {
            return ""; // the user must choose it
        }
        if (type.isDateField(field)) {
            return LocalDate.now().toString(); // today, as yyyy-MM-dd
        }
        if (field.equals("Received by") || field.equals("Released by")) {
            return userName;
        }
        return "";
    }

    /** true for each field that may stay empty. */
    private boolean[] optionalFlags() {
        String[] fields = type.getFields();
        boolean[] flags = new boolean[fields.length];
        for (int i = 0; i < fields.length; i++) {
            flags[i] = type.isOptional(fields[i]);
        }
        return flags;
    }

    private void showWarning(String message) {
        JOptionPane.showMessageDialog(this, message, "ICTD IMS", JOptionPane.WARNING_MESSAGE);
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "ICTD IMS", JOptionPane.ERROR_MESSAGE);
    }

    // ==================================================================
    // Table look
    // ==================================================================
    private void styleTable() {
        table.setFont(BODY_FONT);
        table.setRowHeight(34);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        table.setFillsViewportHeight(true);
        table.setAutoCreateRowSorter(true); // click a header to sort
        table.setGridColor(BORDER_COLOR);
        table.setShowGrid(true);
        table.setIntercellSpacing(new Dimension(1, 1));
        table.setSelectionBackground(new Color(220, 232, 248));
        table.setSelectionForeground(TEXT_COLOR);

        JTableHeader header = table.getTableHeader();
        header.setFont(new Font("SansSerif", Font.BOLD, 12));
        header.setBackground(PRIMARY_COLOR);
        header.setForeground(Color.WHITE);
        header.setPreferredSize(new Dimension(header.getPreferredSize().width, 38));
        header.setReorderingAllowed(false);
    }

    /** Wider columns for long text, normal width for the rest. */
    private void setColumnWidths(String[] fields) {
        for (int i = 0; i < table.getColumnModel().getColumnCount(); i++) {
            int width = 150;
            if (i < fields.length) {
                String name = fields[i];
                if (name.equals("Description") || name.equals("Purpose")) {
                    width = 260;
                } else if (type.isDateField(name)) {
                    width = 115;
                }
            } else {
                width = 100; // the Status column
            }
            table.getColumnModel().getColumn(i).setPreferredWidth(width);
            table.getColumnModel().getColumn(i).setMinWidth(80);
        }
    }

    private JScrollPane createTableScrollPane() {
        JScrollPane scrollPane = new JScrollPane(table,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
        scrollPane.getViewport().setBackground(Color.WHITE);
        scrollPane.getHorizontalScrollBar().setUnitIncrement(16);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        return scrollPane;
    }
}
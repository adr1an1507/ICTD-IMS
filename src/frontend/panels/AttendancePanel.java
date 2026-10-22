/*
 * ============================================================================
 *  ICTD IMS - ATTENDANCE PANEL
 * ============================================================================
 *  What this program does:
 *    The READS Attendance page. A READS Scholar clicks Time In when arriving
 *    and Time Out when leaving. The time is taken from the computer clock.
 *    A table shows the scholar's own attendance history.
 *
 *  Rules:
 *    - One Time In and one Time Out for each day.
 *    - Time Out is only possible after Time In.
 *    - The first Time In asks for the SPN and name. The next days the form
 *      is already filled in with the last values.
 *
 *  How it works:
 *    AttendanceStorage saves the records in data/attendance.txt.
 *    A record is found by username + date, so each scholar only sees and
 *    changes their own attendance.
 * ============================================================================
 */
package frontend.panels;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
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

import backend.storage.AttendanceStorage;
import frontend.components.Formdialog;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class AttendancePanel extends JPanel {

    // Colors and fonts (same style as the dashboards)
    private static final Color PRIMARY_COLOR = new Color(24, 54, 99);
    private static final Color BACKGROUND_COLOR = new Color(236, 240, 246);
    private static final Color TEXT_COLOR = new Color(33, 37, 41);
    private static final Color MUTED_TEXT_COLOR = new Color(95, 105, 120);
    private static final Color BORDER_COLOR = new Color(220, 224, 230);
    private static final Font TITLE_FONT = new Font("SansSerif", Font.BOLD, 22);
    private static final Font CARD_TITLE_FONT = new Font("SansSerif", Font.BOLD, 14);
    private static final Font BODY_FONT = new Font("SansSerif", Font.PLAIN, 13);

    private static final String NO_TIME = "--"; // shown when there is no time yet

    private final AttendanceStorage storage;
    private final String username;   // identifies the scholar (the login username)
    private final String fullName;   // used to guess the names on the first Time In

    private JLabel todayLabel;
    private JLabel statusLabel;
    private JButton timeInButton;
    private JButton timeOutButton;
    private final DefaultTableModel tableModel;

    /**
     * @param username the login username of the READS Scholar
     * @param fullName the full name of the READS Scholar
     * @throws IOException if the attendance file cannot be created or read
     */
    public AttendancePanel(String username, String fullName) throws IOException {
        this.username = username;
        this.fullName = fullName;
        this.storage = new AttendanceStorage(); // loads the saved records

        setLayout(new BorderLayout(0, 15));
        setBackground(BACKGROUND_COLOR);
        setBorder(new EmptyBorder(25, 30, 25, 30));

        // The cells cannot be edited by typing
        String[] columns = {"Date", "SPN", "First Name", "Last Name", "Morning In", "Morning Out"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable table = new JTable(tableModel);
        styleTable(table);

        // Top: page title and today's card
        JLabel title = new JLabel("READS Attendance");
        title.setFont(TITLE_FONT);
        title.setForeground(TEXT_COLOR);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel todayCard = createTodayCard();
        todayCard.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel top = new JPanel();
        top.setBackground(BACKGROUND_COLOR);
        top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));
        top.add(title);
        top.add(Box.createVerticalStrut(15));
        top.add(todayCard);

        add(top, BorderLayout.NORTH);
        add(createTableScrollPane(table), BorderLayout.CENTER);

        refresh();
    }

    // ==================================================================
    // Today card (date, status, Time In / Time Out buttons)
    // ==================================================================
    private JPanel createTodayCard() {
        JLabel cardTitle = new JLabel("Today");
        cardTitle.setFont(CARD_TITLE_FONT);
        cardTitle.setForeground(TEXT_COLOR);

        todayLabel = new JLabel();
        todayLabel.setFont(BODY_FONT);
        todayLabel.setForeground(MUTED_TEXT_COLOR);

        statusLabel = new JLabel();
        statusLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
        statusLabel.setForeground(PRIMARY_COLOR);

        JPanel textPanel = new JPanel();
        textPanel.setBackground(Color.WHITE);
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        cardTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        todayLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        statusLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        textPanel.add(cardTitle);
        textPanel.add(Box.createVerticalStrut(3));
        textPanel.add(todayLabel);
        textPanel.add(Box.createVerticalStrut(10));
        textPanel.add(statusLabel);

        timeInButton = new JButton("Time In");
        timeOutButton = new JButton("Time Out");
        timeInButton.addActionListener(e -> handleTimeIn());
        timeOutButton.addActionListener(e -> handleTimeOut());

        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.add(timeInButton);
        buttonPanel.add(timeOutButton);

        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR),
                new EmptyBorder(16, 18, 16, 18)));
        card.add(textPanel, BorderLayout.CENTER);
        card.add(buttonPanel, BorderLayout.EAST);
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));
        return card;
    }

    // ==================================================================
    // Time In / Time Out
    // ==================================================================
    private void handleTimeIn() {
        String[] labels = {"SPN", "First Name", "Last Name"};
        boolean[] optional = {false, false, false};

        // Start the form with the last values, or a guess from the full name
        String[] values;
        AttendanceStorage.Record last = storage.getLatestRecord(username);
        if (last != null) {
            values = new String[]{last.getSpn(), last.getFirstName(), last.getLastName()};
        } else {
            values = new String[]{"", guessFirstName(), guessLastName()};
        }

        // Show the form again until it is valid (or the user cancels)
        while (true) {
            values = Formdialog.show(this, "Time In", labels, optional, values);
            if (values == null) {
                return; // cancelled
            }

            try {
                AttendanceStorage.Record record =
                        storage.timeIn(username, values[0], values[1], values[2]);
                refresh();
                showInfo("Time In recorded at " + record.getMorningIn() + ".");
                return;
            } catch (IllegalArgumentException ex) {
                showWarning(ex.getMessage()); // for example: SPN is required.
            } catch (IllegalStateException ex) {
                showWarning(ex.getMessage()); // already timed in today
                refresh();
                return;
            } catch (IOException ex) {
                showError("The attendance could not be saved.\n" + ex.getMessage());
                return;
            }
        }
    }

    private void handleTimeOut() {
        try {
            AttendanceStorage.Record record = storage.timeOut(username);
            refresh();
            showInfo("Time Out recorded at " + record.getMorningOut() + ".");
        } catch (IllegalStateException ex) {
            showWarning(ex.getMessage()); // not timed in yet, or already timed out
            refresh();
        } catch (IOException ex) {
            showError("The attendance could not be saved.\n" + ex.getMessage());
        }
    }

    // ==================================================================
    // Showing the data
    // ==================================================================

    /** Updates the today card, the buttons, and the table. */
    private void refresh() {
        todayLabel.setText(LocalDate.now().format(
                DateTimeFormatter.ofPattern("EEEE, MMMM dd, yyyy", Locale.ENGLISH)));

        // Today's status and which buttons can be used
        AttendanceStorage.Record today = storage.getToday(username);
        if (today == null) {
            statusLabel.setText("Morning In: " + NO_TIME + "      Morning Out: " + NO_TIME);
            timeInButton.setEnabled(true);
            timeOutButton.setEnabled(false);
        } else {
            statusLabel.setText("Morning In: " + show(today.getMorningIn())
                    + "      Morning Out: " + show(today.getMorningOut()));
            timeInButton.setEnabled(false);
            timeOutButton.setEnabled(today.getMorningOut().isEmpty());
        }

        // The scholar's own history (newest day first)
        tableModel.setRowCount(0);
        for (AttendanceStorage.Record record : storage.getRecordsFor(username)) {
            tableModel.addRow(new Object[]{
                    record.getDate(), record.getSpn(),
                    record.getFirstName(), record.getLastName(),
                    show(record.getMorningIn()), show(record.getMorningOut())});
        }
    }

    /** Shows "--" instead of an empty time. */
    private String show(String time) {
        return time.isEmpty() ? NO_TIME : time;
    }

    // Guess the names from the full name (the scholar can correct them in the form)
    private String guessFirstName() {
        int space = fullName.trim().lastIndexOf(' ');
        return space == -1 ? fullName.trim() : fullName.trim().substring(0, space);
    }

    private String guessLastName() {
        int space = fullName.trim().lastIndexOf(' ');
        return space == -1 ? "" : fullName.trim().substring(space + 1);
    }

    // ==================================================================
    // Messages
    // ==================================================================
    private void showInfo(String message) {
        JOptionPane.showMessageDialog(this, message, "ICTD IMS", JOptionPane.INFORMATION_MESSAGE);
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
    private void styleTable(JTable table) {
        table.setFont(BODY_FONT);
        table.setRowHeight(34);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setFillsViewportHeight(true);
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

    private JScrollPane createTableScrollPane(JTable table) {
        JScrollPane scrollPane = new JScrollPane(table,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
        scrollPane.getViewport().setBackground(Color.WHITE);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        return scrollPane;
    }
}
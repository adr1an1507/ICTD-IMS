package frontend.dashboards;

import backend.models.LogRecord;
import backend.models.LogType;
import backend.storage.LogStorage;
import frontend.auth.LoginPage;
import frontend.panels.AttendancePanel;
import frontend.panels.LogPanel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Date;

/**
 * READSDashboard
 *
 * Creates the dashboard for READS Scholars in the ICTD IMS.
 * This version focuses on the user interface. Database storage
 * and authentication integration can be added later.
 */
public class READSDashboard extends JFrame {

    // COLORS
    private static final Color PRIMARY_COLOR = new Color(24, 54, 99);
    private static final Color PRIMARY_HOVER = new Color(36, 76, 135);
    private static final Color BACKGROUND_COLOR = new Color(236, 240, 246);
    private static final Color CARD_COLOR = Color.WHITE;
    private static final Color TEXT_COLOR = new Color(33, 37, 41);
    private static final Color MUTED_TEXT_COLOR = new Color(95, 105, 120);
    private static final Color BORDER_COLOR = new Color(220, 224, 230);

    // FONTS
    private static final Font TITLE_FONT = new Font("SansSerif", Font.BOLD, 22);
    private static final Font HEADING_FONT = new Font("SansSerif", Font.BOLD, 18);
    private static final Font CARD_TITLE_FONT = new Font("SansSerif", Font.BOLD, 14);
    private static final Font BODY_FONT = new Font("SansSerif", Font.PLAIN, 13);
    private static final Font BUTTON_FONT = new Font("SansSerif", Font.BOLD, 13);

    // MAIN COMPONENTS
    private JPanel sideBar;
    private JPanel contentPanel;
    private final String scholarName;
    private final String username;     // login username (used for attendance)
    private final LoginPage loginPage; // shown again on Logout

    /**
     * Creates the READS Scholar Dashboard.
     *
     * @param scholarName full name of the logged-in READS Scholar
     * @param username    login username of the READS Scholar
     * @param loginPage   the existing login page (shown again on Logout)
     */
    public READSDashboard(String scholarName, String username, LoginPage loginPage) {
        this.scholarName = scholarName;
        this.username = username;
        this.loginPage = loginPage;
        buildDashboard();
        configureFrame();
        showHome();
    }

    // BUILD DASHBOARD
    private void buildDashboard() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(BACKGROUND_COLOR);

        sideBar = createSideBar();

        contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBackground(BACKGROUND_COLOR);
        contentPanel.setMinimumSize(new Dimension(0, 0));

        mainPanel.add(sideBar, BorderLayout.WEST);
        mainPanel.add(contentPanel, BorderLayout.CENTER);
        setContentPane(mainPanel);
    }

    // SIDEBAR
    private JPanel createSideBar() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setPreferredSize(new Dimension(240, 0));
        panel.setBackground(PRIMARY_COLOR);

        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(PRIMARY_COLOR);
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
        headerPanel.setBorder(new EmptyBorder(25, 22, 25, 22));
        headerPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel titleLabel = new JLabel("ICTD IMS");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 24));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel roleLabel = new JLabel("READS Scholar");
        roleLabel.setFont(new Font("SansSerif", Font.PLAIN, 13));
        roleLabel.setForeground(new Color(220, 225, 235));
        roleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        headerPanel.add(titleLabel);
        headerPanel.add(Box.createVerticalStrut(4));
        headerPanel.add(roleLabel);
        panel.add(headerPanel, BorderLayout.NORTH);

        JPanel navigationPanel = new JPanel();
        navigationPanel.setBackground(PRIMARY_COLOR);
        navigationPanel.setLayout(new BoxLayout(navigationPanel, BoxLayout.Y_AXIS));
        navigationPanel.setBorder(new EmptyBorder(5, 12, 10, 12));

        JButton homeButton = createNavigationButton("Home");
        homeButton.addActionListener(e -> showHome());

        JButton incomingEquipmentButton = createNavigationButton("Incoming Equipment");
        incomingEquipmentButton.addActionListener(
                e -> showLog(LogType.INCOMING_EQUIPMENT));

        JButton outgoingEquipmentButton = createNavigationButton("Outgoing Equipment");
        outgoingEquipmentButton.addActionListener(
                e -> showLog(LogType.OUTGOING_EQUIPMENT));

        JButton incomingCommunicationButton = createNavigationButton("Incoming Communication");
        incomingCommunicationButton.addActionListener(
                e -> showLog(LogType.INCOMING_COMMUNICATION));

        JButton outgoingCommunicationButton = createNavigationButton("Outgoing Communication");
        outgoingCommunicationButton.addActionListener(
                e -> showLog(LogType.OUTGOING_COMMUNICATION));

        JButton borrowButton = createNavigationButton("Borrow Equipment");
        borrowButton.addActionListener(
                e -> showLog(LogType.BORROW_EQUIPMENT));

        JButton requestButton = createNavigationButton("Request Equipment");
        requestButton.addActionListener(
                e -> showLog(LogType.EQUIPMENT_REQUEST));

        JButton attendanceButton = createNavigationButton("READS Attendance");
        attendanceButton.addActionListener(e -> showAttendance());

        navigationPanel.add(homeButton);
        navigationPanel.add(Box.createVerticalStrut(5));
        navigationPanel.add(createSectionLabel("LOGS"));
        navigationPanel.add(incomingEquipmentButton);
        navigationPanel.add(outgoingEquipmentButton);
        navigationPanel.add(incomingCommunicationButton);
        navigationPanel.add(outgoingCommunicationButton);
        navigationPanel.add(borrowButton);
        navigationPanel.add(requestButton);
        navigationPanel.add(Box.createVerticalStrut(8));
        navigationPanel.add(createSectionLabel("ATTENDANCE"));
        navigationPanel.add(attendanceButton);

        panel.add(navigationPanel, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel();
        bottomPanel.setBackground(PRIMARY_COLOR);
        bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.Y_AXIS));
        bottomPanel.setBorder(new EmptyBorder(10, 12, 20, 12));

        JButton profileButton = createNavigationButton("Profile");
        profileButton.addActionListener(e -> showProfile());

        JButton logoutButton = createNavigationButton("Logout");
        logoutButton.addActionListener(e -> logout());

        bottomPanel.add(profileButton);
        bottomPanel.add(Box.createVerticalStrut(5));
        bottomPanel.add(logoutButton);
        panel.add(bottomPanel, BorderLayout.SOUTH);

        return panel;
    }

    // NAVIGATION BUTTON
    private JButton createNavigationButton(String text) {
        JButton button = new JButton(text);
        button.setFont(BUTTON_FONT);
        button.setForeground(Color.WHITE);
        button.setBackground(PRIMARY_COLOR);
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setAlignmentX(Component.LEFT_ALIGNMENT);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        button.setPreferredSize(new Dimension(216, 42));

        button.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                button.setBackground(PRIMARY_HOVER);
                button.setOpaque(true);
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                button.setBackground(PRIMARY_COLOR);
                button.setOpaque(false);
            }
        });

        return button;
    }

    // SIDEBAR SECTION LABEL
    private JLabel createSectionLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("SansSerif", Font.BOLD, 10));
        label.setForeground(new Color(180, 190, 205));
        label.setBorder(new EmptyBorder(10, 12, 5, 0));
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }

    // HOME DASHBOARD
    private void showHome() {
        contentPanel.removeAll();

        // Outer panel fills the content area beside the sidebar.
        JPanel homePanel = new JPanel(new BorderLayout());
        homePanel.setBackground(BACKGROUND_COLOR);
        homePanel.setBorder(new EmptyBorder(25, 30, 25, 30));

        // A vertical BoxLayout keeps every dashboard card left-aligned.
        JPanel dashboardContent = new JPanel();
        dashboardContent.setBackground(BACKGROUND_COLOR);
        dashboardContent.setLayout(new BoxLayout(dashboardContent, BoxLayout.Y_AXIS));
        dashboardContent.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Welcome card
        JPanel welcomePanel = createCardPanel();
        welcomePanel.setLayout(new BoxLayout(welcomePanel, BoxLayout.Y_AXIS));
        welcomePanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel welcomeLabel = new JLabel("Welcome, " + scholarName + "!");
        welcomeLabel.setFont(TITLE_FONT);
        welcomeLabel.setForeground(TEXT_COLOR);
        welcomeLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        String currentDate = new SimpleDateFormat("MMMM dd, yyyy").format(new Date());
        JLabel dateLabel = new JLabel(currentDate);
        dateLabel.setFont(BODY_FONT);
        dateLabel.setForeground(MUTED_TEXT_COLOR);
        dateLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel messageLabel = new JLabel(
                "<html>Welcome to your READS Scholar Dashboard. "
                + "Here you can view your tasks, equipment,<br>"
                + "activities, and ICTD announcements.</html>");
        messageLabel.setFont(BODY_FONT);
        messageLabel.setForeground(MUTED_TEXT_COLOR);
        messageLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        welcomePanel.add(welcomeLabel);
        welcomePanel.add(Box.createVerticalStrut(5));
        welcomePanel.add(dateLabel);
        welcomePanel.add(Box.createVerticalStrut(8));
        welcomePanel.add(messageLabel);
        welcomePanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 150));

        dashboardContent.add(welcomePanel);
        dashboardContent.add(Box.createVerticalStrut(15));

        JPanel taskSummary = createTaskSummary();
        dashboardContent.add(taskSummary);
        dashboardContent.add(Box.createVerticalStrut(15));

        JPanel myTasks = createMyTasks();
        dashboardContent.add(myTasks);
        dashboardContent.add(Box.createVerticalStrut(15));

        JPanel equipmentStatus = createEquipmentStatus();
        dashboardContent.add(equipmentStatus);
        dashboardContent.add(Box.createVerticalStrut(15));

        JPanel recentActivities = createRecentActivities();
        dashboardContent.add(recentActivities);
        dashboardContent.add(Box.createVerticalStrut(15));

        dashboardContent.add(createAnnouncements());

        JScrollPane scrollPane = new JScrollPane(dashboardContent);
        scrollPane.setBorder(null);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.getViewport().setBackground(BACKGROUND_COLOR);

        homePanel.add(scrollPane, BorderLayout.CENTER);
        contentPanel.add(homePanel, BorderLayout.CENTER);
        contentPanel.revalidate();
        contentPanel.repaint();
    }

    // TASK SUMMARY
    private JPanel createTaskSummary() {
        JPanel panel = createCardPanel();
        panel.setLayout(new BorderLayout(0, 12));

        JLabel title = new JLabel("Task Summary");
        title.setFont(CARD_TITLE_FONT);
        title.setForeground(TEXT_COLOR);

        JPanel summary = new JPanel(new GridLayout(1, 4, 10, 0));
        summary.setBackground(CARD_COLOR);
        summary.add(createSummaryItem("Assigned", "0"));
        summary.add(createSummaryItem("Pending", "0"));
        summary.add(createSummaryItem("In Progress", "0"));
        summary.add(createSummaryItem("Completed", "0"));

        panel.add(title, BorderLayout.NORTH);
        panel.add(summary, BorderLayout.CENTER);
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 125));
        return panel;
    }

    // SUMMARY ITEM
    private JPanel createSummaryItem(String title, String value) {
        JPanel panel = new JPanel();
        panel.setBackground(new Color(245, 247, 250));
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(new EmptyBorder(12, 15, 12, 15));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(BODY_FONT);
        titleLabel.setForeground(MUTED_TEXT_COLOR);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("SansSerif", Font.BOLD, 22));
        valueLabel.setForeground(PRIMARY_COLOR);
        valueLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        panel.add(titleLabel);
        panel.add(Box.createVerticalStrut(4));
        panel.add(valueLabel);
        return panel;
    }

    // MY TASKS
    private JPanel createMyTasks() {
        JPanel panel = createCardPanel();
        panel.setLayout(new BorderLayout(0, 10));

        JLabel title = new JLabel("My Tasks");
        title.setFont(CARD_TITLE_FONT);
        title.setForeground(TEXT_COLOR);

        String[] columns = {"Task Description", "Due Date", "Status", "Action"};
        DefaultTableModel model = new DefaultTableModel(columns, 0);
        JTable table = new JTable(model);
        styleTable(table);
        model.addRow(new Object[]{"No tasks assigned", "-", "-", "-"});

        panel.add(title, BorderLayout.NORTH);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.setPreferredSize(new Dimension(700, 115));
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 150));
        return panel;
    }

    // EQUIPMENT STATUS (numbers come from the Borrow Equipment log)
    private JPanel createEquipmentStatus() {
        int borrowed = 0;   // still out
        int returned = 0;
        int due = 0;        // still out, due today
        int overdue = 0;    // still out, due date has passed

        try {
            LogType borrowType = LogType.BORROW_EQUIPMENT;
            int dueIndex = borrowType.indexOfField("Due Date");
            LocalDate today = LocalDate.now();

            for (LogRecord record : new LogStorage(borrowType).getAll()) {
                if (record.getStatus().equals(borrowType.getDoneStatus())) {
                    returned++;
                    continue;
                }
                borrowed++;
                try {
                    LocalDate dueDate = LocalDate.parse(record.getValue(dueIndex));
                    if (dueDate.isEqual(today)) {
                        due++;
                    } else if (dueDate.isBefore(today)) {
                        overdue++;
                    }
                } catch (DateTimeParseException ex) {
                    // a damaged date: skip it
                }
            }
        } catch (IOException ex) {
            // the file could not be read: the numbers stay 0
        }

        JPanel panel = createCardPanel();
        panel.setLayout(new BorderLayout(0, 12));

        JLabel title = new JLabel("Equipment Status");
        title.setFont(CARD_TITLE_FONT);
        title.setForeground(TEXT_COLOR);

        JPanel statusPanel = new JPanel(new GridLayout(1, 4, 10, 0));
        statusPanel.setBackground(CARD_COLOR);
        statusPanel.add(createSummaryItem("Borrowed", String.valueOf(borrowed)));
        statusPanel.add(createSummaryItem("Returned", String.valueOf(returned)));
        statusPanel.add(createSummaryItem("Due", String.valueOf(due)));
        statusPanel.add(createSummaryItem("Overdue", String.valueOf(overdue)));

        panel.add(title, BorderLayout.NORTH);
        panel.add(statusPanel, BorderLayout.CENTER);
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 125));
        return panel;
    }

    // RECENT ACTIVITIES
    private JPanel createRecentActivities() {
        JPanel panel = createCardPanel();
        panel.setLayout(new BorderLayout(0, 8));

        JLabel title = new JLabel("Recent Activities");
        title.setFont(CARD_TITLE_FONT);
        title.setForeground(TEXT_COLOR);

        JTextArea activities = new JTextArea("No recent activities recorded.");
        activities.setFont(BODY_FONT);
        activities.setForeground(MUTED_TEXT_COLOR);
        activities.setBackground(CARD_COLOR);
        activities.setEditable(false);
        activities.setLineWrap(true);
        activities.setWrapStyleWord(true);
        activities.setBorder(new EmptyBorder(5, 0, 0, 0));

        panel.add(title, BorderLayout.NORTH);
        panel.add(activities, BorderLayout.CENTER);
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));
        return panel;
    }

    // ANNOUNCEMENTS
    private JPanel createAnnouncements() {
        JPanel panel = createCardPanel();
        panel.setLayout(new BorderLayout(0, 8));

        JLabel title = new JLabel("Announcements");
        title.setFont(CARD_TITLE_FONT);
        title.setForeground(TEXT_COLOR);

        JTextArea announcements = new JTextArea("No announcements available.");
        announcements.setFont(BODY_FONT);
        announcements.setForeground(MUTED_TEXT_COLOR);
        announcements.setBackground(CARD_COLOR);
        announcements.setEditable(false);
        announcements.setLineWrap(true);
        announcements.setWrapStyleWord(true);
        announcements.setBorder(new EmptyBorder(5, 0, 0, 0));

        panel.add(title, BorderLayout.NORTH);
        panel.add(announcements, BorderLayout.CENTER);
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));
        return panel;
    }

    // LOG PAGES (working: add, edit, delete, done - saved in a file)
    private void showLog(LogType type) {
        try {
            showPanel(new LogPanel(type, scholarName));
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this,
                    "The log file could not be opened.\n" + ex.getMessage(),
                    "File Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // READS ATTENDANCE (working: time in and time out - saved in a file)
    private void showAttendance() {
        try {
            showPanel(new AttendancePanel(username, scholarName));
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this,
                    "The attendance file could not be opened.\n" + ex.getMessage(),
                    "File Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Shows a page in the content area (replaces the old page)
    private void showPanel(JPanel panel) {
        contentPanel.removeAll();
        contentPanel.add(panel, BorderLayout.CENTER);
        contentPanel.revalidate();
        contentPanel.repaint();
    }

    // PROFILE
    private void showProfile() {
        JOptionPane.showMessageDialog(
                this,
                "READS Scholar Profile\n\nName: " + scholarName,
                "Profile",
                JOptionPane.INFORMATION_MESSAGE);
    }

    // LOGOUT
    private void logout() {
        int result = JOptionPane.showConfirmDialog(
                this, "Are you sure you want to logout?",
                "Logout", JOptionPane.YES_NO_OPTION);

        if (result == JOptionPane.YES_OPTION) {
            // Show the existing login page again (null only when testing this window alone)
            if (loginPage != null) {
                loginPage.showLoginPage();
            }
            dispose();
        }
    }

    // STANDARD WHITE CARD
    private JPanel createCardPanel() {
        JPanel panel = new JPanel();
        panel.setBackground(CARD_COLOR);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR),
                new EmptyBorder(16, 18, 16, 18)));
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        return panel;
    }

    // TABLE STYLE
    private void styleTable(JTable table) {
        table.setFont(BODY_FONT);
        table.setRowHeight(34);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
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

    // FRAME SETTINGS
    private void configureFrame() {
        setTitle("ICTD IMS - READS Scholar Dashboard");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1250, 750);
        setMinimumSize(new Dimension(1000, 650));
        setLocationRelativeTo(null);
    }

    // MAIN METHOD FOR TESTING THIS WINDOW ALONE (no login page, so Logout only closes it)
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(
                        UIManager.getCrossPlatformLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }

            new READSDashboard("READS Scholar", "scholar", null).setVisible(true);
        });
    }
}



/*
 * ============================================================================
 *  ICTD IMS - ROLE DASHBOARD (shared base class)
 * ============================================================================
 *  What this program does:
 *    Holds the parts that every role dashboard has in common, so they are
 *    written only once:
 *      - the window, the sidebar, and the content area
 *      - Profile and Logout buttons
 *      - helpers for cards, summary boxes, and tables
 *
 *  Who uses it:
 *    SecretaryDashboard, TechnicianDashboard, and OfficeHeadDashboard extend
 *    this class. Each one only adds its own menu items and pages.
 *
 *  How a dashboard is built (in the child class constructor):
 *    1. super(role, userName, loginPage)  - builds the empty window
 *    2. addNavButton(...)                 - adds the sidebar menu items
 *    3. showPage(...)                     - shows the first page
 *
 *  Logout shows the existing LoginPage again (no second login page).
 * ============================================================================
 */
package frontend.panels;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

import frontend.auth.LoginPage;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.text.SimpleDateFormat;
import java.util.Date;

public abstract class RoleDashboard extends JFrame {

    // ------------------------------------------------------------------
    // Colors and fonts (same style as the other ICTD IMS windows)
    // ------------------------------------------------------------------
    protected static final Color PRIMARY_COLOR = new Color(24, 54, 99);
    protected static final Color PRIMARY_HOVER = new Color(36, 76, 135);
    protected static final Color BACKGROUND_COLOR = new Color(236, 240, 246);
    protected static final Color CARD_COLOR = Color.WHITE;
    protected static final Color TEXT_COLOR = new Color(33, 37, 41);
    protected static final Color MUTED_TEXT_COLOR = new Color(95, 105, 120);
    protected static final Color BORDER_COLOR = new Color(220, 224, 230);

    protected static final Font TITLE_FONT = new Font("SansSerif", Font.BOLD, 22);
    protected static final Font CARD_TITLE_FONT = new Font("SansSerif", Font.BOLD, 14);
    protected static final Font BODY_FONT = new Font("SansSerif", Font.PLAIN, 13);
    protected static final Font BUTTON_FONT = new Font("SansSerif", Font.BOLD, 13);

    // ------------------------------------------------------------------
    // Data
    // ------------------------------------------------------------------
    private final String roleName;       // for example "Secretary"
    private final String userName;       // full name of the logged-in user
    private final LoginPage loginPage;   // shown again on Logout

    // ------------------------------------------------------------------
    // Components
    // ------------------------------------------------------------------
    private JPanel navigationPanel;      // the sidebar menu (child classes add items)
    private JPanel contentPanel;         // the area where pages are shown

    /**
     * Builds the empty dashboard window.
     *
     * @param roleName  the role shown in the sidebar and the title bar
     * @param userName  full name of the logged-in user
     * @param loginPage the existing login page (shown again on Logout)
     */
    protected RoleDashboard(String roleName, String userName, LoginPage loginPage) {
        this.roleName = roleName;
        this.userName = userName;
        this.loginPage = loginPage;

        buildFrame();
    }

    // ==================================================================
    // Window and sidebar
    // ==================================================================
    private void buildFrame() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(BACKGROUND_COLOR);

        contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBackground(BACKGROUND_COLOR);

        mainPanel.add(createSideBar(), BorderLayout.WEST);
        mainPanel.add(contentPanel, BorderLayout.CENTER);
        setContentPane(mainPanel);

        setTitle("ICTD IMS - " + roleName + " Dashboard");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // closing the window stops the program
        setSize(1250, 750);
        setMinimumSize(new Dimension(1000, 650));
        setLocationRelativeTo(null);
    }

    private JPanel createSideBar() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setPreferredSize(new Dimension(240, 0));
        panel.setBackground(PRIMARY_COLOR);

        // Top: "ICTD IMS" and the role name
        JLabel titleLabel = new JLabel("ICTD IMS");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 24));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel roleLabel = new JLabel(roleName);
        roleLabel.setFont(new Font("SansSerif", Font.PLAIN, 13));
        roleLabel.setForeground(new Color(220, 225, 235));
        roleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(PRIMARY_COLOR);
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
        headerPanel.setBorder(new EmptyBorder(25, 22, 25, 22));
        headerPanel.add(titleLabel);
        headerPanel.add(Box.createVerticalStrut(4));
        headerPanel.add(roleLabel);
        panel.add(headerPanel, BorderLayout.NORTH);

        // Middle: menu items (added later by the child class)
        navigationPanel = new JPanel();
        navigationPanel.setBackground(PRIMARY_COLOR);
        navigationPanel.setLayout(new BoxLayout(navigationPanel, BoxLayout.Y_AXIS));
        navigationPanel.setBorder(new EmptyBorder(5, 12, 10, 12));
        panel.add(navigationPanel, BorderLayout.CENTER);

        // Bottom: Profile and Logout
        JButton profileButton = createNavigationButton("Profile");
        profileButton.addActionListener(e -> showProfile());

        JButton logoutButton = createNavigationButton("Logout");
        logoutButton.addActionListener(e -> logout());

        JPanel bottomPanel = new JPanel();
        bottomPanel.setBackground(PRIMARY_COLOR);
        bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.Y_AXIS));
        bottomPanel.setBorder(new EmptyBorder(10, 12, 20, 12));
        bottomPanel.add(profileButton);
        bottomPanel.add(Box.createVerticalStrut(5));
        bottomPanel.add(logoutButton);
        panel.add(bottomPanel, BorderLayout.SOUTH);

        return panel;
    }

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

        // Hover effect: lighter navy while the mouse is on the button
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

    // ==================================================================
    // Methods for child classes
    // ==================================================================

    /** Adds a menu item to the sidebar. The action runs when it is clicked. */
    protected void addNavButton(String text, Runnable action) {
        JButton button = createNavigationButton(text);
        button.addActionListener(e -> action.run());
        navigationPanel.add(button);
    }

    /** Adds a small gray heading in the sidebar (for example "LOGS"). */
    protected void addSectionLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("SansSerif", Font.BOLD, 10));
        label.setForeground(new Color(180, 190, 205));
        label.setBorder(new EmptyBorder(10, 12, 5, 0));
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        navigationPanel.add(label);
    }

    /** Shows a page in the content area (replaces the old page). */
    protected void showPage(JComponent page) {
        contentPanel.removeAll();
        contentPanel.add(page, BorderLayout.CENTER);
        contentPanel.revalidate();
        contentPanel.repaint();
    }

    // ------------------------------------------------------------------
    // Home page builder
    // ------------------------------------------------------------------

    /**
     * Builds a home page: a welcome card first, then the cards you pass in.
     * The page scrolls if the cards do not fit.
     */
    protected JPanel buildHomePage(String message, JComponent... cards) {
        JPanel content = new JPanel();
        content.setBackground(BACKGROUND_COLOR);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));

        // Welcome card
        JLabel welcomeLabel = new JLabel("Welcome, " + userName + "!");
        welcomeLabel.setFont(TITLE_FONT);
        welcomeLabel.setForeground(TEXT_COLOR);
        welcomeLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel dateLabel = new JLabel(new SimpleDateFormat("MMMM dd, yyyy").format(new Date()));
        dateLabel.setFont(BODY_FONT);
        dateLabel.setForeground(MUTED_TEXT_COLOR);
        dateLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel messageLabel = new JLabel("<html>" + message + "</html>");
        messageLabel.setFont(BODY_FONT);
        messageLabel.setForeground(MUTED_TEXT_COLOR);
        messageLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel welcomePanel = createCardPanel();
        welcomePanel.setLayout(new BoxLayout(welcomePanel, BoxLayout.Y_AXIS));
        welcomePanel.add(welcomeLabel);
        welcomePanel.add(Box.createVerticalStrut(5));
        welcomePanel.add(dateLabel);
        welcomePanel.add(Box.createVerticalStrut(8));
        welcomePanel.add(messageLabel);
        welcomePanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 150));
        content.add(welcomePanel);

        // The other cards, one under the other
        for (JComponent card : cards) {
            content.add(Box.createVerticalStrut(15));
            card.setAlignmentX(Component.LEFT_ALIGNMENT);
            content.add(card);
        }

        JScrollPane scrollPane = new JScrollPane(content);
        scrollPane.setBorder(null);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.getViewport().setBackground(BACKGROUND_COLOR);

        JPanel homePanel = new JPanel(new BorderLayout());
        homePanel.setBackground(BACKGROUND_COLOR);
        homePanel.setBorder(new EmptyBorder(25, 30, 25, 30));
        homePanel.add(scrollPane, BorderLayout.CENTER);
        return homePanel;
    }

    // ------------------------------------------------------------------
    // Cards for the home page
    // ------------------------------------------------------------------

    /** A card with a row of number boxes (all start at 0), for example Assigned / Pending. */
    protected JPanel createSummaryCard(String title, String... labels) {
        JPanel row = new JPanel(new GridLayout(1, labels.length, 10, 0));
        row.setBackground(CARD_COLOR);
        for (String label : labels) {
            row.add(createSummaryItem(label, "0"));
        }

        JPanel card = createCardPanel();
        card.setLayout(new BorderLayout(0, 12));
        card.add(createCardTitle(title), BorderLayout.NORTH);
        card.add(row, BorderLayout.CENTER);
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 125));
        return card;
    }

    /** One number box: small title on top, big value below. */
    protected JPanel createSummaryItem(String title, String value) {
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(BODY_FONT);
        titleLabel.setForeground(MUTED_TEXT_COLOR);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("SansSerif", Font.BOLD, 22));
        valueLabel.setForeground(PRIMARY_COLOR);
        valueLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel panel = new JPanel();
        panel.setBackground(new Color(245, 247, 250));
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(new EmptyBorder(12, 15, 12, 15));
        panel.add(titleLabel);
        panel.add(Box.createVerticalStrut(4));
        panel.add(valueLabel);
        return panel;
    }

    /** A card with a title and a short text (for example Announcements). */
    protected JPanel createTextCard(String title, String text) {
        JTextArea textArea = new JTextArea(text);
        textArea.setFont(BODY_FONT);
        textArea.setForeground(MUTED_TEXT_COLOR);
        textArea.setBackground(CARD_COLOR);
        textArea.setEditable(false);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setBorder(new EmptyBorder(5, 0, 0, 0));

        JPanel card = createCardPanel();
        card.setLayout(new BorderLayout(0, 8));
        card.add(createCardTitle(title), BorderLayout.NORTH);
        card.add(textArea, BorderLayout.CENTER);
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));
        return card;
    }

    /** A card with a title and a small table (one "empty" row is shown). */
    protected JPanel createTableCard(String title, String[] columns, String emptyMessage) {
        DefaultTableModel model = new DefaultTableModel(columns, 0);
        JTable table = new JTable(model);
        styleTable(table);

        // One row that says there is nothing yet
        Object[] row = new Object[columns.length];
        java.util.Arrays.fill(row, "-");
        row[0] = emptyMessage;
        model.addRow(row);

        JPanel card = createCardPanel();
        card.setLayout(new BorderLayout(0, 10));
        card.add(createCardTitle(title), BorderLayout.NORTH);
        card.add(new JScrollPane(table), BorderLayout.CENTER);
        card.setPreferredSize(new Dimension(700, 115));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 150));
        return card;
    }

    private JLabel createCardTitle(String text) {
        JLabel label = new JLabel(text);
        label.setFont(CARD_TITLE_FONT);
        label.setForeground(TEXT_COLOR);
        return label;
    }

    // ------------------------------------------------------------------
    // Table page (used by the log and request pages)
    // ------------------------------------------------------------------

    /**
     * Builds a full page with a title, a table, and buttons under it.
     *
     * @param columns the table column names
     * @param widths  column widths (can be null to use the default)
     * @param actions button names. "Add..." opens a form (not connected yet),
     *                "Delete" removes the selected row, any other name needs a
     *                selected row. Pass none for a view-only page.
     */
    protected JPanel createTablePage(String title, String[] columns, int[] widths,
                                     String... actions) {
        // The cells cannot be edited by typing
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable table = new JTable(model);
        styleTable(table);
        if (widths != null) {
            setColumnWidths(table, widths);
        }

        JPanel panel = new JPanel(new BorderLayout(0, 15));
        panel.setBackground(BACKGROUND_COLOR);
        panel.setBorder(new EmptyBorder(25, 30, 25, 30));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(TITLE_FONT);
        titleLabel.setForeground(TEXT_COLOR);

        panel.add(titleLabel, BorderLayout.NORTH);
        panel.add(createTableScrollPane(table), BorderLayout.CENTER);
        if (actions.length > 0) {
            panel.add(createActionButtons(model, table, actions), BorderLayout.SOUTH);
        }
        return panel;
    }

    /** The row of buttons under a table. */
    private JPanel createActionButtons(DefaultTableModel model, JTable table, String[] actions) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        panel.setBackground(BACKGROUND_COLOR);

        for (String action : actions) {
            JButton button = new JButton(action);
            button.addActionListener(e -> handleTableAction(action, model, table));
            panel.add(button);
        }
        return panel;
    }

    /** Runs when a button under a table is clicked. */
    private void handleTableAction(String action, DefaultTableModel model, JTable table) {
        // "Add..." buttons do not need a selected row
        if (action.startsWith("Add")) {
            showInfo(action + " form will be connected here.");
            return;
        }

        // All other buttons work on the selected row
        int row = table.getSelectedRow();
        if (row == -1) {
            showWarning("Please select a row in the table first.");
            return;
        }

        if (action.equals("Delete")) {
            int result = JOptionPane.showConfirmDialog(this, "Delete the selected row?",
                    "Confirm Delete", JOptionPane.YES_NO_OPTION);
            if (result == JOptionPane.YES_OPTION) {
                model.removeRow(table.convertRowIndexToModel(row));
            }
        } else {
            showInfo(action + " will be connected here.");
        }
    }

    // ------------------------------------------------------------------
    // Standard pieces
    // ------------------------------------------------------------------

    /** A white card with a thin border. */
    protected JPanel createCardPanel() {
        JPanel panel = new JPanel();
        panel.setBackground(CARD_COLOR);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR),
                new EmptyBorder(16, 18, 16, 18)));
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        return panel;
    }

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

    private void setColumnWidths(JTable table, int[] widths) {
        for (int i = 0; i < widths.length && i < table.getColumnModel().getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
            table.getColumnModel().getColumn(i).setMinWidth(80);
        }
    }

    /** A scroll pane that scrolls sideways when the table is wide. */
    private JScrollPane createTableScrollPane(JTable table) {
        JScrollPane scrollPane = new JScrollPane(table,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
        scrollPane.getViewport().setBackground(Color.WHITE);
        scrollPane.getHorizontalScrollBar().setUnitIncrement(16);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        return scrollPane;
    }

    // ------------------------------------------------------------------
    // Profile, Logout, messages
    // ------------------------------------------------------------------
    private void showProfile() {
        showInfo(roleName + " Profile\n\nName: " + userName);
    }

    /** Logout: ask first, then show the existing login page again. */
    private void logout() {
        int result = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to logout?", "Logout", JOptionPane.YES_NO_OPTION);

        if (result == JOptionPane.YES_OPTION) {
            if (loginPage != null) {
                loginPage.showLoginPage();
            }
            dispose();
        }
    }

    private void showInfo(String message) {
        JOptionPane.showMessageDialog(this, message, "ICTD IMS", JOptionPane.INFORMATION_MESSAGE);
    }

    private void showWarning(String message) {
        JOptionPane.showMessageDialog(this, message, "ICTD IMS", JOptionPane.WARNING_MESSAGE);
    }
}
/*
 * ============================================================================
 *  ICTD IMS - ADMIN DASHBOARD
 * ============================================================================
 *  What this program does:
 *    The window the Administrator sees after logging in. It is used to
 *    create, view, delete, and reset the password of user accounts.
 *
 *  Pages (chosen from the sidebar):
 *    Dashboard       - number of accounts per role
 *    Create Account  - add a Secretary, Technician, or READS Scholar
 *    Manage Accounts - table of accounts, with Reset Password and Delete
 *    Logout          - closes this window and shows the login page again
 *
 *  How it connects to the other files:
 *    LoginPage      - opens this window with new AdminDashboard(this) and
 *                     is shown again on Logout (no second login page).
 *    AccountStorage - saves and reads the accounts (file + hash table).
 *    LoginLogic     - decides who is the Administrator. It is used by
 *                     LoginPage, so this window does not call it.
 *
 *  Passwords are never shown here. All the checking of names, usernames,
 *  and passwords is done in AccountStorage.
 * ============================================================================
 */
package frontend.dashboards;

import frontend.auth.LoginPage;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableModel;

import backend.auth.AccountStorage;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.io.IOException;
import java.util.Arrays;

public class AdminDashboard extends JFrame {

    // ------------------------------------------------------------------
    // Colors
    // ------------------------------------------------------------------
    private static final Color PRIMARY_COLOR = new Color(24, 54, 99);
    private static final Color PRIMARY_HOVER = new Color(36, 76, 135);
    private static final Color DANGER_COLOR = new Color(176, 42, 48);
    private static final Color DANGER_HOVER = new Color(200, 60, 66);
    private static final Color SIDEBAR_COLOR = new Color(16, 38, 72);
    private static final Color SIDEBAR_ACTIVE_COLOR = new Color(36, 76, 135);
    private static final Color BACKGROUND_COLOR = new Color(236, 240, 246);
    private static final Color CARD_COLOR = Color.WHITE;
    private static final Color BORDER_COLOR = new Color(206, 212, 222);
    private static final Color TEXT_COLOR = new Color(33, 37, 41);
    private static final Color MUTED_TEXT_COLOR = new Color(95, 105, 120);
    private static final Color HEADER_SUBTEXT_COLOR = new Color(200, 214, 235);

    // ------------------------------------------------------------------
    // Fonts
    // ------------------------------------------------------------------
    private static final Font TITLE_FONT = new Font("SansSerif", Font.BOLD, 22);
    private static final Font SECTION_FONT = new Font("SansSerif", Font.BOLD, 18);
    private static final Font NORMAL_FONT = new Font("SansSerif", Font.PLAIN, 13);
    private static final Font LABEL_FONT = new Font("SansSerif", Font.BOLD, 13);
    private static final Font FIELD_FONT = new Font("SansSerif", Font.PLAIN, 14);
    private static final Font NAV_FONT = new Font("SansSerif", Font.BOLD, 14);
    private static final Font BUTTON_FONT = new Font("SansSerif", Font.BOLD, 14);

    // Names of the three pages
    private static final String PAGE_DASHBOARD = "DASHBOARD";
    private static final String PAGE_CREATE = "CREATE_ACCOUNT";
    private static final String PAGE_MANAGE = "MANAGE_ACCOUNTS";

    // ------------------------------------------------------------------
    // Connections to the rest of the program
    // ------------------------------------------------------------------
    private final LoginPage loginPage;            // shown again on Logout
    private final AccountStorage accountStorage;  // saves and reads accounts

    // ------------------------------------------------------------------
    // Components
    // ------------------------------------------------------------------
    // Layout and sidebar
    private CardLayout pageLayout;
    private JPanel pagePanel;
    private JButton dashboardNavButton;
    private JButton createNavButton;
    private JButton manageNavButton;
    private JButton logoutButton;
    private JButton[] pageNavButtons;

    // Dashboard page
    private JLabel totalValueLabel;
    private JLabel secretaryValueLabel;
    private JLabel technicianValueLabel;
    private JLabel readsScholarValueLabel;
    private JLabel officeHeadValueLabel;

    // Create Account page
    private JTextField fullNameField;
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JPasswordField confirmPasswordField;
    private JComboBox<String> roleComboBox;
    private JButton createAccountButton;

    // Manage Accounts page
    private DefaultTableModel tableModel;
    private JTable accountTable;
    private JButton resetPasswordButton;
    private JButton deleteAccountButton;

    /**
     * Builds the dashboard.
     *
     * @param loginPage the existing login page (shown again on Logout)
     * @throws IOException if the accounts file cannot be created or read
     */
    public AdminDashboard(LoginPage loginPage) throws IOException {
        this.loginPage = loginPage;
        this.accountStorage = new AccountStorage(); // loads the saved accounts

        configureFrame();
        buildLayout();
        registerEvents();

        refreshAll();
        showPage(PAGE_DASHBOARD, dashboardNavButton);
    }

    // ==================================================================
    // Window settings
    // ==================================================================
    private void configureFrame() {
        setTitle("ICTD IMS - Admin Dashboard");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // closing the window stops the program
        setMinimumSize(new Dimension(1000, 640));
        setSize(1180, 720);
        setLocationRelativeTo(null);
    }

    // ==================================================================
    // Layout: header (top), sidebar (left), main area (center)
    // ==================================================================
    private void buildLayout() {
        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(buildHeader(), BorderLayout.NORTH);
        getContentPane().add(buildSidebar(), BorderLayout.WEST);
        getContentPane().add(buildMainArea(), BorderLayout.CENTER);
    }

    /** Navy bar with "ICTD IMS" and the full system name. */
    private JPanel buildHeader() {
        JLabel abbreviationLabel = new JLabel("ICTD IMS");
        abbreviationLabel.setFont(new Font("SansSerif", Font.BOLD, 24));
        abbreviationLabel.setForeground(Color.WHITE);

        JLabel systemNameLabel = new JLabel(
                "Information and Communication Technology Department Information Management System");
        systemNameLabel.setFont(NORMAL_FONT);
        systemNameLabel.setForeground(HEADER_SUBTEXT_COLOR);

        JPanel textPanel = new JPanel();
        textPanel.setOpaque(false);
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.add(abbreviationLabel);
        textPanel.add(Box.createVerticalStrut(2));
        textPanel.add(systemNameLabel);

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(PRIMARY_COLOR);
        header.setBorder(BorderFactory.createEmptyBorder(14, 24, 14, 24));
        header.add(textPanel, BorderLayout.WEST);
        return header;
    }

    /** Left menu: Dashboard, Create Account, Manage Accounts, Logout. */
    private JPanel buildSidebar() {
        dashboardNavButton = createNavButton("Dashboard");
        createNavButton = createNavButton("Create Account");
        manageNavButton = createNavButton("Manage Accounts");
        logoutButton = createNavButton("Logout");

        // These three switch pages. Logout is handled separately.
        pageNavButtons = new JButton[]{dashboardNavButton, createNavButton, manageNavButton};

        JPanel navGroup = new JPanel();
        navGroup.setBackground(SIDEBAR_COLOR);
        navGroup.setLayout(new BoxLayout(navGroup, BoxLayout.Y_AXIS));
        navGroup.setBorder(BorderFactory.createEmptyBorder(16, 0, 0, 0));
        navGroup.add(dashboardNavButton);
        navGroup.add(createNavButton);
        navGroup.add(manageNavButton);

        // Logout stays at the bottom, with a thin line above it
        JPanel logoutGroup = new JPanel(new BorderLayout());
        logoutGroup.setBackground(SIDEBAR_COLOR);
        logoutGroup.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(60, 90, 140)));
        logoutGroup.add(logoutButton, BorderLayout.CENTER);

        JPanel sidebar = new JPanel(new BorderLayout());
        sidebar.setBackground(SIDEBAR_COLOR);
        sidebar.setPreferredSize(new Dimension(230, 0));
        sidebar.add(navGroup, BorderLayout.NORTH);
        sidebar.add(logoutGroup, BorderLayout.SOUTH);
        return sidebar;
    }

    /** One sidebar button. */
    private JButton createNavButton(String text) {
        JButton button = new JButton(text);
        button.setFont(NAV_FONT);
        button.setForeground(Color.WHITE);
        button.setBackground(SIDEBAR_COLOR);
        button.setOpaque(true);
        button.setContentAreaFilled(true);
        button.setFocusPainted(false);
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setBorder(BorderFactory.createEmptyBorder(0, 24, 0, 24));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setPreferredSize(new Dimension(230, 48));
        button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 48));
        return button;
    }

    /** Title bar on top, and the three pages below it (one shown at a time). */
    private JPanel buildMainArea() {
        JLabel pageTitleLabel = new JLabel("Admin Dashboard");
        pageTitleLabel.setFont(TITLE_FONT);
        pageTitleLabel.setForeground(PRIMARY_COLOR);

        JLabel welcomeLabel = new JLabel("Welcome, Administrator");
        welcomeLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));
        welcomeLabel.setForeground(MUTED_TEXT_COLOR);

        JPanel titleBar = new JPanel(new BorderLayout());
        titleBar.setBackground(CARD_COLOR);
        titleBar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_COLOR),
                BorderFactory.createEmptyBorder(16, 28, 16, 28)));
        titleBar.add(pageTitleLabel, BorderLayout.WEST);
        titleBar.add(welcomeLabel, BorderLayout.EAST);

        // CardLayout shows only one page at a time
        pageLayout = new CardLayout();
        pagePanel = new JPanel(pageLayout);
        pagePanel.setBackground(BACKGROUND_COLOR);
        pagePanel.setBorder(BorderFactory.createEmptyBorder(24, 28, 24, 28));
        pagePanel.add(buildDashboardPage(), PAGE_DASHBOARD);
        pagePanel.add(buildCreatePage(), PAGE_CREATE);
        pagePanel.add(buildManagePage(), PAGE_MANAGE);

        JPanel mainArea = new JPanel(new BorderLayout());
        mainArea.setBackground(BACKGROUND_COLOR);
        mainArea.add(titleBar, BorderLayout.NORTH);
        mainArea.add(pagePanel, BorderLayout.CENTER);
        return mainArea;
    }

    // ------------------------------------------------------------------
    // Page 1: Dashboard (account counts)
    // ------------------------------------------------------------------
    private JPanel buildDashboardPage() {
        totalValueLabel = createValueLabel();
        secretaryValueLabel = createValueLabel();
        technicianValueLabel = createValueLabel();
        readsScholarValueLabel = createValueLabel();
        officeHeadValueLabel = createValueLabel();

        JPanel summaryRow = new JPanel(new GridLayout(1, 5, 16, 0));
        summaryRow.setOpaque(false);
        summaryRow.add(createSummaryCard("Total Accounts", totalValueLabel));
        summaryRow.add(createSummaryCard(AccountStorage.ROLE_SECRETARY, secretaryValueLabel));
        summaryRow.add(createSummaryCard(AccountStorage.ROLE_TECHNICIAN, technicianValueLabel));
        summaryRow.add(createSummaryCard(AccountStorage.ROLE_READS_SCHOLAR, readsScholarValueLabel));
        summaryRow.add(createSummaryCard(AccountStorage.ROLE_OFFICE_HEAD, officeHeadValueLabel));

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.add(summaryRow, BorderLayout.NORTH);

        JPanel page = new JPanel(new BorderLayout(0, 20));
        page.setOpaque(false);
        page.add(createHeading("Dashboard",
                "Number of accounts in the system."), BorderLayout.NORTH);
        page.add(wrapper, BorderLayout.CENTER);
        return page;
    }

    private JLabel createValueLabel() {
        JLabel label = new JLabel("0");
        label.setFont(new Font("SansSerif", Font.BOLD, 30));
        label.setForeground(PRIMARY_COLOR);
        return label;
    }

    private JPanel createSummaryCard(String title, JLabel valueLabel) {
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(NORMAL_FONT);
        titleLabel.setForeground(MUTED_TEXT_COLOR);

        JPanel card = new JPanel(new BorderLayout(0, 4));
        card.setBackground(CARD_COLOR);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR),
                BorderFactory.createEmptyBorder(16, 20, 16, 20)));
        card.add(titleLabel, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);
        return card;
    }

    // ------------------------------------------------------------------
    // Page 2: Create Account
    // ------------------------------------------------------------------
    private JPanel buildCreatePage() {
        fullNameField = createTextField();
        usernameField = createTextField();
        passwordField = new JPasswordField();
        styleInputField(passwordField);
        confirmPasswordField = new JPasswordField();
        styleInputField(confirmPasswordField);

        roleComboBox = new JComboBox<>(AccountStorage.ROLES);
        roleComboBox.setFont(FIELD_FONT);
        roleComboBox.setBackground(Color.WHITE);
        roleComboBox.setPreferredSize(new Dimension(320, 38));

        createAccountButton = new JButton("Create Account");
        styleButton(createAccountButton, PRIMARY_COLOR, PRIMARY_HOVER, 200);

        // The form: two columns
        JPanel formCard = new JPanel(new GridBagLayout());
        formCard.setBackground(CARD_COLOR);
        formCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR),
                BorderFactory.createEmptyBorder(24, 28, 24, 28)));

        addFormField(formCard, "Full Name", fullNameField, 0, 0, 2);
        addFormField(formCard, "Username", usernameField, 0, 1, 1);
        addFormField(formCard, "Role", roleComboBox, 1, 1, 1);
        addFormField(formCard, "Password", passwordField, 0, 2, 1);
        addFormField(formCard, "Confirm Password", confirmPasswordField, 1, 2, 1);

        // Button on the right side, under the fields
        GridBagConstraints buttonConstraints = new GridBagConstraints();
        buttonConstraints.gridx = 0;
        buttonConstraints.gridy = 6;
        buttonConstraints.gridwidth = 2;
        buttonConstraints.anchor = GridBagConstraints.EAST;
        formCard.add(createAccountButton, buttonConstraints);

        // Keep the card at its normal size, on the left
        JPanel formWrapper = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        formWrapper.setOpaque(false);
        formWrapper.add(formCard);

        JPanel page = new JPanel(new BorderLayout(0, 20));
        page.setOpaque(false);
        page.add(createHeading("Create Account",
                "Add a new Secretary, Technician, READS Scholar, or Office Head account."), BorderLayout.NORTH);
        page.add(formWrapper, BorderLayout.CENTER);
        return page;
    }

    private JTextField createTextField() {
        JTextField field = new JTextField();
        styleInputField(field);
        return field;
    }

    /** Same look for text and password fields. */
    private void styleInputField(JTextField field) {
        field.setFont(FIELD_FONT);
        field.setForeground(TEXT_COLOR);
        field.setPreferredSize(new Dimension(320, 38));
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR),
                BorderFactory.createEmptyBorder(4, 10, 4, 10)));
    }

    /** Colored button with a hover effect. */
    private void styleButton(JButton button, Color normalColor, Color hoverColor, int width) {
        button.setFont(BUTTON_FONT);
        button.setForeground(Color.WHITE);
        button.setBackground(normalColor);
        button.setOpaque(true);
        button.setContentAreaFilled(true);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setPreferredSize(new Dimension(width, 40));

        button.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                button.setBackground(hoverColor);
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                button.setBackground(normalColor);
            }
        });
    }

    /**
     * Adds a label and its field to the form.
     * column: 0 = left, 1 = right. row: 0, 1, 2...
     * gridWidth: 1 = one column wide, 2 = both columns.
     */
    private void addFormField(JPanel form, String labelText, JComponent field,
                              int column, int row, int gridWidth) {
        JLabel label = new JLabel(labelText);
        label.setFont(LABEL_FONT);
        label.setForeground(TEXT_COLOR);

        int leftGap = (column == 1) ? 10 : 0;
        int rightGap = (column == 0 && gridWidth == 1) ? 10 : 0;
        double weight = (gridWidth == 2) ? 1.0 : 0.5;

        // The label takes one grid row, the field takes the next one
        GridBagConstraints labelConstraints = new GridBagConstraints();
        labelConstraints.gridx = column;
        labelConstraints.gridy = row * 2;
        labelConstraints.gridwidth = gridWidth;
        labelConstraints.weightx = weight;
        labelConstraints.fill = GridBagConstraints.HORIZONTAL;
        labelConstraints.insets = new Insets(0, leftGap, 6, rightGap);
        form.add(label, labelConstraints);

        GridBagConstraints fieldConstraints = new GridBagConstraints();
        fieldConstraints.gridx = column;
        fieldConstraints.gridy = row * 2 + 1;
        fieldConstraints.gridwidth = gridWidth;
        fieldConstraints.weightx = weight;
        fieldConstraints.fill = GridBagConstraints.HORIZONTAL;
        fieldConstraints.insets = new Insets(0, leftGap, 18, rightGap);
        form.add(field, fieldConstraints);
    }

    // ------------------------------------------------------------------
    // Page 3: Manage Accounts
    // ------------------------------------------------------------------
    private JPanel buildManagePage() {
        // No password column
        String[] columnNames = {"Username", "Full Name", "Role"};

        // Cells cannot be edited by typing
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        accountTable = new JTable(tableModel);
        accountTable.setFont(FIELD_FONT);
        accountTable.setForeground(TEXT_COLOR);
        accountTable.setRowHeight(32);
        accountTable.setGridColor(new Color(228, 232, 238));
        accountTable.setSelectionBackground(new Color(214, 227, 245));
        accountTable.setSelectionForeground(TEXT_COLOR);
        accountTable.setFillsViewportHeight(true);
        accountTable.setAutoCreateRowSorter(true); // click a header to sort
        accountTable.getTableHeader().setReorderingAllowed(false);
        accountTable.getTableHeader().setFont(LABEL_FONT);
        accountTable.getTableHeader().setBackground(PRIMARY_COLOR);
        accountTable.getTableHeader().setForeground(Color.WHITE);
        accountTable.getTableHeader().setPreferredSize(new Dimension(0, 36));

        JScrollPane scrollPane = new JScrollPane(accountTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
        scrollPane.getViewport().setBackground(CARD_COLOR);

        // Buttons under the table
        resetPasswordButton = new JButton("Reset Password");
        styleButton(resetPasswordButton, PRIMARY_COLOR, PRIMARY_HOVER, 170);
        deleteAccountButton = new JButton("Delete Account");
        styleButton(deleteAccountButton, DANGER_COLOR, DANGER_HOVER, 170);

        JPanel buttonRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonRow.setOpaque(false);
        buttonRow.add(resetPasswordButton);
        buttonRow.add(deleteAccountButton);

        JPanel center = new JPanel(new BorderLayout(0, 12));
        center.setOpaque(false);
        center.add(scrollPane, BorderLayout.CENTER);
        center.add(buttonRow, BorderLayout.SOUTH);

        JPanel page = new JPanel(new BorderLayout(0, 20));
        page.setOpaque(false);
        page.add(createHeading("Manage Accounts",
                "Select an account in the table, then reset its password or delete it."),
                BorderLayout.NORTH);
        page.add(center, BorderLayout.CENTER);
        return page;
    }

    /** Page title with a small description under it. */
    private JPanel createHeading(String title, String description) {
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(SECTION_FONT);
        titleLabel.setForeground(TEXT_COLOR);

        JLabel descriptionLabel = new JLabel(description);
        descriptionLabel.setFont(NORMAL_FONT);
        descriptionLabel.setForeground(MUTED_TEXT_COLOR);

        JPanel heading = new JPanel();
        heading.setOpaque(false);
        heading.setLayout(new BoxLayout(heading, BoxLayout.Y_AXIS));
        heading.add(titleLabel);
        heading.add(Box.createVerticalStrut(4));
        heading.add(descriptionLabel);
        return heading;
    }

    // ==================================================================
    // Events
    // ==================================================================
    private void registerEvents() {
        // Sidebar
        dashboardNavButton.addActionListener(e -> showPage(PAGE_DASHBOARD, dashboardNavButton));
        createNavButton.addActionListener(e -> showPage(PAGE_CREATE, createNavButton));
        manageNavButton.addActionListener(e -> showPage(PAGE_MANAGE, manageNavButton));
        logoutButton.addActionListener(e -> handleLogout());

        // Create Account (Enter in the last field also creates)
        createAccountButton.addActionListener(e -> handleCreateAccount());
        confirmPasswordField.addActionListener(e -> handleCreateAccount());

        // Manage Accounts
        resetPasswordButton.addActionListener(e -> handleResetPassword());
        deleteAccountButton.addActionListener(e -> handleDeleteAccount());
    }

    /** Shows one page and highlights its sidebar button. */
    private void showPage(String pageName, JButton selectedButton) {
        pageLayout.show(pagePanel, pageName);

        for (JButton button : pageNavButtons) {
            button.setBackground(button == selectedButton ? SIDEBAR_ACTIVE_COLOR : SIDEBAR_COLOR);
        }
    }

    /** Logout: show the existing login page again, then close this window. */
    private void handleLogout() {
        loginPage.showLoginPage();
        dispose();
    }

    // ==================================================================
    // Create Account
    // ==================================================================
    private void handleCreateAccount() {
        String fullName = fullNameField.getText().trim();
        String username = usernameField.getText().trim();
        char[] password = passwordField.getPassword();
        char[] confirmPassword = confirmPasswordField.getPassword();
        String role = (String) roleComboBox.getSelectedItem();

        try {
            // Quick checks here. AccountStorage does the rest
            // (spaces, reserved name, duplicate username, password length, role).
            if (fullName.isEmpty()) {
                showWarning("Full Name is required.");
                return;
            }
            if (username.isEmpty()) {
                showWarning("Username is required.");
                return;
            }
            if (password.length == 0) {
                showWarning("Password is required.");
                return;
            }
            if (confirmPassword.length == 0) {
                showWarning("Please confirm the password.");
                return;
            }
            if (!Arrays.equals(password, confirmPassword)) {
                showWarning("Password and Confirm Password do not match.");
                return;
            }

            // Save the account
            try {
                accountStorage.addAccount(fullName, username, password, role);
            } catch (IllegalArgumentException ex) {
                showWarning(ex.getMessage()); // for example: username already taken
                return;
            } catch (IOException ex) {
                showError("The account could not be saved.\n" + ex.getMessage());
                return;
            }

            // Success
            clearCreateForm();
            refreshAll();
            JOptionPane.showMessageDialog(this, "Account created successfully.",
                    "Success", JOptionPane.INFORMATION_MESSAGE);

        } finally {
            // Always erase the passwords from memory
            Arrays.fill(password, '\0');
            Arrays.fill(confirmPassword, '\0');
        }
    }

    private void clearCreateForm() {
        fullNameField.setText("");
        usernameField.setText("");
        passwordField.setText("");
        confirmPasswordField.setText("");
        roleComboBox.setSelectedIndex(0);
        fullNameField.requestFocusInWindow();
    }

    // ==================================================================
    // Manage Accounts
    // ==================================================================

    /** Returns the username of the selected table row, or null if none is selected. */
    private String getSelectedUsername() {
        int viewRow = accountTable.getSelectedRow();
        if (viewRow == -1) {
            return null;
        }
        // The table can be sorted, so convert to the real row number
        int modelRow = accountTable.convertRowIndexToModel(viewRow);
        return (String) tableModel.getValueAt(modelRow, 0);
    }

    private void handleDeleteAccount() {
        String username = getSelectedUsername();
        if (username == null) {
            showWarning("Please select an account in the table first.");
            return;
        }

        int choice = JOptionPane.showConfirmDialog(this,
                "Delete the account \"" + username + "\"?\nThis cannot be undone.",
                "Delete Account", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (choice != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            if (accountStorage.deleteAccount(username)) {
                refreshAll();
                JOptionPane.showMessageDialog(this, "Account deleted.",
                        "Success", JOptionPane.INFORMATION_MESSAGE);
            } else {
                showWarning("That account no longer exists.");
                refreshAll();
            }
        } catch (IOException ex) {
            showError("The account could not be deleted.\n" + ex.getMessage());
        }
    }

    private void handleResetPassword() {
        String username = getSelectedUsername();
        if (username == null) {
            showWarning("Please select an account in the table first.");
            return;
        }

        // A small dialog with two password boxes
        JPasswordField newPasswordField = new JPasswordField(20);
        JPasswordField confirmField = new JPasswordField(20);
        Object[] dialogContent = {
                "New password for \"" + username + "\":", newPasswordField,
                "Confirm new password:", confirmField
        };
        int choice = JOptionPane.showConfirmDialog(this, dialogContent, "Reset Password",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        char[] newPassword = newPasswordField.getPassword();
        char[] confirmPassword = confirmField.getPassword();

        try {
            if (choice != JOptionPane.OK_OPTION) {
                return; // cancelled
            }
            if (!Arrays.equals(newPassword, confirmPassword)) {
                showWarning("The passwords do not match.");
                return;
            }

            try {
                if (accountStorage.resetPassword(username, newPassword)) {
                    JOptionPane.showMessageDialog(this, "Password reset successfully.",
                            "Success", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    showWarning("That account no longer exists.");
                    refreshAll();
                }
            } catch (IllegalArgumentException ex) {
                showWarning(ex.getMessage()); // for example: password too short
            } catch (IOException ex) {
                showError("The password could not be saved.\n" + ex.getMessage());
            }

        } finally {
            Arrays.fill(newPassword, '\0');
            Arrays.fill(confirmPassword, '\0');
        }
    }

    // ==================================================================
    // Update the screen
    // ==================================================================

    /** Reloads the table and the counts from the stored accounts. */
    private void refreshAll() {
        tableModel.setRowCount(0); // remove old rows

        int secretaryCount = 0;
        int technicianCount = 0;
        int readsScholarCount = 0;
        int officeHeadCount = 0;

        // Passwords are never added to the table
        for (AccountStorage.Account account : accountStorage.getAllAccounts()) {
            tableModel.addRow(new Object[]{
                    account.getUsername(), account.getFullName(), account.getRole()});

            String role = account.getRole();
            if (role.equals(AccountStorage.ROLE_SECRETARY)) {
                secretaryCount++;
            } else if (role.equals(AccountStorage.ROLE_TECHNICIAN)) {
                technicianCount++;
            } else if (role.equals(AccountStorage.ROLE_READS_SCHOLAR)) {
                readsScholarCount++;
            } else if (role.equals(AccountStorage.ROLE_OFFICE_HEAD)) {
                officeHeadCount++;
            }
        }

        totalValueLabel.setText(String.valueOf(tableModel.getRowCount()));
        secretaryValueLabel.setText(String.valueOf(secretaryCount));
        technicianValueLabel.setText(String.valueOf(technicianCount));
        readsScholarValueLabel.setText(String.valueOf(readsScholarCount));
        officeHeadValueLabel.setText(String.valueOf(officeHeadCount));
    }

    // ==================================================================
    // Message helpers
    // ==================================================================
    private void showWarning(String message) {
        JOptionPane.showMessageDialog(this, message, "Notice", JOptionPane.WARNING_MESSAGE);
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }
}
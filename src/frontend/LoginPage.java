/*
 * ============================================================================
 *  ICTD IMS - LOGIN PAGE
 * ============================================================================
 *  What this program does:
 *    Shows the login window of the ICTD Information Management System,
 *    built with Java Swing. After a correct login it opens the dashboard
 *    that matches the account:
 *      Administrator -> AdminDashboard
 *      Secretary     -> SecretaryDashboard
 *      Technician    -> TechnicianDashboard
 *      READS Scholar -> READSDashboard
 *      Office Head   -> OfficeHeadDashboard
 * ============================================================================
 */

package frontend;

import backend.AccountStorage;
import backend.LoginLogic;
import users.AdminDashboard;
import users.OfficeHeadDashboard;
import users.READSDashboard;
import users.SecretaryDashboard;
import users.TechnicianDashboard;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.IOException;
import java.util.Arrays;

public class LoginPage extends JFrame {

    // ------------------------------------------------------------------
    // Colors
    // ------------------------------------------------------------------
    private static final Color PRIMARY_COLOR = new Color(24, 54, 99);       // navy blue
    private static final Color PRIMARY_HOVER = new Color(36, 76, 135);      // lighter navy (mouse over)
    private static final Color BACKGROUND_COLOR = new Color(236, 240, 246); // light gray-blue
    private static final Color CARD_COLOR = Color.WHITE;
    private static final Color BORDER_COLOR = new Color(206, 212, 222);
    private static final Color TEXT_COLOR = new Color(33, 37, 41);
    private static final Color MUTED_TEXT_COLOR = new Color(95, 105, 120);  // softer gray text

    // ------------------------------------------------------------------
    // Fonts
    // ------------------------------------------------------------------
    private static final Font ABBREVIATION_FONT = new Font("SansSerif", Font.BOLD, 30);
    private static final Font SYSTEM_NAME_FONT = new Font("SansSerif", Font.PLAIN, 13);
    private static final Font HEADING_FONT = new Font("SansSerif", Font.BOLD, 17);
    private static final Font LABEL_FONT = new Font("SansSerif", Font.BOLD, 13);
    private static final Font FIELD_FONT = new Font("SansSerif", Font.PLAIN, 14);
    private static final Font BUTTON_FONT = new Font("SansSerif", Font.BOLD, 15);

    // ------------------------------------------------------------------
    // Components (created in initializeComponents)
    // ------------------------------------------------------------------
    private JPanel backgroundPanel;
    private JPanel cardPanel;
    private JLabel abbreviationLabel;
    private JLabel systemNameLabel;
    private JLabel headingLabel;
    private JLabel usernameLabel;
    private JLabel passwordLabel;
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JPanel passwordPanel;
    private JButton showPasswordButton;
    private char defaultEchoChar;
    private JButton loginButton;

    // The part of the program that checks the login (no windows inside it)
    private final LoginLogic loginLogic = new LoginLogic();

    /**
     * Builds the login window. The steps run in this order:
     * create components, place them, set up the window, add events.
     */
    public LoginPage() {
        initializeComponents();
        buildLayout();
        configureFrame();
        registerEvents();
    }

    // ==================================================================
    // STEP 1: Create the components
    // (They are only created here. They are placed in buildLayout.)
    // ==================================================================
    private void initializeComponents() {
        // "ICTD IMS" title
        abbreviationLabel = new JLabel("ICTD IMS", SwingConstants.CENTER);
        abbreviationLabel.setFont(ABBREVIATION_FONT);
        abbreviationLabel.setForeground(PRIMARY_COLOR);

        // Full system name (HTML is used so the long text wraps inside the card)
        systemNameLabel = new JLabel(
                "<html><div style='text-align:center; width:340px;'>"
                        + "Information and Communication Technology Department "
                        + "Information Management System"
                        + "</div></html>",
                SwingConstants.CENTER);
        systemNameLabel.setFont(SYSTEM_NAME_FONT);
        systemNameLabel.setForeground(MUTED_TEXT_COLOR);

        // Heading above the form
        headingLabel = new JLabel("Sign in your ICTD IMS account", SwingConstants.CENTER);
        headingLabel.setFont(HEADING_FONT);
        headingLabel.setForeground(TEXT_COLOR);

        // Small labels above the input boxes
        usernameLabel = new JLabel("Username");
        usernameLabel.setFont(LABEL_FONT);
        usernameLabel.setForeground(TEXT_COLOR);

        passwordLabel = new JLabel("Password");
        passwordLabel.setFont(LABEL_FONT);
        passwordLabel.setForeground(TEXT_COLOR);

        // Username box
        usernameField = new JTextField();
        styleInputField(usernameField);

        // Password box (shows dots instead of the real characters)
        passwordField = new JPasswordField();
        styleInputField(passwordField);
        // Save the dot symbol so we can bring it back when hiding the password
        defaultEchoChar = passwordField.getEchoChar();

        // Show/Hide button and the box that holds it together with the password field
        showPasswordButton = new JButton("Show");
        passwordPanel = new JPanel(new BorderLayout());
        stylePasswordPanel();

        // Login button
        loginButton = new JButton("Login");
        styleLoginButton(loginButton);
    }

    /** Gives the username and password fields the same look. */
    private void styleInputField(JTextField field) {
        field.setFont(FIELD_FONT);
        field.setForeground(TEXT_COLOR);
        field.setPreferredSize(new Dimension(340, 38)); // width, height
        // Gray outline + empty space inside, so the text does not touch the edge
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR),
                BorderFactory.createEmptyBorder(4, 10, 4, 10)));
    }

    /**
     * Builds the password box: password field on the left, Show/Hide button
     * on the right, both inside one border so they look like one box.
     */
    private void stylePasswordPanel() {
        // The outer panel draws the border, so the field only gets spacing
        // (this avoids a double border).
        passwordField.setBorder(BorderFactory.createEmptyBorder(4, 10, 4, 4));

        passwordPanel.setBackground(Color.WHITE);
        passwordPanel.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
        passwordPanel.setPreferredSize(new Dimension(340, 38));

        // Plain navy text button
        showPasswordButton.setFont(new Font("SansSerif", Font.BOLD, 12));
        showPasswordButton.setForeground(PRIMARY_COLOR);
        showPasswordButton.setBackground(Color.WHITE);
        showPasswordButton.setOpaque(true);
        showPasswordButton.setContentAreaFilled(false);
        showPasswordButton.setBorderPainted(false);
        showPasswordButton.setFocusPainted(false);
        // Not focusable: clicking it keeps the cursor in the password box,
        // and pressing Enter still triggers Login.
        showPasswordButton.setFocusable(false);
        showPasswordButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        showPasswordButton.setPreferredSize(new Dimension(64, 36));

        passwordPanel.add(passwordField, BorderLayout.CENTER);
        passwordPanel.add(showPasswordButton, BorderLayout.EAST);
    }

    /**
     * Shows or hides the password.
     * Hidden  = typed characters appear as dots.
     * Visible = typed characters appear as normal text.
     * The button text shows what the next click will do.
     */
    private void togglePasswordVisibility() {
        if (passwordField.getEchoChar() == (char) 0) {
            // Password is visible, so hide it
            passwordField.setEchoChar(defaultEchoChar);
            showPasswordButton.setText("Show");
        } else {
            // Password is hidden, so show it
            passwordField.setEchoChar((char) 0);
            showPasswordButton.setText("Hide");
        }
    }

    /** Gives the Login button its navy color, size, and hover effect. */
    private void styleLoginButton(JButton button) {
        button.setFont(BUTTON_FONT);
        button.setForeground(Color.WHITE);
        button.setBackground(PRIMARY_COLOR);
        // These lines make sure the navy color is really shown
        button.setOpaque(true);
        button.setContentAreaFilled(true);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)); // hand cursor
        button.setPreferredSize(new Dimension(340, 42));

        // Hover effect: lighter navy when the mouse is on the button
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                button.setBackground(PRIMARY_HOVER);
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                button.setBackground(PRIMARY_COLOR);
            }
        });
    }

    // ==================================================================
    // STEP 2: Place the components (layout)
    // Order from outside to inside:
    //   backgroundPanel -> cardPanel -> formPanel
    // ==================================================================
    private void buildLayout() {
        // --- The white login card ---
        cardPanel = new JPanel(new BorderLayout());
        cardPanel.setBackground(CARD_COLOR);
        cardPanel.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));

        // Navy strip at the top of the card
        JPanel accentStrip = new JPanel();
        accentStrip.setBackground(PRIMARY_COLOR);
        accentStrip.setPreferredSize(new Dimension(0, 8));
        cardPanel.add(accentStrip, BorderLayout.NORTH);

        // --- The form inside the card (items are stacked top to bottom) ---
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(CARD_COLOR);
        // Empty space around the form: top, left, bottom, right
        formPanel.setBorder(BorderFactory.createEmptyBorder(30, 40, 36, 40));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;                              // one column only
        gbc.fill = GridBagConstraints.HORIZONTAL;   // stretch to full width
        gbc.weightx = 1.0;

        int row = 0; // goes up by 1 for each item, so items stack downward

        // Insets = space around an item (top, left, bottom, right).
        // Only the bottom value changes, to set the gap below each item.

        // "ICTD IMS" title
        gbc.gridy = row++;
        gbc.insets = new Insets(0, 0, 4, 0);
        formPanel.add(abbreviationLabel, gbc);

        // Full system name
        gbc.gridy = row++;
        gbc.insets = new Insets(0, 0, 26, 0);
        formPanel.add(systemNameLabel, gbc);

        // Heading
        gbc.gridy = row++;
        gbc.insets = new Insets(0, 0, 22, 0);
        formPanel.add(headingLabel, gbc);

        // "Username" label
        gbc.gridy = row++;
        gbc.insets = new Insets(0, 0, 6, 0);
        formPanel.add(usernameLabel, gbc);

        // Username box
        gbc.gridy = row++;
        gbc.insets = new Insets(0, 0, 16, 0);
        formPanel.add(usernameField, gbc);

        // "Password" label
        gbc.gridy = row++;
        gbc.insets = new Insets(0, 0, 6, 0);
        formPanel.add(passwordLabel, gbc);

        // Password box (password field + Show/Hide button)
        gbc.gridy = row++;
        gbc.insets = new Insets(0, 0, 26, 0);
        formPanel.add(passwordPanel, gbc);

        // Login button
        gbc.gridy = row++;
        gbc.insets = new Insets(0, 0, 0, 0);
        formPanel.add(loginButton, gbc);

        cardPanel.add(formPanel, BorderLayout.CENTER);

        // --- Background: keeps the card in the center of the window ---
        backgroundPanel = new JPanel(new GridBagLayout());
        backgroundPanel.setBackground(BACKGROUND_COLOR);
        backgroundPanel.add(cardPanel);

        // Use this panel as the window content
        setContentPane(backgroundPanel);
    }

    // ==================================================================
    // STEP 3: Window settings
    // ==================================================================
    private void configureFrame() {
        setTitle("ICTD IMS - Login");                    // text in the title bar
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);  // closing the window stops the program
        setMinimumSize(new Dimension(560, 640));         // smallest allowed size
        setSize(900, 700);                               // starting size
        setLocationRelativeTo(null);                     // open in the center of the screen
    }

    // ==================================================================
    // STEP 4: Events (what happens when the user clicks or presses a key)
    // ==================================================================
    private void registerEvents() {
        // Click Login -> run handleLogin()
        loginButton.addActionListener(e -> handleLogin());

        // Click Show/Hide -> show or hide the password
        showPasswordButton.addActionListener(e -> togglePasswordVisibility());

        // Pressing Enter also clicks the Login button
        getRootPane().setDefaultButton(loginButton);
    }

    /**
     * Runs when Login is clicked (or Enter is pressed).
     * It sends the typed username and password to LoginLogic,
     * then shows a message or opens a dashboard based on the result.
     */
    private void handleLogin() {
        // Get what the user typed (trim() removes extra spaces)
        String username = usernameField.getText().trim();
        // The password is a char[] so it can be erased from memory later
        char[] password = passwordField.getPassword();

        try {
            // Ask LoginLogic to check the login
            LoginLogic.LoginResult result = loginLogic.login(username, password);

            switch (result.getStatus()) {
                case EMPTY_FIELDS:
                    // A box was left empty
                    JOptionPane.showMessageDialog(
                            this,
                            "Please enter your username and password.",
                            "Incomplete Login",
                            JOptionPane.WARNING_MESSAGE);
                    break;

                case ADMIN:
                    // Default Administrator account
                    openAdminDashboard();
                    break;

                case ACCOUNT:
                    // Account created by the Administrator: open the dashboard for its role
                    openDashboardFor(result.getAccount());
                    break;

                default:
                    // INVALID: no account matched
                    JOptionPane.showMessageDialog(
                            this,
                            "Invalid username or password.",
                            "Login Failed",
                            JOptionPane.ERROR_MESSAGE);
                    break;
            }

        } catch (IOException ex) {
            // The accounts file could not be read
            JOptionPane.showMessageDialog(
                    this,
                    "The account file could not be read.\n" + ex.getMessage(),
                    "File Error",
                    JOptionPane.ERROR_MESSAGE);

        } finally {
            // Always erase the password from memory when done
            Arrays.fill(password, '\0');
        }
    }

    /**
     * Opens the Admin Dashboard and hides this login window.
     * This same LoginPage is passed to the dashboard, so Logout can
     * show it again (no second login page is created).
     */
    private void openAdminDashboard() {
        try {
            AdminDashboard adminDashboard = new AdminDashboard(this);
            adminDashboard.setVisible(true);  // show the dashboard
            setVisible(false);                // hide the login window
        } catch (IOException ex) {
            // The dashboard needs the accounts file and it could not be created or read
            JOptionPane.showMessageDialog(
                    this,
                    "The Admin Dashboard could not be opened because the account file "
                            + "could not be created or read.\n" + ex.getMessage(),
                    "File Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Opens the dashboard that matches the account's role and hides
     * this login window. Logout shows this same window again.
     */
    private void openDashboardFor(AccountStorage.Account account) {
        switch (account.getRole()) {
            case AccountStorage.ROLE_SECRETARY:
                new SecretaryDashboard(account.getFullName(), this).setVisible(true);
                setVisible(false);
                break;

            case AccountStorage.ROLE_TECHNICIAN:
                new TechnicianDashboard(account.getFullName(), this).setVisible(true);
                setVisible(false);
                break;

            case AccountStorage.ROLE_READS_SCHOLAR:
                new READSDashboard(account.getFullName(), account.getUsername(), this).setVisible(true);
                setVisible(false);
                break;

            case AccountStorage.ROLE_OFFICE_HEAD:
                new OfficeHeadDashboard(account.getFullName(), this).setVisible(true);
                setVisible(false);
                break;

            default:
                // A role without a dashboard (should not happen)
                JOptionPane.showMessageDialog(
                        this,
                        "No dashboard is available for the role: " + account.getRole(),
                        "ICTD IMS",
                        JOptionPane.ERROR_MESSAGE);
                break;
        }
    }

    /**
     * Called by a dashboard when the user clicks Logout.
     * Clears the form and shows the login window again.
     */
    public void showLoginPage() {
        usernameField.setText("");                  // clear the username
        passwordField.setText("");                  // clear the password
        passwordField.setEchoChar(defaultEchoChar); // hide the password again
        showPasswordButton.setText("Show");         // reset the button text
        setVisible(true);                           // show the window again
        usernameField.requestFocusInWindow();       // put the cursor in the username box
    }

    // ==================================================================
    // Program start
    // ==================================================================
    public static void main(String[] args) {
        // Swing windows should be created with invokeLater (safe way)
        SwingUtilities.invokeLater(() -> {
            try {
                // Same colors and look on every operating system
                UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            new LoginPage().setVisible(true);
        });
    }
}
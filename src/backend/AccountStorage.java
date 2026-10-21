/*
 * ============================================================================
 *  ICTD IMS - ACCOUNT STORAGE
 * ============================================================================
 *  What this program does:
 *    Saves and reads the user accounts of the system in a local text file.
 *    No database is used.
 *
 *  How the accounts are kept in memory (HASH TABLE):
 *    When the program starts, every account in the file is loaded into a
 *    hash table (see HashTable.java).
 *      key   = the username (in lowercase)
 *      value = the Account (name, role, salt, password hash)
 *    Because of the hash table, finding an account by username is fast
 *    (O(1) on average), no matter how many accounts there are.
 *    Every change (add, delete, reset password) is also saved to the file.
 *
 *  Who uses it:
 *    - LoginLogic     : calls verifyLogin() to check a username and password.
 *    - AdminDashboard : calls addAccount(), getAllAccounts(), deleteAccount()
 *                       and resetPassword() to manage the accounts.
 *
 *  Where the accounts are saved:
 *    data/accounts.txt (inside the project folder). The file and the "data"
 *    folder are created automatically if they do not exist.
 *
 *  File format (one account per line, parts separated by "|"):
 *    fullName|username|role|salt|passwordHash
 *
 *  About passwords:
 *    Passwords are NOT saved as plain text. Each password is mixed with a
 *    random "salt" and turned into a hash (SHA-256). Only the salt and the
 *    hash are saved, so nobody can read the real password from the file.
 *
 *  Note:
 *    The default "admin" account is not saved here. It is kept in
 *    LoginLogic.java, so the username "admin" cannot be created again.
 * ============================================================================
 */
package backend;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Locale;

public class AccountStorage {

    // ------------------------------------------------------------------
    // Roles an account can have (use these names everywhere in the system)
    // ------------------------------------------------------------------
    public static final String ROLE_SECRETARY = "Secretary";
    public static final String ROLE_TECHNICIAN = "Technician";
    public static final String ROLE_READS_SCHOLAR = "READS Scholar";
    public static final String ROLE_OFFICE_HEAD = "Office Head";

    // List of all roles (handy for a dropdown box in the Admin Dashboard)
    public static final String[] ROLES = {
            ROLE_SECRETARY, ROLE_TECHNICIAN, ROLE_READS_SCHOLAR, ROLE_OFFICE_HEAD
    };

    // ------------------------------------------------------------------
    // Settings
    // ------------------------------------------------------------------
    private static final String DEFAULT_FILE_PATH = "data/accounts.txt";
    private static final String RESERVED_USERNAME = "admin"; // used by the default admin
    private static final int MIN_PASSWORD_LENGTH = 6;
    private static final int SALT_LENGTH = 16;               // bytes

    // First lines of the file (lines starting with # are ignored when reading)
    private static final String[] FILE_HEADER = {
            "# ICTD IMS accounts file. Do not edit by hand.",
            "# Format: fullName|username|role|salt|passwordHash"
    };

    // The accounts file
    private final Path filePath;

    // The hash table that holds all accounts (key = username in lowercase)
    private HashTable<Account> accounts = new HashTable<>();

    // ==================================================================
    // Account (one saved account)
    // ==================================================================
    public static class Account {
        private final String fullName;
        private final String username;
        private final String role;
        private final String salt;          // random text mixed into the password
        private final String passwordHash;  // the password after hashing

        private Account(String fullName, String username, String role,
                        String salt, String passwordHash) {
            this.fullName = fullName;
            this.username = username;
            this.role = role;
            this.salt = salt;
            this.passwordHash = passwordHash;
        }

        public String getFullName() {
            return fullName;
        }

        public String getUsername() {
            return username;
        }

        public String getRole() {
            return role;
        }
    }

    // ==================================================================
    // Setup
    // ==================================================================

    /**
     * Opens the default accounts file (data/accounts.txt) and loads the
     * accounts into the hash table.
     * Creates the file and folder if they do not exist yet.
     */
    public AccountStorage() throws IOException {
        this(DEFAULT_FILE_PATH);
    }

    /** Same as above, but uses a file path of your choice (useful for testing). */
    public AccountStorage(String path) throws IOException {
        this.filePath = Paths.get(path);
        createFileIfMissing();
        loadAccounts();
    }

    /** Creates the "data" folder and the accounts file when they are missing. */
    private void createFileIfMissing() throws IOException {
        Path folder = filePath.toAbsolutePath().getParent();
        if (folder != null) {
            Files.createDirectories(folder);
        }
        if (Files.notExists(filePath)) {
            Files.write(filePath, Arrays.asList(FILE_HEADER), StandardCharsets.UTF_8);
        }
    }

    /** Reads the file again and rebuilds the hash table (use if the file was changed). */
    public void reload() throws IOException {
        loadAccounts();
    }

    // ==================================================================
    // Login
    // ==================================================================

    /**
     * Checks a username and password.
     * Returns the matching Account, or null if the username or password is wrong.
     * The caller should erase the password array after using this method.
     */
    public Account verifyLogin(String username, char[] password) {
        Account account = findAccount(username);   // fast lookup in the hash table
        if (account == null) {
            return null; // no such username
        }

        // Hash the typed password with the saved salt and compare with the saved hash
        String typedHash = hashPassword(password, account.salt);
        boolean same = MessageDigest.isEqual(
                typedHash.getBytes(StandardCharsets.UTF_8),
                account.passwordHash.getBytes(StandardCharsets.UTF_8));

        return same ? account : null;
    }

    // ==================================================================
    // Manage accounts (used by the Admin Dashboard)
    // ==================================================================

    /**
     * Creates a new account, puts it in the hash table, and saves it to the file.
     * Throws IllegalArgumentException with a readable message when the
     * input is not valid (for example: empty name, duplicate username).
     * The caller should erase the password array after using this method.
     */
    public void addAccount(String fullName, String username, char[] password, String role)
            throws IOException {
        fullName = fullName == null ? "" : fullName.trim();
        username = username == null ? "" : username.trim();

        // --- Check the input ---
        if (fullName.isEmpty()) {
            throw new IllegalArgumentException("Full name is required.");
        }
        if (username.isEmpty()) {
            throw new IllegalArgumentException("Username is required.");
        }
        if (hasForbiddenCharacters(fullName) || hasForbiddenCharacters(username)) {
            throw new IllegalArgumentException("Name and username cannot contain the \"|\" symbol.");
        }
        if (username.contains(" ")) {
            throw new IllegalArgumentException("Username cannot contain spaces.");
        }
        if (username.equalsIgnoreCase(RESERVED_USERNAME)) {
            throw new IllegalArgumentException("The username \"admin\" is reserved.");
        }
        checkPassword(password);
        if (!isValidRole(role)) {
            throw new IllegalArgumentException("Please choose a valid role.");
        }
        if (accounts.containsKey(makeKey(username))) {
            throw new IllegalArgumentException("That username is already taken.");
        }

        // --- Put the account in the hash table, then save the file ---
        String salt = createSalt();
        String hash = hashPassword(password, salt);
        String key = makeKey(username);

        accounts.put(key, new Account(fullName, username, role, salt, hash));
        try {
            saveAccounts();
        } catch (IOException ex) {
            accounts.remove(key);   // saving failed, so undo the change
            throw ex;
        }
    }

    /** Returns all accounts, sorted by username (never null; empty list if none). */
    public List<Account> getAllAccounts() {
        List<Account> list = accounts.values();
        // A hash table has no order, so sort the list to show it neatly
        list.sort((a, b) -> a.username.compareToIgnoreCase(b.username));
        return list;
    }

    /** Finds one account by username (not case-sensitive). Returns null if not found. */
    public Account findAccount(String username) {
        if (username == null) {
            return null;
        }
        return accounts.get(makeKey(username));
    }

    /** Returns true if the username is already used by an account. */
    public boolean usernameExists(String username) {
        return findAccount(username) != null;
    }

    /**
     * Deletes the account with this username.
     * Returns true if it was deleted, false if no such account exists.
     */
    public boolean deleteAccount(String username) throws IOException {
        if (username == null) {
            return false;
        }

        String key = makeKey(username);
        Account removed = accounts.remove(key);
        if (removed == null) {
            return false; // no such account
        }

        try {
            saveAccounts();
        } catch (IOException ex) {
            accounts.put(key, removed);   // saving failed, so put it back
            throw ex;
        }
        return true;
    }

    /**
     * Sets a new password for an account.
     * Returns true if it was changed, false if no such account exists.
     * The caller should erase the password array after using this method.
     */
    public boolean resetPassword(String username, char[] newPassword) throws IOException {
        checkPassword(newPassword);
        if (username == null) {
            return false;
        }

        String key = makeKey(username);
        Account old = accounts.get(key);
        if (old == null) {
            return false; // no such account
        }

        // Replace the old account with the same details and a new salt + hash
        String salt = createSalt();
        String hash = hashPassword(newPassword, salt);
        accounts.put(key, new Account(old.fullName, old.username, old.role, salt, hash));

        try {
            saveAccounts();
        } catch (IOException ex) {
            accounts.put(key, old);   // saving failed, so bring back the old account
            throw ex;
        }
        return true;
    }

    // ==================================================================
    // Reading and writing the file
    // ==================================================================

    /** Reads every account from the file into a new hash table. Broken lines are skipped. */
    private void loadAccounts() throws IOException {
        HashTable<Account> loaded = new HashTable<>();

        for (String line : Files.readAllLines(filePath, StandardCharsets.UTF_8)) {
            line = line.trim();

            // Skip empty lines and comment lines
            if (line.isEmpty() || line.startsWith("#")) {
                continue;
            }

            // fullName | username | role | salt | passwordHash
            String[] parts = line.split("\\|", -1);
            if (parts.length != 5) {
                continue; // wrong format, skip it
            }
            loaded.put(makeKey(parts[1]), new Account(parts[0], parts[1], parts[2], parts[3], parts[4]));
        }
        accounts = loaded;
    }

    /** Saves all accounts from the hash table to the file (replaces the old content). */
    private void saveAccounts() throws IOException {
        List<String> lines = new ArrayList<>(Arrays.asList(FILE_HEADER));

        for (Account a : getAllAccounts()) {
            lines.add(a.fullName + "|" + a.username + "|" + a.role + "|"
                    + a.salt + "|" + a.passwordHash);
        }
        Files.write(filePath, lines, StandardCharsets.UTF_8);
    }

    // ==================================================================
    // Hash table key
    // ==================================================================

    /** The key used in the hash table: the username in lowercase, so "Ana" and "ana" are the same. */
    private static String makeKey(String username) {
        return username.trim().toLowerCase(Locale.ROOT);
    }

    // ==================================================================
    // Password helpers
    // ==================================================================

    /** Makes a new random salt, written as text. */
    private static String createSalt() {
        byte[] salt = new byte[SALT_LENGTH];
        new SecureRandom().nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }

    /** Turns a password + salt into a hash (SHA-256), written as text. */
    private static String hashPassword(char[] password, String salt) {
        byte[] passwordBytes = new String(password).getBytes(StandardCharsets.UTF_8);
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            digest.update(Base64.getDecoder().decode(salt)); // mix in the salt first
            byte[] hash = digest.digest(passwordBytes);
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException ex) {
            // SHA-256 is part of every Java installation, so this should never happen
            throw new IllegalStateException("SHA-256 is not available.", ex);
        } finally {
            Arrays.fill(passwordBytes, (byte) 0); // erase the copy from memory
        }
    }

    /** Throws an error message if the password is empty or too short. */
    private static void checkPassword(char[] password) {
        if (password == null || password.length < MIN_PASSWORD_LENGTH) {
            throw new IllegalArgumentException(
                    "Password must be at least " + MIN_PASSWORD_LENGTH + " characters.");
        }
    }

    // ==================================================================
    // Small checks
    // ==================================================================

    /** The "|" symbol and new lines would break the file format. */
    private static boolean hasForbiddenCharacters(String text) {
        return text.contains("|") || text.contains("\n") || text.contains("\r");
    }

    /** Returns true if the role is one of the roles in ROLES. */
    private static boolean isValidRole(String role) {
        for (String r : ROLES) {
            if (r.equals(role)) {
                return true;
            }
        }
        return false;
    }
}
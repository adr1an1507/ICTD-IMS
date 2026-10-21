/*
 * ============================================================================
 *  ICTD IMS - LOGIN LOGIC
 * ============================================================================
 *  What this program does:
 *    Decides if a login is correct.
 * ============================================================================
 */
package backend;

import java.io.IOException;
import java.util.Arrays;

public class LoginLogic {

    // ------------------------------------------------------------------
    // Default Administrator account (do not change)
    // This account is not saved in the file, so it always works.
    // ------------------------------------------------------------------
    private static final String ADMIN_USERNAME = "admin";
    private static final String ADMIN_PASSWORD = "#ictdunoradmin2026";

    // ==================================================================
    // Result of a login check
    // ==================================================================

    /** The possible outcomes of a login. */
    public enum Status {
        EMPTY_FIELDS, ADMIN, ACCOUNT, INVALID
    }

    /** Holds the outcome of a login, plus the account when Status is ACCOUNT. */
    public static class LoginResult {
        private final Status status;
        private final AccountStorage.Account account; // only used for ACCOUNT, otherwise null

        private LoginResult(Status status, AccountStorage.Account account) {
            this.status = status;
            this.account = account;
        }

        /** What happened (EMPTY_FIELDS, ADMIN, ACCOUNT or INVALID). */
        public Status getStatus() {
            return status;
        }

        /** The logged-in account. Only available when the status is ACCOUNT. */
        public AccountStorage.Account getAccount() {
            return account;
        }
    }

    // ==================================================================
    // Login check
    // ==================================================================

    /**
     * Checks a username and password and returns the result.
     * Throws IOException only when the accounts file cannot be read.
     * This method does not erase the password. The caller (LoginPage) does that.
     */
    public LoginResult login(String username, char[] password) throws IOException {
        // Remove extra spaces in the username
        String cleanUsername = username == null ? "" : username.trim();

        // 1. Both values are required
        if (cleanUsername.isEmpty() || password == null || password.length == 0) {
            return new LoginResult(Status.EMPTY_FIELDS, null);
        }

        // 2. Default Administrator account (the file is not needed for this)
        if (isAdmin(cleanUsername, password)) {
            return new LoginResult(Status.ADMIN, null);
        }

        // 3. Accounts created by the Administrator (Secretary, Technician, READS Scholar)
        AccountStorage accountStorage = new AccountStorage();
        AccountStorage.Account account = accountStorage.verifyLogin(cleanUsername, password);
        if (account != null) {
            return new LoginResult(Status.ACCOUNT, account);
        }

        // 4. Nothing matched
        return new LoginResult(Status.INVALID, null);
    }

    /** Returns true if the username and password match the default admin account. */
    private boolean isAdmin(String username, char[] password) {
        return username.equals(ADMIN_USERNAME)
                && Arrays.equals(password, ADMIN_PASSWORD.toCharArray());
    }
}
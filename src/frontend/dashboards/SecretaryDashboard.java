/*
 * ============================================================================
 *  ICTD IMS - SECRETARY DASHBOARD
 * ============================================================================
 *  What this program does:
 *    The window a Secretary sees after logging in. It is used to record
 *    incoming and outgoing communication (documents) and to handle
 *    equipment requests.
 *
 *  Pages:
 *    Home, Incoming Communication, Outgoing Communication,
 *    Equipment Requests, Profile, Logout
 *
 *  Note:
 *    This version only has the screens. Saving the records (file or database)
 *    will be added later. The shared window parts are in RoleDashboard.java.
 * ============================================================================
 */
package frontend.dashboards;

import frontend.auth.LoginPage;
import frontend.panels.RoleDashboard;

public class SecretaryDashboard extends RoleDashboard {

    // Table columns
    private static final String[] COMMUNICATION_COLUMNS =
            {"Date", "Department", "Doc Code", "Description", "Received by", "Status"};
    private static final int[] COMMUNICATION_WIDTHS = {115, 160, 120, 300, 150, 110};

    private static final String[] REQUEST_COLUMNS =
            {"Date", "Requested by", "Department/Office", "Equipment", "Purpose", "Status"};
    private static final int[] REQUEST_WIDTHS = {115, 160, 170, 170, 260, 110};

    /**
     * @param userName  full name of the logged-in Secretary
     * @param loginPage the existing login page (shown again on Logout)
     */
    public SecretaryDashboard(String userName, LoginPage loginPage) {
        super("Secretary", userName, loginPage);

        // Sidebar menu
        addNavButton("Home", this::showHome);
        addSectionLabel("COMMUNICATION LOGS");
        addNavButton("Incoming Communication",
                () -> showPage(createCommunicationPage("Incoming Communication Logs")));
        addNavButton("Outgoing Communication",
                () -> showPage(createCommunicationPage("Outgoing Communication Logs")));
        addSectionLabel("EQUIPMENT");
        addNavButton("Equipment Requests", () -> showPage(createRequestPage()));

        showHome(); // first page
    }

    // Home page
    private void showHome() {
        showPage(buildHomePage(
                "Welcome to your Secretary Dashboard. Here you can record documents "
                        + "and handle equipment requests.",
                createSummaryCard("Communication Summary",
                        "Incoming", "Outgoing", "Pending", "Completed"),
                createSummaryCard("Equipment Requests",
                        "Received", "Pending", "Approved", "Rejected"),
                createTextCard("Recent Activities", "No recent activities recorded."),
                createTextCard("Announcements", "No announcements available.")));
    }

    // Incoming / outgoing communication log
    private javax.swing.JPanel createCommunicationPage(String title) {
        return createTablePage(title, COMMUNICATION_COLUMNS, COMMUNICATION_WIDTHS,
                "Add", "Edit", "Delete", "Done");
    }

    // Equipment requests
    private javax.swing.JPanel createRequestPage() {
        return createTablePage("Equipment Requests", REQUEST_COLUMNS, REQUEST_WIDTHS,
                "Add", "Edit", "Delete");
    }
}
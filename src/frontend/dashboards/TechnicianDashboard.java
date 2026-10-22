/*
 * ============================================================================
 *  ICTD IMS - TECHNICIAN DASHBOARD
 * ============================================================================
 *  What this program does:
 *    The window a Technician sees after logging in. It is used to follow
 *    assigned tasks, record equipment coming in and going out, and handle
 *    service (repair) requests.
 *
 *  Pages:
 *    Home, My Tasks, Incoming Equipment, Outgoing Equipment,
 *    Service Requests, Profile, Logout
 *
 *  Note:
 *    This version only has the screens. Saving the records (file or database)
 *    will be added later. The shared window parts are in RoleDashboard.java.
 * ============================================================================
 */
package frontend.dashboards;

import javax.swing.JPanel;

import frontend.auth.LoginPage;
import frontend.panels.RoleDashboard;

public class TechnicianDashboard extends RoleDashboard {

    // Table columns
    private static final String[] TASK_COLUMNS =
            {"Task Description", "Assigned by", "Due Date", "Status"};
    private static final int[] TASK_WIDTHS = {380, 160, 120, 120};

    private static final String[] EQUIPMENT_COLUMNS = {
            "Date", "Department/Office", "End-user", "Equipment Name",
            "Equipment Model", "Serial Number", "Inclusions", "Received by"};
    private static final int[] EQUIPMENT_WIDTHS = {105, 155, 125, 160, 145, 130, 150, 125};

    private static final String[] SERVICE_COLUMNS =
            {"Date", "Requested by", "Department/Office", "Equipment", "Problem", "Status"};
    private static final int[] SERVICE_WIDTHS = {115, 160, 170, 170, 260, 110};

    /**
     * @param userName  full name of the logged-in Technician
     * @param loginPage the existing login page (shown again on Logout)
     */
    public TechnicianDashboard(String userName, LoginPage loginPage) {
        super("Technician", userName, loginPage);

        // Sidebar menu
        addNavButton("Home", this::showHome);
        addSectionLabel("TASKS");
        addNavButton("My Tasks", () -> showPage(createTaskPage()));
        addSectionLabel("EQUIPMENT");
        addNavButton("Incoming Equipment",
                () -> showPage(createEquipmentPage("Incoming Equipment Logs")));
        addNavButton("Outgoing Equipment",
                () -> showPage(createEquipmentPage("Outgoing Equipment Logs")));
        addNavButton("Service Requests", () -> showPage(createServicePage()));

        showHome(); // first page
    }

    // Home page
    private void showHome() {
        showPage(buildHomePage(
                "Welcome to your Technician Dashboard. Here you can view your tasks, "
                        + "equipment, and service requests.",
                createSummaryCard("Task Summary",
                        "Assigned", "Pending", "In Progress", "Completed"),
                createTableCard("My Tasks", TASK_COLUMNS, "No tasks assigned"),
                createSummaryCard("Equipment Status",
                        "Received", "Under Repair", "Ready", "Released"),
                createTextCard("Announcements", "No announcements available.")));
    }

    // My tasks
    private JPanel createTaskPage() {
        return createTablePage("My Tasks", TASK_COLUMNS, TASK_WIDTHS,
                "Start Task", "Mark Done");
    }

    // Incoming / outgoing equipment log
    private JPanel createEquipmentPage(String title) {
        return createTablePage(title, EQUIPMENT_COLUMNS, EQUIPMENT_WIDTHS,
                "Add", "Edit", "Delete");
    }

    // Service (repair) requests
    private JPanel createServicePage() {
        return createTablePage("Service Requests", SERVICE_COLUMNS, SERVICE_WIDTHS,
                "Add", "Update Status", "Delete");
    }
}
/*
 * ============================================================================
 *  ICTD IMS - OFFICE HEAD DASHBOARD
 * ============================================================================
 *  What this program does:
 *    The window the Office Head sees after logging in. It is used to assign
 *    tasks, approve or reject equipment requests, and watch the equipment,
 *    communication, and attendance records of the department.
 *
 *  Pages:
 *    Home, Task Assignment, Equipment Requests, Equipment Logs,
 *    Communication Logs, READS Attendance, Profile, Logout
 *
 *  Note:
 *    This version only has the screens. Saving the records (file or database)
 *    will be added later. The shared window parts are in RoleDashboard.java.
 * ============================================================================
 */
package users;

import frontend.LoginPage;

import javax.swing.JPanel;

public class OfficeHeadDashboard extends RoleDashboard {

    // Table columns
    private static final String[] TASK_COLUMNS =
            {"Task Description", "Assigned to", "Role", "Due Date", "Status"};
    private static final int[] TASK_WIDTHS = {340, 170, 130, 115, 115};

    private static final String[] REQUEST_COLUMNS =
            {"Date", "Requested by", "Department/Office", "Equipment", "Purpose", "Status"};
    private static final int[] REQUEST_WIDTHS = {115, 160, 170, 170, 260, 110};

    private static final String[] EQUIPMENT_COLUMNS = {
            "Date", "Department/Office", "End-user", "Equipment Name",
            "Equipment Model", "Serial Number", "Inclusions", "Received by"};
    private static final int[] EQUIPMENT_WIDTHS = {105, 155, 125, 160, 145, 130, 150, 125};

    private static final String[] COMMUNICATION_COLUMNS =
            {"Date", "Department", "Doc Code", "Description", "Received by", "Status"};
    private static final int[] COMMUNICATION_WIDTHS = {115, 160, 120, 300, 150, 110};

    private static final String[] ATTENDANCE_COLUMNS =
            {"SPN", "First Name", "Last Name", "Morning In", "Morning Out"};

    /**
     * @param userName  full name of the logged-in Office Head
     * @param loginPage the existing login page (shown again on Logout)
     */
    public OfficeHeadDashboard(String userName, LoginPage loginPage) {
        super("Office Head", userName, loginPage);

        // Sidebar menu
        addNavButton("Home", this::showHome);
        addSectionLabel("MANAGEMENT");
        addNavButton("Task Assignment", () -> showPage(createTaskPage()));
        addNavButton("Equipment Requests", () -> showPage(createRequestPage()));
        addSectionLabel("MONITORING");
        addNavButton("Equipment Logs", () -> showPage(createEquipmentLogPage()));
        addNavButton("Communication Logs", () -> showPage(createCommunicationLogPage()));
        addNavButton("READS Attendance", () -> showPage(createAttendancePage()));

        showHome(); // first page
    }

    // Home page
    private void showHome() {
        showPage(buildHomePage(
                "Welcome to your Office Head Dashboard. Here you can assign tasks, "
                        + "review requests, and monitor the department records.",
                createSummaryCard("Overview",
                        "Pending Requests", "Open Tasks", "Borrowed Equipment", "Overdue"),
                createTableCard("Pending Approvals", REQUEST_COLUMNS, "No pending requests"),
                createTextCard("Recent Activities", "No recent activities recorded."),
                createTextCard("Announcements", "No announcements available.")));
    }

    // Assign tasks to Technicians and READS Scholars
    private JPanel createTaskPage() {
        return createTablePage("Task Assignment", TASK_COLUMNS, TASK_WIDTHS,
                "Add Task", "Edit", "Delete");
    }

    // Approve or reject equipment requests
    private JPanel createRequestPage() {
        return createTablePage("Equipment Requests", REQUEST_COLUMNS, REQUEST_WIDTHS,
                "Approve", "Reject");
    }

    // View-only pages (no buttons)
    private JPanel createEquipmentLogPage() {
        return createTablePage("Equipment Logs", EQUIPMENT_COLUMNS, EQUIPMENT_WIDTHS);
    }

    private JPanel createCommunicationLogPage() {
        return createTablePage("Communication Logs", COMMUNICATION_COLUMNS, COMMUNICATION_WIDTHS);
    }

    private JPanel createAttendancePage() {
        return createTablePage("READS Attendance", ATTENDANCE_COLUMNS, null);
    }
}
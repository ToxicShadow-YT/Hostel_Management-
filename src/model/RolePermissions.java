package model;

/**
 * Central access policy for staff-facing features.
 */
public final class RolePermissions {

    private RolePermissions() {
    }

    public static boolean canAccessStaffPortal(String role) {
        return "Admin".equalsIgnoreCase(role)
                || "Staff".equalsIgnoreCase(role)
                || "Warden".equalsIgnoreCase(role);
    }

    public static boolean canViewActivityLogs(String role) {
        return "Admin".equalsIgnoreCase(role);
    }
}

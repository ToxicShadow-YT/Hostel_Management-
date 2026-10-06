package model;

import java.sql.Timestamp;

/**
 * Model representing an audit trail activity log entry.
 */
public class ActivityLog {
    private int logId;
    private String username;
    private String role;
    private String action;
    private String description;
    private Timestamp timestamp;

    public ActivityLog() {
    }

    public ActivityLog(int logId, String username, String role, String action, String description, Timestamp timestamp) {
        this.logId = logId;
        this.username = username;
        this.role = role;
        this.action = action;
        this.description = description;
        this.timestamp = timestamp;
    }

    public ActivityLog(String username, String role, String action, String description) {
        this(0, username, role, action, description, new Timestamp(System.currentTimeMillis()));
    }

    public int getLogId() {
        return logId;
    }

    public void setLogId(int logId) {
        this.logId = logId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "[" + timestamp + "] " + username + " (" + role + ") - " + action + ": " + description;
    }
}

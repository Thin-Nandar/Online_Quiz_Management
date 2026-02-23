// common/Activity.java
package common;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.time.format.DateTimeFormatter;

public class Activity implements Serializable {
    private String adminName;
    private String action;
    private LocalDateTime timestamp;

    private static final DateTimeFormatter FMT = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    // Constructor: actor + action
    public Activity(String adminName, String action) {
        this.adminName = adminName;
        this.action = action;
        this.timestamp = LocalDateTime.now();
    }

    // Full constructor (used by fromString)
    public Activity(String adminName, String action, LocalDateTime ts) {
        this.adminName = adminName;
        this.action = action;
        this.timestamp = ts != null ? ts : LocalDateTime.now();
    }

    public String getAdminName() { return adminName; }
    public String getAction() { return action; }
    public LocalDateTime getTimestamp() { return timestamp; }

    // Format: 2025-08-28T12:34:56|adminName|action
    @Override
    public String toString() {
        return String.join("|", timestamp.format(FMT), adminName == null ? "" : adminName, action == null ? "" : action);
    }

    public static Activity fromString(String s) {
        if (s == null) return null;
        try {
            String[] p = s.split("\\|", 3); // only 3 parts expected
            if (p.length < 3) return null;
            LocalDateTime ts;
            try {
                ts = LocalDateTime.parse(p[0].trim(), FMT);
            } catch (DateTimeParseException ex) {
                ts = LocalDateTime.now();
            }
            String admin = p[1].trim();
            String action = p[2].trim();
            return new Activity(admin, action, ts);
        } catch (Exception e) {
            return null;
        }
    }
}

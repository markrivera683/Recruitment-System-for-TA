package com.bupt.ta.model;

/**
 * A TA job application submitted by a student.
 */
public class Application {
    public String id;
    public String userId;          // applicant
    public String moduleName;
    public String moduleCode;
    public String role;            // e.g. "Teaching Assistant"
    public String applicationDate; // ISO yyyy-MM-dd
    /** Pending | Accepted | Rejected */
    public String status = "Pending";
    public String feedback;        // optional message from admin

    public Application() {}

    public Application(String id, String userId, String moduleName, String moduleCode,
                       String role, String applicationDate) {
        this.id              = id;
        this.userId          = userId;
        this.moduleName      = moduleName;
        this.moduleCode      = moduleCode;
        this.role            = role;
        this.applicationDate = applicationDate;
    }
}

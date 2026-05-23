package com.bupt.ta.model;

/**
 * Context for MO retrospective AI review of a processed application (Accepted / Rejected / Withdrawn).
 * Built at request time; not persisted.
 */
public class MoProcessedReviewContext {
    public String applicationId;
    public String applicantName;
    public String moduleName;
    public String moduleCode;
    public String role;
    public String applicationDate;
    public String decisionStatus;
    public String moFeedback;

    public String jobDescription;
    public String jobActivityType;
    public String workloadHours;
    public String jobSkills;

    public String applicantCapabilities;
}

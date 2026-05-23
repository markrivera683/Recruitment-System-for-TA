package com.bupt.ta.model;

import java.util.List;

/**
 * A TA job posting created by a module owner (MO) and published for student applicants.
 *
 * <p>Persisted in {@code WEB-INF/data/jobs.json} through {@link com.bupt.ta.service.JobService}.
 * Access is via JavaBean-style getters and setters for JSON mapping. List fields
 * ({@link #requiredSkills}, {@link #schedule}) may be {@code null} when loaded from sparse JSON.
 *
 * <p>Not thread-safe; the service layer serializes concurrent access to the jobs file.
 */
public class Job {
    private String id;
    private String moduleName;
    private String moduleCode;
    private String description;
    private String activityType;
    private List<String> requiredSkills;
    private String postDate;
    private String duration;
    private String applicationDeadline;
    private String numberOfTAs;
    private List<String> schedule;

    private String status;
    private String createdByMoId;
    private String createdAt;
    private String publishedAt;
    private String workloadHours;

    /** @return unique job identifier */
    public String getId() { return id; }
    /** @param id unique job identifier */
    public void setId(String id) { this.id = id; }

    /** @return human-readable module name shown in job listings */
    public String getModuleName() { return moduleName; }
    /** @param moduleName human-readable module name */
    public void setModuleName(String moduleName) { this.moduleName = moduleName; }

    /** @return module code (paired with module name for application matching) */
    public String getModuleCode() { return moduleCode; }
    /** @param moduleCode module code */
    public void setModuleCode(String moduleCode) { this.moduleCode = moduleCode; }

    /** @return full job description text */
    public String getDescription() { return description; }
    /** @param description full job description text */
    public void setDescription(String description) { this.description = description; }

    /** @return type of teaching activity (e.g. lab, tutorial) */
    public String getActivityType() { return activityType; }
    /** @param activityType type of teaching activity */
    public void setActivityType(String activityType) { this.activityType = activityType; }

    /** @return list of required skills; may be {@code null} */
    public List<String> getRequiredSkills() { return requiredSkills; }
    /** @param requiredSkills list of required skills */
    public void setRequiredSkills(List<String> requiredSkills) { this.requiredSkills = requiredSkills; }

    /** @return posting date string used for sorting and display */
    public String getPostDate() { return postDate; }
    /** @param postDate posting date string */
    public void setPostDate(String postDate) { this.postDate = postDate; }

    /** @return expected duration of the TA assignment */
    public String getDuration() { return duration; }
    /** @param duration expected duration of the assignment */
    public void setDuration(String duration) { this.duration = duration; }

    /** @return application deadline date or text */
    public String getApplicationDeadline() { return applicationDeadline; }
    /** @param applicationDeadline application deadline */
    public void setApplicationDeadline(String applicationDeadline) { this.applicationDeadline = applicationDeadline; }

    /** @return number of TA positions as a string (parsed by {@link JobApplicationStats#parseCapacity(String)}) */
    public String getNumberOfTAs() { return numberOfTAs; }
    /** @param numberOfTAs number of TA positions */
    public void setNumberOfTAs(String numberOfTAs) { this.numberOfTAs = numberOfTAs; }

    /** @return weekly or session schedule lines; may be {@code null} */
    public List<String> getSchedule() { return schedule; }
    /** @param schedule schedule lines */
    public void setSchedule(List<String> schedule) { this.schedule = schedule; }

    /** @return lifecycle status, e.g. draft or published */
    public String getStatus() { return status; }
    /** @param status lifecycle status */
    public void setStatus(String status) { this.status = status; }

    /** @return {@link User#id} of the module owner who created this job */
    public String getCreatedByMoId() { return createdByMoId; }
    /** @param createdByMoId module owner user id */
    public void setCreatedByMoId(String createdByMoId) { this.createdByMoId = createdByMoId; }

    /** @return ISO timestamp when the job record was created */
    public String getCreatedAt() { return createdAt; }
    /** @param createdAt creation timestamp */
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    /** @return ISO timestamp when the job was published, or {@code null} if not yet published */
    public String getPublishedAt() { return publishedAt; }
    /** @param publishedAt publication timestamp */
    public void setPublishedAt(String publishedAt) { this.publishedAt = publishedAt; }

    /** @return expected workload hours for the role (used in MO workload advice) */
    public String getWorkloadHours() { return workloadHours; }
    /** @param workloadHours expected workload hours */
    public void setWorkloadHours(String workloadHours) { this.workloadHours = workloadHours; }
}

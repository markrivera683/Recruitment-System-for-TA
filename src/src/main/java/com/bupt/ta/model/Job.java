package com.bupt.ta.model;

import java.util.List;

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

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getModuleName() { return moduleName; }
    public void setModuleName(String moduleName) { this.moduleName = moduleName; }

    public String getModuleCode() { return moduleCode; }
    public void setModuleCode(String moduleCode) { this.moduleCode = moduleCode; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getActivityType() { return activityType; }
    public void setActivityType(String activityType) { this.activityType = activityType; }

    public List<String> getRequiredSkills() { return requiredSkills; }
    public void setRequiredSkills(List<String> requiredSkills) { this.requiredSkills = requiredSkills; }

    public String getPostDate() { return postDate; }
    public void setPostDate(String postDate) { this.postDate = postDate; }

    public String getDuration() { return duration; }
    public void setDuration(String duration) { this.duration = duration; }

    public String getApplicationDeadline() { return applicationDeadline; }
    public void setApplicationDeadline(String applicationDeadline) { this.applicationDeadline = applicationDeadline; }

    public String getNumberOfTAs() { return numberOfTAs; }
    public void setNumberOfTAs(String numberOfTAs) { this.numberOfTAs = numberOfTAs; }

    public List<String> getSchedule() { return schedule; }
    public void setSchedule(List<String> schedule) { this.schedule = schedule; }
}

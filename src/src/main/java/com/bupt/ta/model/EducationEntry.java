package com.bupt.ta.model;

/**
 * One row in the education history table on the applicant profile page.
 */
public class EducationEntry {
    public String school;
    public String degree;
    public String major;
    public String period;

    public EducationEntry() {}

    public EducationEntry(String school, String degree, String major, String period) {
        this.school = school;
        this.degree = degree;
        this.major  = major;
        this.period = period;
    }
}

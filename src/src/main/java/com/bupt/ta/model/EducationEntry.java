package com.bupt.ta.model;

/** One row of education background on the applicant profile. */
public class EducationEntry {
    public String school;
    public String degree;
    public String major;
    /** 起止时间，自由文本 */
    public String period;

    public EducationEntry() {}

    public EducationEntry(String school, String degree, String major, String period) {
        this.school = school;
        this.degree = degree;
        this.major = major;
        this.period = period;
    }
}

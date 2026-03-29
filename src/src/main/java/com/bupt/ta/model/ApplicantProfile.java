package com.bupt.ta.model;

/**
 * Applicant profile (TA recruitment). Personal data, education, courses, availability, skills, CV.
 */
public class ApplicantProfile {
    public String userId;

    // --- 个人信息
    public String fullName;
    public String gender;
    /** 学位（如：硕士在读） */
    public String degree;
    public String major;
    public String studentId;
    public String idCard;
    public String phone;
    public String email;

    /**
     * JSON array of flat objects: [{"school":"...","degree":"...","major":"...","period":"..."},...]
     */
    public String educationJson;

    /** 已学课程，每行一门 */
    public String courses;

    /** 空闲时间 */
    public String freeTime;

    public String skills;

    /** Stored file name under WEB-INF/data/cv/{userId}/ */
    public String cvFileName;

    // --- legacy fields (older profiles.json)
    public String degreeProgramme;
    public String yearOfStudy;
    public String availability;
    public String selfIntro;

    public ApplicantProfile() {}

    public ApplicantProfile(String userId) {
        this.userId = userId;
    }
}

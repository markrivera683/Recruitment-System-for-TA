package com.bupt.ta.model;

public class ApplicantProfile {
    public String userId;
    public String degreeProgramme;
    public String yearOfStudy;
    public String skills;
    public String availability;
    public String selfIntro;
    public String cvFileName;

    public ApplicantProfile() {}

    public ApplicantProfile(String userId) {
        this.userId = userId;
    }
}

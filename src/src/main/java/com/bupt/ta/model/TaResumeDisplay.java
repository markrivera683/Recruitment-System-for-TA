package com.bupt.ta.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** One TA user plus optional applicant profile (for admin read-only resume list). */
public class TaResumeDisplay {
    public final User user;
    public final ApplicantProfile profile;
    public final List<EducationEntry> education;
    public final List<EducationEntry> visibleEducation;
    public final boolean hasSavedProfile;

    public TaResumeDisplay(User user, ApplicantProfile profile, List<EducationEntry> education) {
        this.user = user;
        this.profile = profile != null ? profile : new ApplicantProfile();
        this.education = education != null ? education : Collections.emptyList();
        this.visibleEducation = filterNonBlankEducation(this.education);
        this.hasSavedProfile = profile != null && profile.userId != null && !profile.userId.isEmpty()
                && hasAnyProfileContent(profile);
    }

    private static List<EducationEntry> filterNonBlankEducation(List<EducationEntry> in) {
        List<EducationEntry> out = new ArrayList<>();
        for (EducationEntry e : in) {
            if (e == null) continue;
            if (nonBlank(e.school) || nonBlank(e.degree) || nonBlank(e.major) || nonBlank(e.period)) {
                out.add(e);
            }
        }
        return out;
    }

    private static boolean hasAnyProfileContent(ApplicantProfile p) {
        return nonBlank(p.fullName) || nonBlank(p.email) || nonBlank(p.phone)
                || nonBlank(p.major) || nonBlank(p.skills) || nonBlank(p.courses)
                || nonBlank(p.educationJson) || nonBlank(p.cvFileName);
    }

    private static boolean nonBlank(String s) {
        return s != null && !s.trim().isEmpty();
    }

    public boolean hasEducationRows() {
        return !visibleEducation.isEmpty();
    }
}

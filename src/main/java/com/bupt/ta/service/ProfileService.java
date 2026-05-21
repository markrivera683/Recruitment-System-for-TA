package com.bupt.ta.service;

import com.bupt.ta.model.ApplicantProfile;
import com.bupt.ta.model.EducationEntry;
import com.bupt.ta.util.ApplicantFieldValidation;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class ProfileService {
    private static final String PROFILES_JSON = "profiles.json";
    private final FileStore store;

    public ProfileService(Path dataDir) {
        this.store = new FileStore(dataDir);
    }

    // ---------- helpers: Map <-> ApplicantProfile

    private static ApplicantProfile mapToProfile(Map<String, String> m) {
        ApplicantProfile p = new ApplicantProfile();
        p.userId = m.getOrDefault("userId", "");

        p.fullName = m.getOrDefault("fullName", "");
        p.gender = m.getOrDefault("gender", "");
        p.degree = m.getOrDefault("degree", "");
        p.major = m.getOrDefault("major", "");
        p.studentId = m.getOrDefault("studentId", "");
        p.idCard = m.getOrDefault("idCard", "");
        p.phone = m.getOrDefault("phone", "");
        p.email = m.getOrDefault("email", "");

        p.educationJson = m.getOrDefault("educationJson", "");
        p.courses = m.getOrDefault("courses", "");
        p.freeTime = m.getOrDefault("freeTime", "");
        if (p.freeTime.isEmpty()) {
            p.freeTime = m.getOrDefault("availability", "");
        }

        p.skills = m.getOrDefault("skills", "");
        p.cvFileName = m.getOrDefault("cvFileName", "");

        p.degreeProgramme = m.getOrDefault("degreeProgramme", "");
        p.yearOfStudy = m.getOrDefault("yearOfStudy", "");
        p.availability = m.getOrDefault("availability", "");
        p.selfIntro = m.getOrDefault("selfIntro", "");
        return p;
    }

    private static Map<String, String> profileToMap(ApplicantProfile p) {
        Map<String, String> m = new LinkedHashMap<>();
        m.put("userId", n(p.userId));
        m.put("fullName", n(p.fullName));
        m.put("gender", n(p.gender));
        m.put("degree", n(p.degree));
        m.put("major", n(p.major));
        m.put("studentId", n(p.studentId));
        m.put("idCard", n(p.idCard));
        m.put("phone", n(p.phone));
        m.put("email", n(p.email));
        m.put("educationJson", n(p.educationJson));
        m.put("courses", n(p.courses));
        m.put("freeTime", n(p.freeTime));
        m.put("skills", n(p.skills));
        m.put("cvFileName", n(p.cvFileName));
        m.put("degreeProgramme", n(p.degreeProgramme));
        m.put("yearOfStudy", n(p.yearOfStudy));
        m.put("availability", n(p.availability));
        m.put("selfIntro", n(p.selfIntro));
        return m;
    }

    private static String n(String s) {
        return s != null ? s : "";
    }

    /** Parse stored education JSON into a list (empty if invalid / empty). */
    public static List<EducationEntry> parseEducationJson(String json) {
        List<EducationEntry> out = new ArrayList<>();
        if (json == null || json.trim().isEmpty()) return out;
        String trimmed = json.trim();
        if (!trimmed.startsWith("[")) return out;
        try {
            List<Map<String, String>> rows = FileStore.parseJsonArray(trimmed);
            for (Map<String, String> row : rows) {
                EducationEntry e = new EducationEntry();
                e.school = row.getOrDefault("school", "");
                e.degree = row.getOrDefault("degree", "");
                e.major = row.getOrDefault("major", "");
                e.period = row.getOrDefault("period", "");
                out.add(e);
            }
        } catch (Exception ignored) {
            // keep empty
        }
        return out;
    }

    public static String buildEducationJson(List<EducationEntry> entries) {
        List<Map<String, String>> maps = new ArrayList<>();
        for (EducationEntry e : entries) {
            Map<String, String> row = new LinkedHashMap<>();
            row.put("school", n(e.school));
            row.put("degree", n(e.degree));
            row.put("major", n(e.major));
            row.put("period", n(e.period));
            maps.add(row);
        }
        return FileStore.toJsonArrayOfObjects(maps);
    }

    /**
     * True when the applicant has filled everything required to submit a job application
     * (same rules as profile validation for those fields).
     */
    public static boolean isApplicantProfileComplete(ApplicantProfile p) {
        if (p == null) {
            return false;
        }
        if (isBlank(p.fullName) || isBlank(p.gender) || isBlank(p.degree) || isBlank(p.major)) {
            return false;
        }
        if (!ApplicantFieldValidation.isAllowedApplicantDegreeLevel(p.degree)) {
            return false;
        }
        if (isBlank(p.studentId) || isBlank(p.idCard) || isBlank(p.phone) || isBlank(p.email)) {
            return false;
        }
        if (isBlank(p.courses) || isBlank(p.freeTime) || isBlank(p.skills)) {
            return false;
        }
        if (isBlank(p.educationJson)) {
            return false;
        }
        return !parseEducationJson(p.educationJson).isEmpty();
    }

    private static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    /** True when the applicant has skills and/or courses for AI matching. */
    public static boolean hasAiMatchingInput(ApplicantProfile p) {
        if (p == null) {
            return false;
        }
        return !isBlank(p.skills) || !isBlank(p.courses);
    }

    /** Combined text sent to AI for skill match and job recommendations. */
    public static String buildAiCapabilityText(ApplicantProfile p) {
        if (p == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        if (!isBlank(p.fullName)) {
            sb.append("Name: ").append(p.fullName.trim()).append("\n");
        }
        if (!isBlank(p.major)) {
            sb.append("Major: ").append(p.major.trim()).append("\n");
        }
        if (!isBlank(p.degree)) {
            sb.append("Degree: ").append(p.degree.trim()).append("\n");
        }
        if (!isBlank(p.skills)) {
            sb.append("Skills: ").append(p.skills.trim()).append("\n");
        }
        if (!isBlank(p.courses)) {
            sb.append("Courses: ").append(p.courses.trim()).append("\n");
        }
        if (!isBlank(p.freeTime)) {
            sb.append("Availability: ").append(p.freeTime.trim()).append("\n");
        }
        return sb.toString().trim();
    }

    // ---------- API

    public Optional<ApplicantProfile> getByUserId(String userId) throws IOException {
        List<Map<String, String>> rows = store.readMaps(PROFILES_JSON);
        return rows.stream()
                   .filter(m -> userId != null && userId.equals(m.get("userId")))
                   .map(ProfileService::mapToProfile)
                   .findFirst();
    }

    public ApplicantProfile upsert(ApplicantProfile profile) throws IOException {
        List<Map<String, String>> rows = store.readMaps(PROFILES_JSON);
        rows.removeIf(m -> profile.userId != null && profile.userId.equals(m.get("userId")));
        rows.add(profileToMap(profile));
        store.writeMaps(PROFILES_JSON, rows);
        return profile;
    }

    /** Remove profile row for user (e.g. admin account deletion). */
    public void deleteByUserId(String userId) throws IOException {
        if (userId == null || userId.isEmpty()) return;
        List<Map<String, String>> rows = store.readMaps(PROFILES_JSON);
        rows.removeIf(m -> userId.equals(m.get("userId")));
        store.writeMaps(PROFILES_JSON, rows);
    }
}

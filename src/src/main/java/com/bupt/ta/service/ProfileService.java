package com.bupt.ta.service;

import com.bupt.ta.model.ApplicantProfile;

import java.io.IOException;
import java.nio.file.Path;
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
        p.userId          = m.getOrDefault("userId", "");
        p.degreeProgramme = m.getOrDefault("degreeProgramme", "");
        p.yearOfStudy     = m.getOrDefault("yearOfStudy", "");
        p.skills          = m.getOrDefault("skills", "");
        p.availability    = m.getOrDefault("availability", "");
        p.selfIntro       = m.getOrDefault("selfIntro", "");
        p.cvFileName      = m.getOrDefault("cvFileName", "");
        return p;
    }

    private static Map<String, String> profileToMap(ApplicantProfile p) {
        Map<String, String> m = new LinkedHashMap<>();
        m.put("userId",          p.userId          != null ? p.userId          : "");
        m.put("degreeProgramme", p.degreeProgramme != null ? p.degreeProgramme : "");
        m.put("yearOfStudy",     p.yearOfStudy     != null ? p.yearOfStudy     : "");
        m.put("skills",          p.skills          != null ? p.skills          : "");
        m.put("availability",    p.availability    != null ? p.availability    : "");
        m.put("selfIntro",       p.selfIntro       != null ? p.selfIntro       : "");
        m.put("cvFileName",      p.cvFileName      != null ? p.cvFileName      : "");
        return m;
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
}

package com.bupt.ta.service;

import com.bupt.ta.model.ApplicantProfile;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

public class ProfileService {
    private static final String PROFILES_JSON = "profiles.json";
    private final FileStore store;

    public ProfileService(Path dataDir) {
        this.store = new FileStore(dataDir);
    }

    public Optional<ApplicantProfile> getByUserId(String userId) throws IOException {
        List<ApplicantProfile> profiles = store.readList(PROFILES_JSON, FileStore.listType(ApplicantProfile.class));
        return profiles.stream().filter(p -> p.userId != null && p.userId.equals(userId)).findFirst();
    }

    public ApplicantProfile upsert(ApplicantProfile profile) throws IOException {
        List<ApplicantProfile> profiles = store.readList(PROFILES_JSON, FileStore.listType(ApplicantProfile.class));
        profiles.removeIf(p -> p.userId != null && p.userId.equals(profile.userId));
        profiles.add(profile);
        store.writeList(PROFILES_JSON, profiles);
        return profile;
    }
}

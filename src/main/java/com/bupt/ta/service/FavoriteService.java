package com.bupt.ta.service;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Per-user job favorites stored in {@code favorites.json} as flat rows: userId, jobId.
 */
public class FavoriteService {

    private static final String FAVORITES_JSON = "favorites.json";
    private final FileStore store;

    public FavoriteService(Path dataDir) {
        this.store = new FileStore(dataDir);
    }

    public Set<String> getFavoriteJobIds(String userId) throws IOException {
        Set<String> ids = new LinkedHashSet<>();
        if (userId == null || userId.isEmpty()) {
            return ids;
        }
        for (Map<String, String> row : store.readMaps(FAVORITES_JSON)) {
            if (userId.equals(row.get("userId"))) {
                String jobId = row.get("jobId");
                if (jobId != null && !jobId.isEmpty()) {
                    ids.add(jobId);
                }
            }
        }
        return ids;
    }

    public boolean isFavorite(String userId, String jobId) throws IOException {
        if (userId == null || userId.isEmpty() || jobId == null || jobId.isEmpty()) {
            return false;
        }
        return getFavoriteJobIds(userId).contains(jobId);
    }

    /** @return {@code true} if the job is favorited after the toggle */
    public boolean toggleFavorite(String userId, String jobId) throws IOException {
        if (userId == null || userId.isEmpty() || jobId == null || jobId.isEmpty()) {
            return false;
        }
        List<Map<String, String>> rows = store.readMaps(FAVORITES_JSON);
        List<Map<String, String>> kept = new ArrayList<>();
        boolean removed = false;
        for (Map<String, String> row : rows) {
            if (userId.equals(row.get("userId")) && jobId.equals(row.get("jobId"))) {
                removed = true;
                continue;
            }
            kept.add(row);
        }
        if (removed) {
            store.writeMaps(FAVORITES_JSON, kept);
            return false;
        }
        Map<String, String> add = new java.util.LinkedHashMap<>();
        add.put("userId", userId);
        add.put("jobId", jobId);
        kept.add(add);
        store.writeMaps(FAVORITES_JSON, kept);
        return true;
    }
}

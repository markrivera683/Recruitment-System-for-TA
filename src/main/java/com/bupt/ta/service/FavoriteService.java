package com.bupt.ta.service;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Manages per-user job favorites in {@code favorites.json}.
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
        return store.readMaps(FAVORITES_JSON).stream()
                .anyMatch(m -> userId.equals(m.get("userId")) && jobId.equals(m.get("jobId")));
    }

    public boolean toggleFavorite(String userId, String jobId) throws IOException {
        if (userId == null || userId.isEmpty() || jobId == null || jobId.isEmpty()) {
            return false;
        }
        List<Map<String, String>> rows = store.readMaps(FAVORITES_JSON);
        boolean removed = rows.removeIf(m -> userId.equals(m.get("userId")) && jobId.equals(m.get("jobId")));
        if (removed) {
            store.writeMaps(FAVORITES_JSON, rows);
            return false;
        }
        Map<String, String> row = new LinkedHashMap<>();
        row.put("userId", userId);
        row.put("jobId", jobId);
        rows.add(row);
        store.writeMaps(FAVORITES_JSON, rows);
        return true;
    }
}

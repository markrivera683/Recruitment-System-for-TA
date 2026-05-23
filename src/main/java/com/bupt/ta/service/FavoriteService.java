package com.bupt.ta.service;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Manages per-user job favorites stored in {@code favorites.json}.
 * <p>
 * Each row is a flat map with {@code userId} and {@code jobId}. Favorite job ids for a user
 * are returned in insertion order (deduplicated). Toggle semantics add or remove a single
 * user–job pair atomically via rewrite of the JSON file.
 */
public class FavoriteService {

    private static final String FAVORITES_JSON = "favorites.json";
    private final FileStore store;

    /**
     * Creates a service backed by JSON files in the given data directory.
     *
     * @param dataDir root directory containing {@code favorites.json}
     */
    public FavoriteService(Path dataDir) {
        this.store = new FileStore(dataDir);
    }

    /**
     * Returns the set of job ids favorited by the user, preserving first-seen order.
     *
     * @param userId owner user id; {@code null} or empty yields an empty set
     * @return favorite job ids; never {@code null}
     * @throws IOException if {@code favorites.json} cannot be read
     */
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

    /**
     * Checks whether the given job is in the user's favorites.
     *
     * @param userId applicant user id
     * @param jobId  job id to test
     * @return {@code true} if the pair exists in storage; {@code false} if ids are blank or not favorited
     * @throws IOException if {@code favorites.json} cannot be read
     */
    public boolean isFavorite(String userId, String jobId) throws IOException {
        if (userId == null || userId.isEmpty() || jobId == null || jobId.isEmpty()) {
            return false;
        }
        return getFavoriteJobIds(userId).contains(jobId);
    }

    /**
     * Adds the job to favorites if absent, or removes it if already favorited.
     *
     * @param userId owner user id
     * @param jobId  job id to toggle
     * @return {@code true} if the job is favorited after the operation; {@code false} if removed or ids invalid
     * @throws IOException if the file cannot be read or written
     */
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

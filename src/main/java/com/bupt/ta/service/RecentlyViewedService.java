package com.bupt.ta.service;

import java.io.IOException;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Tracks per-user recently viewed job postings in {@code recently-viewed.json}.
 * <p>
 * Each row stores {@code userId}, {@code jobId}, and {@code viewedAt} (ISO-8601 instant).
 * History is capped at {@link #MAX_RECENT} distinct jobs per user, ordered most recent first.
 * Recording a view removes any prior row for the same user–job pair before appending.
 */
public class RecentlyViewedService {

    /** Maximum number of distinct recently viewed jobs retained per user. */
    public static final int MAX_RECENT = 3;
    private static final String RECENT_JSON = "recently-viewed.json";
    private final FileStore store;

    /**
     * Creates a service backed by JSON files in the given data directory.
     *
     * @param dataDir root directory containing {@code recently-viewed.json}
     */
    public RecentlyViewedService(Path dataDir) {
        this.store = new FileStore(dataDir);
    }

    /**
     * Returns job ids the user viewed most recently, up to {@link #MAX_RECENT}.
     * <p>
     * Sorted by {@code viewedAt} descending; duplicate job ids appear at most once.
     *
     * @param userId owner user id; {@code null} or empty yields an empty list
     * @return recent job ids, newest first; never {@code null}
     * @throws IOException if {@code recently-viewed.json} cannot be read
     */
    public List<String> getRecentJobIds(String userId) throws IOException {
        if (userId == null || userId.isEmpty()) {
            return new ArrayList<>();
        }
        return store.readMaps(RECENT_JSON).stream()
                .filter(row -> userId.equals(row.get("userId")))
                .filter(row -> row.get("jobId") != null && !row.get("jobId").isEmpty())
                .sorted(Comparator.comparing(
                        (Map<String, String> row) -> row.getOrDefault("viewedAt", ""),
                        Comparator.reverseOrder()))
                .map(row -> row.get("jobId"))
                .distinct()
                .limit(MAX_RECENT)
                .collect(Collectors.toList());
    }

    /**
     * Records that the user viewed a job at the current instant.
     * <p>
     * Replaces any existing entry for the same user and job, then prunes older entries so
     * only {@link #MAX_RECENT} distinct jobs remain for that user. No-op if ids are blank.
     *
     * @param userId viewer user id
     * @param jobId  viewed job id
     * @throws IOException if the file cannot be read or written
     */
    public void recordView(String userId, String jobId) throws IOException {
        if (userId == null || userId.isEmpty() || jobId == null || jobId.isEmpty()) {
            return;
        }
        List<Map<String, String>> rows = store.readMaps(RECENT_JSON);
        List<Map<String, String>> kept = new ArrayList<>();
        for (Map<String, String> row : rows) {
            if (userId.equals(row.get("userId")) && jobId.equals(row.get("jobId"))) {
                continue;
            }
            kept.add(row);
        }
        Map<String, String> add = new LinkedHashMap<>();
        add.put("userId", userId);
        add.put("jobId", jobId);
        add.put("viewedAt", Instant.now().toString());
        kept.add(add);
        store.writeMaps(RECENT_JSON, pruneUserHistory(kept, userId));
    }

    private static List<Map<String, String>> pruneUserHistory(List<Map<String, String>> rows, String userId) {
        List<Map<String, String>> userRows = rows.stream()
                .filter(row -> userId.equals(row.get("userId")))
                .sorted(Comparator.comparing(
                        (Map<String, String> row) -> row.getOrDefault("viewedAt", ""),
                        Comparator.reverseOrder()))
                .collect(Collectors.toList());

        List<String> keepIds = userRows.stream()
                .map(row -> row.get("jobId"))
                .filter(id -> id != null && !id.isEmpty())
                .distinct()
                .limit(MAX_RECENT)
                .collect(Collectors.toList());

        List<Map<String, String>> result = new ArrayList<>();
        for (Map<String, String> row : rows) {
            if (!userId.equals(row.get("userId"))) {
                result.add(row);
            }
        }
        for (String id : keepIds) {
            for (Map<String, String> row : userRows) {
                if (id.equals(row.get("jobId"))) {
                    result.add(row);
                    break;
                }
            }
        }
        return result;
    }
}

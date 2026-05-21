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
 * Per-user recently viewed jobs in {@code recently-viewed.json} (userId, jobId, viewedAt).
 * At most {@link #MAX_RECENT} jobs per user, most recent first.
 */
public class RecentlyViewedService {

    public static final int MAX_RECENT = 3;
    private static final String RECENT_JSON = "recently-viewed.json";
    private final FileStore store;

    public RecentlyViewedService(Path dataDir) {
        this.store = new FileStore(dataDir);
    }

    /** Most recently viewed job ids first (up to {@link #MAX_RECENT}). */
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

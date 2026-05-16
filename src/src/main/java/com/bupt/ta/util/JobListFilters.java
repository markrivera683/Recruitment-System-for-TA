package com.bupt.ta.util;

import com.bupt.ta.model.Job;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

/** Shared list search, sort, and favorited-only filter for job browsing. */
public final class JobListFilters {

    private JobListFilters() {
    }

    public static List<Job> apply(List<Job> published, Set<String> favoriteJobIds, String q, String sortBy) {
        String mode = safe(sortBy);
        if (mode.isEmpty()) {
            mode = "postingDate";
        }
        List<Job> jobs = published;
        if ("favorited".equals(mode)) {
            jobs = jobs.stream()
                    .filter(j -> j.getId() != null && favoriteJobIds.contains(j.getId()))
                    .collect(Collectors.toList());
            mode = "postingDate";
        }
        jobs = applySearch(jobs, q);
        return applySort(jobs, mode);
    }

    private static List<Job> applySearch(List<Job> jobs, String q) {
        if (q == null || q.isEmpty()) {
            return jobs;
        }
        String needle = q.toLowerCase(Locale.ROOT);
        return jobs.stream()
                .filter(j -> contains(j.getModuleName(), needle)
                        || contains(j.getActivityType(), needle)
                        || (j.getRequiredSkills() != null
                        && j.getRequiredSkills().stream().anyMatch(s -> contains(s, needle))))
                .collect(Collectors.toList());
    }

    private static List<Job> applySort(List<Job> jobs, String sortBy) {
        Comparator<Job> cmp;
        if ("moduleName".equals(sortBy)) {
            cmp = Comparator.comparing(j -> lower(j.getModuleName()));
        } else if ("activityType".equals(sortBy)) {
            cmp = Comparator.comparing(j -> lower(j.getActivityType()));
        } else {
            cmp = Comparator.comparing((Job j) -> lower(j.getPostDate())).reversed();
        }
        return jobs.stream().sorted(cmp).collect(Collectors.toList());
    }

    private static boolean contains(String s, String needle) {
        return s != null && s.toLowerCase(Locale.ROOT).contains(needle);
    }

    private static String lower(String s) {
        return s == null ? "" : s.toLowerCase(Locale.ROOT);
    }

    private static String safe(String s) {
        return s == null ? "" : s.trim();
    }
}

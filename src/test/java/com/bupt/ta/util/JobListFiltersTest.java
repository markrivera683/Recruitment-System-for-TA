package com.bupt.ta.util;

import com.bupt.ta.model.Job;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

class JobListFiltersTest {

    @Test
    void apply_searchMatchesModuleName() {
        List<Job> jobs = Arrays.asList(job("1", "Alpha", "Lab"), job("2", "Beta", "Tutorial"));
        List<Job> out = JobListFilters.apply(jobs, Collections.emptySet(), "alpha", "postingDate");
        assertEquals(1, out.size());
        assertEquals("1", out.get(0).getId());
    }

    @Test
    void apply_favoritedPartition_filtersToSavedIds() {
        List<Job> jobs = Arrays.asList(job("1", "A", "Lab"), job("2", "B", "Lab"));
        Set<String> fav = new HashSet<>(Collections.singletonList("2"));
        List<Job> out = JobListFilters.apply(jobs, fav, "", "favorited");
        assertEquals(1, out.size());
        assertEquals("2", out.get(0).getId());
    }

    @Test
    void promoteRecentlyViewed_putsRecentFirstInOrder() {
        List<Job> jobs = Arrays.asList(job("1", "A", "x"), job("2", "B", "x"), job("3", "C", "x"));
        List<Job> out = JobListFilters.promoteRecentlyViewed(jobs, Arrays.asList("3", "1"));
        assertEquals(Arrays.asList("3", "1", "2"), ids(out));
    }

    private static List<String> ids(List<Job> jobs) {
        return jobs.stream().map(Job::getId).collect(java.util.stream.Collectors.toList());
    }

    private static Job job(String id, String name, String type) {
        Job j = new Job();
        j.setId(id);
        j.setModuleName(name);
        j.setActivityType(type);
        j.setPostDate("2026-01-01");
        return j;
    }
}

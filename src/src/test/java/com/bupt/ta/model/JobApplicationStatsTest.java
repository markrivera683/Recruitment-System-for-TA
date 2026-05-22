package com.bupt.ta.model;

import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Tests application counts and job matching used on the job detail page. */
class JobApplicationStatsTest {

    @Test
    void forJob_countsByStatusForMatchingModule() {
        Application pending = app("1", "CS101", "CS101", "Pending");
        Application accepted = app("2", "CS101", "CS101", "Accepted");
        Application other = app("3", "MATH201", "MATH201", "Pending");

        JobApplicationStats stats = JobApplicationStats.forJob(
                Arrays.asList(pending, accepted, other), "CS101", "CS101");

        assertEquals(1, stats.pending);
        assertEquals(1, stats.accepted);
        assertEquals(2, stats.activeTotal());
    }

    @Test
    void matchesJob_requiresModuleNameAndCode() {
        Application a = app("1", "CS101", "CS101", "Pending");
        assertTrue(JobApplicationStats.matchesJob(a, "CS101", "CS101"));
        assertFalse(JobApplicationStats.matchesJob(a, "CS101", "CS102"));
        assertFalse(JobApplicationStats.matchesJob(a, "MATH201", "MATH201"));
    }

    @Test
    void parseCapacity_defaultsAndParsesNumericString() {
        assertEquals(1, JobApplicationStats.parseCapacity(null));
        assertEquals(1, JobApplicationStats.parseCapacity(""));
        assertEquals(3, JobApplicationStats.parseCapacity("3"));
        assertEquals(2, JobApplicationStats.parseCapacity("2 TAs"));
    }

    private static Application app(String id, String name, String code, String status) {
        Application a = new Application(id, "u1", name, code, "Lab Assistant", "2026-05-01");
        a.status = status;
        return a;
    }
}

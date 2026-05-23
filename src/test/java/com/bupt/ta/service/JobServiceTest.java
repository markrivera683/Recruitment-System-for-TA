package com.bupt.ta.service;

import com.bupt.ta.model.Job;
import com.bupt.ta.testsupport.TestFixtures;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JobServiceTest {

    @TempDir
    Path dataDir;

    private JobService newService() throws Exception {
        return new JobService(dataDir.resolve("jobs.json").toString());
    }

    @Test
    void getAllJobs_parsesSeedJson() throws Exception {
        TestFixtures.writeJobsJson(dataDir, TestFixtures.jobJsonSingle("j1", "Alpha", "A101", "Published", "2"));
        List<Job> jobs = newService().getAllJobs();
        assertEquals(1, jobs.size());
        assertEquals("Alpha", jobs.get(0).getModuleName());
        assertEquals("2", jobs.get(0).getNumberOfTAs());
    }

    @Test
    void getAllJobs_emptyFile_returnsEmpty() throws Exception {
        TestFixtures.writeJobsJson(dataDir, "[]");
        assertTrue(newService().getAllJobs().isEmpty());
    }

    @Test
    void getJobById_foundAndMissing() throws Exception {
        TestFixtures.writeJobsJson(dataDir, TestFixtures.jobJsonSingle("j1", "Alpha", "A101", "Published", "2"));
        JobService svc = newService();
        assertNotNull(svc.getJobById("j1"));
        assertNull(svc.getJobById("missing"));
        assertNull(svc.getJobById(null));
    }

    @Test
    void listPublishedJobs_excludesDraft() throws Exception {
        String json = "["
                + "{\"id\":\"j1\",\"moduleName\":\"Pub\",\"moduleCode\":\"P1\",\"activityType\":\"Lab\","
                + "\"requiredSkills\":[],\"description\":\"d\",\"postDate\":\"2026-01-01\","
                + "\"status\":\"Published\",\"numberOfTAs\":\"2\"},"
                + "{\"id\":\"j2\",\"moduleName\":\"Draft\",\"moduleCode\":\"D1\",\"activityType\":\"Lab\","
                + "\"requiredSkills\":[],\"description\":\"d\",\"postDate\":\"\",\"status\":\"Draft\","
                + "\"numberOfTAs\":\"1\"}"
                + "]";
        TestFixtures.writeJobsJson(dataDir, json);
        List<Job> published = newService().listPublishedJobs();
        assertEquals(1, published.size());
        assertEquals("Pub", published.get(0).getModuleName());
    }

    @Test
    void createJob_assignsIdAndDraftStatus() throws Exception {
        TestFixtures.writeJobsJson(dataDir, "[]");
        JobService svc = newService();
        Job j = new Job();
        j.setModuleName("New");
        j.setModuleCode("N1");
        j.setDescription("Desc");
        Job created = svc.createJob(j);
        assertNotNull(created.getId());
        assertEquals("Draft", created.getStatus());
    }

    @Test
    void createJob_publishedSetsDates() throws Exception {
        TestFixtures.writeJobsJson(dataDir, "[]");
        JobService svc = newService();
        Job j = new Job();
        j.setModuleName("Pub");
        j.setModuleCode("P1");
        j.setDescription("Desc");
        j.setStatus("Published");
        Job created = svc.createJob(j);
        assertFalse(created.getPostDate().isEmpty());
        assertFalse(created.getPublishedAt().isEmpty());
    }

    @Test
    void publishJob_changesStatusAndMoId() throws Exception {
        TestFixtures.writeJobsJson(dataDir,
                TestFixtures.jobJsonSingle("j1", "M", "C", "Draft", "2")
                        .replace("\"createdByMoId\": \"mo1\"", "\"createdByMoId\": \"\""));
        JobService svc = newService();
        assertTrue(svc.publishJob("j1", "mo-1"));
        Job j = svc.getJobById("j1");
        assertEquals("Published", j.getStatus());
        assertEquals("mo-1", j.getCreatedByMoId());
    }

    @Test
    void publishJob_unknownId_returnsFalse() throws Exception {
        TestFixtures.writeJobsJson(dataDir, "[]");
        assertFalse(newService().publishJob("missing", "mo"));
    }

    @Test
    void deleteJobById_removesFromFile() throws Exception {
        TestFixtures.writeJobsJson(dataDir, TestFixtures.jobJsonSingle("j1", "M", "C", "Published", "2"));
        JobService svc = newService();
        svc.deleteJobById("j1");
        assertNull(svc.getJobById("j1"));
    }

    @Test
    void getAllJobs_defaultScheduleForLab() throws Exception {
        String json = "[{\"id\":\"j1\",\"moduleName\":\"Lab\",\"moduleCode\":\"L1\",\"activityType\":\"Lab Assistant\","
                + "\"requiredSkills\":[],\"description\":\"d\",\"postDate\":\"2026-01-01\","
                + "\"applicationDeadline\":\"2026-12-31\",\"duration\":\"One semester\",\"numberOfTAs\":\"1\","
                + "\"status\":\"Published\",\"schedule\":[]}]";
        TestFixtures.writeJobsJson(dataDir, json);
        List<String> schedule = newService().getAllJobs().get(0).getSchedule();
        assertFalse(schedule.isEmpty());
        assertTrue(schedule.get(0).contains("Monday"));
    }

    @Test
    void getAllJobs_numberOfTAs_defaultsToTwo() throws Exception {
        String json = "[{\"id\":\"j1\",\"moduleName\":\"M\",\"moduleCode\":\"C\",\"activityType\":\"Lab\","
                + "\"requiredSkills\":[],\"description\":\"d\",\"postDate\":\"2026-01-01\","
                + "\"status\":\"Published\"}]";
        TestFixtures.writeJobsJson(dataDir, json);
        assertEquals("2", newService().getAllJobs().get(0).getNumberOfTAs());
    }
}

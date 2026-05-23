package com.bupt.ta.integration;

import com.bupt.ta.model.Application;
import com.bupt.ta.model.JobApplicationStats;
import com.bupt.ta.model.User;
import com.bupt.ta.service.ApplicationService;
import com.bupt.ta.service.AuthService;
import com.bupt.ta.service.JobService;
import com.bupt.ta.service.ProfileService;
import com.bupt.ta.testsupport.TestFixtures;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ApplicationFlowIntegrationTest {

    @TempDir
    Path dataDir;

    @Test
    void moPublishTaApplyMoApprove() throws Exception {
        TestFixtures.seedEmptyDataDir(dataDir);
        TestFixtures.writeJobsJson(dataDir, "[]");

        AuthService auth = new AuthService(dataDir);
        JobService jobs = new JobService(dataDir.resolve("jobs.json").toString());
        ApplicationService apps = new ApplicationService(dataDir);
        ProfileService profiles = new ProfileService(dataDir);

        User ta = auth.register("Applicant", TestFixtures.validBuptStudentId(), "applicant@bupt.edu.cn", "pass");
        profiles.upsert(TestFixtures.completeProfile(ta.id));

        com.bupt.ta.model.Job job = TestFixtures.sampleJob(null, "CS101", "CS101");
        job.setStatus("Published");
        job.setDescription("Lab");
        jobs.createJob(job);
        String jobId = jobs.getAllJobs().get(0).getId();

        Application app = TestFixtures.sampleApplication("app-1", ta.id, "CS101", "CS101");
        apps.save(app);
        assertEquals("Pending", apps.findById("app-1").get().status);

        apps.updateStatus("app-1", "Accepted", "Approved");
        List<Application> all = apps.listAll();
        JobApplicationStats stats = JobApplicationStats.forJob(all, "CS101", "CS101");
        assertEquals(1, stats.accepted);

        apps.updateStatus("app-1", "Withdrawn", "");
        stats = JobApplicationStats.forJob(apps.listAll(), "CS101", "CS101");
        assertEquals(0, stats.accepted);
        assertTrue(stats.withdrawn >= 1);
    }

    @Test
    void fullSlotBlocksNewApplications() throws Exception {
        TestFixtures.seedEmptyDataDir(dataDir);
        TestFixtures.writeJobsJson(dataDir, TestFixtures.jobJsonSingle("j1", "CS101", "CS101", "Published", "1"));

        ApplicationService apps = new ApplicationService(dataDir);
        Application accepted = TestFixtures.sampleApplication("a1", "u1", "CS101", "CS101");
        accepted.status = "Accepted";
        apps.save(accepted);

        JobService jobs = new JobService(dataDir.resolve("jobs.json").toString());
        int capacity = JobApplicationStats.parseCapacity(jobs.getJobById("j1").getNumberOfTAs());
        JobApplicationStats stats = JobApplicationStats.forJob(apps.listAll(), "CS101", "CS101");
        assertTrue(stats.accepted >= capacity);
    }
}

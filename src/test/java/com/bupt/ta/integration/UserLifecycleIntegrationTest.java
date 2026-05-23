package com.bupt.ta.integration;

import com.bupt.ta.model.User;
import com.bupt.ta.service.ApplicationService;
import com.bupt.ta.service.AuthService;
import com.bupt.ta.service.JobService;
import com.bupt.ta.service.ProfileService;
import com.bupt.ta.testsupport.TestFixtures;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UserLifecycleIntegrationTest {

    @TempDir
    Path dataDir;

    @Test
    void registerLoginApplyAndDeleteUser() throws Exception {
        TestFixtures.seedEmptyDataDir(dataDir);
        TestFixtures.writeJobsJson(dataDir, TestFixtures.jobJsonSingle("j1", "CS101", "CS101", "Published", "2"));

        AuthService auth = new AuthService(dataDir);
        ProfileService profiles = new ProfileService(dataDir);
        ApplicationService apps = new ApplicationService(dataDir);
        JobService jobs = new JobService(dataDir.resolve("jobs.json").toString());

        User u = auth.register("Integration User", TestFixtures.validBuptStudentId(),
                "integration@bupt.edu.cn", "pass123");
        profiles.upsert(TestFixtures.completeProfile(u.id));
        assertTrue(auth.login("integration@bupt.edu.cn", "pass123").isPresent());

        apps.save(TestFixtures.sampleApplication("app-1", u.id, "CS101", "CS101"));
        assertEquals(1, apps.getByUserId(u.id).size());
        assertTrue(jobs.getJobById("j1") != null);

        auth.setUserActive(u.id, false);
        assertFalse(auth.login("integration@bupt.edu.cn", "pass123").isPresent());
        auth.setUserActive(u.id, true);
        assertTrue(auth.login("integration@bupt.edu.cn", "pass123").isPresent());

        apps.deleteByUserId(u.id);
        profiles.deleteByUserId(u.id);
        auth.removeUserRecord(u.id);
        assertFalse(auth.findById(u.id).isPresent());
        assertTrue(apps.getByUserId(u.id).isEmpty());
        assertFalse(profiles.getByUserId(u.id).isPresent());
    }
}

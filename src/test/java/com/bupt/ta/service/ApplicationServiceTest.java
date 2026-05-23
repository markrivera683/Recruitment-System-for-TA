package com.bupt.ta.service;

import com.bupt.ta.model.Application;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Component tests for TA job application persistence. */
class ApplicationServiceTest {

    @TempDir
    Path dataDir;

    @Test
    void saveAndGetByUserId_returnsSubmittedApplication() throws Exception {
        seedEmptyApplications();
        ApplicationService svc = new ApplicationService(dataDir);

        Application app = new Application("app-1", "user-1", "CS101", "CS101", "Lab Assistant", "2026-05-01");
        svc.save(app);

        List<Application> mine = svc.getByUserId("user-1");
        assertEquals(1, mine.size());
        assertEquals("CS101", mine.get(0).moduleName);
        assertEquals("Pending", mine.get(0).status);
    }

    @Test
    void updateStatus_changesStoredStatusAndFeedback() throws Exception {
        seedEmptyApplications();
        ApplicationService svc = new ApplicationService(dataDir);
        svc.save(new Application("app-2", "user-2", "MATH201", "MATH201", "Tutorial", "2026-05-02"));

        svc.updateStatus("app-2", "Accepted", "Welcome aboard");

        Optional<Application> found = svc.findById("app-2");
        assertTrue(found.isPresent());
        assertEquals("Accepted", found.get().status);
        assertEquals("Welcome aboard", found.get().feedback);
    }

    @Test
    void getByUserId_doesNotReturnOtherUsersApplications() throws Exception {
        seedEmptyApplications();
        ApplicationService svc = new ApplicationService(dataDir);
        svc.save(new Application("a1", "user-a", "ENG101", "ENG101", "TA", "2026-05-01"));
        svc.save(new Application("a2", "user-b", "PHY150", "PHY150", "TA", "2026-05-01"));

        assertEquals(1, svc.getByUserId("user-a").size());
        assertEquals("ENG101", svc.getByUserId("user-a").get(0).moduleName);
    }

    private void seedEmptyApplications() throws Exception {
        Files.write(dataDir.resolve("applications.json"), "[]".getBytes(StandardCharsets.UTF_8));
    }
}

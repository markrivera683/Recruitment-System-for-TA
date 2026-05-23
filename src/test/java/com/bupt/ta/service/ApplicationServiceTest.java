package com.bupt.ta.service;

import com.bupt.ta.model.Application;
import com.bupt.ta.testsupport.TestFixtures;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ApplicationServiceTest {

    @TempDir
    Path dataDir;

    @Test
    void saveAndGetByUserId_returnsSubmittedApplication() throws Exception {
        TestFixtures.seedEmptyDataDir(dataDir);
        ApplicationService svc = new ApplicationService(dataDir);
        Application app = TestFixtures.sampleApplication("app-1", "user-1", "CS101", "CS101");
        svc.save(app);
        List<Application> mine = svc.getByUserId("user-1");
        assertEquals(1, mine.size());
        assertEquals("CS101", mine.get(0).moduleName);
        assertEquals("Pending", mine.get(0).status);
    }

    @Test
    void updateStatus_changesStoredStatusAndFeedback() throws Exception {
        TestFixtures.seedEmptyDataDir(dataDir);
        ApplicationService svc = new ApplicationService(dataDir);
        svc.save(TestFixtures.sampleApplication("app-2", "user-2", "MATH201", "MATH201"));
        svc.updateStatus("app-2", "Accepted", "Welcome aboard");
        Optional<Application> found = svc.findById("app-2");
        assertTrue(found.isPresent());
        assertEquals("Accepted", found.get().status);
        assertEquals("Welcome aboard", found.get().feedback);
    }

    @Test
    void getByUserId_doesNotReturnOtherUsersApplications() throws Exception {
        TestFixtures.seedEmptyDataDir(dataDir);
        ApplicationService svc = new ApplicationService(dataDir);
        svc.save(TestFixtures.sampleApplication("a1", "user-a", "ENG101", "ENG101"));
        svc.save(TestFixtures.sampleApplication("a2", "user-b", "PHY150", "PHY150"));
        assertEquals(1, svc.getByUserId("user-a").size());
    }

    @Test
    void listAll_returnsAllApplications() throws Exception {
        TestFixtures.seedEmptyDataDir(dataDir);
        ApplicationService svc = new ApplicationService(dataDir);
        svc.save(TestFixtures.sampleApplication("a1", "u1", "A", "A"));
        svc.save(TestFixtures.sampleApplication("a2", "u2", "B", "B"));
        assertEquals(2, svc.listAll().size());
    }

    @Test
    void findById_nullOrEmpty_returnsEmpty() throws Exception {
        TestFixtures.seedEmptyDataDir(dataDir);
        ApplicationService svc = new ApplicationService(dataDir);
        assertFalse(svc.findById(null).isPresent());
        assertFalse(svc.findById("").isPresent());
    }

    @Test
    void save_upsertsSameId() throws Exception {
        TestFixtures.seedEmptyDataDir(dataDir);
        ApplicationService svc = new ApplicationService(dataDir);
        Application a = TestFixtures.sampleApplication("same", "u1", "CS101", "CS101");
        svc.save(a);
        a.status = "Accepted";
        svc.save(a);
        assertEquals("Accepted", svc.findById("same").get().status);
    }

    @Test
    void updateStatus_unknownId_noOp() throws Exception {
        TestFixtures.seedEmptyDataDir(dataDir);
        ApplicationService svc = new ApplicationService(dataDir);
        svc.updateStatus("missing", "Accepted", "x");
        assertEquals(0, svc.listAll().size());
    }

    @Test
    void deleteByUserId_removesAllForUser() throws Exception {
        TestFixtures.seedEmptyDataDir(dataDir);
        ApplicationService svc = new ApplicationService(dataDir);
        svc.save(TestFixtures.sampleApplication("a1", "u1", "A", "A"));
        svc.save(TestFixtures.sampleApplication("a2", "u1", "B", "B"));
        svc.deleteByUserId("u1");
        assertEquals(0, svc.getByUserId("u1").size());
    }

    @Test
    void getByUserId_nullUserId_returnsEmpty() throws Exception {
        TestFixtures.seedEmptyDataDir(dataDir);
        assertTrue(new ApplicationService(dataDir).getByUserId(null).isEmpty());
    }
}

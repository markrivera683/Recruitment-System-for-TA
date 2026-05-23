package com.bupt.ta.service;

import com.bupt.ta.testsupport.TestFixtures;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RecentlyViewedServiceTest {

    @TempDir
    Path dataDir;

    @Test
    void recordView_keepsAtMostThreeMostRecent() throws Exception {
        TestFixtures.seedEmptyDataDir(dataDir);
        RecentlyViewedService svc = new RecentlyViewedService(dataDir);
        svc.recordView("u1", "a");
        svc.recordView("u1", "b");
        svc.recordView("u1", "c");
        svc.recordView("u1", "d");
        List<String> recent = svc.getRecentJobIds("u1");
        assertEquals(3, recent.size());
        assertEquals("d", recent.get(0));
    }

    @Test
    void recordView_revisitMovesJobToFront() throws Exception {
        TestFixtures.seedEmptyDataDir(dataDir);
        RecentlyViewedService svc = new RecentlyViewedService(dataDir);
        svc.recordView("u1", "a");
        svc.recordView("u1", "b");
        svc.recordView("u1", "a");
        assertEquals("a", svc.getRecentJobIds("u1").get(0));
    }

    @Test
    void getRecentJobIds_nullUserId_returnsEmpty() throws Exception {
        TestFixtures.seedEmptyDataDir(dataDir);
        assertTrue(new RecentlyViewedService(dataDir).getRecentJobIds(null).isEmpty());
    }

    @Test
    void recordView_nullJobId_noOp() throws Exception {
        TestFixtures.seedEmptyDataDir(dataDir);
        RecentlyViewedService svc = new RecentlyViewedService(dataDir);
        svc.recordView("u1", null);
        assertTrue(svc.getRecentJobIds("u1").isEmpty());
    }

    @Test
    void viewsAreScopedPerUser() throws Exception {
        TestFixtures.seedEmptyDataDir(dataDir);
        RecentlyViewedService svc = new RecentlyViewedService(dataDir);
        svc.recordView("u1", "j1");
        svc.recordView("u2", "j2");
        assertEquals("j1", svc.getRecentJobIds("u1").get(0));
        assertEquals("j2", svc.getRecentJobIds("u2").get(0));
    }
}

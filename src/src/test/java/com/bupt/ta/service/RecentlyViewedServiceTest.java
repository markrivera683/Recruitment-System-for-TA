package com.bupt.ta.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RecentlyViewedServiceTest {

    @TempDir
    Path dataDir;

    @Test
    void recordView_keepsAtMostThreeMostRecent() throws Exception {
        Files.write(dataDir.resolve("recently-viewed.json"), "[]".getBytes(java.nio.charset.StandardCharsets.UTF_8));
        RecentlyViewedService svc = new RecentlyViewedService(dataDir);

        svc.recordView("u1", "a");
        svc.recordView("u1", "b");
        svc.recordView("u1", "c");
        svc.recordView("u1", "d");

        List<String> recent = svc.getRecentJobIds("u1");
        assertEquals(3, recent.size());
        assertEquals("d", recent.get(0));
        assertTrue(recent.contains("b"));
        assertTrue(recent.contains("c"));
    }

    @Test
    void recordView_revisitMovesJobToFront() throws Exception {
        Files.write(dataDir.resolve("recently-viewed.json"), "[]".getBytes(java.nio.charset.StandardCharsets.UTF_8));
        RecentlyViewedService svc = new RecentlyViewedService(dataDir);

        svc.recordView("u1", "a");
        svc.recordView("u1", "b");
        svc.recordView("u1", "a");

        assertEquals("a", svc.getRecentJobIds("u1").get(0));
    }
}

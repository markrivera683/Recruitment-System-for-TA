package com.bupt.ta.service;

import com.bupt.ta.testsupport.TestFixtures;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FavoriteServiceTest {

    @TempDir
    Path dataDir;

    @Test
    void toggleFavorite_addThenRemove() throws Exception {
        TestFixtures.seedEmptyDataDir(dataDir);
        FavoriteService svc = new FavoriteService(dataDir);
        assertTrue(svc.toggleFavorite("u1", "job-1"));
        assertTrue(svc.isFavorite("u1", "job-1"));
        assertFalse(svc.toggleFavorite("u1", "job-1"));
        assertFalse(svc.isFavorite("u1", "job-1"));
    }

    @Test
    void favoritesAreScopedPerUser() throws Exception {
        TestFixtures.seedEmptyDataDir(dataDir);
        FavoriteService svc = new FavoriteService(dataDir);
        svc.toggleFavorite("u1", "job-1");
        assertTrue(svc.isFavorite("u1", "job-1"));
        assertFalse(svc.isFavorite("u2", "job-1"));
    }

    @Test
    void getFavoriteJobIds_returnsMultipleInOrder() throws Exception {
        TestFixtures.seedEmptyDataDir(dataDir);
        FavoriteService svc = new FavoriteService(dataDir);
        svc.toggleFavorite("u1", "j1");
        svc.toggleFavorite("u1", "j2");
        Set<String> ids = svc.getFavoriteJobIds("u1");
        assertEquals(2, ids.size());
        assertTrue(ids.contains("j1"));
        assertTrue(ids.contains("j2"));
    }

    @Test
    void getFavoriteJobIds_nullUserId_returnsEmpty() throws Exception {
        TestFixtures.seedEmptyDataDir(dataDir);
        assertTrue(new FavoriteService(dataDir).getFavoriteJobIds(null).isEmpty());
    }

    @Test
    void isFavorite_nullInputs_false() throws Exception {
        TestFixtures.seedEmptyDataDir(dataDir);
        FavoriteService svc = new FavoriteService(dataDir);
        assertFalse(svc.isFavorite(null, "j1"));
        assertFalse(svc.isFavorite("u1", null));
    }

    @Test
    void toggleFavorite_threeToggles() throws Exception {
        TestFixtures.seedEmptyDataDir(dataDir);
        FavoriteService svc = new FavoriteService(dataDir);
        assertTrue(svc.toggleFavorite("u1", "j1"));
        assertFalse(svc.toggleFavorite("u1", "j1"));
        assertTrue(svc.toggleFavorite("u1", "j1"));
    }
}

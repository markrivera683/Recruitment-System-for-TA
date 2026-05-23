package com.bupt.ta.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FavoriteServiceTest {

    @TempDir
    Path dataDir;

    @Test
    void toggleFavorite_addThenRemove() throws Exception {
        Files.write(dataDir.resolve("favorites.json"), "[]".getBytes(java.nio.charset.StandardCharsets.UTF_8));
        FavoriteService svc = new FavoriteService(dataDir);

        assertTrue(svc.toggleFavorite("u1", "job-1"));
        assertTrue(svc.isFavorite("u1", "job-1"));

        assertFalse(svc.toggleFavorite("u1", "job-1"));
        assertFalse(svc.isFavorite("u1", "job-1"));
    }

    @Test
    void favoritesAreScopedPerUser() throws Exception {
        Files.write(dataDir.resolve("favorites.json"), "[]".getBytes(java.nio.charset.StandardCharsets.UTF_8));
        FavoriteService svc = new FavoriteService(dataDir);

        svc.toggleFavorite("u1", "job-1");
        assertTrue(svc.isFavorite("u1", "job-1"));
        assertFalse(svc.isFavorite("u2", "job-1"));
    }
}

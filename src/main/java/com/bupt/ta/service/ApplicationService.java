package com.bupt.ta.service;

import com.bupt.ta.model.Application;

import java.io.IOException;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service for managing TA job applications persisted in {@code applications.json}.
 * <p>
 * Each application is stored as a flat string map and mapped to {@link Application} objects.
 * Supports listing, lookup, creation/update, status changes, and bulk deletion by applicant.
 */
public class ApplicationService {
    private static final String FILE = "applications.json";
    private final FileStore store;

    /**
     * Creates a service backed by JSON files in the given data directory.
     *
     * @param dataDir root directory containing {@code applications.json}
     */
    public ApplicationService(Path dataDir) {
        this.store = new FileStore(dataDir);
    }

    private static Application mapToApp(Map<String, String> m) {
        Application a = new Application();
        a.id              = m.getOrDefault("id", "");
        a.userId          = m.getOrDefault("userId", "");
        a.moduleName      = m.getOrDefault("moduleName", "");
        a.moduleCode      = m.getOrDefault("moduleCode", "");
        a.role            = m.getOrDefault("role", "");
        a.applicationDate = m.getOrDefault("applicationDate", "");
        a.status          = m.getOrDefault("status", "Pending");
        a.feedback        = m.getOrDefault("feedback", "");
        return a;
    }

    private static Map<String, String> appToMap(Application a) {
        Map<String, String> m = new LinkedHashMap<>();
        m.put("id",              a.id              != null ? a.id              : "");
        m.put("userId",          a.userId          != null ? a.userId          : "");
        m.put("moduleName",      a.moduleName      != null ? a.moduleName      : "");
        m.put("moduleCode",      a.moduleCode      != null ? a.moduleCode      : "");
        m.put("role",            a.role            != null ? a.role            : "");
        m.put("applicationDate", a.applicationDate != null ? a.applicationDate : "");
        m.put("status",          a.status          != null ? a.status          : "Pending");
        m.put("feedback",        a.feedback        != null ? a.feedback        : "");
        return m;
    }

    /**
     * Returns all applications submitted by the given user, in file order.
     *
     * @param userId applicant user id; {@code null} yields an empty list
     * @return applications whose {@code userId} matches; never {@code null}
     * @throws IOException if {@code applications.json} cannot be read
     */
    public List<Application> getByUserId(String userId) throws IOException {
        return store.readMaps(FILE).stream()
                    .filter(m -> userId != null && userId.equals(m.get("userId")))
                    .map(ApplicationService::mapToApp)
                    .collect(Collectors.toList());
    }

    /**
     * Returns every application in the store (admin / reporting use).
     *
     * @return all applications; never {@code null}
     * @throws IOException if {@code applications.json} cannot be read
     */
    public List<Application> listAll() throws IOException {
        return store.readMaps(FILE).stream()
                    .map(ApplicationService::mapToApp)
                    .collect(Collectors.toList());
    }

    /**
     * Looks up a single application by its unique id.
     *
     * @param appId application id; {@code null} or empty returns {@link Optional#empty()}
     * @return the matching application, if present
     * @throws IOException if {@code applications.json} cannot be read
     */
    public Optional<Application> findById(String appId) throws IOException {
        if (appId == null || appId.isEmpty()) {
            return Optional.empty();
        }
        return store.readMaps(FILE).stream()
                .filter(m -> appId.equals(m.get("id")))
                .map(ApplicationService::mapToApp)
                .findFirst();
    }

    /**
     * Inserts or replaces an application row keyed by {@link Application#id}.
     *
     * @param app application to persist; existing row with the same id is removed first
     * @return the same {@code app} instance after persistence
     * @throws IOException if the file cannot be read or written
     */
    public Application save(Application app) throws IOException {
        List<Map<String, String>> rows = store.readMaps(FILE);
        rows.removeIf(m -> app.id != null && app.id.equals(m.get("id")));
        rows.add(appToMap(app));
        store.writeMaps(FILE, rows);
        return app;
    }

    /**
     * Updates status and feedback for the application with the given id.
     * <p>
     * If no row matches, the file is still rewritten unchanged. Null status defaults to
     * {@code "Pending"}; null feedback defaults to an empty string.
     *
     * @param appId    target application id
     * @param status   new status (e.g. Accepted, Rejected, Pending)
     * @param feedback optional reviewer feedback shown to the applicant
     * @throws IOException if the file cannot be read or written
     */
    public void updateStatus(String appId, String status, String feedback) throws IOException {
        List<Map<String, String>> rows = store.readMaps(FILE);
        for (Map<String, String> m : rows) {
            if (appId != null && appId.equals(m.get("id"))) {
                m.put("status", status != null ? status : "Pending");
                m.put("feedback", feedback != null ? feedback : "");
                break;
            }
        }
        store.writeMaps(FILE, rows);
    }

    /**
     * Removes all applications submitted by the given user.
     * <p>
     * Typically invoked when an admin deletes a user account and related data must be purged.
     * No-op if {@code userId} is {@code null} or empty.
     *
     * @param userId applicant user id whose applications should be deleted
     * @throws IOException if the file cannot be read or written
     */
    public void deleteByUserId(String userId) throws IOException {
        if (userId == null || userId.isEmpty()) return;
        List<Map<String, String>> rows = store.readMaps(FILE);
        rows.removeIf(m -> userId.equals(m.get("userId")));
        store.writeMaps(FILE, rows);
    }
}

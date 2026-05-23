package com.bupt.ta.service;

import com.bupt.ta.model.Application;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service for managing TA job applications persisted in {@code applications.json}.
 */
public class ApplicationService {

    private static final String APPLICATIONS_JSON = "applications.json";
    private final FileStore store;

    public ApplicationService(Path dataDir) {
        this.store = new FileStore(dataDir);
    }

    private static Application mapToApplication(Map<String, String> m) {
        Application a = new Application();
        a.id = m.getOrDefault("id", "");
        a.userId = m.getOrDefault("userId", "");
        a.moduleName = m.getOrDefault("moduleName", "");
        a.moduleCode = m.getOrDefault("moduleCode", "");
        a.role = m.getOrDefault("role", "");
        a.applicationDate = m.getOrDefault("applicationDate", "");
        a.status = m.getOrDefault("status", "Pending");
        if (a.status.isEmpty()) {
            a.status = "Pending";
        }
        a.feedback = m.getOrDefault("feedback", "");
        return a;
    }

    private static Map<String, String> applicationToMap(Application a) {
        Map<String, String> m = new LinkedHashMap<>();
        m.put("id", n(a.id));
        m.put("userId", n(a.userId));
        m.put("moduleName", n(a.moduleName));
        m.put("moduleCode", n(a.moduleCode));
        m.put("role", n(a.role));
        m.put("applicationDate", n(a.applicationDate));
        m.put("status", n(a.status));
        m.put("feedback", n(a.feedback));
        return m;
    }

    private List<Application> loadAll() throws IOException {
        return store.readMaps(APPLICATIONS_JSON).stream()
                .map(ApplicationService::mapToApplication)
                .collect(Collectors.toList());
    }

    private void saveAll(List<Application> apps) throws IOException {
        List<Map<String, String>> rows = new ArrayList<>();
        for (Application a : apps) {
            rows.add(applicationToMap(a));
        }
        store.writeMaps(APPLICATIONS_JSON, rows);
    }

    public List<Application> getByUserId(String userId) throws IOException {
        if (userId == null) {
            return new ArrayList<>();
        }
        return loadAll().stream()
                .filter(a -> userId.equals(a.userId))
                .collect(Collectors.toList());
    }

    public List<Application> listAll() throws IOException {
        return loadAll();
    }

    public Optional<Application> findById(String appId) throws IOException {
        if (appId == null || appId.isEmpty()) {
            return Optional.empty();
        }
        return loadAll().stream()
                .filter(a -> appId.equals(a.id))
                .findFirst();
    }

    public Application save(Application app) throws IOException {
        if (app.id == null || app.id.isEmpty()) {
            app.id = UUID.randomUUID().toString();
        }
        List<Application> apps = loadAll();
        apps.removeIf(a -> app.id.equals(a.id));
        apps.add(app);
        saveAll(apps);
        return app;
    }

    public void updateStatus(String appId, String status, String feedback) throws IOException {
        List<Application> apps = loadAll();
        for (Application a : apps) {
            if (appId.equals(a.id)) {
                a.status = status != null ? status : "Pending";
                a.feedback = feedback != null ? feedback : "";
                saveAll(apps);
                return;
            }
        }
    }

    public void deleteByUserId(String userId) throws IOException {
        if (userId == null || userId.isEmpty()) {
            return;
        }
        List<Application> apps = loadAll();
        apps.removeIf(a -> userId.equals(a.userId));
        saveAll(apps);
    }

    private static String n(String s) {
        return s != null ? s : "";
    }
}

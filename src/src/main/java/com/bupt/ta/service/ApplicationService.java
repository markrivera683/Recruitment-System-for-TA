package com.bupt.ta.service;

import com.bupt.ta.model.Application;

import java.io.IOException;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class ApplicationService {
    private static final String FILE = "applications.json";
    private final FileStore store;

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

    public List<Application> getByUserId(String userId) throws IOException {
        return store.readMaps(FILE).stream()
                    .filter(m -> userId != null && userId.equals(m.get("userId")))
                    .map(ApplicationService::mapToApp)
                    .collect(Collectors.toList());
    }

    public List<Application> listAll() throws IOException {
        return store.readMaps(FILE).stream()
                    .map(ApplicationService::mapToApp)
                    .collect(Collectors.toList());
    }

    public Optional<Application> findById(String appId) throws IOException {
        if (appId == null || appId.isEmpty()) {
            return Optional.empty();
        }
        return store.readMaps(FILE).stream()
                .filter(m -> appId.equals(m.get("id")))
                .map(ApplicationService::mapToApp)
                .findFirst();
    }

    public Application save(Application app) throws IOException {
        List<Map<String, String>> rows = store.readMaps(FILE);
        rows.removeIf(m -> app.id != null && app.id.equals(m.get("id")));
        rows.add(appToMap(app));
        store.writeMaps(FILE, rows);
        return app;
    }

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

    /** Remove all applications submitted by this user (e.g. admin account deletion). */
    public void deleteByUserId(String userId) throws IOException {
        if (userId == null || userId.isEmpty()) return;
        List<Map<String, String>> rows = store.readMaps(FILE);
        rows.removeIf(m -> userId.equals(m.get("userId")));
        store.writeMaps(FILE, rows);
    }
}

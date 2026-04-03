package com.bupt.ta.service;

import com.bupt.ta.model.Roles;
import com.bupt.ta.model.User;

import java.io.IOException;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

public class AuthService {
    private static final String USERS_JSON = "users.json";
    private final FileStore store;

    public AuthService(Path dataDir) {
        this.store = new FileStore(dataDir);
    }

    // ---------- helpers: Map <-> User

    private static User mapToUser(Map<String, String> m) {
        User u = new User();
        u.id           = m.getOrDefault("id", "");
        u.name         = m.getOrDefault("name", "");
        u.studentId    = m.getOrDefault("studentId", "");
        u.email        = m.getOrDefault("email", "");
        u.passwordHash = m.getOrDefault("passwordHash", "");
        u.role         = m.getOrDefault("role", Roles.TA);
        u.active       = parseBool(m.get("active"), true);
        return u;
    }

    private static boolean parseBool(String s, boolean defaultValue) {
        if (s == null || s.trim().isEmpty()) return defaultValue;
        return "true".equalsIgnoreCase(s.trim()) || "1".equals(s.trim());
    }

    private static Map<String, String> userToMap(User u) {
        Map<String, String> m = new LinkedHashMap<>();
        m.put("id",           u.id           != null ? u.id           : "");
        m.put("name",         u.name         != null ? u.name         : "");
        m.put("studentId",    u.studentId    != null ? u.studentId    : "");
        m.put("email",        u.email        != null ? u.email        : "");
        m.put("passwordHash", u.passwordHash != null ? u.passwordHash : "");
        m.put("role",         u.role         != null ? u.role         : Roles.TA);
        m.put("active",       u.active ? "true" : "false");
        return m;
    }

    // ---------- API

    public Optional<User> findByEmail(String email) throws IOException {
        List<Map<String, String>> rows = store.readMaps(USERS_JSON);
        return rows.stream()
                   .filter(m -> email != null && email.equalsIgnoreCase(m.get("email")))
                   .map(AuthService::mapToUser)
                   .findFirst();
    }

    public Optional<User> findById(String id) throws IOException {
        if (id == null || id.isEmpty()) return Optional.empty();
        List<Map<String, String>> rows = store.readMaps(USERS_JSON);
        return rows.stream()
                   .filter(m -> id.equals(m.get("id")))
                   .map(AuthService::mapToUser)
                   .findFirst();
    }

    public User register(String name, String studentId, String email, String password) throws IOException {
        List<Map<String, String>> rows = store.readMaps(USERS_JSON);
        boolean exists = rows.stream()
                             .anyMatch(m -> email != null && email.equalsIgnoreCase(m.get("email")));
        if (exists) throw new IllegalArgumentException("Email already registered");

        String id = UUID.randomUUID().toString();
        User u = new User(id, name, studentId, email, password);
        rows.add(userToMap(u));
        store.writeMaps(USERS_JSON, rows);
        return u;
    }

    public Optional<User> login(String email, String password) throws IOException {
        Optional<User> u = findByEmail(email);
        if (!u.isPresent()) return Optional.empty();
        User user = u.get();
        if (password == null || !password.equals(user.passwordHash)) return Optional.empty();
        if (!user.active) return Optional.empty();
        return u;
    }

    /** Set account active flag and persist. */
    public void setUserActive(String userId, boolean active) throws IOException {
        List<Map<String, String>> rows = store.readMaps(USERS_JSON);
        for (int i = 0; i < rows.size(); i++) {
            Map<String, String> m = rows.get(i);
            if (userId != null && userId.equals(m.get("id"))) {
                User u = mapToUser(m);
                u.active = active;
                rows.set(i, userToMap(u));
                store.writeMaps(USERS_JSON, rows);
                return;
            }
        }
        throw new IllegalStateException("User not found: " + userId);
    }

    /** Remove user row from users.json only (caller cleans related data). */
    public void removeUserRecord(String userId) throws IOException {
        List<Map<String, String>> rows = store.readMaps(USERS_JSON);
        boolean removed = rows.removeIf(m -> userId != null && userId.equals(m.get("id")));
        if (!removed) throw new IllegalStateException("User not found: " + userId);
        store.writeMaps(USERS_JSON, rows);
    }

    /** Count users with ADMIN role (for protecting last admin). */
    public long countAdmins() throws IOException {
        return store.readMaps(USERS_JSON).stream()
                .map(AuthService::mapToUser)
                .filter(u -> Roles.ADMIN.equals(u.role))
                .count();
    }

    /**
     * Updates name / student id / email for an existing user. Email must stay unique.
     */
    public void updateUserBasics(User user, String newName, String newStudentId, String newEmail)
            throws IOException {
        if (user == null || user.id == null || user.id.isEmpty()) {
            throw new IllegalArgumentException("Invalid user");
        }
        String email = newEmail != null ? newEmail.trim() : "";
        if (email.isEmpty()) throw new IllegalArgumentException("Email is required");

        if (user.email == null || !user.email.equalsIgnoreCase(email)) {
            Optional<User> other = findByEmail(email);
            if (other.isPresent() && !user.id.equals(other.get().id)) {
                throw new IllegalArgumentException("This email is already registered to another account.");
            }
        }

        user.name = newName != null ? newName.trim() : "";
        user.studentId = newStudentId != null ? newStudentId.trim() : "";
        user.email = email;

        List<Map<String, String>> rows = store.readMaps(USERS_JSON);
        boolean found = false;
        for (int i = 0; i < rows.size(); i++) {
            Map<String, String> m = rows.get(i);
            if (user.id.equals(m.get("id"))) {
                rows.set(i, userToMap(user));
                found = true;
                break;
            }
        }
        if (!found) throw new IllegalStateException("User record not found");
        store.writeMaps(USERS_JSON, rows);
    }

    /** All registered users (for admin views). */
    public List<User> listAllUsers() throws IOException {
        return store.readMaps(USERS_JSON).stream()
                    .map(AuthService::mapToUser)
                    .collect(Collectors.toList());
    }
}

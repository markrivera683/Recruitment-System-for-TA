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
        return u;
    }

    private static Map<String, String> userToMap(User u) {
        Map<String, String> m = new LinkedHashMap<>();
        m.put("id",           u.id           != null ? u.id           : "");
        m.put("name",         u.name         != null ? u.name         : "");
        m.put("studentId",    u.studentId    != null ? u.studentId    : "");
        m.put("email",        u.email        != null ? u.email        : "");
        m.put("passwordHash", u.passwordHash != null ? u.passwordHash : "");
        m.put("role",         u.role         != null ? u.role         : Roles.TA);
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
        return (password != null && password.equals(u.get().passwordHash))
               ? u : Optional.empty();
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

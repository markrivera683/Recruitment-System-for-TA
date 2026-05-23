package com.bupt.ta.service;

import com.bupt.ta.model.Roles;
import com.bupt.ta.model.User;
import com.bupt.ta.security.PasswordHasher;

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
 * Authentication and user-account persistence backed by {@code users.json}.
 */
public class AuthService {

    private static final String USERS_JSON = "users.json";
    private final FileStore store;

    public AuthService(Path dataDir) {
        this.store = new FileStore(dataDir);
    }

    private static User mapToUser(Map<String, String> m) {
        User u = new User();
        u.id = m.getOrDefault("id", "");
        u.name = m.getOrDefault("name", "");
        u.studentId = m.getOrDefault("studentId", "");
        u.email = m.getOrDefault("email", "");
        u.passwordHash = m.getOrDefault("passwordHash", "");
        u.role = m.getOrDefault("role", Roles.TA);
        u.active = parseBool(m.get("active"), true);
        return u;
    }

    private static boolean parseBool(String s, boolean defaultValue) {
        if (s == null || s.trim().isEmpty()) {
            return defaultValue;
        }
        return "true".equalsIgnoreCase(s.trim()) || "1".equals(s.trim());
    }

    private static Map<String, String> userToMap(User u) {
        Map<String, String> m = new LinkedHashMap<>();
        m.put("id", u.id != null ? u.id : "");
        m.put("name", u.name != null ? u.name : "");
        m.put("studentId", u.studentId != null ? u.studentId : "");
        m.put("email", u.email != null ? u.email : "");
        m.put("passwordHash", u.passwordHash != null ? u.passwordHash : "");
        m.put("role", u.role != null ? u.role : Roles.TA);
        m.put("active", u.active ? "true" : "false");
        return m;
    }

    private List<User> loadAll() throws IOException {
        return store.readMaps(USERS_JSON).stream()
                .map(AuthService::mapToUser)
                .collect(Collectors.toList());
    }

    private void saveAll(List<User> users) throws IOException {
        List<Map<String, String>> rows = new ArrayList<>();
        for (User u : users) {
            rows.add(userToMap(u));
        }
        store.writeMaps(USERS_JSON, rows);
    }

    public Optional<User> findByEmail(String email) throws IOException {
        if (email == null || email.trim().isEmpty()) {
            return Optional.empty();
        }
        return loadAll().stream()
                .filter(u -> email.equalsIgnoreCase(u.email))
                .findFirst();
    }

    public Optional<User> findById(String id) throws IOException {
        if (id == null || id.isEmpty()) {
            return Optional.empty();
        }
        return loadAll().stream()
                .filter(u -> id.equals(u.id))
                .findFirst();
    }

    public User register(String name, String studentId, String email, String password) throws IOException {
        if (findByEmail(email).isPresent()) {
            throw new IllegalArgumentException("Email already registered");
        }

        User u = new User();
        u.id = UUID.randomUUID().toString();
        u.name = name != null ? name.trim() : "";
        u.studentId = studentId != null ? studentId.trim() : "";
        u.email = email != null ? email.trim() : "";
        u.passwordHash = PasswordHasher.hash(password);
        u.role = Roles.TA;
        u.active = true;

        List<User> users = loadAll();
        users.add(u);
        saveAll(users);
        return u;
    }

    public Optional<User> verifyCredentials(String email, String password) throws IOException {
        Optional<User> u = findByEmail(email);
        if (!u.isPresent()) {
            return Optional.empty();
        }
        User user = u.get();
        if (!PasswordHasher.verify(password, user.passwordHash)) {
            return Optional.empty();
        }
        return Optional.of(user);
    }

    public Optional<User> login(String email, String password) throws IOException {
        Optional<User> u = verifyCredentials(email, password);
        if (!u.isPresent()) {
            return Optional.empty();
        }
        if (!u.get().active) {
            return Optional.empty();
        }
        return u;
    }

    public void setUserActive(String userId, boolean active) throws IOException {
        List<User> users = loadAll();
        boolean found = false;
        for (User u : users) {
            if (userId.equals(u.id)) {
                u.active = active;
                found = true;
                break;
            }
        }
        if (!found) {
            throw new IllegalStateException("User not found: " + userId);
        }
        saveAll(users);
    }

    public void removeUserRecord(String userId) throws IOException {
        List<User> users = loadAll();
        boolean removed = users.removeIf(u -> userId.equals(u.id));
        if (!removed) {
            throw new IllegalStateException("User not found: " + userId);
        }
        saveAll(users);
    }

    public long countAdmins() throws IOException {
        return loadAll().stream().filter(u -> Roles.ADMIN.equals(u.role)).count();
    }

    public void updateUserBasics(User user, String newName, String newStudentId, String newEmail)
            throws IOException {
        if (user == null || user.id == null || user.id.isEmpty()) {
            throw new IllegalArgumentException("Invalid user");
        }
        String email = newEmail != null ? newEmail.trim() : "";
        if (email.isEmpty()) {
            throw new IllegalArgumentException("Email is required");
        }

        if (user.email == null || !user.email.equalsIgnoreCase(email)) {
            Optional<User> other = findByEmail(email);
            if (other.isPresent() && !user.id.equals(other.get().id)) {
                throw new IllegalArgumentException("This email is already registered to another account.");
            }
        }

        user.name = newName != null ? newName.trim() : "";
        user.studentId = newStudentId != null ? newStudentId.trim() : "";
        user.email = email;

        List<User> users = loadAll();
        for (int i = 0; i < users.size(); i++) {
            if (user.id.equals(users.get(i).id)) {
                users.set(i, user);
                saveAll(users);
                return;
            }
        }
        throw new IllegalStateException("User not found: " + user.id);
    }

    public List<User> listAllUsers() throws IOException {
        return loadAll();
    }

    public User insertUser(User u) throws IOException {
        if (u.id == null || u.id.isEmpty()) {
            u.id = UUID.randomUUID().toString();
        }
        if (findByEmail(u.email).isPresent()) {
            throw new IllegalArgumentException("Email already registered");
        }
        List<User> users = loadAll();
        users.add(u);
        saveAll(users);
        return u;
    }

    public void updatePasswordHash(String userId, String passwordHash) throws IOException {
        if (userId == null || userId.isEmpty()) {
            throw new IllegalArgumentException("Invalid user");
        }
        List<User> users = loadAll();
        boolean found = false;
        for (User u : users) {
            if (userId.equals(u.id)) {
                u.passwordHash = passwordHash != null ? passwordHash : "";
                found = true;
                break;
            }
        }
        if (!found) {
            throw new IllegalStateException("User not found: " + userId);
        }
        saveAll(users);
    }

    public User createUserByAdmin(String name, String email, String password, String role) throws IOException {
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email is required");
        }
        if (password == null || password.trim().isEmpty()) {
            throw new IllegalArgumentException("Password is required");
        }
        String normalizedRole = role != null ? role.trim() : "";
        if (!Roles.TA.equals(normalizedRole) && !Roles.MO.equals(normalizedRole)) {
            throw new IllegalArgumentException("Role must be TA or MO");
        }
        if (findByEmail(email).isPresent()) {
            throw new IllegalArgumentException("Email already registered");
        }

        User u = new User();
        u.id = UUID.randomUUID().toString();
        u.name = name != null ? name.trim() : "";
        u.studentId = "";
        u.email = email.trim();
        u.passwordHash = PasswordHasher.hash(password);
        u.role = normalizedRole;
        u.active = true;

        List<User> users = loadAll();
        users.add(u);
        saveAll(users);
        return u;
    }
}

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

/**
 * Authentication and user-account persistence backed by {@code users.json}.
 * <p>
 * Handles registration, credential verification, login (active accounts only), profile field
 * updates, activation flags, and admin-oriented listing. Passwords are stored as plain hashes
 * in the JSON store (matching existing {@link User} construction).
 */
public class AuthService {
    private static final String USERS_JSON = "users.json";
    private final FileStore store;

    /**
     * Creates a service that reads and writes user records under {@code dataDir}.
     *
     * @param dataDir root directory containing {@code users.json}
     */
    public AuthService(Path dataDir) {
        this.store = new FileStore(dataDir);
    }

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

    /**
     * Finds a user by email address (case-insensitive).
     *
     * @param email email to look up
     * @return matching user, if any
     * @throws IOException if {@code users.json} cannot be read
     */
    public Optional<User> findByEmail(String email) throws IOException {
        List<Map<String, String>> rows = store.readMaps(USERS_JSON);
        return rows.stream()
                   .filter(m -> email != null && email.equalsIgnoreCase(m.get("email")))
                   .map(AuthService::mapToUser)
                   .findFirst();
    }

    /**
     * Finds a user by internal id.
     *
     * @param id user id; {@code null} or empty yields {@link Optional#empty()}
     * @return matching user, if any
     * @throws IOException if {@code users.json} cannot be read
     */
    public Optional<User> findById(String id) throws IOException {
        if (id == null || id.isEmpty()) return Optional.empty();
        List<Map<String, String>> rows = store.readMaps(USERS_JSON);
        return rows.stream()
                   .filter(m -> id.equals(m.get("id")))
                   .map(AuthService::mapToUser)
                   .findFirst();
    }

    /**
     * Registers a new TA user with a generated UUID id.
     *
     * @param name      display name
     * @param studentId student identifier
     * @param email     unique login email
     * @param password  plain-text password (hashed via {@link User} constructor)
     * @return the newly created user
     * @throws IOException              if the file cannot be read or written
     * @throws IllegalArgumentException if the email is already registered
     */
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

    /**
     * Verifies email and password without requiring an active account.
     * <p>
     * Used by the web login flow to distinguish invalid credentials from deactivated accounts.
     * For API-style login that rejects inactive users, use {@link #login} instead.
     *
     * @param email    login email
     * @param password plain-text password to compare
     * @return the user when email exists and password matches (including inactive accounts)
     * @throws IOException if {@code users.json} cannot be read
     */
    public Optional<User> verifyCredentials(String email, String password) throws IOException {
        Optional<User> u = findByEmail(email);
        if (!u.isPresent()) {
            return Optional.empty();
        }
        User user = u.get();
        if (password == null || !password.equals(user.passwordHash)) {
            return Optional.empty();
        }
        return Optional.of(user);
    }

    /**
     * Authenticates a user when credentials match and the account is active.
     *
     * @param email    login email
     * @param password plain-text password to compare
     * @return the user on successful login; empty if credentials fail or account is inactive
     * @throws IOException if {@code users.json} cannot be read
     */
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

    /**
     * Sets the {@link User#active} flag for the given user and persists the change.
     *
     * @param userId user id to update
     * @param active new active state
     * @throws IOException              if the file cannot be read or written
     * @throws IllegalStateException      if no user with {@code userId} exists
     */
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

    /**
     * Deletes the user row from {@code users.json} only.
     * <p>
     * Related profile, application, and favorite data must be removed by the caller.
     *
     * @param userId id of the user record to remove
     * @throws IOException              if the file cannot be read or written
     * @throws IllegalStateException      if no user with {@code userId} exists
     */
    public void removeUserRecord(String userId) throws IOException {
        List<Map<String, String>> rows = store.readMaps(USERS_JSON);
        boolean removed = rows.removeIf(m -> userId != null && userId.equals(m.get("id")));
        if (!removed) throw new IllegalStateException("User not found: " + userId);
        store.writeMaps(USERS_JSON, rows);
    }

    /**
     * Counts users whose role is {@link Roles#ADMIN}.
     * <p>
     * Used to prevent deletion or deactivation of the last administrator.
     *
     * @return number of admin users
     * @throws IOException if {@code users.json} cannot be read
     */
    public long countAdmins() throws IOException {
        return store.readMaps(USERS_JSON).stream()
                .map(AuthService::mapToUser)
                .filter(u -> Roles.ADMIN.equals(u.role))
                .count();
    }

    /**
     * Updates name, student id, and email for an existing user.
     * <p>
     * Email must remain unique across all other accounts (case-insensitive).
     *
     * @param user          existing user instance (must have a non-empty id)
     * @param newName       updated display name
     * @param newStudentId  updated student id
     * @param newEmail      updated email (required, trimmed)
     * @throws IOException              if the file cannot be read or written
     * @throws IllegalArgumentException if user is invalid, email is empty, or email is taken
     * @throws IllegalStateException    if the user row is missing from storage
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

    /**
     * Returns every registered user (admin user-management views).
     *
     * @return all users in file order; never {@code null}
     * @throws IOException if {@code users.json} cannot be read
     */
    public List<User> listAllUsers() throws IOException {
        return store.readMaps(USERS_JSON).stream()
                    .map(AuthService::mapToUser)
                    .collect(Collectors.toList());
    }
}

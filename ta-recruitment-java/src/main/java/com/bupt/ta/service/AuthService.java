package com.bupt.ta.service;

import com.bupt.ta.model.User;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class AuthService {
    private static final String USERS_JSON = "users.json";
    private final FileStore store;

    public AuthService(Path dataDir) {
        this.store = new FileStore(dataDir);
    }

    public Optional<User> findByEmail(String email) throws IOException {
        List<User> users = store.readList(USERS_JSON, FileStore.listType(User.class));
        return users.stream().filter(u -> u.email != null && u.email.equalsIgnoreCase(email)).findFirst();
    }

    public User register(String name, String studentId, String email, String password) throws IOException {
        List<User> users = store.readList(USERS_JSON, FileStore.listType(User.class));
        boolean exists = users.stream().anyMatch(u -> u.email != null && u.email.equalsIgnoreCase(email));
        if (exists) throw new IllegalArgumentException("Email already registered");

        // For coursework prototype, keep as-is; in report, state it should be hashed.
        String id = UUID.randomUUID().toString();
        User u = new User(id, name, studentId, email, password);
        users.add(u);
        store.writeList(USERS_JSON, users);
        return u;
    }

    public Optional<User> login(String email, String password) throws IOException {
        Optional<User> u = findByEmail(email);
        if (u.isEmpty()) return Optional.empty();
        return password != null && password.equals(u.get().passwordHash) ? u : Optional.empty();
    }
}

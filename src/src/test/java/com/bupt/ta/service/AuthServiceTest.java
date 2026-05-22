package com.bupt.ta.service;

import com.bupt.ta.model.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Component tests for authentication (login / registration). */
class AuthServiceTest {

    @TempDir
    Path dataDir;

    @Test
    void login_validCredentials_returnsUser() throws Exception {
        seedUser("ta@test.local", "secret123", true);
        AuthService auth = new AuthService(dataDir);

        Optional<User> u = auth.login("ta@test.local", "secret123");
        assertTrue(u.isPresent());
        assertEquals("ta@test.local", u.get().email);
    }

    @Test
    void login_wrongPassword_returnsEmpty() throws Exception {
        seedUser("ta@test.local", "secret123", true);
        AuthService auth = new AuthService(dataDir);

        assertFalse(auth.login("ta@test.local", "wrong").isPresent());
    }

    @Test
    void login_inactiveAccount_returnsEmpty() throws Exception {
        seedUser("ta@test.local", "secret123", false);
        AuthService auth = new AuthService(dataDir);

        assertFalse(auth.login("ta@test.local", "secret123").isPresent());
    }

    private void seedUser(String email, String password, boolean active) throws Exception {
        String json = "[\n"
                + "  {\"id\":\"u1\",\"name\":\"Test TA\",\"studentId\":\"S001\",\"email\":\""
                + email + "\",\"passwordHash\":\"" + password + "\",\"role\":\"TA\",\"active\":\""
                + (active ? "true" : "false") + "\"}\n"
                + "]";
        Files.write(dataDir.resolve("users.json"), json.getBytes(StandardCharsets.UTF_8));
    }
}

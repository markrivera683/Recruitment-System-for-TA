package com.bupt.ta.service;

import com.bupt.ta.model.Roles;
import com.bupt.ta.model.User;
import com.bupt.ta.testsupport.TestFixtures;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
        assertFalse(new AuthService(dataDir).login("ta@test.local", "wrong").isPresent());
    }

    @Test
    void login_inactiveAccount_returnsEmpty() throws Exception {
        seedUser("ta@test.local", "secret123", false);
        assertFalse(new AuthService(dataDir).login("ta@test.local", "secret123").isPresent());
    }

    @Test
    void register_success_createsActiveTa() throws Exception {
        TestFixtures.seedEmptyDataDir(dataDir);
        AuthService auth = new AuthService(dataDir);
        User u = auth.register("New TA", TestFixtures.validBuptStudentId(), "new@bupt.edu.cn", "pass");
        assertEquals(Roles.TA, u.role);
        assertTrue(u.active);
        assertEquals(1, auth.listAllUsers().size());
    }

    @Test
    void register_duplicateEmail_throws() throws Exception {
        seedUser("dup@bupt.edu.cn", "x", true);
        AuthService auth = new AuthService(dataDir);
        assertThrows(IllegalArgumentException.class,
                () -> auth.register("A", "2021000001", "dup@bupt.edu.cn", "y"));
    }

    @Test
    void verifyCredentials_inactiveStillReturnsUser() throws Exception {
        seedUser("inactive@bupt.edu.cn", "pass", false);
        AuthService auth = new AuthService(dataDir);
        assertTrue(auth.verifyCredentials("inactive@bupt.edu.cn", "pass").isPresent());
        assertFalse(auth.login("inactive@bupt.edu.cn", "pass").isPresent());
    }

    @Test
    void findByEmail_caseInsensitive() throws Exception {
        seedUser("Case@Bupt.edu.cn", "pass", true);
        AuthService auth = new AuthService(dataDir);
        assertTrue(auth.findByEmail("case@bupt.edu.cn").isPresent());
    }

    @Test
    void findById_nullOrEmpty_returnsEmpty() throws Exception {
        TestFixtures.seedEmptyDataDir(dataDir);
        AuthService auth = new AuthService(dataDir);
        assertFalse(auth.findById(null).isPresent());
        assertFalse(auth.findById("").isPresent());
    }

    @Test
    void setUserActive_togglesAndPersists() throws Exception {
        seedUser("u@bupt.edu.cn", "p", true);
        AuthService auth = new AuthService(dataDir);
        String id = auth.findByEmail("u@bupt.edu.cn").get().id;
        auth.setUserActive(id, false);
        assertFalse(auth.login("u@bupt.edu.cn", "p").isPresent());
        auth.setUserActive(id, true);
        assertTrue(auth.login("u@bupt.edu.cn", "p").isPresent());
    }

    @Test
    void setUserActive_unknownUser_throws() throws Exception {
        TestFixtures.seedEmptyDataDir(dataDir);
        assertThrows(IllegalStateException.class,
                () -> new AuthService(dataDir).setUserActive("missing", true));
    }

    @Test
    void removeUserRecord_deletesRow() throws Exception {
        seedUser("del@bupt.edu.cn", "p", true);
        AuthService auth = new AuthService(dataDir);
        String id = auth.findByEmail("del@bupt.edu.cn").get().id;
        auth.removeUserRecord(id);
        assertFalse(auth.findById(id).isPresent());
    }

    @Test
    void countAdmins_countsOnlyAdminRole() throws Exception {
        String json = "["
                + "{\"id\":\"a1\",\"name\":\"Admin\",\"studentId\":\"1\",\"email\":\"a@t.com\","
                + "\"passwordHash\":\"x\",\"role\":\"ADMIN\",\"active\":\"true\"},"
                + "{\"id\":\"t1\",\"name\":\"TA\",\"studentId\":\"2\",\"email\":\"t@t.com\","
                + "\"passwordHash\":\"x\",\"role\":\"TA\",\"active\":\"true\"}"
                + "]";
        Files.write(dataDir.resolve("users.json"), json.getBytes(StandardCharsets.UTF_8));
        assertEquals(1, new AuthService(dataDir).countAdmins());
    }

    @Test
    void updateUserBasics_changesFields() throws Exception {
        seedUser("old@bupt.edu.cn", "p", true);
        AuthService auth = new AuthService(dataDir);
        User u = auth.findByEmail("old@bupt.edu.cn").get();
        auth.updateUserBasics(u, "New Name", "2021000099", "new@bupt.edu.cn");
        User updated = auth.findById(u.id).get();
        assertEquals("New Name", updated.name);
        assertEquals("new@bupt.edu.cn", updated.email);
    }

    @Test
    void updateUserBasics_duplicateEmail_throws() throws Exception {
        String json = "["
                + "{\"id\":\"u1\",\"name\":\"A\",\"studentId\":\"1\",\"email\":\"a@t.com\","
                + "\"passwordHash\":\"x\",\"role\":\"TA\",\"active\":\"true\"},"
                + "{\"id\":\"u2\",\"name\":\"B\",\"studentId\":\"2\",\"email\":\"b@t.com\","
                + "\"passwordHash\":\"x\",\"role\":\"TA\",\"active\":\"true\"}"
                + "]";
        Files.write(dataDir.resolve("users.json"), json.getBytes(StandardCharsets.UTF_8));
        AuthService auth = new AuthService(dataDir);
        User u1 = auth.findByEmail("a@t.com").get();
        assertThrows(IllegalArgumentException.class,
                () -> auth.updateUserBasics(u1, "A", "1", "b@t.com"));
    }

    @Test
    void listAllUsers_returnsAll() throws Exception {
        String json = "["
                + "{\"id\":\"u1\",\"name\":\"One\",\"studentId\":\"2021000001\",\"email\":\"one@bupt.edu.cn\","
                + "\"passwordHash\":\"p\",\"role\":\"TA\",\"active\":\"true\"},"
                + "{\"id\":\"u2\",\"name\":\"Two\",\"studentId\":\"2021000002\",\"email\":\"two@bupt.edu.cn\","
                + "\"passwordHash\":\"p2\",\"role\":\"TA\",\"active\":\"true\"}"
                + "]";
        Files.write(dataDir.resolve("users.json"), json.getBytes(StandardCharsets.UTF_8));
        assertEquals(2, new AuthService(dataDir).listAllUsers().size());
    }

    private void seedUser(String email, String password, boolean active) throws Exception {
        if (!Files.exists(dataDir.resolve("users.json"))) {
            TestFixtures.seedEmptyDataDir(dataDir);
        }
        String json = "[\n"
                + "  {\"id\":\"u1\",\"name\":\"Test TA\",\"studentId\":\"S001\",\"email\":\""
                + email + "\",\"passwordHash\":\"" + password + "\",\"role\":\"TA\",\"active\":\""
                + (active ? "true" : "false") + "\"}\n"
                + "]";
        Files.write(dataDir.resolve("users.json"), json.getBytes(StandardCharsets.UTF_8));
    }
}

package com.bupt.ta.servlet;

import com.bupt.ta.model.Roles;
import com.bupt.ta.model.User;
import com.bupt.ta.service.ApplicationService;
import com.bupt.ta.service.AuthService;
import com.bupt.ta.service.ProfileService;
import com.bupt.ta.testsupport.ServletTestSupport;
import com.bupt.ta.testsupport.TestFixtures;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.verify;

class AdminUserServletTest {

    @TempDir
    Path dataDir;

    private AdminUserServlet servlet;
    private AuthService auth;

    @BeforeEach
    void setUp() throws Exception {
        TestFixtures.seedEmptyDataDir(dataDir);
        auth = new AuthService(dataDir);
        servlet = new AdminUserServlet();
        ServletTestSupport.injectField(servlet, "dataDir", dataDir);
        ServletTestSupport.injectField(servlet, "auth", auth);
        ServletTestSupport.injectField(servlet, "applications", new ApplicationService(dataDir));
        ServletTestSupport.injectField(servlet, "profiles", new ProfileService(dataDir));
    }

    @Test
    void post_activateUser() throws Exception {
        seedUser("u2", Roles.TA, false);
        User admin = TestFixtures.sampleAdmin("admin1");
        HttpServletRequest req = ServletTestSupport.mockRequest(
                ServletTestSupport.mockServletContext(dataDir), admin,
                ServletTestSupport.params("action", "activate", "userId", "u2"));
        HttpServletResponse resp = ServletTestSupport.mockResponse();
        servlet.doPost(req, resp);
        verify(resp).sendRedirect(contains("/admin"));
        assertTrue(auth.findById("u2").get().active);
    }

    @Test
    void post_cannotModifySelf() throws Exception {
        User admin = TestFixtures.sampleAdmin("admin1");
        HttpServletRequest req = ServletTestSupport.mockRequest(
                ServletTestSupport.mockServletContext(dataDir), admin,
                ServletTestSupport.params("action", "deactivate", "userId", "admin1"));
        HttpServletResponse resp = ServletTestSupport.mockResponse();
        servlet.doPost(req, resp);
        verify(resp).sendRedirect(contains("/admin?msg="));
    }

    @Test
    void post_nonAdmin_forbidden() throws Exception {
        User ta = TestFixtures.sampleTa("u1", "ta@bupt.edu.cn");
        HttpServletRequest req = ServletTestSupport.mockRequest(
                ServletTestSupport.mockServletContext(dataDir), ta,
                ServletTestSupport.params("action", "activate", "userId", "u2"));
        HttpServletResponse resp = ServletTestSupport.mockResponse();
        servlet.doPost(req, resp);
        verify(resp).sendError(HttpServletResponse.SC_FORBIDDEN);
    }

    @Test
    void post_deleteUser_removesFromAuth() throws Exception {
        seedUser("u2", Roles.TA, true);
        User admin = TestFixtures.sampleAdmin("admin1");
        HttpServletRequest req = ServletTestSupport.mockRequest(
                ServletTestSupport.mockServletContext(dataDir), admin,
                ServletTestSupport.params("action", "delete", "userId", "u2"));
        HttpServletResponse resp = ServletTestSupport.mockResponse();
        servlet.doPost(req, resp);
        assertFalse(auth.findById("u2").isPresent());
    }

    private void seedUser(String id, String role, boolean active) throws Exception {
        String json = "[{\"id\":\"" + id + "\",\"name\":\"User\",\"studentId\":\"2021000001\","
                + "\"email\":\"" + id + "@bupt.edu.cn\",\"passwordHash\":\"x\",\"role\":\"" + role + "\","
                + "\"active\":\"" + (active ? "true" : "false") + "\"}]";
        Files.write(dataDir.resolve("users.json"), json.getBytes(StandardCharsets.UTF_8));
        auth = new AuthService(dataDir);
        ServletTestSupport.injectField(servlet, "auth", auth);
    }
}

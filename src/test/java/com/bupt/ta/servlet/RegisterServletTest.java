package com.bupt.ta.servlet;

import com.bupt.ta.service.AuthService;
import com.bupt.ta.service.ProfileService;
import com.bupt.ta.testsupport.ServletTestSupport;
import com.bupt.ta.testsupport.TestFixtures;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.nio.file.Path;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.verify;

class RegisterServletTest {

    @TempDir
    Path dataDir;

    private RegisterServlet servlet;

    @BeforeEach
    void setUp() throws Exception {
        TestFixtures.seedEmptyDataDir(dataDir);
        servlet = new RegisterServlet();
        ServletTestSupport.injectField(servlet, "auth", new AuthService(dataDir));
        ServletTestSupport.injectField(servlet, "profiles", new ProfileService(dataDir));
    }

    @Test
    void post_validRegistration_redirectsToJob() throws Exception {
        HttpServletRequest req = ServletTestSupport.mockRequest(
                ServletTestSupport.mockServletContext(dataDir), (com.bupt.ta.model.User) null,
                ServletTestSupport.params(
                        "name", "Li Wei",
                        "studentId", TestFixtures.validBuptStudentId(),
                        "email", "newuser@bupt.edu.cn",
                        "phone", "13800138000",
                        "password", "secret123",
                        "confirm", "secret123"));
        HttpServletResponse resp = ServletTestSupport.mockResponse();
        servlet.doPost(req, resp);
        verify(resp).sendRedirect(contains("/job"));
    }

    @Test
    void post_invalidFields_forwardsToRegister() throws Exception {
        HttpServletRequest req = ServletTestSupport.mockRequest(
                ServletTestSupport.mockServletContext(dataDir), (com.bupt.ta.model.User) null,
                ServletTestSupport.params(
                        "name", "X",
                        "studentId", "bad",
                        "email", "not-an-email",
                        "phone", "",
                        "password", "a",
                        "confirm", "b"));
        HttpServletResponse resp = ServletTestSupport.mockResponse();
        servlet.doPost(req, resp);
        verify(req.getRequestDispatcher(anyString())).forward(req, resp);
    }

    @Test
    void post_duplicateEmail_forwardsWithError() throws Exception {
        new AuthService(dataDir).register("Existing", TestFixtures.validBuptStudentId(),
                "dup@bupt.edu.cn", "pass");
        HttpServletRequest req = ServletTestSupport.mockRequest(
                ServletTestSupport.mockServletContext(dataDir), (com.bupt.ta.model.User) null,
                ServletTestSupport.params(
                        "name", "Li Wei",
                        "studentId", TestFixtures.validBuptStudentId(),
                        "email", "dup@bupt.edu.cn",
                        "phone", "13800138000",
                        "password", "secret123",
                        "confirm", "secret123"));
        HttpServletResponse resp = ServletTestSupport.mockResponse();
        servlet.doPost(req, resp);
        verify(req.getRequestDispatcher(anyString())).forward(req, resp);
    }
}

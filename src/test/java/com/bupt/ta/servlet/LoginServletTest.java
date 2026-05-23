package com.bupt.ta.servlet;

import com.bupt.ta.model.Roles;
import com.bupt.ta.model.User;
import com.bupt.ta.service.AuthService;
import com.bupt.ta.testsupport.ServletTestSupport;
import com.bupt.ta.testsupport.TestFixtures;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class LoginServletTest {

    @TempDir
    Path dataDir;

    private AuthService auth;
    private LoginServlet servlet;

    @BeforeEach
    void setUp() throws Exception {
        TestFixtures.seedEmptyDataDir(dataDir);
        String json = "[{\"id\":\"u1\",\"name\":\"TA User\",\"studentId\":\"2021000001\","
                + "\"email\":\"ta@bupt.edu.cn\",\"passwordHash\":\"pass123\",\"role\":\"TA\",\"active\":\"true\"}]";
        Files.write(dataDir.resolve("users.json"), json.getBytes(StandardCharsets.UTF_8));
        auth = new AuthService(dataDir);
        servlet = new LoginServlet();
        ServletTestSupport.injectField(servlet, "auth", auth);
    }

    @Test
    void post_validCredentials_redirectsToJob() throws Exception {
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        HttpSession session = mock(HttpSession.class);
        when(req.getParameter("email")).thenReturn("ta@bupt.edu.cn");
        when(req.getParameter("password")).thenReturn("pass123");
        when(req.getSession(true)).thenReturn(session);
        when(req.getContextPath()).thenReturn("/ta-recruitment");
        servlet.doPost(req, resp);
        verify(session).setAttribute(eq("user"), org.mockito.ArgumentMatchers.any(User.class));
        verify(resp).sendRedirect("/ta-recruitment/job");
    }

    @Test
    void post_admin_redirectsToAdmin() throws Exception {
        String json = "[{\"id\":\"a1\",\"name\":\"Admin\",\"studentId\":\"2021000001\","
                + "\"email\":\"admin@bupt.edu.cn\",\"passwordHash\":\"admin123\",\"role\":\"ADMIN\",\"active\":\"true\"}]";
        Files.write(dataDir.resolve("users.json"), json.getBytes(StandardCharsets.UTF_8));
        ServletTestSupport.injectField(servlet, "auth", new AuthService(dataDir));

        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        HttpSession session = mock(HttpSession.class);
        when(req.getParameter("email")).thenReturn("admin@bupt.edu.cn");
        when(req.getParameter("password")).thenReturn("admin123");
        when(req.getSession(true)).thenReturn(session);
        when(req.getContextPath()).thenReturn("/ta-recruitment");
        servlet.doPost(req, resp);
        verify(resp).sendRedirect("/ta-recruitment/admin");
    }

    @Test
    void post_inactiveAccount_forwardsWithError() throws Exception {
        String json = "[{\"id\":\"u1\",\"name\":\"TA\",\"studentId\":\"2021000001\","
                + "\"email\":\"inactive@bupt.edu.cn\",\"passwordHash\":\"pass\",\"role\":\"TA\",\"active\":\"false\"}]";
        Files.write(dataDir.resolve("users.json"), json.getBytes(StandardCharsets.UTF_8));
        ServletTestSupport.injectField(servlet, "auth", new AuthService(dataDir));

        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        RequestDispatcher dispatcher = mock(RequestDispatcher.class);
        when(req.getParameter("email")).thenReturn("inactive@bupt.edu.cn");
        when(req.getParameter("password")).thenReturn("pass");
        when(req.getRequestDispatcher("/WEB-INF/jsp/login.jsp")).thenReturn(dispatcher);
        servlet.doPost(req, resp);
        verify(req).setAttribute(eq("error"), eq("This account has been deactivated."));
    }

    @Test
    void post_wrongPassword_forwardsWithError() throws Exception {
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        RequestDispatcher dispatcher = mock(RequestDispatcher.class);
        when(req.getParameter("email")).thenReturn("ta@bupt.edu.cn");
        when(req.getParameter("password")).thenReturn("wrong");
        when(req.getRequestDispatcher("/WEB-INF/jsp/login.jsp")).thenReturn(dispatcher);
        servlet.doPost(req, resp);
        verify(req).setAttribute(eq("error"), eq("Invalid email or password"));
    }
}

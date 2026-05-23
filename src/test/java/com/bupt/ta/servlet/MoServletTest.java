package com.bupt.ta.servlet;

import com.bupt.ta.model.User;
import com.bupt.ta.service.ApplicationService;
import com.bupt.ta.service.AuthService;
import com.bupt.ta.service.JobService;
import com.bupt.ta.service.WorkloadService;
import com.bupt.ta.testsupport.ServletTestSupport;
import com.bupt.ta.testsupport.TestFixtures;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.nio.file.Path;

import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.verify;

class MoServletTest {

    @TempDir
    Path dataDir;

    private MoServlet servlet;
    private User mo;

    @BeforeEach
    void setUp() throws Exception {
        TestFixtures.seedEmptyDataDir(dataDir);
        TestFixtures.writeJobsJson(dataDir, "[]");
        mo = TestFixtures.sampleMo("mo-1");
        servlet = new MoServlet();
        ServletTestSupport.injectField(servlet, "applications", new ApplicationService(dataDir));
        ServletTestSupport.injectField(servlet, "auth", new AuthService(dataDir));
        ServletTestSupport.injectField(servlet, "workloadService", new WorkloadService());
        ServletTestSupport.injectField(servlet, "jobs",
                new JobService(dataDir.resolve("jobs.json").toString()));
    }

    @Test
    void get_nonMo_forbidden() throws Exception {
        User ta = TestFixtures.sampleTa("ta-1", "ta@bupt.edu.cn");
        HttpServletRequest req = ServletTestSupport.mockRequest(
                ServletTestSupport.mockServletContext(dataDir), ta, null);
        HttpServletResponse resp = ServletTestSupport.mockResponse();
        servlet.doGet(req, resp);
        verify(resp).sendError(HttpServletResponse.SC_FORBIDDEN);
    }

    @Test
    void post_createJobDraft() throws Exception {
        HttpServletRequest req = ServletTestSupport.mockRequest(
                ServletTestSupport.mockServletContext(dataDir), mo,
                ServletTestSupport.params(
                        "action", "createJob",
                        "moduleName", "New Module",
                        "moduleCode", "NM101",
                        "description", "A description",
                        "numberOfTAs", "2"));
        HttpServletResponse resp = ServletTestSupport.mockResponse();
        servlet.doPost(req, resp);
        verify(resp).sendRedirect(contains("/mo?msg="));
    }

    @Test
    void post_approveApplication() throws Exception {
        ApplicationService apps = new ApplicationService(dataDir);
        apps.save(TestFixtures.sampleApplication("app-1", "ta-1", "CS101", "CS101"));
        HttpServletRequest req = ServletTestSupport.mockRequest(
                ServletTestSupport.mockServletContext(dataDir), mo,
                ServletTestSupport.params(
                        "action", "approveApp",
                        "appId", "app-1",
                        "feedback", "Welcome"));
        HttpServletResponse resp = ServletTestSupport.mockResponse();
        servlet.doPost(req, resp);
        verify(resp).sendRedirect(contains("Accepted"));
    }

    @Test
    void post_missingFields_redirectsWithMessage() throws Exception {
        HttpServletRequest req = ServletTestSupport.mockRequest(
                ServletTestSupport.mockServletContext(dataDir), mo,
                ServletTestSupport.params("action", "createJob", "moduleName", "", "moduleCode", "", "description", ""));
        HttpServletResponse resp = ServletTestSupport.mockResponse();
        servlet.doPost(req, resp);
        verify(resp).sendRedirect(contains("/mo?msg="));
    }
}

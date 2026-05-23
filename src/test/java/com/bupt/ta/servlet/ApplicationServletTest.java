package com.bupt.ta.servlet;

import com.bupt.ta.model.User;
import com.bupt.ta.service.ApplicationService;
import com.bupt.ta.service.JobService;
import com.bupt.ta.service.ProfileService;
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

class ApplicationServletTest {

    @TempDir
    Path dataDir;

    private ApplicationServlet servlet;
    private User ta;

    @BeforeEach
    void setUp() throws Exception {
        TestFixtures.seedEmptyDataDir(dataDir);
        TestFixtures.writeJobsJson(dataDir, TestFixtures.jobJsonSingle("job-1", "CS101", "CS101", "Published", "2"));
        servlet = new ApplicationServlet();
        ServletTestSupport.injectField(servlet, "appService", new ApplicationService(dataDir));
        ServletTestSupport.injectField(servlet, "profiles", new ProfileService(dataDir));
        ServletTestSupport.injectField(servlet, "jobService",
                new JobService(dataDir.resolve("jobs.json").toString()));
        ta = TestFixtures.sampleTa("ta-1", "ta@bupt.edu.cn");
    }

    @Test
    void get_notLoggedIn_redirectsLogin() throws Exception {
        HttpServletRequest req = ServletTestSupport.mockRequest(
                ServletTestSupport.mockServletContext(dataDir), (User) null, null);
        HttpServletResponse resp = ServletTestSupport.mockResponse();
        servlet.doGet(req, resp);
        verify(resp).sendRedirect(contains("/login"));
    }

    @Test
    void post_incompleteProfile_redirectsProfile() throws Exception {
        HttpServletRequest req = ServletTestSupport.mockRequest(
                ServletTestSupport.mockServletContext(dataDir), ta,
                ServletTestSupport.params(
                        "jobId", "job-1",
                        "moduleName", "CS101",
                        "moduleCode", "CS101",
                        "role", "TA"));
        HttpServletResponse resp = ServletTestSupport.mockResponse();
        servlet.doPost(req, resp);
        verify(resp).sendRedirect(contains("/profile?msg="));
    }

    @Test
    void post_successfulApply_redirectsApplications() throws Exception {
        new ProfileService(dataDir).upsert(TestFixtures.completeProfile("ta-1"));
        HttpServletRequest req = ServletTestSupport.mockRequest(
                ServletTestSupport.mockServletContext(dataDir), ta,
                ServletTestSupport.params(
                        "jobId", "job-1",
                        "moduleName", "CS101",
                        "moduleCode", "CS101",
                        "role", "TA"));
        HttpServletResponse resp = ServletTestSupport.mockResponse();
        servlet.doPost(req, resp);
        verify(resp).sendRedirect(contains("/applications?msg="));
    }

    @Test
    void post_duplicateApply_redirectsWithError() throws Exception {
        new ProfileService(dataDir).upsert(TestFixtures.completeProfile("ta-1"));
        ApplicationService apps = new ApplicationService(dataDir);
        apps.save(TestFixtures.sampleApplication("existing", "ta-1", "CS101", "CS101"));

        HttpServletRequest req = ServletTestSupport.mockRequest(
                ServletTestSupport.mockServletContext(dataDir), ta,
                ServletTestSupport.params(
                        "jobId", "job-1",
                        "moduleName", "CS101",
                        "moduleCode", "CS101",
                        "role", "TA"));
        HttpServletResponse resp = ServletTestSupport.mockResponse();
        servlet.doPost(req, resp);
        verify(resp).sendRedirect(contains("/job?id=job-1&err="));
    }

    @Test
    void post_withdraw_ownApplication() throws Exception {
        ApplicationService apps = new ApplicationService(dataDir);
        apps.save(TestFixtures.sampleApplication("app-w", "ta-1", "CS101", "CS101"));
        HttpServletRequest req = ServletTestSupport.mockRequest(
                ServletTestSupport.mockServletContext(dataDir), ta,
                ServletTestSupport.params("action", "withdraw", "appId", "app-w"));
        HttpServletResponse resp = ServletTestSupport.mockResponse();
        servlet.doPost(req, resp);
        verify(resp).sendRedirect(contains("/applications?msg="));
    }
}

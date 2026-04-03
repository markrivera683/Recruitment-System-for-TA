package com.bupt.ta.servlet;

import com.bupt.ta.service.JobService;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet(urlPatterns = {"/admin/jobs"})
public class AdminJobServlet extends BaseServlet {

    private JobService jobService;

    @Override
    public void init() {
        String p = getServletContext().getRealPath("/WEB-INF/data/jobs.json");
        this.jobService = new JobService(p);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (!ensureAdmin(req, resp)) {
            return;
        }
        String action = req.getParameter("action");
        String jobId = req.getParameter("jobId");
        String ctx = req.getContextPath();

        if (!"delete".equals(action) || jobId == null || jobId.trim().isEmpty()) {
            resp.sendRedirect(ctx + "/admin?msg=" + java.net.URLEncoder.encode("Invalid job action.", java.nio.charset.StandardCharsets.UTF_8));
            return;
        }

        try {
            jobService.deleteJobById(jobId.trim());
        } catch (IOException e) {
            resp.sendRedirect(ctx + "/admin?msg=" + java.net.URLEncoder.encode("Failed to delete job: " + e.getMessage(), java.nio.charset.StandardCharsets.UTF_8));
            return;
        }
        resp.sendRedirect(ctx + "/admin");
    }
}

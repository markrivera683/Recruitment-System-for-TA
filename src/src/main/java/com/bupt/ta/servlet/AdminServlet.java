package com.bupt.ta.servlet;

import com.bupt.ta.model.User;
import com.bupt.ta.service.ApplicationService;
import com.bupt.ta.service.AuthService;
import com.bupt.ta.service.JobService;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@WebServlet(urlPatterns = {"/admin"})
public class AdminServlet extends BaseServlet {
    private AuthService auth;
    private ApplicationService applications;
    private JobService jobs;

    @Override
    public void init() {
        Path dataDir = Paths.get(getServletContext().getRealPath("/WEB-INF/data"));
        auth = new AuthService(dataDir);
        applications = new ApplicationService(dataDir);
        String jobsPath = getServletContext().getRealPath("/WEB-INF/data/jobs.json");
        jobs = new JobService(jobsPath);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        if (!ensureAdmin(req, resp)) {
            return;
        }
        String msg = req.getParameter("msg");
        if (msg != null && !msg.trim().isEmpty()) {
            req.setAttribute("adminMessage", msg);
        }
        List<User> users = auth.listAllUsers();
        req.setAttribute("applications", applications.listAll());
        req.setAttribute("users", users);
        req.setAttribute("jobs", jobs.getAllJobs());
        req.getRequestDispatcher("/WEB-INF/jsp/admin/dashboard.jsp").forward(req, resp);
    }
}

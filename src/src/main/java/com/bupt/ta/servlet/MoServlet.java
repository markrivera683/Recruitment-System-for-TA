package com.bupt.ta.servlet;

import com.bupt.ta.service.ApplicationService;
import com.bupt.ta.service.JobService;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

@WebServlet(urlPatterns = {"/mo"})
public class MoServlet extends BaseServlet {
    private ApplicationService applications;
    private JobService jobs;

    @Override
    public void init() {
        Path dataDir = Paths.get(getServletContext().getRealPath("/WEB-INF/data"));
        applications = new ApplicationService(dataDir);
        String jobsPath = getServletContext().getRealPath("/WEB-INF/data/jobs.json");
        jobs = new JobService(jobsPath);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        if (!ensureMo(req, resp)) {
            return;
        }

        req.setAttribute("jobs", jobs.getAllJobs());
        req.setAttribute("applications", applications.listAll());
        req.getRequestDispatcher("/WEB-INF/jsp/mo/dashboard.jsp").forward(req, resp);
    }
}
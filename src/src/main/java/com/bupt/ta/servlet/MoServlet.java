package com.bupt.ta.servlet;

import com.bupt.ta.model.Application;
import com.bupt.ta.service.ApplicationService;
import com.bupt.ta.service.JobService;
import com.bupt.ta.service.ProfileService;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.nio.file.Path;
import java.nio.file.Paths;

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
        List<Application> apps = applications.listAll();
        req.setAttribute("applications", apps);

        Path dataDir = Paths.get(getServletContext().getRealPath("/WEB-INF/data"));
        ProfileService profiles = new ProfileService(dataDir);
        Map<String, String> cvByUserId = new HashMap<>();
        for (Application a : apps) {
            if (a == null || a.userId == null || cvByUserId.containsKey(a.userId)) {
                continue;
            }
            String fn = profiles.getByUserId(a.userId)
                    .map(pr -> pr.cvFileName)
                    .filter(s -> s != null && !s.trim().isEmpty())
                    .orElse(null);
            cvByUserId.put(a.userId, fn);
        }
        req.setAttribute("cvByUserId", cvByUserId);

        req.getRequestDispatcher("/WEB-INF/jsp/mo/dashboard.jsp").forward(req, resp);
    }
}
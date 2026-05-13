package com.bupt.ta.servlet;

import com.bupt.ta.model.Application;
import com.bupt.ta.model.ApplicantProfile;
import com.bupt.ta.model.User;
import com.bupt.ta.service.ApplicationService;
import com.bupt.ta.service.ProfileService;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@WebServlet(urlPatterns = {"/applications"})
public class ApplicationServlet extends BaseServlet {
    private ApplicationService appService;
    private ProfileService profiles;

    @Override
    public void init() {
        Path dataDir = Paths.get(getServletContext().getRealPath("/WEB-INF/data"));
        appService = new ApplicationService(dataDir);
        profiles = new ProfileService(dataDir);
    }

    // ------------------------------------------------------------------ GET
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        User u = currentUser(req);
        if (u == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }
        String filter = req.getParameter("filter"); // All | Pending | Accepted | Rejected | Withdrawn
        if (filter == null || filter.trim().isEmpty()) filter = "All";

        List<Application> apps = appService.getByUserId(u.id);

        long pending  = apps.stream().filter(a -> "Pending".equals(a.status)).count();
        long accepted = apps.stream().filter(a -> "Accepted".equals(a.status)).count();
        long rejected = apps.stream().filter(a -> "Rejected".equals(a.status)).count();

        List<Application> filtered;
        if ("All".equals(filter)) {
            filtered = apps;
        } else {
            final String f = filter;
            filtered = apps.stream().filter(a -> f.equals(a.status))
                           .collect(Collectors.toList());
        }

        req.setAttribute("applications", filtered);
        req.setAttribute("allApps",      apps);
        req.setAttribute("filter",       filter);
        req.setAttribute("countPending",  pending);
        req.setAttribute("countAccepted", accepted);
        req.setAttribute("countRejected", rejected);
        req.getRequestDispatcher("/WEB-INF/jsp/application-status.jsp").forward(req, resp);
    }

    // ------------------------------------------------------------------ POST
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        User u = currentUser(req);
        if (u == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        String action = req.getParameter("action");

        // --- Withdraw an existing application ---
        if ("withdraw".equals(action)) {
            String appId = req.getParameter("appId");
            if (appId != null && !appId.trim().isEmpty()) {
                // Only allow the owner to withdraw
                List<Application> mine = appService.getByUserId(u.id);
                boolean owns = mine.stream().anyMatch(a -> appId.trim().equals(a.id));
                if (owns) {
                    appService.updateStatus(appId.trim(), "Withdrawn", "");
                }
            }
            resp.sendRedirect(req.getContextPath() + "/applications");
            return;
        }

        // --- Submit a new application ---
        String jobId     = req.getParameter("jobId");
        String moduleName = req.getParameter("moduleName");
        String moduleCode = req.getParameter("moduleCode");
        String role       = req.getParameter("role");

        if (moduleName == null || moduleName.trim().isEmpty()) {
            resp.sendRedirect(req.getContextPath() + "/job");
            return;
        }

        // Require completed profile before applying for a job.
        ApplicantProfile profile = profiles.getByUserId(u.id).orElse(null);
        if (!isProfileComplete(profile)) {
            String msg = urlEncode("Please complete your profile before applying for a job.");
            resp.sendRedirect(req.getContextPath() + "/profile?msg=" + msg);
            return;
        }

        // Prevent duplicate active applications for the same job
        List<Application> existing = appService.getByUserId(u.id);
        boolean duplicate = existing.stream().anyMatch(a ->
            moduleName.trim().equals(a.moduleName) &&
            !"Withdrawn".equals(a.status) &&
            !"Rejected".equals(a.status)
        );
        if (duplicate) {
            String encodedMsg = urlEncode(
                    "You already have an active application for " + moduleName.trim() + ".");
            resp.sendRedirect(req.getContextPath() + "/job?id=" + (jobId != null ? jobId : "") + "&err=" + encodedMsg);
            return;
        }

        Application app = new Application(
            UUID.randomUUID().toString(),
            u.id,
            moduleName.trim(),
            moduleCode != null ? moduleCode.trim() : "",
            role != null && !role.trim().isEmpty() ? role.trim() : "Teaching Assistant",
            LocalDate.now().toString()
        );
        appService.save(app);

        resp.sendRedirect(req.getContextPath() + "/applications");
    }

    private static boolean isProfileComplete(ApplicantProfile p) {
        return ProfileService.isApplicantProfileComplete(p);
    }
}

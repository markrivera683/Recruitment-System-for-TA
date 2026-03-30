package com.bupt.ta.servlet;

import com.bupt.ta.model.Application;
import com.bupt.ta.model.User;
import com.bupt.ta.service.ApplicationService;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@WebServlet(urlPatterns = {"/applications"})
public class ApplicationServlet extends BaseServlet {
    private ApplicationService appService;

    @Override
    public void init() {
        Path dataDir = Paths.get(getServletContext().getRealPath("/WEB-INF/data"));
        appService = new ApplicationService(dataDir);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        User u = currentUser(req);
        if (u == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }
        String filter = req.getParameter("filter"); // All | Pending | Accepted | Rejected
        if (filter == null || filter.trim().isEmpty()) filter = "All";

        List<Application> apps = appService.getByUserId(u.id);

        // Seed demo data for new users so the page isn't empty
        if (apps.isEmpty()) {
            seedDemo(u.id);
            apps = appService.getByUserId(u.id);
        }

        long pending  = apps.stream().filter(a -> "Pending".equals(a.status)).count();
        long accepted = apps.stream().filter(a -> "Accepted".equals(a.status)).count();
        long rejected = apps.stream().filter(a -> "Rejected".equals(a.status)).count();

        List<Application> filtered;
        if ("All".equals(filter)) {
            filtered = apps;
        } else {
            final String f = filter;
            filtered = apps.stream().filter(a -> f.equals(a.status))
                           .collect(java.util.stream.Collectors.toList());
        }

        req.setAttribute("applications", filtered);
        req.setAttribute("allApps",      apps);
        req.setAttribute("filter",       filter);
        req.setAttribute("countPending",  pending);
        req.setAttribute("countAccepted", accepted);
        req.setAttribute("countRejected", rejected);
        req.getRequestDispatcher("/WEB-INF/jsp/application-status.jsp").forward(req, resp);
    }

    private void seedDemo(String userId) throws IOException {
        String[][] seed = {
            {"1", "Data Structures & Algorithms", "CS2040", "Teaching Assistant",  "2026-03-01", "Accepted",
             "Congratulations! Please check your email for onboarding details."},
            {"2", "Introduction to Programming",  "CS1010", "Lab Demonstrator",     "2026-03-05", "Pending",  ""},
            {"3", "Database Systems",              "CS3223", "Teaching Assistant",  "2026-02-20", "Rejected",
             "Thank you for applying. The position has been filled."},
            {"4", "Operating Systems",             "CS3210", "Tutor",               "2026-03-10", "Pending",  ""},
            {"5", "Computer Networks",             "CS4226", "Teaching Assistant",  "2026-02-28", "Accepted",
             "Welcome aboard! Orientation is scheduled for next Monday."},
        };
        for (String[] row : seed) {
            Application a = new Application(
                userId + "-" + row[0], userId, row[1], row[2], row[3], row[4]);
            a.status   = row[5];
            a.feedback = row[6];
            appService.save(a);
        }
    }
}

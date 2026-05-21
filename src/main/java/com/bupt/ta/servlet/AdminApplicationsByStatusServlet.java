package com.bupt.ta.servlet;

import com.bupt.ta.model.Application;
import com.bupt.ta.model.User;
import com.bupt.ta.service.ApplicationService;
import com.bupt.ta.service.AuthService;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Admin view: applications filtered by status bucket (pending / accepted / rejected).
 */
@WebServlet(urlPatterns = {"/admin/applications/by-status"})
public class AdminApplicationsByStatusServlet extends BaseServlet {

    private ApplicationService applications;
    private AuthService auth;

    @Override
    public void init() {
        Path dataDir = Paths.get(getServletContext().getRealPath("/WEB-INF/data"));
        applications = new ApplicationService(dataDir);
        auth = new AuthService(dataDir);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (!ensureAdmin(req, resp)) {
            return;
        }
        String ctx = req.getContextPath();
        String raw = req.getParameter("status");
        String bucket = normalizeStatus(raw);
        if (bucket == null) {
            resp.sendRedirect(ctx + "/admin?msg=" + urlEncode("Invalid status."));
            return;
        }

        String msg = req.getParameter("msg");
        if (msg != null && !msg.trim().isEmpty()) {
            req.setAttribute("listMessage", msg.trim());
        }

        List<Application> filtered = applications.listAll().stream()
                .filter(a -> a != null && matchesBucket(a, bucket))
                .sorted(Comparator
                        .comparing((Application a) -> a.applicationDate == null ? "" : a.applicationDate).reversed()
                        .thenComparing(a -> a.moduleName == null ? "" : a.moduleName,
                                String.CASE_INSENSITIVE_ORDER))
                .collect(Collectors.toList());

        Map<String, User> userById = new HashMap<>();
        for (User u : auth.listAllUsers()) {
            if (u != null && u.id != null && !u.id.trim().isEmpty()) {
                userById.put(u.id.trim(), u);
            }
        }

        req.setAttribute("statusBucket", bucket);
        req.setAttribute("pageTitle", pageTitleFor(bucket));
        req.setAttribute("applications", filtered);
        req.setAttribute("userById", userById);
        req.getRequestDispatcher("/WEB-INF/jsp/admin/applications-by-status.jsp").forward(req, resp);
    }

    /** Returns pending | accepted | rejected, or null. */
    private static String normalizeStatus(String raw) {
        if (raw == null) return null;
        String s = raw.trim().toLowerCase();
        if ("pending".equals(s) || "accepted".equals(s) || "rejected".equals(s)) {
            return s;
        }
        return null;
    }

    private static boolean matchesBucket(Application a, String bucket) {
        String raw = a.status == null ? "" : a.status.trim();
        boolean acc = "Accepted".equalsIgnoreCase(raw);
        boolean rej = "Rejected".equalsIgnoreCase(raw);
        switch (bucket) {
            case "pending":
                return !acc && !rej;
            case "accepted":
                return acc;
            case "rejected":
                return rej;
            default:
                return false;
        }
    }

    private static String pageTitleFor(String bucket) {
        switch (bucket) {
            case "accepted":
                return "Accepted applications";
            case "rejected":
                return "Rejected applications";
            default:
                return "Pending applications";
        }
    }
}

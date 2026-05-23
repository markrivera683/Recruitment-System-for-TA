package com.bupt.ta.servlet;

import com.bupt.ta.model.Application;
import com.bupt.ta.service.ApplicationService;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

/**
 * Admin override for application status transitions (force accept, reject, or pending).
 *
 * <p><b>URL pattern:</b> {@code /admin/applications}
 *
 * <p><b>Role access:</b> {@link com.bupt.ta.model.Roles#ADMIN} only via {@link #ensureAdmin}.
 *
 * <p>Uses the same persistence layer as MO approve/reject. Only POST is supported; redirects
 * back to the applications-by-status list.
 */
@WebServlet(urlPatterns = {"/admin/applications"})
public class AdminApplicationServlet extends BaseServlet {

    private ApplicationService applications;

    /**
     * Initializes {@link ApplicationService} from {@code WEB-INF/data}.
     */
    @Override
    public void init() {
        Path dataDir = Paths.get(getServletContext().getRealPath("/WEB-INF/data"));
        applications = new ApplicationService(dataDir);
    }

    /**
     * Forces an application to Accepted, Rejected, or Pending with optional feedback.
     *
     * @param req  the incoming request; expects {@code action} ({@code forceAccept},
     *             {@code forceReject}, {@code forcePend}), {@code appId}, optional {@code feedback}
     *             and {@code returnStatus}
     * @param resp the response; redirects to the status list with a flash message
     * @throws ServletException if dispatch fails
     * @throws IOException      if status update fails
     */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (!ensureAdmin(req, resp)) {
            return;
        }
        req.setCharacterEncoding("UTF-8");
        String ctx = req.getContextPath();
        String action = req.getParameter("action");
        String appId = req.getParameter("appId");
        String feedback = req.getParameter("feedback");
        String fb = feedback == null ? "" : feedback.trim();
        String returnStatus = normalizeReturnStatus(req.getParameter("returnStatus"));
        String listUrl = ctx + "/admin/applications/by-status?status=" + returnStatus;

        if (appId == null || appId.trim().isEmpty()) {
            resp.sendRedirect(listUrl + "&msg=" + urlEncode("Missing application id."));
            return;
        }
        appId = appId.trim();

        Optional<Application> opt = applications.findById(appId);
        if (!opt.isPresent()) {
            resp.sendRedirect(listUrl + "&msg=" + urlEncode("Application not found."));
            return;
        }
        Application app = opt.get();
        String raw = app.status == null ? "" : app.status.trim();
        boolean isAccepted = "Accepted".equalsIgnoreCase(raw);
        boolean isRejected = "Rejected".equalsIgnoreCase(raw);
        boolean isPending = !isAccepted && !isRejected;

        try {
            if ("forceAccept".equals(action)) {
                if (!isPending && !isRejected) {
                    resp.sendRedirect(listUrl + "&msg=" + urlEncode("Only pending or rejected applications can be accepted."));
                    return;
                }
                applications.updateStatus(appId, "Accepted", fb);
            } else if ("forceReject".equals(action)) {
                if (!isPending && !isAccepted) {
                    resp.sendRedirect(listUrl + "&msg=" + urlEncode("Only pending or accepted applications can be rejected."));
                    return;
                }
                applications.updateStatus(appId, "Rejected", fb);
            } else if ("forcePend".equals(action)) {
                if (!isAccepted && !isRejected) {
                    resp.sendRedirect(listUrl + "&msg=" + urlEncode("Only accepted or rejected applications can be moved back to pending."));
                    return;
                }
                applications.updateStatus(appId, "Pending", fb);
            } else {
                resp.sendRedirect(listUrl + "&msg=" + urlEncode("Unknown action."));
                return;
            }
        } catch (IOException e) {
            resp.sendRedirect(listUrl + "&msg=" + urlEncode("Update failed: " + e.getMessage()));
            return;
        }
        resp.sendRedirect(listUrl + "&msg=" + urlEncode("Application status updated."));
    }

    private static String normalizeReturnStatus(String raw) {
        if (raw == null) return "pending";
        String s = raw.trim().toLowerCase();
        if ("pending".equals(s) || "accepted".equals(s) || "rejected".equals(s)) {
            return s;
        }
        return "pending";
    }
}

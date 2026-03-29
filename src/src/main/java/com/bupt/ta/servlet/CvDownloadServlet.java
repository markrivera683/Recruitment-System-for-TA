package com.bupt.ta.servlet;

import com.bupt.ta.model.ApplicantProfile;
import com.bupt.ta.model.User;
import com.bupt.ta.service.ProfileService;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Serves the uploaded CV for the logged-in user only.
 */
@WebServlet(urlPatterns = {"/cv"})
public class CvDownloadServlet extends BaseServlet {
    private ProfileService profiles;
    private Path dataDir;

    @Override
    public void init() {
        dataDir = Paths.get(getServletContext().getRealPath("/WEB-INF/data"));
        profiles = new ProfileService(dataDir);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        User u = currentUser(req);
        if (u == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }
        ApplicantProfile p = profiles.getByUserId(u.id).orElse(null);
        if (p == null || p.cvFileName == null || p.cvFileName.trim().isEmpty()) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        Path file = dataDir.resolve("cv").resolve(u.id).resolve(p.cvFileName);
        if (!Files.isRegularFile(file)) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        String probe = Files.probeContentType(file);
        if (probe != null) {
            resp.setContentType(probe);
        } else {
            resp.setContentType("application/octet-stream");
        }
        resp.setHeader("Content-Disposition", "inline; filename=\"" + p.cvFileName.replace("\"", "") + "\"");
        long len = Files.size(file);
        if (len <= Integer.MAX_VALUE) {
            resp.setContentLength((int) len);
        }
        try (OutputStream out = resp.getOutputStream()) {
            Files.copy(file, out);
        }
    }
}

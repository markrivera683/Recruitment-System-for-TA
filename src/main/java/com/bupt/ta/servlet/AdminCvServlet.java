package com.bupt.ta.servlet;

import com.bupt.ta.model.ApplicantProfile;
import com.bupt.ta.model.Roles;
import com.bupt.ta.model.User;
import com.bupt.ta.service.AuthService;
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

@WebServlet("/admin/cv")
public class AdminCvServlet extends BaseServlet {

    private AuthService authService;
    private ProfileService profileService;
    private Path dataDir;

    @Override
    public void init() {
        dataDir = Paths.get(getServletContext().getRealPath("/WEB-INF/data"));
        this.authService = new AuthService(dataDir);
        this.profileService = new ProfileService(dataDir);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (!ensureAdmin(req, resp)) {
            return;
        }
        String userId = req.getParameter("userId");
        if (userId == null || userId.trim().isEmpty()) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        userId = userId.trim();
        if (userId.contains("..") || userId.indexOf('/') >= 0 || userId.indexOf('\\') >= 0) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        User target = authService.findById(userId).orElse(null);
        if (target == null || !Roles.TA.equals(target.role)) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        ApplicantProfile p = profileService.getByUserId(userId).orElse(null);
        if (p == null || p.cvFileName == null || p.cvFileName.trim().isEmpty()) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        Path cvRoot = dataDir.resolve("cv").normalize();
        Path userCvDir = cvRoot.resolve(userId).normalize();
        if (!userCvDir.startsWith(cvRoot)) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        Path file = userCvDir.resolve(p.cvFileName).normalize();
        if (!file.startsWith(userCvDir) || !Files.isRegularFile(file)) {
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

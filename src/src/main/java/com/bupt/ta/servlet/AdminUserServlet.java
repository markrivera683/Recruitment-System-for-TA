package com.bupt.ta.servlet;

import com.bupt.ta.model.Roles;
import com.bupt.ta.model.User;
import com.bupt.ta.service.ApplicationService;
import com.bupt.ta.service.AuthService;
import com.bupt.ta.service.ProfileService;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.Optional;
import java.util.stream.Stream;

@WebServlet(urlPatterns = {"/admin/users"})
public class AdminUserServlet extends BaseServlet {

    private AuthService auth;
    private ApplicationService applications;
    private ProfileService profiles;
    private Path dataDir;

    @Override
    public void init() {
        dataDir = Paths.get(getServletContext().getRealPath("/WEB-INF/data"));
        auth = new AuthService(dataDir);
        applications = new ApplicationService(dataDir);
        profiles = new ProfileService(dataDir);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (!ensureAdmin(req, resp)) {
            return;
        }

        User admin = currentUser(req);
        String action = req.getParameter("action");
        String userId = req.getParameter("userId");
        String ctx = req.getContextPath();

        if (userId == null || userId.trim().isEmpty()) {
            resp.sendRedirect(ctx + "/admin?msg=" + enc("Missing user id."));
            return;
        }
        userId = userId.trim();

        if (admin != null && userId.equals(admin.id)) {
            resp.sendRedirect(ctx + "/admin?msg=" + enc("You cannot modify or remove your own account."));
            return;
        }

        Optional<User> targetOpt = auth.findById(userId);
        if (!targetOpt.isPresent()) {
            resp.sendRedirect(ctx + "/admin?msg=" + enc("User not found."));
            return;
        }
        User target = targetOpt.get();

        if (Roles.ADMIN.equals(target.role) && auth.countAdmins() <= 1) {
            resp.sendRedirect(ctx + "/admin?msg=" + enc("Cannot remove or deactivate the last administrator."));
            return;
        }

        try {
            if ("deactivate".equals(action)) {
                auth.setUserActive(userId, false);
            } else if ("delete".equals(action)) {
                applications.deleteByUserId(userId);
                profiles.deleteByUserId(userId);
                deleteCvDir(dataDir, userId);
                auth.removeUserRecord(userId);
            } else {
                resp.sendRedirect(ctx + "/admin?msg=" + enc("Unknown action."));
                return;
            }
        } catch (Exception e) {
            resp.sendRedirect(ctx + "/admin?msg=" + enc("Operation failed: " + e.getMessage()));
            return;
        }

        resp.sendRedirect(ctx + "/admin");
    }

    private static String enc(String s) {
        return URLEncoder.encode(s == null ? "" : s, StandardCharsets.UTF_8);
    }

    private static void deleteCvDir(Path dataDir, String userId) throws IOException {
        Path dir = dataDir.resolve("cv").resolve(userId);
        if (!Files.exists(dir)) {
            return;
        }
        try (Stream<Path> walk = Files.walk(dir)) {
            walk.sorted(Comparator.reverseOrder()).forEach(p -> {
                try {
                    Files.deleteIfExists(p);
                } catch (IOException ignored) {
                }
            });
        }
    }
}

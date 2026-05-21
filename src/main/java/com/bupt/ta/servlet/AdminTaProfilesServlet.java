package com.bupt.ta.servlet;

import com.bupt.ta.model.ApplicantProfile;
import com.bupt.ta.model.Roles;
import com.bupt.ta.model.TaResumeDisplay;
import com.bupt.ta.model.User;
import com.bupt.ta.model.EducationEntry;
import com.bupt.ta.service.AuthService;
import com.bupt.ta.service.ProfileService;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@WebServlet("/admin/ta-profiles")
public class AdminTaProfilesServlet extends BaseServlet {

    private AuthService authService;
    private ProfileService profileService;

    @Override
    public void init() {
        String data = getServletContext().getRealPath("/WEB-INF/data");
        java.nio.file.Path dataDir = Paths.get(data);
        this.authService = new AuthService(dataDir);
        this.profileService = new ProfileService(dataDir);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (!ensureAdmin(req, resp)) {
            return;
        }
        List<User> all = authService.listAllUsers();
        List<TaResumeDisplay> rows = new ArrayList<>();
        for (User u : all) {
            if (u == null || !Roles.TA.equals(u.role)) {
                continue;
            }
            Optional<ApplicantProfile> opt = profileService.getByUserId(u.id);
            ApplicantProfile p = opt.orElse(null);
            List<EducationEntry> edu = p != null
                    ? ProfileService.parseEducationJson(p.educationJson)
                    : new ArrayList<>();
            rows.add(new TaResumeDisplay(u, p, edu));
        }
        rows.sort(Comparator.comparing(AdminTaProfilesServlet::sortKey, String.CASE_INSENSITIVE_ORDER));
        req.setAttribute("taResumes", rows);
        req.getRequestDispatcher("/WEB-INF/jsp/admin/ta-profiles.jsp").forward(req, resp);
    }

    private static String sortKey(TaResumeDisplay t) {
        if (t.profile.fullName != null && !t.profile.fullName.trim().isEmpty()) {
            return t.profile.fullName.trim();
        }
        if (t.user.name != null && !t.user.name.trim().isEmpty()) {
            return t.user.name.trim();
        }
        return t.user.email != null ? t.user.email : "";
    }
}

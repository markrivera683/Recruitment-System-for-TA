package com.bupt.ta.servlet;

import com.bupt.ta.model.ApplicantProfile;
import com.bupt.ta.model.User;
import com.bupt.ta.service.ProfileService;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.file.Path;

@WebServlet(urlPatterns = {"/profile"})
public class ProfileServlet extends BaseServlet {
    private ProfileService profiles;

    @Override
    public void init() {
        Path dataDir = Path.of(getServletContext().getRealPath("/WEB-INF/data"));
        profiles = new ProfileService(dataDir);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User u = currentUser(req);
        if (u == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }
        ApplicantProfile p = profiles.getByUserId(u.id).orElse(new ApplicantProfile(u.id));
        req.setAttribute("profile", p);
        req.getRequestDispatcher("/WEB-INF/jsp/profile.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
        User u = currentUser(req);
        if (u == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }
        ApplicantProfile p = new ApplicantProfile(u.id);
        p.degreeProgramme = req.getParameter("degreeProgramme");
        p.yearOfStudy = req.getParameter("yearOfStudy");
        p.skills = req.getParameter("skills");
        p.availability = req.getParameter("availability");
        p.selfIntro = req.getParameter("selfIntro");
        profiles.upsert(p);
        resp.sendRedirect(req.getContextPath() + "/profile");
    }
}

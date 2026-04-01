package com.bupt.ta.servlet;

import com.bupt.ta.model.ApplicantProfile;
import com.bupt.ta.model.EducationEntry;
import com.bupt.ta.model.User;
import com.bupt.ta.service.AuthService;
import com.bupt.ta.service.ProfileService;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@WebServlet(urlPatterns = {"/profile"})
@MultipartConfig(
        fileSizeThreshold = 0,
        maxFileSize = 10 * 1024 * 1024,
        maxRequestSize = 12 * 1024 * 1024
)
public class ProfileServlet extends BaseServlet {
    private ProfileService profiles;
    private AuthService auth;
    private Path dataDir;

    @Override
    public void init() {
        dataDir = Paths.get(getServletContext().getRealPath("/WEB-INF/data"));
        profiles = new ProfileService(dataDir);
        auth = new AuthService(dataDir);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        User u = currentUser(req);
        if (u == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }
        Optional<ApplicantProfile> existing = profiles.getByUserId(u.id);
        ApplicantProfile p = existing.orElse(new ApplicantProfile(u.id));
        boolean editable = !existing.isPresent() || "1".equals(req.getParameter("edit"));
        mergeDefaultsFromUser(u, p);
        req.setAttribute("profile", p);
        req.setAttribute("user", u);
        req.setAttribute("editable", editable);
        List<EducationEntry> edus = ProfileService.parseEducationJson(p.educationJson);
        if (edus.isEmpty()) {
            edus = new ArrayList<>();
            edus.add(new EducationEntry());
        }
        req.setAttribute("educationList", edus);
        req.getRequestDispatcher("/WEB-INF/jsp/profile.jsp").forward(req, resp);
    }

    private static void mergeDefaultsFromUser(User u, ApplicantProfile p) {
        if ((p.fullName == null || p.fullName.trim().isEmpty()) && u.name != null) {
            p.fullName = u.name;
        }
        if ((p.studentId == null || p.studentId.trim().isEmpty()) && u.studentId != null) {
            p.studentId = u.studentId;
        }
        if ((p.email == null || p.email.trim().isEmpty()) && u.email != null) {
            p.email = u.email;
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        User u = currentUser(req);
        if (u == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        Optional<ApplicantProfile> existing = profiles.getByUserId(u.id);
        ApplicantProfile p = new ApplicantProfile(u.id);
        p.fullName = trim(req.getParameter("fullName"));
        p.gender = trim(req.getParameter("gender"));
        p.degree = trim(req.getParameter("degree"));
        p.major = trim(req.getParameter("major"));
        p.studentId = trim(req.getParameter("studentId"));
        p.idCard = trim(req.getParameter("idCard"));
        p.phone = trim(req.getParameter("phone"));
        p.email = trim(req.getParameter("email"));
        p.courses = req.getParameter("courses");
        if (p.courses != null) {
            p.courses = p.courses.replace("\r\n", "\n").replace("\r", "\n");
        }
        p.freeTime = trim(req.getParameter("freeTime"));
        p.skills = req.getParameter("skills");
        if (p.skills != null) {
            p.skills = p.skills.replace("\r\n", "\n").replace("\r", "\n");
        }

        List<EducationEntry> eduRows = buildEducationFromRequest(req);
        p.educationJson = ProfileService.buildEducationJson(eduRows);

        p.cvFileName = existing.map(e -> e.cvFileName).orElse("");
        p.degreeProgramme = existing.map(e -> e.degreeProgramme).orElse("");
        p.yearOfStudy = existing.map(e -> e.yearOfStudy).orElse("");
        p.availability = existing.map(e -> e.availability).orElse("");
        p.selfIntro = existing.map(e -> e.selfIntro).orElse("");

        Part cvPart = req.getPart("cv");
        if (cvPart != null && cvPart.getSize() > 0) {
            String submitted = cvPart.getSubmittedFileName();
            String safe = safeFileName(submitted);
            Path cvUserDir = dataDir.resolve("cv").resolve(u.id);
            Files.createDirectories(cvUserDir);
            Path dest = cvUserDir.resolve(safe);
            String oldName = p.cvFileName;
            cvPart.write(dest.toAbsolutePath().toString());
            p.cvFileName = safe;
            if (oldName != null && !oldName.isEmpty() && !oldName.equals(safe)) {
                Path oldPath = cvUserDir.resolve(oldName);
                Files.deleteIfExists(oldPath);
            }
        }

        try {
            auth.updateUserBasics(u, p.fullName, p.studentId, p.email);
            req.getSession().setAttribute("user", u);
            profiles.upsert(p);
        } catch (IllegalArgumentException ex) {
            req.setAttribute("error", ex.getMessage());
            req.setAttribute("profile", p);
            req.setAttribute("user", u);
            req.setAttribute("editable", true);
            List<EducationEntry> edus = ProfileService.parseEducationJson(p.educationJson);
            if (edus.isEmpty()) {
                edus = new ArrayList<>();
                edus.add(new EducationEntry());
            }
            req.setAttribute("educationList", edus);
            req.getRequestDispatcher("/WEB-INF/jsp/profile.jsp").forward(req, resp);
            return;
        }

        resp.sendRedirect(req.getContextPath() + "/profile");
    }

    private static String trim(String s) {
        return s != null ? s.trim() : "";
    }

    private static List<EducationEntry> buildEducationFromRequest(HttpServletRequest req) {
        String[] schools = req.getParameterValues("edu_school");
        if (schools == null) return new ArrayList<>();
        String[] degrees = req.getParameterValues("edu_degree");
        String[] majors = req.getParameterValues("edu_major");
        String[] periods = req.getParameterValues("edu_period");
        List<EducationEntry> list = new ArrayList<>();
        for (int i = 0; i < schools.length; i++) {
            String school = trim(schools[i]);
            String degree = (degrees != null && i < degrees.length) ? trim(degrees[i]) : "";
            String major = (majors != null && i < majors.length) ? trim(majors[i]) : "";
            String period = (periods != null && i < periods.length) ? trim(periods[i]) : "";
            if (school.isEmpty() && degree.isEmpty() && major.isEmpty() && period.isEmpty()) {
                continue;
            }
            list.add(new EducationEntry(school, degree, major, period));
        }
        return list;
    }

    private static String safeFileName(String submitted) {
        if (submitted == null) return "cv.pdf";
        String name = submitted;
        int p = Math.max(name.lastIndexOf('\\'), name.lastIndexOf('/'));
        if (p >= 0) {
            name = name.substring(p + 1);
        }
        name = name.trim();
        if (name.isEmpty()) return "cv.pdf";
        if (name.contains("..")) {
            name = name.replace("..", "_");
        }
        return name;
    }
}

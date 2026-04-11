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
import javax.servlet.http.HttpSession;
import javax.servlet.http.Part;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

@WebServlet(urlPatterns = {"/profile"})
@MultipartConfig(
        fileSizeThreshold = 0,
        maxFileSize = 10 * 1024 * 1024,
        maxRequestSize = 12 * 1024 * 1024
)
public class ProfileServlet extends BaseServlet {
    /** Session key: CV file name on disk not yet written to profiles.json (validation failed after upload). */
    public static final String PENDING_CV_SESSION_ATTR = "taPendingCvFileName";

    private static final Pattern NAME_PATTERN = Pattern.compile("^[\\p{L} .'-]{2,60}$");
    private static final Pattern STUDENT_ID_PATTERN = Pattern.compile("^[A-Za-z0-9_-]{4,30}$");
    private static final Pattern ID_CARD_PATTERN = Pattern.compile("^[A-Za-z0-9]{8,30}$");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^\\+?[0-9()\\-\\s]{6,25}$");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");

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
        String msg = req.getParameter("msg");
        mergeDefaultsFromUser(u, p);
        applyPendingCvFromSession(req, u.id, p);
        req.setAttribute("profile", p);
        req.setAttribute("user", u);
        req.setAttribute("editable", editable);
        if (msg != null && !msg.trim().isEmpty()) {
            req.setAttribute("infoMessage", escapeHtml(msg.trim()));
        }
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
        final String dbCvFileNameAtStart = existing
                .map(e -> e.cvFileName == null ? "" : e.cvFileName.trim())
                .filter(s -> !s.isEmpty())
                .orElse("");

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

        Path cvUserDir = dataDir.resolve("cv").resolve(u.id);
        Part cvPart = req.getPart("cv");
        final boolean newCvUpload = cvPart != null && cvPart.getSize() > 0;
        if (!newCvUpload) {
            mergePendingCvIntoProfileForPost(req, u.id, p, cvUserDir);
        }

        String preDeleteCvName = p.cvFileName == null ? "" : p.cvFileName.trim();
        boolean deleteCv = "1".equals(req.getParameter("deleteCv"));
        if (deleteCv) {
            p.cvFileName = "";
        }

        String uploadedName = null;
        Path uploadedPath = null;
        if (newCvUpload) {
            String submitted = cvPart.getSubmittedFileName();
            String safe = safeFileName(submitted);
            if (!isAllowedCvFile(safe)) {
                p.cvFileName = preDeleteCvName;
                Map<String, String> cvError = new LinkedHashMap<>();
                cvError.put("cv", "Invalid CV file type. Please upload PDF, DOC, or DOCX.");
                forwardProfileEdit(req, resp, u, p, eduRows, cvError);
                return;
            }
            Files.createDirectories(cvUserDir);
            Path dest = cvUserDir.resolve(safe);
            cvPart.write(dest.toAbsolutePath().toString());
            uploadedName = safe;
            uploadedPath = dest;
            p.cvFileName = safe;
        }

        Map<String, String> fieldErrors = validateProfileInput(p, eduRows);
        if (!fieldErrors.isEmpty()) {
            setPendingCvSessionIfFileExists(req, u.id, p, cvUserDir);
            forwardProfileEdit(req, resp, u, p, eduRows, fieldErrors);
            return;
        }

        try {
            auth.updateUserBasics(u, p.fullName, p.studentId, p.email);
            req.getSession().setAttribute("user", u);
            profiles.upsert(p);
            req.getSession().removeAttribute(PENDING_CV_SESSION_ATTR);
            if (!dbCvFileNameAtStart.isEmpty()) {
                boolean replacedByDifferent = uploadedName != null && !dbCvFileNameAtStart.equals(uploadedName);
                boolean deletedWithoutReplace = deleteCv && uploadedName == null;
                if (replacedByDifferent) {
                    archiveReplacedCv(cvUserDir, dbCvFileNameAtStart);
                } else if (deletedWithoutReplace) {
                    Files.deleteIfExists(cvUserDir.resolve(dbCvFileNameAtStart));
                }
            }
        } catch (IllegalArgumentException ex) {
            if (uploadedPath != null && uploadedName != null
                    && !uploadedName.equals(dbCvFileNameAtStart)) {
                Files.deleteIfExists(uploadedPath);
            }
            p.cvFileName = dbCvFileNameAtStart;
            mergePendingCvIntoProfileForPost(req, u.id, p, cvUserDir);
            Map<String, String> authErrors = new LinkedHashMap<>();
            authErrors.put("email", ex.getMessage());
            List<EducationEntry> edus = ProfileService.parseEducationJson(p.educationJson);
            if (edus.isEmpty()) {
                edus = new ArrayList<>();
                edus.add(new EducationEntry());
            }
            forwardProfileEdit(req, resp, u, p, edus, authErrors);
            return;
        } catch (Exception ex) {
            if (uploadedPath != null && uploadedName != null
                    && !uploadedName.equals(dbCvFileNameAtStart)) {
                try {
                    Files.deleteIfExists(uploadedPath);
                } catch (IOException ignored) {
                    // best-effort cleanup
                }
            }
            p.cvFileName = dbCvFileNameAtStart;
            mergePendingCvIntoProfileForPost(req, u.id, p, cvUserDir);
            Map<String, String> saveErr = new LinkedHashMap<>();
            saveErr.put("cv", "Could not save your profile. Your CV upload was cancelled. Please try again.");
            forwardProfileEdit(req, resp, u, p, eduRows, saveErr);
            return;
        }

        resp.sendRedirect(req.getContextPath() + "/profile");
    }

    /**
     * When the browser sends no new {@code cv} part, restore the in-memory filename from a prior
     * failed save (session + file on disk) so validation re-runs with the correct CV state.
     */
    private void mergePendingCvIntoProfileForPost(HttpServletRequest req, String userId,
                                                  ApplicantProfile p, Path cvUserDir) {
        HttpSession s = req.getSession(false);
        if (s == null) {
            return;
        }
        Object o = s.getAttribute(PENDING_CV_SESSION_ATTR);
        if (!(o instanceof String)) {
            return;
        }
        String pend = ((String) o).trim();
        if (pend.isEmpty()) {
            return;
        }
        Path pendFile = cvUserDir.resolve(pend);
        try {
            if (!Files.isRegularFile(pendFile)) {
                s.removeAttribute(PENDING_CV_SESSION_ATTR);
                return;
            }
        } catch (Exception e) {
            s.removeAttribute(PENDING_CV_SESSION_ATTR);
            return;
        }
        String dbName = p.cvFileName == null ? "" : p.cvFileName.trim();
        if (dbName.isEmpty() || !dbName.equals(pend)) {
            p.cvFileName = pend;
        }
    }

    private void applyPendingCvFromSession(HttpServletRequest req, String userId, ApplicantProfile p) {
        HttpSession s = req.getSession(false);
        if (s == null) {
            return;
        }
        Object o = s.getAttribute(PENDING_CV_SESSION_ATTR);
        if (!(o instanceof String)) {
            return;
        }
        String pend = ((String) o).trim();
        if (pend.isEmpty()) {
            return;
        }
        Path f = dataDir.resolve("cv").resolve(userId).resolve(pend);
        try {
            if (Files.isRegularFile(f)) {
                p.cvFileName = pend;
            } else {
                s.removeAttribute(PENDING_CV_SESSION_ATTR);
            }
        } catch (Exception e) {
            s.removeAttribute(PENDING_CV_SESSION_ATTR);
        }
    }

    private static void setPendingCvSessionIfFileExists(HttpServletRequest req, String userId,
                                                        ApplicantProfile p, Path cvUserDir) {
        HttpSession s = req.getSession(true);
        String fn = p.cvFileName == null ? "" : p.cvFileName.trim();
        if (fn.isEmpty()) {
            s.removeAttribute(PENDING_CV_SESSION_ATTR);
            return;
        }
        Path f = cvUserDir.resolve(fn);
        try {
            if (Files.isRegularFile(f)) {
                s.setAttribute(PENDING_CV_SESSION_ATTR, fn);
            } else {
                s.removeAttribute(PENDING_CV_SESSION_ATTR);
            }
        } catch (Exception e) {
            s.removeAttribute(PENDING_CV_SESSION_ATTR);
        }
    }

    private void forwardProfileEdit(HttpServletRequest req, HttpServletResponse resp, User u,
                                    ApplicantProfile p, List<EducationEntry> eduRows,
                                    Map<String, String> fieldErrors) throws ServletException, IOException {
        req.setAttribute("profile", p);
        req.setAttribute("user", u);
        req.setAttribute("editable", true);
        req.setAttribute("educationList", eduRows.isEmpty() ? defaultEduList() : eduRows);
        req.setAttribute("fieldErrors", fieldErrors);
        req.getRequestDispatcher("/WEB-INF/jsp/profile.jsp").forward(req, resp);
    }

    /** Moves the previous CV into {@code _archive/} when the applicant uploads a new file. */
    private static void archiveReplacedCv(Path cvUserDir, String oldName) throws IOException {
        Path oldPath = cvUserDir.resolve(oldName);
        if (!Files.isRegularFile(oldPath)) {
            return;
        }
        Path archiveDir = cvUserDir.resolve("_archive");
        Files.createDirectories(archiveDir);
        String stamp = String.valueOf(System.currentTimeMillis());
        Path dest = archiveDir.resolve(stamp + "_" + oldName);
        Files.move(oldPath, dest, StandardCopyOption.REPLACE_EXISTING);
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

    private static List<EducationEntry> defaultEduList() {
        List<EducationEntry> edus = new ArrayList<>();
        edus.add(new EducationEntry());
        return edus;
    }

    private static Map<String, String> validateProfileInput(ApplicantProfile p, List<EducationEntry> edus) {
        Map<String, String> errors = new LinkedHashMap<>();
        if (isBlank(p.fullName) || !NAME_PATTERN.matcher(p.fullName).matches()) {
            errors.put("fullName", "Please enter a valid full name (2-60 letters).");
        }
        if (!("Male".equals(p.gender) || "Female".equals(p.gender) || "Other".equals(p.gender))) {
            errors.put("gender", "Please select a valid gender.");
        }
        if (isBlank(p.degree)) errors.put("degree", "Degree is required.");
        if (isBlank(p.major)) errors.put("major", "Major is required.");
        if (isBlank(p.studentId) || !STUDENT_ID_PATTERN.matcher(p.studentId).matches()) {
            errors.put("studentId", "Please enter a valid student ID (letters/numbers, 4-30 chars).");
        }
        if (isBlank(p.idCard) || !ID_CARD_PATTERN.matcher(p.idCard).matches()) {
            errors.put("idCard", "Please enter a valid national ID (8-30 letters/numbers).");
        }
        if (isBlank(p.phone) || !PHONE_PATTERN.matcher(p.phone).matches()) {
            errors.put("phone", "Please enter a valid phone number.");
        }
        if (isBlank(p.email) || !EMAIL_PATTERN.matcher(p.email).matches()) {
            errors.put("email", "Please enter a valid email address.");
        }
        if (isBlank(p.courses)) errors.put("courses", "Courses completed is required.");
        if (isBlank(p.freeTime)) errors.put("freeTime", "Availability is required.");
        if (isBlank(p.skills)) errors.put("skills", "Skills is required.");

        if (edus == null || edus.isEmpty()) {
            errors.put("education", "Please add at least one education background record.");
            return errors;
        }
        for (int i = 0; i < edus.size(); i++) {
            EducationEntry e = edus.get(i);
            if (isBlank(e.school)) errors.put("edu_school_" + i, "School is required.");
            if (isBlank(e.degree)) errors.put("edu_degree_" + i, "Degree is required.");
            if (isBlank(e.major)) errors.put("edu_major_" + i, "Major is required.");
            if (isBlank(e.period)) errors.put("edu_period_" + i, "Period is required.");
        }
        return errors;
    }

    private static boolean isAllowedCvFile(String fileName) {
        if (fileName == null) return false;
        String n = fileName.toLowerCase();
        return n.endsWith(".pdf") || n.endsWith(".doc") || n.endsWith(".docx");
    }

    private static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    private static String escapeHtml(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
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

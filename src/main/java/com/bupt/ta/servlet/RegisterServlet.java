package com.bupt.ta.servlet;

import com.bupt.ta.model.ApplicantProfile;
import com.bupt.ta.model.User;
import com.bupt.ta.service.AuthService;
import com.bupt.ta.service.ProfileService;
import com.bupt.ta.util.ApplicantFieldValidation;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

@WebServlet(urlPatterns = {"/register"})
public class RegisterServlet extends BaseServlet {
    private AuthService auth;
    private ProfileService profiles;

    @Override
    public void init() {
        Path dataDir = Paths.get(getServletContext().getRealPath("/WEB-INF/data"));
        auth = new AuthService(dataDir);
        profiles = new ProfileService(dataDir);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.getRequestDispatcher("/WEB-INF/jsp/register.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        String name = trim(req.getParameter("name"));
        String studentId = trim(req.getParameter("studentId"));
        String email = trim(req.getParameter("email"));
        String password = req.getParameter("password");
        String confirm = req.getParameter("confirm");
        String phoneInput = trim(req.getParameter("phone"));
        String phoneNorm = ApplicantFieldValidation.normalizeChinaPhone(phoneInput);

        Map<String, String> fieldErrors = validateRegistration(
                name, studentId, email, password, confirm, phoneInput, phoneNorm);
        if (!fieldErrors.isEmpty()) {
            repopulateRegisterForm(req, name, studentId, email, phoneInput);
            req.setAttribute("fieldErrors", fieldErrors);
            req.getRequestDispatcher("/WEB-INF/jsp/register.jsp").forward(req, resp);
            return;
        }

        try {
            User u = auth.register(name, studentId, email, password);
            ApplicantProfile pr = new ApplicantProfile(u.id);
            pr.fullName = name;
            pr.studentId = studentId;
            pr.phone = phoneNorm;
            pr.email = email;
            pr.educationJson = ProfileService.buildEducationJson(new ArrayList<>());
            profiles.upsert(pr);
            req.getSession(true).setAttribute("user", u);
            resp.sendRedirect(req.getContextPath() + "/job");
        } catch (IllegalArgumentException e) {
            repopulateRegisterForm(req, name, studentId, email, phoneInput);
            Map<String, String> fe = new LinkedHashMap<>();
            String msg = e.getMessage() != null ? e.getMessage() : "Registration failed.";
            if (msg.toLowerCase().contains("email")) {
                fe.put("email", msg);
            }
            req.setAttribute("fieldErrors", fe);
            if (fe.isEmpty()) {
                req.setAttribute("error", msg);
            }
            req.getRequestDispatcher("/WEB-INF/jsp/register.jsp").forward(req, resp);
        } catch (IOException e) {
            repopulateRegisterForm(req, name, studentId, email, phoneInput);
            req.setAttribute("error", "Registration could not be completed. Please try again.");
            req.getRequestDispatcher("/WEB-INF/jsp/register.jsp").forward(req, resp);
        }
    }

    private static void repopulateRegisterForm(HttpServletRequest req, String name, String studentId,
                                               String email, String phoneInput) {
        req.setAttribute("vName", name);
        req.setAttribute("vStudentId", studentId);
        req.setAttribute("vEmail", email);
        req.setAttribute("vPhone", phoneInput);
    }

    private static Map<String, String> validateRegistration(String name, String studentId, String email,
                                                            String password, String confirm,
                                                            String phoneInput, String phoneNorm) {
        Map<String, String> errors = new LinkedHashMap<>();
        if (isBlank(name) || !ApplicantFieldValidation.isValidFullName(name)) {
            errors.put("name", "Please enter a valid full name (2-60 letters).");
        }
        if (!ApplicantFieldValidation.isValidBuptTenDigitStudentId(studentId)) {
            errors.put("studentId",
                    "Student ID must be exactly 10 digits; the first 4 digits are your admission year (e.g. 2023xxxxxxxx).");
        }
        if (isBlank(email) || !ApplicantFieldValidation.isValidEmailWithRealDomain(email)) {
            errors.put("email", "Please enter a valid email with a real domain (e.g. name@bupt.edu.cn).");
        }
        if (isBlank(phoneInput)) {
            errors.put("phone", "Phone is required.");
        } else if (phoneNorm == null || !ApplicantFieldValidation.isValidChinaMobileNormalized(phoneNorm)) {
            errors.put("phone",
                    "Please enter a China mobile number: +86 and 11 digits (e.g. +8613912345678 or 13912345678).");
        }
        if (password == null || password.trim().isEmpty()) {
            errors.put("password", "Password is required.");
        } else if (confirm == null || !password.equals(confirm)) {
            errors.put("confirm", "Passwords do not match.");
        }
        return errors;
    }

    private static String trim(String s) {
        return s != null ? s.trim() : "";
    }

    private static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }
}

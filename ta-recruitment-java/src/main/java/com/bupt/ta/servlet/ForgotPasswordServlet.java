package com.bupt.ta.servlet;

import com.bupt.ta.service.AuthService;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.file.Path;

@WebServlet(urlPatterns = {"/forgot-password"})
public class ForgotPasswordServlet extends BaseServlet {
    private AuthService auth;

    @Override
    public void init() {
        Path dataDir = Path.of(getServletContext().getRealPath("/WEB-INF/data"));
        auth = new AuthService(dataDir);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.getRequestDispatcher("/WEB-INF/jsp/forgot-password.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // Simplified for coursework: we do not send real email.
        // We just show a generic message to avoid leaking whether an email exists.
        req.setAttribute("message", "If the email is registered, a reset link/code would be sent.");
        req.getRequestDispatcher("/WEB-INF/jsp/forgot-password.jsp").forward(req, resp);
    }
}

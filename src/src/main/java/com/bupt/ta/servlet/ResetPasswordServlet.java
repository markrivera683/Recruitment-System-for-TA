package com.bupt.ta.servlet;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet(urlPatterns = {"/reset-password"})
public class ResetPasswordServlet extends BaseServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.getRequestDispatcher("/WEB-INF/jsp/reset-password.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // Placeholder: in a real system you'd validate token and update password.
        // For coursework prototype/demo, you can implement token storage in a file and update user password.
        req.setAttribute("message", "Password reset completed (demo placeholder). You can now log in.");
        req.getRequestDispatcher("/WEB-INF/jsp/reset-password.jsp").forward(req, resp);
    }
}

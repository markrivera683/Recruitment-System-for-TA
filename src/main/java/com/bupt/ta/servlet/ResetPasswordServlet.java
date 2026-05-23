package com.bupt.ta.servlet;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Displays the password-reset form and a demo completion message.
 *
 * <p><b>URL pattern:</b> {@code /reset-password}
 *
 * <p><b>Role access:</b> Public (no login required).
 *
 * <p>Prototype placeholder: token validation and actual password change are not implemented.
 */
@WebServlet(urlPatterns = {"/reset-password"})
public class ResetPasswordServlet extends BaseServlet {

    /**
     * Forwards to the reset-password JSP.
     *
     * @param req  the incoming request
     * @param resp the response
     * @throws ServletException if the JSP forward fails
     * @throws IOException      if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.getRequestDispatcher("/WEB-INF/jsp/reset-password.jsp").forward(req, resp);
    }

    /**
     * Accepts reset form submission and displays a demo completion message.
     *
     * @param req  the incoming request
     * @param resp the response; re-forwards to the reset-password JSP with a {@code message}
     *             attribute
     * @throws ServletException if the JSP forward fails
     * @throws IOException      if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        // Prototype placeholder: token validation not implemented.
        req.setAttribute("message",
            "Password reset completed (demo). You may now log in.");
        req.getRequestDispatcher("/WEB-INF/jsp/reset-password.jsp").forward(req, resp);
    }
}

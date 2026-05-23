package com.bupt.ta.servlet;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Displays the forgot-password form and a generic acknowledgment (no real email is sent).
 *
 * <p><b>URL pattern:</b> {@code /forgot-password}
 *
 * <p><b>Role access:</b> Public (no login required).
 *
 * <p>Coursework prototype: POST always shows a generic message to avoid leaking whether an email
 * is registered.
 */
@WebServlet(urlPatterns = {"/forgot-password"})
public class ForgotPasswordServlet extends BaseServlet {

    /**
     * Forwards to the forgot-password JSP.
     *
     * @param req  the incoming request
     * @param resp the response
     * @throws ServletException if the JSP forward fails
     * @throws IOException      if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.getRequestDispatcher("/WEB-INF/jsp/forgot-password.jsp").forward(req, resp);
    }

    /**
     * Accepts an email address and displays a generic success message without sending mail.
     *
     * @param req  the incoming request
     * @param resp the response; re-forwards to the forgot-password JSP with a {@code message}
     *             attribute
     * @throws ServletException if the JSP forward fails
     * @throws IOException      if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        // Simplified for coursework: no real email sent.
        // Show a generic message to avoid leaking whether the address is registered.
        req.setAttribute("message",
            "If that email is registered, a reset link would be sent.");
        req.getRequestDispatcher("/WEB-INF/jsp/forgot-password.jsp").forward(req, resp);
    }
}

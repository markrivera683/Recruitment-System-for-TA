package com.bupt.ta.servlet;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet(urlPatterns = {"/forgot-password"})
public class ForgotPasswordServlet extends BaseServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.getRequestDispatcher("/WEB-INF/jsp/forgot-password.jsp").forward(req, resp);
    }

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

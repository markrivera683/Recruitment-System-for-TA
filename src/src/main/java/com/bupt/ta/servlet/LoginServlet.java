package com.bupt.ta.servlet;

import com.bupt.ta.model.User;
import com.bupt.ta.service.AuthService;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;

@WebServlet(urlPatterns = {"/login"})
public class LoginServlet extends BaseServlet {
    private AuthService auth;

    @Override
    public void init() {
        Path dataDir = Path.of(getServletContext().getRealPath("/WEB-INF/data"));
        auth = new AuthService(dataDir);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.getRequestDispatcher("/WEB-INF/jsp/login.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
        String email = req.getParameter("email");
        String password = req.getParameter("password");
        Optional<User> u = auth.login(email, password);
        if (u.isEmpty()) {
            req.setAttribute("error", "Invalid email or password");
            req.getRequestDispatcher("/WEB-INF/jsp/login.jsp").forward(req, resp);
            return;
        }
        req.getSession(true).setAttribute("user", u.get());
        resp.sendRedirect(req.getContextPath() + "/profile");
    }
}

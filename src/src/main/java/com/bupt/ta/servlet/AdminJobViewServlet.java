package com.bupt.ta.servlet;

import com.bupt.ta.model.Job;
import com.bupt.ta.service.JobService;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet("/admin/job-view")
public class AdminJobViewServlet extends BaseServlet {

    private JobService jobService;

    @Override
    public void init() {
        String p = getServletContext().getRealPath("/WEB-INF/data/jobs.json");
        this.jobService = new JobService(p);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (!ensureAdmin(req, resp)) {
            return;
        }
        String id = req.getParameter("id");
        if (id == null || id.trim().isEmpty()) {
            resp.sendRedirect(req.getContextPath() + "/admin?msg=" + urlEncode("Missing job id."));
            return;
        }
        Job job = jobService.getJobById(id.trim());
        req.setAttribute("job", job);
        req.getRequestDispatcher("/WEB-INF/jsp/admin/job-view.jsp").forward(req, resp);
    }
}

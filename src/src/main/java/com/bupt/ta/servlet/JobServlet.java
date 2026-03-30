/*
功能：
/jobs → list
/job?id=xxx → detail
*/
package com.bupt.ta.servlet;

import com.bupt.ta.model.Job;
import com.bupt.ta.service.JobService;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

@WebServlet("/job")
public class JobServlet extends HttpServlet {

    private JobService jobService;

    @Override
    public void init() throws ServletException {
        String p = getServletContext().getRealPath("/WEB-INF/data/jobs.json");
        this.jobService = new JobService(p);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String id = req.getParameter("id");

        if (id == null) {
            // LIST PAGE
            List<Job> jobs = jobService.getAllJobs();
            req.setAttribute("jobs", jobs);
            req.getRequestDispatcher("/WEB-INF/jsp/jobs.jsp")
               .forward(req, resp);

        } else {
            // DETAIL PAGE
            Job job = jobService.getJobById(id);
            req.setAttribute("job", job);
            req.getRequestDispatcher("/WEB-INF/jsp/job-detail.jsp")
               .forward(req, resp);
        }
    }
}

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
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@WebServlet("/job")
public class JobServlet extends BaseServlet {

    private JobService jobService;

    @Override
    public void init() throws ServletException {
        String p = getServletContext().getRealPath("/WEB-INF/data/jobs.json");
        this.jobService = new JobService(p);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        if (currentUser(req) == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        String id = req.getParameter("id");

        if (id == null) {
            // LIST PAGE
            String q = safe(req.getParameter("q"));
            String sortBy = safe(req.getParameter("sortBy"));
            if (sortBy.isEmpty()) sortBy = "postingDate";

            List<Job> jobs = jobService.getAllJobs();
            jobs = applySearch(jobs, q);
            jobs = applySort(jobs, sortBy);

            req.setAttribute("jobs", jobs);
            req.setAttribute("q", q);
            req.setAttribute("sortBy", sortBy);
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

    private static String safe(String s) {
        return s == null ? "" : s.trim();
    }

    private static List<Job> applySearch(List<Job> jobs, String q) {
        if (q == null || q.isEmpty()) return jobs;
        String needle = q.toLowerCase(Locale.ROOT);
        return jobs.stream()
                .filter(j -> contains(j.getModuleName(), needle)
                        || contains(j.getActivityType(), needle)
                        || (j.getRequiredSkills() != null
                            && j.getRequiredSkills().stream().anyMatch(s -> contains(s, needle))))
                .collect(Collectors.toList());
    }

    private static List<Job> applySort(List<Job> jobs, String sortBy) {
        Comparator<Job> cmp;
        if ("moduleName".equals(sortBy)) {
            cmp = Comparator.comparing(j -> lower(j.getModuleName()));
        } else if ("activityType".equals(sortBy)) {
            cmp = Comparator.comparing(j -> lower(j.getActivityType()));
        } else {
            // postingDate desc (fallback to postDate)
            cmp = Comparator.comparing((Job j) -> lower(j.getPostDate())).reversed();
        }
        return jobs.stream().sorted(cmp).collect(Collectors.toList());
    }

    private static boolean contains(String s, String needle) {
        return s != null && s.toLowerCase(Locale.ROOT).contains(needle);
    }

    private static String lower(String s) {
        return s == null ? "" : s.toLowerCase(Locale.ROOT);
    }
}

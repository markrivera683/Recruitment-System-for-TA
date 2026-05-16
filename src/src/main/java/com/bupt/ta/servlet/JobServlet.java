/*
功能：
/job → list
/job?id=xxx → detail
POST action=toggleFavorite → save/unsave job
*/
package com.bupt.ta.servlet;

import com.bupt.ta.model.Job;
import com.bupt.ta.model.User;
import com.bupt.ta.service.FavoriteService;
import com.bupt.ta.service.JobService;
import com.bupt.ta.util.JobListFilters;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;
import java.util.Set;

@WebServlet("/job")
public class JobServlet extends BaseServlet {

    private JobService jobService;
    private FavoriteService favoriteService;

    @Override
    public void init() throws ServletException {
        String dataDir = getServletContext().getRealPath("/WEB-INF/data");
        String p = dataDir + "/jobs.json";
        this.jobService = new JobService(p);
        this.favoriteService = new FavoriteService(Paths.get(dataDir));
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        User user = currentUser(req);
        if (user == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        String id = req.getParameter("id");

        if (id == null) {
            // ── LIST PAGE ──
            String q = safe(req.getParameter("q"));
            String sortBy = safe(req.getParameter("sortBy"));
            if (sortBy.isEmpty()) {
                sortBy = "postingDate";
            }

            Set<String> favoriteIds = favoriteService.getFavoriteJobIds(user.id);
            List<Job> jobs = JobListFilters.apply(jobService.listPublishedJobs(), favoriteIds, q, sortBy);

            req.setAttribute("jobs", jobs);
            req.setAttribute("q", q);
            req.setAttribute("sortBy", sortBy);

            req.getRequestDispatcher("/WEB-INF/jsp/jobs.jsp")
                    .forward(req, resp);

        } else {
            // ── DETAIL PAGE ──
            Job job = jobService.getJobById(id);
            req.setAttribute("job", job);
            if (job != null && job.getId() != null) {
                req.setAttribute("jobFavorited", favoriteService.isFavorite(user.id, job.getId()));
            } else {
                req.setAttribute("jobFavorited", Boolean.FALSE);
            }

            req.getRequestDispatcher("/WEB-INF/jsp/job-detail.jsp")
                    .forward(req, resp);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        User user = currentUser(req);
        String ctx = req.getContextPath();
        if (user == null) {
            resp.sendRedirect(ctx + "/login");
            return;
        }

        if (!"toggleFavorite".equals(req.getParameter("action"))) {
            resp.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
            return;
        }

        String jobId = safe(req.getParameter("jobId"));
        if (jobId.isEmpty()) {
            resp.sendRedirect(ctx + "/job");
            return;
        }

        try {
            favoriteService.toggleFavorite(user.id, jobId);
        } catch (IOException e) {
            resp.sendRedirect(ctx + "/job?id=" + urlEncode(jobId) + "&err=" + urlEncode("Could not update favorites."));
            return;
        }

        String returnTo = safe(req.getParameter("returnTo"));
        if ("list".equals(returnTo)) {
            String q = safe(req.getParameter("q"));
            String sortBy = safe(req.getParameter("sortBy"));
            if (sortBy.isEmpty()) {
                sortBy = "postingDate";
            }
            StringBuilder url = new StringBuilder(ctx).append("/job?sortBy=").append(urlEncode(sortBy));
            if (!q.isEmpty()) {
                url.append("&q=").append(urlEncode(q));
            }
            resp.sendRedirect(url.toString());
            return;
        }

        resp.sendRedirect(ctx + "/job?id=" + urlEncode(jobId));
    }

    private static String safe(String s) {
        return s == null ? "" : s.trim();
    }
}

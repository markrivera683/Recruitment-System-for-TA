package com.bupt.ta.servlet;

import com.bupt.ta.model.Application;
import com.bupt.ta.model.Roles;
import com.bupt.ta.model.TaWorkloadStats;
import com.bupt.ta.model.User;
import com.bupt.ta.service.ApplicationService;
import com.bupt.ta.service.AuthService;
import com.bupt.ta.service.JobService;
import com.bupt.ta.service.WorkloadService;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@WebServlet(urlPatterns = {"/admin"})
public class AdminServlet extends BaseServlet {
    private AuthService auth;
    private ApplicationService applications;
    private JobService jobs;
    private WorkloadService workloadService;

    @Override
    public void init() {
        Path dataDir = Paths.get(getServletContext().getRealPath("/WEB-INF/data"));
        auth = new AuthService(dataDir);
        applications = new ApplicationService(dataDir);
        String jobsPath = getServletContext().getRealPath("/WEB-INF/data/jobs.json");
        jobs = new JobService(jobsPath);
        workloadService = new WorkloadService();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        if (!ensureAdmin(req, resp)) {
            return;
        }
        String msg = req.getParameter("msg");
        if (msg != null && !msg.trim().isEmpty()) {
            req.setAttribute("adminMessage", msg);
        }
        List<User> users = auth.listAllUsers();
        List<Application> appList = applications.listAll();
        req.setAttribute("applications", appList);
        req.setAttribute("users", users);
        req.setAttribute("jobs", jobs.getAllJobs());

        Map<String, TaWorkloadStats> taWorkload = workloadService.buildTaWorkloadStats(users, appList);
        req.setAttribute("taWorkload", taWorkload);

        List<User> taUsersWorkloadOrder = users.stream()
                .filter(user -> user != null && Roles.TA.equals(user.role)
                        && user.id != null && !user.id.trim().isEmpty())
                .sorted(Comparator
                        .comparingInt((User user) -> {
                            TaWorkloadStats row = taWorkload.get(user.id.trim());
                            return row == null ? 0 : row.accepted;
                        }).reversed()
                        .thenComparingInt((User user) -> {
                            TaWorkloadStats row = taWorkload.get(user.id.trim());
                            return row == null ? 0 : row.total;
                        }).reversed()
                        .thenComparing(user -> user.name != null ? user.name : "",
                                String.CASE_INSENSITIVE_ORDER))
                .collect(Collectors.toList());
        req.setAttribute("taUsersWorkloadOrder", taUsersWorkloadOrder);

        req.getRequestDispatcher("/WEB-INF/jsp/admin/dashboard.jsp").forward(req, resp);
    }
}

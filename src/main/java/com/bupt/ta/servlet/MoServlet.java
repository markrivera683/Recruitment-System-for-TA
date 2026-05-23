package com.bupt.ta.servlet;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.bupt.ta.ai.LmConfig;
import com.bupt.ta.model.Application;
import com.bupt.ta.model.Job;
import com.bupt.ta.model.MoWorkloadSnapshot;
import com.bupt.ta.model.User;
import com.bupt.ta.service.ApplicationService;
import com.bupt.ta.service.AuthService;
import com.bupt.ta.service.JobService;
import com.bupt.ta.service.ProfileService;
import com.bupt.ta.service.WorkloadService;

/**
 * Module owner (MO) dashboard and job/application management.
 *
 * <p><b>URL pattern:</b> {@code /mo} (mapped in {@code web.xml})
 *
 * <p><b>Role access:</b> {@link com.bupt.ta.model.Roles#MO} only via {@link #ensureMo}.
 *
 * <p>GET renders the MO dashboard with jobs, applications, workload snapshots, and CV links.
 * POST handles job creation, publishing, and application approve/reject actions.
 */
public class MoServlet extends BaseServlet {
    private ApplicationService applications;
    private JobService jobs;
    private AuthService auth;
    private WorkloadService workloadService;

    /**
     * Initializes application, auth, job, and workload services from {@code WEB-INF/data}.
     */
    @Override
    public void init() {
        Path dataDir = Paths.get(getServletContext().getRealPath("/WEB-INF/data"));
        applications = new ApplicationService(dataDir);
        auth = new AuthService(dataDir);
        workloadService = new WorkloadService();
        String jobsPath = getServletContext().getRealPath("/WEB-INF/data/jobs.json");
        jobs = new JobService(jobsPath);
    }

    /**
     * Loads MO dashboard data including pending workload snapshots and CV availability.
     *
     * @param req  the incoming request; optional {@code msg} flash message
     * @param resp the response; 403 or redirect when not MO
     * @throws ServletException if the JSP forward fails
     * @throws IOException      if data loading fails
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        if (!ensureMo(req, resp)) {
            return;
        }

        String msg = req.getParameter("msg");
        if (msg != null && !msg.trim().isEmpty()) {
            req.setAttribute("moMessage", msg.trim());
        }

        req.setAttribute("jobs", jobs.getAllJobs());
        List<Application> apps = applications.listAll();
        req.setAttribute("applications", apps);

        Path dataDir = Paths.get(getServletContext().getRealPath("/WEB-INF/data"));
        ProfileService profiles = new ProfileService(dataDir);

        List<Job> allJobs = jobs.getAllJobs();
        Map<String, String> applicantNamesByUserId = new HashMap<>();
        for (Application a : apps) {
            if (a == null || a.userId == null || applicantNamesByUserId.containsKey(a.userId)) {
                continue;
            }
            String uid = a.userId.trim();
            String name = auth.findById(uid).map(u -> u.name).orElse(null);
            if (name == null || name.trim().isEmpty()) {
                name = profiles.getByUserId(uid).map(p -> p.fullName).orElse(uid);
            }
            applicantNamesByUserId.put(uid, name);
        }
        Map<String, MoWorkloadSnapshot> workloadSnapshots =
                workloadService.buildSnapshotsForPendingApplications(apps, allJobs, applicantNamesByUserId);
        req.setAttribute("workloadSnapshots", workloadSnapshots);
        req.setAttribute("aiEnabled", LmConfig.load(getServletContext()).isEnabled());

        Map<String, String> cvByUserId = new HashMap<>();
        for (Application a : apps) {
            if (a == null || a.userId == null || cvByUserId.containsKey(a.userId)) {
                continue;
            }
            String fn = profiles.getByUserId(a.userId)
                    .map(pr -> pr.cvFileName)
                    .filter(s -> s != null && !s.trim().isEmpty())
                    .orElse(null);
            cvByUserId.put(a.userId, fn);
        }
        req.setAttribute("cvByUserId", cvByUserId);

        req.getRequestDispatcher("/WEB-INF/jsp/mo/dashboard.jsp").forward(req, resp);
    }

    /**
     * Handles MO actions: create job, publish job, approve or reject application.
     *
     * @param req  the incoming request; {@code action} is {@code createJob}, {@code publishJob},
     *             {@code approveApp}, or {@code rejectApp} with action-specific parameters
     * @param resp the response; redirects to {@code /mo} with a flash message
     * @throws ServletException if dispatch fails
     * @throws IOException      if persistence fails
     */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        if (!ensureMo(req, resp)) {
            return;
        }
        req.setCharacterEncoding("UTF-8");

        String action = n(req.getParameter("action"));
        String ctx = req.getContextPath();
        User mo = currentUser(req);

        if ("createJob".equals(action)) {
            String moduleName = n(req.getParameter("moduleName"));
            String moduleCode = n(req.getParameter("moduleCode"));
            String activityType = n(req.getParameter("activityType"));
            String description = n(req.getParameter("description"));
            String deadline = n(req.getParameter("applicationDeadline"));
            String numberOfTAs = n(req.getParameter("numberOfTAs"));
            String workload = n(req.getParameter("workloadHours"));
            String duration = n(req.getParameter("duration"));
            boolean publishNow = "1".equals(req.getParameter("publishNow"));

            if (moduleName.isEmpty() || moduleCode.isEmpty() || description.isEmpty()) {
                resp.sendRedirect(ctx + "/mo?msg=" + urlEncode("Module name, code and description are required."));
                return;
            }

            Job j = new Job();
            String today = LocalDate.now().toString();
            j.setModuleName(moduleName);
            j.setModuleCode(moduleCode);
            j.setActivityType(activityType.isEmpty() ? "Teaching Assistant" : activityType);
            j.setDescription(description);
            j.setApplicationDeadline(deadline);
            j.setNumberOfTAs(numberOfTAs.isEmpty() ? "2" : numberOfTAs);
            j.setDuration(duration.isEmpty() ? "One semester" : duration);
            j.setWorkloadHours(workload);
            j.setRequiredSkills(splitSkills(req.getParameter("requiredSkills")));
            j.setCreatedByMoId(mo == null ? "" : n(mo.id));
            j.setCreatedAt(today);

            if (publishNow) {
                j.setStatus("Published");
                j.setPostDate(today);
                j.setPublishedAt(today);
            } else {
                j.setStatus("Draft");
                j.setPostDate("");
                j.setPublishedAt("");
            }

            jobs.createJob(j);
            resp.sendRedirect(ctx + "/mo?msg=" + urlEncode(publishNow ? "Job published." : "Draft saved."));
            return;
        }

        if ("publishJob".equals(action)) {
            String jobId = n(req.getParameter("jobId"));
            if (jobId.isEmpty()) {
                resp.sendRedirect(ctx + "/mo?msg=" + urlEncode("Missing job id."));
                return;
            }
            boolean ok = jobs.publishJob(jobId, mo == null ? "" : n(mo.id));
            resp.sendRedirect(ctx + "/mo?msg=" + urlEncode(ok ? "Job published successfully." : "Job not found."));
            return;
        }

        if ("approveApp".equals(action) || "rejectApp".equals(action)) {
            String appId = n(req.getParameter("appId"));
            String feedback = n(req.getParameter("feedback"));
            if (appId.isEmpty()) {
                resp.sendRedirect(ctx + "/mo?msg=" + urlEncode("Missing application id."));
                return;
            }
            String newStatus = "approveApp".equals(action) ? "Accepted" : "Rejected";
            applications.updateStatus(appId, newStatus, feedback);
            resp.sendRedirect(ctx + "/mo?msg=" + urlEncode("Application updated to " + newStatus + "."));
            return;
        }

        resp.sendRedirect(ctx + "/mo?msg=" + urlEncode("Unknown action."));
    }

    private static String n(String s) {
        return s == null ? "" : s.trim();
    }

    private static List<String> splitSkills(String raw) {
        List<String> out = new ArrayList<>();
        if (raw == null) return out;
        String[] arr = raw.split("[,\\n\\r]");
        for (String p : arr) {
            String t = p == null ? "" : p.trim();
            if (!t.isEmpty()) out.add(t);
        }
        return out;
    }
}
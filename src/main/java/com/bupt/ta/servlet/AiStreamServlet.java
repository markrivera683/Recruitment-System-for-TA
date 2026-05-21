package com.bupt.ta.servlet;

import com.bupt.ta.ai.LmClient;
import com.bupt.ta.ai.LmClientFactory;
import com.bupt.ta.ai.LmConfig;
import com.bupt.ta.ai.LmException;
import com.bupt.ta.ai.LmRequest;
import com.bupt.ta.ai.LmStreamListener;
import com.bupt.ta.model.ApplicantProfile;
import com.bupt.ta.model.Job;
import com.bupt.ta.model.MoWorkloadSnapshot;
import com.bupt.ta.model.Roles;
import com.bupt.ta.model.User;
import com.bupt.ta.service.ApplicationService;
import com.bupt.ta.service.AuthService;
import com.bupt.ta.service.FavoriteService;
import com.bupt.ta.service.JobService;
import com.bupt.ta.service.ProfileService;
import com.bupt.ta.service.WorkloadService;
import com.bupt.ta.util.JobListFilters;
import com.bupt.ta.service.ai.MissingSkillService;
import com.bupt.ta.service.ai.RecommendationService;
import com.bupt.ta.service.ai.SkillMatchService;
import com.bupt.ta.service.ai.WorkloadAdviceService;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * SSE stream of AI completions (Markdown deltas) for job recommendation and skill analysis.
 * Protocol: {@code data: {"type":"meta"|"delta"|"done"|"error", "b64": "..."}} (UTF-8 text in Base64).
 * Mapped in {@code web.xml} as {@code /api/ai/stream}.
 */
public class AiStreamServlet extends BaseServlet {

    private JobService jobService;
    private FavoriteService favoriteService;
    private ProfileService profileService;
    private RecommendationService recommendationService;
    private SkillMatchService skillMatchService;
    private MissingSkillService missingSkillService;
    private WorkloadAdviceService workloadAdviceService;
    private ApplicationService applicationService;
    private AuthService authService;
    private WorkloadService workloadService;

    @Override
    public void init() throws ServletException {
        String dataDir = getServletContext().getRealPath("/WEB-INF/data");
        String p = dataDir + "/jobs.json";
        this.jobService = new JobService(p);
        this.favoriteService = new FavoriteService(Paths.get(dataDir));
        this.profileService = new ProfileService(Paths.get(dataDir));
        this.applicationService = new ApplicationService(Paths.get(dataDir));
        this.authService = new AuthService(Paths.get(dataDir));
        this.workloadService = new WorkloadService();

        LmConfig lmConfig = LmConfig.load(getServletContext());
        LmClient client = LmClientFactory.create(lmConfig);
        this.recommendationService = new RecommendationService(client, lmConfig);
        this.skillMatchService = new SkillMatchService(client, lmConfig);
        this.missingSkillService = new MissingSkillService(client, lmConfig);
        this.workloadAdviceService = new WorkloadAdviceService(client, lmConfig);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        User user = currentUser(req);
        if (user == null) {
            resp.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        LmConfig lmConfig = LmConfig.load(getServletContext());
        if (!lmConfig.isEnabled()) {
            beginSse(resp);
            writeError(resp.getWriter(), "AI features are disabled (LM_ENABLED=false).");
            return;
        }

        LmClient client = LmClientFactory.create(lmConfig);
        String feature = safe(req.getParameter("feature"));

        if ("moWorkloadAdvice".equals(feature)) {
            if (!ensureMoForStream(req, resp)) {
                return;
            }
            beginSse(resp);
            PrintWriter out = resp.getWriter();
            try {
                streamMoWorkloadAdvice(req, client, out);
            } catch (LmException e) {
                writeError(out, e.getMessage());
            } catch (Exception e) {
                String msg = e.getMessage();
                writeError(out, msg != null && !msg.isEmpty() ? msg : "AI stream failed");
            }
            return;
        }

        beginSse(resp);
        PrintWriter out = resp.getWriter();

        try {
            switch (feature) {
                case "recommendation":
                    streamRecommendation(req, client, out);
                    break;
                case "skillMatch":
                    streamSkillMatch(req, client, out);
                    break;
                case "missingSkills":
                    streamMissingSkills(req, client, out);
                    break;
                default:
                    writeError(out, "Unknown feature. Use recommendation, skillMatch, missingSkills, or moWorkloadAdvice.");
            }
        } catch (LmException e) {
            writeError(out, e.getMessage());
        } catch (Exception e) {
            String msg = e.getMessage();
            writeError(out, msg != null && !msg.isEmpty() ? msg : "AI stream failed");
        }
    }

    private static void beginSse(HttpServletResponse resp) {
        resp.setContentType("text/event-stream;charset=UTF-8");
        resp.setCharacterEncoding("UTF-8");
        resp.setHeader("Cache-Control", "no-cache, no-store");
        resp.setHeader("Connection", "keep-alive");
        resp.setHeader("X-Accel-Buffering", "no");
    }

    private void streamRecommendation(HttpServletRequest req, LmClient client, PrintWriter out)
            throws IOException, LmException {
        User user = currentUser(req);
        String q = safe(req.getParameter("q"));
        String sortBy = safe(req.getParameter("sortBy"));
        if (sortBy.isEmpty()) {
            sortBy = "postingDate";
        }
        Set<String> favoriteIds = favoriteService.getFavoriteJobIds(user.id);
        List<Job> jobs = JobListFilters.apply(jobService.listPublishedJobs(), favoriteIds, q, sortBy);

        Optional<ApplicantProfile> profileOpt = profileService.getByUserId(user.id);
        if (!profileOpt.isPresent()) {
            writeError(out, "Please complete your profile first to get AI recommendations.");
            return;
        }
        String candidateInfo = buildCandidateInfo(profileOpt.get());
        String positionsInfo = buildPositionsInfo(jobs);
        LmRequest lmReq = recommendationService.buildRecommendRequest(candidateInfo, positionsInfo);
        writeMeta(out, lmReq.getModel());
        client.stream(lmReq, sseListener(out));
    }

    private void streamSkillMatch(HttpServletRequest req, LmClient client, PrintWriter out)
            throws IOException, LmException {
        User user = currentUser(req);
        String jobId = safe(req.getParameter("jobId"));
        Job job = jobService.getJobById(jobId);
        if (job == null) {
            writeError(out, "Job not found.");
            return;
        }
        Optional<ApplicantProfile> profileOpt = profileService.getByUserId(user.id);
        if (!profileOpt.isPresent()) {
            writeError(out, "Please complete your profile first to get AI skill analysis.");
            return;
        }
        ApplicantProfile profile = profileOpt.get();
        String userSkills = profile.skills != null ? profile.skills : "";
        String jobSkills = job.getRequiredSkills() != null
                ? String.join(", ", job.getRequiredSkills()) : "";
        LmRequest lmReq = skillMatchService.buildMatchRequest(userSkills, jobSkills);
        writeMeta(out, lmReq.getModel());
        client.stream(lmReq, sseListener(out));
    }

    private void streamMissingSkills(HttpServletRequest req, LmClient client, PrintWriter out)
            throws IOException, LmException {
        User user = currentUser(req);
        String jobId = safe(req.getParameter("jobId"));
        Job job = jobService.getJobById(jobId);
        if (job == null) {
            writeError(out, "Job not found.");
            return;
        }
        Optional<ApplicantProfile> profileOpt = profileService.getByUserId(user.id);
        if (!profileOpt.isPresent()) {
            writeError(out, "Please complete your profile first to get AI skill analysis.");
            return;
        }
        ApplicantProfile profile = profileOpt.get();
        String userSkills = profile.skills != null ? profile.skills : "";
        String jobSkills = job.getRequiredSkills() != null
                ? String.join(", ", job.getRequiredSkills()) : "";
        LmRequest lmReq = missingSkillService.buildMissingRequest(userSkills, jobSkills);
        writeMeta(out, lmReq.getModel());
        client.stream(lmReq, sseListener(out));
    }

    private void streamMoWorkloadAdvice(HttpServletRequest req, LmClient client, PrintWriter out)
            throws IOException, LmException {
        String applicationId = safe(req.getParameter("applicationId"));
        if (applicationId.isEmpty()) {
            writeError(out, "Missing applicationId.");
            return;
        }

        List<com.bupt.ta.model.Application> applications = applicationService.listAll();
        List<Job> allJobs = jobService.getAllJobs();
        String applicantName = resolveApplicantName(applicationId, applications);
        MoWorkloadSnapshot snapshot = workloadService.buildSnapshotForApplication(
                applicationId, applications, allJobs, applicantName);
        if (snapshot == null) {
            writeError(out, "Application not found or not pending.");
            return;
        }

        LmRequest lmReq = workloadAdviceService.buildAdviceRequest(snapshot);
        writeMeta(out, lmReq.getModel());
        client.stream(lmReq, sseListener(out));
    }

    private String resolveApplicantName(String applicationId,
                                        List<com.bupt.ta.model.Application> applications)
            throws IOException {
        com.bupt.ta.model.Application target = null;
        for (com.bupt.ta.model.Application app : applications) {
            if (app != null && applicationId.equals(app.id)) {
                target = app;
                break;
            }
        }
        if (target == null || target.userId == null) {
            return "";
        }
        String userId = target.userId.trim();
        Optional<User> userOpt = authService.findById(userId);
        if (userOpt.isPresent()) {
            User u = userOpt.get();
            if (u.name != null && !u.name.trim().isEmpty()) {
                return u.name.trim();
            }
            return u.id != null ? u.id : userId;
        }
        Optional<ApplicantProfile> profileOpt = profileService.getByUserId(userId);
        if (profileOpt.isPresent()) {
            ApplicantProfile p = profileOpt.get();
            if (p.fullName != null && !p.fullName.trim().isEmpty()) {
                return p.fullName.trim();
            }
        }
        return userId;
    }

    /** MO-only check for SSE; returns false if response already committed. */
    private boolean ensureMoForStream(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        User u = currentUser(req);
        if (u == null) {
            resp.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return false;
        }
        if (!Roles.MO.equals(u.role)) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN);
            return false;
        }
        return true;
    }

    private static LmStreamListener sseListener(PrintWriter out) {
        return new LmStreamListener() {
            @Override
            public void onDelta(String text) {
                writeDelta(out, text);
                out.flush();
            }

            @Override
            public void onComplete(String model) {
                writeDone(out);
                out.flush();
            }

            @Override
            public void onError(String message) {
                writeError(out, message);
                out.flush();
            }
        };
    }

    private static void writeMeta(PrintWriter out, String model) {
        String b64 = Base64.getEncoder().encodeToString(
                (model != null ? model : "").getBytes(StandardCharsets.UTF_8));
        out.write("data: {\"type\":\"meta\",\"b64\":\"" + b64 + "\"}\n\n");
        out.flush();
    }

    private static void writeDelta(PrintWriter out, String delta) {
        String b64 = Base64.getEncoder().encodeToString(delta.getBytes(StandardCharsets.UTF_8));
        out.write("data: {\"type\":\"delta\",\"b64\":\"" + b64 + "\"}\n\n");
    }

    private static void writeDone(PrintWriter out) {
        out.write("data: {\"type\":\"done\"}\n\n");
    }

    private static void writeError(PrintWriter out, String message) {
        String b64 = Base64.getEncoder().encodeToString(
                (message != null ? message : "").getBytes(StandardCharsets.UTF_8));
        out.write("data: {\"type\":\"error\",\"b64\":\"" + b64 + "\"}\n\n");
        out.flush();
    }

    private String buildCandidateInfo(ApplicantProfile p) {
        StringBuilder sb = new StringBuilder();
        if (p.fullName != null && !p.fullName.isEmpty()) sb.append("Name: ").append(p.fullName).append("\n");
        if (p.major != null && !p.major.isEmpty()) sb.append("Major: ").append(p.major).append("\n");
        if (p.degree != null && !p.degree.isEmpty()) sb.append("Degree: ").append(p.degree).append("\n");
        if (p.skills != null && !p.skills.isEmpty()) sb.append("Skills: ").append(p.skills).append("\n");
        if (p.courses != null && !p.courses.isEmpty()) sb.append("Courses: ").append(p.courses).append("\n");
        if (p.freeTime != null && !p.freeTime.isEmpty()) sb.append("Availability: ").append(p.freeTime).append("\n");
        return sb.toString();
    }

    private String buildPositionsInfo(List<Job> jobs) {
        StringBuilder sb = new StringBuilder();
        for (Job j : jobs) {
            sb.append("- ").append(j.getModuleCode()).append(": ").append(j.getModuleName());
            if (j.getActivityType() != null) sb.append(" (").append(j.getActivityType()).append(")");
            if (j.getRequiredSkills() != null) {
                sb.append(" | Skills: ").append(String.join(", ", j.getRequiredSkills()));
            }
            sb.append("\n");
        }
        return sb.toString();
    }

    private static String safe(String s) {
        return s == null ? "" : s.trim();
    }

}

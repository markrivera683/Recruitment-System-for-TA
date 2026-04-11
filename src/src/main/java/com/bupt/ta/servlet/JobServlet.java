/*
功能：
/jobs → list
/job?id=xxx → detail
*/
package com.bupt.ta.servlet;

import com.bupt.ta.ai.LmClientFactory;
import com.bupt.ta.ai.LmConfig;
import com.bupt.ta.model.ApplicantProfile;
import com.bupt.ta.model.Job;
import com.bupt.ta.model.User;
import com.bupt.ta.service.JobService;
import com.bupt.ta.service.ProfileService;
import com.bupt.ta.service.ai.AiFeatureOutput;
import com.bupt.ta.service.ai.AiFeatureService;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;

@WebServlet("/job")
public class JobServlet extends BaseServlet {

    private JobService jobService;
    private ProfileService profileService;
    private AiFeatureService aiService;

    @Override
    public void init() throws ServletException {
        String dataDir = getServletContext().getRealPath("/WEB-INF/data");
        String p = dataDir + "/jobs.json";
        this.jobService = new JobService(p);
        this.profileService = new ProfileService(Paths.get(dataDir));

        LmConfig lmConfig = LmConfig.load(getServletContext());
        this.aiService = new AiFeatureService(lmConfig, LmClientFactory.create(lmConfig));
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
            if (sortBy.isEmpty()) sortBy = "postingDate";

            List<Job> jobs = jobService.listPublishedJobs();
            jobs = applySearch(jobs, q);
            jobs = applySort(jobs, sortBy);

            req.setAttribute("jobs", jobs);
            req.setAttribute("q", q);
            req.setAttribute("sortBy", sortBy);

            // AI recommendation (async-safe: runs only when user clicks "AI Recommend")
            if ("1".equals(req.getParameter("aiRec"))) {
                Optional<ApplicantProfile> profileOpt = profileService.getByUserId(user.id);
                if (profileOpt.isPresent()) {
                    ApplicantProfile profile = profileOpt.get();
                    String candidateInfo = buildCandidateInfo(profile);
                    String positionsInfo = buildPositionsInfo(jobs);
                    AiFeatureOutput recOutput = aiService.recommendJobs(candidateInfo, positionsInfo);
                    req.setAttribute("aiRecommendation", recOutput);
                } else {
                    req.setAttribute("aiRecommendation",
                            AiFeatureOutput.error("Please complete your profile first to get AI recommendations."));
                }
            }

            req.getRequestDispatcher("/WEB-INF/jsp/jobs.jsp")
               .forward(req, resp);

        } else {
            // ── DETAIL PAGE ──
            Job job = jobService.getJobById(id);
            req.setAttribute("job", job);

            // AI skill analysis (runs only when user clicks "AI Analyze")
            if ("1".equals(req.getParameter("aiAnalyze")) && job != null) {
                Optional<ApplicantProfile> profileOpt = profileService.getByUserId(user.id);
                if (profileOpt.isPresent()) {
                    ApplicantProfile profile = profileOpt.get();
                    String userSkills = profile.skills != null ? profile.skills : "";
                    String jobSkills = job.getRequiredSkills() != null
                            ? String.join(", ", job.getRequiredSkills()) : "";

                    AiFeatureOutput matchOutput = aiService.matchApplicantSkills(userSkills, jobSkills);
                    AiFeatureOutput gapOutput = aiService.identifyMissingSkills(userSkills, jobSkills);
                    req.setAttribute("aiSkillMatch", matchOutput);
                    req.setAttribute("aiMissingSkills", gapOutput);
                } else {
                    AiFeatureOutput noProfile = AiFeatureOutput.error(
                            "Please complete your profile first to get AI skill analysis.");
                    req.setAttribute("aiSkillMatch", noProfile);
                    req.setAttribute("aiMissingSkills", noProfile);
                }
            }

            req.getRequestDispatcher("/WEB-INF/jsp/job-detail.jsp")
               .forward(req, resp);
        }
    }

    /** Build a text summary of the candidate for the LM prompt. */
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

    /** Build a text summary of open positions for the LM prompt. */
    private String buildPositionsInfo(List<Job> jobs) {
        StringBuilder sb = new StringBuilder();
        for (Job j : jobs) {
            sb.append("- ").append(j.getModuleCode()).append(": ").append(j.getModuleName());
            if (j.getActivityType() != null) sb.append(" (").append(j.getActivityType()).append(")");
            if (j.getRequiredSkills() != null) sb.append(" | Skills: ").append(String.join(", ", j.getRequiredSkills()));
            sb.append("\n");
        }
        return sb.toString();
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

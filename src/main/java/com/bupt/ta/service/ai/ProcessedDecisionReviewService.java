package com.bupt.ta.service.ai;

import com.bupt.ta.ai.AiFeatureNames;
import com.bupt.ta.ai.LmClient;
import com.bupt.ta.ai.LmConfig;
import com.bupt.ta.ai.LmException;
import com.bupt.ta.ai.LmRequest;
import com.bupt.ta.ai.LmResponse;
import com.bupt.ta.model.Application;
import com.bupt.ta.model.ApplicantProfile;
import com.bupt.ta.model.Job;
import com.bupt.ta.model.MoProcessedReviewContext;
import com.bupt.ta.service.ProfileService;

/**
 * Generates MO-facing retrospective AI insight for processed applications
 * (Accepted / Rejected / Withdrawn).
 */
public final class ProcessedDecisionReviewService {
    private final LmClient client;
    private final LmConfig config;

    public ProcessedDecisionReviewService(LmClient client, LmConfig config) {
        this.client = client;
        this.config = config;
    }

    public LmResponse reviewProcessedDecision(MoProcessedReviewContext context) throws LmException {
        return client.generate(buildReviewRequest(context));
    }

    public LmRequest buildReviewRequest(MoProcessedReviewContext context) {
        return LmRequest.builder()
                .featureName(AiFeatureNames.DECISION_REVIEW)
                .systemPrompt("You help module organisers (MO) review past TA hiring decisions.\n"
                        + "Use ONLY the applicant profile, job details, recorded decision, and MO feedback provided.\n"
                        + "Do not invent qualifications or outcomes.\n"
                        + "Return concise Markdown with sections:\n"
                        + "## Fit Summary\n"
                        + "## Decision Assessment (Aligned / Questionable / Withdrawn)\n"
                        + "## Profile Highlights\n"
                        + "## Notes for Future Roles\n"
                        + "For Withdrawn applications, focus on fit summary and profile highlights only.")
                .userPrompt(buildUserPrompt(context))
                .temperature(0.25d)
                .maxTokens(550)
                .model(AiLmDefaults.modelOrFallback(config))
                .build();
    }

    public static MoProcessedReviewContext buildContext(
            Application app,
            Job job,
            ApplicantProfile profile,
            String applicantName) {
        MoProcessedReviewContext ctx = new MoProcessedReviewContext();
        if (app == null) {
            return ctx;
        }
        ctx.applicationId = app.id;
        ctx.applicantName = applicantName != null && !applicantName.trim().isEmpty()
                ? applicantName.trim()
                : (app.userId != null ? app.userId : "");
        ctx.moduleName = nz(app.moduleName);
        ctx.moduleCode = nz(app.moduleCode);
        ctx.role = nz(app.role);
        ctx.applicationDate = nz(app.applicationDate);
        ctx.decisionStatus = nz(app.status);
        ctx.moFeedback = app.feedback != null ? app.feedback.trim() : "";

        if (job != null) {
            ctx.jobDescription = nz(job.getDescription());
            ctx.jobActivityType = nz(job.getActivityType());
            ctx.workloadHours = nz(job.getWorkloadHours());
            if (job.getRequiredSkills() != null && !job.getRequiredSkills().isEmpty()) {
                ctx.jobSkills = String.join(", ", job.getRequiredSkills());
            }
        }

        if (profile != null && ProfileService.hasAiMatchingInput(profile)) {
            ctx.applicantCapabilities = ProfileService.buildAiCapabilityText(profile);
        } else {
            ctx.applicantCapabilities = "(Profile skills/courses not available)";
        }
        return ctx;
    }

    static String buildUserPrompt(MoProcessedReviewContext c) {
        if (c == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("Decision recorded: ").append(AiLmDefaults.nz(c.decisionStatus)).append("\n");
        if (c.moFeedback != null && !c.moFeedback.isEmpty()) {
            sb.append("MO feedback: ").append(c.moFeedback).append("\n");
        } else {
            sb.append("MO feedback: (none recorded)\n");
        }
        sb.append("\nApplicant: ").append(AiLmDefaults.nz(c.applicantName)).append("\n");
        sb.append("Application: ").append(AiLmDefaults.nz(c.moduleName));
        if (c.moduleCode != null && !c.moduleCode.isEmpty()) {
            sb.append(" [").append(c.moduleCode).append("]");
        }
        if (c.role != null && !c.role.isEmpty()) {
            sb.append(" — ").append(c.role);
        }
        sb.append("\nApplied: ").append(AiLmDefaults.nz(c.applicationDate)).append("\n\n");

        sb.append("Job requirements:\n");
        sb.append("- Activity: ").append(AiLmDefaults.nz(c.jobActivityType)).append("\n");
        sb.append("- Skills: ").append(c.jobSkills != null && !c.jobSkills.isEmpty()
                ? c.jobSkills : "(not specified)").append("\n");
        sb.append("- Workload: ").append(AiLmDefaults.nz(c.workloadHours)).append("\n");
        sb.append("- Description: ").append(c.jobDescription != null && !c.jobDescription.isEmpty()
                ? c.jobDescription : "(not specified)").append("\n\n");

        sb.append("Applicant profile:\n");
        sb.append(c.applicantCapabilities != null ? c.applicantCapabilities : "(not available)");
        return sb.toString();
    }

    private static String nz(String s) {
        return s == null ? "" : s.trim();
    }
}

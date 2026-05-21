package com.bupt.ta.service.ai;

import com.bupt.ta.ai.LmClient;
import com.bupt.ta.ai.LmConfig;
import com.bupt.ta.ai.LmException;
import com.bupt.ta.ai.LmResponse;
import com.bupt.ta.model.MoWorkloadSnapshot;

/**
 * Application-facing AI entry point. Servlets should depend on this class, not on {@link LmClient} directly.
 */
public final class AiFeatureService {
    private final LmConfig config;
    private final SkillMatchService skillMatch;
    private final MissingSkillService missingSkill;
    private final RecommendationService recommendation;
    private final WorkloadAdviceService workloadAdvice;

    public AiFeatureService(LmConfig config, LmClient client) {
        this.config = config;
        this.skillMatch = new SkillMatchService(client, config);
        this.missingSkill = new MissingSkillService(client, config);
        this.recommendation = new RecommendationService(client, config);
        this.workloadAdvice = new WorkloadAdviceService(client, config);
    }

    public AiFeatureOutput matchApplicantSkills(String applicantSkills, String jobRequirements) {
        return run(() -> skillMatch.matchApplicantSkills(applicantSkills, jobRequirements), "Skill match failed: ");
    }

    public AiFeatureOutput identifyMissingSkills(String candidateSkills, String requiredJobSkills) {
        return run(() -> missingSkill.identifyMissingSkills(candidateSkills, requiredJobSkills),
                "Missing-skill analysis failed: ");
    }

    public AiFeatureOutput recommendJobs(String candidateProfile, String openPositions) {
        return run(() -> recommendation.recommendJobs(candidateProfile, openPositions), "Job recommendation failed: ");
    }

    public AiFeatureOutput adviseMoOnWorkload(MoWorkloadSnapshot snapshot) {
        return run(() -> workloadAdvice.adviseMoOnWorkload(snapshot), "Workload advice failed: ");
    }

    private AiFeatureOutput run(LmCall call, String errPrefix) {
        if (!config.isEnabled()) {
            return AiFeatureOutput.disabled();
        }
        try {
            return AiFeatureOutput.fromResponse(call.get());
        } catch (LmException e) {
            return AiFeatureOutput.error(errPrefix + e.getMessage());
        }
    }

    @FunctionalInterface
    private interface LmCall {
        LmResponse get() throws LmException;
    }
}

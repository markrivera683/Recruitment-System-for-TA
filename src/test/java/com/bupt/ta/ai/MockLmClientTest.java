package com.bupt.ta.ai;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link MockLmClient} — deterministic outputs per {@link AiFeatureNames}.
 */
class MockLmClientTest {

    private final MockLmClient client = new MockLmClient();

    @Test
    void skillMatch_returnsStableScoreAndLists() throws LmException {
        LmRequest req = LmRequest.builder()
                .featureName(AiFeatureNames.SKILL_MATCH)
                .userPrompt("Applicant skills:\nJava, SQL\n\nJob requirements:\nJava, Docker, Kubernetes")
                .model("x")
                .build();
        LmResponse r = client.generate(req);
        assertTrue(r.isSuccess());
        assertEquals("mock", r.getProvider());
        assertTrue(r.getText().contains("Match score:"));
        assertTrue(r.getText().contains("java"));
        assertTrue(r.getText().contains("docker"));
    }

    @Test
    void missingSkills_listsGaps() throws LmException {
        LmRequest req = LmRequest.builder()
                .featureName(AiFeatureNames.MISSING_SKILLS)
                .userPrompt("Candidate skills:\nJava\n\nRequired job skills:\nJava, Docker")
                .build();
        LmResponse r = client.generate(req);
        assertTrue(r.isSuccess());
        assertTrue(r.getText().toLowerCase().contains("missing"));
        assertTrue(r.getText().toLowerCase().contains("docker"));
    }

    @Test
    void jobRecommendation_ranksLines() throws LmException {
        LmRequest req = LmRequest.builder()
                .featureName(AiFeatureNames.JOB_RECOMMENDATION)
                .userPrompt("Candidate profile:\nKnows Java.\n\nOpen positions:\nCS101 — Java lab\nCS999 — Other")
                .build();
        LmResponse r = client.generate(req);
        assertTrue(r.isSuccess());
        assertTrue(r.getText().contains("Recommended"));
        assertTrue(r.getText().contains("CS101"));
    }

    @Test
    void workloadAdvice_highLoadRecommendsCautionOrReject() throws LmException {
        LmRequest req = LmRequest.builder()
                .featureName(AiFeatureNames.WORKLOAD_ADVICE)
                .userPrompt("Accepted count: 1\nPending count: 3\nPotential load if approve: 4\nWarning threshold: 3\n")
                .build();
        LmResponse r = client.generate(req);
        assertTrue(r.isSuccess());
        String lower = r.getText().toLowerCase();
        assertTrue(lower.contains("reject") || lower.contains("caution"));
    }

    @Test
    void workloadAdvice_lowLoadRecommendsApprove() throws LmException {
        LmRequest req = LmRequest.builder()
                .featureName(AiFeatureNames.WORKLOAD_ADVICE)
                .userPrompt("Accepted count: 0\nPending count: 1\nPotential load if approve: 1\nWarning threshold: 3\n")
                .build();
        LmResponse r = client.generate(req);
        assertTrue(r.isSuccess());
        assertTrue(r.getText().toLowerCase().contains("approve"));
    }

    @Test
    void forceDisabled_returnsFailure() throws LmException {
        MockLmClient disabled = new MockLmClient(true);
        LmResponse r = disabled.generate(LmRequest.builder().featureName(AiFeatureNames.SKILL_MATCH).userPrompt("x").build());
        assertFalse(r.isSuccess());
        assertTrue(r.getErrorMessage().contains("disabled"));
    }
}

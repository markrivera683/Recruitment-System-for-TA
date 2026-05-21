package com.bupt.ta.nomvn;

import com.bupt.ta.ai.AiFeatureNames;
import com.bupt.ta.ai.LmClient;
import com.bupt.ta.ai.LmClientFactory;
import com.bupt.ta.ai.LmConfig;
import com.bupt.ta.ai.LmProviderType;
import com.bupt.ta.ai.LmRequest;
import com.bupt.ta.ai.LmResponse;
import com.bupt.ta.ai.MockLmClient;
import com.bupt.ta.service.ai.AiFeatureOutput;
import com.bupt.ta.service.ai.AiFeatureService;

import java.lang.reflect.Constructor;

/**
 * No-Maven smoke checks for LM workflow (uses plain javac/java, no JUnit).
 */
public final class NoMvnWorkflowChecks {

    public static void main(String[] args) throws Exception {
        testProviderParsing();
        testMockClientSkillMatch();
        testFactoryFallbackAndHttpSelection();
        testAiFeatureServiceWithMock();
        testDisabledFlow();
        System.out.println("[OK] No-Maven LM workflow checks passed.");
    }

    private static void testProviderParsing() {
        require(LmProviderType.fromString(null) == LmProviderType.MOCK, "null provider should map to MOCK");
        require(LmProviderType.fromString("openai") == LmProviderType.OPENAI, "openai should map to OPENAI");
        require(LmProviderType.fromString("custom") == LmProviderType.CUSTOM, "custom should map to CUSTOM");
    }

    private static void testMockClientSkillMatch() {
        LmClient mock = new MockLmClient();
        LmRequest req = LmRequest.builder()
                .featureName(AiFeatureNames.SKILL_MATCH)
                .userPrompt("Applicant skills:\nJava, SQL\n\nJob requirements:\nJava, Docker")
                .build();
        LmResponse resp = runGenerate(mock, req);
        require(resp.isSuccess(), "mock skill-match should succeed");
        require(resp.getText().contains("Match score"), "mock response should contain score");
    }

    private static void testFactoryFallbackAndHttpSelection() throws Exception {
        LmConfig openaiMissingCreds = newConfig(
                true, LmProviderType.OPENAI, "", "", "", 30000, "/chat/completions");
        LmClient c1 = LmClientFactory.create(openaiMissingCreds);
        require(c1 instanceof MockLmClient, "openai without creds should fallback to mock");

        LmConfig openaiWithCreds = newConfig(
                true, LmProviderType.OPENAI, "sk-test", "https://api.example.com/v1", "", 30000, "/chat/completions");
        LmClient c2 = LmClientFactory.create(openaiWithCreds);
        require(!(c2 instanceof MockLmClient), "openai with creds should select http client");
    }

    private static void testAiFeatureServiceWithMock() throws Exception {
        LmConfig mockCfg = newConfig(true, LmProviderType.MOCK, "", "", "", 30000, "/chat/completions");
        AiFeatureService svc = new AiFeatureService(mockCfg, LmClientFactory.create(mockCfg));
        AiFeatureOutput out = svc.recommendJobs("Java student", "CS101 Lab TA\nCS201 Grading");
        require(out.isSuccess(), "recommendJobs should succeed with mock");
        require(out.getText() != null && !out.getText().isEmpty(), "recommendJobs text should not be empty");
    }

    private static void testDisabledFlow() throws Exception {
        LmConfig disabled = newConfig(false, LmProviderType.MOCK, "", "", "", 30000, "/chat/completions");
        AiFeatureService svc = new AiFeatureService(disabled, LmClientFactory.create(disabled));
        AiFeatureOutput out = svc.matchApplicantSkills("Java", "Java,Docker");
        require(!out.isSuccess(), "disabled flow should return unsuccessful output");
        require(out.getErrorMessage() != null && out.getErrorMessage().contains("disabled"),
                "disabled flow should include message");
    }

    private static LmResponse runGenerate(LmClient client, LmRequest req) {
        try {
            return client.generate(req);
        } catch (Exception e) {
            throw new IllegalStateException("generate() failed: " + e.getMessage(), e);
        }
    }

    private static LmConfig newConfig(boolean enabled, LmProviderType type, String key,
                                      String baseUrl, String model, int timeoutMs, String chatPath) throws Exception {
        Constructor<LmConfig> ctor = LmConfig.class.getDeclaredConstructor(
                boolean.class, LmProviderType.class, String.class, String.class, String.class, int.class, String.class);
        ctor.setAccessible(true);
        return ctor.newInstance(enabled, type, key, baseUrl, model, timeoutMs, chatPath);
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new IllegalStateException("Check failed: " + message);
        }
    }

    private NoMvnWorkflowChecks() {}
}

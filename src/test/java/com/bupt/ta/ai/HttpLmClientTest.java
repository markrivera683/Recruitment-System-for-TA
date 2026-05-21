package com.bupt.ta.ai;

import com.bupt.ta.testsupport.LmTestSupport;
import org.junit.jupiter.api.Test;

import javax.servlet.ServletContext;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * {@link HttpLmClient} — fails fast when credentials are incomplete (no real network).
 */
class HttpLmClientTest {

    @Test
    void generate_withoutCredentials_throwsLmException() {
        ServletContext ctx = LmTestSupport.servletContextWithLmProperties(
                "LM_PROVIDER=openai\nLM_API_KEY=\nLM_BASE_URL=\n");
        LmConfig cfg = LmConfig.load(ctx);
        HttpLmClient http = new HttpLmClient(cfg);

        LmRequest req = LmRequest.builder()
                .featureName(AiFeatureNames.SKILL_MATCH)
                .systemPrompt("sys")
                .userPrompt("user")
                .build();

        LmException ex = assertThrows(LmException.class, () -> http.generate(req));
        assertEquals("HTTP LM requires LM_BASE_URL and LM_API_KEY.", ex.getMessage());
    }
}

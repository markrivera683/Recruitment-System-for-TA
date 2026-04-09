package com.bupt.ta.ai;

import com.bupt.ta.testsupport.LmTestSupport;
import org.junit.jupiter.api.Test;

import javax.servlet.ServletContext;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;

/**
 * {@link LmClientFactory#create(LmConfig)} — mock vs HTTP vs fallback.
 */
class LmClientFactoryTest {

    @Test
    void nullConfig_returnsMock() {
        assertInstanceOf(MockLmClient.class, LmClientFactory.create(null));
    }

    @Test
    void disabled_returnsMock() {
        ServletContext ctx = LmTestSupport.servletContextWithLmProperties("LM_ENABLED=false\n");
        LmConfig cfg = LmConfig.load(ctx);
        assertInstanceOf(MockLmClient.class, LmClientFactory.create(cfg));
    }

    @Test
    void providerMock_returnsMock() {
        ServletContext ctx = LmTestSupport.servletContextWithLmProperties("LM_PROVIDER=mock\n");
        LmConfig cfg = LmConfig.load(ctx);
        assertInstanceOf(MockLmClient.class, LmClientFactory.create(cfg));
    }

    @Test
    void providerOpenAi_withoutCredentials_fallsBackToMock() {
        ServletContext ctx = LmTestSupport.servletContextWithLmProperties(
                "LM_PROVIDER=openai\nLM_API_KEY=\nLM_BASE_URL=\n");
        LmConfig cfg = LmConfig.load(ctx);
        assertInstanceOf(MockLmClient.class, LmClientFactory.create(cfg));
    }

    @Test
    void providerOpenAi_withCredentials_returnsHttpClient() {
        ServletContext ctx = LmTestSupport.servletContextWithLmProperties(
                "LM_PROVIDER=openai\nLM_API_KEY=sk-test\nLM_BASE_URL=https://api.example.com/v1\n");
        LmConfig cfg = LmConfig.load(ctx);
        assertInstanceOf(HttpLmClient.class, LmClientFactory.create(cfg));
    }
}

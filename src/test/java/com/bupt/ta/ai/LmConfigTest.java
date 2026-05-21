package com.bupt.ta.ai;

import com.bupt.ta.testsupport.LmTestSupport;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import javax.servlet.ServletContext;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link LmConfig#load(javax.servlet.ServletContext)} — file-based defaults and overrides.
 */
class LmConfigTest {

    @AfterEach
    void clearLmSystemProps() {
        System.clearProperty("LM_PROVIDER");
        System.clearProperty("LM_BASE_URL");
        System.clearProperty("LM_API_KEY");
    }

    @Test
    void load_noFile_usesBuiltInDefaults() {
        ServletContext ctx = LmTestSupport.servletContextWithLmProperties(null);
        LmConfig c = LmConfig.load(ctx);
        assertTrue(c.isEnabled());
        assertEquals(LmProviderType.MOCK, c.getProviderType());
        assertEquals("", c.getApiKey());
        assertEquals("", c.getBaseUrl());
        assertEquals(30_000, c.getTimeoutMs());
        assertEquals("/chat/completions", c.getHttpChatPath());
    }

    @Test
    void load_fromPropertiesFile() {
        String props = ""
                + "LM_ENABLED=false\n"
                + "LM_PROVIDER=openai\n"
                + "LM_API_KEY=secret\n"
                + "LM_BASE_URL=https://example.com/v1\n"
                + "LM_MODEL=my-model\n"
                + "LM_TIMEOUT_MS=5000\n"
                + "LM_HTTP_CHAT_PATH=/v1/chat\n";
        LmConfig c = LmConfig.load(LmTestSupport.servletContextWithLmProperties(props));
        assertFalse(c.isEnabled());
        assertEquals(LmProviderType.OPENAI, c.getProviderType());
        assertEquals("secret", c.getApiKey());
        assertEquals("https://example.com/v1", c.getBaseUrl());
        assertEquals("my-model", c.getModel());
        assertEquals(5000, c.getTimeoutMs());
        assertEquals("/v1/chat", c.getHttpChatPath());
        assertTrue(c.hasHttpCredentials());
    }

}

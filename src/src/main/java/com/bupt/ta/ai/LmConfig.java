package com.bupt.ta.ai;

import com.bupt.ta.util.AppConfig;

import javax.servlet.ServletContext;
import java.util.Properties;

/**
 * LM integration settings. Safe defaults: mock provider, no outbound HTTP required.
 */
public final class LmConfig {
    private final boolean enabled;
    private final LmProviderType providerType;
    private final String apiKey;
    private final String baseUrl;
    private final String model;
    private final int timeoutMs;
    /** Relative to {@link #baseUrl}, e.g. /chat/completions for OpenAI-compatible APIs. */
    private final String httpChatPath;

    private LmConfig(boolean enabled, LmProviderType providerType, String apiKey, String baseUrl,
                     String model, int timeoutMs, String httpChatPath) {
        this.enabled = enabled;
        this.providerType = providerType;
        this.apiKey = apiKey != null ? apiKey : "";
        this.baseUrl = baseUrl != null ? baseUrl.trim() : "";
        this.model = model != null ? model.trim() : "";
        this.timeoutMs = timeoutMs;
        this.httpChatPath = httpChatPath != null && !httpChatPath.isEmpty() ? httpChatPath : "/chat/completions";
    }

    public static LmConfig load(ServletContext ctx) {
        Properties p = AppConfig.loadWebInfProperties(ctx, "lm.properties");
        String enabledStr = AppConfig.resolve(
                "LM_ENABLED",
                new String[]{"LM_ENABLED", "lm.enabled"},
                p,
                "LM_ENABLED",
                "true");
        boolean enabled = parseBoolean(enabledStr, true);

        String providerRaw = AppConfig.resolve(
                "LM_PROVIDER",
                new String[]{"LM_PROVIDER", "lm.provider"},
                p,
                "LM_PROVIDER",
                "mock");
        LmProviderType type = LmProviderType.fromString(providerRaw);

        String apiKey = AppConfig.resolve(
                "LM_API_KEY",
                new String[]{"LM_API_KEY", "lm.apiKey"},
                p,
                "LM_API_KEY",
                "");

        String baseUrl = AppConfig.resolve(
                "LM_BASE_URL",
                new String[]{"LM_BASE_URL", "lm.baseUrl"},
                p,
                "LM_BASE_URL",
                "");

        String model = AppConfig.resolve(
                "LM_MODEL",
                new String[]{"LM_MODEL", "lm.model"},
                p,
                "LM_MODEL",
                "");

        String timeoutStr = AppConfig.resolve(
                "LM_TIMEOUT_MS",
                new String[]{"LM_TIMEOUT_MS", "lm.timeoutMs"},
                p,
                "LM_TIMEOUT_MS",
                "30000");
        int timeoutMs = parseInt(timeoutStr, 30_000);

        String httpPath = AppConfig.resolve(
                "LM_HTTP_CHAT_PATH",
                new String[]{"LM_HTTP_CHAT_PATH", "lm.httpChatPath"},
                p,
                "LM_HTTP_CHAT_PATH",
                "/chat/completions");

        return new LmConfig(enabled, type, apiKey, baseUrl, model, timeoutMs, httpPath);
    }

    private static boolean parseBoolean(String s, boolean defaultVal) {
        if (s == null || s.isEmpty()) return defaultVal;
        if ("1".equals(s) || "true".equalsIgnoreCase(s) || "yes".equalsIgnoreCase(s)) {
            return true;
        }
        if ("0".equals(s) || "false".equalsIgnoreCase(s) || "no".equalsIgnoreCase(s)) {
            return false;
        }
        return defaultVal;
    }

    private static int parseInt(String s, int defaultVal) {
        try {
            return Integer.parseInt(s.trim());
        } catch (Exception e) {
            return defaultVal;
        }
    }

    public boolean isEnabled() {
        return enabled;
    }

    public LmProviderType getProviderType() {
        return providerType;
    }

    public String getApiKey() {
        return apiKey;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public String getModel() {
        return model;
    }

    public int getTimeoutMs() {
        return timeoutMs;
    }

    public String getHttpChatPath() {
        return httpChatPath;
    }

    /** True when HTTP provider could attempt a real call (still subject to runtime errors). */
    public boolean hasHttpCredentials() {
        return !apiKey.isEmpty() && !baseUrl.isEmpty();
    }
}

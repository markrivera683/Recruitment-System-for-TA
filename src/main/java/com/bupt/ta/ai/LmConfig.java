package com.bupt.ta.ai;

import com.bupt.ta.util.AppConfig;

import javax.servlet.ServletContext;
import java.util.Properties;

/**
 * Immutable snapshot of language-model integration settings for the TA application.
 *
 * <p>{@link LmConfig} is the configuration hub of the LM layer. Settings are loaded once
 * per web application from {@code WEB-INF/lm.properties} and environment variables via
 * {@link AppConfig#resolve}, then consumed by {@link LmClientFactory} to choose between
 * {@link MockLmClient} and {@link HttpLmClient}.
 *
 * <p><strong>Configuration keys</strong> (each also accepts a dotted {@code lm.*} property
 * name in {@code lm.properties}; see {@link #load}):
 * <ul>
 *   <li>{@code LM_ENABLED} — default {@code true}; when {@code false}, factory returns
 *       {@link MockLmClient} with {@code forceDisabled} and AI features report as disabled</li>
 *   <li>{@code LM_PROVIDER} — default {@code mock}; maps to {@link LmProviderType}
 *       ({@code mock}, {@code openai}, {@code custom}); unknown values fall back to
 *       {@link LmProviderType#MOCK}</li>
 *   <li>{@code LM_API_KEY} — default empty; required with {@code LM_BASE_URL} for HTTP providers</li>
 *   <li>{@code LM_BASE_URL} — default empty; provider API root (no trailing path required)</li>
 *   <li>{@code LM_MODEL} — default empty; global default model; per-request override via
 *       {@link LmRequest#getModel()}; HTTP client falls back to {@link LmModelDefaults#CHAT_FALLBACK}</li>
 *   <li>{@code LM_TIMEOUT_MS} — default {@code 30000}; HTTP read/connect timeout in milliseconds</li>
 *   <li>{@code LM_HTTP_CHAT_PATH} — default {@code /chat/completions}; appended to {@link #getBaseUrl()}
 *       for OpenAI-compatible chat endpoints</li>
 * </ul>
 *
 * <p><strong>Fallback behaviour:</strong> Safe defaults require no outbound network
 * ({@link LmProviderType#MOCK}). Boolean and integer parsing uses documented defaults
 * when values are missing or malformed.
 *
 * @see LmClientFactory#create(LmConfig)
 * @see HttpLmClient
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

    /**
     * Loads LM settings from {@code WEB-INF/lm.properties} and environment variables.
     *
     * <p>Resolution order for each key is handled by {@link AppConfig#resolve}: environment
     * variables take precedence over servlet properties. Property aliases include both
     * uppercase env-style names and lowercase dotted keys (for example {@code LM_PROVIDER}
     * and {@code lm.provider}).
     *
     * @param ctx servlet context used to locate {@code WEB-INF/lm.properties}; must not be {@code null}
     * @return a fully constructed {@link LmConfig} with safe defaults for missing or invalid values
     */
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

    /**
     * Returns whether AI / LM features are enabled at the configuration level.
     *
     * @return {@code true} when {@code LM_ENABLED} resolves to an affirmative value;
     *         {@code false} when explicitly disabled (mock client will report AI as off)
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Returns the configured backend provider type.
     *
     * @return {@link LmProviderType} from {@code LM_PROVIDER}; never {@code null}
     */
    public LmProviderType getProviderType() {
        return providerType;
    }

    /**
     * Returns the API key for HTTP providers ({@code LM_API_KEY}).
     *
     * @return API key string, or empty string when not configured
     */
    public String getApiKey() {
        return apiKey;
    }

    /**
     * Returns the provider API base URL ({@code LM_BASE_URL}), without the chat path.
     *
     * @return trimmed base URL, or empty string when not configured
     */
    public String getBaseUrl() {
        return baseUrl;
    }

    /**
     * Returns the default model name ({@code LM_MODEL}) for requests that do not specify one.
     *
     * <p>When empty, {@link HttpLmClient} falls back to {@link LmRequest#getModel()} then
     * {@link LmModelDefaults#CHAT_FALLBACK}.
     *
     * @return trimmed model identifier, or empty string when not configured
     */
    public String getModel() {
        return model;
    }

    /**
     * Returns the HTTP request timeout in milliseconds ({@code LM_TIMEOUT_MS}).
     *
     * @return timeout value; defaults to {@code 30000} when unset or unparsable
     */
    public int getTimeoutMs() {
        return timeoutMs;
    }

    /**
     * Returns the relative chat completions path ({@code LM_HTTP_CHAT_PATH}).
     *
     * <p>Appended to {@link #getBaseUrl()} by {@link HttpLmClient}. Defaults to
     * {@code /chat/completions} when unset or empty.
     *
     * @return path beginning with {@code /}, suitable for OpenAI-compatible APIs
     */
    public String getHttpChatPath() {
        return httpChatPath;
    }

    /**
     * Indicates whether an HTTP provider has the minimum credentials to attempt a real call.
     *
     * <p>Requires both non-empty {@link #getApiKey()} and {@link #getBaseUrl()}. A {@code true}
     * result does not guarantee success at runtime (network errors still occur). When {@code false},
     * {@link LmClientFactory} falls back to {@link MockLmClient} for {@link LmProviderType#OPENAI}
     * and {@link LmProviderType#CUSTOM}, and {@link HttpLmClient} throws {@link LmException} if
     * invoked directly.
     *
     * @return {@code true} when both API key and base URL are configured
     */
    public boolean hasHttpCredentials() {
        return !apiKey.isEmpty() && !baseUrl.isEmpty();
    }
}

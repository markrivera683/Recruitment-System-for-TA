package com.bupt.ta.ai;

/**
 * Identifies which language-model backend the application should use.
 *
 * <p>{@link LmProviderType} is resolved from the {@code LM_PROVIDER} configuration key
 * (see {@link LmConfig#load}) and drives {@link LmClientFactory#create(LmConfig)}:
 * <ul>
 *   <li>{@link #MOCK} — {@link MockLmClient}, no network</li>
 *   <li>{@link #OPENAI} — {@link HttpLmClient} against an OpenAI-compatible endpoint</li>
 *   <li>{@link #CUSTOM} — same HTTP pipeline as {@link #OPENAI}; reserved for custom
 *       base URLs, paths, or headers (see {@link HttpLmClient} TODO markers)</li>
 * </ul>
 *
 * <p>Unrecognised provider strings fall back to {@link #MOCK}. For {@link #OPENAI} and
 * {@link #CUSTOM}, missing {@code LM_BASE_URL} or {@code LM_API_KEY} also falls back to
 * {@link MockLmClient} at factory time.
 *
 * @see LmConfig#getProviderType()
 * @see LmClientFactory
 */
public enum LmProviderType {

    /** Offline deterministic client; default when provider is unknown or credentials are missing. */
    MOCK,

    /** OpenAI-compatible Chat Completions over HTTPS POST with JSON body. */
    OPENAI,

    /**
     * Same HTTP pipeline as {@link #OPENAI}; intended for custom endpoints or vendor adapters.
     * Vendor-specific differences should remain inside {@link HttpLmClient}.
     */
    CUSTOM;

    /**
     * Parses a configuration string into a provider type.
     *
     * <p>Matching is case-insensitive. Recognised values: {@code mock}, {@code openai},
     * {@code custom}. {@code null}, blank, or any other value returns {@link #MOCK}.
     *
     * @param raw value from {@code LM_PROVIDER} / {@code lm.provider}; may be {@code null}
     * @return the corresponding {@link LmProviderType}, never {@code null}
     */
    public static LmProviderType fromString(String raw) {
        if (raw == null) {
            return MOCK;
        }
        String s = raw.trim().toLowerCase();
        switch (s) {
            case "openai":
                return OPENAI;
            case "custom":
                return CUSTOM;
            case "mock":
            default:
                return MOCK;
        }
    }
}

package com.bupt.ta.ai;

import java.util.logging.Logger;

/**
 * Factory for creating the appropriate {@link LmClient} implementation from {@link LmConfig}.
 *
 * <p>This is the wiring entry point of the LM integration layer. Application code should
 * load {@link LmConfig} once (via {@link LmConfig#load}) and call {@link #create(LmConfig)}
 * rather than instantiating {@link HttpLmClient} or {@link MockLmClient} directly.
 *
 * <p><strong>Selection and fallback rules:</strong>
 * <ul>
 *   <li>{@code null} config → {@link MockLmClient} with a warning log</li>
 *   <li>{@link LmConfig#isEnabled()} {@code false} → {@link MockLmClient} with
 *       {@code forceDisabled=true} (AI reported as disabled)</li>
 *   <li>{@link LmProviderType#MOCK} → {@link MockLmClient}</li>
 *   <li>{@link LmProviderType#OPENAI} or {@link LmProviderType#CUSTOM} with
 *       {@link LmConfig#hasHttpCredentials()} → {@link HttpLmClient}</li>
 *   <li>{@link LmProviderType#OPENAI} or {@link LmProviderType#CUSTOM} without credentials
 *       → {@link MockLmClient} with a warning log</li>
 * </ul>
 *
 * @see LmConfig
 * @see LmClient
 */
public final class LmClientFactory {
    private static final Logger LOG = Logger.getLogger(LmClientFactory.class.getName());

    /** Prevents instantiation of this factory utility class. */
    private LmClientFactory() {}

    /**
     * Creates an {@link LmClient} matching the supplied configuration, with safe fallbacks
     * to {@link MockLmClient} when settings are missing or incomplete.
     *
     * @param config LM settings from {@link LmConfig#load}; may be {@code null} (treated as mock)
     * @return a non-null {@link LmClient} ready for {@link LmClient#generate(LmRequest)} or
     *         {@link LmClient#stream(LmRequest, LmStreamListener)}
     */
    public static LmClient create(LmConfig config) {
        if (config == null) {
            LOG.warning("LmConfig was null; using mock LM client.");
            return new MockLmClient();
        }
        if (!config.isEnabled()) {
            LOG.info("LM_ENABLED=false — mock client will report AI as disabled.");
            return new MockLmClient(true);
        }
        switch (config.getProviderType()) {
            case MOCK:
                return new MockLmClient();
            case OPENAI:
            case CUSTOM:
                if (!config.hasHttpCredentials()) {
                    LOG.warning("LM_PROVIDER=" + config.getProviderType()
                            + " but LM_BASE_URL or LM_API_KEY missing — falling back to mock provider.");
                    return new MockLmClient();
                }
                return new HttpLmClient(config);
        }
        // Defensive fallback for compiler exhaustiveness checks.
        return new MockLmClient();
    }
}

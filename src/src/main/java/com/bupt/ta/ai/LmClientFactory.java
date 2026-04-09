package com.bupt.ta.ai;

import java.util.logging.Logger;

/** Selects mock vs HTTP implementation based on {@link LmConfig} (safe defaults). */
public final class LmClientFactory {
    private static final Logger LOG = Logger.getLogger(LmClientFactory.class.getName());

    private LmClientFactory() {}

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

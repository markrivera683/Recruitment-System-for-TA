package com.bupt.ta.ai;

/** Default model name when none is configured (HTTP client and prompt builders). */
public final class LmModelDefaults {
    /** OpenAI-style chat model placeholder when {@link LmConfig#getModel()} is empty. */
    public static final String CHAT_FALLBACK = "gpt-4o-mini";

    private LmModelDefaults() {}
}

package com.bupt.ta.ai;

/**
 * Pluggable LM backend identifier. Extend with new vendors without changing servlet code.
 */
public enum LmProviderType {
    MOCK,
    /** OpenAI-compatible Chat Completions (HTTPS POST JSON). */
    OPENAI,
    /** Same HTTP pipeline as OPENAI; reserved for custom endpoints / headers (see {@link HttpLmClient} TODOs). */
    CUSTOM;

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

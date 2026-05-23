package com.bupt.ta.service.ai;

import com.bupt.ta.ai.LmConfig;
import com.bupt.ta.ai.LmModelDefaults;
import com.bupt.ta.util.Strings;

/**
 * Shared defaults and null-safe helpers for AI feature services.
 * <p>
 * Centralises model fallback selection and string normalisation so individual feature
 * services ({@link SkillMatchService}, {@link MissingSkillService}, etc.) do not duplicate
 * {@link LmConfig#getModel()} handling or empty-string guards. Not intended for use
 * outside the {@code com.bupt.ta.service.ai} package.
 */
final class AiLmDefaults {

    /** Model id used when {@link LmConfig#getModel()} is null or empty. */
    static final String FALLBACK_MODEL = LmModelDefaults.CHAT_FALLBACK;

    /**
     * Returns an empty string for {@code null} input.
     *
     * @param s input text
     * @return {@code s} or {@code ""} when {@code s} is null
     */
    static String nz(String s) {
        return Strings.nullToEmpty(s);
    }

    /**
     * Resolves the configured chat model or the package fallback.
     *
     * @param config LM configuration
     * @return non-empty model identifier
     */
    static String modelOrFallback(LmConfig config) {
        String m = config.getModel();
        return (m != null && !m.isEmpty()) ? m : FALLBACK_MODEL;
    }

    private AiLmDefaults() {}
}

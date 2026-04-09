package com.bupt.ta.service.ai;

import com.bupt.ta.ai.LmConfig;
import com.bupt.ta.ai.LmModelDefaults;
import com.bupt.ta.util.Strings;

/**
 * Shared defaults for AI feature services (avoids duplicating pickModel / null guards).
 */
final class AiLmDefaults {

    static final String FALLBACK_MODEL = LmModelDefaults.CHAT_FALLBACK;

    static String nz(String s) {
        return Strings.nullToEmpty(s);
    }

    static String modelOrFallback(LmConfig config) {
        String m = config.getModel();
        return (m != null && !m.isEmpty()) ? m : FALLBACK_MODEL;
    }

    private AiLmDefaults() {}
}

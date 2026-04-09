package com.bupt.ta.ai;

/**
 * Pluggable language-model client. Implementations must not leak vendor JSON to callers.
 */
public interface LmClient {
    LmResponse generate(LmRequest request) throws LmException;
}

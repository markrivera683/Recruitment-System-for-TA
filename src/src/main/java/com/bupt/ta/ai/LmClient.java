package com.bupt.ta.ai;

/**
 * Pluggable language-model client. Implementations must not leak vendor JSON to callers.
 */
public interface LmClient {

    LmResponse generate(LmRequest request) throws LmException;

    /**
     * Stream completion as incremental text. Default implementation calls {@link #generate} and
     * emits fixed-size chunks (HTTP providers should override with real SSE).
     */
    default void stream(LmRequest request, LmStreamListener listener) throws LmException {
        LmResponse r = generate(request);
        if (r.isSuccess()) {
            String t = r.getText();
            if (t != null && !t.isEmpty()) {
                int step = 48;
                for (int i = 0; i < t.length(); i += step) {
                    listener.onDelta(t.substring(i, Math.min(t.length(), i + step)));
                }
            }
            listener.onComplete(r.getModel());
        } else {
            String err = r.getErrorMessage();
            listener.onError(err != null && !err.isEmpty() ? err : "Generation failed");
        }
    }
}

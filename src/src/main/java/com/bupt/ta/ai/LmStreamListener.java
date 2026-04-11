package com.bupt.ta.ai;

/**
 * Callbacks for streaming chat completions (OpenAI-compatible SSE or simulated chunks).
 */
public interface LmStreamListener {

    void onDelta(String text);

    void onComplete(String model);

    void onError(String message);
}

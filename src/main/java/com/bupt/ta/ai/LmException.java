package com.bupt.ta.ai;

/** Wraps configuration, transport, or parsing failures from the LM layer. */
public class LmException extends Exception {
    public LmException(String message) {
        super(message);
    }

    public LmException(String message, Throwable cause) {
        super(message, cause);
    }
}

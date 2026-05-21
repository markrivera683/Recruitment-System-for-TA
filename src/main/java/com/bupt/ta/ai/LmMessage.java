package com.bupt.ta.ai;

import java.util.Objects;

/** One chat message (system / user / assistant). */
public final class LmMessage {
    public final String role;
    public final String content;

    public LmMessage(String role, String content) {
        this.role = role != null ? role : "user";
        this.content = content != null ? content : "";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof LmMessage)) return false;
        LmMessage lmMessage = (LmMessage) o;
        return Objects.equals(role, lmMessage.role) && Objects.equals(content, lmMessage.content);
    }

    @Override
    public int hashCode() {
        return Objects.hash(role, content);
    }
}

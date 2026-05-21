package com.bupt.ta.ai;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class LmRequestTest {

    @Test
    void builder_preservesFields() {
        LmRequest req = LmRequest.builder()
                .featureName("test-feature")
                .systemPrompt("sys")
                .userPrompt("usr")
                .temperature(0.5d)
                .maxTokens(256)
                .model("m")
                .addMessage(new LmMessage("user", "hello"))
                .build();
        assertEquals("test-feature", req.getFeatureName());
        assertEquals("sys", req.getSystemPrompt());
        assertEquals("usr", req.getUserPrompt());
        assertEquals(0.5d, req.getTemperature());
        assertEquals(256, req.getMaxTokens());
        assertEquals("m", req.getModel());
        assertEquals(1, req.getMessages().size());
        assertEquals("hello", req.getMessages().get(0).content);
    }

    @Test
    void messagesList_isUnmodifiable() {
        LmRequest req = LmRequest.builder().addMessage(new LmMessage("user", "x")).build();
        assertThrows(UnsupportedOperationException.class, () -> req.getMessages().clear());
    }
}

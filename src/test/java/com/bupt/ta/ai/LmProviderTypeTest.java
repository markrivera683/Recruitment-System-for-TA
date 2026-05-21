package com.bupt.ta.ai;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LmProviderTypeTest {

    @Test
    void fromString_defaultsToMock() {
        assertEquals(LmProviderType.MOCK, LmProviderType.fromString(null));
        assertEquals(LmProviderType.MOCK, LmProviderType.fromString(""));
        assertEquals(LmProviderType.MOCK, LmProviderType.fromString("unknown"));
    }

    @Test
    void fromString_recognisesVendorsCaseInsensitive() {
        assertEquals(LmProviderType.MOCK, LmProviderType.fromString("mock"));
        assertEquals(LmProviderType.MOCK, LmProviderType.fromString("MOCK"));
        assertEquals(LmProviderType.OPENAI, LmProviderType.fromString("openai"));
        assertEquals(LmProviderType.CUSTOM, LmProviderType.fromString("custom"));
    }
}

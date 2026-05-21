package com.bupt.ta.testsupport;

import javax.servlet.ServletContext;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Shared test fixtures for LM integration tests (mock {@link ServletContext} + optional {@code lm.properties} content).
 */
public final class LmTestSupport {

    private LmTestSupport() {}

    /**
     * @param propertiesUtf8 full content of {@code WEB-INF/lm.properties}, or {@code null} if the file is absent
     */
    public static ServletContext servletContextWithLmProperties(String propertiesUtf8) {
        ServletContext ctx = mock(ServletContext.class);
        if (propertiesUtf8 != null) {
            when(ctx.getResourceAsStream("/WEB-INF/lm.properties"))
                    .thenReturn(new ByteArrayInputStream(propertiesUtf8.getBytes(StandardCharsets.UTF_8)));
        } else {
            when(ctx.getResourceAsStream("/WEB-INF/lm.properties")).thenReturn(null);
        }
        return ctx;
    }
}

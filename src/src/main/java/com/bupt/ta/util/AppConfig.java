package com.bupt.ta.util;

import javax.servlet.ServletContext;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

/**
 * Loads optional {@code WEB-INF/*.properties} files and resolves configuration with a fixed priority:
 * environment variable → JVM system property → properties file → default value.
 */
public final class AppConfig {

    private AppConfig() {}

    public static Properties loadWebInfProperties(ServletContext ctx, String fileName) {
        Properties p = new Properties();
        if (ctx == null || fileName == null || fileName.isEmpty()) {
            return p;
        }
        String path = "/WEB-INF/" + fileName;
        try (InputStream in = ctx.getResourceAsStream(path)) {
            if (in == null) {
                return p;
            }
            p.load(new InputStreamReader(in, StandardCharsets.UTF_8));
        } catch (IOException e) {
            // Non-fatal: missing or unreadable file is OK for optional local overrides
        }
        return p;
    }

    /**
     * @param envVar       e.g. LM_ENABLED
     * @param sysPropKeys  tried in order, e.g. "LM_ENABLED", "lm.enabled"
     */
    public static String resolve(String envVar, String[] sysPropKeys, Properties fileProps, String fileKey, String defaultValue) {
        String fromEnv = envVar != null ? System.getenv(envVar) : null;
        if (notBlank(fromEnv)) {
            return fromEnv.trim();
        }
        if (sysPropKeys != null) {
            for (String k : sysPropKeys) {
                if (k == null) continue;
                String v = System.getProperty(k);
                if (notBlank(v)) {
                    return v.trim();
                }
            }
        }
        if (fileProps != null && fileKey != null) {
            String v = fileProps.getProperty(fileKey);
            if (notBlank(v)) {
                return v.trim();
            }
        }
        return defaultValue;
    }

    private static boolean notBlank(String s) {
        return s != null && !s.trim().isEmpty();
    }
}

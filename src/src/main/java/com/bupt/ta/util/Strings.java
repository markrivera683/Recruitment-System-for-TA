package com.bupt.ta.util;

/** Small string helpers used across the app (no external dependencies). */
public final class Strings {

    private Strings() {}

    public static String nullToEmpty(String s) {
        return s == null ? "" : s;
    }

    /** @return first non-null, non-blank argument, or {@code ""} */
    public static String firstNonBlank(String... parts) {
        if (parts == null) {
            return "";
        }
        for (String p : parts) {
            if (p != null && !p.trim().isEmpty()) {
                return p.trim();
            }
        }
        return "";
    }
}

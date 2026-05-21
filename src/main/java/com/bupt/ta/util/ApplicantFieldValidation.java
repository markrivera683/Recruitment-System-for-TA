package com.bupt.ta.util;

import java.time.Year;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Shared validation for applicant identity fields (profile and registration).
 * Degree level is stored in English; only graduate applicants (Master / Doctoral) are allowed.
 */
public final class ApplicantFieldValidation {
    private ApplicantFieldValidation() {}

    private static final Pattern NAME_PATTERN = Pattern.compile("^[\\p{L} .'-]{2,60}$");
    private static final Pattern PHONE_CN_PATTERN = Pattern.compile("^\\+861[3-9]\\d{9}$");
    private static final Pattern ID_CARD_18_SHAPE_PATTERN = Pattern.compile(
            "^[1-9]\\d{5}(19|20)\\d{2}(0[1-9]|1[0-2])(0[1-9]|[12]\\d|3[01])\\d{3}[\\dXx]$");

    private static final Set<String> APPLICANT_DEGREE_LEVEL_WHITELIST;

    static {
        Set<String> d = new LinkedHashSet<>();
        d.add("Master");
        d.add("Doctoral");
        APPLICANT_DEGREE_LEVEL_WHITELIST = Collections.unmodifiableSet(d);
    }

    /** Values allowed for {@link com.bupt.ta.model.ApplicantProfile#degree} (current study level). */
    public static Set<String> applicantDegreeLevelWhitelist() {
        return APPLICANT_DEGREE_LEVEL_WHITELIST;
    }

    public static boolean isAllowedApplicantDegreeLevel(String degree) {
        return degree != null && APPLICANT_DEGREE_LEVEL_WHITELIST.contains(degree.trim());
    }

    public static boolean isValidFullName(String name) {
        return name != null && NAME_PATTERN.matcher(name.trim()).matches();
    }

    /**
     * Domain must look like a real host (e.g. bupt.edu.cn): dot in domain, ASCII TLD 2+ letters.
     */
    public static boolean isValidEmailWithRealDomain(String email) {
        if (email == null) {
            return false;
        }
        String e = email.trim();
        int at = e.lastIndexOf('@');
        if (at < 1 || at == e.length() - 1) {
            return false;
        }
        String local = e.substring(0, at);
        String domain = e.substring(at + 1);
        if (local.isEmpty() || local.contains("@") || domain.contains("@")) {
            return false;
        }
        if (!local.matches("^[a-zA-Z0-9._%+-]+$")) {
            return false;
        }
        if (!domain.contains(".") || domain.startsWith(".") || domain.endsWith(".")) {
            return false;
        }
        if (!domain.matches("^[a-zA-Z0-9](?:[a-zA-Z0-9-]*[a-zA-Z0-9])?(?:\\.[a-zA-Z0-9](?:[a-zA-Z0-9-]*[a-zA-Z0-9])?)+$")) {
            return false;
        }
        int lastDot = domain.lastIndexOf('.');
        String tld = domain.substring(lastDot + 1);
        return tld.matches("[a-zA-Z]{2,}");
    }

    /**
     * Normalize mainland mobile to {@code +86} plus 11 digits. Accepts 11-digit local form or 13-digit starting with 86.
     */
    public static String normalizeChinaPhone(String raw) {
        if (raw == null) {
            return null;
        }
        String digits = raw.replaceAll("[^0-9]", "");
        if (digits.length() == 11 && digits.charAt(0) == '1') {
            return "+86" + digits;
        }
        if (digits.length() == 13 && digits.startsWith("86") && digits.charAt(2) == '1') {
            return "+86" + digits.substring(2);
        }
        return null;
    }

    public static boolean isValidChinaMobileNormalized(String phone) {
        return phone != null && PHONE_CN_PATTERN.matcher(phone).matches();
    }

    /** BUPT-style 10-digit student number; first 4 digits = admission year (2000 … current year + 1). */
    public static boolean isValidBuptTenDigitStudentId(String raw) {
        if (raw == null) {
            return false;
        }
        String s = raw.trim();
        if (!s.matches("^\\d{10}$")) {
            return false;
        }
        int cohort;
        try {
            cohort = Integer.parseInt(s.substring(0, 4), 10);
        } catch (NumberFormatException e) {
            return false;
        }
        int y = Year.now().getValue();
        return cohort >= 2000 && cohort <= y + 1;
    }

    /** 18-digit PRC resident ID: shape + ISO 7064:2003 MOD 11-2 check character. */
    public static boolean isValidChineseResidentId18(String raw) {
        if (raw == null) {
            return false;
        }
        String s = raw.trim().toUpperCase();
        if (!ID_CARD_18_SHAPE_PATTERN.matcher(s).matches()) {
            return false;
        }
        return idCard18ChecksumValid(s);
    }

    private static boolean idCard18ChecksumValid(String s) {
        int[] weights = {7, 9, 10, 5, 8, 4, 2, 1, 6, 3, 7, 9, 10, 5, 8, 4, 2};
        String checkChars = "10X98765432";
        int sum = 0;
        for (int i = 0; i < 17; i++) {
            char c = s.charAt(i);
            if (c < '0' || c > '9') {
                return false;
            }
            sum += (c - '0') * weights[i];
        }
        return checkChars.charAt(sum % 11) == s.charAt(17);
    }
}

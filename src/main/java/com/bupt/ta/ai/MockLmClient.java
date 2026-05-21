package com.bupt.ta.ai;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Deterministic offline LM for coursework demos. No network; outputs are stable for the same inputs.
 */
public final class MockLmClient implements LmClient {
    private static final String PROVIDER = "mock";
    private final boolean forceDisabled;

    public MockLmClient() {
        this(false);
    }

    /** When true, {@link #generate} returns a clear "disabled" message (LM_ENABLED=false). */
    public MockLmClient(boolean forceDisabled) {
        this.forceDisabled = forceDisabled;
    }

    @Override
    public void stream(LmRequest request, LmStreamListener listener) {
        LmResponse r = generate(request);
        if (!r.isSuccess()) {
            listener.onError(r.getErrorMessage() != null ? r.getErrorMessage() : "Generation failed");
            return;
        }
        String t = r.getText();
        if (t == null) {
            t = "";
        }
        int step = 28;
        for (int i = 0; i < t.length(); i += step) {
            listener.onDelta(t.substring(i, Math.min(t.length(), i + step)));
        }
        listener.onComplete(r.getModel());
    }

    @Override
    public LmResponse generate(LmRequest request) {
        String model = request.getModel() != null && !request.getModel().isEmpty()
                ? request.getModel()
                : "mock-model";
        if (forceDisabled) {
            return new LmResponse(
                    "",
                    PROVIDER,
                    model,
                    false,
                    null,
                    "AI features are disabled (LM_ENABLED=false). Enable them via environment or WEB-INF/lm.properties.");
        }
        String fn = request.getFeatureName() == null ? "" : request.getFeatureName();
        switch (fn) {
            case AiFeatureNames.SKILL_MATCH:
                return skillMatch(request, model);
            case AiFeatureNames.MISSING_SKILLS:
                return missingSkills(request, model);
            case AiFeatureNames.JOB_RECOMMENDATION:
                return jobRecommendation(request, model);
            case AiFeatureNames.WORKLOAD_ADVICE:
                return workloadAdvice(request, model);
            default:
                return genericEcho(request, model);
        }
    }

    private static LmResponse genericEcho(LmRequest request, String model) {
        String up = request.getUserPrompt() != null ? request.getUserPrompt() : "";
        String text = "[Mock LM] No specific handler for feature \""
                + request.getFeatureName()
                + "\". Echo user prompt:\n\n"
                + up;
        return new LmResponse(text, PROVIDER, model, true, null, null);
    }

    private static LmResponse skillMatch(LmRequest request, String model) {
        String up = request.getUserPrompt() != null ? request.getUserPrompt() : "";
        String appBlock = extractSection(up, "Applicant skills:", "Job requirements:");
        String jobBlock = extractSection(up, "Job requirements:", null);
        Set<String> app = splitSkills(appBlock.isEmpty() ? up : appBlock);
        Set<String> job = splitSkills(jobBlock);
        if (job.isEmpty()) {
            job = splitSkills(up);
        }

        Set<String> matched = new LinkedHashSet<>(app);
        matched.retainAll(job);
        Set<String> suggested = new LinkedHashSet<>(job);
        suggested.removeAll(app);

        int denom = Math.max(job.size(), 1);
        int score = (int) Math.round((matched.size() * 100.0) / denom);
        score = Math.min(100, Math.max(0, score));

        StringBuilder sb = new StringBuilder();
        sb.append("Match score: ").append(score).append("%\n");
        sb.append("Matched skills: ").append(joinReadable(matched)).append("\n");
        sb.append("Suggested skills to strengthen: ").append(joinReadable(suggested)).append("\n");
        sb.append("\n(Deterministic mock output — replace provider with a real LM for production use.)");
        return new LmResponse(sb.toString().trim(), PROVIDER, model, true, null, null);
    }

    private static LmResponse missingSkills(LmRequest request, String model) {
        String up = request.getUserPrompt() != null ? request.getUserPrompt() : "";
        String cand = extractSection(up, "Candidate skills:", "Required job skills:");
        String reqd = extractSection(up, "Required job skills:", null);
        Set<String> c = splitSkills(cand.isEmpty() ? up : cand);
        Set<String> r = splitSkills(reqd);
        if (r.isEmpty()) {
            r = splitSkills(up);
        }
        Set<String> missing = new LinkedHashSet<>(r);
        missing.removeAll(c);

        StringBuilder sb = new StringBuilder();
        sb.append("Missing compared to job requirements:\n");
        if (missing.isEmpty()) {
            sb.append("- (none — candidate covers all listed skills)\n");
        } else {
            for (String m : missing) {
                sb.append("- ").append(m).append("\n");
            }
        }
        sb.append("\n(Deterministic mock output — verify with module organisers before decisions.)");
        return new LmResponse(sb.toString().trim(), PROVIDER, model, true, null, null);
    }

    private static LmResponse jobRecommendation(LmRequest request, String model) {
        String up = request.getUserPrompt() != null ? request.getUserPrompt() : "";
        String profile = extractSection(up, "Candidate profile:", "Open positions:");
        String jobs = extractSection(up, "Open positions:", null);
        List<String> lines = new ArrayList<>();
        for (String line : jobs.split("\\r?\\n")) {
            String t = line.trim();
            if (!t.isEmpty()) {
                lines.add(t);
            }
        }
        if (lines.isEmpty()) {
            for (String line : up.split("\\r?\\n")) {
                String t = line.trim();
                if (!t.isEmpty()) {
                    lines.add(t);
                }
            }
        }

        StringBuilder sb = new StringBuilder();
        sb.append("Recommended positions (ranked, mock rules):\n");
        if (lines.isEmpty()) {
            sb.append("1) (no job lines parsed — paste one job per line under \"Open positions:\")\n");
        } else {
            int rank = 1;
            for (int i = 0; i < Math.min(3, lines.size()); i++) {
                String jobLine = lines.get(i);
                String reason = buildMockReason(profile, jobLine, i);
                sb.append(rank++).append(") ").append(jobLine).append("\n   Reason: ").append(reason).append("\n");
            }
        }
        sb.append("\n(Deterministic mock output — use as discussion starter, not automatic placement.)");
        return new LmResponse(sb.toString().trim(), PROVIDER, model, true, null, null);
    }

    private static LmResponse workloadAdvice(LmRequest request, String model) {
        String up = request.getUserPrompt() != null ? request.getUserPrompt() : "";
        int potential = parseIntAfterMarker(up, "Potential load if approve:");
        int threshold = parseIntAfterMarker(up, "Warning threshold:");
        if (threshold <= 0) {
            threshold = 3;
        }
        int accepted = parseIntAfterMarker(up, "Accepted count:");
        int pending = parseIntAfterMarker(up, "Pending count:");

        String recommendation;
        if (potential >= threshold + 1) {
            recommendation = "Reject";
        } else if (potential >= threshold) {
            recommendation = "Caution";
        } else {
            recommendation = "Approve";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("## Workload Summary\n");
        sb.append("- Accepted: ").append(accepted).append("\n");
        sb.append("- Pending: ").append(pending).append("\n");
        sb.append("- Potential load if approve: **").append(potential).append("**\n");
        sb.append("- Warning threshold: ").append(threshold).append("\n\n");

        sb.append("## Recommendation (Approve / Reject / Caution)\n");
        sb.append("**").append(recommendation).append("**\n\n");

        sb.append("## Reasoning\n");
        if ("Reject".equals(recommendation)) {
            sb.append("Potential load exceeds the safe threshold. Approving would likely overload this TA.\n");
        } else if ("Caution".equals(recommendation)) {
            sb.append("Potential load meets or exceeds the warning threshold. Review schedule overlap before approving.\n");
        } else {
            sb.append("Current accepted + pending workload is below the warning threshold. Approval is reasonable from a load perspective.\n");
        }
        sb.append("\n## If Approved — Expected Load\n");
        sb.append("The TA would carry **").append(potential).append("** active assignments (accepted + pending).\n");
        sb.append("\n(Deterministic mock output — MO retains final decision authority.)");
        return new LmResponse(sb.toString().trim(), PROVIDER, model, true, null, null);
    }

    private static int parseIntAfterMarker(String text, String marker) {
        int i = text.indexOf(marker);
        if (i < 0) {
            return 0;
        }
        int start = i + marker.length();
        StringBuilder num = new StringBuilder();
        for (int j = start; j < text.length(); j++) {
            char c = text.charAt(j);
            if (Character.isDigit(c)) {
                num.append(c);
            } else if (num.length() > 0) {
                break;
            }
        }
        if (num.length() == 0) {
            return 0;
        }
        try {
            return Integer.parseInt(num.toString());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private static String buildMockReason(String profile, String jobLine, int index) {
        String p = profile == null ? "" : profile.toLowerCase(Locale.ROOT);
        String j = jobLine.toLowerCase(Locale.ROOT);
        if (p.contains("java") && j.contains("java")) {
            return "Profile mentions Java and the role highlights Java-related work.";
        }
        if (p.contains("lab") && j.contains("lab")) {
            return "Lab experience in the profile aligns with a lab-focused posting.";
        }
        if (index == 0) {
            return "Listed first among parsed openings — mock ranking uses stable ordering.";
        }
        if (index == 1) {
            return "Second option for breadth — compare workload and schedule with the candidate.";
        }
        return "Additional option — confirm prerequisites with the module organiser.";
    }

    private static String extractSection(String full, String startMarker, String endMarker) {
        if (full == null) {
            return "";
        }
        int i = full.indexOf(startMarker);
        if (i < 0) {
            return "";
        }
        int start = i + startMarker.length();
        if (endMarker != null) {
            int j = full.indexOf(endMarker, start);
            if (j < 0) {
                return full.substring(start).trim();
            }
            return full.substring(start, j).trim();
        }
        return full.substring(start).trim();
    }

    private static LinkedHashSet<String> splitSkills(String raw) {
        LinkedHashSet<String> set = new LinkedHashSet<>();
        if (raw == null) {
            return set;
        }
        for (String part : raw.split("[,;\\n]")) {
            String t = part.trim().toLowerCase(Locale.ROOT);
            if (!t.isEmpty()) {
                set.add(t);
            }
        }
        return set;
    }

    private static String joinReadable(Set<String> skills) {
        if (skills == null || skills.isEmpty()) {
            return "(none)";
        }
        return skills.stream().sorted().collect(Collectors.joining(", "));
    }
}

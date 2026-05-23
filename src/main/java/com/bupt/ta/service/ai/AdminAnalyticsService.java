package com.bupt.ta.service.ai;

import com.bupt.ta.ai.AiFeatureNames;
import com.bupt.ta.ai.LmClient;
import com.bupt.ta.ai.LmConfig;
import com.bupt.ta.ai.LmException;
import com.bupt.ta.ai.LmRequest;
import com.bupt.ta.ai.LmResponse;
import com.bupt.ta.service.admin.AdminDashboardMetrics;
import com.bupt.ta.service.admin.AdminMetricsBuilder;

/**
 * Generates admin-facing platform analytics narrative from dashboard metrics.
 */
public final class AdminAnalyticsService {
    private final LmClient client;
    private final LmConfig config;

    public AdminAnalyticsService(LmClient client, LmConfig config) {
        this.client = client;
        this.config = config;
    }

    public LmResponse analyze(AdminDashboardMetrics metrics) throws LmException {
        return client.generate(buildRequest(metrics));
    }

    public LmRequest buildRequest(AdminDashboardMetrics metrics) {
        return LmRequest.builder()
                .featureName(AiFeatureNames.ADMIN_ANALYTICS)
                .systemPrompt("You are an analytics assistant for a university TA recruitment platform admin.\n"
                        + "Use ONLY the metrics provided — do not invent numbers.\n"
                        + "Return concise Markdown with sections:\n"
                        + "## Executive Summary\n"
                        + "## User & Applicant Trends\n"
                        + "## Application Pipeline Health\n"
                        + "## Workload & Capacity Alerts\n"
                        + "## Recommended Actions\n"
                        + "Highlight month-over-month changes when visible in the data.")
                .userPrompt(AdminMetricsBuilder.buildAnalyticsPrompt(metrics))
                .temperature(0.3d)
                .maxTokens(650)
                .model(AiLmDefaults.modelOrFallback(config))
                .build();
    }
}

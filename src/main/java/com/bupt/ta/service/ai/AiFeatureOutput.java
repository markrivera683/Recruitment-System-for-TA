package com.bupt.ta.service.ai;

import com.bupt.ta.ai.LmResponse;

/** Safe DTO for JSP / servlet — never includes stack traces. */
public final class AiFeatureOutput {
    private final boolean success;
    private final String text;
    private final String errorMessage;
    private final String provider;
    private final String model;

    private AiFeatureOutput(boolean success, String text, String errorMessage, String provider, String model) {
        this.success = success;
        this.text = text != null ? text : "";
        this.errorMessage = errorMessage;
        this.provider = provider != null ? provider : "";
        this.model = model != null ? model : "";
    }

    public boolean isSuccess() {
        return success;
    }

    public String getText() {
        return text;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public String getProvider() {
        return provider;
    }

    public String getModel() {
        return model;
    }

    public static AiFeatureOutput fromResponse(LmResponse r) {
        if (r == null) {
            return error("Empty model response.");
        }
        if (!r.isSuccess()) {
            String err = r.getErrorMessage() != null ? r.getErrorMessage() : "Model request failed.";
            return new AiFeatureOutput(false, "", err, r.getProvider(), r.getModel());
        }
        return new AiFeatureOutput(true, r.getText(), null, r.getProvider(), r.getModel());
    }

    public static AiFeatureOutput error(String message) {
        return new AiFeatureOutput(false, "", message, "", "");
    }

    public static AiFeatureOutput disabled() {
        return error("AI features are disabled (LM_ENABLED=false).");
    }
}

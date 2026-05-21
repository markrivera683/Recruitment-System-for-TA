package com.bupt.ta.ai;

/** Normalised LM output; servlet layer should only consume this shape. */
public final class LmResponse {
    private final String text;
    private final String provider;
    private final String model;
    private final boolean success;
    private final String rawResponse;
    private final String errorMessage;

    public LmResponse(String text, String provider, String model, boolean success,
                      String rawResponse, String errorMessage) {
        this.text = text != null ? text : "";
        this.provider = provider != null ? provider : "";
        this.model = model != null ? model : "";
        this.success = success;
        this.rawResponse = rawResponse;
        this.errorMessage = errorMessage;
    }

    public String getText() {
        return text;
    }

    public String getProvider() {
        return provider;
    }

    public String getModel() {
        return model;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getRawResponse() {
        return rawResponse;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}

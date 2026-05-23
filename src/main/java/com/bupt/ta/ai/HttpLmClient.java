package com.bupt.ta.ai;

import com.bupt.ta.util.HttpJsonClient;
import com.bupt.ta.util.Strings;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * HTTPS JSON implementation of {@link LmClient} for OpenAI-compatible Chat Completions APIs.
 *
 * <p>{@link HttpLmClient} is the production-oriented backend of the LM integration layer.
 * It is constructed by {@link LmClientFactory} when {@link LmConfig#getProviderType()} is
 * {@link LmProviderType#OPENAI} or {@link LmProviderType#CUSTOM} and
 * {@link LmConfig#hasHttpCredentials()} is {@code true}. All vendor-specific concerns—URL
 * assembly, {@code Authorization} header, JSON body shape, response parsing, and SSE
 * streaming—are encapsulated here so servlets and AI services only see {@link LmRequest}
 * and {@link LmResponse}.
 *
 * <p><strong>Configuration used:</strong> {@link LmConfig#getBaseUrl()},
 * {@link LmConfig#getHttpChatPath()}, {@link LmConfig#getApiKey()},
 * {@link LmConfig#getTimeoutMs()}, and {@link LmConfig#getModel()} (with per-request
 * override via {@link LmRequest#getModel()} and final fallback to
 * {@link LmModelDefaults#CHAT_FALLBACK}).
 *
 * <p><strong>Fallback behaviour:</strong> If credentials are missing, both
 * {@link #generate(LmRequest)} and {@link #stream(LmRequest, LmStreamListener)} throw
 * {@link LmException}. Provider error JSON is converted to unsuccessful {@link LmResponse}
 * or {@link LmStreamListener#onError(String)} rather than throwing when the HTTP call succeeds
 * but the payload contains an {@code error} object.
 *
 * <p>Vendor-specific differences (Azure api-version, Gemini/Claude adapters, custom headers)
 * should stay inside this class; see inline TODO markers.
 *
 * @see LmClientFactory
 * @see LmConfig
 */
public final class HttpLmClient implements LmClient {
    private static final Logger LOG = Logger.getLogger(HttpLmClient.class.getName());
    private final LmConfig config;

    /**
     * Creates an HTTP client bound to the given LM configuration.
     *
     * @param config settings loaded via {@link LmConfig#load}; must not be {@code null}
     */
    public HttpLmClient(LmConfig config) {
        this.config = config;
    }

    /**
     * Performs a synchronous Chat Completions request and returns a normalised response.
     *
     * <p>Builds an OpenAI-compatible JSON body from {@link LmRequest#getMessages()} or,
     * when empty, from {@link LmRequest#getSystemPrompt()} and {@link LmRequest#getUserPrompt()}.
     * Posts to {@code baseUrl + httpChatPath} with a Bearer token from {@link LmConfig#getApiKey()}.
     *
     * @param request completion parameters and prompts; must not be {@code null}
     * @return {@link LmResponse} with assistant text on success, or {@code success=false} with
     *         {@link LmResponse#getErrorMessage()} when the provider returns an error object or
     *         assistant text cannot be parsed
     * @throws LmException if {@link LmConfig#hasHttpCredentials()} is {@code false}, JSON body
     *                     construction fails, or the HTTP transport throws
     */
    @Override
    public LmResponse generate(LmRequest request) throws LmException {
        if (!config.hasHttpCredentials()) {
            throw new LmException("HTTP LM requires LM_BASE_URL and LM_API_KEY.");
        }
        String url = joinBaseAndPath(config.getBaseUrl(), config.getHttpChatPath());
        String model = Strings.firstNonBlank(request.getModel(), config.getModel(), LmModelDefaults.CHAT_FALLBACK);
        String body;
        try {
            // TODO(OPENAI): adjust field names if your vendor uses a different JSON schema.
            // TODO(AZURE_OPENAI): add api-version query param and swap Authorization for api-key header.
            // TODO(GEMINI/CLAUDE): replace buildOpenAiCompatibleChatBody + extractText with vendor-specific adapters.
            body = buildOpenAiCompatibleChatBody(request, model);
        } catch (Exception e) {
            throw new LmException("Failed to build request JSON: " + e.getMessage(), e);
        }

        Map<String, String> headers = new LinkedHashMap<>();
        headers.put("Authorization", "Bearer " + config.getApiKey());
        // TODO(CUSTOM): inject extra headers here without touching servlets.

        try {
            String raw = HttpJsonClient.postJson(url, headers, body, config.getTimeoutMs());
            if (raw != null && raw.contains("\"error\"")) {
                String apiMsg = extractJsonStringValue(raw, "message", raw.indexOf("\"error\""));
                return new LmResponse("", "http", model, false, raw,
                        apiMsg != null ? apiMsg : "Provider returned an error object.");
            }
            String text = extractModelText(raw);
            if (text == null || text.isEmpty()) {
                return new LmResponse("", "http", model, false, raw, "Could not parse assistant text from HTTP response.");
            }
            return new LmResponse(text, "http", model, true, raw, null);
        } catch (Exception e) {
            LOG.log(Level.WARNING, "LM HTTP request failed: " + url, e);
            throw new LmException("LM HTTP request failed: " + e.getMessage(), e);
        }
    }

    /**
     * Streams a Chat Completions response via Server-Sent Events (SSE).
     *
     * <p>Sends {@code "stream": true} in the request body, reads {@code data:} lines from the
     * response, forwards {@code delta.content} fragments to {@link LmStreamListener#onDelta(String)},
     * and completes on {@code [DONE]} or end-of-stream. Provider errors invoke
     * {@link LmStreamListener#onError(String)} without throwing.
     *
     * @param request  same shape as {@link #generate(LmRequest)}
     * @param listener streaming callbacks; must not be {@code null}
     * @throws LmException if credentials are missing, the request body cannot be built, or
     *                     streaming I/O fails before a terminal listener callback
     */
    @Override
    public void stream(LmRequest request, LmStreamListener listener) throws LmException {
        if (!config.hasHttpCredentials()) {
            throw new LmException("HTTP LM requires LM_BASE_URL and LM_API_KEY.");
        }
        String url = joinBaseAndPath(config.getBaseUrl(), config.getHttpChatPath());
        String model = Strings.firstNonBlank(request.getModel(), config.getModel(), LmModelDefaults.CHAT_FALLBACK);
        String body;
        try {
            body = buildOpenAiCompatibleChatBodyStream(request, model);
        } catch (Exception e) {
            throw new LmException("Failed to build streaming request JSON: " + e.getMessage(), e);
        }
        Map<String, String> headers = new LinkedHashMap<>();
        headers.put("Authorization", "Bearer " + config.getApiKey());
        try (InputStream rawIn = HttpJsonClient.postJsonStream(url, headers, body, config.getTimeoutMs());
             BufferedReader br = new BufferedReader(new InputStreamReader(rawIn, StandardCharsets.UTF_8))) {
            String line;
            String lastModel = model;
            while ((line = br.readLine()) != null) {
                String trimmed = line.trim();
                if (trimmed.isEmpty()) {
                    continue;
                }
                if (!trimmed.startsWith("data:")) {
                    continue;
                }
                String payload = trimmed.substring(trimmed.indexOf(':') + 1).trim();
                if ("[DONE]".equals(payload)) {
                    listener.onComplete(lastModel);
                    return;
                }
                if (payload.contains("\"error\"")) {
                    int errIdx = payload.indexOf("\"error\"");
                    String apiMsg = extractJsonStringValue(payload, "message", errIdx >= 0 ? errIdx : 0);
                    listener.onError(apiMsg != null ? apiMsg : payload);
                    return;
                }
                String m = extractJsonStringValue(payload, "model", 0);
                if (m != null && !m.isEmpty()) {
                    lastModel = m;
                }
                int deltaIdx = payload.indexOf("\"delta\"");
                String delta = extractJsonStringValue(payload, "content", deltaIdx >= 0 ? deltaIdx : 0);
                if (delta != null && !delta.isEmpty()) {
                    listener.onDelta(delta);
                }
            }
            listener.onComplete(lastModel);
        } catch (Exception e) {
            LOG.log(Level.WARNING, "LM HTTP stream failed: " + url, e);
            throw new LmException("LM HTTP stream failed: " + e.getMessage(), e);
        }
    }

    private static String joinBaseAndPath(String baseUrl, String path) {
        String b = baseUrl == null ? "" : baseUrl.trim();
        String p = path == null ? "" : path.trim();
        if (!p.startsWith("/")) {
            p = "/" + p;
        }
        while (b.endsWith("/")) {
            b = b.substring(0, b.length() - 1);
        }
        return b + p;
    }

    /**
     * OpenAI Chat Completions compatible payload.
     * TODO: split into strategy classes if you support multiple vendors side-by-side.
     */
    private static String buildOpenAiCompatibleChatBody(LmRequest request, String model) {
        StringBuilder messagesJson = new StringBuilder();
        if (request.getMessages() != null && !request.getMessages().isEmpty()) {
            boolean first = true;
            for (LmMessage m : request.getMessages()) {
                if (!first) {
                    messagesJson.append(',');
                }
                first = false;
                messagesJson.append("{\"role\":\"").append(jsonEscape(m.role))
                        .append("\",\"content\":\"").append(jsonEscape(m.content)).append("\"}");
            }
        } else {
            String sys = request.getSystemPrompt() != null ? request.getSystemPrompt() : "";
            String usr = request.getUserPrompt() != null ? request.getUserPrompt() : "";
            messagesJson.append("{\"role\":\"system\",\"content\":\"").append(jsonEscape(sys)).append("\"},");
            messagesJson.append("{\"role\":\"user\",\"content\":\"").append(jsonEscape(usr)).append("\"}");
        }

        StringBuilder sb = new StringBuilder();
        sb.append('{');
        sb.append("\"model\":\"").append(jsonEscape(model)).append("\",");
        sb.append("\"temperature\":").append(request.getTemperature()).append(',');
        sb.append("\"max_tokens\":").append(request.getMaxTokens()).append(',');
        sb.append("\"messages\":[").append(messagesJson).append(']');
        sb.append('}');
        return sb.toString();
    }

    private static String buildOpenAiCompatibleChatBodyStream(LmRequest request, String model) {
        StringBuilder messagesJson = new StringBuilder();
        if (request.getMessages() != null && !request.getMessages().isEmpty()) {
            boolean first = true;
            for (LmMessage m : request.getMessages()) {
                if (!first) {
                    messagesJson.append(',');
                }
                first = false;
                messagesJson.append("{\"role\":\"").append(jsonEscape(m.role))
                        .append("\",\"content\":\"").append(jsonEscape(m.content)).append("\"}");
            }
        } else {
            String sys = request.getSystemPrompt() != null ? request.getSystemPrompt() : "";
            String usr = request.getUserPrompt() != null ? request.getUserPrompt() : "";
            messagesJson.append("{\"role\":\"system\",\"content\":\"").append(jsonEscape(sys)).append("\"},");
            messagesJson.append("{\"role\":\"user\",\"content\":\"").append(jsonEscape(usr)).append("\"}");
        }

        StringBuilder sb = new StringBuilder();
        sb.append('{');
        sb.append("\"model\":\"").append(jsonEscape(model)).append("\",");
        sb.append("\"temperature\":").append(request.getTemperature()).append(',');
        sb.append("\"max_tokens\":").append(request.getMaxTokens()).append(',');
        sb.append("\"stream\":true,");
        sb.append("\"messages\":[").append(messagesJson).append(']');
        sb.append('}');
        return sb.toString();
    }

    private static String jsonEscape(String s) {
        if (s == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder(s.length() + 16);
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '\\':
                    sb.append("\\\\");
                    break;
                case '"':
                    sb.append("\\\"");
                    break;
                case '\n':
                    sb.append("\\n");
                    break;
                case '\r':
                    sb.append("\\r");
                    break;
                case '\t':
                    sb.append("\\t");
                    break;
                default:
                    if (c < 0x20) {
                        sb.append(String.format("\\u%04x", (int) c));
                    } else {
                        sb.append(c);
                    }
            }
        }
        return sb.toString();
    }

    private static String extractModelText(String json) {
        if (json == null) {
            return null;
        }
        int choices = json.indexOf("\"choices\"");
        int from = choices >= 0 ? choices : 0;
        String content = extractJsonStringValue(json, "content", from);
        if (content != null && !content.isEmpty()) {
            return content;
        }
        return extractJsonStringValue(json, "text", from);
    }

    /**
     * Very small JSON string extractor for predictable provider payloads (coursework prototype).
     * TODO: replace with robust JSON parsing if responses become nested or reordered.
     */
    private static String extractJsonStringValue(String json, String field, int searchFrom) {
        if (json == null || field == null || searchFrom < 0) {
            return null;
        }
        String needle = "\"" + field + "\"";
        int idx = json.indexOf(needle, searchFrom);
        if (idx < 0) {
            return null;
        }
        int colon = json.indexOf(':', idx + needle.length());
        if (colon < 0) {
            return null;
        }
        int i = colon + 1;
        while (i < json.length() && Character.isWhitespace(json.charAt(i))) {
            i++;
        }
        if (i >= json.length() || json.charAt(i) != '"') {
            return null;
        }
        return readJsonStringContents(json, i);
    }

    private static String readJsonStringContents(String json, int openQuoteIndex) {
        StringBuilder sb = new StringBuilder();
        for (int i = openQuoteIndex + 1; i < json.length(); i++) {
            char c = json.charAt(i);
            if (c == '"') {
                break;
            }
            if (c == '\\' && i + 1 < json.length()) {
                char n = json.charAt(++i);
                switch (n) {
                    case 'n':
                        sb.append('\n');
                        break;
                    case 'r':
                        sb.append('\r');
                        break;
                    case 't':
                        sb.append('\t');
                        break;
                    case '"':
                        sb.append('"');
                        break;
                    case '\\':
                        sb.append('\\');
                        break;
                    default:
                        sb.append(n);
                }
                continue;
            }
            sb.append(c);
        }
        return sb.toString();
    }

}

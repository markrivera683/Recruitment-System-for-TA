package com.bupt.ta.util;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;

/** Minimal HTTPS JSON POST helper (JDK 11 {@link java.net.http.HttpClient}) — no extra libraries. */
public final class HttpJsonClient {

    private HttpJsonClient() {}

    /**
     * POST JSON and return the response body as a stream (for SSE / chunked completions).
     * Caller must close the stream. On non-2xx, throws with body snippet in message.
     */
    public static InputStream postJsonStream(String url, Map<String, String> headers, String body, int timeoutMs)
            throws IOException, InterruptedException {
        int t = Math.max(1_000, timeoutMs);
        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofMillis(t))
                .build();
        HttpRequest.Builder b = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofMillis(t))
                .POST(HttpRequest.BodyPublishers.ofString(body == null ? "" : body, StandardCharsets.UTF_8))
                .header("Accept", "text/event-stream")
                .header("Content-Type", "application/json; charset=utf-8");
        if (headers != null) {
            headers.forEach((k, v) -> {
                if (k != null && v != null) {
                    b.header(k, v);
                }
            });
        }
        HttpResponse<InputStream> resp = client.send(b.build(), HttpResponse.BodyHandlers.ofInputStream());
        int code = resp.statusCode();
        if (code < 200 || code >= 300) {
            String errBody = readStreamSnippet(resp.body(), 4_000);
            throw new IOException("HTTP " + code + ": " + errBody);
        }
        return resp.body();
    }

    private static String readStreamSnippet(InputStream in, int max) throws IOException {
        if (in == null) {
            return "";
        }
        byte[] buf = new byte[max];
        int n = in.read(buf);
        in.close();
        if (n <= 0) {
            return "";
        }
        return new String(buf, 0, n, StandardCharsets.UTF_8);
    }

    public static String postJson(String url, Map<String, String> headers, String body, int timeoutMs)
            throws IOException, InterruptedException {
        int t = Math.max(1_000, timeoutMs);
        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofMillis(t))
                .build();
        HttpRequest.Builder b = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofMillis(t))
                .POST(HttpRequest.BodyPublishers.ofString(body == null ? "" : body, StandardCharsets.UTF_8))
                .header("Content-Type", "application/json; charset=utf-8");
        if (headers != null) {
            headers.forEach((k, v) -> {
                if (k != null && v != null) {
                    b.header(k, v);
                }
            });
        }
        HttpResponse<String> resp = client.send(b.build(), HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        int code = resp.statusCode();
        if (code < 200 || code >= 300) {
            throw new IOException("HTTP " + code + ": " + truncate(resp.body(), 2_000));
        }
        return resp.body();
    }

    private static String truncate(String s, int max) {
        if (s == null || s.length() <= max) {
            return s;
        }
        return s.substring(0, max) + "...";
    }
}

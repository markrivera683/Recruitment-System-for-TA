package com.bupt.ta.util;

import java.io.IOException;
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

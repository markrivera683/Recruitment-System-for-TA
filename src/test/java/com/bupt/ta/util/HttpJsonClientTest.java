package com.bupt.ta.util;

import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HttpJsonClientTest {

    private HttpServer server;

    @AfterEach
    void stopServer() {
        if (server != null) {
            server.stop(0);
        }
    }

    @Test
    void postJson_returnsBodyOn200() throws Exception {
        startServer(200, "{\"ok\":true}");
        String url = "http://127.0.0.1:" + server.getAddress().getPort() + "/api";
        String body = HttpJsonClient.postJson(url, Collections.singletonMap("X-Test", "1"), "{}", 5000);
        assertTrue(body.contains("ok"));
    }

    @Test
    void postJson_throwsOn4xx() throws Exception {
        startServer(400, "bad request");
        String url = "http://127.0.0.1:" + server.getAddress().getPort() + "/api";
        IOException ex = assertThrows(IOException.class,
                () -> HttpJsonClient.postJson(url, null, "{}", 5000));
        assertTrue(ex.getMessage().contains("400"));
    }

    @Test
    void postJson_emptyBody() throws Exception {
        startServer(200, "");
        String url = "http://127.0.0.1:" + server.getAddress().getPort() + "/api";
        assertEquals("", HttpJsonClient.postJson(url, null, null, 5000));
    }

    private void startServer(int status, String responseBody) throws IOException {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/api", exchange -> {
            byte[] bytes = responseBody.getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(status, bytes.length);
            exchange.getResponseBody().write(bytes);
            exchange.close();
        });
        server.setExecutor(Executors.newSingleThreadExecutor());
        server.start();
    }
}

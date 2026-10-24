package com.example.toeic_api.modules.ai.service;

import static org.junit.jupiter.api.Assertions.*;

import com.example.toeic_api.shared.exception.*;
import com.fasterxml.jackson.databind.*;
import com.sun.net.httpserver.HttpServer;

import org.junit.jupiter.api.*;

import java.net.*;
import java.nio.charset.StandardCharsets;

class GroqJsonClientTest {
    final ObjectMapper mapper = new ObjectMapper();
    HttpServer server;
    GroqJsonClient client;
    JsonNode received;
    int status = 200;
    String response =
            "{\"choices\":[{\"finish_reason\":\"stop\",\"message\":{\"content\":\"{\\\"terms\\\":[\\\"invoice\\\"]}\"}}]}";

    @BeforeEach
    void setup() throws Exception {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext(
                "/chat",
                e -> {
                    received = mapper.readTree(e.getRequestBody());
                    var bytes = response.getBytes(StandardCharsets.UTF_8);
                    e.sendResponseHeaders(status, bytes.length);
                    try (var out = e.getResponseBody()) {
                        out.write(bytes);
                    }
                });
        server.start();
        client =
                new GroqJsonClient(
                        mapper,
                        "local-test-key",
                        URI.create("http://127.0.0.1:" + server.getAddress().getPort() + "/chat"));
    }

    @AfterEach
    void stop() {
        server.stop(0);
    }

    @Test
    void returnsJsonAndKeepsKeysOutOfPrompts() {
        assertEquals(
                "invoice",
                client.complete("openai/gpt-oss-20b", "Extract terms", "document", 500)
                        .path("terms")
                        .get(0)
                        .asText());
        assertEquals("low", received.path("reasoning_effort").asText());
        assertFalse(received.toString().contains("local-test-key"));
        client.complete("qwen/qwen3.8-27b", "Extract terms", "document", 500);
        assertEquals("none", received.path("reasoning_effort").asText());
    }

    @Test
    void mapsProviderModelAndQuotaErrorsAndRejectsTruncatedReplies() {
        status = 404;
        response = "{\"error\":{\"code\":\"model_not_found\"}}";
        assertEquals(
                ErrorCode.AI_MODEL_UNAVAILABLE,
                assertThrows(AppException.class, () -> client.complete("model", "x", "y", 500))
                        .getErrorCode());
        status = 429;
        assertEquals(
                ErrorCode.AI_BUSY,
                assertThrows(AppException.class, () -> client.complete("model", "x", "y", 500))
                        .getErrorCode());
        status = 200;
        response = "{\"choices\":[{\"finish_reason\":\"length\"}]}";
        assertEquals(
                ErrorCode.AI_RESPONSE_TRUNCATED,
                assertThrows(AppException.class, () -> client.complete("model", "x", "y", 500))
                        .getErrorCode());
    }
}

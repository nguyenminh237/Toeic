package com.example.toeic_api.modules.ai.service;

import static org.junit.jupiter.api.Assertions.*;

import com.example.toeic_api.shared.exception.AppException;
import com.example.toeic_api.shared.exception.ErrorCode;
import com.fasterxml.jackson.databind.*;
import com.sun.net.httpserver.HttpServer;

import org.junit.jupiter.api.*;

import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

class GroqExplanationServiceTest {
    private final ObjectMapper mapper = new ObjectMapper();
    private HttpServer server;
    private GroqExplanationService service;
    private JsonNode request;
    private int status;
    private String body;

    @BeforeEach
    void setup() throws Exception {
        status = 200;
        body =
                mapper.writeValueAsString(
                        Map.of(
                                "choices",
                                List.of(
                                        Map.of(
                                                "finish_reason",
                                                "stop",
                                                "message",
                                                Map.of("content", valid().toString())))));
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext(
                "/chat",
                e -> {
                    request = mapper.readTree(e.getRequestBody());
                    byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
                    e.sendResponseHeaders(status, bytes.length);
                    try (var out = e.getResponseBody()) {
                        out.write(bytes);
                    }
                });
        server.start();
        service =
                new GroqExplanationService(
                        mapper,
                        "test-secret",
                        "test-model",
                        URI.create("http://127.0.0.1:" + server.getAddress().getPort() + "/chat"));
    }

    @AfterEach
    void close() {
        server.stop(0);
    }

    private JsonNode context() throws Exception {
        return mapper.readTree(
                """
{"partNumber":2,"questionText":"Why?","correctAnswer":"A","options":{"A":"Because…","B":"Tomorrow","C":"At work"}}
""");
    }

    private JsonNode valid() throws Exception {
        return mapper.readTree(
                """
{"correctAnswer":"A","explanation":"Because answers why; the other responses give a time or place.",
"tip":"Look for a reason when the question begins with why.","translation":null}
""");
    }

    @Test
    void rendersConciseExplanationAndTipWithoutForcingTranslationOrFourSections() throws Exception {
        String result = service.generate(context());
        assertTrue(result.startsWith("Chọn A. Because answers why"));
        assertTrue(result.contains("Mẹo: Look for a reason"));
        assertFalse(result.contains("Dịch:"));
        assertFalse(result.contains("Bước 2:"));
        assertEquals("test-model", request.path("model").asText());
        assertEquals("json_object", request.path("response_format").path("type").asText());
        assertFalse(request.toString().contains("test-secret"));
    }

    @Test
    void rejectsChangedCorrectAnswer() throws Exception {
        var result = (com.fasterxml.jackson.databind.node.ObjectNode) valid();
        result.put("correctAnswer", "B");
        assertEquals(
                ErrorCode.AI_INVALID_RESPONSE,
                assertThrows(AppException.class, () -> service.render(result, context()))
                        .getErrorCode());
    }

    @Test
    void requiresExplanationAndTipButNotIndividualDistractors() throws Exception {
        var missingTip = (com.fasterxml.jackson.databind.node.ObjectNode) valid();
        missingTip.remove("tip");
        assertThrows(AppException.class, () -> service.render(missingTip, context()));
        var missingExplanation = (com.fasterxml.jackson.databind.node.ObjectNode) valid();
        missingExplanation.put("explanation", " ");
        assertThrows(AppException.class, () -> service.render(missingExplanation, context()));
    }

    @Test
    void rendersTranslationOnlyWhenProvidedForMeaningQuestions() throws Exception {
        var result = (com.fasterxml.jackson.databind.node.ObjectNode) valid();
        result.put("translation", "Hãy giữ lại hóa đơn.");
        assertTrue(service.render(result, context()).endsWith("Dịch: Hãy giữ lại hóa đơn."));
        result.remove("translation");
        assertFalse(service.render(result, context()).contains("Dịch:"));
        result.putArray("translation");
        assertThrows(AppException.class, () -> service.render(result, context()));
    }

    @Test
    void rejectsProviderRefusalAndMalformedResponse() throws Exception {
        assertThrows(
                AppException.class,
                () ->
                        service.render(
                                mapper.readTree("{\"error\":\"missing context\"}"), context()));
        body = "not-json";
        assertEquals(
                ErrorCode.AI_INVALID_RESPONSE,
                assertThrows(AppException.class, () -> service.generate(context())).getErrorCode());
    }

    @Test
    void mapsRateLimitsWithoutLeakingProviderBody() throws Exception {
        status = 429;
        body = "sensitive provider response";
        var error = assertThrows(AppException.class, () -> service.generate(context()));
        assertEquals(ErrorCode.AI_BUSY, error.getErrorCode());
        assertFalse(error.getMessage().contains(body));
    }

    @Test
    void rejectsTruncatedCompletion() throws Exception {
        body =
                mapper.writeValueAsString(
                        Map.of(
                                "choices",
                                List.of(
                                        Map.of(
                                                "finish_reason",
                                                "length",
                                                "message",
                                                Map.of("content", valid().toString())))));
        assertThrows(AppException.class, () -> service.generate(context()));
    }

    @Test
    void unavailableModelHasActionableErrorAndDoesNotLeakProviderBody() throws Exception {
        status = 404;
        body =
                "{\"error\":{\"code\":\"model_not_found\",\"message\":\"test-secret private"
                        + " details\"}}";
        var error = assertThrows(AppException.class, () -> service.generate(context()));
        assertEquals(ErrorCode.AI_MODEL_UNAVAILABLE, error.getErrorCode());
        assertTrue(error.getMessage().contains("GROQ_MODEL"));
        assertFalse(error.getMessage().contains("test-secret"));
    }

    @Test
    void providerAuthenticationErrorDoesNotBecomeAnApplication401() throws Exception {
        for (int code : new int[] {401, 403}) {
            status = code;
            body = "{\"error\":{\"code\":\"invalid_api_key\"}}";
            var error = assertThrows(AppException.class, () -> service.generate(context()));
            assertEquals(ErrorCode.AI_PROVIDER_AUTH_ERROR, error.getErrorCode());
            assertEquals(503, error.getErrorCode().getHttpStatusCode().value());
        }
    }

    @Test
    void requestErrorAndProviderOutageAreDistinguished() throws Exception {
        status = 400;
        body = "not-json";
        assertEquals(
                ErrorCode.AI_REQUEST_REJECTED,
                assertThrows(AppException.class, () -> service.generate(context())).getErrorCode());
        status = 503;
        assertEquals(
                ErrorCode.AI_FAILED,
                assertThrows(AppException.class, () -> service.generate(context())).getErrorCode());
    }

    @Test
    void gptOssUsesLowReasoningAndTrimsConfiguration() throws Exception {
        service =
                new GroqExplanationService(
                        mapper,
                        " test-secret ",
                        " openai/gpt-oss-20b ",
                        URI.create("http://127.0.0.1:" + server.getAddress().getPort() + "/chat"));
        service.generate(context());
        assertEquals("openai/gpt-oss-20b", request.path("model").asText());
        assertEquals("low", request.path("reasoning_effort").asText());
        assertEquals(4096, request.path("max_completion_tokens").asInt());
    }

    @Test
    void networkFailureIsReportedSeparately() throws Exception {
        int port = server.getAddress().getPort();
        server.stop(0);
        service =
                new GroqExplanationService(
                        mapper,
                        "test-secret",
                        "test-model",
                        URI.create("http://127.0.0.1:" + port + "/chat"));
        assertEquals(
                ErrorCode.AI_CONNECTION_FAILED,
                assertThrows(AppException.class, () -> service.generate(context())).getErrorCode());
    }

    @Test
    void missingKeyDoesNotCallProvider() throws Exception {
        var unconfigured = new GroqExplanationService(mapper, "", "test-model");
        assertEquals(
                ErrorCode.AI_NOT_CONFIGURED,
                assertThrows(AppException.class, () -> unconfigured.generate(context()))
                        .getErrorCode());
        assertNull(request);
    }
}

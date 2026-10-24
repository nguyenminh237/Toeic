package com.example.toeic_api.modules.ai.service;

import com.example.toeic_api.shared.exception.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.*;

import org.springframework.beans.factory.annotation.*;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.http.*;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.Semaphore;

@Component
public class GroqJsonClient {
    private final ObjectMapper mapper;
    private final String key;
    private final URI endpoint;
    private final HttpClient client =
            HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();
    private final Semaphore slots = new Semaphore(2);

    @Autowired
    public GroqJsonClient(ObjectMapper mapper, @Value("${app.groq.api-key:}") String key) {
        this(mapper, key, URI.create("https://api.groq.com/openai/v1/chat/completions"));
    }

    GroqJsonClient(ObjectMapper mapper, String key, URI endpoint) {
        this.mapper = mapper;
        this.key = key == null ? "" : key.trim();
        this.endpoint = endpoint;
    }

    public JsonNode complete(String model, String instructions, Object content, int tokens) {
        if (key.isBlank()) throw new AppException(ErrorCode.AI_NOT_CONFIGURED);
        if (model == null || model.isBlank())
            throw new AppException(ErrorCode.AI_MODEL_UNAVAILABLE);
        if (!slots.tryAcquire()) throw new AppException(ErrorCode.AI_BUSY);
        try {
            var payload = new LinkedHashMap<String, Object>();
            payload.put("model", model);
            payload.put("temperature", 0.2);
            payload.put("max_completion_tokens", tokens);
            payload.put("response_format", Map.of("type", "json_object"));
            payload.put(
                    "messages",
                    List.of(
                            Map.of("role", "system", "content", instructions),
                            Map.of("role", "user", "content", content)));
            if (model.startsWith("openai/gpt-oss-")) payload.put("reasoning_effort", "low");
            if (model.startsWith("qwen/qwen3.")) payload.put("reasoning_effort", "none");
            var request =
                    HttpRequest.newBuilder(endpoint)
                            .timeout(Duration.ofSeconds(80))
                            .header("Authorization", "Bearer " + key)
                            .header("Content-Type", "application/json")
                            .POST(
                                    HttpRequest.BodyPublishers.ofString(
                                            mapper.writeValueAsString(payload)))
                            .build();
            var response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() / 100 != 2) {
                int status = response.statusCode();
                var code = mapper.readTree(response.body()).path("error").path("code").asText();
                throw new AppException(
                        status == 401 || status == 403
                                ? ErrorCode.AI_PROVIDER_AUTH_ERROR
                                : status == 429
                                        ? ErrorCode.AI_BUSY
                                        : status == 404
                                                        || Set.of(
                                                                        "model_not_found",
                                                                        "model_decommissioned")
                                                                .contains(code)
                                                ? ErrorCode.AI_MODEL_UNAVAILABLE
                                                : status == 400 || status == 413 || status == 422
                                                        ? ErrorCode.AI_REQUEST_REJECTED
                                                        : ErrorCode.AI_FAILED);
            }
            var choice = mapper.readTree(response.body()).path("choices").path(0);
            if (choice.path("finish_reason").asText().equals("length"))
                throw new AppException(ErrorCode.AI_RESPONSE_TRUNCATED);
            if (!choice.path("finish_reason").asText().equals("stop"))
                throw new AppException(ErrorCode.AI_INVALID_RESPONSE);
            var result = mapper.readTree(choice.path("message").path("content").asText());
            if (result == null || !result.isObject() || result.has("error"))
                throw new AppException(ErrorCode.AI_INVALID_RESPONSE);
            return result;
        } catch (AppException e) {
            throw e;
        } catch (HttpTimeoutException e) {
            throw new AppException(ErrorCode.AI_TIMEOUT);
        } catch (JsonProcessingException e) {
            throw new AppException(ErrorCode.AI_INVALID_RESPONSE);
        } catch (IOException e) {
            throw new AppException(ErrorCode.AI_CONNECTION_FAILED);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new AppException(ErrorCode.AI_FAILED);
        } finally {
            slots.release();
        }
    }
}

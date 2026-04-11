package com.example.bossbot.ai.service;

import com.example.bossbot.ai.config.OllamaConfig;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

@Component
@Slf4j
@ConditionalOnProperty(name = "ollama.enabled", havingValue = "true")
public class OllamaHttpClient {

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final String baseUrl;

    public OllamaHttpClient(OllamaConfig config) {
        this.baseUrl = config.getUrl();
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        this.objectMapper = new ObjectMapper();
    }

    public JsonNode post(String path, Object requestBody) {
        try {
            HttpRequest request = buildRequest(path, requestBody, Duration.ofSeconds(30));
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                throw new OllamaException("Ollama returned status " + response.statusCode() + ": " + response.body());
            }

            return objectMapper.readTree(response.body());
        } catch (OllamaException e) {
            throw e;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new OllamaException("Ollama request interrupted", e);
        } catch (Exception e) {
            throw new OllamaException("Failed to call Ollama " + path, e);
        }
    }

    public InputStream postStream(String path, Object requestBody) {
        try {
            HttpRequest request = buildRequest(path, requestBody, null);
            HttpResponse<InputStream> response = httpClient.send(request, HttpResponse.BodyHandlers.ofInputStream());

            if (response.statusCode() != 200) {
                String body = new String(response.body().readAllBytes());
                throw new OllamaException("Ollama chat returned status " + response.statusCode() + ": " + body);
            }

            return response.body();
        } catch (OllamaException e) {
            throw e;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new OllamaException("Ollama stream request interrupted", e);
        } catch (Exception e) {
            throw new OllamaException("Failed to stream from Ollama " + path, e);
        }
    }

    public JsonNode parseJson(String line) {
        try {
            return objectMapper.readTree(line);
        } catch (Exception e) {
            throw new OllamaException("Failed to parse Ollama response", e);
        }
    }

    private HttpRequest buildRequest(String path, Object requestBody, Duration timeout) {
        String json = serialize(requestBody);
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + path))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json));

        if (timeout != null) {
            builder.timeout(timeout);
        }

        return builder.build();
    }

    private String serialize(Object body) {
        try {
            return objectMapper.writeValueAsString(body);
        } catch (Exception e) {
            throw new OllamaException("Failed to serialize request body", e);
        }
    }
}

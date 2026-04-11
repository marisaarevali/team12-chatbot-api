package com.example.bossbot.ai.service;

import com.example.bossbot.ai.config.OllamaConfig;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
@ConditionalOnProperty(name = "ollama.enabled", havingValue = "true")
public class OllamaEmbeddingServiceImpl implements OllamaEmbeddingService {

    private final OllamaConfig config;
    private final OllamaHttpClient httpClient;

    @Override
    public double[] embed(String text) {
        return embedBatch(List.of(text)).getFirst();
    }

    @Override
    public List<double[]> embedBatch(List<String> texts) {
        JsonNode root = httpClient.post("/api/embed",
                Map.of("model", config.getModel(), "input", texts));

        JsonNode embeddingsNode = root.get("embeddings");
        if (embeddingsNode == null || !embeddingsNode.isArray()) {
            throw new OllamaException("Unexpected Ollama response format: missing 'embeddings' array");
        }

        List<double[]> results = new ArrayList<>();
        for (JsonNode embeddingNode : embeddingsNode) {
            double[] embedding = new double[embeddingNode.size()];
            for (int i = 0; i < embeddingNode.size(); i++) {
                embedding[i] = embeddingNode.get(i).asDouble();
            }
            results.add(embedding);
        }

        if (results.size() != texts.size()) {
            throw new OllamaException("Ollama returned " + results.size() + " embeddings for " + texts.size() + " inputs");
        }

        return results;
    }
}

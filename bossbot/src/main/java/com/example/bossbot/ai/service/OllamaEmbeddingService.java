package com.example.bossbot.ai.service;

import java.util.List;

public interface OllamaEmbeddingService {

    double[] embed(String text);

    List<double[]> embedBatch(List<String> texts);
}

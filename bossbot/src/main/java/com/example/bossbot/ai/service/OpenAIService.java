package com.example.bossbot.ai.service;

import com.example.bossbot.message.dto.MessageResponse;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

public interface OpenAIService {

    record ChatResult(String response, boolean usedFallback) {}

    /**
     * Streams a chat response based on conversation history and the latest user message.
     *
     * @param conversationHistory previous messages in the conversation
     * @param userMessage         the latest user message
     * @param tokenCallback       called for each token as it arrives
     * @param cancelFlag          set to true to abort streaming early
     * @return the full response and whether a fallback AI provider was used
     */
    ChatResult streamChat(List<MessageResponse> conversationHistory, String userMessage, Consumer<String> tokenCallback, AtomicBoolean cancelFlag);
}

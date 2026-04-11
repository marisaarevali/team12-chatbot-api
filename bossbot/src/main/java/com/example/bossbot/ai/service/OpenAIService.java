package com.example.bossbot.ai.service;

import com.example.bossbot.message.dto.MessageResponse;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

public interface OpenAIService {

    /**
     * Streams a chat response based on conversation history and the latest user message.
     *
     * @param conversationHistory previous messages in the conversation
     * @param userMessage         the latest user message
     * @param tokenCallback       called for each token as it arrives
     * @param cancelFlag          set to true to abort streaming early
     * @return the full concatenated response (may be partial if cancelled)
     */
    String streamChat(List<MessageResponse> conversationHistory, String userMessage, Consumer<String> tokenCallback, AtomicBoolean cancelFlag);
}

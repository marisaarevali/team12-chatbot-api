package com.example.bossbot.ai.service;

import com.example.bossbot.ai.config.OllamaConfig;
import com.example.bossbot.message.dto.MessageResponse;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

@Service
@Slf4j
@RequiredArgsConstructor
@ConditionalOnExpression("'${openai.api-key:mock}' == 'mock' and ${ollama.enabled:false}")
public class OllamaChatServiceImpl implements OpenAIService {

    private final OllamaConfig config;
    private final PromptBuilder promptBuilder;
    private final OllamaHttpClient httpClient;

    @Override
    public String streamChat(List<MessageResponse> conversationHistory, String userMessage, Consumer<String> tokenCallback, AtomicBoolean cancelFlag) {
        log.info("Using Ollama chat with model: {}", config.getChatModel());

        List<PromptBuilder.ChatMessage> chatMessages = promptBuilder.buildMessages(conversationHistory, userMessage);
        List<Map<String, String>> messages = chatMessages.stream()
                .map(msg -> Map.of("role", msg.role(), "content", msg.content()))
                .toList();

        InputStream stream = httpClient.postStream("/api/chat",
                Map.of("model", config.getChatModel(), "messages", messages, "stream", true));

        StringBuilder fullResponse = new StringBuilder();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (cancelFlag.get()) {
                    log.info("Ollama chat cancelled by user");
                    break;
                }
                if (line.isBlank()) continue;

                JsonNode node = httpClient.parseJson(line);
                JsonNode messageNode = node.get("message");
                if (messageNode != null && messageNode.has("content")) {
                    String content = messageNode.get("content").asText();
                    if (!content.isEmpty()) {
                        tokenCallback.accept(content);
                        fullResponse.append(content);
                    }
                }
            }
        } catch (OllamaException e) {
            throw e;
        } catch (Exception e) {
            throw new OllamaException("Error reading Ollama chat stream", e);
        }

        log.info("Ollama chat response completed. Total length: {}", fullResponse.length());
        return fullResponse.toString();
    }
}

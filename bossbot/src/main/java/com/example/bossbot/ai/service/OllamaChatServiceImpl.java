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
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

@Service
@Slf4j
@RequiredArgsConstructor
@ConditionalOnExpression("'${openai.api-key:mock}' == 'mock' and ${ollama.enabled:false}")
public class OllamaChatServiceImpl implements OpenAIService {

    private static final long STREAM_READ_TIMEOUT_MS = 120_000;

    private final OllamaConfig config;
    private final PromptBuilder promptBuilder;
    private final OllamaHttpClient httpClient;
    private final ScheduledExecutorService watchdog = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, "ollama-stream-watchdog");
        t.setDaemon(true);
        return t;
    });

    @Override
    public ChatResult streamChat(List<MessageResponse> conversationHistory, String userMessage, Consumer<String> tokenCallback, AtomicBoolean cancelFlag) {
        log.info("Using Ollama chat with model: {}", config.getChatModel());

        List<PromptBuilder.ChatMessage> chatMessages = promptBuilder.buildMessages(conversationHistory, userMessage);
        List<Map<String, String>> messages = chatMessages.stream()
                .map(msg -> Map.of("role", msg.role(), "content", msg.content()))
                .toList();

        InputStream stream = httpClient.postStream("/api/chat",
                Map.of("model", config.getChatModel(), "messages", messages, "stream", true));

        StringBuilder fullResponse = new StringBuilder();
        AtomicLong lastActivity = new AtomicLong(System.currentTimeMillis());

        ScheduledFuture<?> timeoutTask = watchdog.scheduleAtFixedRate(() -> {
            if (System.currentTimeMillis() - lastActivity.get() > STREAM_READ_TIMEOUT_MS) {
                log.warn("Ollama stream read timeout ({}ms with no data), closing stream", STREAM_READ_TIMEOUT_MS);
                try {
                    stream.close();
                } catch (Exception e) {
                    log.debug("Error closing timed-out stream", e);
                }
            }
        }, 30, 30, TimeUnit.SECONDS);

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream))) {
            String line;
            while ((line = reader.readLine()) != null) {
                lastActivity.set(System.currentTimeMillis());
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
        } finally {
            timeoutTask.cancel(false);
        }

        log.info("Ollama chat response completed. Total length: {}", fullResponse.length());
        return new ChatResult(fullResponse.toString(), false);
    }
}

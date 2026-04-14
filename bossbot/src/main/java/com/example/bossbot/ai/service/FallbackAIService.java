package com.example.bossbot.ai.service;

import com.example.bossbot.ai.config.OllamaConfig;
import com.example.bossbot.ai.config.OpenAIConfig;
import com.example.bossbot.message.dto.MessageResponse;
import com.openai.errors.RateLimitException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

@Service
@Slf4j
@ConditionalOnExpression("'${openai.api-key:mock}' != 'mock' and ${ollama.enabled:false}")
public class FallbackAIService implements OpenAIService {

    private static final long RETRY_COOLDOWN_MS = 5 * 60 * 1000; // 5 minutes

    private final OpenAIServiceImpl openAIDelegate;
    private final OllamaChatServiceImpl ollamaDelegate;
    private final AtomicLong lastQuotaFailure = new AtomicLong(0);

    public FallbackAIService(OpenAIConfig openAIConfig, OllamaConfig ollamaConfig,
                             PromptBuilder promptBuilder, OllamaHttpClient httpClient) {
        this.openAIDelegate = new OpenAIServiceImpl(openAIConfig, promptBuilder);
        this.ollamaDelegate = new OllamaChatServiceImpl(ollamaConfig, promptBuilder, httpClient);
        log.info("FallbackAIService initialized: OpenAI primary, Ollama ({}) as fallback", ollamaConfig.getChatModel());
    }

    // Package-private constructor for testing
    FallbackAIService(OpenAIServiceImpl openAIDelegate, OllamaChatServiceImpl ollamaDelegate) {
        this.openAIDelegate = openAIDelegate;
        this.ollamaDelegate = ollamaDelegate;
    }

    // Package-private for testing — simulate cooldown state
    void setLastQuotaFailure(long timestamp) {
        lastQuotaFailure.set(timestamp);
    }

    @Override
    public ChatResult streamChat(List<MessageResponse> conversationHistory, String userMessage,
                                 Consumer<String> tokenCallback, AtomicBoolean cancelFlag) {
        long failedAt = lastQuotaFailure.get();
        boolean inCooldown = failedAt > 0 && (System.currentTimeMillis() - failedAt) < RETRY_COOLDOWN_MS;

        if (inCooldown) {
            log.info("OpenAI in cooldown (retrying in {}s), using Ollama",
                    (RETRY_COOLDOWN_MS - (System.currentTimeMillis() - failedAt)) / 1000);
            ChatResult ollamaResult = ollamaDelegate.streamChat(conversationHistory, userMessage, tokenCallback, cancelFlag);
            return new ChatResult(ollamaResult.response(), true);
        }

        StringBuilder partialResponse = new StringBuilder();
        Consumer<String> trackingCallback = token -> {
            tokenCallback.accept(token);
            partialResponse.append(token);
        };

        try {
            ChatResult result = openAIDelegate.streamChat(conversationHistory, userMessage, trackingCallback, cancelFlag);
            if (failedAt > 0) {
                log.info("OpenAI recovered, clearing cooldown");
                lastQuotaFailure.set(0);
            }
            return result;
        } catch (RateLimitException e) {
            lastQuotaFailure.set(System.currentTimeMillis());

            if (!partialResponse.isEmpty()) {
                log.warn("OpenAI rate limited mid-stream ({} chars already sent), returning partial response", partialResponse.length());
                return new ChatResult(partialResponse.toString(), true);
            }

            log.warn("OpenAI rate limit / quota exceeded ({}), falling back to Ollama", e.getMessage());
            ChatResult ollamaResult = ollamaDelegate.streamChat(conversationHistory, userMessage, tokenCallback, cancelFlag);
            return new ChatResult(ollamaResult.response(), true);
        }
    }
}

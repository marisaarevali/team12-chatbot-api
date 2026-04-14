package com.example.bossbot.ai.service;

import com.openai.core.http.Headers;
import com.openai.errors.RateLimitException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("FallbackAIService Tests")
class FallbackAIServiceTest {

    @Mock
    private OpenAIServiceImpl openAIDelegate;

    @Mock
    private OllamaChatServiceImpl ollamaDelegate;

    private FallbackAIService service;
    private List<String> receivedTokens;
    private AtomicBoolean cancelFlag;

    private static RateLimitException rateLimitException() {
        return RateLimitException.builder()
                .headers(Headers.builder().build())
                .build();
    }

    @BeforeEach
    void setUp() {
        service = new FallbackAIService(openAIDelegate, ollamaDelegate);
        receivedTokens = new ArrayList<>();
        cancelFlag = new AtomicBoolean(false);
    }

    @Test
    @DisplayName("Should use OpenAI when quota is not exhausted")
    void testOpenAI_success() {
        when(openAIDelegate.streamChat(any(), eq("Hi"), any(), any()))
                .thenReturn(new OpenAIService.ChatResult("OpenAI response", false));

        OpenAIService.ChatResult result = service.streamChat(List.of(), "Hi", receivedTokens::add, cancelFlag);

        assertThat(result.response()).isEqualTo("OpenAI response");
        assertThat(result.usedFallback()).isFalse();
        verify(ollamaDelegate, never()).streamChat(any(), any(), any(), any());
    }

    @Test
    @DisplayName("Should fall back to Ollama on RateLimitException")
    void testFallback_onRateLimit() {
        when(openAIDelegate.streamChat(any(), eq("Hi"), any(), any()))
                .thenThrow(rateLimitException());
        when(ollamaDelegate.streamChat(any(), eq("Hi"), any(), any()))
                .thenReturn(new OpenAIService.ChatResult("Ollama response", false));

        OpenAIService.ChatResult result = service.streamChat(List.of(), "Hi", receivedTokens::add, cancelFlag);

        assertThat(result.response()).isEqualTo("Ollama response");
        assertThat(result.usedFallback()).isTrue();
        verify(openAIDelegate).streamChat(any(), eq("Hi"), any(), any());
        verify(ollamaDelegate).streamChat(any(), eq("Hi"), any(), any());
    }

    @Test
    @DisplayName("Should skip OpenAI during cooldown after quota exhausted")
    void testDirectOllama_duringCooldown() {
        // Simulate a recent failure (within cooldown)
        service.setLastQuotaFailure(System.currentTimeMillis());

        when(ollamaDelegate.streamChat(any(), eq("Hello"), any(), any()))
                .thenReturn(new OpenAIService.ChatResult("Ollama response", false));

        OpenAIService.ChatResult result = service.streamChat(List.of(), "Hello", receivedTokens::add, cancelFlag);

        assertThat(result.response()).isEqualTo("Ollama response");
        assertThat(result.usedFallback()).isTrue();
        verify(openAIDelegate, never()).streamChat(any(), any(), any(), any());
    }

    @Test
    @DisplayName("Should retry OpenAI after cooldown expires and recover")
    void testRetryOpenAI_afterCooldownExpires() {
        // Simulate an old failure (cooldown expired)
        service.setLastQuotaFailure(System.currentTimeMillis() - 6 * 60 * 1000);

        when(openAIDelegate.streamChat(any(), eq("Hi"), any(), any()))
                .thenReturn(new OpenAIService.ChatResult("OpenAI is back", false));

        OpenAIService.ChatResult result = service.streamChat(List.of(), "Hi", receivedTokens::add, cancelFlag);

        assertThat(result.response()).isEqualTo("OpenAI is back");
        assertThat(result.usedFallback()).isFalse();
        verify(ollamaDelegate, never()).streamChat(any(), any(), any(), any());
    }

    @Test
    @DisplayName("Should not catch non-rate-limit exceptions")
    void testNonRateLimitException_propagates() {
        when(openAIDelegate.streamChat(any(), eq("Hi"), any(), any()))
                .thenThrow(new RuntimeException("Network error"));

        assertThatThrownBy(() -> service.streamChat(List.of(), "Hi", receivedTokens::add, cancelFlag))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Network error");

        verify(ollamaDelegate, never()).streamChat(any(), any(), any(), any());
    }

    @Test
    @DisplayName("Should propagate OllamaException when fallback also fails")
    void testOllamaFailure_afterFallback() {
        when(openAIDelegate.streamChat(any(), eq("Hi"), any(), any()))
                .thenThrow(rateLimitException());
        when(ollamaDelegate.streamChat(any(), eq("Hi"), any(), any()))
                .thenThrow(new OllamaException("Ollama is down"));

        assertThatThrownBy(() -> service.streamChat(List.of(), "Hi", receivedTokens::add, cancelFlag))
                .isInstanceOf(OllamaException.class)
                .hasMessage("Ollama is down");
    }
}

package com.example.bossbot.ai.service;

import com.example.bossbot.ai.config.OllamaConfig;
import com.example.bossbot.message.dto.MessageResponse;
import com.example.bossbot.message.entity.MessageRole;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("OllamaChatService Tests")
class OllamaChatServiceImplTest {

    @Mock
    private OllamaHttpClient httpClient;

    private OllamaChatServiceImpl service;
    private List<String> receivedTokens;
    private AtomicBoolean cancelFlag;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        OllamaConfig config = new OllamaConfig();
        config.setChatModel("llama3.2:3b");

        com.example.bossbot.chat.config.ChatConfig chatConfig = new com.example.bossbot.chat.config.ChatConfig();
        chatConfig.setSystemPrompt("You are BossBot.");
        chatConfig.setMaxHistoryMessages(20);
        PromptBuilder promptBuilder = new PromptBuilder(chatConfig);

        service = new OllamaChatServiceImpl(config, promptBuilder, httpClient);
        receivedTokens = new ArrayList<>();
        cancelFlag = new AtomicBoolean(false);
    }

    @Test
    @DisplayName("Should stream chat response from Ollama")
    void testStreamChat_success() throws Exception {
        String streamBody = """
                {"message":{"role":"assistant","content":"Hello"},"done":false}
                {"message":{"role":"assistant","content":" there!"},"done":false}
                {"message":{"role":"assistant","content":""},"done":true}
                """;
        when(httpClient.postStream(eq("/api/chat"), any()))
                .thenReturn(new ByteArrayInputStream(streamBody.getBytes(StandardCharsets.UTF_8)));
        when(httpClient.parseJson(any(String.class))).thenAnswer(inv ->
                objectMapper.readTree(inv.getArgument(0, String.class)));

        String result = service.streamChat(List.of(), "Hi", receivedTokens::add, cancelFlag);

        assertThat(result).isEqualTo("Hello there!");
        assertThat(receivedTokens).containsExactly("Hello", " there!");
    }

    @Test
    @DisplayName("Should include conversation history in request")
    void testStreamChat_withHistory() throws Exception {
        MessageResponse historyMsg = MessageResponse.builder()
                .id(1L).conversationId(1L).role(MessageRole.USER).content("Previous message")
                .isActive(true).createdAt(LocalDateTime.now()).createdBy(1L).build();

        String streamBody = "{\"message\":{\"role\":\"assistant\",\"content\":\"OK\"},\"done\":true}\n";
        when(httpClient.postStream(eq("/api/chat"), any()))
                .thenReturn(new ByteArrayInputStream(streamBody.getBytes(StandardCharsets.UTF_8)));
        when(httpClient.parseJson(any(String.class))).thenAnswer(inv ->
                objectMapper.readTree(inv.getArgument(0, String.class)));

        String result = service.streamChat(List.of(historyMsg), "New message", receivedTokens::add, cancelFlag);

        assertThat(result).isEqualTo("OK");
    }

    @Test
    @DisplayName("Should throw on non-200 status")
    void testStreamChat_httpError() {
        when(httpClient.postStream(eq("/api/chat"), any()))
                .thenThrow(new OllamaException("Ollama chat returned status 500"));

        assertThatThrownBy(() -> service.streamChat(List.of(), "Hi", receivedTokens::add, cancelFlag))
                .isInstanceOf(OllamaException.class)
                .hasMessageContaining("Ollama chat returned status 500");
    }

    @Test
    @DisplayName("Should throw on connection failure")
    void testStreamChat_connectionFailure() {
        when(httpClient.postStream(eq("/api/chat"), any()))
                .thenThrow(new OllamaException("Failed to stream from Ollama /api/chat",
                        new java.net.ConnectException("Connection refused")));

        assertThatThrownBy(() -> service.streamChat(List.of(), "Hi", receivedTokens::add, cancelFlag))
                .isInstanceOf(OllamaException.class);
    }

    @Test
    @DisplayName("Should stop streaming when cancel flag is set")
    void testStreamChat_cancellation() throws Exception {
        String streamBody = """
                {"message":{"role":"assistant","content":"First"},"done":false}
                {"message":{"role":"assistant","content":" Second"},"done":false}
                {"message":{"role":"assistant","content":" Third"},"done":false}
                {"message":{"role":"assistant","content":""},"done":true}
                """;
        when(httpClient.postStream(eq("/api/chat"), any()))
                .thenReturn(new ByteArrayInputStream(streamBody.getBytes(StandardCharsets.UTF_8)));
        when(httpClient.parseJson(any(String.class))).thenAnswer(inv ->
                objectMapper.readTree(inv.getArgument(0, String.class)));

        AtomicBoolean earlyCancel = new AtomicBoolean(false);
        List<String> tokens = new ArrayList<>();
        Consumer<String> callback = token -> {
            tokens.add(token);
            earlyCancel.set(true);
        };

        String result = service.streamChat(List.of(), "Hi", callback, earlyCancel);

        assertThat(tokens).containsExactly("First");
        assertThat(result).isEqualTo("First");
    }
}

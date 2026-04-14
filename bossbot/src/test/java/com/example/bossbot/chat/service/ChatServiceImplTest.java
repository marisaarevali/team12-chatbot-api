package com.example.bossbot.chat.service;

import com.example.bossbot.ai.service.OpenAIService;
import com.example.bossbot.chat.dto.ChatWebSocketResponse;
import com.example.bossbot.chat.dto.ModerationResult;
import com.example.bossbot.message.dto.CreateMessageRequest;
import com.example.bossbot.message.dto.MessageResponse;
import com.example.bossbot.message.entity.MessageRole;
import com.example.bossbot.message.service.MessageService;
import com.example.bossbot.stampanswer.dto.StampAnswerResponse;
import com.example.bossbot.stampanswer.service.StampAnswerService;
import com.example.bossbot.stampanswer.service.StampMatcherService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ChatService Tests")
class ChatServiceImplTest {

    @Mock
    private MessageService messageService;

    @Mock
    private ContentModerationService contentModerationService;

    @Mock
    private StampAnswerService stampAnswerService;

    @Mock
    private StampMatcherService stampMatcherService;

    @Mock
    private OpenAIService openAIService;

    @InjectMocks
    private ChatServiceImpl service;

    private MessageResponse userMessageResponse;
    private MessageResponse botMessageResponse;
    private List<ChatWebSocketResponse> sentResponses;
    private Consumer<ChatWebSocketResponse> send;
    private AtomicBoolean cancelFlag;

    @BeforeEach
    void setUp() {
        userMessageResponse = MessageResponse.builder()
                .id(1L)
                .conversationId(10L)
                .role(MessageRole.USER)
                .content("Hello")
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .createdBy(1L)
                .build();

        botMessageResponse = MessageResponse.builder()
                .id(2L)
                .conversationId(10L)
                .role(MessageRole.BOT)
                .content("Hi there!")
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .createdBy(1L)
                .build();

        sentResponses = new ArrayList<>();
        send = sentResponses::add;
        cancelFlag = new AtomicBoolean(false);
    }

    @Test
    @DisplayName("Should block message when HIGH severity moderation result")
    void testProcessMessage_BlockedByModeration() {
        // Given
        when(messageService.create(any(CreateMessageRequest.class))).thenReturn(userMessageResponse);
        when(contentModerationService.check("Hello")).thenReturn(
                ModerationResult.of(ModerationResult.Severity.HIGH, "badword")
        );

        // When
        service.processMessage(10L, "Hello", send, cancelFlag);

        // Then
        assertThat(sentResponses).hasSize(2);
        assertThat(sentResponses.get(0).getType()).isEqualTo("MESSAGE");
        assertThat(sentResponses.get(1).getType()).isEqualTo("BLOCKED");
        verify(stampAnswerService, never()).getByQuestion(any());
        verify(openAIService, never()).streamChat(any(), any(), any(), any());
    }

    @Test
    @DisplayName("Should warn but continue when LOW severity moderation result")
    void testProcessMessage_WarnedByModeration() {
        // Given
        when(messageService.create(any(CreateMessageRequest.class)))
                .thenReturn(userMessageResponse)
                .thenReturn(botMessageResponse);
        when(contentModerationService.check("Hello")).thenReturn(
                ModerationResult.of(ModerationResult.Severity.LOW, "mildword")
        );
        when(stampAnswerService.getByQuestion("Hello")).thenReturn(
                StampAnswerResponse.builder().id(1L).answer("Hi there!").build()
        );

        // When
        service.processMessage(10L, "Hello", send, cancelFlag);

        // Then
        assertThat(sentResponses).hasSize(3);
        assertThat(sentResponses.get(0).getType()).isEqualTo("MESSAGE");
        assertThat(sentResponses.get(1).getType()).isEqualTo("WARNING");
        assertThat(sentResponses.get(2).getType()).isEqualTo("MESSAGE");
    }

    @Test
    @DisplayName("Should return stamp answer on exact match")
    void testProcessMessage_ExactStampAnswer() {
        // Given
        when(messageService.create(any(CreateMessageRequest.class)))
                .thenReturn(userMessageResponse)
                .thenReturn(botMessageResponse);
        when(contentModerationService.check("Hello")).thenReturn(ModerationResult.none());
        when(stampAnswerService.getByQuestion("Hello")).thenReturn(
                StampAnswerResponse.builder().id(1L).answer("Hi there!").build()
        );

        // When
        service.processMessage(10L, "Hello", send, cancelFlag);

        // Then
        assertThat(sentResponses).hasSize(2);
        assertThat(sentResponses.get(0).getType()).isEqualTo("MESSAGE");
        assertThat(sentResponses.get(1).getType()).isEqualTo("MESSAGE");
        verify(openAIService, never()).streamChat(any(), any(), any(), any());
    }

    @Test
    @DisplayName("Should return stamp answer on semantic match")
    void testProcessMessage_SemanticMatch() {
        // Given
        StampAnswerResponse semanticMatch = StampAnswerResponse.builder().id(2L).answer("Semantic answer").build();
        when(messageService.create(any(CreateMessageRequest.class)))
                .thenReturn(userMessageResponse)
                .thenReturn(botMessageResponse);
        when(contentModerationService.check("Hello")).thenReturn(ModerationResult.none());
        when(stampAnswerService.getByQuestion("Hello")).thenReturn(null);
        when(stampMatcherService.findBestMatch("Hello")).thenReturn(Optional.of(semanticMatch));

        // When
        service.processMessage(10L, "Hello", send, cancelFlag);

        // Then
        assertThat(sentResponses).hasSize(2);
        assertThat(sentResponses.get(0).getType()).isEqualTo("MESSAGE");
        assertThat(sentResponses.get(1).getType()).isEqualTo("MESSAGE");
        verify(stampAnswerService).recordUsage(2L);
        verify(openAIService, never()).streamChat(any(), any(), any(), any());
    }

    @Test
    @DisplayName("Should fall back to AI when no stamp answer found")
    void testProcessMessage_AIFallback() {
        // Given
        when(messageService.create(any(CreateMessageRequest.class)))
                .thenReturn(userMessageResponse)
                .thenReturn(botMessageResponse);
        when(contentModerationService.check("Hello")).thenReturn(ModerationResult.none());
        when(stampAnswerService.getByQuestion("Hello")).thenReturn(null);
        when(stampMatcherService.findBestMatch("Hello")).thenReturn(Optional.empty());
        when(messageService.getAll(10L)).thenReturn(List.of(userMessageResponse));
        when(openAIService.streamChat(any(), eq("Hello"), any(), any())).thenReturn(new OpenAIService.ChatResult("AI response", false));

        // When
        service.processMessage(10L, "Hello", send, cancelFlag);

        // Then
        assertThat(sentResponses).hasSize(3);
        assertThat(sentResponses.get(0).getType()).isEqualTo("MESSAGE");
        assertThat(sentResponses.get(1).getType()).isEqualTo("STREAM_START");
        assertThat(sentResponses.get(2).getType()).isEqualTo("STREAM_END");
    }

    @Test
    @DisplayName("Should send error response when exception occurs")
    void testProcessMessage_Exception() {
        // Given
        when(messageService.create(any(CreateMessageRequest.class))).thenThrow(new RuntimeException("DB error"));

        // When
        service.processMessage(10L, "Hello", send, cancelFlag);

        // Then
        assertThat(sentResponses).hasSize(1);
        assertThat(sentResponses.get(0).getType()).isEqualTo("ERROR");
    }
}

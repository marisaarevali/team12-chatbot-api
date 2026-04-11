package com.example.bossbot.message.service;

import com.example.bossbot.common.ResourceNotFoundException;
import com.example.bossbot.message.dto.CreateMessageRequest;
import com.example.bossbot.message.dto.MessageResponse;
import com.example.bossbot.message.entity.Message;
import com.example.bossbot.message.entity.MessageRole;
import com.example.bossbot.message.repository.MessageRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.any;

@ExtendWith(MockitoExtension.class)
@DisplayName("MessageService Tests")
class MessageServiceImplTest {

    @Mock
    private MessageRepository repository;

    @InjectMocks
    private MessageServiceImpl service;

    private Message testEntity;
    private CreateMessageRequest createRequest;

    @BeforeEach
    void setUp() {
        testEntity = Message.builder()
                .id(1L)
                .conversationId(10L)
                .role(MessageRole.USER)
                .content("Hello, how can I get started?")
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .createdBy(1L)
                .build();

        createRequest = CreateMessageRequest.builder()
                .conversationId(10L)
                .role(MessageRole.USER)
                .content("Hello, how can I get started?")
                .build();
    }

    @Test
    @DisplayName("Should create message successfully")
    void testCreate_Success() {
        // Given
        when(repository.save(any(Message.class))).thenReturn(testEntity);

        // When
        MessageResponse response = service.create(createRequest);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getConversationId()).isEqualTo(10L);
        assertThat(response.getRole()).isEqualTo(MessageRole.USER);
        assertThat(response.getContent()).isEqualTo(testEntity.getContent());
        verify(repository).save(any(Message.class));
    }

    @Test
    @DisplayName("Should get message by ID successfully")
    void testGetById_Success() {
        // Given
        when(repository.findById(1L)).thenReturn(Optional.of(testEntity));

        // When
        MessageResponse response = service.getById(1L);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getContent()).isEqualTo(testEntity.getContent());
        verify(repository).findById(1L);
    }

    @Test
    @DisplayName("Should throw exception when message not found by ID")
    void testGetById_NotFound() {
        // Given
        when(repository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> service.getById(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("not found");
    }

    @Test
    @DisplayName("Should get all messages filtered by conversation ID")
    void testGetAll_WithConversationId() {
        // Given
        List<Message> entities = List.of(testEntity);
        when(repository.findByConversationIdAndIsActiveTrueOrderByCreatedAtAscIdAsc(10L)).thenReturn(entities);

        // When
        List<MessageResponse> responses = service.getAll(10L);

        // Then
        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).getConversationId()).isEqualTo(10L);
        verify(repository).findByConversationIdAndIsActiveTrueOrderByCreatedAtAscIdAsc(10L);
    }

    @Test
    @DisplayName("Should soft delete message successfully")
    void testDelete_Success() {
        // Given
        when(repository.findById(1L)).thenReturn(Optional.of(testEntity));
        when(repository.save(any(Message.class))).thenReturn(testEntity);

        // When
        service.delete(1L);

        // Then
        verify(repository).findById(1L);
        verify(repository).save(any(Message.class));
        assertThat(testEntity.getIsActive()).isFalse();
    }

    @Test
    @DisplayName("Should throw exception when deleting non-existent message")
    void testDelete_NotFound() {
        // Given
        when(repository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> service.delete(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("not found");
        verify(repository, never()).save(any());
    }
}

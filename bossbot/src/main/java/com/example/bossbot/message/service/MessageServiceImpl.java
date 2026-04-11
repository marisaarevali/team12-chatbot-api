package com.example.bossbot.message.service;

import com.example.bossbot.common.ResourceNotFoundException;
import com.example.bossbot.message.dto.CreateMessageRequest;
import com.example.bossbot.message.dto.MessageResponse;
import com.example.bossbot.message.entity.Message;
import com.example.bossbot.message.repository.MessageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class MessageServiceImpl implements MessageService {

    private static final String MESSAGE_NOT_FOUND = "Message not found with ID: ";

    private final MessageRepository repository;

    @Override
    @Transactional
    public MessageResponse create(CreateMessageRequest request) {
        log.info("Creating new message in conversation: {}", request.getConversationId());

        // TODO: Replace with authenticated user ID from Spring Security context
        Long currentUserId = 1L;

        Message entity = Message.builder()
                .conversationId(request.getConversationId())
                .role(request.getRole())
                .content(request.getContent())
                .isActive(true)
                .createdBy(currentUserId)
                .build();

        Message saved = repository.save(entity);
        log.info("Created message with ID: {}", saved.getId());

        return MessageResponse.fromEntity(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public MessageResponse getById(Long id) {
        log.info("Fetching message with ID: {}", id);

        Message entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(MESSAGE_NOT_FOUND + id));

        return MessageResponse.fromEntity(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MessageResponse> getAll(Long conversationId) {
        log.info("Fetching messages for conversation ID: {}", conversationId);
        return repository.findByConversationIdAndIsActiveTrueOrderByCreatedAtAscIdAsc(conversationId).stream()
                .map(MessageResponse::fromEntity)
                .toList();
    }

    @Override
    @Transactional
    public void delete(Long id) {
        log.info("Soft deleting message with ID: {}", id);

        Message entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(MESSAGE_NOT_FOUND + id));

        entity.setIsActive(false);
        repository.save(entity);

        log.info("Soft deleted message with ID: {}", id);
    }
}

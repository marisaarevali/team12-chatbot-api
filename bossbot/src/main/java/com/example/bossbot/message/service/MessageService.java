package com.example.bossbot.message.service;

import com.example.bossbot.message.dto.CreateMessageRequest;
import com.example.bossbot.message.dto.MessageResponse;

import java.util.List;

public interface MessageService {

    /**
     * Create a new message
     */
    MessageResponse create(CreateMessageRequest request);

    /**
     * Get a message by ID
     */
    MessageResponse getById(Long id);

    /**
     * Get all active messages for a conversation, ordered oldest first.
     */
    List<MessageResponse> getAll(Long conversationId);

    /**
     * Soft delete a message
     */
    void delete(Long id);
}

package com.example.bossbot.message.controller;

import com.example.bossbot.message.dto.CreateMessageRequest;
import com.example.bossbot.message.dto.MessageResponse;
import com.example.bossbot.message.service.MessageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/messages")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Messages", description = "Chat message operations")
public class MessageController {

    private final MessageService messageService;

    /**
     * Create a new message
     * POST /api/v1/messages
     */
    @Operation(summary = "Create a new message")
    @ApiResponse(responseCode = "201", description = "Message created successfully")
    @ApiResponse(responseCode = "400", description = "Invalid request body")
    @PostMapping
    public ResponseEntity<MessageResponse> create(@Valid @RequestBody CreateMessageRequest request) {
        log.info("REST request to create message in conversation: {}", request.getConversationId());
        MessageResponse response = messageService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Get a message by ID
     * GET /api/v1/messages/{id}
     */
    @Operation(summary = "Get a message by ID")
    @ApiResponse(responseCode = "200", description = "Message found")
    @ApiResponse(responseCode = "404", description = "Message not found")
    @GetMapping("/{id}")
    public ResponseEntity<MessageResponse> getById(@PathVariable Long id) {
        log.info("REST request to get message by ID: {}", id);
        MessageResponse response = messageService.getById(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Get all messages for a conversation
     * GET /api/v1/messages?conversationId={id}
     */
    @Operation(summary = "Get all messages for a conversation")
    @ApiResponse(responseCode = "200", description = "Messages returned")
    @ApiResponse(responseCode = "400", description = "conversationId query parameter is required")
    @GetMapping
    public ResponseEntity<List<MessageResponse>> getAll(
            @RequestParam Long conversationId) {
        log.info("REST request to get messages. Conversation ID: {}", conversationId);
        List<MessageResponse> responses = messageService.getAll(conversationId);
        return ResponseEntity.ok(responses);
    }

    /**
     * Soft delete a message
     * DELETE /api/v1/messages/{id}
     */
    @Operation(summary = "Soft delete a message")
    @ApiResponse(responseCode = "204", description = "Message deleted successfully")
    @ApiResponse(responseCode = "404", description = "Message not found")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        log.info("REST request to delete message with ID: {}", id);
        messageService.delete(id);
        return ResponseEntity.noContent().build();
    }

}

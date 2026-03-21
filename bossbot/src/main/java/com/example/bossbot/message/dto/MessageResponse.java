package com.example.bossbot.message.dto;

import com.example.bossbot.message.entity.Message;
import com.example.bossbot.message.entity.MessageRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageResponse {

    private Long id;
    private Long conversationId;
    private MessageRole role;
    private String content;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private Long createdBy;

    /**
     * Maps entity to response DTO
     */
    public static MessageResponse fromEntity(Message entity) {
        return MessageResponse.builder()
                .id(entity.getId())
                .conversationId(entity.getConversationId())
                .role(entity.getRole())
                .content(entity.getContent())
                .isActive(entity.getIsActive())
                .createdAt(entity.getCreatedAt())
                .createdBy(entity.getCreatedBy())
                .build();
    }
}

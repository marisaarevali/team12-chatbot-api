package com.example.bossbot.bannedword.dto;

import com.example.bossbot.bannedword.entity.BannedWord;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BannedWordResponse {

    private Long id;
    private String word;
    private String category;
    private String severity;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long createdBy;
    private Long updatedBy;

    public static BannedWordResponse fromEntity(BannedWord entity) {
        return BannedWordResponse.builder()
                .id(entity.getId())
                .word(entity.getWord())
                .category(entity.getCategory())
                .severity(entity.getSeverity())
                .isActive(entity.getIsActive())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .createdBy(entity.getCreatedBy())
                .updatedBy(entity.getUpdatedBy())
                .build();
    }
}
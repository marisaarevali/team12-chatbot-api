package com.example.bossbot.bannedword.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateBannedWordRequest {

    @NotBlank(message = "Word is required")
    private String word;

    private String category;

    private String severity;

    @Builder.Default
    private Boolean isActive = true;
}
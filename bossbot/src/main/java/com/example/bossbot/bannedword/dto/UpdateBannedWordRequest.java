package com.example.bossbot.bannedword.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateBannedWordRequest {

    private String word;

    private String category;

    private String severity;

    private Boolean isActive;
}
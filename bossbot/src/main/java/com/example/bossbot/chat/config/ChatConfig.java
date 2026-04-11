package com.example.bossbot.chat.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "chat")
public class ChatConfig {

    private int maxMessageLength;
    private int maxHistoryMessages;
    private String systemPrompt;

    // Rate limiting (progressive backoff)
    private int rateLimitBurstCount = 3;
    private long rateLimitBaseMs = 1000;
    private long rateLimitMaxMs = 16000;
    private long rateLimitResetMs = 30000;
}

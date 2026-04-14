package com.example.bossbot.ai.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "ollama")
public class OllamaConfig {

    private boolean enabled = false;
    private String url = "http://localhost:11434";
    private String model = "nomic-embed-text";
    private String chatModel = "llama3.2:3b";
}

package com.example.bossbot.stampanswer.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "stamp-matcher")
public class StampMatcherConfig {

    private double similarityThreshold = 0.7;
    private double keywordThreshold = 0.5;
    private int minInputLength = 3;
}

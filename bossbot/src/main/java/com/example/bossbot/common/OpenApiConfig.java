package com.example.bossbot.common;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI bossbotOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Bossbot API")
                        .description("Chatbot API for Bossbot")
                        .version("v1"));
    }
}

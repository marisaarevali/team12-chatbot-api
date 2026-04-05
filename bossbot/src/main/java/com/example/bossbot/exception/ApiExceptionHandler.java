package com.example.bossbot.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

/**
 * Global exception handler for handling exceptions in a REST API.
 * <p>
 * This class is annotated with {@code @RestControllerAdvice} to handle exceptions
 * globally across the application. It provides a consistent structure for handling
 * errors and returns appropriate HTTP responses with detailed error information.
 * <p>
 * - https://www.baeldung.com/spring-response-status-exception
 * - https://docs.spring.io/spring-framework/reference/web/webflux/controller/ann-advice.html
 * - https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/http/ProblemDetail.html
 * - https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/web/bind/annotation/RestControllerAdvice.html
 */
@RestControllerAdvice
public class ApiExceptionHandler {
    @ExceptionHandler(ResponseStatusException.class)
    public ProblemDetail handleResponseStatus(ResponseStatusException ex, HttpServletRequest request){
        ProblemDetail detail = ProblemDetail.forStatus(ex.getStatusCode());
        switch(ex.getStatusCode().value()){
            case 400 -> detail.setTitle("Invalid request body");
            case 401 -> detail.setTitle("Unauthorized");
            case 403 -> detail.setTitle("Forbidden");
            case 404 -> detail.setTitle("Not found");
            default -> detail.setTitle("Request failed");
        }

        detail.setDetail(ex.getReason() != null ? ex.getReason() : "Unexpected error occurred");
        detail.setProperty("path", request.getRequestURI());
        return detail;
    }
}

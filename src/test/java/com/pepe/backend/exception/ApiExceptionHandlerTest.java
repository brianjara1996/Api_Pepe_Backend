package com.pepe.backend.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ApiExceptionHandlerTest {

    private final ApiExceptionHandler handler = new ApiExceptionHandler();

    @Test
    void shouldReturn429ForQuotaException() {
        ResponseEntity<Map<String, Object>> response = handler.handleQuota(
                new AiQuotaExceededException("quota")
        );

        assertEquals(HttpStatus.TOO_MANY_REQUESTS, response.getStatusCode());
        assertEquals("AI quota exceeded", response.getBody().get("error"));
    }

    @Test
    void shouldReturn503ForServiceUnavailableException() {
        ResponseEntity<Map<String, Object>> response = handler.handleAiUnavailable(
                new AiServiceUnavailableException("down")
        );

        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, response.getStatusCode());
        assertEquals("AI service unavailable", response.getBody().get("error"));
    }
}

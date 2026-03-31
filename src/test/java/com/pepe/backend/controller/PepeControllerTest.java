package com.pepe.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pepe.backend.dto.TextProcessRequest;
import com.pepe.backend.service.PepeService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PepeController.class)
class PepeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PepeService pepeService;

    @Test
    void shouldReturnBadRequestWhenTextMissing() throws Exception {
        TextProcessRequest request = new TextProcessRequest();
        request.setText("");

        mockMvc.perform(post("/api/text/process")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("text is required"));
    }

    @Test
    void shouldReturnBadRequestWhenTextTooLong() throws Exception {
        TextProcessRequest request = new TextProcessRequest();
        request.setText("a".repeat(2001));

        mockMvc.perform(post("/api/text/process")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("text must be at most 2000 characters"));
    }
}

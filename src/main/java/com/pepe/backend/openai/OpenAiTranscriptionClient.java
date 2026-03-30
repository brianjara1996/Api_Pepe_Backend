package com.pepe.backend.openai;

import com.fasterxml.jackson.databind.JsonNode;
import com.pepe.backend.config.OpenAiProperties;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Component
public class OpenAiTranscriptionClient {

    private final RestClient restClient;
    private final OpenAiProperties properties;

    public OpenAiTranscriptionClient(RestClient restClient, OpenAiProperties properties) {
        this.restClient = restClient;
        this.properties = properties;
    }

    public String transcribe(MultipartFile audio) {
        try {
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("model", properties.getTranscriptionModel());
            body.add("language", "it");
            body.add("response_format", "json");
            body.add("file", new MultipartInputFile(audio.getOriginalFilename(), audio.getBytes(), audio.getContentType()));

            JsonNode response = restClient.post()
                    .uri(properties.getBaseUrl() + "/audio/transcriptions")
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .header("Authorization", "Bearer " + properties.getApiKey())
                    .body(body)
                    .retrieve()
                    .body(JsonNode.class);

            if (response == null || response.get("text") == null) {
                throw new IllegalStateException("Invalid transcription response");
            }
            return response.get("text").asText();
        } catch (IOException e) {
            throw new RuntimeException("Unable to read audio bytes", e);
        }
    }
}
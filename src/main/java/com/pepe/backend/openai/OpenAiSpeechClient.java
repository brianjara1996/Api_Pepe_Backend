package com.pepe.backend.openai;


import com.pepe.backend.config.OpenAiProperties;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Base64;
import java.util.Map;

@Component
public class OpenAiSpeechClient {

    private final RestClient restClient;
    private final OpenAiProperties properties;

    public OpenAiSpeechClient(RestClient restClient, OpenAiProperties properties) {
        this.restClient = restClient;
        this.properties = properties;
    }

    public String speakToBase64(String text) {
        byte[] audio = restClient.post()
                .uri(properties.getBaseUrl() + "/audio/speech")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + properties.getApiKey())
                .body(Map.of(
                        "model", properties.getTtsModel(),
                        "voice", properties.getTtsVoice(),
                        "input", text,
                        "format", "mp3"
                ))
                .retrieve()
                .body(byte[].class);

        if (audio == null || audio.length == 0) {
            throw new IllegalStateException("Empty TTS audio response");
        }
        return Base64.getEncoder().encodeToString(audio);
    }
}
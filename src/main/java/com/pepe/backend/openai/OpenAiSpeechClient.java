package com.pepe.backend.openai;


import com.pepe.backend.config.OpenAiProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Component
public class OpenAiSpeechClient {

    private static final Logger log = LoggerFactory.getLogger(OpenAiSpeechClient.class);
    private static final String DEFAULT_MALE_ELDERLY_STYLE =
            "Parla in italiano con voce chiaramente maschile, anziana, baritonale, lenta e rassicurante. Evita timbro femminile.";

    private final RestClient restClient;
    private final OpenAiProperties properties;

    public OpenAiSpeechClient(RestClient restClient, OpenAiProperties properties) {
        this.restClient = restClient;
        this.properties = properties;
    }

    public String speakToBase64(String text) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("model", properties.getTtsModel());
        payload.put("voice", properties.getTtsVoice());
        payload.put("input", text);
        payload.put("format", "mp3");

        String customStyle = properties.getTtsStyleInstructions();
        if (customStyle != null && !customStyle.isBlank()) {
            payload.put("instructions", DEFAULT_MALE_ELDERLY_STYLE + " " + customStyle);
        } else {
            payload.put("instructions", DEFAULT_MALE_ELDERLY_STYLE);
        }

        log.info("Generating TTS audio with voice '{}'", properties.getTtsVoice());

        byte[] audio = restClient.post()
                .uri(properties.getBaseUrl() + "/audio/speech")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + properties.getApiKey())
                .body(payload)
                .retrieve()
                .body(byte[].class);

        if (audio == null || audio.length == 0) {
            throw new IllegalStateException("Empty TTS audio response");
        }
        return Base64.getEncoder().encodeToString(audio);
    }
}

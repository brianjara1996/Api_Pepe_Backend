package com.pepe.backend.openai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pepe.backend.config.GeminiProperties;
import com.pepe.backend.exception.AiQuotaExceededException;
import com.pepe.backend.exception.AiServiceUnavailableException;
import com.pepe.backend.model.AiAction;
import com.pepe.backend.model.AiDecision;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.util.List;
import java.util.Map;

@Component
public class GeminiResponseClient {

    private static final Logger log = LoggerFactory.getLogger(GeminiResponseClient.class);
    private final RestClient restClient;
    private final GeminiProperties properties;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public GeminiResponseClient(RestClient restClient, GeminiProperties properties) {
        this.restClient = restClient;
        this.properties = properties;
    }

    public AiDecision askPepe(String userText,
                              String profileName,
                              String familyTarget,
                              String region,
                              boolean dialectEnabled) {

        try {
            String systemPrompt = buildInstructions(profileName, familyTarget, region, dialectEnabled);

            Map<String, Object> payload = Map.of(
                    "model", properties.getModel(),
                    "messages", List.of(
                            Map.of("role", "system", "content", systemPrompt),
                            Map.of("role", "user", "content", userText)
                    ),
                    "tools", List.of(
                            Map.of(
                                    "type", "function",
                                    "function", Map.of(
                                            "name", "send_whatsapp",
                                            "description", "Invia un messaggio WhatsApp a un familiare",
                                            "parameters", Map.of(
                                                    "type", "object",
                                                    "properties", Map.of(
                                                            "target", Map.of("type", "string"),
                                                            "message", Map.of("type", "string")
                                                    ),
                                                    "required", List.of("target", "message")
                                            )
                                    )
                            ),
                            Map.of(
                                    "type", "function",
                                    "function", Map.of(
                                            "name", "read_last_whatsapp_message",
                                            "description", "Legge l'ultimo messaggio WhatsApp ricevuto da un familiare",
                                            "parameters", Map.of(
                                                    "type", "object",
                                                    "properties", Map.of(
                                                            "target", Map.of("type", "string")
                                                    ),
                                                    "required", List.of("target")
                                            )
                                    )
                            )
                    ),
                    "tool_choice", "auto"
            );

            JsonNode response = restClient.post()
                    .uri(properties.getBaseUrl() + "/chat/completions")
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("Authorization", "Bearer " + properties.getApiKey())
                    .body(payload)
                    .retrieve()
                    .body(JsonNode.class);

            if (response == null || !response.path("choices").isArray() || response.path("choices").isEmpty()) {
                throw new AiServiceUnavailableException("Empty or invalid response from AI provider");
            }

            JsonNode message = response.path("choices").get(0).path("message");

            AiDecision decision = new AiDecision();

            JsonNode toolCalls = message.path("tool_calls");
            if (toolCalls.isArray() && !toolCalls.isEmpty()) {
                JsonNode firstTool = toolCalls.get(0);
                String functionName = firstTool.path("function").path("name").asText();
                String arguments = firstTool.path("function").path("arguments").asText();

                JsonNode args = objectMapper.readTree(arguments);

                AiAction action = new AiAction();
                action.setType(functionName);
                action.setTarget(args.path("target").asText(""));
                action.setMessage(args.path("message").asText(""));

                decision.setAction(action);

                if ("send_whatsapp".equals(functionName)) {
                    decision.setReplyText("Va bene, mando un messaggio a " + action.getTarget() + ".");
                } else if ("read_last_whatsapp_message".equals(functionName)) {
                    decision.setReplyText("Va bene, controllo l'ultimo messaggio di " + action.getTarget() + ".");
                } else {
                    decision.setReplyText("Va bene.");
                }
            } else {
                decision.setReplyText(message.path("content").asText("Ciao, sono Pepe."));
                AiAction action = new AiAction();
                action.setType("conversation");
                action.setTarget("");
                action.setMessage("");
                decision.setAction(action);
            }

            return decision;

        } catch (RestClientResponseException e) {
            if (e.getStatusCode().value() == 429) {
                throw new AiQuotaExceededException("AI provider quota exceeded", e);
            }
            if (e.getStatusCode().is5xxServerError()) {
                throw new AiServiceUnavailableException("AI provider is unavailable", e);
            }
            throw new AiServiceUnavailableException("AI request failed with status " + e.getStatusCode().value(), e);
        } catch (AiQuotaExceededException | AiServiceUnavailableException e) {
            throw e;
        } catch (Exception e) {
            log.error("Gemini response processing failed", e);
            throw new AiServiceUnavailableException("Unexpected AI processing error", e);
        }
    }

    private String buildInstructions(String profileName, String familyTarget, String region, boolean dialectEnabled) {
        return """
                Sei Pepe, un assistente vocale per persone anziane in Italia.

                Regole:
                - parla sempre in italiano semplice
                - usa frasi brevi e calde
                - non usare linguaggio tecnico
                - sii paziente, gentile e rassicurante
                - se l'utente chiede di mandare un messaggio, usa la funzione send_whatsapp
                - se l'utente chiede di leggere l'ultimo messaggio, usa la funzione read_last_whatsapp_message
                - se non serve alcuna azione, rispondi normalmente senza funzioni
                - nome utente: %s
                - familiare principale: %s
                - regione: %s
                - dialetto abilitato: %s
                """.formatted(profileName, familyTarget, region, dialectEnabled);
    }
}

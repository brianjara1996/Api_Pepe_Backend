package com.pepe.backend.openai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pepe.backend.config.OpenAiProperties;
import com.pepe.backend.model.AiAction;
import com.pepe.backend.model.AiDecision;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class OpenAiResponseClient {

    private final RestClient restClient;
    private final OpenAiProperties properties;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public OpenAiResponseClient(RestClient restClient, OpenAiProperties properties) {
        this.restClient = restClient;
        this.properties = properties;
    }

    public AiDecision askPepe(String userText,
                              String profileName,
                              String familyTarget,
                              String region,
                              boolean dialectEnabled) {
        try {
            String instructions = buildInstructions(profileName, familyTarget, region, dialectEnabled);

            Map<String, Object> textSchema = new HashMap<>();
            textSchema.put("type", "object");
            textSchema.put("additionalProperties", false);
            textSchema.put("properties", Map.of(
                    "replyText", Map.of("type", "string"),
                    "action", Map.of(
                            "type", "object",
                            "additionalProperties", false,
                            "properties", Map.of(
                                    "type", Map.of("type", "string"),
                                    "target", Map.of("type", "string"),
                                    "message", Map.of("type", "string")
                            ),
                            "required", List.of("type", "target", "message")
                    )
            ));
            textSchema.put("required", List.of("replyText", "action"));

            Map<String, Object> payload = new HashMap<>();
            payload.put("model", properties.getResponseModel());
            payload.put("input", List.of(
                    Map.of(
                            "role", "system",
                            "content", List.of(Map.of("type", "input_text", "text", instructions))
                    ),
                    Map.of(
                            "role", "user",
                            "content", List.of(Map.of("type", "input_text", "text", userText))
                    )
            ));
            payload.put("tools", List.of(
                    Map.of("type", "web_search")
            ));
            payload.put("text", Map.of(
                    "format", Map.of(
                            "type", "json_schema",
                            "name", "pepe_response",
                            "schema", textSchema,
                            "strict", true
                    )
            ));

            JsonNode response = restClient.post()
                    .uri(properties.getBaseUrl() + "/responses")
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("Authorization", "Bearer " + properties.getApiKey())
                    .body(payload)
                    .retrieve()
                    .body(JsonNode.class);

            String jsonText = extractStructuredText(response);
            JsonNode parsed = objectMapper.readTree(jsonText);

            AiDecision decision = new AiDecision();
            decision.setReplyText(parsed.path("replyText").asText());

            JsonNode actionNode = parsed.path("action");
            if (!actionNode.isMissingNode() && !actionNode.isNull()) {
                AiAction action = new AiAction();
                action.setType(actionNode.path("type").asText("conversation"));
                action.setTarget(actionNode.path("target").asText(""));
                action.setMessage(actionNode.path("message").asText(""));
                decision.setAction(action);
            }

            return decision;
        } catch (Exception e) {
            throw new RuntimeException("OpenAI response processing failed", e);
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
                - usa web search solo se la domanda richiede dati attuali o recenti (partite, meteo, notizie, prezzi, orari, borsa)
                - se la domanda non richiede dati attuali, non usare internet e rispondi direttamente
                - non inventare mai informazioni attuali: se non trovi dati affidabili, dillo chiaramente
                - se l'utente chiede di mandare un messaggio, imposta action.type = send_whatsapp
                - se l'utente chiede di leggere l'ultimo messaggio, imposta action.type = read_last_whatsapp_message
                - se non serve alcuna azione, imposta action.type = conversation
                - non inventare mai che un messaggio è stato inviato davvero: limita la risposta a "va bene" o simile
                - target predefinito: %s
				- nome utente: %s
                - regione: %s
                - dialetto abilitato: %s

                Se action.type = conversation:
                - metti target = ""
                - metti message = ""

                Se action.type = send_whatsapp:
                - compila target e message

                Se action.type = read_last_whatsapp_message:
                - compila target
                - metti message = ""
                """.formatted(familyTarget, profileName, region, dialectEnabled);
    }

    private String extractStructuredText(JsonNode response) {
        JsonNode output = response.path("output");
        if (output.isArray()) {
            for (JsonNode item : output) {
                JsonNode content = item.path("content");
                if (content.isArray()) {
                    for (JsonNode c : content) {
                        if (c.has("text")) {
                            return c.get("text").asText();
                        }
                    }
                }
            }
        }
        JsonNode outputText = response.path("output_text");
        if (!outputText.isMissingNode() && !outputText.isNull() && !outputText.asText().isBlank()) {
            return outputText.asText();
        }

        throw new IllegalStateException("No structured text found in OpenAI response");
    }
}

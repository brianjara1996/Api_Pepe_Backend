package com.pepe.backend.service;

import com.pepe.backend.config.PepeAppProperties;
import com.pepe.backend.dto.PepeActionDto;
import com.pepe.backend.dto.PepeResponseDto;
import com.pepe.backend.dto.TextProcessRequest;
import com.pepe.backend.model.AiAction;
import com.pepe.backend.model.AiDecision;
import com.pepe.backend.openai.GeminiResponseClient;
import com.pepe.backend.whatsapp.WhatsappService;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class PepeService {

    private final GeminiResponseClient responseClient;
    private final WhatsappService whatsappService;
    private final PepeAppProperties appProperties;

    public PepeService(GeminiResponseClient responseClient,
                       WhatsappService whatsappService,
                       PepeAppProperties appProperties) {
        this.responseClient = responseClient;
        this.whatsappService = whatsappService;
        this.appProperties = appProperties;
    }

    public PepeResponseDto processText(TextProcessRequest request) {
        String profileName = fallback(request.getProfileName(), appProperties.getDefaultProfileName());
        String familyTarget = fallback(request.getFamilyTarget(), appProperties.getDefaultFamilyTarget());

        AiDecision decision = responseClient.askPepe(
                request.getText(),
                profileName,
                familyTarget,
                appProperties.getRegion(),
                appProperties.isDialectEnabled()
        );

        PepeActionDto actionDto = executeAction(decision.getAction());

        PepeResponseDto dto = new PepeResponseDto();
        dto.setTranscript(request.getText());
        dto.setReplyText(decision.getReplyText());
        dto.setAction(actionDto);
        dto.setAudioBase64(null); // por ahora desactivado
        return dto;
    }

    public PepeResponseDto processVoice(MultipartFile audio, String profileName, String familyTarget) {
        throw new UnsupportedOperationException("Per ora prova solo /api/text/process con Gemini.");
    }

    private PepeActionDto executeAction(AiAction action) {
        if (action == null || action.getType() == null || action.getType().isBlank()) {
            return null;
        }

        PepeActionDto dto = new PepeActionDto();
        dto.setType(action.getType());
        dto.setTarget(action.getTarget());
        dto.setMessage(action.getMessage());

        switch (action.getType()) {
            case "send_whatsapp" -> {
                whatsappService.sendMessage(action.getTarget(), action.getMessage());
                dto.setSuccess(true);
            }
            case "read_last_whatsapp_message" -> {
                String lastMessage = whatsappService.readLastMessage(action.getTarget());
                dto.setMessage(lastMessage);
                dto.setSuccess(true);
            }
            case "conversation" -> dto.setSuccess(true);
            default -> dto.setSuccess(false);
        }

        return dto;
    }

    private String fallback(String value, String fallback) {
        return (value == null || value.isBlank()) ? fallback : value;
    }
}
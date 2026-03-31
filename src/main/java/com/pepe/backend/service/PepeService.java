package com.pepe.backend.service;

import com.pepe.backend.config.PepeAppProperties;
import com.pepe.backend.dto.PepeActionDto;
import com.pepe.backend.dto.PepeResponseDto;
import com.pepe.backend.dto.TextProcessRequest;
import com.pepe.backend.model.AiAction;
import com.pepe.backend.model.AiDecision;
import com.pepe.backend.openai.OpenAiResponseClient;
import com.pepe.backend.openai.OpenAiSpeechClient;
import com.pepe.backend.whatsapp.WhatsappService;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Service
public class PepeService {
    private static final String WEB_SEARCH_WAITING_MESSAGE = "Sto cercando su internet, ma ci sta mettendo troppo. Riprova tra qualche secondo.";
    private static final long STREAM_EMITTER_TIMEOUT_MS = 120_000L;
    private static final long WAITING_MESSAGE_INTERVAL_SECONDS = 5L;

    private final OpenAiResponseClient responseClient;
    private final OpenAiSpeechClient speechClient;
    private final WhatsappService whatsappService;
    private final PepeAppProperties appProperties;

    public PepeService(OpenAiResponseClient responseClient,
                       OpenAiSpeechClient speechClient,
                       WhatsappService whatsappService,
                       PepeAppProperties appProperties) {
        this.responseClient = responseClient;
        this.speechClient = speechClient;
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
        dto.setAudioBase64(speechClient.speakToBase64(decision.getReplyText()));
        return dto;
    }

    public SseEmitter processTextStream(TextProcessRequest request) {
        SseEmitter emitter = new SseEmitter(STREAM_EMITTER_TIMEOUT_MS);

        CompletableFuture.runAsync(() -> {
            try {
                PepeResponseDto finalResponse = processTextWithProgress(request, emitter);
                emitter.send(SseEmitter.event()
                        .name("final")
                        .data(finalResponse));
                emitter.complete();
            } catch (Exception e) {
                emitter.completeWithError(e);
            }
        });

        return emitter;
    }

    public PepeResponseDto processVoice(MultipartFile audio, String profileName, String familyTarget) {
        throw new UnsupportedOperationException("Per ora prova solo /api/text/process con OpenAI.");
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

    private PepeResponseDto processTextWithProgress(TextProcessRequest request, SseEmitter emitter)
            throws IOException, InterruptedException, ExecutionException {

        String profileName = fallback(request.getProfileName(), appProperties.getDefaultProfileName());
        String familyTarget = fallback(request.getFamilyTarget(), appProperties.getDefaultFamilyTarget());

        CompletableFuture<AiDecision> decisionFuture = CompletableFuture.supplyAsync(() ->
                responseClient.askPepe(
                        request.getText(),
                        profileName,
                        familyTarget,
                        appProperties.getRegion(),
                        appProperties.isDialectEnabled()
                )
        );

        boolean needsWebLookup = needsWebLookup(request.getText());
        AiDecision decision;

        while (true) {
            try {
                decision = decisionFuture.get(WAITING_MESSAGE_INTERVAL_SECONDS, TimeUnit.SECONDS);
                break;
            } catch (TimeoutException timeoutException) {
                if (needsWebLookup) {
                    emitter.send(SseEmitter.event()
                            .name("progress")
                            .data(Map.of(
                                    "type", "progress",
                                    "replyText", WEB_SEARCH_WAITING_MESSAGE
                            )));
                }
            }
        }

        PepeActionDto actionDto = executeAction(decision.getAction());

        PepeResponseDto dto = new PepeResponseDto();
        dto.setTranscript(request.getText());
        dto.setReplyText(decision.getReplyText());
        dto.setAction(actionDto);
        dto.setAudioBase64(speechClient.speakToBase64(decision.getReplyText()));
        return dto;
    }

    private boolean needsWebLookup(String text) {
        if (text == null || text.isBlank()) {
            return false;
        }

        String normalized = text.toLowerCase(Locale.ROOT);
        return normalized.contains("partit")
                || normalized.contains("notiz")
                || normalized.contains("news")
                || normalized.contains("clima")
                || normalized.contains("meteo")
                || normalized.contains("prezz")
                || normalized.contains("borsa")
                || normalized.contains("orar")
                || normalized.contains("treno")
                || normalized.contains("volo");
    }
}

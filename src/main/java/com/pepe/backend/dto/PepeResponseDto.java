package com.pepe.backend.dto;

public class PepeResponseDto {
    private String transcript;
    private String replyText;
    private PepeActionDto action;
    private String audioBase64;

    public String getTranscript() {
        return transcript;
    }

    public void setTranscript(String transcript) {
        this.transcript = transcript;
    }

    public String getReplyText() {
        return replyText;
    }

    public void setReplyText(String replyText) {
        this.replyText = replyText;
    }

    public PepeActionDto getAction() {
        return action;
    }

    public void setAction(PepeActionDto action) {
        this.action = action;
    }

    public String getAudioBase64() {
        return audioBase64;
    }

    public void setAudioBase64(String audioBase64) {
        this.audioBase64 = audioBase64;
    }
}


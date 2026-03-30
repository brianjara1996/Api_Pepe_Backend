package com.pepe.backend.model;

public class AiDecision {
    private String replyText;
    private AiAction action;

    public String getReplyText() {
        return replyText;
    }

    public void setReplyText(String replyText) {
        this.replyText = replyText;
    }

    public AiAction getAction() {
        return action;
    }

    public void setAction(AiAction action) {
        this.action = action;
    }
}

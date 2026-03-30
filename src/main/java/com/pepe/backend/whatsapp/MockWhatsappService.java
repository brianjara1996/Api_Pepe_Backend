package com.pepe.backend.whatsapp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class MockWhatsappService implements WhatsappService {

    private static final Logger log = LoggerFactory.getLogger(MockWhatsappService.class);

    @Override
    public void sendMessage(String target, String message) {
        log.info("[MOCK WHATSAPP] Sending message to {} -> {}", target, message);
    }

    @Override
    public String readLastMessage(String target) {
        log.info("[MOCK WHATSAPP] Reading last message from {}", target);
        return "Ciao papà, tutto bene? Ti chiamo stasera.";
    }
}


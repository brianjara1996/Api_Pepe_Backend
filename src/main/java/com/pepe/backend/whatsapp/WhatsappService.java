package com.pepe.backend.whatsapp;

public interface WhatsappService {

    void sendMessage(String target, String message);

    String readLastMessage(String target);
}
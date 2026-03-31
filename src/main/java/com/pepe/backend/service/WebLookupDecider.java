package com.pepe.backend.service;

import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
public class WebLookupDecider {

    public boolean shouldUseWebSearch(String text) {
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

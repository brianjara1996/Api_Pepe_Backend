package com.pepe.backend.controller;

import com.pepe.backend.dto.PepeResponseDto;
import com.pepe.backend.dto.TextProcessRequest;
import com.pepe.backend.service.PepeService;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class PepeController {

    private final PepeService pepeService;

    public PepeController(PepeService pepeService) {
        this.pepeService = pepeService;
    }

    @PostMapping(value = "/text/process", consumes = MediaType.APPLICATION_JSON_VALUE)
    public PepeResponseDto processText(@Valid @RequestBody TextProcessRequest request) {
        return pepeService.processText(request);
    }

    @PostMapping(value = "/voice/process", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public PepeResponseDto processVoice(
            @RequestPart("audio") MultipartFile audio,
            @RequestPart(value = "profileName", required = false) String profileName,
            @RequestPart(value = "familyTarget", required = false) String familyTarget) {
        return pepeService.processVoice(audio, profileName, familyTarget);
    }
}


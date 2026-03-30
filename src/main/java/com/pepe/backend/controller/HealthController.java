package com.pepe.backend.controller;


import com.pepe.backend.dto.HealthDto;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class HealthController {

    @GetMapping("/health")
    public HealthDto health() {
        return new HealthDto("UP", "pepe-backend");
    }
}


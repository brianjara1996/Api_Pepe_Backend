package com.pepe.backend.dto;

public class HealthDto {
    private String status;
    private String app;

    public HealthDto() {
    }

    public HealthDto(String status, String app) {
        this.status = status;
        this.app = app;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getApp() {
        return app;
    }

    public void setApp(String app) {
        this.app = app;
    }
}


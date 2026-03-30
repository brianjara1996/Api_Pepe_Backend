package com.pepe.backend.config;


import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "pepe.app")
public class PepeAppProperties {
    private String defaultProfileName;
    private String defaultFamilyTarget;
    private String region;
    private boolean dialectEnabled;
    private boolean mockWhatsapp;

    public String getDefaultProfileName() {
        return defaultProfileName;
    }

    public void setDefaultProfileName(String defaultProfileName) {
        this.defaultProfileName = defaultProfileName;
    }

    public String getDefaultFamilyTarget() {
        return defaultFamilyTarget;
    }

    public void setDefaultFamilyTarget(String defaultFamilyTarget) {
        this.defaultFamilyTarget = defaultFamilyTarget;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public boolean isDialectEnabled() {
        return dialectEnabled;
    }

    public void setDialectEnabled(boolean dialectEnabled) {
        this.dialectEnabled = dialectEnabled;
    }

    public boolean isMockWhatsapp() {
        return mockWhatsapp;
    }

    public void setMockWhatsapp(boolean mockWhatsapp) {
        this.mockWhatsapp = mockWhatsapp;
    }
}

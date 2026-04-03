package com.pepe.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class TextProcessRequest {
    @NotBlank(message = "text is required")
    @Size(max = 2000, message = "text must be at most 2000 characters")
    private String text;
    private String profileName;
    private String familyTarget;

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getProfileName() {
        return profileName;
    }

    public void setProfileName(String profileName) {
        this.profileName = profileName;
    }

    public String getFamilyTarget() {
        return familyTarget;
    }

    public void setFamilyTarget(String familyTarget) {
        this.familyTarget = familyTarget;
    }
}

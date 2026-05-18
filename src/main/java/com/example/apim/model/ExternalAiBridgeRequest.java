package com.example.apim.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class ExternalAiBridgeRequest {

    @NotBlank
    @Size(max = 10000)
    private String freeText = "";

    public String getFreeText() {
        return freeText;
    }

    public void setFreeText(String freeText) {
        this.freeText = freeText == null ? "" : freeText;
    }
}

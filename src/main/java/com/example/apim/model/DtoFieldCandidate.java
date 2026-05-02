package com.example.apim.model;

public record DtoFieldCandidate(
        String name,
        String javaType,
        boolean required,
        String validationHint,
        boolean sensitive
) {
}

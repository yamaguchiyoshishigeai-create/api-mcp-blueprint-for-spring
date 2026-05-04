package com.example.apim.model;

import java.util.ArrayList;
import java.util.List;

public record NormalizedBlueprintInput(
        List<String> systemTypes,
        String primaryDomain,
        List<String> relatedDomains,
        List<String> allDomains,
        String targetDomainText,
        BlueprintInput originalInput
) {
    public NormalizedBlueprintInput {
        systemTypes = immutableSafeList(systemTypes);
        primaryDomain = valueOrEmpty(primaryDomain);
        relatedDomains = immutableSafeList(relatedDomains);
        allDomains = immutableSafeList(allDomains);
        targetDomainText = valueOrEmpty(targetDomainText);
        originalInput = originalInput == null ? new BlueprintInput() : originalInput;
    }

    private static String valueOrEmpty(String value) {
        return value == null ? "" : value;
    }

    private static List<String> immutableSafeList(List<String> values) {
        if (values == null || values.isEmpty()) {
            return List.of();
        }
        List<String> safeValues = new ArrayList<>(values.size());
        for (String value : values) {
            safeValues.add(valueOrEmpty(value));
        }
        return List.copyOf(safeValues);
    }
}

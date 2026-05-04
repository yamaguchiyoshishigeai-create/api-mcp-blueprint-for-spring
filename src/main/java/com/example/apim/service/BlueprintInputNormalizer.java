package com.example.apim.service;

import com.example.apim.model.BlueprintInput;
import com.example.apim.model.NormalizedBlueprintInput;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

@Service
public class BlueprintInputNormalizer {

    public NormalizedBlueprintInput normalize(BlueprintInput input) {
        BlueprintInput safeInput = input == null ? new BlueprintInput() : input;

        List<String> normalizedSystemTypes = normalizeDistinct(safeInput.getSystemTypes());
        List<String> legacyDomains = splitAndNormalizeDomains(safeInput.getTargetDomain());
        List<String> explicitRelatedDomains = normalizeDistinct(safeInput.getRelatedDomains());

        String primaryDomain = firstNonBlank(
                normalizeText(safeInput.getPrimaryDomain()),
                legacyDomains.isEmpty() ? "" : legacyDomains.get(0)
        );

        List<String> baseRelatedDomains = !explicitRelatedDomains.isEmpty()
                ? explicitRelatedDomains
                : tailDomains(legacyDomains);

        List<String> relatedDomains = excludePrimaryDomain(baseRelatedDomains, primaryDomain);
        List<String> allDomains = mergeDomains(primaryDomain, relatedDomains);
        String targetDomainText = String.join(" / ", allDomains);

        return new NormalizedBlueprintInput(
                normalizedSystemTypes,
                primaryDomain,
                relatedDomains,
                allDomains,
                targetDomainText,
                safeInput
        );
    }

    private List<String> splitAndNormalizeDomains(String targetDomain) {
        String safeTargetDomain = normalizeText(targetDomain);
        if (safeTargetDomain.isEmpty()) {
            return List.of();
        }
        String[] splitDomains = safeTargetDomain.split("/");
        List<String> candidates = new ArrayList<>(splitDomains.length);
        for (String splitDomain : splitDomains) {
            candidates.add(splitDomain);
        }
        return normalizeDistinct(candidates);
    }

    private List<String> normalizeDistinct(List<String> values) {
        if (values == null || values.isEmpty()) {
            return List.of();
        }
        LinkedHashSet<String> distinctValues = new LinkedHashSet<>();
        for (String value : values) {
            String normalized = normalizeText(value);
            if (!normalized.isEmpty()) {
                distinctValues.add(normalized);
            }
        }
        return List.copyOf(distinctValues);
    }

    private List<String> tailDomains(List<String> values) {
        if (values.size() <= 1) {
            return List.of();
        }
        return values.subList(1, values.size());
    }

    private List<String> excludePrimaryDomain(List<String> relatedDomains, String primaryDomain) {
        if (relatedDomains.isEmpty()) {
            return List.of();
        }
        List<String> filtered = new ArrayList<>(relatedDomains.size());
        for (String relatedDomain : relatedDomains) {
            if (!relatedDomain.equals(primaryDomain)) {
                filtered.add(relatedDomain);
            }
        }
        return List.copyOf(filtered);
    }

    private List<String> mergeDomains(String primaryDomain, List<String> relatedDomains) {
        LinkedHashSet<String> merged = new LinkedHashSet<>();
        if (!primaryDomain.isEmpty()) {
            merged.add(primaryDomain);
        }
        merged.addAll(relatedDomains);
        return List.copyOf(merged);
    }

    private String firstNonBlank(String first, String second) {
        return first.isEmpty() ? second : first;
    }

    private String normalizeText(String value) {
        return value == null ? "" : value.trim();
    }
}

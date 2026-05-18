package com.example.apim.model;

import java.util.ArrayList;
import java.util.List;

public record ExternalAiImportResult(
        boolean valid,
        boolean canGenerate,
        String reason,
        List<String> missingInformation,
        BlueprintInput blueprintInput,
        List<String> errors,
        List<String> warnings
) {
    public ExternalAiImportResult {
        reason = reason == null ? "" : reason;
        missingInformation = safeList(missingInformation);
        errors = safeList(errors);
        warnings = safeList(warnings);
    }

    public static ExternalAiImportResult invalid(List<String> errors, List<String> warnings) {
        return new ExternalAiImportResult(false, false, "", List.of(), null, errors, warnings);
    }

    public static ExternalAiImportResult cannotGenerate(String reason, List<String> missingInformation,
                                                        List<String> warnings) {
        return new ExternalAiImportResult(true, false, reason, missingInformation, null, List.of(), warnings);
    }

    public static ExternalAiImportResult canGenerate(BlueprintInput input, String reason, List<String> warnings) {
        return new ExternalAiImportResult(true, true, reason, List.of(), input, List.of(), warnings);
    }

    private static List<String> safeList(List<String> values) {
        if (values == null || values.isEmpty()) {
            return List.of();
        }
        return List.copyOf(new ArrayList<>(values));
    }
}

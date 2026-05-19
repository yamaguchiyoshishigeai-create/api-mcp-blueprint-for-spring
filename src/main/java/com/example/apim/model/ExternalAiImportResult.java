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
        List<String> warnings,
        ExtractionSummary extractionSummary
) {
    public ExternalAiImportResult {
        reason = reason == null ? "" : reason;
        missingInformation = safeList(missingInformation);
        errors = safeList(errors);
        warnings = safeList(warnings);
        extractionSummary = extractionSummary == null ? ExtractionSummary.empty() : extractionSummary;
    }

    public static ExternalAiImportResult invalid(List<String> errors, List<String> warnings) {
        return new ExternalAiImportResult(false, false, "", List.of(), null, errors, warnings,
                ExtractionSummary.empty());
    }

    public static ExternalAiImportResult cannotGenerate(String reason, List<String> missingInformation,
                                                        List<String> warnings) {
        return new ExternalAiImportResult(true, false, reason, missingInformation, null, List.of(), warnings,
                ExtractionSummary.empty());
    }

    public static ExternalAiImportResult canGenerate(BlueprintInput input, String reason, List<String> warnings) {
        return canGenerate(input, reason, warnings, ExtractionSummary.empty());
    }

    public static ExternalAiImportResult canGenerate(BlueprintInput input, String reason, List<String> warnings,
                                                     ExtractionSummary extractionSummary) {
        return new ExternalAiImportResult(true, true, reason, List.of(), input, List.of(), warnings,
                extractionSummary);
    }

    private static List<String> safeList(List<String> values) {
        if (values == null || values.isEmpty()) {
            return List.of();
        }
        return List.copyOf(new ArrayList<>(values));
    }

    public record ExtractionSummary(
            List<String> domains,
            List<String> businessObjects,
            List<String> actors,
            List<String> operations,
            List<String> relationships,
            List<String> approvalRequiredOperations,
            List<String> auditLogRequiredOperations,
            List<String> ambiguities
    ) {
        public ExtractionSummary {
            domains = safeList(domains);
            businessObjects = safeList(businessObjects);
            actors = safeList(actors);
            operations = safeList(operations);
            relationships = safeList(relationships);
            approvalRequiredOperations = safeList(approvalRequiredOperations);
            auditLogRequiredOperations = safeList(auditLogRequiredOperations);
            ambiguities = safeList(ambiguities);
        }

        public static ExtractionSummary empty() {
            return new ExtractionSummary(List.of(), List.of(), List.of(), List.of(), List.of(), List.of(),
                    List.of(), List.of());
        }

        public boolean hasContent() {
            return !domains.isEmpty()
                    || !businessObjects.isEmpty()
                    || !actors.isEmpty()
                    || !operations.isEmpty()
                    || !relationships.isEmpty()
                    || !approvalRequiredOperations.isEmpty()
                    || !auditLogRequiredOperations.isEmpty()
                    || !ambiguities.isEmpty();
        }
    }
}

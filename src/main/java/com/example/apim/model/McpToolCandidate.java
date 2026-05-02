package com.example.apim.model;

public record McpToolCandidate(
        String name,
        String purpose,
        String arguments,
        String returnValue,
        String relatedApi,
        String operationType,
        String aiExecutionPolicy,
        String approvalRequired,
        String auditLogRequired
) {
}

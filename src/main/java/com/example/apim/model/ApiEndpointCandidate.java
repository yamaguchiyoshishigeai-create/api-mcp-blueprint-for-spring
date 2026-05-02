package com.example.apim.model;

public record ApiEndpointCandidate(
        String httpMethod,
        String path,
        String purpose,
        String actors,
        String requestDto,
        String responseDto,
        String authorization,
        String approvalRequired,
        String auditLogRequired
) {
}

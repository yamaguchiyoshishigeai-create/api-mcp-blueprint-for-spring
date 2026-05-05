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
        String auditLogRequired,
        String domainRole,
        String domainName
) {
    public ApiEndpointCandidate(
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
        this(httpMethod, path, purpose, actors, requestDto, responseDto, authorization, approvalRequired, auditLogRequired, "", "");
    }

    public ApiEndpointCandidate {
        domainRole = valueOrEmpty(domainRole);
        domainName = valueOrEmpty(domainName);
    }

    private static String valueOrEmpty(String value) {
        return value == null ? "" : value;
    }
}

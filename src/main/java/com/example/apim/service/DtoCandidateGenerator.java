package com.example.apim.service;

import com.example.apim.model.ApiEndpointCandidate;
import com.example.apim.model.DtoCandidate;
import com.example.apim.model.DtoFieldCandidate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class DtoCandidateGenerator {

    private static final String RELATED_DOMAIN_REFERENCE_API = "関連ドメイン参照API";

    public List<DtoCandidate> generate(String domainClass, List<ApiEndpointCandidate> endpoints) {
        Map<String, DtoCandidate> dtos = new LinkedHashMap<>();
        for (ApiEndpointCandidate endpoint : endpoints) {
            if (RELATED_DOMAIN_REFERENCE_API.equals(endpoint.domainRole())) {
                addRelatedDomainReferenceDtos(dtos, endpoint);
                continue;
            }
            switch (endpoint.httpMethod()) {
                case "GET" -> {
                    if (endpoint.path().endsWith("}")) {
                        addIfMissing(dtos, responseDto(endpoint, domainClass + "Response"), "詳細取得レスポンス",
                                defaultResponseFields());
                    } else {
                        addIfMissing(dtos, requestDto(endpoint, domainClass + "SearchRequest"), "検索条件",
                                defaultSearchFields());
                        addIfMissing(dtos, responseDto(endpoint, domainClass + "SummaryResponse"), "一覧レスポンス",
                                defaultSummaryFields());
                    }
                }
                case "POST" -> {
                    if (endpoint.path().contains("approval-requests")) {
                        addIfMissing(dtos, requestDto(endpoint, domainClass + "ApprovalRequest"), "承認依頼DTO",
                                defaultApprovalFields());
                        addIfMissing(dtos, responseDto(endpoint, "ApprovalResponse"), "承認結果DTO",
                                approvalResponseFields());
                    } else if (endpoint.path().contains("notifications")) {
                        addIfMissing(dtos, requestDto(endpoint, "NotificationRequest"), "通知要求DTO",
                                notificationFields());
                    } else if (endpoint.path().contains("permissions")) {
                        addIfMissing(dtos, requestDto(endpoint, "PermissionChangeRequest"), "権限変更DTO",
                                permissionFields());
                    } else {
                        addIfMissing(dtos, requestDto(endpoint, domainClass + "CreateRequest"), "作成要求DTO",
                                defaultCreateFields());
                        addIfMissing(dtos, responseDto(endpoint, ""), "作成レスポンス", defaultResponseFields());
                    }
                }
                case "PUT" -> addIfMissing(dtos, requestDto(endpoint, domainClass + "UpdateRequest"), "更新要求DTO",
                        defaultUpdateFields());
                case "DELETE" -> addIfMissing(dtos, responseDto(endpoint, "ApprovalResponse"), "承認結果DTO",
                        approvalResponseFields());
                default -> {
                }
            }
        }
        return new ArrayList<>(dtos.values());
    }

    private void addRelatedDomainReferenceDtos(Map<String, DtoCandidate> dtos, ApiEndpointCandidate endpoint) {
        String domainLabel = endpoint.domainName().isBlank() ? "関連ドメイン" : endpoint.domainName();
        if (endpoint.requestDto() != null && !endpoint.requestDto().isBlank()) {
            addIfMissing(dtos, endpoint.requestDto(), domainLabel + "参照検索条件", defaultSearchFields());
        }
        if (endpoint.responseDto() == null || endpoint.responseDto().isBlank()) {
            return;
        }
        if (endpoint.path().endsWith("}")) {
            addIfMissing(dtos, endpoint.responseDto(), domainLabel + "参照詳細レスポンス", defaultResponseFields());
        } else {
            addIfMissing(dtos, endpoint.responseDto(), domainLabel + "参照一覧レスポンス", defaultSummaryFields());
        }
    }

    private void addIfMissing(Map<String, DtoCandidate> dtos, String name, String purpose, List<DtoFieldCandidate> fields) {
        if (name == null || name.isBlank() || dtos.containsKey(name)) {
            return;
        }
        dtos.put(name, new DtoCandidate(name, purpose, fields));
    }

    private String requestDto(ApiEndpointCandidate endpoint, String fallback) {
        return endpoint.requestDto() == null || endpoint.requestDto().isBlank() ? fallback : endpoint.requestDto();
    }

    private String responseDto(ApiEndpointCandidate endpoint, String fallback) {
        return endpoint.responseDto() == null || endpoint.responseDto().isBlank() ? fallback : endpoint.responseDto();
    }

    private List<DtoFieldCandidate> defaultSearchFields() {
        return List.of(
                new DtoFieldCandidate("keyword", "String", false, "max=100", false),
                new DtoFieldCandidate("status", "String", false, "enum", false)
        );
    }

    private List<DtoFieldCandidate> defaultSummaryFields() {
        return List.of(
                new DtoFieldCandidate("id", "Long", true, "positive", false),
                new DtoFieldCandidate("name", "String", true, "notBlank", false),
                new DtoFieldCandidate("updatedAt", "OffsetDateTime", false, "", false)
        );
    }

    private List<DtoFieldCandidate> defaultResponseFields() {
        return List.of(
                new DtoFieldCandidate("id", "Long", true, "positive", false),
                new DtoFieldCandidate("name", "String", true, "notBlank", false),
                new DtoFieldCandidate("status", "String", false, "", false),
                new DtoFieldCandidate("updatedAt", "OffsetDateTime", false, "", false)
        );
    }

    private List<DtoFieldCandidate> defaultCreateFields() {
        return List.of(
                new DtoFieldCandidate("name", "String", true, "notBlank", false),
                new DtoFieldCandidate("reason", "String", false, "max=500", false)
        );
    }

    private List<DtoFieldCandidate> defaultUpdateFields() {
        return List.of(
                new DtoFieldCandidate("id", "Long", true, "positive", false),
                new DtoFieldCandidate("name", "String", false, "max=200", false),
                new DtoFieldCandidate("reason", "String", false, "max=500", false)
        );
    }

    private List<DtoFieldCandidate> defaultApprovalFields() {
        return List.of(
                new DtoFieldCandidate("targetId", "Long", true, "positive", false),
                new DtoFieldCandidate("reason", "String", true, "notBlank", false)
        );
    }

    private List<DtoFieldCandidate> approvalResponseFields() {
        return List.of(
                new DtoFieldCandidate("approvalId", "Long", true, "positive", false),
                new DtoFieldCandidate("status", "String", true, "enum", false)
        );
    }

    private List<DtoFieldCandidate> notificationFields() {
        return List.of(
                new DtoFieldCandidate("target", "String", true, "notBlank", false),
                new DtoFieldCandidate("message", "String", true, "notBlank", false)
        );
    }

    private List<DtoFieldCandidate> permissionFields() {
        return List.of(
                new DtoFieldCandidate("targetUserId", "String", true, "notBlank", false),
                new DtoFieldCandidate("role", "String", true, "notBlank", false)
        );
    }
}

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

    public List<DtoCandidate> generate(String domainClass, List<ApiEndpointCandidate> endpoints) {
        Map<String, DtoCandidate> dtos = new LinkedHashMap<>();
        for (ApiEndpointCandidate endpoint : endpoints) {
            switch (endpoint.httpMethod()) {
                case "GET" -> {
                    if (endpoint.path().endsWith("}")) {
                        addIfMissing(dtos, domainClass + "Response", "詳細取得レスポンス", defaultResponseFields());
                    } else {
                        addIfMissing(dtos, domainClass + "SearchRequest", "検索条件", defaultSearchFields());
                        addIfMissing(dtos, domainClass + "SummaryResponse", "一覧レスポンス", defaultSummaryFields());
                    }
                }
                case "POST" -> {
                    if (endpoint.path().contains("approval-requests")) {
                        addIfMissing(dtos, domainClass + "ApprovalRequest", "承認依頼DTO", defaultApprovalFields());
                        addIfMissing(dtos, "ApprovalResponse", "承認結果DTO", approvalResponseFields());
                    } else if (endpoint.path().contains("notifications")) {
                        addIfMissing(dtos, "NotificationRequest", "通知要求DTO", notificationFields());
                    } else if (endpoint.path().contains("permissions")) {
                        addIfMissing(dtos, "PermissionChangeRequest", "権限変更DTO", permissionFields());
                    } else {
                        addIfMissing(dtos, domainClass + "CreateRequest", "作成要求DTO", defaultCreateFields());
                    }
                }
                case "PUT" -> addIfMissing(dtos, domainClass + "UpdateRequest", "更新要求DTO", defaultUpdateFields());
                case "DELETE" -> addIfMissing(dtos, "ApprovalResponse", "承認結果DTO", approvalResponseFields());
                default -> {
                }
            }
        }
        return new ArrayList<>(dtos.values());
    }

    private void addIfMissing(Map<String, DtoCandidate> dtos, String name, String purpose, List<DtoFieldCandidate> fields) {
        if (name == null || name.isBlank() || dtos.containsKey(name)) {
            return;
        }
        dtos.put(name, new DtoCandidate(name, purpose, fields));
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

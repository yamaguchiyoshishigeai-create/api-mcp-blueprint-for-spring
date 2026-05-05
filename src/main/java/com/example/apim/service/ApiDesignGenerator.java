package com.example.apim.service;

import com.example.apim.model.ApiEndpointCandidate;
import com.example.apim.model.NormalizedBlueprintInput;
import com.example.apim.support.DomainNameNormalizer;
import com.example.apim.support.OperationType;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
public class ApiDesignGenerator {

    private static final String PRIMARY_DOMAIN_API = "主ドメインAPI";
    private static final String RELATED_DOMAIN_REFERENCE_API = "関連ドメイン参照API";

    public List<ApiEndpointCandidate> generate(String domainPath, String domainClass, Set<OperationType> operations, String actors) {
        return generate(domainPath, domainClass, operations, actors, "", "");
    }

    public List<ApiEndpointCandidate> generate(
            NormalizedBlueprintInput normalizedInput,
            DomainNameNormalizer domainNameNormalizer,
            Set<OperationType> operations,
            String actors
    ) {
        String primaryDomain = primaryDomainText(normalizedInput);
        String primaryDomainPath = domainNameNormalizer.normalizeUrlSegment(primaryDomain);
        String primaryDomainClass = domainNameNormalizer.normalizeClassName(primaryDomain);

        List<ApiEndpointCandidate> endpoints = new ArrayList<>();
        endpoints.addAll(generate(primaryDomainPath, primaryDomainClass, operations, actors, PRIMARY_DOMAIN_API, primaryDomain));
        for (String relatedDomain : normalizedInput.relatedDomains()) {
            String relatedDomainPath = domainNameNormalizer.normalizeUrlSegment(relatedDomain);
            String relatedDomainClass = domainNameNormalizer.normalizeClassName(relatedDomain);
            endpoints.add(endpoint("GET", "/api/" + relatedDomainPath,
                    "参照一覧取得", actors,
                    relatedDomainClass + "ReferenceSearchRequest", relatedDomainClass + "ReferenceSummaryResponse",
                    "閲覧権限", "不要", "推奨", RELATED_DOMAIN_REFERENCE_API, relatedDomain));
            endpoints.add(endpoint("GET", "/api/" + relatedDomainPath + "/{id}",
                    "参照詳細取得", actors,
                    "", relatedDomainClass + "ReferenceResponse",
                    "閲覧権限", "不要", "推奨", RELATED_DOMAIN_REFERENCE_API, relatedDomain));
        }
        return endpoints;
    }

    private List<ApiEndpointCandidate> generate(
            String domainPath,
            String domainClass,
            Set<OperationType> operations,
            String actors,
            String domainRole,
            String domainName
    ) {
        List<ApiEndpointCandidate> endpoints = new ArrayList<>();
        if (operations.contains(OperationType.SEARCH)) {
            endpoints.add(endpoint("GET", "/api/" + domainPath, "検索・一覧取得", actors,
                    domainClass + "SearchRequest", domainClass + "SummaryResponse",
                    "閲覧権限", "不要", "推奨", domainRole, domainName));
        }
        if (operations.contains(OperationType.READ)) {
            endpoints.add(endpoint("GET", "/api/" + domainPath + "/{id}", "詳細取得", actors,
                    "", domainClass + "Response",
                    "閲覧権限", "不要", "推奨", domainRole, domainName));
        }
        if (operations.contains(OperationType.CREATE)) {
            endpoints.add(endpoint("POST", "/api/" + domainPath, "新規作成", actors,
                    domainClass + "CreateRequest", domainClass + "Response",
                    "作成権限", "検討", "必須", domainRole, domainName));
        }
        if (operations.contains(OperationType.UPDATE)) {
            endpoints.add(endpoint("PUT", "/api/" + domainPath + "/{id}", "更新", actors,
                    domainClass + "UpdateRequest", domainClass + "Response",
                    "更新権限", "原則必要", "必須", domainRole, domainName));
        }
        if (operations.contains(OperationType.DELETE)) {
            endpoints.add(endpoint("DELETE", "/api/" + domainPath + "/{id}", "削除", actors,
                    "", "ApprovalResponse",
                    "削除権限", "必須", "必須", domainRole, domainName));
        }
        if (operations.contains(OperationType.APPROVAL)) {
            endpoints.add(endpoint("POST", "/api/" + domainPath + "/{id}/approval-requests", "承認依頼作成", actors,
                    domainClass + "ApprovalRequest", "ApprovalResponse",
                    "承認依頼権限", "必須", "必須", domainRole, domainName));
            endpoints.add(endpoint("POST", "/api/approval-requests/{id}/approve", "承認", actors,
                    "ApprovalActionRequest", "ApprovalResponse",
                    "承認権限", "必須", "必須", domainRole, domainName));
            endpoints.add(endpoint("POST", "/api/approval-requests/{id}/reject", "却下", actors,
                    "ApprovalActionRequest", "ApprovalResponse",
                    "承認権限", "必須", "必須", domainRole, domainName));
        }
        if (operations.contains(OperationType.SUMMARY)) {
            endpoints.add(endpoint("GET", "/api/" + domainPath + "/{id}/summary", "要約取得", actors,
                    "", domainClass + "SummaryResponse",
                    "閲覧権限", "不要", "推奨", domainRole, domainName));
        }
        if (operations.contains(OperationType.NOTIFICATION)) {
            endpoints.add(endpoint("POST", "/api/" + domainPath + "/{id}/notifications", "通知送信", actors,
                    "NotificationRequest", "NotificationResponse",
                    "通知権限", "原則必要", "必須", domainRole, domainName));
        }
        if (operations.contains(OperationType.PERMISSION)) {
            endpoints.add(endpoint("POST", "/api/" + domainPath + "/{id}/permissions", "権限変更", actors,
                    "PermissionChangeRequest", "PermissionChangeResponse",
                    "管理者権限", "必須", "必須", domainRole, domainName));
        }
        return endpoints;
    }

    private ApiEndpointCandidate endpoint(
            String method,
            String path,
            String purpose,
            String actors,
            String requestDto,
            String responseDto,
            String authorization,
            String approvalRequired,
            String auditRequired,
            String domainRole,
            String domainName
    ) {
        return new ApiEndpointCandidate(method, path, scopedPurpose(purpose, domainRole, domainName), actors, requestDto,
                responseDto, authorization, approvalRequired, auditRequired, domainRole, domainName);
    }

    private String scopedPurpose(String purpose, String domainRole, String domainName) {
        if (domainRole == null || domainRole.isBlank()) {
            return purpose;
        }
        String domainLabel = domainName == null || domainName.isBlank() ? "" : "(" + domainName + ")";
        return domainRole + domainLabel + ": " + purpose;
    }

    private String primaryDomainText(NormalizedBlueprintInput normalizedInput) {
        if (normalizedInput.primaryDomain() != null && !normalizedInput.primaryDomain().isBlank()) {
            return normalizedInput.primaryDomain();
        }
        return normalizedInput.targetDomainText();
    }
}

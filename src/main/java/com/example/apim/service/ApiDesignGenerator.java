package com.example.apim.service;

import com.example.apim.model.ApiEndpointCandidate;
import com.example.apim.support.OperationType;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
public class ApiDesignGenerator {

    public List<ApiEndpointCandidate> generate(String domainPath, String domainClass, Set<OperationType> operations, String actors) {
        List<ApiEndpointCandidate> endpoints = new ArrayList<>();
        if (operations.contains(OperationType.SEARCH)) {
            endpoints.add(endpoint("GET", "/api/" + domainPath, "検索・一覧取得", actors,
                    domainClass + "SearchRequest", domainClass + "SummaryResponse",
                    "閲覧権限", "不要", "推奨"));
        }
        if (operations.contains(OperationType.READ)) {
            endpoints.add(endpoint("GET", "/api/" + domainPath + "/{id}", "詳細取得", actors,
                    "", domainClass + "Response",
                    "閲覧権限", "不要", "推奨"));
        }
        if (operations.contains(OperationType.CREATE)) {
            endpoints.add(endpoint("POST", "/api/" + domainPath, "新規作成", actors,
                    domainClass + "CreateRequest", domainClass + "Response",
                    "作成権限", "検討", "必須"));
        }
        if (operations.contains(OperationType.UPDATE)) {
            endpoints.add(endpoint("PUT", "/api/" + domainPath + "/{id}", "更新", actors,
                    domainClass + "UpdateRequest", domainClass + "Response",
                    "更新権限", "原則必要", "必須"));
        }
        if (operations.contains(OperationType.DELETE)) {
            endpoints.add(endpoint("DELETE", "/api/" + domainPath + "/{id}", "削除", actors,
                    "", "ApprovalResponse",
                    "削除権限", "必須", "必須"));
        }
        if (operations.contains(OperationType.APPROVAL)) {
            endpoints.add(endpoint("POST", "/api/" + domainPath + "/{id}/approval-requests", "承認依頼作成", actors,
                    domainClass + "ApprovalRequest", "ApprovalResponse",
                    "承認依頼権限", "必須", "必須"));
            endpoints.add(endpoint("POST", "/api/approval-requests/{id}/approve", "承認", actors,
                    "ApprovalActionRequest", "ApprovalResponse",
                    "承認権限", "必須", "必須"));
            endpoints.add(endpoint("POST", "/api/approval-requests/{id}/reject", "却下", actors,
                    "ApprovalActionRequest", "ApprovalResponse",
                    "承認権限", "必須", "必須"));
        }
        if (operations.contains(OperationType.SUMMARY)) {
            endpoints.add(endpoint("GET", "/api/" + domainPath + "/{id}/summary", "要約取得", actors,
                    "", domainClass + "SummaryResponse",
                    "閲覧権限", "不要", "推奨"));
        }
        if (operations.contains(OperationType.NOTIFICATION)) {
            endpoints.add(endpoint("POST", "/api/" + domainPath + "/{id}/notifications", "通知送信", actors,
                    "NotificationRequest", "NotificationResponse",
                    "通知権限", "原則必要", "必須"));
        }
        if (operations.contains(OperationType.PERMISSION)) {
            endpoints.add(endpoint("POST", "/api/" + domainPath + "/{id}/permissions", "権限変更", actors,
                    "PermissionChangeRequest", "PermissionChangeResponse",
                    "管理者権限", "必須", "必須"));
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
            String auditRequired
    ) {
        return new ApiEndpointCandidate(method, path, purpose, actors, requestDto, responseDto, authorization, approvalRequired, auditRequired);
    }
}

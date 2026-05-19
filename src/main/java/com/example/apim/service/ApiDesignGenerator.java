package com.example.apim.service;

import com.example.apim.model.ApiEndpointCandidate;
import com.example.apim.model.BlueprintInput;
import com.example.apim.model.NormalizedBlueprintInput;
import com.example.apim.support.DomainNameNormalizer;
import com.example.apim.support.OperationType;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

@Service
public class ApiDesignGenerator {

    private static final String PRIMARY_DOMAIN_API = "主ドメインAPI";
    private static final String RELATED_DOMAIN_REFERENCE_API = "関連ドメイン参照API";
    private static final String BUSINESS_OBJECT_API = "業務オブジェクトAPI";

    public List<ApiEndpointCandidate> generate(String domainPath, String domainClass, Set<OperationType> operations, String actors) {
        return generate(domainPath, domainClass, operations, actors, "", "");
    }

    public List<ApiEndpointCandidate> generate(
            NormalizedBlueprintInput normalizedInput,
            DomainNameNormalizer domainNameNormalizer,
            Set<OperationType> operations,
            String actors
    ) {
        if (normalizedInput != null
                && normalizedInput.originalInput() != null
                && normalizedInput.originalInput().hasV2BusinessOperationModel()) {
            return generateFromV2(normalizedInput.originalInput(), domainNameNormalizer);
        }

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

    public List<ApiEndpointCandidate> generateFromV2(BlueprintInput input, DomainNameNormalizer domainNameNormalizer) {
        BlueprintInput safeInput = input == null ? new BlueprintInput() : input;
        DomainNameNormalizer normalizer = domainNameNormalizer == null ? new DomainNameNormalizer() : domainNameNormalizer;
        Map<String, BlueprintInput.V2BusinessObject> objectsById = objectsById(safeInput);
        Map<String, BlueprintInput.V2Actor> actorsById = actorsById(safeInput);
        Map<String, ApiEndpointCandidate> endpoints = new LinkedHashMap<>();
        boolean hasApprovalRequest = false;

        for (BlueprintInput.V2Operation operation : safeInput.getV2Operations()) {
            List<BlueprintInput.V2BusinessObject> targetObjects = targetObjects(operation, objectsById);
            for (BlueprintInput.V2BusinessObject object : targetObjects) {
                addReadEndpoints(endpoints, object, normalizer, actorsFor(operation, actorsById));
            }

            BlueprintInput.V2BusinessObject targetObject = primaryTargetObject(operation, targetObjects);
            if (targetObject == null) {
                continue;
            }
            String resourcePath = normalizer.normalizeUrlSegment(targetObject.name());
            String className = normalizer.normalizeClassName(targetObject.name());
            String actors = actorsFor(operation, actorsById);
            if (isSummaryOperation(operation)) {
                putEndpoint(endpoints, endpoint("GET", "/api/" + resourcePath + "/{id}/" + summarySlug(operation),
                        "AI要約取得", actors, "", className + "SummaryResponse",
                        "閲覧権限", approvalFor(operation), auditFor(operation), BUSINESS_OBJECT_API, targetObject.name()));
            } else if (isCandidateOperation(operation)) {
                putEndpoint(endpoints, endpoint("GET", "/api/" + resourcePath + "/" + candidateSlug(operation),
                        "AI候補取得", actors, "", className + "CandidateListResponse",
                        "閲覧権限", approvalFor(operation), auditFor(operation), BUSINESS_OBJECT_API, targetObject.name()));
            } else if (isDraftOperation(operation)) {
                putEndpoint(endpoints, endpoint("POST", "/api/" + resourcePath + "/{id}/" + draftSlug(operation),
                        "文案・下書き作成", actors, className + "DraftRequest", className + "DraftResponse",
                        "作成権限", approvalFor(operation), auditFor(operation), BUSINESS_OBJECT_API, targetObject.name()));
            } else if (isApprovalRequestOperation(operation)) {
                hasApprovalRequest = true;
                putEndpoint(endpoints, endpoint("POST", "/api/" + resourcePath + "/{id}/" + requestSlug(operation),
                        "承認付き変更依頼作成", actors, className + "ChangeRequest", "ApprovalResponse",
                        "承認依頼権限", "必須", "必須", BUSINESS_OBJECT_API, targetObject.name()));
            } else if (isProposalOperation(operation)) {
                putEndpoint(endpoints, endpoint("POST", "/api/" + resourcePath + "/{id}/change-proposals",
                        "変更提案作成", actors, className + "ChangeProposalRequest", className + "ChangeProposalResponse",
                        "提案作成権限", approvalFor(operation), auditFor(operation), BUSINESS_OBJECT_API, targetObject.name()));
            } else if (isDangerousWrite(operation)) {
                hasApprovalRequest = true;
                putEndpoint(endpoints, endpoint("POST", "/api/" + resourcePath + "/{id}/" + requestSlug(operation),
                        "承認付き実行依頼作成", actors, className + "ChangeRequest", "ApprovalResponse",
                        "承認依頼権限", "必須", "必須", BUSINESS_OBJECT_API, targetObject.name()));
            }
        }

        if (hasApprovalRequest) {
            putEndpoint(endpoints, endpoint("POST", "/api/approval-requests/{id}/approve", "承認", "",
                    "ApprovalActionRequest", "ApprovalResponse",
                    "承認権限", "必須", "必須", "承認フローAPI", "承認依頼"));
            putEndpoint(endpoints, endpoint("POST", "/api/approval-requests/{id}/reject", "却下", "",
                    "ApprovalActionRequest", "ApprovalResponse",
                    "承認権限", "必須", "必須", "承認フローAPI", "承認依頼"));
        }

        return new ArrayList<>(endpoints.values());
    }

    private Map<String, BlueprintInput.V2BusinessObject> objectsById(BlueprintInput input) {
        Map<String, BlueprintInput.V2BusinessObject> objectsById = new LinkedHashMap<>();
        for (BlueprintInput.V2BusinessObject object : input.getV2BusinessObjects()) {
            if (!object.id().isBlank() && !object.name().isBlank()) {
                objectsById.putIfAbsent(object.id(), object);
            }
        }
        return objectsById;
    }

    private Map<String, BlueprintInput.V2Actor> actorsById(BlueprintInput input) {
        Map<String, BlueprintInput.V2Actor> actorsById = new LinkedHashMap<>();
        for (BlueprintInput.V2Actor actor : input.getV2Actors()) {
            if (!actor.id().isBlank() && !actor.name().isBlank()) {
                actorsById.putIfAbsent(actor.id(), actor);
            }
        }
        return actorsById;
    }

    private List<BlueprintInput.V2BusinessObject> targetObjects(
            BlueprintInput.V2Operation operation,
            Map<String, BlueprintInput.V2BusinessObject> objectsById
    ) {
        List<BlueprintInput.V2BusinessObject> objects = new ArrayList<>();
        for (String objectId : operation.objectIds()) {
            BlueprintInput.V2BusinessObject object = objectsById.get(objectId);
            if (object != null) {
                objects.add(object);
            }
        }
        return List.copyOf(objects);
    }

    private void addReadEndpoints(
            Map<String, ApiEndpointCandidate> endpoints,
            BlueprintInput.V2BusinessObject object,
            DomainNameNormalizer normalizer,
            String actors
    ) {
        String resourcePath = normalizer.normalizeUrlSegment(object.name());
        String className = normalizer.normalizeClassName(object.name());
        putEndpoint(endpoints, endpoint("GET", "/api/" + resourcePath, "検索・一覧取得", actors,
                className + "SearchRequest", className + "SummaryResponse",
                "閲覧権限", "不要", "推奨", BUSINESS_OBJECT_API, object.name()));
        putEndpoint(endpoints, endpoint("GET", "/api/" + resourcePath + "/{id}", "詳細取得", actors,
                "", className + "Response",
                "閲覧権限", "不要", "推奨", BUSINESS_OBJECT_API, object.name()));
    }

    private BlueprintInput.V2BusinessObject primaryTargetObject(
            BlueprintInput.V2Operation operation,
            List<BlueprintInput.V2BusinessObject> targetObjects
    ) {
        if (targetObjects.isEmpty()) {
            return null;
        }
        String label = operation.label();
        for (BlueprintInput.V2BusinessObject object : targetObjects) {
            if (!object.name().isBlank() && label.contains(object.name())) {
                return object;
            }
        }
        if (targetObjects.size() > 1 && "顧客".equals(targetObjects.get(0).name())) {
            return targetObjects.get(1);
        }
        return targetObjects.get(0);
    }

    private String actorsFor(BlueprintInput.V2Operation operation, Map<String, BlueprintInput.V2Actor> actorsById) {
        List<String> names = new ArrayList<>();
        for (String actorId : operation.actorIds()) {
            BlueprintInput.V2Actor actor = actorsById.get(actorId);
            if (actor != null && !actor.name().isBlank()) {
                names.add(actor.name());
            }
        }
        return String.join(" / ", names);
    }

    private boolean isSummaryOperation(BlueprintInput.V2Operation operation) {
        return "ai_summary".equals(operation.intent()) || containsAny(operation.label(), "要約", "サマリ");
    }

    private boolean isCandidateOperation(BlueprintInput.V2Operation operation) {
        return "candidate_list".equals(operation.outputType())
                || containsAny(operation.intent(), "analysis")
                || containsAny(operation.label(), "候補", "抽出", "リスク");
    }

    private boolean isDraftOperation(BlueprintInput.V2Operation operation) {
        return "ai_draft".equals(operation.intent())
                || "draft_only".equals(operation.executionMode())
                || containsAny(operation.label(), "文案", "下書き", "草案");
    }

    private boolean isApprovalRequestOperation(BlueprintInput.V2Operation operation) {
        return operation.approvalRequired()
                || "approval_request".equals(operation.intent())
                || "human_approved_write".equals(operation.executionMode());
    }

    private boolean isProposalOperation(BlueprintInput.V2Operation operation) {
        return "proposal".equals(operation.intent()) || containsAny(operation.label(), "提案", "変更案", "更新案");
    }

    private boolean isDangerousWrite(BlueprintInput.V2Operation operation) {
        return operation.externalAction()
                || operation.stateChanging()
                || Set.of("write", "state_transition", "delete", "external_action", "admin").contains(operation.intent())
                || Set.of("high", "critical").contains(operation.riskLevel());
    }

    private String summarySlug(BlueprintInput.V2Operation operation) {
        String label = operation.label();
        if (label.contains("履歴")) {
            return "history-summary";
        }
        if (label.contains("リスク")) {
            return "risk-summary";
        }
        return "summary";
    }

    private String candidateSlug(BlueprintInput.V2Operation operation) {
        String label = operation.label();
        if (label.contains("失注") || label.toLowerCase(Locale.ROOT).contains("loss")) {
            return "loss-risk-candidates";
        }
        if (label.contains("遅延")) {
            return "delay-candidates";
        }
        if (label.contains("リスク")) {
            return "risk-candidates";
        }
        return "candidates";
    }

    private String draftSlug(BlueprintInput.V2Operation operation) {
        String label = operation.label();
        if (label.contains("フォローアップ")) {
            return "follow-up-drafts";
        }
        if (label.contains("通知")) {
            return "notification-drafts";
        }
        return "drafts";
    }

    private String requestSlug(BlueprintInput.V2Operation operation) {
        String label = operation.label();
        if (label.contains("受注確度")) {
            return "probability-change-requests";
        }
        if (label.contains("契約条件")) {
            return "condition-change-requests";
        }
        if (label.contains("請求確定")) {
            return "confirmation-requests";
        }
        if (label.contains("入金消込")) {
            return "reconciliation-requests";
        }
        return "change-requests";
    }

    private String approvalFor(BlueprintInput.V2Operation operation) {
        return operation.approvalRequired() ? "必須" : "不要";
    }

    private String auditFor(BlueprintInput.V2Operation operation) {
        return "required".equals(operation.auditLogRequired()) || operation.approvalRequired() ? "必須" : "推奨";
    }

    private void putEndpoint(Map<String, ApiEndpointCandidate> endpoints, ApiEndpointCandidate endpoint) {
        String key = endpoint.httpMethod() + " " + endpoint.path();
        endpoints.putIfAbsent(key, endpoint);
    }

    private boolean containsAny(String value, String... keywords) {
        String safeValue = value == null ? "" : value.toLowerCase(Locale.ROOT);
        for (String keyword : keywords) {
            if (safeValue.contains(keyword.toLowerCase(Locale.ROOT))) {
                return true;
            }
        }
        return false;
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

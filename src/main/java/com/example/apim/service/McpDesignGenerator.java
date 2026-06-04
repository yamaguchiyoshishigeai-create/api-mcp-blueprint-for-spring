package com.example.apim.service;

import com.example.apim.model.ApiEndpointCandidate;
import com.example.apim.model.ApiMcpMapping;
import com.example.apim.model.BlueprintInput;
import com.example.apim.model.McpPromptCandidate;
import com.example.apim.model.McpResourceCandidate;
import com.example.apim.model.McpToolCandidate;
import com.example.apim.model.NormalizedBlueprintInput;
import com.example.apim.support.DomainNameNormalizer;
import com.example.apim.support.OperationType;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

@Service
public class McpDesignGenerator {

    public record McpDesignResult(
            List<McpToolCandidate> tools,
            List<McpResourceCandidate> resources,
            List<McpPromptCandidate> prompts,
            List<ApiMcpMapping> mappings
    ) {
    }

    public McpDesignResult generate(String domainClass, String domainPath, Set<OperationType> operations, List<ApiEndpointCandidate> endpoints) {
        Set<OperationType> safeOperations = operations == null ? Set.of() : operations;
        List<ApiEndpointCandidate> safeEndpoints = endpoints == null ? List.of() : endpoints;
        String domainPluralClass = toPluralClass(domainPath, domainClass);
        List<McpToolCandidate> tools = new ArrayList<>();
        List<McpResourceCandidate> resources = new ArrayList<>();
        List<McpPromptCandidate> prompts = new ArrayList<>();
        List<ApiMcpMapping> mappings = new ArrayList<>();

        if (safeOperations.contains(OperationType.SEARCH)) {
            tools.add(tool("search" + domainPluralClass, "検索", "keyword", domainClass + "SummaryResponse",
                    "/api/" + domainPath, "read", "AI実行可", "不要", "推奨"));
        }
        if (safeOperations.contains(OperationType.READ)) {
            tools.add(tool("get" + domainClass + "Detail", "詳細取得", "id", domainClass + "Response",
                    "/api/" + domainPath + "/{id}", "read", "AI実行可", "不要", "推奨"));
        }
        if (safeOperations.contains(OperationType.SUMMARY)) {
            tools.add(tool("summarize" + domainClass + "Interactions", "要約", "id", domainClass + "SummaryResponse",
                    "/api/" + domainPath + "/{id}/summary", "read", "AI実行可（機密情報注意）", "不要", "推奨"));
        }
        if (safeOperations.contains(OperationType.CREATE)) {
            tools.add(tool("create" + domainClass + "Draft", "作成下書き", "payload", domainClass + "CreateRequest",
                    "/api/" + domainPath, "write", "下書き作成または承認後実行", "検討", "必須"));
        }
        if (safeOperations.contains(OperationType.UPDATE)) {
            tools.add(tool("propose" + domainClass + "Update", "更新提案", "payload", domainClass + "UpdateRequest",
                    "/api/" + domainPath + "/{id}", "write", "提案のみまたは承認後実行", "原則必要", "必須"));
        }
        if (safeOperations.contains(OperationType.DELETE)) {
            tools.add(tool("request" + domainPluralClass + "DeletionApproval", "削除承認依頼", "id", "ApprovalResponse",
                    "/api/" + domainPath + "/{id}", "write", "AI実行不可", "必須", "必須"));
        }
        if (safeOperations.contains(OperationType.APPROVAL)) {
            tools.add(tool("requestApprovalFor" + domainPluralClass, "承認依頼", "approvalRequest", "ApprovalResponse",
                    "/api/" + domainPath + "/{id}/approval-requests", "write", "承認後実行", "必須", "必須"));
        }

        resources.add(new McpResourceCandidate(domainPath + "-catalog", "業務ドメインの基本情報", "read-only"));
        resources.add(new McpResourceCandidate(domainPath + "-audit-policy", "監査ログポリシー", "restricted"));

        prompts.add(new McpPromptCandidate("analyze-" + domainPath + "-requirements",
                "業務要件からAPI&MCP境界を整理する",
                "入力要件を読み取り、API候補・MCP候補・承認要件を整理してください。"));
        prompts.add(new McpPromptCandidate("review-" + domainPath + "-safety",
                "危険操作の承認・監査設計を確認する",
                "削除・外部送信・権限変更について承認要否と監査ログ項目を提示してください。"));

        for (McpToolCandidate tool : tools) {
            String matchedApi = safeEndpoints.stream()
                    .filter(e -> e.path().equals(tool.relatedApi()))
                    .map(ApiEndpointCandidate::path)
                    .findFirst()
                    .orElse(tool.relatedApi());
            mappings.add(new ApiMcpMapping(matchedApi, tool.name(), tool.aiExecutionPolicy()));
        }

        return new McpDesignResult(tools, resources, prompts, mappings);
    }

    public McpDesignResult generate(
            NormalizedBlueprintInput normalizedInput,
            DomainNameNormalizer domainNameNormalizer,
            Set<OperationType> operations,
            List<ApiEndpointCandidate> endpoints
    ) {
        NormalizedBlueprintInput safeInput = normalizedInput == null
                ? new NormalizedBlueprintInput(List.of(), "", List.of(), List.of(), "", null)
                : normalizedInput;
        DomainNameNormalizer safeNormalizer = domainNameNormalizer == null ? new DomainNameNormalizer() : domainNameNormalizer;
        Set<OperationType> safeOperations = operations == null ? Set.of() : operations;
        List<ApiEndpointCandidate> safeEndpoints = endpoints == null ? List.of() : endpoints;

        String primaryDomain = primaryDomainText(safeInput);
        String primaryDomainPath = safeNormalizer.normalizeUrlSegment(primaryDomain);
        String primaryDomainClass = safeNormalizer.normalizeClassName(primaryDomain);
        if (safeInput.relatedDomains().isEmpty()) {
            return generate(primaryDomainClass, primaryDomainPath, safeOperations, safeEndpoints);
        }

        List<McpToolCandidate> tools = new ArrayList<>();
        List<McpResourceCandidate> resources = new ArrayList<>();
        List<McpPromptCandidate> prompts = new ArrayList<>();

        tools.addAll(primaryDomainTools(primaryDomainClass, primaryDomainPath, primaryDomain, safeOperations));
        for (String relatedDomain : safeInput.relatedDomains()) {
            String relatedDomainPath = safeNormalizer.normalizeUrlSegment(relatedDomain);
            String relatedDomainClass = safeNormalizer.normalizeClassName(relatedDomain);
            tools.addAll(relatedDomainReferenceTools(relatedDomainClass, relatedDomainPath, relatedDomain, safeEndpoints));
        }

        resources.add(new McpResourceCandidate(primaryDomainPath + "-catalog",
                scopedPurpose("主ドメインresource", primaryDomain, "業務ドメインの基本情報"), "read-only"));
        resources.add(new McpResourceCandidate(primaryDomainPath + "-audit-policy",
                scopedPurpose("主ドメインresource", primaryDomain, "監査ログポリシー"), "restricted"));
        for (String relatedDomain : safeInput.relatedDomains()) {
            String relatedDomainPath = safeNormalizer.normalizeUrlSegment(relatedDomain);
            resources.add(new McpResourceCandidate(relatedDomainPath + "-reference-catalog",
                    scopedPurpose("関連ドメインresource", relatedDomain, "参照用基本情報"), "read-only"));
            resources.add(new McpResourceCandidate(relatedDomainPath + "-reference-boundary-policy",
                    scopedPurpose("関連ドメインresource", relatedDomain, "連携・要約時の参照範囲"), "read-only"));
        }

        prompts.add(new McpPromptCandidate("analyze-" + primaryDomainPath + "-cross-domain-requirements",
                scopedPurpose("横断prompt", primaryDomain, "複数ドメイン利用場面とAPI&MCP境界を整理する"),
                "利用場面: 主ドメインの業務操作に関連ドメイン参照を添えて判断材料を整理する。"
                        + " 禁止事項: 関連ドメインへの書き込み・削除・権限変更を自動実行しない。"
                        + " 人間確認条件: 主ドメイン更新、関連ドメイン境界越え、センシティブ情報を含む要約は承認者確認を必須にする。"));
        prompts.add(new McpPromptCandidate("review-" + primaryDomainPath + "-cross-domain-safety",
                scopedPurpose("横断prompt", primaryDomain, "禁止事項・人間確認条件・監査要否を確認する"),
                "利用場面: 主ドメインtoolと関連ドメインresourceを組み合わせる前の安全確認。"
                        + " 禁止事項: AI判断だけで外部送信、削除、承認、関連ドメイン更新を行わない。"
                        + " 人間確認条件: 承認必須操作、監査ログ必須操作、個人情報・契約・決済等のセンシティブ情報を扱う場合。"));

        return new McpDesignResult(tools, resources, prompts, scopedMappings(tools, safeEndpoints));
    }

    public McpDesignResult generate(
            BlueprintInput input,
            NormalizedBlueprintInput normalizedInput,
            DomainNameNormalizer domainNameNormalizer,
            Set<OperationType> operations,
            List<ApiEndpointCandidate> endpoints
    ) {
        if (!hasV2SafetyClassification(input)) {
            NormalizedBlueprintInput safeInput = normalizedInput == null
                    ? new NormalizedBlueprintInput(List.of(), "", List.of(), List.of(), "", input)
                    : normalizedInput;
            DomainNameNormalizer safeNormalizer = domainNameNormalizer == null ? new DomainNameNormalizer() : domainNameNormalizer;
            String primaryDomain = primaryDomainText(safeInput);
            String domainPath = safeNormalizer.normalizeUrlSegment(primaryDomain);
            String domainClass = safeNormalizer.normalizeClassName(primaryDomain);
            if (safeInput.relatedDomains().isEmpty()) {
                return generate(domainClass, domainPath, operations, endpoints);
            }
            return generate(safeInput, safeNormalizer, operations, endpoints);
        }

        NormalizedBlueprintInput safeInput = normalizedInput == null
                ? new NormalizedBlueprintInput(List.of(), "", List.of(), List.of(), "", input)
                : normalizedInput;
        DomainNameNormalizer safeNormalizer = domainNameNormalizer == null ? new DomainNameNormalizer() : domainNameNormalizer;
        Set<OperationType> safeOperations = operations == null ? Set.of() : operations;
        List<ApiEndpointCandidate> safeEndpoints = endpoints == null ? List.of() : endpoints;
        DomainIdentity primaryDomain = resolvePrimaryDomain(input, safeInput, safeNormalizer, safeEndpoints);

        Map<String, McpToolCandidate> tools = new LinkedHashMap<>();
        v2PrimaryDomainTools(input, primaryDomain, safeOperations).forEach(tool -> tools.putIfAbsent(tool.name(), tool));
        for (String relatedDomain : safeInput.relatedDomains()) {
            if (relatedDomain.equals(primaryDomain.rawName())) {
                continue;
            }
            String relatedDomainPath = safeNormalizer.normalizeUrlSegment(relatedDomain);
            String relatedDomainClass = safeNormalizer.normalizeClassName(relatedDomain);
            relatedDomainReferenceTools(relatedDomainClass, relatedDomainPath, relatedDomain, safeEndpoints)
                    .forEach(tool -> tools.putIfAbsent(tool.name(), tool));
        }

        List<McpResourceCandidate> resources = scopedResources(safeInput, safeNormalizer, primaryDomain);
        List<McpPromptCandidate> prompts = scopedPrompts(safeInput, primaryDomain);
        List<McpToolCandidate> safeTools = tools.values().stream()
                .filter(this::doesNotUseDomainItemFallback)
                .toList();
        return new McpDesignResult(safeTools, resources, prompts, scopedMappings(safeTools, safeEndpoints));
    }

    private List<McpToolCandidate> v2PrimaryDomainTools(
            BlueprintInput input,
            DomainIdentity domain,
            Set<OperationType> operations
    ) {
        Map<String, McpToolCandidate> tools = new LinkedHashMap<>();
        OperationBuckets buckets = OperationBuckets.from(input);

        for (String label : buckets.allowedAiOperations()) {
            addAllowedAiTool(tools, label, buckets, domain);
        }
        for (String label : buckets.readOnlyOperations()) {
            addReadOnlyTool(tools, label, buckets, domain);
        }
        for (String label : buckets.writeOperations()) {
            addWriteBoundaryTools(tools, label, buckets, domain);
        }
        for (String label : buckets.approvalRequiredOperations()) {
            addApprovalRequestTool(tools, label, buckets, domain);
        }
        for (String label : buckets.auditLogRequiredOperations()) {
            if (!hasToolForLabel(tools, label, domain)) {
                addAuditOnlyTool(tools, label, domain);
            }
        }

        if (tools.isEmpty()) {
            return primaryDomainTools(domain.className(), domain.path(), domain.rawName(), operations);
        }
        return new ArrayList<>(tools.values());
    }

    private void addAllowedAiTool(
            Map<String, McpToolCandidate> tools,
            String label,
            OperationBuckets buckets,
            DomainIdentity domain
    ) {
        if (isProposalOperation(label)) {
            putTool(tools, proposalTool(domain, label, "AI実行可（変更提案のみ）",
                    approvalFor(label, buckets), auditFor(label, buckets)));
        } else if (isDraftOperation(label)) {
            putTool(tools, draftTool(domain, label, "AI実行可（文案・下書き作成のみ）",
                    approvalFor(label, buckets), auditFor(label, buckets)));
        } else if (isCandidateOperation(label)) {
            putTool(tools, candidateTool(domain, label, "AI実行可（候補作成のみ）",
                    approvalFor(label, buckets), auditFor(label, buckets)));
        } else if (isSummaryOperation(label)) {
            putTool(tools, tool("summarize" + domain.className() + "Interactions", purpose(domain, label, "要約"),
                    "id", domain.className() + "SummaryResponse", "/api/" + domain.path() + "/{id}/summary",
                    "summary", "AI実行可（機密情報注意）", approvalFor(label, buckets), auditFor(label, buckets)));
        } else if (isReadDetailOperation(label)) {
            putTool(tools, tool("get" + domain.className() + "Detail", purpose(domain, label, "詳細取得"),
                    "id", domain.className() + "Response", "/api/" + domain.path() + "/{id}",
                    "read", "AI実行可", approvalFor(label, buckets), auditFor(label, buckets)));
        } else {
            putTool(tools, tool("search" + domain.pluralClass(), purpose(domain, label, "検索"),
                    "keyword", domain.className() + "SummaryResponse", "/api/" + domain.path(),
                    "read", "AI実行可", approvalFor(label, buckets), auditFor(label, buckets)));
        }
    }

    private void addReadOnlyTool(
            Map<String, McpToolCandidate> tools,
            String label,
            OperationBuckets buckets,
            DomainIdentity domain
    ) {
        if (isSummaryOperation(label)) {
            putTool(tools, tool("summarize" + domain.className() + "Interactions", purpose(domain, label, "要約"),
                    "id", domain.className() + "SummaryResponse", "/api/" + domain.path() + "/{id}/summary",
                    "summary", "AI実行可（参照・要約のみ）", approvalFor(label, buckets), auditFor(label, buckets)));
        } else if (isReadDetailOperation(label)) {
            putTool(tools, tool("get" + domain.className() + "Detail", purpose(domain, label, "詳細取得"),
                    "id", domain.className() + "Response", "/api/" + domain.path() + "/{id}",
                    "read", "AI実行可（参照のみ）", approvalFor(label, buckets), auditFor(label, buckets)));
        } else {
            putTool(tools, tool("search" + domain.pluralClass(), purpose(domain, label, "検索"),
                    "keyword", domain.className() + "SummaryResponse", "/api/" + domain.path(),
                    "read", "AI実行可（参照のみ）", approvalFor(label, buckets), auditFor(label, buckets)));
        }
    }

    private void addWriteBoundaryTools(
            Map<String, McpToolCandidate> tools,
            String label,
            OperationBuckets buckets,
            DomainIdentity domain
    ) {
        if (isNotificationOperation(label)) {
            putTool(tools, draftNotificationTool(domain, label, "AI実行可（文案作成のみ）",
                    approvalFor(label, buckets), auditFor(label, buckets)));
            putTool(tools, tool("execute" + domain.className() + "NotificationAfterApproval",
                    purpose(domain, label, "送信実行境界"),
                    "approvedNotificationRequest", "NotificationResponse", "/api/" + domain.path() + "/{id}/notifications",
                    "execution", "AI直接実行不可（人間承認後の実行境界）", approvalFor(label, buckets), auditFor(label, buckets)));
        } else if (isCreateOperation(label) || isDraftOperation(label)) {
            putTool(tools, draftTool(domain, label, "AI実行可（下書き作成のみ）",
                    approvalFor(label, buckets), auditFor(label, buckets)));
        } else {
            putTool(tools, proposalTool(domain, label, "AI実行可（変更提案のみ）",
                    approvalFor(label, buckets), auditFor(label, buckets)));
            if (buckets.isApprovalRequired(label) || buckets.isAuditRequired(label)) {
                putTool(tools, tool("execute" + domain.className() + "UpdateAfterApproval",
                        purpose(domain, label, "実更新境界"),
                        "approvedPayload", domain.className() + "Response", "/api/" + domain.path() + "/{id}",
                        "execution", "AI直接実行不可（人間承認後の実行境界）", approvalFor(label, buckets), auditFor(label, buckets)));
            }
        }
    }

    private void addApprovalRequestTool(
            Map<String, McpToolCandidate> tools,
            String label,
            OperationBuckets buckets,
            DomainIdentity domain
    ) {
        if (isDeleteOperation(label)) {
            putTool(tools, tool("request" + domain.pluralClass() + "DeletionApproval",
                    purpose(domain, label, "削除承認依頼"),
                    "id", "ApprovalResponse", "/api/" + domain.path() + "/{id}",
                    "approval-request", "AI直接実行不可（承認依頼まで）", "必須", "必須"));
        } else if (isNotificationOperation(label)) {
            putTool(tools, draftNotificationTool(domain, label, "AI実行可（文案作成のみ）", "必須", auditFor(label, buckets)));
            putTool(tools, tool("request" + domain.pluralClass() + "NotificationApproval",
                    purpose(domain, label, "送信承認依頼"),
                    "approvalRequest", "ApprovalResponse", "/api/" + domain.path() + "/{id}/approval-requests",
                    "approval-request", "AI直接実行不可（承認依頼まで）", "必須", "必須"));
        } else if (isApprovalAction(label)) {
            putTool(tools, tool("requestApprovalFor" + domain.pluralClass(), purpose(domain, label, "承認依頼"),
                    "approvalRequest", "ApprovalResponse", "/api/" + domain.path() + "/{id}/approval-requests",
                    "approval-request", "AI直接実行不可（承認依頼まで）", "必須", "必須"));
        } else {
            putTool(tools, tool("request" + domain.pluralClass() + "ChangeApproval",
                    purpose(domain, label, "変更承認依頼"),
                    "approvalRequest", "ApprovalResponse", "/api/" + domain.path() + "/{id}/approval-requests",
                    "approval-request", "AI直接実行不可（承認依頼まで）", "必須", "必須"));
        }
    }

    private void addAuditOnlyTool(Map<String, McpToolCandidate> tools, String label, DomainIdentity domain) {
        if (isDraftOperation(label)) {
            putTool(tools, draftTool(domain, label, "AI実行可（文案・下書き作成のみ）", "不要", "必須"));
        } else if (isSummaryOperation(label)) {
            putTool(tools, tool("summarize" + domain.className() + "Interactions", purpose(domain, label, "要約"),
                    "id", domain.className() + "SummaryResponse", "/api/" + domain.path() + "/{id}/summary",
                    "summary", "AI実行可（監査ログ必須）", "不要", "必須"));
        } else {
            putTool(tools, tool("record" + domain.className() + "AuditTrail", purpose(domain, label, "監査ログ記録境界"),
                    "auditEvent", "AuditLogResponse", "/api/" + domain.path() + "/{id}",
                    "execution", "AI直接実行不可（監査ログ必須操作の実行境界）", "原則必要", "必須"));
        }
    }

    private McpToolCandidate proposalTool(
            DomainIdentity domain,
            String label,
            String policy,
            String approval,
            String audit
    ) {
        return tool("propose" + domain.className() + "Update", purpose(domain, label, "変更提案"),
                "payload", domain.className() + "UpdateRequest", "/api/" + domain.path() + "/{id}",
                "proposal", policy, approval, audit);
    }

    private McpToolCandidate draftTool(
            DomainIdentity domain,
            String label,
            String policy,
            String approval,
            String audit
    ) {
        return tool("create" + domain.className() + "Draft", purpose(domain, label, "下書き作成"),
                "payload", domain.className() + "CreateRequest", "/api/" + domain.path(),
                "draft", policy, approval, audit);
    }

    private McpToolCandidate draftNotificationTool(
            DomainIdentity domain,
            String label,
            String policy,
            String approval,
            String audit
    ) {
        return tool("draft" + domain.className() + "Notification", purpose(domain, label, "送信文案作成"),
                "payload", "NotificationRequest", "/api/" + domain.path() + "/{id}/notifications",
                "draft", policy, approval, audit);
    }

    private McpToolCandidate candidateTool(
            DomainIdentity domain,
            String label,
            String policy,
            String approval,
            String audit
    ) {
        return tool("suggest" + domain.className() + "Candidates", purpose(domain, label, "候補作成"),
                "criteria", domain.className() + "CandidateResponse", "/api/" + domain.path(),
                "candidate", policy, approval, audit);
    }

    private boolean hasV2SafetyClassification(BlueprintInput input) {
        if (input == null) {
            return false;
        }
        return hasText(input.getReadOnlyOperations())
                || hasText(input.getWriteOperations())
                || hasText(input.getApprovalRequiredOperations())
                || hasText(input.getAuditLogRequiredOperations());
    }

    private DomainIdentity resolvePrimaryDomain(
            BlueprintInput input,
            NormalizedBlueprintInput normalizedInput,
            DomainNameNormalizer normalizer,
            List<ApiEndpointCandidate> endpoints
    ) {
        String rawName = primaryDomainText(normalizedInput);
        String path = normalizer.normalizeUrlSegment(rawName);
        String className = normalizer.normalizeClassName(rawName);
        if (isDomainItemFallback(path, className)) {
            ApiEndpointCandidate endpoint = primaryEndpoint(endpoints);
            if (endpoint != null) {
                path = pathFromEndpoint(endpoint.path());
                className = classNameFromEndpoint(endpoint, path);
            }
        }
        if (isDomainItemFallback(path, className)) {
            String fallbackName = fallbackDomainName(input, normalizedInput);
            path = normalizer.normalizeUrlSegment(fallbackName);
            className = normalizer.normalizeClassName(fallbackName);
            rawName = fallbackName;
        }
        if (isDomainItemFallback(path, className)) {
            path = "business-operations";
            className = "BusinessOperation";
        }
        return new DomainIdentity(rawName, path, className, toPluralClass(path, className));
    }

    private ApiEndpointCandidate primaryEndpoint(List<ApiEndpointCandidate> endpoints) {
        return endpoints.stream()
                .filter(endpoint -> !endpoint.path().contains("/domain-items"))
                .filter(endpoint -> endpoint.domainRole().isBlank() || endpoint.domainRole().contains("主ドメイン"))
                .filter(endpoint -> !endpoint.path().startsWith("/api/approval-requests/"))
                .findFirst()
                .orElse(null);
    }

    private String pathFromEndpoint(String endpointPath) {
        if (endpointPath == null || endpointPath.isBlank()) {
            return "business-operations";
        }
        String withoutPrefix = endpointPath.startsWith("/api/") ? endpointPath.substring(5) : endpointPath;
        int slash = withoutPrefix.indexOf('/');
        return slash < 0 ? withoutPrefix : withoutPrefix.substring(0, slash);
    }

    private String classNameFromEndpoint(ApiEndpointCandidate endpoint, String path) {
        String candidate = firstNonBlank(endpoint.responseDto(), endpoint.requestDto());
        String stripped = candidate.replaceAll("(SearchRequest|SummaryResponse|ReferenceSummaryResponse|ReferenceResponse|CreateRequest|UpdateRequest|Response|Request)$", "");
        if (!stripped.isBlank() && !stripped.equals("Approval") && !stripped.equals("Notification")) {
            return stripped;
        }
        return new DomainNameNormalizer().normalizeClassName(path.replace("-", " "));
    }

    private String fallbackDomainName(BlueprintInput input, NormalizedBlueprintInput normalizedInput) {
        String normalizedPrimary = normalizedInput == null ? "" : normalizedInput.primaryDomain();
        String inputPrimary = input == null ? "" : input.getPrimaryDomain();
        String targetDomain = input == null ? "" : input.getTargetDomain();
        String targetDomainText = normalizedInput == null ? "" : normalizedInput.targetDomainText();
        return firstNonBlank(normalizedPrimary, inputPrimary, targetDomain, targetDomainText, "BusinessOperation");
    }

    private List<McpResourceCandidate> scopedResources(
            NormalizedBlueprintInput input,
            DomainNameNormalizer normalizer,
            DomainIdentity primaryDomain
    ) {
        List<McpResourceCandidate> resources = new ArrayList<>();
        resources.add(new McpResourceCandidate(primaryDomain.path() + "-catalog",
                scopedPurpose("主ドメインresource", primaryDomain.rawName(), "業務ドメインの基本情報"), "read-only"));
        resources.add(new McpResourceCandidate(primaryDomain.path() + "-audit-policy",
                scopedPurpose("主ドメインresource", primaryDomain.rawName(), "監査ログポリシー"), "restricted"));
        for (String relatedDomain : input.relatedDomains()) {
            if (relatedDomain.equals(primaryDomain.rawName())) {
                continue;
            }
            String relatedDomainPath = normalizer.normalizeUrlSegment(relatedDomain);
            if ("domain-items".equals(relatedDomainPath)) {
                continue;
            }
            resources.add(new McpResourceCandidate(relatedDomainPath + "-reference-catalog",
                    scopedPurpose("関連ドメインresource", relatedDomain, "参照用基本情報"), "read-only"));
            resources.add(new McpResourceCandidate(relatedDomainPath + "-reference-boundary-policy",
                    scopedPurpose("関連ドメインresource", relatedDomain, "連携・要約時の参照範囲"), "read-only"));
        }
        return resources;
    }

    private List<McpPromptCandidate> scopedPrompts(NormalizedBlueprintInput input, DomainIdentity primaryDomain) {
        List<McpPromptCandidate> prompts = new ArrayList<>();
        boolean hasRelatedDomains = input.relatedDomains().stream().anyMatch(domain -> !domain.equals(primaryDomain.rawName()));
        if (!hasRelatedDomains) {
            prompts.add(new McpPromptCandidate("analyze-" + primaryDomain.path() + "-requirements",
                    scopedPurpose("主ドメインprompt", primaryDomain.rawName(), "業務要件からAPI&MCP境界を整理する"),
                    "入力要件を読み取り、API候補・MCP候補・承認要件を整理してください。"));
            prompts.add(new McpPromptCandidate("review-" + primaryDomain.path() + "-safety",
                    scopedPurpose("主ドメインprompt", primaryDomain.rawName(), "危険操作の承認・監査設計を確認する"),
                    "削除・外部送信・権限変更について承認要否と監査ログ項目を提示してください。"));
            return prompts;
        }
        prompts.add(new McpPromptCandidate("analyze-" + primaryDomain.path() + "-cross-domain-requirements",
                scopedPurpose("横断prompt", primaryDomain.rawName(), "複数ドメイン利用場面とAPI&MCP境界を整理する"),
                "利用場面: 主ドメインの業務操作に関連ドメイン参照を添えて判断材料を整理する。"
                        + " 禁止事項: 関連ドメインへの書き込み・削除・権限変更を自動実行しない。"
                        + " 人間確認条件: 主ドメイン更新、関連ドメイン境界越え、センシティブ情報を含む要約は承認者確認を必須にする。"));
        prompts.add(new McpPromptCandidate("review-" + primaryDomain.path() + "-cross-domain-safety",
                scopedPurpose("横断prompt", primaryDomain.rawName(), "禁止事項・人間確認条件・監査要否を確認する"),
                "利用場面: 主ドメインtoolと関連ドメインresourceを組み合わせる前の安全確認。"
                        + " 禁止事項: AI判断だけで外部送信、削除、承認、関連ドメイン更新を行わない。"
                        + " 人間確認条件: 承認必須操作、監査ログ必須操作、個人情報・契約・決済等のセンシティブ情報を扱う場合。"));
        return prompts;
    }

    private void putTool(Map<String, McpToolCandidate> tools, McpToolCandidate tool) {
        if (doesNotUseDomainItemFallback(tool)) {
            tools.putIfAbsent(tool.name(), tool);
        }
    }

    private boolean hasToolForLabel(Map<String, McpToolCandidate> tools, String label, DomainIdentity domain) {
        String normalized = normalizeOperationLabel(label);
        return tools.values().stream().anyMatch(tool ->
                tool.purpose().contains(normalized)
                        || tool.name().contains(domain.className())
                        && (isSummaryOperation(label) && tool.operationType().equals("summary")
                        || isDraftOperation(label) && tool.operationType().equals("draft")
                        || isCandidateOperation(label) && tool.operationType().equals("candidate")
                        || isProposalOperation(label) && tool.operationType().equals("proposal")));
    }

    private boolean doesNotUseDomainItemFallback(McpToolCandidate tool) {
        String generated = tool.name() + "\n" + tool.purpose() + "\n" + tool.relatedApi();
        return !generated.contains("DomainItem")
                && !generated.contains("domain-items")
                && !generated.contains("executeDomainItem");
    }

    private String approvalFor(String label, OperationBuckets buckets) {
        return buckets.isApprovalRequired(label) ? "必須" : "不要";
    }

    private String auditFor(String label, OperationBuckets buckets) {
        return buckets.isAuditRequired(label) || buckets.isApprovalRequired(label) ? "必須" : "推奨";
    }

    private String purpose(DomainIdentity domain, String label, String action) {
        return "主ドメインtool(" + domain.rawName() + "): " + action + " / v2分類: " + normalizeOperationLabel(label);
    }

    private boolean isReadDetailOperation(String label) {
        return containsAny(label, "詳細", "参照", "取得", "確認") && !isSummaryOperation(label);
    }

    private boolean isSummaryOperation(String label) {
        return containsAny(label, "要約", "整理", "サマリ");
    }

    private boolean isCreateOperation(String label) {
        return containsAny(label, "登録", "作成", "追加", "受付");
    }

    private boolean isDraftOperation(String label) {
        return containsAny(label, "下書き", "文案", "草案", "返信案");
    }

    private boolean isCandidateOperation(String label) {
        return containsAny(label, "候補", "抽出", "選定");
    }

    private boolean isProposalOperation(String label) {
        return containsAny(label, "更新案", "変更案", "修正案", "提案", "更新", "変更", "編集", "分類");
    }

    private boolean isDeleteOperation(String label) {
        return containsAny(label, "削除", "廃棄", "取消", "廃止");
    }

    private boolean isApprovalAction(String label) {
        return containsAny(label, "承認", "却下", "申請");
    }

    private boolean isNotificationOperation(String label) {
        return containsAny(label, "通知", "送信", "公開", "外部共有", "共有リンク");
    }

    private boolean containsAny(String label, String... keywords) {
        String normalized = label == null ? "" : label.toLowerCase(Locale.ROOT);
        for (String keyword : keywords) {
            if (normalized.contains(keyword.toLowerCase(Locale.ROOT))) {
                return true;
            }
        }
        return false;
    }

    private boolean isDomainItemFallback(String path, String className) {
        return "domain-items".equals(path) || "DomainItem".equals(className);
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return "";
    }

    private static String normalizeOperationLabel(String label) {
        if (label == null) {
            return "";
        }
        return label.strip()
                .replaceFirst("^[\\-・*\\s]+", "")
                .replaceFirst("^\\d+[.)、]\\s*", "")
                .strip();
    }

    private record DomainIdentity(String rawName, String path, String className, String pluralClass) {
    }

    private record OperationBuckets(
            List<String> allowedAiOperations,
            List<String> readOnlyOperations,
            List<String> writeOperations,
            List<String> approvalRequiredOperations,
            List<String> auditLogRequiredOperations
    ) {
        private static OperationBuckets from(BlueprintInput input) {
            if (input == null) {
                return new OperationBuckets(List.of(), List.of(), List.of(), List.of(), List.of());
            }
            return new OperationBuckets(
                    operationLabels(input.getAllowedAiOperations()),
                    operationLabels(input.getReadOnlyOperations()),
                    operationLabels(input.getWriteOperations()),
                    operationLabels(input.getApprovalRequiredOperations()),
                    operationLabels(input.getAuditLogRequiredOperations())
            );
        }

        private boolean isApprovalRequired(String label) {
            return containsMatchingLabel(approvalRequiredOperations, label);
        }

        private boolean isAuditRequired(String label) {
            return containsMatchingLabel(auditLogRequiredOperations, label);
        }

        private static List<String> operationLabels(String text) {
            if (text == null || text.isBlank()) {
                return List.of();
            }
            Set<String> labels = new LinkedHashSet<>();
            for (String line : text.split("\\R")) {
                String label = normalizeOperationLabel(line);
                if (!label.isBlank()) {
                    labels.add(label);
                }
            }
            return List.copyOf(labels);
        }

        private static boolean containsMatchingLabel(List<String> labels, String target) {
            String normalizedTarget = normalizeOperationLabel(target);
            if (normalizedTarget.isBlank()) {
                return false;
            }
            return labels.stream().anyMatch(label ->
                    label.equals(normalizedTarget)
                            || label.contains(normalizedTarget)
                            || normalizedTarget.contains(label)
            );
        }
    }

    private List<McpToolCandidate> primaryDomainTools(
            String domainClass,
            String domainPath,
            String domainName,
            Set<OperationType> operations
    ) {
        String domainPluralClass = toPluralClass(domainPath, domainClass);
        List<McpToolCandidate> tools = new ArrayList<>();
        if (operations.contains(OperationType.SEARCH)) {
            tools.add(tool("search" + domainPluralClass, scopedPurpose("主ドメインtool", domainName, "検索"),
                    "keyword", domainClass + "SummaryResponse", "/api/" + domainPath, "read", "AI実行可", "不要", "推奨"));
        }
        if (operations.contains(OperationType.READ)) {
            tools.add(tool("get" + domainClass + "Detail", scopedPurpose("主ドメインtool", domainName, "詳細取得"),
                    "id", domainClass + "Response", "/api/" + domainPath + "/{id}", "read", "AI実行可", "不要", "推奨"));
        }
        if (operations.contains(OperationType.SUMMARY)) {
            tools.add(tool("summarize" + domainClass + "Interactions", scopedPurpose("主ドメインtool", domainName, "要約"),
                    "id", domainClass + "SummaryResponse", "/api/" + domainPath + "/{id}/summary", "read",
                    "AI実行可（機密情報注意）", "不要", "推奨"));
        }
        if (operations.contains(OperationType.CREATE)) {
            tools.add(tool("create" + domainClass + "Draft", scopedPurpose("主ドメインtool", domainName, "作成下書き"),
                    "payload", domainClass + "CreateRequest", "/api/" + domainPath, "write",
                    "下書き作成または承認後実行", "検討", "必須"));
        }
        if (operations.contains(OperationType.UPDATE)) {
            tools.add(tool("propose" + domainClass + "Update", scopedPurpose("主ドメインtool", domainName, "更新提案"),
                    "payload", domainClass + "UpdateRequest", "/api/" + domainPath + "/{id}", "write",
                    "提案のみまたは承認後実行", "原則必要", "必須"));
        }
        if (operations.contains(OperationType.DELETE)) {
            tools.add(tool("request" + domainPluralClass + "DeletionApproval",
                    scopedPurpose("主ドメインtool", domainName, "削除承認依頼"),
                    "id", "ApprovalResponse", "/api/" + domainPath + "/{id}", "write", "AI実行不可", "必須", "必須"));
        }
        if (operations.contains(OperationType.APPROVAL)) {
            tools.add(tool("requestApprovalFor" + domainPluralClass, scopedPurpose("主ドメインtool", domainName, "承認依頼"),
                    "approvalRequest", "ApprovalResponse", "/api/" + domainPath + "/{id}/approval-requests", "write",
                    "承認後実行", "必須", "必須"));
        }
        return tools;
    }

    private List<McpToolCandidate> relatedDomainReferenceTools(
            String relatedDomainClass,
            String relatedDomainPath,
            String relatedDomainName,
            List<ApiEndpointCandidate> endpoints
    ) {
        List<McpToolCandidate> tools = new ArrayList<>();
        if (hasEndpoint(endpoints, "/api/" + relatedDomainPath)) {
            tools.add(tool("search" + relatedDomainClass + "References",
                    scopedPurpose("関連ドメイン参照tool", relatedDomainName, "参照一覧取得"),
                    "keyword", relatedDomainClass + "ReferenceSummaryResponse", "/api/" + relatedDomainPath,
                    "read", "AI実行可（参照・要約のみ）", "不要", "推奨"));
        }
        if (hasEndpoint(endpoints, "/api/" + relatedDomainPath + "/{id}")) {
            tools.add(tool("get" + relatedDomainClass + "ReferenceDetail",
                    scopedPurpose("関連ドメイン参照tool", relatedDomainName, "参照詳細取得"),
                    "id", relatedDomainClass + "ReferenceResponse", "/api/" + relatedDomainPath + "/{id}",
                    "read", "AI実行可（参照・要約のみ）", "不要", "推奨"));
        }
        return tools;
    }

    private List<ApiMcpMapping> scopedMappings(List<McpToolCandidate> tools, List<ApiEndpointCandidate> endpoints) {
        List<ApiMcpMapping> mappings = new ArrayList<>();
        for (McpToolCandidate tool : tools) {
            ApiEndpointCandidate matchedEndpoint = endpoints.stream()
                    .filter(e -> e.path().equals(tool.relatedApi()))
                    .findFirst()
                    .orElse(null);
            String matchedApi = matchedEndpoint == null ? tool.relatedApi() : matchedEndpoint.path();
            mappings.add(new ApiMcpMapping(matchedApi, tool.name(), mappingNote(tool, matchedEndpoint)));
        }
        return mappings;
    }

    private String mappingNote(McpToolCandidate tool, ApiEndpointCandidate endpoint) {
        String apiBoundary = endpoint == null || endpoint.domainRole().isBlank() ? "API境界未指定" : endpoint.domainRole();
        String domainName = endpoint == null || endpoint.domainName().isBlank() ? "" : "(" + endpoint.domainName() + ")";
        return apiBoundary + domainName
                + " / AI: " + tool.aiExecutionPolicy()
                + " / Approval: " + tool.approvalRequired()
                + " / Audit: " + tool.auditLogRequired();
    }

    private boolean hasEndpoint(List<ApiEndpointCandidate> endpoints, String path) {
        return endpoints.stream().anyMatch(e -> e.path().equals(path));
    }

    private String scopedPurpose(String boundary, String domainName, String purpose) {
        String domainLabel = domainName == null || domainName.isBlank() ? "" : "(" + domainName + ")";
        return boundary + domainLabel + ": " + purpose;
    }

    private String primaryDomainText(NormalizedBlueprintInput normalizedInput) {
        if (normalizedInput.primaryDomain() != null && !normalizedInput.primaryDomain().isBlank()) {
            return normalizedInput.primaryDomain();
        }
        return normalizedInput.targetDomainText();
    }

    private String toPluralClass(String domainPath, String domainClass) {
        if (domainPath == null || domainPath.isBlank()) {
            return domainClass;
        }
        String[] tokens = domainPath.split("[^A-Za-z0-9]+");
        StringBuilder sb = new StringBuilder();
        for (String token : tokens) {
            if (token.isBlank()) {
                continue;
            }
            String lower = token.toLowerCase();
            sb.append(Character.toUpperCase(lower.charAt(0))).append(lower.substring(1));
        }
        return sb.isEmpty() ? domainClass : sb.toString();
    }

    private McpToolCandidate tool(
            String name,
            String purpose,
            String args,
            String returnValue,
            String relatedApi,
            String operationType,
            String policy,
            String approval,
            String audit
    ) {
        return new McpToolCandidate(name, purpose, args, returnValue, relatedApi, operationType, policy, approval, audit);
    }
}

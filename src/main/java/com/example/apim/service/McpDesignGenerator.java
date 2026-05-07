package com.example.apim.service;

import com.example.apim.model.ApiEndpointCandidate;
import com.example.apim.model.ApiMcpMapping;
import com.example.apim.model.McpPromptCandidate;
import com.example.apim.model.McpResourceCandidate;
import com.example.apim.model.McpToolCandidate;
import com.example.apim.model.NormalizedBlueprintInput;
import com.example.apim.support.DomainNameNormalizer;
import com.example.apim.support.OperationType;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
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
                "業務要件からAPI/MCP境界を整理する",
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
                scopedPurpose("横断prompt", primaryDomain, "複数ドメイン利用場面とAPI/MCP境界を整理する"),
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

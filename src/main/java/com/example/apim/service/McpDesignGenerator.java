package com.example.apim.service;

import com.example.apim.model.ApiEndpointCandidate;
import com.example.apim.model.ApiMcpMapping;
import com.example.apim.model.McpPromptCandidate;
import com.example.apim.model.McpResourceCandidate;
import com.example.apim.model.McpToolCandidate;
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
        String domainPluralClass = toPluralClass(domainPath, domainClass);
        List<McpToolCandidate> tools = new ArrayList<>();
        List<McpResourceCandidate> resources = new ArrayList<>();
        List<McpPromptCandidate> prompts = new ArrayList<>();
        List<ApiMcpMapping> mappings = new ArrayList<>();

        if (operations.contains(OperationType.SEARCH)) {
            tools.add(tool("search" + domainPluralClass, "検索", "keyword", domainClass + "SummaryResponse",
                    "/api/" + domainPath, "read", "AI実行可", "不要", "推奨"));
        }
        if (operations.contains(OperationType.READ)) {
            tools.add(tool("get" + domainClass + "Detail", "詳細取得", "id", domainClass + "Response",
                    "/api/" + domainPath + "/{id}", "read", "AI実行可", "不要", "推奨"));
        }
        if (operations.contains(OperationType.SUMMARY)) {
            tools.add(tool("summarize" + domainClass + "Interactions", "要約", "id", domainClass + "SummaryResponse",
                    "/api/" + domainPath + "/{id}/summary", "read", "AI実行可（機密情報注意）", "不要", "推奨"));
        }
        if (operations.contains(OperationType.CREATE)) {
            tools.add(tool("create" + domainClass + "Draft", "作成下書き", "payload", domainClass + "CreateRequest",
                    "/api/" + domainPath, "write", "下書き作成または承認後実行", "検討", "必須"));
        }
        if (operations.contains(OperationType.UPDATE)) {
            tools.add(tool("propose" + domainClass + "Update", "更新提案", "payload", domainClass + "UpdateRequest",
                    "/api/" + domainPath + "/{id}", "write", "提案のみまたは承認後実行", "原則必要", "必須"));
        }
        if (operations.contains(OperationType.DELETE)) {
            tools.add(tool("request" + domainPluralClass + "DeletionApproval", "削除承認依頼", "id", "ApprovalResponse",
                    "/api/" + domainPath + "/{id}", "write", "AI実行不可", "必須", "必須"));
        }
        if (operations.contains(OperationType.APPROVAL)) {
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
            String matchedApi = endpoints.stream()
                    .filter(e -> e.path().equals(tool.relatedApi()))
                    .map(ApiEndpointCandidate::path)
                    .findFirst()
                    .orElse(tool.relatedApi());
            mappings.add(new ApiMcpMapping(matchedApi, tool.name(), tool.aiExecutionPolicy()));
        }

        return new McpDesignResult(tools, resources, prompts, mappings);
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

package com.example.apim.service;

import com.example.apim.model.*;
import org.springframework.stereotype.Service;

@Service
public class MarkdownDocumentGenerator {

    public String generate(BlueprintInput input, BlueprintResult result) {
        return generate(new BlueprintInputNormalizer().normalize(input), result);
    }

    public String generate(NormalizedBlueprintInput normalizedInput, BlueprintResult result) {
        BlueprintInput input = normalizedInput.originalInput();
        StringBuilder sb = new StringBuilder();
        sb.append("# api-mcp-blueprint.md\n\n");
        appendTitle(sb, input, normalizedInput);
        appendInputSummary(sb, input, normalizedInput);
        appendApiDesignSummary(sb, result);
        appendEndpoints(sb, result);
        appendDtoCandidates(sb, result);
        appendControllerSkeleton(sb, result);
        appendMcpTools(sb, result);
        appendMcpResources(sb, result);
        appendMcpPrompts(sb, result);
        appendApiMcpMappings(sb, result);
        appendSecurityNotes(sb, result);
        appendOutOfScope(sb);
        appendNextSteps(sb);
        return sb.toString();
    }

    private void appendTitle(StringBuilder sb, BlueprintInput input, NormalizedBlueprintInput normalizedInput) {
        sb.append("## 1. 設計対象概要\n")
                .append(buildOverviewSummary(input, normalizedInput)).append("\n\n")
                .append("## 2. 入力要件サマリー\n");
    }

    private void appendInputSummary(StringBuilder sb, BlueprintInput input, NormalizedBlueprintInput normalizedInput) {
        sb.append("- 対象システム種別: ").append(joinOrDefault(normalizedInput.systemTypes(), "未指定")).append("\n")
                .append("- 主ドメイン: ").append(valueOrDefault(normalizedInput.primaryDomain(), "未指定")).append("\n")
                .append("- 関連ドメイン: ").append(joinOrDefault(normalizedInput.relatedDomains(), "なし")).append("\n")
                .append("- 正規化後ドメイン一覧: ").append(joinOrDefault(normalizedInput.allDomains(), "未指定")).append("\n")
                .append("- 対象ドメイン: ").append(valueOrDefault(normalizedInput.targetDomainText(), input.getTargetDomain())).append("\n")
                .append("- ユーザー種別: ").append(input.getUserTypes().replace("\n", " / ")).append("\n")
                .append("- 必要な操作: ").append(input.getRequiredOperations().replace("\n", " / ")).append("\n\n")
                .append("### v2確認結果からの操作分類\n")
                .append("- AI許可操作: ").append(multilineOrDefault(input.getAllowedAiOperations(), "未指定")).append("\n")
                .append("- 読み取り専用操作: ").append(multilineOrDefault(input.getReadOnlyOperations(), "なし")).append("\n")
                .append("- 書き込み・状態変更操作: ").append(multilineOrDefault(input.getWriteOperations(), "なし")).append("\n")
                .append("- 承認必須操作: ").append(multilineOrDefault(input.getApprovalRequiredOperations(), "なし")).append("\n")
                .append("- 監査ログ必須操作: ").append(multilineOrDefault(input.getAuditLogRequiredOperations(), "なし")).append("\n\n")
                .append("### v2確認結果からの利用者・認証\n")
                .append("- 想定認証方式: ").append(valueOrDefault(input.getAuthenticationMethod(), "未指定")).append("\n")
                .append("- 想定利用者: ").append(valueOrDefault(input.getTargetUsers(), "未指定")).append("\n\n")
                .append("## 3. 想定ユーザー・ロール\n")
                .append(input.getUserTypes()).append("\n\n");
    }

    private void appendApiDesignSummary(StringBuilder sb, BlueprintResult result) {
        sb.append("## 4. API設計サマリー\n")
                .append(result.getApiDesignSummary()).append("\n\n")
                .append("## 5. RESTエンドポイント一覧\n\n")
                .append("| Method | Path | Purpose | Approval | Audit |\n")
                .append("|---|---|---|---|---|\n");
        for (ApiEndpointCandidate endpoint : result.getApiEndpoints()) {
            sb.append("| ").append(endpoint.httpMethod()).append(" | ")
                    .append(endpoint.path()).append(" | ")
                    .append(endpoint.purpose()).append(" | ")
                    .append(endpoint.approvalRequired()).append(" | ")
                    .append(endpoint.auditLogRequired()).append(" |\n");
        }
        sb.append('\n');
    }

    private void appendEndpoints(StringBuilder sb, BlueprintResult result) {
        sb.append("## 6. Request / Response DTO候補\n");
        for (DtoCandidate dto : result.getDtoCandidates()) {
            sb.append("- ").append(dto.getName()).append(": ").append(dto.getPurpose()).append('\n');
        }
        sb.append('\n');
    }

    private void appendDtoCandidates(StringBuilder sb, BlueprintResult result) {
        sb.append("## 7. Spring Controller雛形\n\n```java\n")
                .append(result.getControllerSkeleton().sourceCode())
                .append("\n```\n\n");
    }

    private void appendControllerSkeleton(StringBuilder sb, BlueprintResult result) {
        sb.append("## 8. MCP tools一覧\n\n")
                .append("| Tool | Type | Policy | Approval |\n")
                .append("|---|---|---|---|\n");
        for (McpToolCandidate tool : result.getMcpTools()) {
            sb.append("| ").append(tool.name()).append(" | ")
                    .append(tool.operationType()).append(" | ")
                    .append(tool.aiExecutionPolicy()).append(" | ")
                    .append(tool.approvalRequired()).append(" |\n");
        }
        sb.append('\n');
    }

    private void appendMcpTools(StringBuilder sb, BlueprintResult result) {
        sb.append("## 9. MCP resources一覧\n");
        for (McpResourceCandidate resource : result.getMcpResources()) {
            sb.append("- ").append(resource.name()).append(": ").append(resource.purpose())
                    .append(" (").append(resource.scope()).append(")\n");
        }
        sb.append('\n');
    }

    private void appendMcpResources(StringBuilder sb, BlueprintResult result) {
        sb.append("## 10. MCP prompts一覧\n");
        for (McpPromptCandidate prompt : result.getMcpPrompts()) {
            sb.append("- ").append(prompt.name()).append(": ").append(prompt.purpose()).append('\n');
        }
        sb.append('\n');
    }

    private void appendMcpPrompts(StringBuilder sb, BlueprintResult result) {
        sb.append("## 11. API / MCP tool 対応表\n\n")
                .append("| API | MCP Tool | Notes |\n")
                .append("|---|---|---|\n");
        for (ApiMcpMapping mapping : result.getApiMcpMappings()) {
            sb.append("| ").append(mapping.apiPath()).append(" | ")
                    .append(mapping.toolName()).append(" | ")
                    .append(mapping.notes()).append(" |\n");
        }
        sb.append('\n');
    }

    private void appendApiMcpMappings(StringBuilder sb, BlueprintResult result) {
        sb.append("## 12. 権限・承認・監査ログ設計\n")
                .append("書き込み系操作は承認要否を設定し、AI操作は必ず区別して監査ログに記録する。\n\n");
    }

    private void appendSecurityNotes(StringBuilder sb, BlueprintResult result) {
        sb.append("## 13. セキュリティ注意点\n");
        for (SecurityNote note : result.getSecurityNotes()) {
            sb.append("- [").append(note.category()).append("] ").append(note.message()).append('\n');
        }
        sb.append('\n');
    }

    private void appendOutOfScope(StringBuilder sb) {
        sb.append("## 14. 後続フェーズで具体化する事項\n")
                .append("以下は初期設計時点で実装有無・方式を確定せず、後続フェーズで要件、運用条件、セキュリティ方針に応じて具体化する。\n")
                .append("この設計書を後続AIへ渡す場合も、これらを実装禁止事項ではなく、追加設計・実装判断が必要な事項として扱う。\n")
                .append("- MCPサーバーとしての実行形態\n")
                .append("- 外部LLM API連携\n")
                .append("- DB永続化\n")
                .append("- 認証認可方式\n")
                .append("- OpenAPI定義の生成・公開範囲\n\n");
    }

    private void appendNextSteps(StringBuilder sb) {
        sb.append("## 15. 次の実装ステップ\n")
                .append("APIM-005の実装内容をレビューし、APIM-006以降で改善する。");
    }

    private String joinOrDefault(java.util.List<String> values, String defaultValue) {
        if (values == null || values.isEmpty()) {
            return defaultValue;
        }
        return String.join(" / ", values);
    }

    private String valueOrDefault(String value, String defaultValue) {
        return value == null || value.isBlank() ? defaultValue : value;
    }

    private String multilineOrDefault(String value, String defaultValue) {
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        java.util.List<String> values = new java.util.ArrayList<>();
        for (String line : value.split("\\R")) {
            String normalized = line.trim();
            if (normalized.startsWith("- ")) {
                normalized = normalized.substring(2).trim();
            }
            if (!normalized.isBlank()) {
                values.add(normalized);
            }
        }
        if (values.isEmpty()) {
            return defaultValue;
        }
        return String.join(" / ", values);
    }

    private String buildOverviewSummary(BlueprintInput input, NormalizedBlueprintInput normalizedInput) {
        String systemTypeText = joinOrDefault(normalizedInput.systemTypes(), "業務システム");
        String primaryDomainText = valueOrDefault(normalizedInput.primaryDomain(), "対象ドメイン");
        String relatedDomainText = normalizedInput.relatedDomains().isEmpty()
                ? ""
                : "（関連ドメイン: " + String.join(" / ", normalizedInput.relatedDomains()) + "）";
        String operations = normalizeOperationsForOverview(input.getRequiredOperations());
        String operationClause = operations.isEmpty()
                ? ""
                : "必要な操作として" + operations + "を想定し、";
        return "本設計書は、入力要件に基づき、" + systemTypeText + "の" + primaryDomainText
                + relatedDomainText + "を対象に、" + operationClause
                + "API、MCP tools、resources、prompts、権限・承認・監査設計を整理した設計成果物である。";
    }

    private String normalizeOperationsForOverview(String requiredOperations) {
        if (requiredOperations == null || requiredOperations.isBlank()) {
            return "";
        }
        java.util.List<String> operations = new java.util.ArrayList<>();
        for (String line : requiredOperations.split("\\R")) {
            String normalized = line.trim();
            if (normalized.startsWith("- ")) {
                normalized = normalized.substring(2).trim();
            }
            if (!normalized.isBlank()) {
                operations.add(normalized);
            }
        }
        if (operations.isEmpty()) {
            return requiredOperations.trim();
        }
        return String.join("、", operations);
    }
}

package com.example.apim.service;

import com.example.apim.model.*;
import org.springframework.stereotype.Service;

@Service
public class MarkdownDocumentGenerator {

    public String generate(BlueprintInput input, BlueprintResult result) {
        StringBuilder sb = new StringBuilder();
        sb.append("# api-mcp-blueprint.md\n\n");
        appendTitle(sb);
        appendInputSummary(sb, input);
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

    private void appendTitle(StringBuilder sb) {
        sb.append("## 1. 設計対象概要\n")
                .append("APIM for Spring の初期MVP向け設計成果物。\n\n")
                .append("## 2. 入力要件サマリー\n");
    }

    private void appendInputSummary(StringBuilder sb, BlueprintInput input) {
        sb.append("- 対象ドメイン: ").append(input.getTargetDomain()).append("\n")
                .append("- ユーザー種別: ").append(input.getUserTypes().replace("\n", " / ")).append("\n")
                .append("- 必要な操作: ").append(input.getRequiredOperations().replace("\n", " / ")).append("\n\n")
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
        sb.append("## 14. 初期MVPで実装しないこと\n")
                .append("- 完全動作するMCPサーバー\n")
                .append("- 外部LLM API連携\n")
                .append("- DB永続化\n")
                .append("- 認証認可の本格実装\n")
                .append("- OpenAPI完全生成\n\n");
    }

    private void appendNextSteps(StringBuilder sb) {
        sb.append("## 15. 次の実装ステップ\n")
                .append("APIM-005の実装内容をレビューし、APIM-006以降で改善する。");
    }
}

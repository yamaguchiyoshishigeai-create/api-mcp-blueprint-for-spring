package com.example.apim.service;

import com.example.apim.model.*;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

@Service
public class MarkdownDocumentGenerator {

    public String generate(BlueprintInput input, BlueprintResult result) {
        return generate(new BlueprintInputNormalizer().normalize(input), result);
    }

    public String generate(NormalizedBlueprintInput normalizedInput, BlueprintResult result) {
        BlueprintInput input = normalizedInput.originalInput();
        StringBuilder sb = new StringBuilder();
        sb.append("# api-mcp-blueprint.md\n\n");
        appendOverview(sb, input, normalizedInput);
        appendInputSummary(sb, input, normalizedInput);
        appendBusinessStructure(sb, input, normalizedInput);
        appendSafetyClassification(sb, input);
        appendAmbiguities(sb, input);
        appendRestApiCandidates(sb, result);
        appendMcpTools(sb, result);
        appendMcpResources(sb, result);
        appendMcpPrompts(sb, result);
        appendApiMcpMappings(sb, result);
        appendSecurityDesign(sb, result);
        appendImplementationTasks(sb, result);
        return sb.toString();
    }

    private void appendOverview(StringBuilder sb, BlueprintInput input, NormalizedBlueprintInput normalizedInput) {
        sb.append("## 1. 設計対象概要\n")
                .append(buildOverviewSummary(input, normalizedInput)).append("\n\n");
    }

    private void appendInputSummary(StringBuilder sb, BlueprintInput input, NormalizedBlueprintInput normalizedInput) {
        sb.append("## 2. 入力自由文サマリー\n")
                .append("- 対象システム種別: ").append(joinOrDefault(normalizedInput.systemTypes(), "未指定")).append("\n")
                .append("- 主ドメイン: ").append(valueOrDefault(normalizedInput.primaryDomain(), "未指定")).append("\n")
                .append("- 関連ドメイン: ").append(joinOrDefault(normalizedInput.relatedDomains(), "なし")).append("\n")
                .append("- 正規化後ドメイン一覧: ").append(joinOrDefault(normalizedInput.allDomains(), "未指定")).append("\n")
                .append("- 対象ドメイン: ").append(valueOrDefault(normalizedInput.targetDomainText(), input.getTargetDomain())).append("\n")
                .append("- 入力要件: ").append(valueOrDefault(oneLine(input.getBusinessRequirements()), "未指定")).append("\n")
                .append("- 必要な操作: ").append(valueOrDefault(oneLine(input.getRequiredOperations()), "未指定")).append("\n")
                .append("- 想定認証方式: ").append(valueOrDefault(input.getAuthenticationMethod(), "未指定")).append("\n")
                .append("- 想定利用者: ").append(valueOrDefault(input.getTargetUsers(), oneLine(input.getUserTypes()))).append("\n\n");
    }

    private void appendBusinessStructure(StringBuilder sb, BlueprintInput input, NormalizedBlueprintInput normalizedInput) {
        sb.append("## 3. 抽出された業務構造\n\n");
        appendDomains(sb, input, normalizedInput);
        appendBusinessObjects(sb, input);
        appendActors(sb, input);
        appendOperations(sb, input);
        appendRelationships(sb, input);
    }

    private void appendDomains(StringBuilder sb, BlueprintInput input, NormalizedBlueprintInput normalizedInput) {
        sb.append("### 3.1 業務領域 (domains)\n\n");
        if (!input.getV2Domains().isEmpty()) {
            sb.append("| ID | Name | Role | Description |\n")
                    .append("|---|---|---|---|\n");
            for (BlueprintInput.V2Domain domain : input.getV2Domains()) {
                sb.append("| ").append(table(domain.id())).append(" | ")
                        .append(table(domain.name())).append(" | ")
                        .append(table(valueOrDefault(domain.role(), "未指定"))).append(" | ")
                        .append(table(valueOrDefault(domain.description(), "未指定"))).append(" |\n");
            }
            sb.append('\n');
            return;
        }

        sb.append("- 主ドメイン: ").append(valueOrDefault(normalizedInput.primaryDomain(), "未指定")).append("\n")
                .append("- 関連ドメイン: ").append(joinOrDefault(normalizedInput.relatedDomains(), "なし")).append("\n")
                .append("- 全ドメイン: ").append(joinOrDefault(normalizedInput.allDomains(), "未指定")).append("\n\n");
    }

    private void appendBusinessObjects(StringBuilder sb, BlueprintInput input) {
        sb.append("### 3.2 業務オブジェクト (businessObjects)\n\n");
        if (input.getV2BusinessObjects().isEmpty()) {
            sb.append("- v2業務オブジェクトは未指定。旧入力では対象ドメインと操作からAPI候補を生成する。\n\n");
            return;
        }

        sb.append("| ID | Name | Domain | Sensitivity | Data Categories |\n")
                .append("|---|---|---|---|---|\n");
        Map<String, String> domainNames = domainNamesById(input);
        for (BlueprintInput.V2BusinessObject object : input.getV2BusinessObjects()) {
            sb.append("| ").append(table(object.id())).append(" | ")
                    .append(table(object.name())).append(" | ")
                    .append(table(valueOrDefault(domainNames.get(object.domainId()), object.domainId()))).append(" | ")
                    .append(table(valueOrDefault(object.sensitivity(), "未指定"))).append(" | ")
                    .append(table(joinOrDefault(object.dataCategories(), "なし"))).append(" |\n");
        }
        sb.append('\n');
    }

    private void appendActors(StringBuilder sb, BlueprintInput input) {
        sb.append("### 3.3 利用者・ロール (actors)\n\n");
        if (!input.getV2Actors().isEmpty()) {
            sb.append("| ID | Name | Type |\n")
                    .append("|---|---|---|\n");
            for (BlueprintInput.V2Actor actor : input.getV2Actors()) {
                sb.append("| ").append(table(actor.id())).append(" | ")
                        .append(table(actor.name())).append(" | ")
                        .append(table(valueOrDefault(actor.actorType(), "未指定"))).append(" |\n");
            }
            sb.append('\n');
            return;
        }

        for (String actor : lines(input.getUserTypes())) {
            sb.append("- ").append(actor).append('\n');
        }
        sb.append('\n');
    }

    private void appendOperations(StringBuilder sb, BlueprintInput input) {
        sb.append("### 3.4 業務操作一覧 (operations)\n\n");
        if (input.getV2Operations().isEmpty()) {
            for (String operation : lines(input.getRequiredOperations())) {
                sb.append("- ").append(operation).append('\n');
            }
            sb.append('\n');
            return;
        }

        sb.append("| ID | Label | Intent | Execution Mode | Target Object | Actor | AI Permission | Approval | Audit | Risk | External | State Change | Output |\n")
                .append("|---|---|---|---|---|---|---|---|---|---|---|---|---|\n");
        Map<String, String> objectNames = objectNamesById(input);
        Map<String, String> actorNames = actorNamesById(input);
        for (BlueprintInput.V2Operation operation : input.getV2Operations()) {
            sb.append("| ").append(table(operation.id())).append(" | ")
                    .append(table(operation.label())).append(" | ")
                    .append(table(valueOrDefault(operation.intent(), "未指定"))).append(" | ")
                    .append(table(valueOrDefault(operation.executionMode(), "未指定"))).append(" | ")
                    .append(table(namesFor(operation.objectIds(), objectNames))).append(" | ")
                    .append(table(namesFor(operation.actorIds(), actorNames))).append(" | ")
                    .append(table(valueOrDefault(operation.aiPermission(), "未指定"))).append(" | ")
                    .append(operation.approvalRequired() ? "必須" : "不要").append(" | ")
                    .append(table(valueOrDefault(operation.auditLogRequired(), "未指定"))).append(" | ")
                    .append(table(valueOrDefault(operation.riskLevel(), "未指定"))).append(" | ")
                    .append(operation.externalAction()).append(" | ")
                    .append(operation.stateChanging()).append(" | ")
                    .append(table(valueOrDefault(operation.outputType(), "未指定"))).append(" |\n");
        }
        sb.append('\n');
    }

    private void appendRelationships(StringBuilder sb, BlueprintInput input) {
        sb.append("### 3.5 関係性 (relationships)\n\n");
        if (input.getV2Relationships().isEmpty()) {
            sb.append("- v2関係性は未指定。\n\n");
            return;
        }

        sb.append("| ID | Source | Target | Type | Description |\n")
                .append("|---|---|---|---|---|\n");
        Map<String, String> objectNames = objectNamesById(input);
        for (BlueprintInput.V2Relationship relationship : input.getV2Relationships()) {
            sb.append("| ").append(table(relationship.id())).append(" | ")
                    .append(table(valueOrDefault(objectNames.get(relationship.fromObjectId()), relationship.fromObjectId()))).append(" | ")
                    .append(table(valueOrDefault(objectNames.get(relationship.toObjectId()), relationship.toObjectId()))).append(" | ")
                    .append(table(valueOrDefault(relationship.type(), "未指定"))).append(" | ")
                    .append(table(valueOrDefault(relationship.description(), "未指定"))).append(" |\n");
        }
        sb.append('\n');
    }

    private void appendSafetyClassification(StringBuilder sb, BlueprintInput input) {
        sb.append("## 4. AI支援・承認・監査の分類\n\n");
        appendOperationBucket(sb, "### 4.1 AI支援可能操作", input,
                operation -> "allowed".equals(operation.aiPermission())
                        && !operation.approvalRequired()
                        && !operation.externalAction()
                        && !operation.stateChanging()
                        && !isAiDirectExecutionForbidden(operation),
                lines(input.getAllowedAiOperations()));
        appendOperationBucket(sb, "### 4.2 人間承認必須操作", input,
                BlueprintInput.V2Operation::approvalRequired,
                lines(input.getApprovalRequiredOperations()));
        appendOperationBucket(sb, "### 4.3 監査ログ必須操作", input,
                operation -> "required".equals(operation.auditLogRequired()) || operation.approvalRequired(),
                lines(input.getAuditLogRequiredOperations()));
        appendOperationBucket(sb, "### 4.4 AI直接実行不可操作", input,
                this::isAiDirectExecutionForbidden,
                aiForbiddenLegacyOperations(input));
    }

    private void appendOperationBucket(StringBuilder sb, String heading, BlueprintInput input,
                                       Predicate<BlueprintInput.V2Operation> filter, List<String> fallbackLabels) {
        sb.append(heading).append("\n\n");
        List<BlueprintInput.V2Operation> operations = input.getV2Operations().stream()
                .filter(filter)
                .toList();
        if (!operations.isEmpty()) {
            sb.append("| Operation | AI実行方針 (AI Policy) | 承認要否 | 監査ログ要否 | Risk | Human Confirmation Boundary |\n")
                    .append("|---|---|---|---|---|---|\n");
            for (BlueprintInput.V2Operation operation : operations) {
                sb.append("| ").append(table(operation.label())).append(" | ")
                        .append(table(aiPolicyText(operation))).append(" | ")
                        .append(operation.approvalRequired() ? "必須" : "不要").append(" | ")
                        .append(table(auditText(operation))).append(" | ")
                        .append(table(valueOrDefault(operation.riskLevel(), "未指定"))).append(" | ")
                        .append(table(humanBoundary(operation))).append(" |\n");
            }
            sb.append('\n');
            return;
        }

        if (!input.getV2Operations().isEmpty()) {
            sb.append("- 該当なし\n\n");
            return;
        }

        if (fallbackLabels.isEmpty()) {
            sb.append("- 該当なし\n\n");
            return;
        }
        for (String label : fallbackLabels) {
            sb.append("- ").append(label).append('\n');
        }
        sb.append('\n');
    }

    private void appendAmbiguities(StringBuilder sb, BlueprintInput input) {
        sb.append("## 5. 曖昧点・確認事項 (ambiguities)\n\n");
        if (input.getV2Ambiguities().isEmpty()) {
            sb.append("- v2曖昧点は未指定。後続設計で承認者、外部送信、確定処理の境界を確認する。\n\n");
            return;
        }

        sb.append("| ID | Category | Description | Impact | Confirmation Item |\n")
                .append("|---|---|---|---|---|\n");
        for (BlueprintInput.V2Ambiguity ambiguity : input.getV2Ambiguities()) {
            sb.append("| ").append(table(ambiguity.id())).append(" | ")
                    .append(table(valueOrDefault(ambiguity.type(), "未指定"))).append(" | ")
                    .append(table(valueOrDefault(ambiguity.message(), "未指定"))).append(" | ")
                    .append(table(valueOrDefault(ambiguity.severity(), "未指定"))).append(" | ")
                    .append(table(valueOrDefault(ambiguity.defaultHandling(), "後続設計で確認する"))).append(" |\n");
        }
        sb.append('\n');
    }

    private void appendRestApiCandidates(StringBuilder sb, BlueprintResult result) {
        sb.append("## 6. REST API候補\n\n")
                .append(valueOrDefault(result.getApiDesignSummary(), "業務構造と安全条件からREST API候補を生成した。"))
                .append("\n\n")
                .append("| Method | Path | Purpose | Request DTO | Response DTO | Auth / Authorization | 承認要否 (Approval) | 監査ログ要否 (Audit) | Related Business Object / Operation |\n")
                .append("|---|---|---|---|---|---|---|---|---|\n");
        for (ApiEndpointCandidate endpoint : result.getApiEndpoints()) {
            sb.append("| ").append(table(endpoint.httpMethod())).append(" | ")
                    .append(table(endpoint.path())).append(" | ")
                    .append(table(endpoint.purpose())).append(" | ")
                    .append(table(valueOrDefault(endpoint.requestDto(), "なし"))).append(" | ")
                    .append(table(valueOrDefault(endpoint.responseDto(), "なし"))).append(" | ")
                    .append(table(valueOrDefault(endpoint.authorization(), "未指定"))).append(" | ")
                    .append(table(valueOrDefault(endpoint.approvalRequired(), "未指定"))).append(" | ")
                    .append(table(valueOrDefault(endpoint.auditLogRequired(), "未指定"))).append(" | ")
                    .append(table(relatedEndpointText(endpoint))).append(" |\n");
        }
        sb.append('\n');
    }

    private void appendMcpTools(StringBuilder sb, BlueprintResult result) {
        sb.append("## 7. MCP tools候補\n\n")
                .append("| Tool | Purpose | Type | Related API | AI実行方針 (AI Execution Policy) | 承認要否 | 監査ログ要否 | Human Confirmation Boundary |\n")
                .append("|---|---|---|---|---|---|---|---|\n");
        for (McpToolCandidate tool : result.getMcpTools()) {
            sb.append("| ").append(table(tool.name())).append(" | ")
                    .append(table(tool.purpose())).append(" | ")
                    .append(table(tool.operationType())).append(" | ")
                    .append(table(tool.relatedApi())).append(" | ")
                    .append(table(tool.aiExecutionPolicy())).append(" | ")
                    .append(table(tool.approvalRequired())).append(" | ")
                    .append(table(tool.auditLogRequired())).append(" | ")
                    .append(table(toolBoundary(tool))).append(" |\n");
        }
        sb.append('\n');
    }

    private void appendMcpResources(StringBuilder sb, BlueprintResult result) {
        sb.append("## 8. MCP resources候補\n\n")
                .append("| Resource | Purpose | Scope | AI実行方針 (AI Execution Policy) | 承認要否 | 監査ログ要否 |\n")
                .append("|---|---|---|---|---|---|\n");
        for (McpResourceCandidate resource : result.getMcpResources()) {
            sb.append("| ").append(table(resource.name())).append(" | ")
                    .append(table(resource.purpose())).append(" | ")
                    .append(table(resource.scope())).append(" | ")
                    .append(table("参照のみ")).append(" | ")
                    .append(table("不要")).append(" | ")
                    .append(table(resource.scope().contains("restricted") ? "推奨" : "任意")).append(" |\n");
        }
        sb.append('\n');
    }

    private void appendMcpPrompts(StringBuilder sb, BlueprintResult result) {
        sb.append("## 9. MCP prompts候補\n\n")
                .append("| Prompt | Purpose | AI実行方針 (AI Execution Policy) | Human Confirmation Boundary |\n")
                .append("|---|---|---|---|\n");
        for (McpPromptCandidate prompt : result.getMcpPrompts()) {
            sb.append("| ").append(table(prompt.name())).append(" | ")
                    .append(table(prompt.purpose())).append(" | ")
                    .append(table("分析・確認支援のみ")).append(" | ")
                    .append(table("生成結果を人間が確認してからAPI/tool実行へ進める")).append(" |\n");
        }
        sb.append('\n');
    }

    private void appendApiMcpMappings(StringBuilder sb, BlueprintResult result) {
        sb.append("## 10. API/MCP対応表\n\n")
                .append("| API | MCP Tool | Safety Notes |\n")
                .append("|---|---|---|\n");
        for (ApiMcpMapping mapping : result.getApiMcpMappings()) {
            sb.append("| ").append(table(mapping.apiPath())).append(" | ")
                    .append(table(mapping.toolName())).append(" | ")
                    .append(table(mapping.notes())).append(" |\n");
        }
        sb.append('\n');
    }

    private void appendSecurityDesign(StringBuilder sb, BlueprintResult result) {
        sb.append("## 11. セキュリティ・承認・監査設計\n")
                .append("書き込み、外部送信、金銭、契約、入金、権限、削除に関わる操作は、AI直接実行不可または人間承認必須として扱い、監査ログ対象にする。\n\n");
        if (!result.getSecurityNotes().isEmpty()) {
            for (SecurityNote note : result.getSecurityNotes()) {
                sb.append("- [").append(note.category()).append("] ").append(note.message()).append('\n');
            }
            sb.append('\n');
        }
    }

    private void appendImplementationTasks(StringBuilder sb, BlueprintResult result) {
        sb.append("## 12. 後続実装タスク\n\n")
                .append("### 12.1 Request / Response DTO候補\n");
        for (DtoCandidate dto : result.getDtoCandidates()) {
            sb.append("- ").append(dto.getName()).append(": ").append(dto.getPurpose()).append('\n');
        }
        sb.append("\n### 12.2 Spring Controller雛形\n\n```java\n")
                .append(result.getControllerSkeleton().sourceCode())
                .append("\n```\n\n")
                .append("### 12.3 後続フェーズで具体化する事項\n")
                .append("以下は初期設計時点で実装有無・方式を確定せず、後続フェーズで要件、運用条件、セキュリティ方針に応じて具体化する。\n")
                .append("この設計書を後続AIへ渡す場合も、これらを実装禁止事項ではなく、追加設計・実装判断が必要な事項として扱う。\n")
                .append("- MCPサーバーとしての実行形態\n")
                .append("- 外部LLM API連携\n")
                .append("- DB永続化\n")
                .append("- 認証認可方式\n")
                .append("- OpenAPI定義の生成・公開範囲\n\n")
                .append("### 12.4 次の実装ステップ\n")
                .append("業務構造、曖昧点、安全分類をレビューし、REST API候補とMCP候補を確定してから実装へ進む。");
    }

    private Map<String, String> domainNamesById(BlueprintInput input) {
        Map<String, String> values = new LinkedHashMap<>();
        for (BlueprintInput.V2Domain domain : input.getV2Domains()) {
            values.put(domain.id(), domain.name());
        }
        return values;
    }

    private Map<String, String> objectNamesById(BlueprintInput input) {
        Map<String, String> values = new LinkedHashMap<>();
        for (BlueprintInput.V2BusinessObject object : input.getV2BusinessObjects()) {
            values.put(object.id(), object.name());
        }
        return values;
    }

    private Map<String, String> actorNamesById(BlueprintInput input) {
        Map<String, String> values = new LinkedHashMap<>();
        for (BlueprintInput.V2Actor actor : input.getV2Actors()) {
            values.put(actor.id(), actor.name());
        }
        return values;
    }

    private String namesFor(List<String> ids, Map<String, String> namesById) {
        List<String> names = new ArrayList<>();
        for (String id : ids) {
            names.add(valueOrDefault(namesById.get(id), id));
        }
        return joinOrDefault(names, "未指定");
    }

    private List<String> aiForbiddenLegacyOperations(BlueprintInput input) {
        Set<String> labels = new LinkedHashSet<>();
        labels.addAll(lines(input.getApprovalRequiredOperations()));
        labels.addAll(lines(input.getAuditLogRequiredOperations()));
        labels.addAll(lines(input.getWriteOperations()).stream()
                .filter(this::containsDangerKeyword)
                .toList());
        return List.copyOf(labels);
    }

    private boolean isAiDirectExecutionForbidden(BlueprintInput.V2Operation operation) {
        return "not_allowed_directly".equals(operation.aiPermission())
                || operation.approvalRequired()
                || operation.externalAction()
                || operation.stateChanging()
                || Set.of("high", "critical").contains(operation.riskLevel())
                || containsDangerKeyword(operation.label());
    }

    private boolean containsDangerKeyword(String value) {
        String normalized = value == null ? "" : value.toLowerCase(Locale.ROOT);
        return List.of("外部送信", "送信", "金銭", "契約", "請求", "入金", "決済", "権限", "削除", "確定")
                .stream()
                .anyMatch(keyword -> normalized.contains(keyword.toLowerCase(Locale.ROOT)));
    }

    private String aiPolicyText(BlueprintInput.V2Operation operation) {
        if (isAiDirectExecutionForbidden(operation)) {
            return "AI直接実行不可";
        }
        if ("draft_only".equals(operation.executionMode())) {
            return "AI支援可（文案・下書きのみ）";
        }
        if ("ai_assisted".equals(operation.executionMode())) {
            return "AI支援可";
        }
        return "allowed".equals(operation.aiPermission()) ? "AI支援可" : valueOrDefault(operation.aiPermission(), "未指定");
    }

    private String auditText(BlueprintInput.V2Operation operation) {
        return "required".equals(operation.auditLogRequired()) || operation.approvalRequired() ? "必須" : operation.auditLogRequired();
    }

    private String humanBoundary(BlueprintInput.V2Operation operation) {
        if (operation.approvalRequired()) {
            return "人間承認後に実行";
        }
        if (operation.stateChanging() || operation.externalAction()) {
            return "実行前に人間確認";
        }
        if ("draft_only".equals(operation.executionMode())) {
            return "作成結果を人間が確認";
        }
        return "通常レビュー";
    }

    private String toolBoundary(McpToolCandidate tool) {
        String policy = tool.aiExecutionPolicy() == null ? "" : tool.aiExecutionPolicy();
        if (policy.contains("AI直接実行不可") || "必須".equals(tool.approvalRequired())) {
            return "人間承認後に実行";
        }
        if (policy.contains("提案") || policy.contains("文案") || policy.contains("下書き") || policy.contains("候補")) {
            return "生成結果を人間が確認";
        }
        return "参照・確認用途";
    }

    private String relatedEndpointText(ApiEndpointCandidate endpoint) {
        String role = valueOrDefault(endpoint.domainRole(), "未指定");
        String name = valueOrDefault(endpoint.domainName(), "未指定");
        return role + " / " + name;
    }

    private String joinOrDefault(List<String> values, String defaultValue) {
        if (values == null || values.isEmpty()) {
            return defaultValue;
        }
        List<String> normalized = values.stream()
                .filter(value -> value != null && !value.isBlank())
                .toList();
        if (normalized.isEmpty()) {
            return defaultValue;
        }
        return String.join(" / ", normalized);
    }

    private String valueOrDefault(String value, String defaultValue) {
        return value == null || value.isBlank() ? defaultValue : value;
    }

    private List<String> lines(String value) {
        if (value == null || value.isBlank()) {
            return List.of();
        }
        List<String> values = new ArrayList<>();
        for (String line : value.split("\\R")) {
            String normalized = line.trim()
                    .replaceFirst("^[\\-・*\\s]+", "")
                    .replaceFirst("^\\d+[.)、]\\s*", "")
                    .trim();
            if (!normalized.isBlank()) {
                values.add(normalized);
            }
        }
        return values;
    }

    private String oneLine(String value) {
        return joinOrDefault(lines(value), "");
    }

    private String table(String value) {
        return valueOrDefault(value, "未指定")
                .replace("|", "\\|")
                .replace("\r", " ")
                .replace("\n", " ");
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
                + "業務構造、曖昧点、安全分類、API、MCP tools、resources、prompts、権限・承認・監査設計を整理した設計成果物である。";
    }

    private String normalizeOperationsForOverview(String requiredOperations) {
        List<String> operations = lines(requiredOperations);
        if (operations.isEmpty()) {
            return requiredOperations == null ? "" : requiredOperations.trim();
        }
        return String.join("、", operations);
    }
}

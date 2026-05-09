package com.example.apim.service;

import com.example.apim.model.ApiEndpointCandidate;
import com.example.apim.model.ApiMcpMapping;
import com.example.apim.model.BlueprintInput;
import com.example.apim.model.BlueprintResult;
import com.example.apim.model.ControllerSkeleton;
import com.example.apim.model.DtoCandidate;
import com.example.apim.model.DtoFieldCandidate;
import com.example.apim.model.McpPromptCandidate;
import com.example.apim.model.McpResourceCandidate;
import com.example.apim.model.McpToolCandidate;
import com.example.apim.model.NormalizedBlueprintInput;
import com.example.apim.model.SecurityNote;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ImplementationInstructionGenerator {

    public String generate(BlueprintInput input, BlueprintResult result) {
        return generate(new BlueprintInputNormalizer().normalize(input), result);
    }

    public String generate(NormalizedBlueprintInput normalizedInput, BlueprintResult result) {
        BlueprintInput input = normalizedInput.originalInput();
        StringBuilder sb = new StringBuilder();
        appendPurpose(sb);
        appendTargetSystem(sb, input, normalizedInput);
        appendBusinessSummary(sb, input);
        appendRestApis(sb, result);
        appendDtoModels(sb, result);
        appendControllerAndService(sb, result);
        appendMcpCandidates(sb, result);
        appendApiMcpPolicy(sb, result);
        appendSecurityPolicy(sb, input, result);
        appendOutOfScope(sb);
        appendTestViewpoints(sb);
        appendNotesForNextAi(sb, normalizedInput);
        return sb.toString();
    }

    private void appendPurpose(StringBuilder sb) {
        sb.append("# implementation-instructions.md\n\n")
                .append("## 1. 実装目的\n")
                .append("この指示書は、生成されたAPI/MCP設計候補をもとに、対象業務アプリケーションを実装するための実装支援AI向け指示である。\n")
                .append("入力された業務要件、対象ドメイン、必要操作、AI許可操作、セキュリティ注意点を実装材料として扱う。\n\n");
    }

    private void appendTargetSystem(StringBuilder sb, BlueprintInput input, NormalizedBlueprintInput normalizedInput) {
        sb.append("## 2. 実装対象システム\n")
                .append("- 対象システム種別: ").append(joinOrDefault(normalizedInput.systemTypes(), "未指定")).append("\n")
                .append("- 対象ドメイン: ").append(valueOrDefault(normalizedInput.targetDomainText(), input.getTargetDomain())).append("\n")
                .append("- 主ドメイン: ").append(valueOrDefault(normalizedInput.primaryDomain(), "未指定")).append("\n")
                .append("- 関連ドメイン: ").append(joinOrDefault(normalizedInput.relatedDomains(), "なし")).append("\n")
                .append("- 正規化後ドメイン一覧: ").append(joinOrDefault(normalizedInput.allDomains(), "未指定")).append("\n")
                .append("- 対象ユーザー: ").append(valueOrDefault(normalizeMultiline(input.getTargetUsers()), "未指定")).append("\n")
                .append("- ユーザー種別: ").append(valueOrDefault(normalizeMultiline(input.getUserTypes()), "未指定")).append("\n")
                .append("- 出力言語: ").append(valueOrDefault(input.getOutputLanguage(), "未指定")).append("\n\n")
                .append("### ドメイン実装境界\n")
                .append("- 主ドメインを中心にREST API、DTO、Controller、Service、MCP tool/resource/promptを実装する\n")
                .append("- 関連ドメインは参照・連携境界として扱い、書き込み系操作を広げる場合は承認条件を明示する\n")
                .append("- Controller、DTO、tool、resource、prompt候補はドメイン境界を意識して実装する\n")
                .append("- 完全な実装分割、実行可能MCPサーバー生成、MCP仕様ファイルの完全生成までは行わない\n\n");
    }

    private void appendBusinessSummary(StringBuilder sb, BlueprintInput input) {
        sb.append("## 3. 入力業務要件の要約\n")
                .append("- 業務要件: ").append(valueOrDefault(normalizeMultiline(input.getBusinessRequirements()), "未指定")).append("\n")
                .append("- 必要操作: ").append(valueOrDefault(normalizeMultiline(input.getRequiredOperations()), "未指定")).append("\n")
                .append("- AI許可操作: ").append(valueOrDefault(normalizeMultiline(input.getAllowedAiOperations()), "未指定")).append("\n")
                .append("- 読み取り系操作: ").append(valueOrDefault(normalizeMultiline(input.getReadOnlyOperations()), "未指定")).append("\n")
                .append("- 書き込み系操作: ").append(valueOrDefault(normalizeMultiline(input.getWriteOperations()), "未指定")).append("\n")
                .append("- 承認必須操作: ").append(valueOrDefault(normalizeMultiline(input.getApprovalRequiredOperations()), "未指定")).append("\n")
                .append("- 監査ログ必須操作: ").append(valueOrDefault(normalizeMultiline(input.getAuditLogRequiredOperations()), "未指定")).append("\n")
                .append("- 認証方式候補: ").append(valueOrDefault(input.getAuthenticationMethod(), "未指定")).append("\n\n");
    }

    private void appendRestApis(StringBuilder sb, BlueprintResult result) {
        sb.append("## 4. 実装するREST API\n")
                .append("以下のRESTエンドポイント候補を対象業務アプリケーションのAPIとして実装する。\n\n")
                .append("| Method | Path | Purpose | Request DTO | Response DTO | Authorization | Approval | Audit |\n")
                .append("|---|---|---|---|---|---|---|---|\n");
        if (empty(result.getApiEndpoints())) {
            sb.append("| 未生成 | 未生成 | RESTエンドポイント候補が生成されていない | - | - | - | - | - |\n");
        }
        for (ApiEndpointCandidate endpoint : safeList(result.getApiEndpoints())) {
            sb.append("| ").append(endpoint.httpMethod()).append(" | ")
                    .append(endpoint.path()).append(" | ")
                    .append(endpoint.purpose()).append(" | ")
                    .append(endpoint.requestDto()).append(" | ")
                    .append(endpoint.responseDto()).append(" | ")
                    .append(endpoint.authorization()).append(" | ")
                    .append(endpoint.approvalRequired()).append(" | ")
                    .append(endpoint.auditLogRequired()).append(" |\n");
        }
        sb.append('\n');
    }

    private void appendDtoModels(StringBuilder sb, BlueprintResult result) {
        sb.append("## 5. 実装するDTO / 入出力モデル\n")
                .append("以下のDTO候補をRequest / Responseモデルとして実装し、入力検証、必須項目、機密項目の扱いを明確にする。\n");
        if (empty(result.getDtoCandidates())) {
            sb.append("- DTO候補は未生成。REST APIごとに最小限のRequest / Responseモデルを定義する。\n\n");
            return;
        }
        for (DtoCandidate dto : safeList(result.getDtoCandidates())) {
            sb.append("- ").append(dto.getName()).append(": ").append(dto.getPurpose()).append('\n');
            for (DtoFieldCandidate field : safeList(dto.getFields())) {
                sb.append("  - ").append(field.name()).append(": ").append(field.javaType())
                        .append(field.required() ? " / required" : " / optional")
                        .append(field.sensitive() ? " / sensitive" : "")
                        .append(" / ").append(valueOrDefault(field.validationHint(), "validation未指定"))
                        .append('\n');
            }
        }
        sb.append('\n');
    }

    private void appendControllerAndService(StringBuilder sb, BlueprintResult result) {
        ControllerSkeleton controllerSkeleton = result.getControllerSkeleton() == null
                ? new ControllerSkeleton("", "")
                : result.getControllerSkeleton();
        sb.append("## 6. 実装するController / Service候補\n")
                .append("生成されたController雛形を対象業務アプリケーションのController候補として扱い、業務ロジックはService層へ分離する。\n")
                .append("- Controller候補: ").append(valueOrDefault(controllerSkeleton.className(), "未生成")).append("\n")
                .append("- Service候補: Controllerごとに対応する業務Serviceを作成し、承認・監査ログ・入力検証の責務を明確にする\n\n")
                .append("```java\n")
                .append(valueOrDefault(controllerSkeleton.sourceCode(), "// Controller雛形は未生成"))
                .append("\n```\n\n");
    }

    private void appendMcpCandidates(StringBuilder sb, BlueprintResult result) {
        sb.append("## 7. 実装するMCP tools/resources/prompts\n")
                .append("MCP候補はAIエージェントに公開する操作入口、参照範囲、定型指示として実装判断する。\n\n")
                .append("MCP tools/resources/prompts候補をもとに、実装可能な範囲でMCPサーバーまたはMCP連携層の実装を検討する。\n")
                .append("transport、認証・認可、承認、人間確認、監査ログ、テスト方式が未定義の場合は、設計補完またはTODOとして明示する。\n\n")
                .append("### MCP tools\n")
                .append("| Tool | Purpose | Arguments | Return | Related API | Policy | Approval | Audit |\n")
                .append("|---|---|---|---|---|---|---|---|\n");
        if (empty(result.getMcpTools())) {
            sb.append("| 未生成 | MCP tool候補が生成されていない | - | - | - | - | - | - |\n");
        }
        for (McpToolCandidate tool : safeList(result.getMcpTools())) {
            sb.append("| ").append(tool.name()).append(" | ")
                    .append(tool.purpose()).append(" | ")
                    .append(tool.arguments()).append(" | ")
                    .append(tool.returnValue()).append(" | ")
                    .append(tool.relatedApi()).append(" | ")
                    .append(tool.aiExecutionPolicy()).append(" | ")
                    .append(tool.approvalRequired()).append(" | ")
                    .append(tool.auditLogRequired()).append(" |\n");
        }
        sb.append("\n### MCP resources\n");
        if (empty(result.getMcpResources())) {
            sb.append("- MCP resource候補は未生成。\n");
        }
        for (McpResourceCandidate resource : safeList(result.getMcpResources())) {
            sb.append("- ").append(resource.name()).append(": ").append(resource.purpose())
                    .append(" (scope: ").append(resource.scope()).append(")\n");
        }
        sb.append("\n### MCP prompts\n");
        if (empty(result.getMcpPrompts())) {
            sb.append("- MCP prompt候補は未生成。\n");
        }
        for (McpPromptCandidate prompt : safeList(result.getMcpPrompts())) {
            sb.append("- ").append(prompt.name()).append(": ").append(prompt.purpose())
                    .append(" / template: ").append(valueOrDefault(prompt.promptTemplate(), "未指定")).append('\n');
        }
        sb.append('\n');
    }

    private void appendApiMcpPolicy(StringBuilder sb, BlueprintResult result) {
        sb.append("## 8. API/MCP対応方針\n")
                .append("人間向けREST APIとAI向けMCP toolを対応付け、同じ承認条件・監査ログ条件を適用する。\n\n")
                .append("| API | MCP Tool | Notes |\n")
                .append("|---|---|---|\n");
        if (empty(result.getApiMcpMappings())) {
            sb.append("| 未生成 | 未生成 | API/MCP対応表が生成されていない |\n");
        }
        for (ApiMcpMapping mapping : safeList(result.getApiMcpMappings())) {
            sb.append("| ").append(mapping.apiPath()).append(" | ")
                    .append(mapping.toolName()).append(" | ")
                    .append(mapping.notes()).append(" |\n");
        }
        sb.append('\n');
    }

    private void appendSecurityPolicy(StringBuilder sb, BlueprintInput input, BlueprintResult result) {
        sb.append("## 9. 認証・認可・承認・人間確認・監査ログ方針\n")
                .append("- 認証方式候補: ").append(valueOrDefault(input.getAuthenticationMethod(), "未指定")).append("\n")
                .append("- 認可: ユーザー種別、AI実行ポリシー、REST APIのauthorizationを照合して実装する\n")
                .append("- 承認: 書き込み、削除、外部送信、状態変更は人間確認を要求する\n")
                .append("- 監査ログ: AIが関与した参照・提案・承認依頼・変更操作は操作者、入力、対象ID、結果、承認者を記録する\n");
        for (SecurityNote note : safeList(result.getSecurityNotes())) {
            sb.append("- [").append(note.category()).append("] ").append(note.message()).append('\n');
        }
        sb.append('\n');
    }

    private void appendOutOfScope(StringBuilder sb) {
        sb.append("## 10. 後続フェーズで具体化する事項\n")
                .append("以下は本指示書の初期実装指示時点で実装有無や方式を固定せず、後続フェーズで要件、運用条件、セキュリティ方針、公開方針に応じて具体化する事項である。\n")
                .append("このAI実装指示書を後続AIへ渡す場合も、これらを実装禁止事項ではなく、追加設計・実装判断が必要な事項として扱う。\n")
                .append("- 外部LLM API連携方式\n")
                .append("- DB永続化およびマイグレーション方式\n")
                .append("- 認証・認可方式の本格化（認証・認可・承認・監査ログの方針とテスト観点は維持する）\n")
                .append("- OpenAPI定義の生成・公開範囲\n")
                .append("- 生成対象アプリのデプロイ方式（本番運用、CI/CD設計、秘密情報管理を含む）\n\n");
    }

    private void appendTestViewpoints(StringBuilder sb) {
        sb.append("## 11. テスト観点\n")
                .append("- REST APIごとの正常系、入力不正、権限不足、承認未了、監査ログ記録を確認する\n")
                .append("- DTOの必須項目、型、バリデーション、機密項目の出力抑制を確認する\n")
                .append("- MCP toolが対応REST APIと同じ認可・承認・監査ログ方針を守ることを確認する\n")
                .append("- MCP resourceが許可された参照範囲だけを返すことを確認する\n")
                .append("- MCP promptに禁止操作、人間確認条件、機密情報抑制が含まれることを確認する\n\n");
    }

    private void appendNotesForNextAi(StringBuilder sb, NormalizedBlueprintInput normalizedInput) {
        sb.append("## 12. 後続AIへの注意事項\n")
                .append("- この指示書の対象は、利用者の業務要件から作る対象業務アプリケーションである\n")
                .append("- 主ドメイン: ").append(valueOrDefault(normalizedInput.primaryDomain(), "未指定")).append("\n")
                .append("- 関連ドメイン: ").append(joinOrDefault(normalizedInput.relatedDomains(), "なし")).append("\n")
                .append("- 生成済み候補は確定仕様ではなく、人間レビュー前提の初期設計として扱う\n")
                .append("- AI許可操作を超える自動実行を追加しない\n")
                .append("- 承認必須操作、監査ログ必須操作、禁止操作を省略しない\n")
                .append("- MCPサーバーまたはMCP連携層を実装する場合は、不足前提をTODOとして明示し、未実装の機能を実装済みと記述しない\n")
                .append("- 機密情報、個人情報、認証情報をログや応答へ過剰出力しない\n");
    }

    private String joinOrDefault(List<String> values, String defaultValue) {
        if (values == null || values.isEmpty()) {
            return defaultValue;
        }
        List<String> filtered = values.stream()
                .filter(value -> value != null && !value.isBlank())
                .toList();
        if (filtered.isEmpty()) {
            return defaultValue;
        }
        return String.join(" / ", filtered);
    }

    private String valueOrDefault(String value, String defaultValue) {
        return value == null || value.isBlank() ? defaultValue : value;
    }

    private String normalizeMultiline(String value) {
        return valueOrDefault(value, "").replace("\r\n", "\n").replace("\r", "\n").replace("\n", " / ");
    }

    private <T> List<T> safeList(List<T> values) {
        return values == null ? List.of() : values;
    }

    private boolean empty(List<?> values) {
        return values == null || values.isEmpty();
    }
}

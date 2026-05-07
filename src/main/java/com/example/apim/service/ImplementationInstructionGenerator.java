package com.example.apim.service;

import com.example.apim.model.BlueprintInput;
import com.example.apim.model.BlueprintResult;
import com.example.apim.model.NormalizedBlueprintInput;
import org.springframework.stereotype.Service;

@Service
public class ImplementationInstructionGenerator {

    public String generate(BlueprintInput input, BlueprintResult result) {
        return generate(new BlueprintInputNormalizer().normalize(input), result);
    }

    public String generate(NormalizedBlueprintInput normalizedInput, BlueprintResult result) {
        BlueprintInput input = normalizedInput.originalInput();
        StringBuilder sb = new StringBuilder();
        sb.append("# implementation-instructions.md\n\n")
                .append("## 1. 実装目的\n")
                .append("APIM for Spring のMVPを段階実装する。\n\n")
                .append("## 2. 実装対象\n")
                .append("- 入力フォーム\n")
                .append("- API/MCP候補生成\n")
                .append("- 結果表示とプレビュー\n\n")
                .append("## 3. 実装しないこと\n")
                .append("- 完全動作するMCPサーバーは実装しない\n")
                .append("- DB永続化\n")
                .append("- 外部LLM API連携\n")
                .append("- Spring Security\n\n")
                .append("## 4. 想定技術スタック\n")
                .append("- Java 17\n")
                .append("- Spring Boot / MVC / Thymeleaf\n")
                .append("- Maven\n\n")
                .append("## 5. Controller実装方針\n")
                .append("- BlueprintControllerで入力・生成・プレビューを扱う\n\n")
                .append("## 6. DTO実装方針\n")
                .append("- 入力はBean Validationで検証\n")
                .append("- DTO候補は必要最小限のフィールドで出力\n\n")
                .append("## 7. Service境界\n")
                .append("- 生成処理はサービスへ分離し責務を固定する\n\n")
                .append("### ドメイン実装境界\n")
                .append("- 主ドメイン: ").append(valueOrDefault(normalizedInput.primaryDomain(), "未指定")).append("\n")
                .append("- 関連ドメイン: ").append(joinOrDefault(normalizedInput.relatedDomains(), "なし")).append("\n")
                .append("- 正規化後ドメイン一覧: ").append(joinOrDefault(normalizedInput.allDomains(), "未指定")).append("\n")
                .append("- 実装時は主ドメインを中心にAPI/MCP候補を扱い、関連ドメインは参照・連携境界として扱う\n")
                .append("- Controller、DTO、tool、resource、prompt候補はドメイン境界を意識して生成される\n")
                .append("- ただし、完全な実装分割、実行可能MCPサーバー生成、MCP仕様ファイルの完全生成までは行わない\n\n")
                .append("## 8. 認可・承認・監査ログ方針\n")
                .append("- 書き込み系は承認要否を明示\n")
                .append("- AI操作は監査ログ観点を必ず出力\n\n")
                .append("## 9. MCP設計の将来実装方針\n")
                .append("- 現時点はtools/resources/promptsの定義案のみ\n\n")
                .append("## 10. テスト観点\n")
                .append("- API候補生成\n")
                .append("- MCP候補生成\n")
                .append("- セキュリティ注意点\n")
                .append("- MVCエンドポイント\n\n")
                .append("## 11. 成果物一覧\n")
                .append("- api-mcp-blueprint.md\n")
                .append("- implementation-instructions.md\n")
                .append("- Controller雛形\n\n")
                .append("## 12. 検証手順\n")
                .append("- mvn test\n")
                .append("- 画面アクセス確認: GET /\n\n")
                .append("## 13. 注意事項\n")
                .append("- 出力は人間レビュー前提\n")
                .append("- 機密情報の過剰出力を避ける\n\n")
                .append("### 入力サマリー\n")
                .append("- 対象システム種別: ").append(joinOrDefault(normalizedInput.systemTypes(), "未指定")).append("\n")
                .append("- 対象ドメイン: ").append(valueOrDefault(normalizedInput.targetDomainText(), input.getTargetDomain())).append("\n")
                .append("- 主ドメイン: ").append(valueOrDefault(normalizedInput.primaryDomain(), "未指定")).append("\n")
                .append("- 関連ドメイン: ").append(joinOrDefault(normalizedInput.relatedDomains(), "なし")).append("\n")
                .append("- 必要操作: ").append(input.getRequiredOperations().replace("\n", " / ")).append("\n")
                .append("- 出力言語: ").append(input.getOutputLanguage()).append("\n")
                .append("- API候補件数: ").append(result.getApiEndpoints().size()).append("\n")
                .append("- MCP tool件数: ").append(result.getMcpTools().size()).append("\n");
        return sb.toString();
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
}

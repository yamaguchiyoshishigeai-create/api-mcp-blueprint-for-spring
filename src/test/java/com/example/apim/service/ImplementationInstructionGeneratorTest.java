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
import com.example.apim.model.SecurityNote;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ImplementationInstructionGeneratorTest {

    @Test
    void generatesTargetApplicationInstructionsWithoutApimInternalImplementationScope() {
        String markdown = new ImplementationInstructionGenerator().generate(sampleInput(), sampleResult());

        assertThat(markdown)
                .contains("対象業務アプリケーションを実装するための実装支援AI向け指示")
                .contains("APIM for Spring本体の改修指示ではない")
                .contains("## 1. 実装目的")
                .contains("## 2. 実装対象システム")
                .contains("## 3. 入力業務要件の要約")
                .contains("## 4. 実装するREST API")
                .contains("## 5. 実装するDTO / 入出力モデル")
                .contains("## 6. 実装するController / Service候補")
                .contains("## 7. 実装するMCP tools/resources/prompts")
                .contains("## 8. API/MCP対応方針")
                .contains("## 9. 認証・認可・承認・人間確認・監査ログ方針")
                .contains("## 10. 実装しないこと")
                .contains("## 11. テスト観点")
                .contains("## 12. 後続AIへの注意事項")
                .doesNotContain("APIM for Spring のMVPを段階実装する")
                .doesNotContain("- 入力フォーム")
                .doesNotContain("- API/MCP候補生成")
                .doesNotContain("- 結果表示とプレビュー")
                .doesNotContain("BlueprintController");
    }

    @Test
    void includesInputDomainOperationsGeneratedCandidatesAndRiskControls() {
        String markdown = new ImplementationInstructionGenerator().generate(sampleInput(), sampleResult());

        assertThat(markdown)
                .contains("- 対象システム種別: EC / 販売管理")
                .contains("- 対象ドメイン: 注文管理 / 在庫管理 / 商品管理")
                .contains("- 主ドメイン: 注文管理")
                .contains("- 関連ドメイン: 在庫管理 / 商品管理")
                .contains("- 必要操作: 注文検索 / 注文ステータス更新")
                .contains("- AI許可操作: 注文検索 / 注文変更案の作成")
                .contains("- 出力言語: 日本語")
                .contains("| GET | /api/orders | 注文を検索する | OrderSearchRequest | OrderSummaryResponse")
                .contains("| PUT | /api/orders/{id}/status | 注文ステータスを更新する | OrderStatusUpdateRequest | OrderResponse")
                .contains("- OrderStatusUpdateRequest: 注文ステータス更新リクエスト")
                .contains("status: String / required")
                .contains("public class OrderController")
                .contains("searchOrders")
                .contains("proposeOrderUpdate")
                .contains("orders-catalog")
                .contains("analyze-order-risk")
                .contains("| /api/orders/{id}/status | proposeOrderUpdate |")
                .contains("[承認] 注文ステータス更新は人間承認後に実行する")
                .contains("[監査ログ] AIによる変更案作成と承認結果を記録する")
                .contains("完全動作するMCPサーバーは実装しない");
    }

    @Test
    void includesDomainBoundaryImplementationNotes() {
        String markdown = new ImplementationInstructionGenerator().generate(sampleInput(), sampleResult());

        assertThat(markdown)
                .contains("### ドメイン実装境界")
                .contains("関連ドメインは参照・連携境界として扱い")
                .contains("Controller、DTO、tool、resource、prompt候補はドメイン境界を意識して実装する")
                .contains("完全な実装分割、実行可能MCPサーバー生成、MCP仕様ファイルの完全生成までは行わない")
                .doesNotContain("TSK-032ではController、DTO、tool、resource、promptのドメイン別分割は行わない");
    }

    private BlueprintInput sampleInput() {
        BlueprintInput input = new BlueprintInput();
        input.setBusinessRequirements("注文を検索し、ステータス更新は承認後に実行する。");
        input.setTargetDomain("注文管理 / 在庫管理 / 商品管理");
        input.setSystemTypes(List.of("EC / 販売管理"));
        input.setPrimaryDomain("注文管理");
        input.setRelatedDomains(List.of("在庫管理", "商品管理"));
        input.setUserTypes("EC運営担当\nAIアシスタント");
        input.setRequiredOperations("注文検索\n注文ステータス更新");
        input.setAllowedAiOperations("注文検索\n注文変更案の作成");
        input.setApprovalRequiredOperations("注文ステータス更新");
        input.setAuditLogRequiredOperations("AIによる変更案作成\n承認結果");
        input.setAuthenticationMethod("Spring Security + セッション認証");
        input.setTargetUsers("EC運営担当、倉庫担当、管理者、AIアシスタント");
        input.setOutputLanguage("日本語");
        return input;
    }

    private BlueprintResult sampleResult() {
        BlueprintResult result = new BlueprintResult();
        result.setApiEndpoints(List.of(
                new ApiEndpointCandidate("GET", "/api/orders", "注文を検索する", "EC運営担当",
                        "OrderSearchRequest", "OrderSummaryResponse", "認証済みユーザー", "不要", "必須"),
                new ApiEndpointCandidate("PUT", "/api/orders/{id}/status", "注文ステータスを更新する", "管理者",
                        "OrderStatusUpdateRequest", "OrderResponse", "管理者", "必須", "必須")
        ));
        result.setDtoCandidates(List.of(
                new DtoCandidate("OrderStatusUpdateRequest", "注文ステータス更新リクエスト", List.of(
                        new DtoFieldCandidate("status", "String", true, "allowed status only", false),
                        new DtoFieldCandidate("reason", "String", true, "承認理由を必須にする", false)
                ))
        ));
        result.setControllerSkeleton(new ControllerSkeleton("OrderController", """
                package com.example.generated.controller;

                public class OrderController {
                }
                """));
        result.setMcpTools(List.of(
                new McpToolCandidate("searchOrders", "注文検索", "query", "orders",
                        "/api/orders", "read", "AI実行可", "不要", "必須"),
                new McpToolCandidate("proposeOrderUpdate", "注文変更案の作成", "orderId,status",
                        "proposal", "/api/orders/{id}/status", "write-proposal", "提案のみ", "必須", "必須")
        ));
        result.setMcpResources(List.of(
                new McpResourceCandidate("orders-catalog", "注文の参照範囲", "read-only")
        ));
        result.setMcpPrompts(List.of(
                new McpPromptCandidate("analyze-order-risk", "注文変更リスクを分析する",
                        "禁止操作と人間確認条件を確認する")
        ));
        result.setApiMcpMappings(List.of(
                new ApiMcpMapping("/api/orders", "searchOrders", "参照APIとして対応"),
                new ApiMcpMapping("/api/orders/{id}/status", "proposeOrderUpdate", "承認後にAPI実行")
        ));
        result.setSecurityNotes(List.of(
                new SecurityNote("承認", "注文ステータス更新は人間承認後に実行する"),
                new SecurityNote("監査ログ", "AIによる変更案作成と承認結果を記録する")
        ));
        return result;
    }
}

package com.example.apim.service;

import com.example.apim.model.BlueprintInput;
import com.example.apim.model.BlueprintResult;
import com.example.apim.model.ControllerSkeleton;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MarkdownDocumentGeneratorTest {

    @Test
    void includesMainSections() {
        MarkdownDocumentGenerator generator = new MarkdownDocumentGenerator();
        BlueprintInput input = new BlueprintInput();
        input.setTargetDomain("顧客管理");
        input.setSystemTypes(List.of("CRM"));
        input.setPrimaryDomain("顧客管理");
        input.setRelatedDomains(List.of("問い合わせ管理"));
        input.setUserTypes("営業担当");
        input.setRequiredOperations("検索");

        BlueprintResult result = new BlueprintResult();
        result.setApiDesignSummary("summary");
        result.setControllerSkeleton(new ControllerSkeleton("CustomerController", "@RestController"));

        String markdown = generator.generate(input, result);

        assertThat(markdown).contains("## 5. RESTエンドポイント一覧");
        assertThat(markdown).contains("## 8. MCP tools一覧");
        assertThat(markdown).contains("## 13. セキュリティ注意点");
        assertThat(markdown).contains("## 11. API / MCP tool 対応表");
    }

    @Test
    void usesInputDrivenOverviewInsteadOfApimInternalMvpText() {
        MarkdownDocumentGenerator generator = new MarkdownDocumentGenerator();
        BlueprintInput input = new BlueprintInput();
        input.setTargetDomain("顧客管理");
        input.setSystemTypes(List.of("CRM"));
        input.setPrimaryDomain("顧客管理");
        input.setRelatedDomains(List.of("問い合わせ管理"));
        input.setUserTypes("営業担当");
        input.setRequiredOperations("顧客検索");

        BlueprintResult result = new BlueprintResult();
        result.setControllerSkeleton(new ControllerSkeleton("CustomerController", "@RestController"));

        String markdown = generator.generate(input, result);

        assertThat(markdown)
                .contains("## 1. 設計対象概要")
                .contains("入力要件に基づき")
                .contains("CRMの顧客管理（関連ドメイン: 問い合わせ管理）")
                .contains("必要な操作として顧客検索を想定し")
                .contains("API、MCP tools、resources、prompts、権限・承認・監査設計を整理した設計成果物である。")
                .doesNotContain("APIM for Spring の初期MVP向け設計成果物。");
    }

    @Test
    void includesDomainBoundarySummaryFromNormalizedInput() {
        MarkdownDocumentGenerator generator = new MarkdownDocumentGenerator();
        BlueprintInput input = new BlueprintInput();
        input.setTargetDomain("顧客管理 / 問い合わせ管理");
        input.setSystemTypes(List.of("顧客対応CRM", "保守サポート管理"));
        input.setPrimaryDomain("顧客管理");
        input.setRelatedDomains(List.of("顧客管理", "問い合わせ管理"));
        input.setUserTypes("営業担当");
        input.setRequiredOperations("検索");

        BlueprintResult result = new BlueprintResult();
        result.setControllerSkeleton(new ControllerSkeleton("CustomerController", "@RestController"));

        String markdown = generator.generate(input, result);

        assertThat(markdown)
                .contains("- 対象システム種別: 顧客対応CRM / 保守サポート管理")
                .contains("- 主ドメイン: 顧客管理")
                .contains("- 関連ドメイン: 問い合わせ管理")
                .contains("- 正規化後ドメイン一覧: 顧客管理 / 問い合わせ管理");
    }

    @Test
    void keepsLegacyTargetDomainOnlyInputReadable() {
        MarkdownDocumentGenerator generator = new MarkdownDocumentGenerator();
        BlueprintInput input = new BlueprintInput();
        input.setTargetDomain("注文管理");
        input.setUserTypes("業務担当");
        input.setRequiredOperations("注文検索");

        BlueprintResult result = new BlueprintResult();
        result.setControllerSkeleton(new ControllerSkeleton("OrderController", "@RestController"));

        String markdown = generator.generate(input, result);

        assertThat(markdown)
                .contains("- 主ドメイン: 注文管理")
                .contains("- 関連ドメイン: なし")
                .contains("- 正規化後ドメイン一覧: 注文管理");
    }

    @Test
    void outOfScopeSectionIsExpressedAsLaterPhaseDecisions() {
        MarkdownDocumentGenerator generator = new MarkdownDocumentGenerator();
        BlueprintInput input = new BlueprintInput();
        input.setTargetDomain("注文管理");
        input.setUserTypes("業務担当");
        input.setRequiredOperations("注文検索");

        BlueprintResult result = new BlueprintResult();
        result.setControllerSkeleton(new ControllerSkeleton("OrderController", "@RestController"));

        String markdown = generator.generate(input, result);

        assertThat(markdown)
                .contains("## 14. 後続フェーズで具体化する事項")
                .contains("後続フェーズで要件、運用条件、セキュリティ方針に応じて具体化する。")
                .contains("実装禁止事項ではなく、追加設計・実装判断が必要な事項として扱う。")
                .contains("- MCPサーバーとしての実行形態")
                .contains("- 外部LLM API連携")
                .contains("- DB永続化")
                .contains("- 認証認可方式")
                .contains("- OpenAPI定義の生成・公開範囲")
                .doesNotContain("## 14. 初期MVPで実装しないこと")
                .doesNotContain("- 完全動作するMCPサーバー")
                .doesNotContain("- 認証認可の本格実装")
                .doesNotContain("- OpenAPI完全生成");
    }
}

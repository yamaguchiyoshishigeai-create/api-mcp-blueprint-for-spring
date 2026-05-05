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
}

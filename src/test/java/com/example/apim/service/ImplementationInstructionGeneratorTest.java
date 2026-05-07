package com.example.apim.service;

import com.example.apim.model.BlueprintInput;
import com.example.apim.model.BlueprintResult;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ImplementationInstructionGeneratorTest {

    @Test
    void includesOutOfScopeMcpServerStatement() {
        ImplementationInstructionGenerator generator = new ImplementationInstructionGenerator();
        BlueprintInput input = new BlueprintInput();
        input.setTargetDomain("顧客管理");
        input.setRequiredOperations("顧客検索");
        BlueprintResult result = new BlueprintResult();

        String markdown = generator.generate(input, result);

        assertThat(markdown).contains("完全動作するMCPサーバーは実装しない");
    }

    @Test
    void includesDomainBoundaryImplementationNotes() {
        ImplementationInstructionGenerator generator = new ImplementationInstructionGenerator();
        BlueprintInput input = new BlueprintInput();
        input.setTargetDomain("注文管理 / 在庫管理 / 商品管理");
        input.setSystemTypes(List.of("EC / 販売管理"));
        input.setPrimaryDomain("注文管理");
        input.setRelatedDomains(List.of("在庫管理", "商品管理"));
        input.setRequiredOperations("注文検索");
        BlueprintResult result = new BlueprintResult();

        String markdown = generator.generate(input, result);

        assertThat(markdown)
                .contains("### ドメイン実装境界")
                .contains("- 主ドメイン: 注文管理")
                .contains("- 関連ドメイン: 在庫管理 / 商品管理")
                .contains("- 正規化後ドメイン一覧: 注文管理 / 在庫管理 / 商品管理")
                .contains("関連ドメインは参照・連携境界として扱う")
                .contains("Controller、DTO、tool、resource、prompt候補はドメイン境界を意識して生成される")
                .contains("完全な実装分割、実行可能MCPサーバー生成、MCP仕様ファイルの完全生成までは行わない")
                .doesNotContain("TSK-032ではController、DTO、tool、resource、promptのドメイン別分割は行わない")
                .contains("- 対象システム種別: EC / 販売管理");
    }
}

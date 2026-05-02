package com.example.apim.service;

import com.example.apim.model.BlueprintInput;
import com.example.apim.model.BlueprintResult;
import com.example.apim.model.ControllerSkeleton;
import org.junit.jupiter.api.Test;

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
    }
}

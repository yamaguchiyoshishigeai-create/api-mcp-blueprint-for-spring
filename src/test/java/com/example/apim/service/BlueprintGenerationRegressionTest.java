package com.example.apim.service;

import com.example.apim.model.BlueprintInput;
import com.example.apim.model.BlueprintResult;
import com.example.apim.support.DomainNameNormalizer;
import com.example.apim.support.NamingSupport;
import com.example.apim.support.OperationClassifier;
import com.example.apim.testsupport.BlueprintInputFixtures;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class BlueprintGenerationRegressionTest {

    private final BlueprintGenerationService service = newService();

    @Test
    void equipmentLoanManagementKeepsBusinessVocabularyAndRiskControls() {
        BlueprintResult result = service.generate(BlueprintInputFixtures.equipmentLoanManagement());

        assertGeneratedVocabulary(result, "equipment", "Equipment");
        assertNoDomainItemsFallback(result);
        assertRiskControlsPresent(result);
        assertDocumentsKeepRequiredSectionsAndOutOfScope(result);
    }

    @Test
    void internalApplicationWorkflowKeepsBusinessVocabularyAndRiskControls() {
        BlueprintResult result = service.generate(BlueprintInputFixtures.internalApplicationWorkflow());

        assertGeneratedVocabulary(result, "applications", "Application");
        assertNoDomainItemsFallback(result);
        assertRiskControlsPresent(result);
        assertDocumentsKeepRequiredSectionsAndOutOfScope(result);
    }

    @Test
    void knowledgeSearchAndSummaryKeepsBusinessVocabularyAndRiskControls() {
        BlueprintResult result = service.generate(BlueprintInputFixtures.knowledgeSearchAndSummary());

        assertGeneratedVocabulary(result, "knowledge-articles", "KnowledgeArticle");
        assertNoDomainItemsFallback(result);
        assertRiskControlsPresent(result);
        assertDocumentsKeepRequiredSectionsAndOutOfScope(result);
    }

    private void assertGeneratedVocabulary(BlueprintResult result, String expectedPathToken, String expectedClassToken) {
        assertThat(result.getApiEndpoints()).anyMatch(e -> e.path().contains(expectedPathToken));
        assertThat(result.getDtoCandidates()).anyMatch(d -> d.getName().contains(expectedClassToken));
        assertThat(result.getMcpTools()).anyMatch(t -> t.name().contains(expectedClassToken));
    }

    private void assertNoDomainItemsFallback(BlueprintResult result) {
        assertThat(majorGeneratedNames(result)).doesNotContain("domain-items", "DomainItem");
    }

    private String majorGeneratedNames(BlueprintResult result) {
        List<String> names = new ArrayList<>();
        names.addAll(result.getApiEndpoints().stream().map(e -> e.path()).toList());
        names.addAll(result.getDtoCandidates().stream().map(d -> d.getName()).toList());
        names.addAll(result.getMcpTools().stream().map(t -> t.name()).toList());
        names.addAll(result.getMcpResources().stream().map(r -> r.name()).toList());
        names.addAll(result.getMcpPrompts().stream().map(p -> p.name()).toList());
        names.add(result.getControllerSkeleton().className());
        names.add(result.getControllerSkeleton().sourceCode());
        return String.join("\n", names);
    }

    private void assertRiskControlsPresent(BlueprintResult result) {
        assertThat(result.getSecurityNotes()).anyMatch(n -> n.category().equals("承認"));
        assertThat(result.getSecurityNotes()).anyMatch(n -> n.category().equals("監査ログ"));
        assertThat(result.getSecurityNotes()).anyMatch(n -> n.category().equals("禁止操作"));
        assertThat(result.getApiEndpoints()).anyMatch(e -> e.approvalRequired().equals("必須"));
        assertThat(result.getApiEndpoints()).anyMatch(e -> e.auditLogRequired().equals("必須"));
        assertThat(result.getMcpTools()).anyMatch(t -> t.approvalRequired().equals("必須"));
        assertThat(result.getMcpTools()).anyMatch(t -> t.auditLogRequired().equals("必須"));
    }

    private void assertDocumentsKeepRequiredSectionsAndOutOfScope(BlueprintResult result) {
        assertThat(result.getBlueprintMarkdown())
                .contains("## 5. RESTエンドポイント一覧")
                .contains("## 6. Request / Response DTO候補")
                .contains("## 8. MCP tools一覧")
                .contains("## 12. 権限・承認・監査ログ設計")
                .contains("## 13. セキュリティ注意点")
                .contains("## 14. 初期MVPで実装しないこと")
                .contains("完全動作するMCPサーバー")
                .contains("外部LLM API連携")
                .contains("DB永続化")
                .contains("OpenAPI完全生成");

        assertThat(result.getImplementationInstructions())
                .contains("## 1. 実装目的")
                .contains("## 4. 実装するREST API")
                .contains("## 7. 実装するMCP tools/resources/prompts")
                .contains("## 9. 認証・認可・承認・人間確認・監査ログ方針")
                .contains("## 10. 実装しないこと")
                .contains("## 11. テスト観点")
                .contains("対象業務アプリケーションを実装するための実装支援AI向け指示")
                .contains("完全動作するMCPサーバーは実装しない")
                .contains("外部LLM API連携")
                .contains("DB永続化")
                .contains("APIM for Spring本体の改修指示ではない")
                .doesNotContain("APIM for Spring のMVPを段階実装する");
    }

    private BlueprintGenerationService newService() {
        return new BlueprintGenerationService(
                new OperationClassifier(),
                new BlueprintInputNormalizer(),
                new DomainNameNormalizer(),
                new ApiDesignGenerator(),
                new DtoCandidateGenerator(),
                new ControllerSkeletonGenerator(new NamingSupport()),
                new McpDesignGenerator(),
                new SecurityNotesGenerator(),
                new MarkdownDocumentGenerator(),
                new ImplementationInstructionGenerator()
        );
    }
}

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
        assertThat(majorGeneratedNames(result)).doesNotContain("domain-items", "DomainItem", "executeDomainItem", "/domain-items");
    }

    private String majorGeneratedNames(BlueprintResult result) {
        List<String> names = new ArrayList<>();
        names.addAll(result.getApiEndpoints().stream().map(e -> e.path()).toList());
        names.addAll(result.getDtoCandidates().stream().map(d -> d.getName()).toList());
        names.addAll(result.getMcpTools().stream().map(t -> t.name()).toList());
        names.addAll(result.getMcpTools().stream().map(t -> t.purpose()).toList());
        names.addAll(result.getMcpTools().stream().map(t -> t.relatedApi()).toList());
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
                .contains("## 3. 抽出された業務構造")
                .contains("## 4. AI支援・承認・監査の分類")
                .contains("## 5. 曖昧点・確認事項")
                .contains("## 6. REST API候補")
                .contains("## 7. MCP tools候補")
                .contains("## 8. MCP resources候補")
                .contains("## 9. MCP prompts候補")
                .contains("## 10. API/MCP対応表")
                .contains("## 11. セキュリティ・承認・監査設計")
                .contains("### 12.1 Request / Response DTO候補")
                .contains("### 12.3 後続フェーズで具体化する事項")
                .contains("後続フェーズで要件、運用条件、セキュリティ方針に応じて具体化する。")
                .contains("実装禁止事項ではなく、追加設計・実装判断が必要な事項として扱う。")
                .contains("MCPサーバーとしての実行形態")
                .contains("外部LLM API連携")
                .contains("DB永続化")
                .contains("OpenAPI定義の生成・公開範囲")
                .doesNotContain("## 14. 初期MVPで実装しないこと")
                .doesNotContain("## 5. RESTエンドポイント一覧")
                .doesNotContain("完全動作するMCPサーバー")
                .doesNotContain("OpenAPI完全生成")
                .doesNotContain("APIM for Spring の初期MVP向け設計成果物。");

        assertThat(result.getImplementationInstructions())
                .contains("## 1. 実装目的")
                .contains("## 4. 実装するREST API")
                .contains("## 7. 実装するMCP tools/resources/prompts")
                .contains("## 9. 認証・認可・承認・人間確認・監査ログ方針")
                .contains("## 10. 後続フェーズで具体化する事項")
                .contains("## 11. テスト観点")
                .contains("## 12. 後続AIへの注意事項")
                .contains("対象業務アプリケーションを実装するための実装支援AI向け指示")
                .contains("MCP tools/resources/prompts候補をもとに、実装可能な範囲でMCPサーバーまたはMCP連携層の実装を検討する")
                .contains("transport、認証・認可、承認、人間確認、監査ログ、テスト方式が未定義の場合は、設計補完またはTODOとして明示する")
                .contains("MCPサーバーまたはMCP連携層を実装する場合は、不足前提をTODOとして明示し、未実装の機能を実装済みと記述しない")
                .contains("このAI実装指示書を後続AIへ渡す場合も、これらを実装禁止事項ではなく、追加設計・実装判断が必要な事項として扱う。")
                .contains("認証・認可方式の本格化（認証・認可・承認・監査ログの方針とテスト観点は維持する）")
                .contains("外部LLM API連携")
                .contains("DB永続化")
                .doesNotContain("## 10. 実装しないこと")
                .doesNotContain("認証・認可の本格実装。ただし、認証・認可・承認・監査ログの方針とテスト観点は残す")
                .doesNotContain("APIM for Spring本体の改修指示ではない")
                .doesNotContain("- APIM for Spring本体の改修")
                .doesNotContain("完全動作するMCPサーバーは実装しない")
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

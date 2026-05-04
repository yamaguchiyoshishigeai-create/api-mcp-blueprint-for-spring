package com.example.apim.service;

import com.example.apim.model.BlueprintInput;
import com.example.apim.support.DomainNameNormalizer;
import com.example.apim.support.NamingSupport;
import com.example.apim.support.OperationClassifier;
import com.example.apim.testsupport.BlueprintInputFixtures;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class BlueprintGenerationServiceTest {

    @Test
    void generatesNonEmptyBlueprintResult() {
        BlueprintGenerationService service = newService();

        BlueprintInput input = new BlueprintInput();
        input.setBusinessRequirements("顧客情報を検索して更新する。");
        input.setTargetDomain("顧客管理");
        input.setUserTypes("営業担当\n管理者");
        input.setRequiredOperations("顧客検索\n顧客更新");
        input.setAllowedAiOperations("顧客検索\n顧客更新案の作成");

        var result = service.generate(input);

        assertThat(result.getApiEndpoints()).isNotEmpty();
        assertThat(result.getMcpTools()).isNotEmpty();
        assertThat(result.getSecurityNotes()).isNotEmpty();
        assertThat(result.getBlueprintMarkdown()).isNotBlank();
        assertThat(result.getImplementationInstructions()).isNotBlank();
    }

    @Test
    void customerSampleGeneratesExpectedApiAndMcpCandidates() {
        BlueprintGenerationService service = newService();

        BlueprintInput input = new BlueprintInput();
        input.setBusinessRequirements("""
                社内の営業担当が顧客情報を検索し、問い合わせ履歴を確認できる。
                管理者は顧客情報を登録・更新できる。
                AIアシスタントには顧客検索と問い合わせ履歴の要約を許可したい。
                顧客情報の更新はAIが直接実行せず、変更案を作成して人間承認後に反映する。
                """);
        input.setTargetDomain("顧客管理");
        input.setUserTypes("- 営業担当\n- 管理者\n- AIアシスタント");
        input.setRequiredOperations("- 顧客検索\n- 顧客詳細取得\n- 顧客登録\n- 顧客更新\n- 問い合わせ履歴取得\n- 問い合わせ履歴要約");
        input.setAllowedAiOperations("- 顧客検索\n- 顧客詳細参照\n- 問い合わせ履歴要約\n- 顧客更新案の作成");
        input.setApprovalRequiredOperations("- 顧客情報更新\n- 顧客削除");

        var result = service.generate(input);

        assertThat(result.getApiEndpoints()).anyMatch(e -> e.path().equals("/api/customers") && e.httpMethod().equals("GET"));
        assertThat(result.getApiEndpoints()).anyMatch(e -> e.path().equals("/api/customers/{id}") && e.httpMethod().equals("GET"));
        assertThat(result.getApiEndpoints()).anyMatch(e -> e.path().equals("/api/customers") && e.httpMethod().equals("POST"));
        assertThat(result.getApiEndpoints()).anyMatch(e -> e.path().equals("/api/customers/{id}") && e.httpMethod().equals("PUT"));

        assertThat(result.getMcpTools()).anyMatch(t -> t.name().equals("searchCustomers"));
        assertThat(result.getMcpTools()).anyMatch(t -> t.name().equals("getCustomerDetail"));
        assertThat(result.getMcpTools()).anyMatch(t -> t.name().startsWith("summarize"));
        assertThat(result.getMcpTools()).anyMatch(t -> t.name().equals("proposeCustomerUpdate"));
    }

    @Test
    void equipmentSampleUsesEquipmentVocabularyAcrossOutputs() {
        var result = newService().generate(BlueprintInputFixtures.equipmentLoanManagement());

        assertThat(result.getApiEndpoints()).anyMatch(e -> e.path().equals("/api/equipment"));
        assertThat(result.getApiEndpoints()).noneMatch(e -> e.path().contains("/api/domain-items"));
        assertThat(result.getDtoCandidates()).anyMatch(d -> d.getName().equals("EquipmentSearchRequest"));
        assertThat(result.getDtoCandidates()).anyMatch(d -> d.getName().equals("EquipmentSummaryResponse"));
        assertThat(result.getDtoCandidates()).anyMatch(d -> d.getName().equals("EquipmentResponse"));
        assertThat(result.getDtoCandidates()).anyMatch(d -> d.getName().equals("EquipmentCreateRequest"));
        assertThat(result.getDtoCandidates()).anyMatch(d -> d.getName().equals("EquipmentUpdateRequest"));
        assertThat(result.getMcpTools()).anyMatch(t -> t.name().equals("searchEquipment"));
        assertThat(result.getMcpTools()).anyMatch(t -> t.name().equals("getEquipmentDetail"));
        assertThat(result.getMcpTools()).anyMatch(t -> t.name().equals("proposeEquipmentUpdate"));
        assertThat(result.getMcpTools()).anyMatch(t -> t.name().equals("requestEquipmentDeletionApproval"));
        assertThat(result.getSecurityNotes()).anyMatch(n -> n.category().equals("外部送信"));
    }

    @Test
    void applicationSampleUsesApplicationVocabularyAcrossOutputs() {
        var result = newService().generate(BlueprintInputFixtures.internalApplicationWorkflow());

        assertThat(result.getApiEndpoints()).anyMatch(e -> e.path().equals("/api/applications"));
        assertThat(result.getApiEndpoints()).noneMatch(e -> e.path().contains("/api/domain-items"));
        assertThat(result.getDtoCandidates()).anyMatch(d -> d.getName().equals("ApplicationSearchRequest"));
        assertThat(result.getDtoCandidates()).anyMatch(d -> d.getName().equals("ApplicationSummaryResponse"));
        assertThat(result.getDtoCandidates()).anyMatch(d -> d.getName().equals("ApplicationResponse"));
        assertThat(result.getDtoCandidates()).anyMatch(d -> d.getName().equals("ApplicationCreateRequest"));
        assertThat(result.getDtoCandidates()).anyMatch(d -> d.getName().equals("ApplicationUpdateRequest"));
        assertThat(result.getMcpTools()).anyMatch(t -> t.name().equals("searchApplications"));
        assertThat(result.getMcpTools()).anyMatch(t -> t.name().equals("getApplicationDetail"));
        assertThat(result.getMcpTools()).anyMatch(t -> t.name().startsWith("summarizeApplication"));
        assertThat(result.getMcpTools()).anyMatch(t -> t.name().equals("requestApprovalForApplications"));
    }

    @Test
    void knowledgeSampleUsesKnowledgeArticleVocabularyAcrossOutputs() {
        var result = newService().generate(BlueprintInputFixtures.knowledgeSearchAndSummary());

        assertThat(result.getApiEndpoints()).anyMatch(e -> e.path().equals("/api/knowledge-articles"));
        assertThat(result.getApiEndpoints()).noneMatch(e -> e.path().contains("/api/domain-items"));
        assertThat(result.getDtoCandidates()).anyMatch(d -> d.getName().equals("KnowledgeArticleSearchRequest"));
        assertThat(result.getDtoCandidates()).anyMatch(d -> d.getName().equals("KnowledgeArticleSummaryResponse"));
        assertThat(result.getDtoCandidates()).anyMatch(d -> d.getName().equals("KnowledgeArticleResponse"));
        assertThat(result.getDtoCandidates()).anyMatch(d -> d.getName().equals("KnowledgeArticleCreateRequest"));
        assertThat(result.getDtoCandidates()).anyMatch(d -> d.getName().equals("KnowledgeArticleUpdateRequest"));
        assertThat(result.getMcpTools()).anyMatch(t -> t.name().equals("searchKnowledgeArticles"));
        assertThat(result.getMcpTools()).anyMatch(t -> t.name().equals("getKnowledgeArticleDetail"));
        assertThat(result.getMcpTools()).anyMatch(t -> t.name().startsWith("summarizeKnowledgeArticle"));
        assertThat(result.getSecurityNotes()).anyMatch(n -> n.category().equals("外部送信"));
    }

    @Test
    void prioritizesPrimaryAndRelatedDomainsWhenTargetDomainIsEmpty() {
        BlueprintInput input = new BlueprintInput();
        input.setBusinessRequirements("顧客情報を検索して更新する。");
        input.setTargetDomain("");
        input.setPrimaryDomain("注文管理");
        input.setRelatedDomains(java.util.List.of("在庫管理", "商品管理"));
        input.setUserTypes("営業担当\n管理者");
        input.setRequiredOperations("注文検索\n注文更新");
        input.setAllowedAiOperations("注文検索\n注文更新案の作成");

        var result = newService().generate(input);

        assertThat(result.getApiEndpoints()).anyMatch(e -> e.path().equals("/api/orders"));
        assertThat(result.getApiEndpoints()).noneMatch(e -> e.path().contains("/api/domain-items"));
        assertThat(result.getDtoCandidates()).anyMatch(d -> d.getName().equals("OrderSearchRequest"));
        assertThat(result.getInputSummary()).contains("対象ドメイン: 注文管理 / 在庫管理 / 商品管理");
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

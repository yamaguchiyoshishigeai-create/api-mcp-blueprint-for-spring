package com.example.apim.service;

import com.example.apim.model.BlueprintInput;
import com.example.apim.support.DomainNameNormalizer;
import com.example.apim.support.NamingSupport;
import com.example.apim.support.OperationClassifier;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class BlueprintGenerationServiceTest {

    @Test
    void generatesNonEmptyBlueprintResult() {
        BlueprintGenerationService service = new BlueprintGenerationService(
                new OperationClassifier(),
                new DomainNameNormalizer(),
                new ApiDesignGenerator(),
                new DtoCandidateGenerator(),
                new ControllerSkeletonGenerator(new NamingSupport()),
                new McpDesignGenerator(),
                new SecurityNotesGenerator(),
                new MarkdownDocumentGenerator(),
                new ImplementationInstructionGenerator()
        );

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
        BlueprintGenerationService service = new BlueprintGenerationService(
                new OperationClassifier(),
                new DomainNameNormalizer(),
                new ApiDesignGenerator(),
                new DtoCandidateGenerator(),
                new ControllerSkeletonGenerator(new NamingSupport()),
                new McpDesignGenerator(),
                new SecurityNotesGenerator(),
                new MarkdownDocumentGenerator(),
                new ImplementationInstructionGenerator()
        );

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
}

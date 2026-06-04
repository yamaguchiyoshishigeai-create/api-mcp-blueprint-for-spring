package com.example.apim.service;

import com.example.apim.model.BlueprintInput;
import com.example.apim.model.BlueprintResult;
import com.example.apim.model.ExternalAiImportResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.example.apim.support.DomainNameNormalizer;
import com.example.apim.support.NamingSupport;
import com.example.apim.support.OperationClassifier;
import com.example.apim.testsupport.BlueprintInputFixtures;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

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
        input.setSystemTypes(java.util.List.of("EC / 販売管理", "資産・備品管理"));
        input.setPrimaryDomain("注文管理");
        input.setRelatedDomains(java.util.List.of("在庫管理", "商品管理"));
        input.setUserTypes("営業担当\n管理者");
        input.setRequiredOperations("注文検索\n注文更新");
        input.setAllowedAiOperations("注文検索\n注文更新案の作成");

        var result = newService().generate(input);

        assertThat(result.getApiEndpoints()).anyMatch(e -> e.path().equals("/api/orders"));
        assertThat(result.getApiEndpoints()).anyMatch(e -> e.path().equals("/api/orders")
                && e.domainRole().equals("主ドメインAPI"));
        assertThat(result.getApiEndpoints()).anyMatch(e -> e.path().equals("/api/inventory")
                && e.domainRole().equals("関連ドメイン参照API"));
        assertThat(result.getApiEndpoints()).anyMatch(e -> e.path().equals("/api/products/{id}")
                && e.domainName().equals("商品管理"));
        assertThat(result.getApiEndpoints()).noneMatch(e -> e.path().equals("/api/inventory")
                && e.httpMethod().equals("POST"));
        assertThat(result.getMcpTools()).anyMatch(t -> t.name().equals("searchOrders")
                && t.purpose().contains("主ドメインtool(注文管理)"));
        assertThat(result.getMcpTools()).anyMatch(t -> t.name().equals("searchInventoryReferences")
                && t.purpose().contains("関連ドメイン参照tool(在庫管理)")
                && t.operationType().equals("read"));
        assertThat(result.getMcpTools()).noneMatch(t -> t.relatedApi().startsWith("/api/inventory")
                && t.operationType().equals("write"));
        assertThat(result.getMcpResources()).anyMatch(r -> r.name().equals("orders-catalog")
                && r.purpose().contains("主ドメインresource(注文管理)"));
        assertThat(result.getMcpResources()).anyMatch(r -> r.name().equals("products-reference-catalog")
                && r.purpose().contains("関連ドメインresource(商品管理)")
                && r.scope().equals("read-only"));
        assertThat(result.getMcpPrompts()).anyMatch(p -> p.name().equals("analyze-orders-cross-domain-requirements")
                && p.promptTemplate().contains("禁止事項")
                && p.promptTemplate().contains("人間確認条件"));
        assertThat(result.getApiMcpMappings()).anyMatch(m -> m.apiPath().equals("/api/orders")
                && m.toolName().equals("searchOrders")
                && m.notes().contains("主ドメインAPI(注文管理)"));
        assertThat(result.getApiMcpMappings()).anyMatch(m -> m.apiPath().equals("/api/inventory")
                && m.toolName().equals("searchInventoryReferences")
                && m.notes().contains("関連ドメイン参照API(在庫管理)"));
        assertThat(result.getApiEndpoints()).noneMatch(e -> e.path().contains("/api/domain-items"));
        assertThat(result.getDtoCandidates()).anyMatch(d -> d.getName().equals("OrderSearchRequest"));
        assertThat(result.getDtoCandidates()).anyMatch(d -> d.getName().equals("InventoryReferenceSearchRequest"));
        assertThat(result.getDtoCandidates()).anyMatch(d -> d.getName().equals("ProductReferenceResponse"));
        assertThat(result.getControllerSkeleton().sourceCode())
                .contains("public class OrderController")
                .contains("関連ドメイン参照Controller候補: InventoryReferenceController (/api/inventory)")
                .contains("関連ドメイン参照Controller候補: ProductReferenceController (/api/products)")
                .contains("@RequestMapping(\"/api/orders\")")
                .doesNotContain("public class InventoryReferenceController")
                .doesNotContain("public class ProductReferenceController");
        assertThat(result.getControllerSkeleton().sourceCode())
                .containsOnlyOnce("package com.example.generated.controller;")
                .containsOnlyOnce("import org.springframework.web.bind.annotation.*;");
        assertThat(result.getInputSummary()).contains("対象ドメイン: 注文管理 / 在庫管理 / 商品管理");
        assertThat(result.getBlueprintMarkdown())
                .contains("- 対象システム種別: EC / 販売管理 / 資産・備品管理")
                .contains("- 主ドメイン: 注文管理")
                .contains("- 関連ドメイン: 在庫管理 / 商品管理")
                .contains("- 正規化後ドメイン一覧: 注文管理 / 在庫管理 / 商品管理")
                .contains("主ドメインAPI(注文管理):")
                .contains("関連ドメイン参照API(在庫管理):");
        assertThat(result.getImplementationInstructions())
                .contains("### ドメイン実装境界")
                .contains("関連ドメインは参照・連携境界として扱い")
                .contains("- 対象システム種別: EC / 販売管理 / 資産・備品管理")
                .contains("searchOrders")
                .contains("searchInventoryReferences")
                .contains("## 8. API&MCP対応方針")
                .doesNotContain("APIM for Spring のMVPを段階実装する");
    }

    @Test
    void supportInquirySampleGeneratesDistinctApiMcpAndImplementationInstructions() {
        BlueprintInput input = new BlueprintInput();
        input.setBusinessRequirements("""
                問い合わせ・サポート管理として、問い合わせ受付、分類、FAQ検索、AI要約、返信下書き作成を扱う。
                回答確定送信と重要問い合わせの状態変更は人間確認後に実行する。
                """);
        input.setTargetDomain("問い合わせ管理 / FAQ管理 / ナレッジ検索・要約 / 顧客管理 / 通知管理");
        input.setSystemTypes(java.util.List.of("support-management", "knowledge-platform"));
        input.setPrimaryDomain("問い合わせ管理");
        input.setRelatedDomains(java.util.List.of("問い合わせ管理", "FAQ管理", "ナレッジ検索・要約", "顧客管理", "通知管理"));
        input.setUserTypes("- サポート担当\n- 品質管理担当\n- 管理者\n- AIアシスタント");
        input.setRequiredOperations("""
                - 問い合わせ検索
                - 問い合わせ詳細取得
                - 問い合わせ受付登録
                - 問い合わせ分類
                - FAQ検索
                - 問い合わせ要約
                - 返信下書き作成
                - 回答確定通知
                """);
        input.setAllowedAiOperations("""
                - 問い合わせ検索
                - 問い合わせ詳細参照
                - FAQ検索
                - 問い合わせ要約
                - 返信下書き作成
                """);
        input.setWriteOperations("- 問い合わせ受付登録\n- 問い合わせ分類更新\n- 回答確定通知");
        input.setApprovalRequiredOperations("- 回答確定送信\n- 重要問い合わせの状態変更\n- 顧客情報更新");
        input.setAuditLogRequiredOperations("- 問い合わせ分類更新\n- AIによる問い合わせ要約\n- 返信下書き作成\n- 回答確定送信");
        input.setAuthenticationMethod("OAuth2 / OIDC");
        input.setTargetUsers("サポート担当、品質管理担当、管理者、AIアシスタント");
        input.setOutputLanguage("日本語");

        var result = newService().generate(input);

        assertThat(result.getApiEndpoints()).anyMatch(e -> e.path().equals("/api/inquiries") && e.httpMethod().equals("GET"));
        assertThat(result.getApiEndpoints()).anyMatch(e -> e.path().equals("/api/inquiries/{id}/summary"));
        assertThat(result.getApiEndpoints()).anyMatch(e -> e.path().equals("/api/faqs")
                && e.domainRole().equals("関連ドメイン参照API"));
        assertThat(result.getMcpTools()).anyMatch(t -> t.name().equals("searchInquiries"));
        assertThat(result.getMcpTools()).anyMatch(t -> t.name().equals("getFaqReferenceDetail"));
        assertThat(result.getMcpResources()).anyMatch(r -> r.name().equals("inquiries-catalog"));
        assertThat(result.getMcpPrompts()).anyMatch(p -> p.name().equals("analyze-inquiries-cross-domain-requirements"));
        assertThat(result.getApiMcpMappings()).anyMatch(m -> m.apiPath().equals("/api/inquiries")
                && m.toolName().equals("searchInquiries"));
        assertThat(result.getImplementationInstructions())
                .contains("- 対象システム種別: support-management / knowledge-platform")
                .contains("- 主ドメイン: 問い合わせ管理")
                .contains("searchInquiries")
                .contains("searchFaqReferences")
                .contains("回答確定送信");
        assertThat(result.getBlueprintMarkdown())
                .contains("- 主ドメイン: 問い合わせ管理")
                .contains("関連ドメイン参照API(FAQ管理):");
    }

    @Test
    void generationServiceUsesV2SafetyClassifiedMcpToolsAndMappings() {
        BlueprintInput input = new BlueprintInput();
        input.setBusinessRequirements("""
                問い合わせを検索・参照し、AIで返信下書きと更新案を作成する。
                回答確定送信は承認後に実行し、監査ログを必須にする。
                """);
        input.setTargetDomain("問い合わせ管理");
        input.setPrimaryDomain("問い合わせ管理");
        input.setUserTypes("- サポート担当\n- 管理者\n- AIアシスタント");
        input.setRequiredOperations("""
                - 問い合わせ検索
                - 問い合わせ詳細取得
                - 問い合わせ更新
                - 返信下書き作成
                - 回答確定送信
                """);
        input.setAllowedAiOperations("""
                - 問い合わせ検索
                - 問い合わせ詳細参照
                - 返信下書き作成
                - 問い合わせ更新案の作成
                """);
        input.setWriteOperations("- 回答確定送信\n- 問い合わせ更新");
        input.setApprovalRequiredOperations("- 回答確定送信");
        input.setAuditLogRequiredOperations("- 返信下書き作成\n- 回答確定送信");

        var result = newService().generate(input);

        assertThat(result.getMcpTools()).anyMatch(t -> t.name().equals("createInquiryDraft")
                && t.operationType().equals("draft")
                && t.auditLogRequired().equals("必須"));
        assertThat(result.getMcpTools()).anyMatch(t -> t.name().equals("proposeInquiryUpdate")
                && t.operationType().equals("proposal"));
        assertThat(result.getMcpTools()).anyMatch(t -> t.name().equals("requestInquiriesNotificationApproval")
                && t.operationType().equals("approval-request")
                && t.aiExecutionPolicy().contains("AI直接実行不可")
                && t.approvalRequired().equals("必須")
                && t.auditLogRequired().equals("必須"));
        assertThat(result.getApiMcpMappings()).anyMatch(m -> m.toolName().equals("requestInquiriesNotificationApproval")
                && m.notes().contains("AI:")
                && m.notes().contains("Approval: 必須")
                && m.notes().contains("Audit: 必須"));
    }

    @Test
    void salesContractBillingV2SampleGeneratesBusinessObjectOperationApis() throws Exception {
        String json = Files.readString(Path.of("docs", "20_設計", "自由文構造化v2", "samples",
                "sales-contract-billing.v2.json"), StandardCharsets.UTF_8);
        ExternalAiImportResult importResult = new ExternalAiPromptBridgeService(new ObjectMapper()).importJson(json);
        assertThat(importResult.valid()).isTrue();
        assertThat(importResult.canGenerate()).isTrue();

        BlueprintResult result = newService().generate(importResult.blueprintInput());
        List<String> paths = result.getApiEndpoints().stream()
                .map(endpoint -> endpoint.httpMethod() + " " + endpoint.path())
                .toList();

        assertThat(paths).contains(
                "GET /api/customers",
                "GET /api/opportunities",
                "GET /api/quotes",
                "GET /api/contracts",
                "GET /api/invoices",
                "GET /api/payments",
                "GET /api/opportunities/{id}/history-summary",
                "GET /api/opportunities/loss-risk-candidates",
                "POST /api/opportunities/{id}/follow-up-drafts",
                "POST /api/quotes/{id}/change-requests",
                "POST /api/contracts/{id}/condition-change-requests",
                "POST /api/invoices/{id}/confirmation-requests",
                "POST /api/payments/{id}/reconciliation-requests"
        );
        assertThat(paths.stream().filter(path -> path.contains("/api/invoices")).count()).isLessThan(paths.size() / 2);
        assertThat(result.getApiEndpoints()).noneMatch(endpoint -> endpoint.httpMethod().equals("PUT")
                && (endpoint.path().contains("/api/quotes")
                || endpoint.path().contains("/api/contracts")
                || endpoint.path().contains("/api/invoices")
                || endpoint.path().contains("/api/payments")));
        assertThat(result.getApiEndpoints()).anyMatch(endpoint -> endpoint.path().equals("/api/quotes/{id}/change-requests")
                && endpoint.approvalRequired().equals("必須"));
        assertThat(result.getApiEndpoints()).anyMatch(endpoint -> endpoint.path().equals("/api/invoices/{id}/confirmation-requests")
                && endpoint.auditLogRequired().equals("必須"));
        assertThat(result.getBlueprintMarkdown())
                .contains(
                        "営業案件管理",
                        "契約請求管理",
                        "顧客",
                        "商談",
                        "見積",
                        "契約",
                        "請求",
                        "入金",
                        "営業担当",
                        "契約担当者",
                        "商談履歴要約",
                        "失注リスク候補提示",
                        "フォローアップ文案作成",
                        "見積金額変更",
                        "受注確度変更",
                        "契約条件変更",
                        "請求確定",
                        "入金消込",
                        "## 3. 抽出された業務構造",
                        "### 3.5 関係性",
                        "## 4. AI支援・承認・監査の分類",
                        "### 4.1 AI支援可能操作",
                        "### 4.2 人間承認必須操作",
                        "### 4.3 監査ログ必須操作",
                        "### 4.4 AI直接実行不可操作",
                        "## 5. 曖昧点・確認事項",
                        "## 6. REST API候補",
                        "## 7. MCP tools候補",
                        "承認要否",
                        "AI Execution Policy");
        assertThat(result.getBlueprintMarkdown().indexOf("## 3. 抽出された業務構造"))
                .isLessThan(result.getBlueprintMarkdown().indexOf("## 6. REST API候補"));
        assertThat(result.getBlueprintMarkdown().indexOf("## 4. AI支援・承認・監査の分類"))
                .isLessThan(result.getBlueprintMarkdown().indexOf("## 6. REST API候補"));
        assertThat(result.getBlueprintMarkdown().indexOf("## 5. 曖昧点・確認事項"))
                .isLessThan(result.getBlueprintMarkdown().indexOf("## 6. REST API候補"));
        assertThat(result.getBlueprintMarkdown().indexOf("## 6. REST API候補"))
                .isLessThan(result.getBlueprintMarkdown().indexOf("## 7. MCP tools候補"));
        assertThat(majorGeneratedNames(result))
                .doesNotContain("DomainItem")
                .doesNotContain("domain-items")
                .doesNotContain("executeDomainItem");
    }

    @Test
    void legacyTargetDomainOnlyKeepsSingleDomainApiDesignCandidates() {
        BlueprintInput input = new BlueprintInput();
        input.setBusinessRequirements("顧客情報を検索して詳細参照する。");
        input.setTargetDomain("顧客管理");
        input.setUserTypes("営業担当");
        input.setRequiredOperations("顧客検索\n顧客詳細取得");
        input.setAllowedAiOperations("顧客検索");

        var result = newService().generate(input);

        assertThat(result.getApiEndpoints()).anyMatch(e -> e.path().equals("/api/customers"));
        assertThat(result.getApiEndpoints()).anyMatch(e -> e.path().equals("/api/customers/{id}"));
        assertThat(result.getApiEndpoints()).allMatch(e -> e.domainRole().isBlank());
        assertThat(result.getDtoCandidates()).anyMatch(d -> d.getName().equals("CustomerSearchRequest"));
        assertThat(result.getControllerSkeleton().sourceCode())
                .contains("public class CustomerController")
                .doesNotContain("ReferenceController");
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

    private String majorGeneratedNames(BlueprintResult result) {
        List<String> names = new ArrayList<>();
        names.addAll(result.getApiEndpoints().stream().map(endpoint -> endpoint.path()).toList());
        names.addAll(result.getDtoCandidates().stream().map(dto -> dto.getName()).toList());
        names.addAll(result.getMcpTools().stream().map(tool -> tool.name()).toList());
        names.addAll(result.getMcpTools().stream().map(tool -> tool.relatedApi()).toList());
        names.add(result.getControllerSkeleton().className());
        names.add(result.getControllerSkeleton().sourceCode());
        return String.join("\n", names);
    }

}

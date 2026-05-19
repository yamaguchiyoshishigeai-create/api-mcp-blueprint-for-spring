package com.example.apim.service;

import com.example.apim.model.ApiEndpointCandidate;
import com.example.apim.model.BlueprintInput;
import com.example.apim.model.NormalizedBlueprintInput;
import com.example.apim.support.DomainNameNormalizer;
import com.example.apim.support.OperationType;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class McpDesignGeneratorTest {

    @Test
    void generatesSearchCustomersTool() {
        McpDesignGenerator generator = new McpDesignGenerator();
        List<ApiEndpointCandidate> endpoints = List.of(
                new ApiEndpointCandidate("GET", "/api/customers", "検索", "営業担当",
                        "CustomerSearchRequest", "CustomerSummaryResponse", "閲覧権限", "不要", "推奨")
        );

        McpDesignGenerator.McpDesignResult result = generator.generate(
                "Customer", "customers", Set.of(OperationType.SEARCH), endpoints
        );

        assertThat(result.tools()).anyMatch(t -> t.name().equals("searchCustomers"));
    }

    @Test
    void generatesBusinessVocabularyToolsForApplicationsAndKnowledgeArticles() {
        McpDesignGenerator generator = new McpDesignGenerator();
        List<ApiEndpointCandidate> applicationEndpoints = List.of(
                new ApiEndpointCandidate("GET", "/api/applications", "検索", "申請者",
                        "ApplicationSearchRequest", "ApplicationSummaryResponse", "閲覧権限", "不要", "推奨"),
                new ApiEndpointCandidate("GET", "/api/applications/{id}", "詳細取得", "申請者",
                        "", "ApplicationResponse", "閲覧権限", "不要", "推奨")
        );

        McpDesignGenerator.McpDesignResult appResult = generator.generate(
                "Application",
                "applications",
                Set.of(OperationType.SEARCH, OperationType.READ),
                applicationEndpoints
        );

        assertThat(appResult.tools()).anyMatch(t -> t.name().equals("searchApplications"));
        assertThat(appResult.tools()).anyMatch(t -> t.name().equals("getApplicationDetail"));

        List<ApiEndpointCandidate> knowledgeEndpoints = List.of(
                new ApiEndpointCandidate("GET", "/api/knowledge-articles", "検索", "ナレッジ管理者",
                        "KnowledgeArticleSearchRequest", "KnowledgeArticleSummaryResponse", "閲覧権限", "不要", "推奨")
        );
        McpDesignGenerator.McpDesignResult knowledgeResult = generator.generate(
                "KnowledgeArticle",
                "knowledge-articles",
                Set.of(OperationType.SEARCH),
                knowledgeEndpoints
        );

        assertThat(knowledgeResult.tools()).anyMatch(t -> t.name().equals("searchKnowledgeArticles"));
    }

    @Test
    void keepsApprovalRequiredPolicyForDeletionAndApprovalTools() {
        McpDesignGenerator generator = new McpDesignGenerator();
        List<ApiEndpointCandidate> endpoints = List.of(
                new ApiEndpointCandidate("DELETE", "/api/equipment/{id}", "削除", "管理者",
                        "", "ApprovalResponse", "削除権限", "必須", "必須"),
                new ApiEndpointCandidate("POST", "/api/equipment/{id}/approval-requests", "承認依頼", "管理者",
                        "EquipmentApprovalRequest", "ApprovalResponse", "承認依頼権限", "必須", "必須")
        );

        McpDesignGenerator.McpDesignResult result = generator.generate(
                "Equipment",
                "equipment",
                Set.of(OperationType.DELETE, OperationType.APPROVAL),
                endpoints
        );

        assertThat(result.tools()).anyMatch(t ->
                t.name().equals("requestEquipmentDeletionApproval")
                        && t.aiExecutionPolicy().equals("AI実行不可")
                        && t.approvalRequired().equals("必須")
        );
        assertThat(result.tools()).anyMatch(t ->
                t.name().equals("requestApprovalForEquipment")
                        && t.approvalRequired().equals("必須")
                        && t.auditLogRequired().equals("必須")
        );
    }

    @Test
    void separatesPrimaryDomainToolsAndRelatedDomainReferenceTools() {
        McpDesignGenerator generator = new McpDesignGenerator();
        DomainNameNormalizer normalizer = new DomainNameNormalizer();
        NormalizedBlueprintInput normalizedInput = new NormalizedBlueprintInput(
                List.of("EC / 販売管理"),
                "注文管理",
                List.of("商品管理", "在庫管理"),
                List.of("注文管理", "商品管理", "在庫管理"),
                "注文管理 / 商品管理 / 在庫管理",
                new BlueprintInput()
        );
        List<ApiEndpointCandidate> endpoints = new ApiDesignGenerator().generate(
                normalizedInput,
                normalizer,
                Set.of(OperationType.SEARCH, OperationType.READ, OperationType.UPDATE),
                "営業担当 / 管理者"
        );

        McpDesignGenerator.McpDesignResult result = generator.generate(
                normalizedInput,
                normalizer,
                Set.of(OperationType.SEARCH, OperationType.READ, OperationType.UPDATE),
                endpoints
        );

        assertThat(result.tools()).anyMatch(t ->
                t.name().equals("searchOrders")
                        && t.purpose().contains("主ドメインtool(注文管理)")
                        && t.relatedApi().equals("/api/orders")
        );
        assertThat(result.tools()).anyMatch(t ->
                t.name().equals("searchProductReferences")
                        && t.purpose().contains("関連ドメイン参照tool(商品管理)")
                        && t.relatedApi().equals("/api/products")
                        && t.operationType().equals("read")
                        && t.approvalRequired().equals("不要")
        );
        assertThat(result.tools()).anyMatch(t ->
                t.name().equals("getInventoryReferenceDetail")
                        && t.purpose().contains("関連ドメイン参照tool(在庫管理)")
                        && t.relatedApi().equals("/api/inventory/{id}")
        );
        assertThat(result.tools()).noneMatch(t ->
                t.relatedApi().startsWith("/api/products") && t.operationType().equals("write")
        );
    }

    @Test
    void generatesDomainBoundaryResourcesAndCrossDomainPrompts() {
        McpDesignGenerator generator = new McpDesignGenerator();
        DomainNameNormalizer normalizer = new DomainNameNormalizer();
        NormalizedBlueprintInput normalizedInput = new NormalizedBlueprintInput(
                List.of("EC / 販売管理"),
                "注文管理",
                List.of("商品管理"),
                List.of("注文管理", "商品管理"),
                "注文管理 / 商品管理",
                new BlueprintInput()
        );
        List<ApiEndpointCandidate> endpoints = new ApiDesignGenerator().generate(
                normalizedInput,
                normalizer,
                Set.of(OperationType.SEARCH, OperationType.READ),
                "営業担当"
        );

        McpDesignGenerator.McpDesignResult result = generator.generate(
                normalizedInput,
                normalizer,
                Set.of(OperationType.SEARCH, OperationType.READ),
                endpoints
        );

        assertThat(result.resources()).anyMatch(r ->
                r.name().equals("orders-catalog")
                        && r.purpose().contains("主ドメインresource(注文管理)")
                        && r.scope().equals("read-only")
        );
        assertThat(result.resources()).anyMatch(r ->
                r.name().equals("products-reference-catalog")
                        && r.purpose().contains("関連ドメインresource(商品管理)")
                        && r.scope().equals("read-only")
        );
        assertThat(result.prompts()).anyMatch(p ->
                p.name().equals("analyze-orders-cross-domain-requirements")
                        && p.promptTemplate().contains("利用場面")
                        && p.promptTemplate().contains("禁止事項")
                        && p.promptTemplate().contains("人間確認条件")
                        && p.promptTemplate().contains("センシティブ情報")
        );
    }

    @Test
    void apiMcpMappingsFollowPrimaryAndRelatedApiBoundaries() {
        McpDesignGenerator generator = new McpDesignGenerator();
        DomainNameNormalizer normalizer = new DomainNameNormalizer();
        NormalizedBlueprintInput normalizedInput = new NormalizedBlueprintInput(
                List.of("EC / 販売管理"),
                "注文管理",
                List.of("商品管理"),
                List.of("注文管理", "商品管理"),
                "注文管理 / 商品管理",
                new BlueprintInput()
        );
        List<ApiEndpointCandidate> endpoints = new ApiDesignGenerator().generate(
                normalizedInput,
                normalizer,
                Set.of(OperationType.SEARCH, OperationType.READ, OperationType.UPDATE),
                "営業担当"
        );

        McpDesignGenerator.McpDesignResult result = generator.generate(
                normalizedInput,
                normalizer,
                Set.of(OperationType.SEARCH, OperationType.READ, OperationType.UPDATE),
                endpoints
        );

        assertThat(result.mappings()).anyMatch(m ->
                m.apiPath().equals("/api/orders")
                        && m.toolName().equals("searchOrders")
                        && m.notes().contains("主ドメインAPI(注文管理)")
        );
        assertThat(result.mappings()).anyMatch(m ->
                m.apiPath().equals("/api/products")
                        && m.toolName().equals("searchProductReferences")
                        && m.notes().contains("関連ドメイン参照API(商品管理)")
                        && m.notes().contains("AI:")
                        && m.notes().contains("Audit:")
        );
    }

    @Test
    void generatesSafetyClassifiedToolsFromV2OperationBuckets() {
        McpDesignGenerator generator = new McpDesignGenerator();
        DomainNameNormalizer normalizer = new DomainNameNormalizer();
        BlueprintInput input = new BlueprintInput();
        input.setTargetDomain("問い合わせ管理");
        input.setPrimaryDomain("問い合わせ管理");
        input.setAllowedAiOperations("- 問い合わせ検索\n- 問い合わせ要約\n- 対応候補抽出\n- 返信下書き作成\n- 問い合わせ更新案の作成");
        input.setReadOnlyOperations("- 問い合わせ詳細参照");
        input.setWriteOperations("- 回答確定送信\n- 問い合わせ分類更新");
        input.setApprovalRequiredOperations("- 回答確定送信\n- 重要問い合わせの状態変更");
        input.setAuditLogRequiredOperations("- 問い合わせ要約\n- 返信下書き作成\n- 回答確定送信");
        NormalizedBlueprintInput normalizedInput = new NormalizedBlueprintInput(
                List.of("support-management"),
                "問い合わせ管理",
                List.of(),
                List.of("問い合わせ管理"),
                "問い合わせ管理",
                input
        );
        List<ApiEndpointCandidate> endpoints = new ApiDesignGenerator().generate(
                "inquiries",
                "Inquiry",
                Set.of(OperationType.SEARCH, OperationType.READ, OperationType.UPDATE, OperationType.SUMMARY,
                        OperationType.NOTIFICATION),
                "サポート担当"
        );

        McpDesignGenerator.McpDesignResult result = generator.generate(
                input,
                normalizedInput,
                normalizer,
                Set.of(OperationType.SEARCH, OperationType.READ, OperationType.UPDATE, OperationType.SUMMARY,
                        OperationType.NOTIFICATION),
                endpoints
        );

        assertThat(result.tools()).anyMatch(t -> t.name().equals("searchInquiries")
                && t.operationType().equals("read")
                && t.aiExecutionPolicy().equals("AI実行可"));
        assertThat(result.tools()).anyMatch(t -> t.name().equals("summarizeInquiryInteractions")
                && t.operationType().equals("summary")
                && t.auditLogRequired().equals("必須"));
        assertThat(result.tools()).anyMatch(t -> t.name().equals("suggestInquiryCandidates")
                && t.operationType().equals("candidate")
                && t.aiExecutionPolicy().contains("候補作成のみ"));
        assertThat(result.tools()).anyMatch(t -> t.name().equals("createInquiryDraft")
                && t.operationType().equals("draft")
                && t.aiExecutionPolicy().contains("下書き作成のみ")
                && t.auditLogRequired().equals("必須"));
        assertThat(result.tools()).anyMatch(t -> t.name().equals("proposeInquiryUpdate")
                && t.operationType().equals("proposal")
                && t.aiExecutionPolicy().contains("変更提案のみ"));
        assertThat(result.tools()).anyMatch(t -> t.name().equals("requestInquiriesNotificationApproval")
                && t.operationType().equals("approval-request")
                && t.aiExecutionPolicy().contains("AI直接実行不可")
                && t.approvalRequired().equals("必須")
                && t.auditLogRequired().equals("必須"));
        assertThat(result.tools()).anyMatch(t -> t.name().equals("executeInquiryNotificationAfterApproval")
                && t.operationType().equals("execution")
                && t.aiExecutionPolicy().contains("AI直接実行不可"));
        assertThat(result.mappings()).anyMatch(m -> m.toolName().equals("requestInquiriesNotificationApproval")
                && m.notes().contains("AI:")
                && m.notes().contains("Approval: 必須")
                && m.notes().contains("Audit: 必須"));
    }

    @Test
    void v2OperationLabelsNeverDriveDomainItemFallbackNames() {
        McpDesignGenerator generator = new McpDesignGenerator();
        DomainNameNormalizer normalizer = new DomainNameNormalizer();
        BlueprintInput input = new BlueprintInput();
        input.setTargetDomain("備品貸出管理");
        input.setPrimaryDomain("備品貸出管理");
        input.setAllowedAiOperations("- 対象を確認\n- 更新案の作成");
        input.setApprovalRequiredOperations("- 対象を削除\n- 対象を外部送信");
        input.setAuditLogRequiredOperations("- 対象を削除\n- 対象を外部送信");
        NormalizedBlueprintInput normalizedInput = new NormalizedBlueprintInput(
                List.of("asset-management"),
                "備品貸出管理",
                List.of(),
                List.of("備品貸出管理"),
                "備品貸出管理",
                input
        );
        List<ApiEndpointCandidate> endpoints = new ApiDesignGenerator().generate(
                "equipment",
                "Equipment",
                Set.of(OperationType.READ, OperationType.UPDATE, OperationType.DELETE, OperationType.NOTIFICATION),
                "管理者"
        );

        McpDesignGenerator.McpDesignResult result = generator.generate(
                input,
                normalizedInput,
                normalizer,
                Set.of(OperationType.READ, OperationType.UPDATE, OperationType.DELETE, OperationType.NOTIFICATION),
                endpoints
        );

        String generated = String.join("\n",
                result.tools().stream().flatMap(t -> java.util.stream.Stream.of(t.name(), t.purpose(), t.relatedApi())).toList());
        assertThat(result.tools()).anyMatch(t -> t.name().equals("getEquipmentDetail"));
        assertThat(result.tools()).anyMatch(t -> t.name().equals("proposeEquipmentUpdate"));
        assertThat(result.tools()).anyMatch(t -> t.name().equals("requestEquipmentDeletionApproval"));
        assertThat(generated).doesNotContain("DomainItem", "domain-items", "executeDomainItem");
    }
}

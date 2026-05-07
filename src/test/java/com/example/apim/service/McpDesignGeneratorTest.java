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
}

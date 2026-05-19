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

class ApiDesignGeneratorTest {

    @Test
    void generatesSearchEndpointForCustomerDomain() {
        ApiDesignGenerator generator = new ApiDesignGenerator();
        List<ApiEndpointCandidate> endpoints = generator.generate(
                "customers",
                "Customer",
                Set.of(OperationType.SEARCH),
                "営業担当"
        );

        assertThat(endpoints)
                .anyMatch(e -> e.httpMethod().equals("GET") && e.path().equals("/api/customers"));
    }

    @Test
    void generatesKnowledgeArticleEndpointsWithoutDomainItemsFallback() {
        ApiDesignGenerator generator = new ApiDesignGenerator();
        List<ApiEndpointCandidate> endpoints = generator.generate(
                "knowledge-articles",
                "KnowledgeArticle",
                Set.of(OperationType.SEARCH, OperationType.READ, OperationType.SUMMARY),
                "ナレッジ管理者"
        );

        assertThat(endpoints).anyMatch(e -> e.path().equals("/api/knowledge-articles"));
        assertThat(endpoints).anyMatch(e -> e.path().equals("/api/knowledge-articles/{id}"));
        assertThat(endpoints).anyMatch(e -> e.path().equals("/api/knowledge-articles/{id}/summary"));
        assertThat(endpoints).noneMatch(e -> e.path().contains("/api/domain-items"));
    }

    @Test
    void separatesPrimaryDomainApiAndRelatedDomainReferenceApiCandidates() {
        ApiDesignGenerator generator = new ApiDesignGenerator();
        NormalizedBlueprintInput normalizedInput = new NormalizedBlueprintInput(
                List.of("EC / 販売管理"),
                "注文管理",
                List.of("商品管理", "在庫管理"),
                List.of("注文管理", "商品管理", "在庫管理"),
                "注文管理 / 商品管理 / 在庫管理",
                new BlueprintInput()
        );

        List<ApiEndpointCandidate> endpoints = generator.generate(
                normalizedInput,
                new DomainNameNormalizer(),
                Set.of(OperationType.SEARCH, OperationType.READ, OperationType.UPDATE),
                "営業担当 / 管理者"
        );

        assertThat(endpoints)
                .anyMatch(e -> e.path().equals("/api/orders") && e.domainRole().equals("主ドメインAPI"));
        assertThat(endpoints)
                .anyMatch(e -> e.path().equals("/api/products") && e.domainRole().equals("関連ドメイン参照API"));
        assertThat(endpoints)
                .anyMatch(e -> e.path().equals("/api/inventory/{id}") && e.domainName().equals("在庫管理"));
        assertThat(endpoints)
                .noneMatch(e -> e.path().equals("/api/products") && e.httpMethod().equals("POST"));
    }

    @Test
    void generatesEndpointsFromV2BusinessObjectsAndOperations() {
        BlueprintInput input = new BlueprintInput();
        input.setV2BusinessObjects(List.of(
                new BlueprintInput.V2BusinessObject("customer", "顧客", "sales", "confidential", List.of()),
                new BlueprintInput.V2BusinessObject("opportunity", "商談", "sales", "confidential", List.of()),
                new BlueprintInput.V2BusinessObject("quote", "見積", "sales", "confidential", List.of()),
                new BlueprintInput.V2BusinessObject("invoice", "請求", "billing", "restricted", List.of())
        ));
        input.setV2Actors(List.of(new BlueprintInput.V2Actor("sales_rep", "営業担当", "human_user")));
        input.setV2Operations(List.of(
                new BlueprintInput.V2Operation("search_customers", "顧客検索", "", List.of("sales_rep"),
                        List.of("customer"), "search", "direct_read", "allowed", false, "recommended",
                        "low", false, false, "list"),
                new BlueprintInput.V2Operation("summarize_opportunity_history", "商談履歴要約", "",
                        List.of("sales_rep"), List.of("opportunity"), "ai_summary", "ai_assisted",
                        "allowed", false, "recommended", "medium", false, false, "summary"),
                new BlueprintInput.V2Operation("draft_followup_message", "フォローアップ文案作成", "",
                        List.of("sales_rep"), List.of("customer", "opportunity"), "ai_draft", "draft_only",
                        "allowed", false, "recommended", "medium", false, false, "draft_text"),
                new BlueprintInput.V2Operation("request_quote_amount_change", "見積金額変更依頼", "",
                        List.of("sales_rep"), List.of("quote"), "approval_request", "human_approved_write",
                        "not_allowed_directly", true, "required", "high", false, true, "approval_request"),
                new BlueprintInput.V2Operation("request_invoice_confirmation", "請求確定依頼", "",
                        List.of("sales_rep"), List.of("invoice"), "approval_request", "human_approved_write",
                        "not_allowed_directly", true, "required", "high", false, true, "approval_request")
        ));

        List<ApiEndpointCandidate> endpoints = new ApiDesignGenerator().generateFromV2(input, new DomainNameNormalizer());

        assertThat(endpoints).anyMatch(e -> e.path().equals("/api/customers") && e.httpMethod().equals("GET"));
        assertThat(endpoints).anyMatch(e -> e.path().equals("/api/opportunities/{id}/history-summary"));
        assertThat(endpoints).anyMatch(e -> e.path().equals("/api/opportunities/{id}/follow-up-drafts")
                && e.httpMethod().equals("POST"));
        assertThat(endpoints).anyMatch(e -> e.path().equals("/api/quotes/{id}/change-requests")
                && e.approvalRequired().equals("必須"));
        assertThat(endpoints).anyMatch(e -> e.path().equals("/api/invoices/{id}/confirmation-requests")
                && e.auditLogRequired().equals("必須"));
        assertThat(endpoints).noneMatch(e -> e.path().contains("domain-items"));
        assertThat(endpoints).noneMatch(e -> e.httpMethod().equals("PUT"));
    }
}

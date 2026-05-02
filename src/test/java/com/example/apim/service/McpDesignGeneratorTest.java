package com.example.apim.service;

import com.example.apim.model.ApiEndpointCandidate;
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
}

package com.example.apim.service;

import com.example.apim.model.ApiEndpointCandidate;
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
}

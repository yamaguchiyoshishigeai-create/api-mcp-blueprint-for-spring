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
}

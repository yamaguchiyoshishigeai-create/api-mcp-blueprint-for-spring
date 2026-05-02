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
}

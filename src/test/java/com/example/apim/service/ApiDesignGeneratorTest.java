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
}

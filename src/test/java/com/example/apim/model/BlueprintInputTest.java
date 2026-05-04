package com.example.apim.model;

import java.util.List;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class BlueprintInputTest {

    @Test
    void defaultsAndNullSafetyArePreservedForNewFields() {
        BlueprintInput input = new BlueprintInput();

        assertThat(input.getSystemTypes()).isEmpty();
        assertThat(input.getPrimaryDomain()).isEmpty();
        assertThat(input.getRelatedDomains()).isEmpty();

        input.setSystemTypes(null);
        input.setPrimaryDomain(null);
        input.setRelatedDomains(null);

        assertThat(input.getSystemTypes()).isEmpty();
        assertThat(input.getPrimaryDomain()).isEmpty();
        assertThat(input.getRelatedDomains()).isEmpty();
    }

    @Test
    void listFieldsAreStoredSafely() {
        BlueprintInput input = new BlueprintInput();
        List<String> systemTypes = List.of("customer-crm", "support-management");
        List<String> relatedDomains = List.of("顧客管理", "問い合わせ管理");

        input.setSystemTypes(systemTypes);
        input.setPrimaryDomain("顧客管理");
        input.setRelatedDomains(relatedDomains);

        assertThat(input.getSystemTypes()).containsExactly("customer-crm", "support-management");
        assertThat(input.getPrimaryDomain()).isEqualTo("顧客管理");
        assertThat(input.getRelatedDomains()).containsExactly("顧客管理", "問い合わせ管理");
    }
}

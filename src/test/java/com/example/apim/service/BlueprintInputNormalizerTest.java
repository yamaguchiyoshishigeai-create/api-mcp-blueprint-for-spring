package com.example.apim.service;

import com.example.apim.model.BlueprintInput;
import com.example.apim.model.NormalizedBlueprintInput;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class BlueprintInputNormalizerTest {

    private final BlueprintInputNormalizer normalizer = new BlueprintInputNormalizer();

    @Test
    void normalizesLegacyTargetDomainOnlyInput() {
        BlueprintInput input = new BlueprintInput();
        input.setTargetDomain(" 注文管理 / 商品管理 / 在庫管理 / 商品管理 ");

        NormalizedBlueprintInput normalized = normalizer.normalize(input);

        assertThat(normalized.primaryDomain()).isEqualTo("注文管理");
        assertThat(normalized.relatedDomains()).containsExactly("商品管理", "在庫管理");
        assertThat(normalized.allDomains()).containsExactly("注文管理", "商品管理", "在庫管理");
        assertThat(normalized.targetDomainText()).isEqualTo("注文管理 / 商品管理 / 在庫管理");
    }

    @Test
    void prioritizesPrimaryAndRelatedDomainsWhenProvided() {
        BlueprintInput input = new BlueprintInput();
        input.setTargetDomain("顧客管理 / 問い合わせ管理");
        input.setPrimaryDomain(" 注文管理 ");
        input.setRelatedDomains(List.of(" 在庫管理 ", "商品管理", "注文管理", "在庫管理"));

        NormalizedBlueprintInput normalized = normalizer.normalize(input);

        assertThat(normalized.primaryDomain()).isEqualTo("注文管理");
        assertThat(normalized.relatedDomains()).containsExactly("在庫管理", "商品管理");
        assertThat(normalized.allDomains()).containsExactly("注文管理", "在庫管理", "商品管理");
        assertThat(normalized.targetDomainText()).isEqualTo("注文管理 / 在庫管理 / 商品管理");
    }

    @Test
    void trimsAndDeduplicatesSystemTypesSafely() {
        BlueprintInput input = new BlueprintInput();
        input.setPrimaryDomain("顧客管理");
        input.setSystemTypes(List.of(" crm ", "", "analytics", "crm", " "));

        NormalizedBlueprintInput normalized = normalizer.normalize(input);

        assertThat(normalized.systemTypes()).containsExactly("crm", "analytics");
    }

    @Test
    void handlesNullInputWithoutThrowingAndReturnsImmutableLists() {
        NormalizedBlueprintInput normalized = normalizer.normalize(null);

        assertThat(normalized.systemTypes()).isEmpty();
        assertThat(normalized.primaryDomain()).isEmpty();
        assertThat(normalized.relatedDomains()).isEmpty();
        assertThat(normalized.allDomains()).isEmpty();
        assertThat(normalized.targetDomainText()).isEmpty();
        assertThat(normalized.originalInput()).isNotNull();
        assertThat(normalized.systemTypes()).isUnmodifiable();
        assertThat(normalized.relatedDomains()).isUnmodifiable();
        assertThat(normalized.allDomains()).isUnmodifiable();
    }
}

package com.example.apim.support;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DomainNameNormalizerTest {

    private final DomainNameNormalizer normalizer = new DomainNameNormalizer();

    @Test
    void keepsExistingDomainMappings() {
        assertThat(normalizer.normalizeUrlSegment("顧客管理")).isEqualTo("customers");
        assertThat(normalizer.normalizeClassName("顧客管理")).isEqualTo("Customer");

        assertThat(normalizer.normalizeUrlSegment("ユーザー管理")).isEqualTo("users");
        assertThat(normalizer.normalizeClassName("ユーザー管理")).isEqualTo("User");
    }

    @Test
    void mapsPrimaryJapaneseDomainsToBusinessVocabulary() {
        assertThat(normalizer.normalizeUrlSegment("備品貸出管理")).isEqualTo("equipment");
        assertThat(normalizer.normalizeClassName("備品貸出管理")).isEqualTo("Equipment");

        assertThat(normalizer.normalizeUrlSegment("社内申請ワークフロー")).isEqualTo("applications");
        assertThat(normalizer.normalizeClassName("社内申請ワークフロー")).isEqualTo("Application");

        assertThat(normalizer.normalizeUrlSegment("ナレッジ検索・要約")).isEqualTo("knowledge-articles");
        assertThat(normalizer.normalizeClassName("ナレッジ検索・要約")).isEqualTo("KnowledgeArticle");
    }
}

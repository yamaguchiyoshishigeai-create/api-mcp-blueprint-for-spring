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

    @Test
    void mapsNewFirstVisitorSampleDomainsToBusinessVocabulary() {
        assertThat(normalizer.normalizeUrlSegment("問い合わせ管理")).isEqualTo("inquiries");
        assertThat(normalizer.normalizeClassName("問い合わせ管理")).isEqualTo("Inquiry");

        assertThat(normalizer.normalizeUrlSegment("契約管理")).isEqualTo("contracts");
        assertThat(normalizer.normalizeClassName("契約管理")).isEqualTo("Contract");

        assertThat(normalizer.normalizeUrlSegment("請求管理")).isEqualTo("invoices");
        assertThat(normalizer.normalizeClassName("請求管理")).isEqualTo("Invoice");

        assertThat(normalizer.normalizeUrlSegment("決済管理")).isEqualTo("payments");
        assertThat(normalizer.normalizeClassName("決済管理")).isEqualTo("Payment");
    }
}

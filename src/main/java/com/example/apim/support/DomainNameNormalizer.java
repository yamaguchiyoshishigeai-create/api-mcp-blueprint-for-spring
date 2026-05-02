package com.example.apim.support;

import org.springframework.stereotype.Component;

import java.util.Locale;
import java.util.List;
import java.util.regex.Pattern;

@Component
public class DomainNameNormalizer {

    private static final List<DomainVocabulary> VOCABULARIES = List.of(
            new DomainVocabulary("備品貸出管理", "equipment", "Equipment"),
            new DomainVocabulary("社内申請ワークフロー", "applications", "Application"),
            new DomainVocabulary("ナレッジ検索・要約", "knowledge-articles", "KnowledgeArticle"),
            new DomainVocabulary("修理依頼", "repair-requests", "RepairRequest"),
            new DomainVocabulary("経費精算", "expense-claims", "ExpenseClaim"),
            new DomainVocabulary("休暇申請", "leave-requests", "LeaveRequest"),
            new DomainVocabulary("稟議申請", "approval-requests", "ApprovalRequest"),
            new DomainVocabulary("問い合わせ回答", "support-answers", "SupportAnswer"),
            new DomainVocabulary("外部共有リンク", "external-share-links", "ExternalShareLink"),
            new DomainVocabulary("顧客管理", "customers", "Customer"),
            new DomainVocabulary("注文管理", "orders", "Order"),
            new DomainVocabulary("在庫管理", "inventory", "Inventory"),
            new DomainVocabulary("商品管理", "products", "Product"),
            new DomainVocabulary("ユーザー管理", "users", "User"),
            new DomainVocabulary("備品", "equipment", "Equipment"),
            new DomainVocabulary("貸出", "equipment-loans", "EquipmentLoan"),
            new DomainVocabulary("予約", "reservations", "Reservation"),
            new DomainVocabulary("申請", "applications", "Application"),
            new DomainVocabulary("承認", "approvals", "Approval"),
            new DomainVocabulary("ナレッジ", "knowledge-articles", "KnowledgeArticle"),
            new DomainVocabulary("FAQ", "faqs", "Faq")
    );

    private static final Pattern NON_ALNUM = Pattern.compile("[^a-z0-9]+");
    private static final Pattern MATCH_SEPARATORS = Pattern.compile("[\\s\\-_/・･]+");

    public String normalizeUrlSegment(String domain) {
        if (domain == null || domain.isBlank()) {
            return "domain-items";
        }
        for (DomainVocabulary vocabulary : VOCABULARIES) {
            if (containsTerm(domain, vocabulary.japaneseTerm())) {
                return vocabulary.urlSegment();
            }
        }
        String ascii = domain.toLowerCase(Locale.ROOT).replace("管理", "")
                .replace("ドメイン", "").trim();
        ascii = NON_ALNUM.matcher(ascii).replaceAll("-");
        ascii = ascii.replaceAll("-{2,}", "-").replaceAll("^-|-$", "");
        return ascii.isBlank() ? "domain-items" : ascii + "-items";
    }

    public String normalizeClassName(String domain) {
        if (domain == null || domain.isBlank()) {
            return "DomainItem";
        }
        for (DomainVocabulary vocabulary : VOCABULARIES) {
            if (containsTerm(domain, vocabulary.japaneseTerm())) {
                return vocabulary.className();
            }
        }
        String cleaned = domain.replace("管理", "")
                .replace("ドメイン", "")
                .replaceAll("[^A-Za-z0-9 ]", " ")
                .trim();
        if (cleaned.isBlank()) {
            return "DomainItem";
        }
        return new NamingSupport().toPascalCase(cleaned);
    }

    private boolean containsTerm(String rawDomain, String term) {
        if (rawDomain.contains(term)) {
            return true;
        }
        String normalizedDomain = MATCH_SEPARATORS.matcher(rawDomain).replaceAll("");
        String normalizedTerm = MATCH_SEPARATORS.matcher(term).replaceAll("");
        return !normalizedTerm.isBlank() && normalizedDomain.contains(normalizedTerm);
    }

    private record DomainVocabulary(String japaneseTerm, String urlSegment, String className) {
    }
}

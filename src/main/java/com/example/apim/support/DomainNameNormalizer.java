package com.example.apim.support;

import org.springframework.stereotype.Component;

import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

@Component
public class DomainNameNormalizer {

    private static final Map<String, String> URL_MAP = Map.of(
            "顧客管理", "customers",
            "注文管理", "orders",
            "在庫管理", "inventory",
            "商品管理", "products"
    );

    private static final Map<String, String> CLASS_MAP = Map.of(
            "顧客管理", "Customer",
            "注文管理", "Order",
            "在庫管理", "Inventory",
            "商品管理", "Product"
    );

    private static final Pattern NON_ALNUM = Pattern.compile("[^a-z0-9]+");

    public String normalizeUrlSegment(String domain) {
        if (domain == null || domain.isBlank()) {
            return "domain-items";
        }
        for (Map.Entry<String, String> e : URL_MAP.entrySet()) {
            if (domain.contains(e.getKey())) {
                return e.getValue();
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
        for (Map.Entry<String, String> e : CLASS_MAP.entrySet()) {
            if (domain.contains(e.getKey())) {
                return e.getValue();
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
}

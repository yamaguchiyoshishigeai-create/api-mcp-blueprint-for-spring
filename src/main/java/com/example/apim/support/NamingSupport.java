package com.example.apim.support;

import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Locale;
import java.util.stream.Collectors;

@Component
public class NamingSupport {

    public String toPascalCase(String raw) {
        if (raw == null || raw.isBlank()) {
            return "";
        }
        return Arrays.stream(raw.split("[\\s_\\-]+"))
                .filter(s -> !s.isBlank())
                .map(this::capitalizeAscii)
                .collect(Collectors.joining());
    }

    public String toCamelCase(String raw) {
        String pascal = toPascalCase(raw);
        if (pascal.isBlank()) {
            return "";
        }
        return Character.toLowerCase(pascal.charAt(0)) + pascal.substring(1);
    }

    private String capitalizeAscii(String token) {
        String lower = token.toLowerCase(Locale.ROOT);
        if (lower.length() == 1) {
            return lower.toUpperCase(Locale.ROOT);
        }
        return Character.toUpperCase(lower.charAt(0)) + lower.substring(1);
    }
}

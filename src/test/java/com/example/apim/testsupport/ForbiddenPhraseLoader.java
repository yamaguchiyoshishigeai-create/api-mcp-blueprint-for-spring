package com.example.apim.testsupport;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

public final class ForbiddenPhraseLoader {

    private static final String RESOURCE_PATH = "regression/forbidden-output-phrases.txt";

    private ForbiddenPhraseLoader() {
    }

    public static List<String> loadForbiddenOutputPhrases() {
        InputStream resourceStream = ForbiddenPhraseLoader.class.getClassLoader().getResourceAsStream(RESOURCE_PATH);
        if (resourceStream == null) {
            throw new IllegalStateException("Forbidden phrase resource not found: " + RESOURCE_PATH);
        }

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(resourceStream, StandardCharsets.UTF_8))) {
            return reader.lines()
                    .map(String::strip)
                    .filter(line -> !line.isBlank())
                    .toList();
        } catch (IOException ex) {
            throw new UncheckedIOException("Failed to load forbidden phrases: " + RESOURCE_PATH, ex);
        }
    }
}

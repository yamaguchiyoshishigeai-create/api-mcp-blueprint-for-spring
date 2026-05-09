package com.example.apim.service;

import com.example.apim.testsupport.ForbiddenPhraseLoader;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

class RegressionGuardMetaTest {

    private static final String IMPORTANT_FORBIDDEN_PHRASE = "APIM for Spring本体の改修指示ではない";
    private static final Path REGRESSION_EVIDENCE_MATRIX_PATH =
            Path.of("docs/00_プロジェクト管理/06_品質管理/RegressionEvidenceMatrix.md");

    @Test
    void forbiddenPhraseListIsNotEmpty() {
        List<String> forbiddenPhrases = ForbiddenPhraseLoader.loadForbiddenOutputPhrases();
        assertThat(forbiddenPhrases).isNotEmpty();
    }

    @Test
    void forbiddenPhraseListStillContainsImportantPhrase() {
        List<String> forbiddenPhrases = ForbiddenPhraseLoader.loadForbiddenOutputPhrases();
        assertThat(forbiddenPhrases).contains(IMPORTANT_FORBIDDEN_PHRASE);
    }

    @Test
    void importantGuardTestClassExists() {
        assertThatCode(() -> Class.forName("com.example.apim.service.GeneratedOutputForbiddenPhraseGuardTest"))
                .doesNotThrowAnyException();
    }

    @Test
    void regressionEvidenceMatrixContainsTsk044Evidence() throws IOException {
        assertThat(Files.exists(REGRESSION_EVIDENCE_MATRIX_PATH))
                .as("RegressionEvidenceMatrix.md must exist")
                .isTrue();

        String matrix = Files.readString(REGRESSION_EVIDENCE_MATRIX_PATH, StandardCharsets.UTF_8);
        assertThat(matrix).contains("TSK-044");
        assertThat(matrix).contains("ImplementationInstructionGeneratorTest");
        assertThat(matrix).contains("BlueprintGenerationRegressionTest");
        assertThat(matrix).contains(IMPORTANT_FORBIDDEN_PHRASE);
    }
}

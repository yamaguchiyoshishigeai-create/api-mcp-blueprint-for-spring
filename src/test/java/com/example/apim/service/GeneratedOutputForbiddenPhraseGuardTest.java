package com.example.apim.service;

import com.example.apim.model.BlueprintInput;
import com.example.apim.model.BlueprintResult;
import com.example.apim.support.DomainNameNormalizer;
import com.example.apim.support.NamingSupport;
import com.example.apim.support.OperationClassifier;
import com.example.apim.testsupport.BlueprintInputFixtures;
import com.example.apim.testsupport.ForbiddenPhraseLoader;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class GeneratedOutputForbiddenPhraseGuardTest {

    private final BlueprintGenerationService service = newService();

    @Test
    void generatedBlueprintAndImplementationInstructionsDoNotContainAnyForbiddenPhrase() {
        List<String> forbiddenPhrases = ForbiddenPhraseLoader.loadForbiddenOutputPhrases();
        assertThat(forbiddenPhrases).isNotEmpty();

        List<BlueprintInput> fixtures = List.of(
                BlueprintInputFixtures.equipmentLoanManagement(),
                BlueprintInputFixtures.internalApplicationWorkflow(),
                BlueprintInputFixtures.knowledgeSearchAndSummary()
        );

        for (BlueprintInput fixture : fixtures) {
            BlueprintResult result = service.generate(fixture);
            assertNoForbiddenPhrase(result.getBlueprintMarkdown(), forbiddenPhrases, "blueprint markdown");
            assertNoForbiddenPhrase(result.getImplementationInstructions(), forbiddenPhrases, "implementation instructions");
        }
    }

    private void assertNoForbiddenPhrase(String output, List<String> forbiddenPhrases, String artifactName) {
        assertThat(output).isNotBlank();
        for (String phrase : forbiddenPhrases) {
            assertThat(output)
                    .as("%s must not contain forbidden phrase: %s", artifactName, phrase)
                    .doesNotContain(phrase);
        }
    }

    private BlueprintGenerationService newService() {
        return new BlueprintGenerationService(
                new OperationClassifier(),
                new BlueprintInputNormalizer(),
                new DomainNameNormalizer(),
                new ApiDesignGenerator(),
                new DtoCandidateGenerator(),
                new ControllerSkeletonGenerator(new NamingSupport()),
                new McpDesignGenerator(),
                new SecurityNotesGenerator(),
                new MarkdownDocumentGenerator(),
                new ImplementationInstructionGenerator()
        );
    }
}

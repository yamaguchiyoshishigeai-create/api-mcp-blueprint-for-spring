package com.example.apim.service;

import com.example.apim.model.BlueprintInput;
import com.example.apim.support.DomainNameNormalizer;
import com.example.apim.support.NamingSupport;
import com.example.apim.support.OperationClassifier;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class BlueprintGenerationServiceTest {

    @Test
    void generatesNonEmptyBlueprintResult() {
        BlueprintGenerationService service = new BlueprintGenerationService(
                new OperationClassifier(),
                new DomainNameNormalizer(),
                new ApiDesignGenerator(),
                new DtoCandidateGenerator(),
                new ControllerSkeletonGenerator(new NamingSupport()),
                new McpDesignGenerator(),
                new SecurityNotesGenerator(),
                new MarkdownDocumentGenerator(),
                new ImplementationInstructionGenerator()
        );

        BlueprintInput input = new BlueprintInput();
        input.setBusinessRequirements("顧客情報を検索して更新する。");
        input.setTargetDomain("顧客管理");
        input.setUserTypes("営業担当\n管理者");
        input.setRequiredOperations("顧客検索\n顧客更新");
        input.setAllowedAiOperations("顧客検索\n顧客更新案の作成");

        var result = service.generate(input);

        assertThat(result.getApiEndpoints()).isNotEmpty();
        assertThat(result.getMcpTools()).isNotEmpty();
        assertThat(result.getSecurityNotes()).isNotEmpty();
        assertThat(result.getBlueprintMarkdown()).isNotBlank();
        assertThat(result.getImplementationInstructions()).isNotBlank();
    }
}

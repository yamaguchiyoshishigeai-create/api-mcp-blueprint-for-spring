package com.example.apim.service;

import com.example.apim.model.BlueprintInput;
import com.example.apim.model.NormalizedBlueprintInput;
import com.example.apim.model.BlueprintResult;
import com.example.apim.support.DomainNameNormalizer;
import com.example.apim.support.OperationClassifier;
import com.example.apim.support.OperationType;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class BlueprintGenerationService {

    private final OperationClassifier operationClassifier;
    private final BlueprintInputNormalizer blueprintInputNormalizer;
    private final DomainNameNormalizer domainNameNormalizer;
    private final ApiDesignGenerator apiDesignGenerator;
    private final DtoCandidateGenerator dtoCandidateGenerator;
    private final ControllerSkeletonGenerator controllerSkeletonGenerator;
    private final McpDesignGenerator mcpDesignGenerator;
    private final SecurityNotesGenerator securityNotesGenerator;
    private final MarkdownDocumentGenerator markdownDocumentGenerator;
    private final ImplementationInstructionGenerator implementationInstructionGenerator;

    public BlueprintGenerationService(
            OperationClassifier operationClassifier,
            BlueprintInputNormalizer blueprintInputNormalizer,
            DomainNameNormalizer domainNameNormalizer,
            ApiDesignGenerator apiDesignGenerator,
            DtoCandidateGenerator dtoCandidateGenerator,
            ControllerSkeletonGenerator controllerSkeletonGenerator,
            McpDesignGenerator mcpDesignGenerator,
            SecurityNotesGenerator securityNotesGenerator,
            MarkdownDocumentGenerator markdownDocumentGenerator,
            ImplementationInstructionGenerator implementationInstructionGenerator
    ) {
        this.operationClassifier = operationClassifier;
        this.blueprintInputNormalizer = blueprintInputNormalizer;
        this.domainNameNormalizer = domainNameNormalizer;
        this.apiDesignGenerator = apiDesignGenerator;
        this.dtoCandidateGenerator = dtoCandidateGenerator;
        this.controllerSkeletonGenerator = controllerSkeletonGenerator;
        this.mcpDesignGenerator = mcpDesignGenerator;
        this.securityNotesGenerator = securityNotesGenerator;
        this.markdownDocumentGenerator = markdownDocumentGenerator;
        this.implementationInstructionGenerator = implementationInstructionGenerator;
    }

    public BlueprintResult generate(BlueprintInput input) {
        NormalizedBlueprintInput normalizedInput = blueprintInputNormalizer.normalize(input);
        String primaryDomainText = normalizedInput.primaryDomain().isBlank()
                ? normalizedInput.targetDomainText()
                : normalizedInput.primaryDomain();
        String domainPath = domainNameNormalizer.normalizeUrlSegment(primaryDomainText);
        String domainClass = domainNameNormalizer.normalizeClassName(primaryDomainText);
        Set<OperationType> operations = operationClassifier.classify(input);
        String actors = input.getUserTypes().replace("\n", " / ");

        BlueprintResult result = new BlueprintResult();
        result.setInputSummary(buildInputSummary(input, normalizedInput));
        result.setApiDesignSummary("業務要件からREST API候補とMCP設計候補を生成した。");
        if (normalizedInput.relatedDomains().isEmpty()) {
            result.setApiEndpoints(apiDesignGenerator.generate(domainPath, domainClass, operations, actors));
        } else {
            result.setApiEndpoints(apiDesignGenerator.generate(normalizedInput, domainNameNormalizer, operations, actors));
        }
        result.setDtoCandidates(dtoCandidateGenerator.generate(domainClass, result.getApiEndpoints()));
        if (normalizedInput.relatedDomains().isEmpty()) {
            result.setControllerSkeleton(controllerSkeletonGenerator.generate(domainClass, domainPath, result.getApiEndpoints()));
        } else {
            result.setControllerSkeleton(controllerSkeletonGenerator.generate(normalizedInput, domainNameNormalizer, result.getApiEndpoints()));
        }

        McpDesignGenerator.McpDesignResult mcp = normalizedInput.relatedDomains().isEmpty()
                ? mcpDesignGenerator.generate(domainClass, domainPath, operations, result.getApiEndpoints())
                : mcpDesignGenerator.generate(normalizedInput, domainNameNormalizer, operations, result.getApiEndpoints());
        result.setMcpTools(mcp.tools());
        result.setMcpResources(mcp.resources());
        result.setMcpPrompts(mcp.prompts());
        result.setApiMcpMappings(mcp.mappings());
        result.setSecurityNotes(securityNotesGenerator.generate(input, operations));
        result.setBlueprintMarkdown(markdownDocumentGenerator.generate(normalizedInput, result));
        result.setImplementationInstructions(implementationInstructionGenerator.generate(normalizedInput, result));
        return result;
    }

    private String buildInputSummary(BlueprintInput input, NormalizedBlueprintInput normalizedInput) {
        String targetDomainText = normalizedInput.targetDomainText().isBlank()
                ? input.getTargetDomain()
                : normalizedInput.targetDomainText();
        return "対象ドメイン: " + targetDomainText
                + " / 必要操作: " + input.getRequiredOperations().replace("\n", " / ")
                + " / AI許可操作: " + input.getAllowedAiOperations().replace("\n", " / ");
    }
}

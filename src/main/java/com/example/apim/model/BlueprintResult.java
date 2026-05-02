package com.example.apim.model;

import java.util.ArrayList;
import java.util.List;

public class BlueprintResult {
    private String inputSummary = "";
    private String apiDesignSummary = "";
    private List<ApiEndpointCandidate> apiEndpoints = new ArrayList<>();
    private List<DtoCandidate> dtoCandidates = new ArrayList<>();
    private ControllerSkeleton controllerSkeleton = new ControllerSkeleton("", "");
    private List<McpToolCandidate> mcpTools = new ArrayList<>();
    private List<McpResourceCandidate> mcpResources = new ArrayList<>();
    private List<McpPromptCandidate> mcpPrompts = new ArrayList<>();
    private List<ApiMcpMapping> apiMcpMappings = new ArrayList<>();
    private List<SecurityNote> securityNotes = new ArrayList<>();
    private String blueprintMarkdown = "";
    private String implementationInstructions = "";

    public String getInputSummary() {
        return inputSummary;
    }

    public void setInputSummary(String inputSummary) {
        this.inputSummary = inputSummary;
    }

    public String getApiDesignSummary() {
        return apiDesignSummary;
    }

    public void setApiDesignSummary(String apiDesignSummary) {
        this.apiDesignSummary = apiDesignSummary;
    }

    public List<ApiEndpointCandidate> getApiEndpoints() {
        return apiEndpoints;
    }

    public void setApiEndpoints(List<ApiEndpointCandidate> apiEndpoints) {
        this.apiEndpoints = apiEndpoints;
    }

    public List<DtoCandidate> getDtoCandidates() {
        return dtoCandidates;
    }

    public void setDtoCandidates(List<DtoCandidate> dtoCandidates) {
        this.dtoCandidates = dtoCandidates;
    }

    public ControllerSkeleton getControllerSkeleton() {
        return controllerSkeleton;
    }

    public void setControllerSkeleton(ControllerSkeleton controllerSkeleton) {
        this.controllerSkeleton = controllerSkeleton;
    }

    public List<McpToolCandidate> getMcpTools() {
        return mcpTools;
    }

    public void setMcpTools(List<McpToolCandidate> mcpTools) {
        this.mcpTools = mcpTools;
    }

    public List<McpResourceCandidate> getMcpResources() {
        return mcpResources;
    }

    public void setMcpResources(List<McpResourceCandidate> mcpResources) {
        this.mcpResources = mcpResources;
    }

    public List<McpPromptCandidate> getMcpPrompts() {
        return mcpPrompts;
    }

    public void setMcpPrompts(List<McpPromptCandidate> mcpPrompts) {
        this.mcpPrompts = mcpPrompts;
    }

    public List<ApiMcpMapping> getApiMcpMappings() {
        return apiMcpMappings;
    }

    public void setApiMcpMappings(List<ApiMcpMapping> apiMcpMappings) {
        this.apiMcpMappings = apiMcpMappings;
    }

    public List<SecurityNote> getSecurityNotes() {
        return securityNotes;
    }

    public void setSecurityNotes(List<SecurityNote> securityNotes) {
        this.securityNotes = securityNotes;
    }

    public String getBlueprintMarkdown() {
        return blueprintMarkdown;
    }

    public void setBlueprintMarkdown(String blueprintMarkdown) {
        this.blueprintMarkdown = blueprintMarkdown;
    }

    public String getImplementationInstructions() {
        return implementationInstructions;
    }

    public void setImplementationInstructions(String implementationInstructions) {
        this.implementationInstructions = implementationInstructions;
    }
}

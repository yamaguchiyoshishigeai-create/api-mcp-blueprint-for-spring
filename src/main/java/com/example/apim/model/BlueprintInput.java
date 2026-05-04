package com.example.apim.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.ArrayList;
import java.util.List;

public class BlueprintInput {

    @NotBlank
    @Size(max = 10000)
    private String businessRequirements = "";

    @NotBlank
    @Size(max = 100)
    private String targetDomain = "";

    @Size(max = 20)
    private List<@Size(max = 100) String> systemTypes = new ArrayList<>();

    @Size(max = 100)
    private String primaryDomain = "";

    @Size(max = 20)
    private List<@Size(max = 100) String> relatedDomains = new ArrayList<>();

    @NotBlank
    @Size(max = 2000)
    private String userTypes = "";

    @NotBlank
    @Size(max = 3000)
    private String requiredOperations = "";

    @NotBlank
    @Size(max = 3000)
    private String allowedAiOperations = "";

    @Size(max = 2000)
    private String readOnlyOperations = "";

    @Size(max = 2000)
    private String writeOperations = "";

    @Size(max = 2000)
    private String approvalRequiredOperations = "";

    @Size(max = 2000)
    private String auditLogRequiredOperations = "";

    @Size(max = 200)
    private String authenticationMethod = "";

    @Size(max = 500)
    private String targetUsers = "";

    private String outputLanguage = "日本語";

    public String getBusinessRequirements() {
        return businessRequirements;
    }

    public void setBusinessRequirements(String businessRequirements) {
        this.businessRequirements = valueOrEmpty(businessRequirements);
    }

    public String getTargetDomain() {
        return targetDomain;
    }

    public void setTargetDomain(String targetDomain) {
        this.targetDomain = valueOrEmpty(targetDomain);
    }

    public List<String> getSystemTypes() {
        return systemTypes;
    }

    public void setSystemTypes(List<String> systemTypes) {
        this.systemTypes = valuesOrEmpty(systemTypes);
    }

    public String getPrimaryDomain() {
        return primaryDomain;
    }

    public void setPrimaryDomain(String primaryDomain) {
        this.primaryDomain = valueOrEmpty(primaryDomain);
    }

    public List<String> getRelatedDomains() {
        return relatedDomains;
    }

    public void setRelatedDomains(List<String> relatedDomains) {
        this.relatedDomains = valuesOrEmpty(relatedDomains);
    }

    public String getUserTypes() {
        return userTypes;
    }

    public void setUserTypes(String userTypes) {
        this.userTypes = valueOrEmpty(userTypes);
    }

    public String getRequiredOperations() {
        return requiredOperations;
    }

    public void setRequiredOperations(String requiredOperations) {
        this.requiredOperations = valueOrEmpty(requiredOperations);
    }

    public String getAllowedAiOperations() {
        return allowedAiOperations;
    }

    public void setAllowedAiOperations(String allowedAiOperations) {
        this.allowedAiOperations = valueOrEmpty(allowedAiOperations);
    }

    public String getReadOnlyOperations() {
        return readOnlyOperations;
    }

    public void setReadOnlyOperations(String readOnlyOperations) {
        this.readOnlyOperations = valueOrEmpty(readOnlyOperations);
    }

    public String getWriteOperations() {
        return writeOperations;
    }

    public void setWriteOperations(String writeOperations) {
        this.writeOperations = valueOrEmpty(writeOperations);
    }

    public String getApprovalRequiredOperations() {
        return approvalRequiredOperations;
    }

    public void setApprovalRequiredOperations(String approvalRequiredOperations) {
        this.approvalRequiredOperations = valueOrEmpty(approvalRequiredOperations);
    }

    public String getAuditLogRequiredOperations() {
        return auditLogRequiredOperations;
    }

    public void setAuditLogRequiredOperations(String auditLogRequiredOperations) {
        this.auditLogRequiredOperations = valueOrEmpty(auditLogRequiredOperations);
    }

    public String getAuthenticationMethod() {
        return authenticationMethod;
    }

    public void setAuthenticationMethod(String authenticationMethod) {
        this.authenticationMethod = valueOrEmpty(authenticationMethod);
    }

    public String getTargetUsers() {
        return targetUsers;
    }

    public void setTargetUsers(String targetUsers) {
        this.targetUsers = valueOrEmpty(targetUsers);
    }

    public String getOutputLanguage() {
        return outputLanguage;
    }

    public void setOutputLanguage(String outputLanguage) {
        this.outputLanguage = valueOrEmpty(outputLanguage);
    }

    private String valueOrEmpty(String value) {
        return value == null ? "" : value;
    }

    private List<String> valuesOrEmpty(List<String> values) {
        if (values == null) {
            return new ArrayList<>();
        }
        List<String> safeValues = new ArrayList<>(values.size());
        for (String value : values) {
            safeValues.add(valueOrEmpty(value));
        }
        return safeValues;
    }
}

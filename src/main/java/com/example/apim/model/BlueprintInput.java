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

    private String outputLanguage = "\u65e5\u672c\u8a9e";

    private List<V2BusinessObject> v2BusinessObjects = new ArrayList<>();

    private List<V2Actor> v2Actors = new ArrayList<>();

    private List<V2Operation> v2Operations = new ArrayList<>();

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

    public List<V2BusinessObject> getV2BusinessObjects() {
        return v2BusinessObjects;
    }

    public void setV2BusinessObjects(List<V2BusinessObject> v2BusinessObjects) {
        this.v2BusinessObjects = v2BusinessObjects == null ? new ArrayList<>() : new ArrayList<>(v2BusinessObjects);
    }

    public List<V2Actor> getV2Actors() {
        return v2Actors;
    }

    public void setV2Actors(List<V2Actor> v2Actors) {
        this.v2Actors = v2Actors == null ? new ArrayList<>() : new ArrayList<>(v2Actors);
    }

    public List<V2Operation> getV2Operations() {
        return v2Operations;
    }

    public void setV2Operations(List<V2Operation> v2Operations) {
        this.v2Operations = v2Operations == null ? new ArrayList<>() : new ArrayList<>(v2Operations);
    }

    public boolean hasV2BusinessOperationModel() {
        return !v2BusinessObjects.isEmpty() && !v2Operations.isEmpty();
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

    public record V2BusinessObject(
            String id,
            String name,
            String domainId,
            String sensitivity,
            List<String> dataCategories
    ) {
        public V2BusinessObject {
            id = valueOrEmptyStatic(id);
            name = valueOrEmptyStatic(name);
            domainId = valueOrEmptyStatic(domainId);
            sensitivity = valueOrEmptyStatic(sensitivity);
            dataCategories = dataCategories == null ? List.of() : List.copyOf(dataCategories);
        }
    }

    public record V2Actor(
            String id,
            String name,
            String actorType
    ) {
        public V2Actor {
            id = valueOrEmptyStatic(id);
            name = valueOrEmptyStatic(name);
            actorType = valueOrEmptyStatic(actorType);
        }
    }

    public record V2Operation(
            String id,
            String label,
            String description,
            List<String> actorIds,
            List<String> objectIds,
            String intent,
            String executionMode,
            String aiPermission,
            boolean approvalRequired,
            String auditLogRequired,
            String riskLevel,
            boolean externalAction,
            boolean stateChanging,
            String outputType
    ) {
        public V2Operation {
            id = valueOrEmptyStatic(id);
            label = valueOrEmptyStatic(label);
            description = valueOrEmptyStatic(description);
            actorIds = actorIds == null ? List.of() : List.copyOf(actorIds);
            objectIds = objectIds == null ? List.of() : List.copyOf(objectIds);
            intent = valueOrEmptyStatic(intent);
            executionMode = valueOrEmptyStatic(executionMode);
            aiPermission = valueOrEmptyStatic(aiPermission);
            auditLogRequired = valueOrEmptyStatic(auditLogRequired);
            riskLevel = valueOrEmptyStatic(riskLevel);
            outputType = valueOrEmptyStatic(outputType);
        }
    }

    private static String valueOrEmptyStatic(String value) {
        return value == null ? "" : value;
    }
}

package com.example.apim.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class BlueprintInput {

    private static final Set<String> GENERIC_OPERATION_VALUES =
            Collections.unmodifiableSet(new LinkedHashSet<>(Arrays.asList(
                    "\u691c\u7d22",
                    "\u4e00\u89a7",
                    "\u8a73\u7d30\u53d6\u5f97",
                    "\u8a73\u7d30\u53c2\u7167",
                    "\u767b\u9332",
                    "\u66f4\u65b0",
                    "\u524a\u9664",
                    "\u627f\u8a8d",
                    "\u5374\u4e0b",
                    "\u8981\u7d04",
                    "\u901a\u77e5",
                    "\u4e0b\u66f8\u304d\u4f5c\u6210",
                    "\u66f4\u65b0\u6848\u306e\u4f5c\u6210",
                    "\u6a29\u9650\u5909\u66f4",
                    "\u5916\u90e8\u9001\u4fe1"
            )));

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
        this.requiredOperations = normalizeOperationText(requiredOperations);
    }

    public String getAllowedAiOperations() {
        return allowedAiOperations;
    }

    public void setAllowedAiOperations(String allowedAiOperations) {
        this.allowedAiOperations = normalizeOperationText(allowedAiOperations);
    }

    public String getReadOnlyOperations() {
        return readOnlyOperations;
    }

    public void setReadOnlyOperations(String readOnlyOperations) {
        this.readOnlyOperations = normalizeOperationText(readOnlyOperations);
    }

    public String getWriteOperations() {
        return writeOperations;
    }

    public void setWriteOperations(String writeOperations) {
        this.writeOperations = normalizeOperationText(writeOperations);
    }

    public String getApprovalRequiredOperations() {
        return approvalRequiredOperations;
    }

    public void setApprovalRequiredOperations(String approvalRequiredOperations) {
        this.approvalRequiredOperations = normalizeOperationText(approvalRequiredOperations);
    }

    public String getAuditLogRequiredOperations() {
        return auditLogRequiredOperations;
    }

    public void setAuditLogRequiredOperations(String auditLogRequiredOperations) {
        this.auditLogRequiredOperations = normalizeOperationText(auditLogRequiredOperations);
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

    private String normalizeOperationText(String value) {
        String safeValue = valueOrEmpty(value);
        List<String> operations = parseOperationLines(safeValue);
        boolean hasSpecificOperation = false;

        for (String operation : operations) {
            if (!GENERIC_OPERATION_VALUES.contains(operation)) {
                hasSpecificOperation = true;
                break;
            }
        }

        if (!hasSpecificOperation) {
            return safeValue;
        }

        List<String> normalizedOperations = new ArrayList<>();
        for (String operation : operations) {
            if (!GENERIC_OPERATION_VALUES.contains(operation)) {
                normalizedOperations.add(operation);
            }
        }

        return bulletList(normalizedOperations);
    }

    private List<String> parseOperationLines(String value) {
        List<String> operations = new ArrayList<>();

        for (String line : value.split("\\R")) {
            String operation = line.trim().replaceFirst("^-+\\s*", "").trim();
            if (!operation.isEmpty()) {
                operations.add(operation);
            }
        }

        return new ArrayList<>(new LinkedHashSet<>(operations));
    }

    private String bulletList(List<String> values) {
        StringBuilder builder = new StringBuilder();

        for (String value : values) {
            if (builder.length() > 0) {
                builder.append('\n');
            }
            builder.append("- ").append(value);
        }

        return builder.toString();
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

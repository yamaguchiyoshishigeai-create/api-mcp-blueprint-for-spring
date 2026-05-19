package com.example.apim.service;

import com.example.apim.model.BlueprintInput;
import com.example.apim.model.BlueprintResult;
import com.example.apim.model.ExternalAiImportResult;
import com.example.apim.support.DomainNameNormalizer;
import com.example.apim.support.NamingSupport;
import com.example.apim.support.OperationClassifier;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

class ComplexFreeTextRegressionTest {

    private static final List<String> COMPLEX_V2_SAMPLES = List.of(
            "order-inventory-delivery-billing-payment.v2.json",
            "internal-application-approval-notification-audit.v2.json",
            "inquiry-knowledge-customer-notification.v2.json",
            "draft-and-send-boundary.v2.json",
            "proposal-and-actual-update-boundary.v2.json",
            "ambiguous-and-insufficient-input.v2.json"
    );

    private final ExternalAiPromptBridgeService bridgeService =
            new ExternalAiPromptBridgeService(new ObjectMapper());
    private final BlueprintGenerationService generationService = newService();

    @Test
    void additionalV2SamplesImportAndPreserveCrossDomainStructure() throws Exception {
        for (String sample : COMPLEX_V2_SAMPLES) {
            ExternalAiImportResult importResult = importSample(sample);

            assertThat(importResult.valid()).as(sample).isTrue();
            assertThat(importResult.canGenerate()).as(sample).isTrue();
            BlueprintInput input = importResult.blueprintInput();
            assertThat(input.getV2Domains()).as(sample).hasSizeGreaterThan(1);
            assertThat(input.getV2BusinessObjects()).as(sample).hasSizeGreaterThan(1);
            assertThat(input.getV2Operations()).as(sample).isNotEmpty();
            assertThat(input.getV2Operations()).as(sample).allMatch(operation -> !operation.objectIds().isEmpty());
            assertOperationReferencesKnownObjects(input, sample);
            assertDangerousOperationsAreSafeSideClassified(input, sample);
        }

        BlueprintInput draftAndSend = importSample("draft-and-send-boundary.v2.json").blueprintInput();
        assertThat(draftAndSend.getV2Operations()).extracting(BlueprintInput.V2Operation::label)
                .contains("顧客通知文案作成", "顧客通知送信依頼");
        assertThat(operationByLabel(draftAndSend, "顧客通知文案作成").executionMode()).isEqualTo("draft_only");
        assertThat(operationByLabel(draftAndSend, "顧客通知送信依頼").aiPermission())
                .isEqualTo("not_allowed_directly");

        BlueprintInput proposalAndUpdate = importSample("proposal-and-actual-update-boundary.v2.json").blueprintInput();
        assertThat(proposalAndUpdate.getV2Operations()).extracting(BlueprintInput.V2Operation::label)
                .contains("顧客情報変更提案", "顧客情報実更新依頼", "契約条件変更提案", "契約条件実更新依頼");
        assertThat(operationByLabel(proposalAndUpdate, "顧客情報変更提案").executionMode())
                .isEqualTo("proposal_only");
        assertThat(operationByLabel(proposalAndUpdate, "契約条件実更新依頼").approvalRequired()).isTrue();
    }

    @Test
    void orderInventoryDeliveryBillingPaymentSampleKeepsDistributedApiCandidatesAndSafetyControls()
            throws Exception {
        BlueprintResult result = generateSample("order-inventory-delivery-billing-payment.v2.json");
        List<String> paths = endpointPaths(result);

        assertThat(paths).contains(
                "GET /api/orders",
                "GET /api/inventory",
                "GET /api/deliveries",
                "GET /api/invoices",
                "GET /api/payments"
        );
        assertThat(paths).anyMatch(path -> path.startsWith("POST /api/inventory/")
                && path.endsWith("/change-requests"));
        assertThat(paths).anyMatch(path -> path.startsWith("POST /api/payments/")
                && path.endsWith("/change-requests"));
        assertThat(paths.stream().filter(path -> path.contains("/api/invoices")).count())
                .isLessThan(paths.size() / 2);
        assertThat(result.getApiEndpoints()).noneMatch(endpoint -> endpoint.httpMethod().equals("PUT"));
        assertThat(result.getApiEndpoints()).anyMatch(endpoint -> endpoint.path().contains("/api/payments/")
                && endpoint.approvalRequired().equals("必須")
                && endpoint.auditLogRequired().equals("必須"));
        assertThat(result.getMcpTools()).anyMatch(tool -> tool.operationType().equals("approval-request")
                && tool.aiExecutionPolicy().contains("AI直接実行不可")
                && tool.approvalRequired().equals("必須")
                && tool.auditLogRequired().equals("必須"));
        assertNoDomainItemFallback(result);
    }

    @Test
    void internalApplicationApprovalNotificationAuditSampleKeepsApplicationApprovalNotificationAndAuditBoundaries()
            throws Exception {
        BlueprintResult result = generateSample("internal-application-approval-notification-audit.v2.json");
        List<String> paths = endpointPaths(result);

        assertThat(paths).contains(
                "GET /api/applications",
                "GET /api/approval-requests",
                "GET /api/notifications",
                "GET /api/audit-logs"
        );
        assertThat(result.getApiEndpoints()).anyMatch(endpoint -> endpoint.path().contains("/api/approval-requests/")
                && endpoint.approvalRequired().equals("必須"));
        assertThat(result.getApiEndpoints()).anyMatch(endpoint -> endpoint.path().contains("/api/notifications/")
                && endpoint.approvalRequired().equals("必須")
                && endpoint.auditLogRequired().equals("必須"));
        assertThat(result.getMcpTools()).anyMatch(tool -> tool.operationType().equals("draft")
                && tool.aiExecutionPolicy().contains("文案"));
        assertThat(result.getMcpTools()).anyMatch(tool -> tool.operationType().equals("approval-request")
                && tool.aiExecutionPolicy().contains("AI直接実行不可"));
        assertNoDomainItemFallback(result);
    }

    @Test
    void supportKnowledgeCustomerNotificationSampleKeepsAllMajorBusinessObjects() throws Exception {
        BlueprintResult result = generateSample("inquiry-knowledge-customer-notification.v2.json");
        List<String> paths = endpointPaths(result);

        assertThat(paths).contains(
                "GET /api/inquiries",
                "GET /api/knowledge-articles",
                "GET /api/customers",
                "GET /api/notifications"
        );
        assertThat(result.getApiEndpoints()).anyMatch(endpoint -> endpoint.path().contains("/api/notifications/")
                && endpoint.approvalRequired().equals("必須"));
        assertThat(result.getMcpTools()).anyMatch(tool -> tool.operationType().equals("draft"));
        assertThat(result.getMcpTools()).anyMatch(tool -> tool.operationType().equals("approval-request"));
        assertNoDomainItemFallback(result);
    }

    @Test
    void draftCreationAndSendingAreNotMixedInApiOrMcpCandidates() throws Exception {
        BlueprintResult result = generateSample("draft-and-send-boundary.v2.json");

        assertThat(result.getApiEndpoints()).anyMatch(endpoint -> endpoint.path().equals(
                "/api/notifications/{id}/notification-drafts")
                && endpoint.httpMethod().equals("POST")
                && endpoint.approvalRequired().equals("不要"));
        assertThat(result.getApiEndpoints()).anyMatch(endpoint -> endpoint.path().contains("/api/notifications/")
                && endpoint.path().endsWith("/change-requests")
                && endpoint.approvalRequired().equals("必須")
                && endpoint.auditLogRequired().equals("必須"));
        assertThat(result.getMcpTools()).anyMatch(tool -> tool.operationType().equals("draft")
                && tool.aiExecutionPolicy().contains("文案"));
        assertThat(result.getMcpTools()).anyMatch(tool -> tool.operationType().equals("approval-request")
                && tool.aiExecutionPolicy().contains("AI直接実行不可")
                && tool.approvalRequired().equals("必須"));
    }

    @Test
    void proposalCreationAndActualUpdateAreNotMixedInApiOrMcpCandidates() throws Exception {
        BlueprintResult result = generateSample("proposal-and-actual-update-boundary.v2.json");

        assertThat(endpointPaths(result)).contains(
                "POST /api/customers/{id}/change-proposals",
                "POST /api/customers/{id}/change-requests",
                "POST /api/contracts/{id}/change-proposals",
                "POST /api/contracts/{id}/condition-change-requests"
        );
        assertThat(result.getApiEndpoints()).noneMatch(endpoint -> endpoint.httpMethod().equals("PUT"));
        assertThat(result.getApiEndpoints()).anyMatch(endpoint -> endpoint.path().endsWith("/change-requests")
                && endpoint.approvalRequired().equals("必須"));
        assertThat(result.getApiEndpoints()).anyMatch(endpoint -> endpoint.path().endsWith("/condition-change-requests")
                && endpoint.approvalRequired().equals("必須"));
        assertThat(result.getMcpTools()).anyMatch(tool -> tool.operationType().equals("proposal")
                && tool.aiExecutionPolicy().contains("提案"));
        assertThat(result.getMcpTools()).anyMatch(tool -> tool.operationType().equals("approval-request")
                && tool.aiExecutionPolicy().contains("AI直接実行不可"));
    }

    @Test
    void markdownKeepsBusinessStructureAndSafetyBeforeApiCandidatesAcrossComplexSamples() throws Exception {
        for (String sample : List.of(
                "order-inventory-delivery-billing-payment.v2.json",
                "internal-application-approval-notification-audit.v2.json",
                "ambiguous-and-insufficient-input.v2.json")) {
            BlueprintResult result = generateSample(sample);
            String markdown = result.getBlueprintMarkdown();

            assertThat(markdown.indexOf("## 3. 抽出された業務構造"))
                    .as(sample).isLessThan(markdown.indexOf("## 6. REST API候補"));
            assertThat(markdown.indexOf("## 4. AI支援・承認・監査の分類"))
                    .as(sample).isLessThan(markdown.indexOf("## 6. REST API候補"));
            assertThat(markdown.indexOf("## 5. 曖昧点・確認事項"))
                    .as(sample).isLessThan(markdown.indexOf("## 6. REST API候補"));
            assertThat(markdown.indexOf("## 6. REST API候補"))
                    .as(sample).isLessThan(markdown.indexOf("## 7. MCP tools候補"));
            assertThat(markdown).as(sample)
                    .contains(
                            "domains",
                            "businessObjects",
                            "actors",
                            "operations",
                            "relationships",
                            "ambiguities",
                            "### 4.1 AI支援可能操作",
                            "### 4.2 人間承認必須操作",
                            "### 4.3 監査ログ必須操作",
                            "### 4.4 AI直接実行不可操作",
                            "AI Execution Policy",
                            "Human Confirmation Boundary"
                    );

            String aiSupported = section(markdown, "### 4.1 AI支援可能操作", "### 4.2 人間承認必須操作");
            assertThat(aiSupported).as(sample)
                    .doesNotContain("critical")
                    .doesNotContain("AI直接実行不可");
        }
    }

    private ExternalAiImportResult importSample(String fileName) throws Exception {
        String json = Files.readString(samplePath(fileName), StandardCharsets.UTF_8);
        return bridgeService.importJson(json);
    }

    private BlueprintResult generateSample(String fileName) throws Exception {
        ExternalAiImportResult importResult = importSample(fileName);
        assertThat(importResult.valid()).as(fileName).isTrue();
        assertThat(importResult.canGenerate()).as(fileName).isTrue();
        return generationService.generate(importResult.blueprintInput());
    }

    private Path samplePath(String fileName) {
        return Path.of("docs", "20_設計", "自由文構造化v2", "samples", fileName);
    }

    private List<String> endpointPaths(BlueprintResult result) {
        return result.getApiEndpoints().stream()
                .map(endpoint -> endpoint.httpMethod() + " " + endpoint.path())
                .toList();
    }

    private void assertOperationReferencesKnownObjects(BlueprintInput input, String sample) {
        Set<String> objectIds = input.getV2BusinessObjects().stream()
                .map(BlueprintInput.V2BusinessObject::id)
                .collect(Collectors.toSet());
        for (BlueprintInput.V2Operation operation : input.getV2Operations()) {
            assertThat(objectIds).as(sample + " / " + operation.id()).containsAll(operation.objectIds());
        }
    }

    private void assertDangerousOperationsAreSafeSideClassified(BlueprintInput input, String sample) {
        for (BlueprintInput.V2Operation operation : input.getV2Operations()) {
            if (isDangerous(operation)) {
                assertThat(operation.approvalRequired()
                        || operation.aiPermission().equals("not_allowed_directly")
                        || operation.aiPermission().equals("human_only"))
                        .as(sample + " / " + operation.id())
                        .isTrue();
                assertThat(operation.auditLogRequired())
                        .as(sample + " / " + operation.id())
                        .isEqualTo("required");
            } else if (containsDangerKeyword(operation.label())) {
                assertThat(operation.approvalRequired() || operation.auditLogRequired().equals("required"))
                        .as(sample + " / " + operation.id())
                        .isTrue();
            }
        }
    }

    private boolean isDangerous(BlueprintInput.V2Operation operation) {
        return operation.externalAction()
                || operation.stateChanging()
                || Set.of("high", "critical").contains(operation.riskLevel())
                || Set.of("write", "state_transition", "delete", "external_action", "admin")
                .contains(operation.intent());
    }

    private boolean containsDangerKeyword(String label) {
        return List.of("金銭", "契約", "請求", "入金", "決済", "権限", "外部送信", "送信", "削除", "確定")
                .stream()
                .anyMatch(keyword -> label.contains(keyword));
    }

    private BlueprintInput.V2Operation operationByLabel(BlueprintInput input, String label) {
        return input.getV2Operations().stream()
                .filter(operation -> operation.label().equals(label))
                .findFirst()
                .orElseThrow();
    }

    private void assertNoDomainItemFallback(BlueprintResult result) {
        assertThat(majorGeneratedNames(result))
                .doesNotContain("DomainItem")
                .doesNotContain("domain-items")
                .doesNotContain("executeDomainItem");
    }

    private String majorGeneratedNames(BlueprintResult result) {
        List<String> values = new java.util.ArrayList<>();
        values.addAll(result.getApiEndpoints().stream().map(endpoint -> endpoint.path()).toList());
        values.addAll(result.getDtoCandidates().stream().map(dto -> dto.getName()).toList());
        values.addAll(result.getMcpTools().stream().map(tool -> tool.name()).toList());
        values.addAll(result.getMcpTools().stream().map(tool -> tool.relatedApi()).toList());
        values.add(result.getControllerSkeleton().className());
        values.add(result.getControllerSkeleton().sourceCode());
        return String.join("\n", values);
    }

    private String section(String markdown, String startHeading, String endHeading) {
        int start = markdown.indexOf(startHeading);
        int end = markdown.indexOf(endHeading);
        assertThat(start).isGreaterThanOrEqualTo(0);
        assertThat(end).isGreaterThan(start);
        return markdown.substring(start, end);
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

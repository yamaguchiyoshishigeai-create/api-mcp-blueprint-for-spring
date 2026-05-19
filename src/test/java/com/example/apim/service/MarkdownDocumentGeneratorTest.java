package com.example.apim.service;

import com.example.apim.model.BlueprintInput;
import com.example.apim.model.BlueprintResult;
import com.example.apim.model.ControllerSkeleton;
import com.example.apim.model.McpToolCandidate;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MarkdownDocumentGeneratorTest {

    @Test
    void includesMainSections() {
        MarkdownDocumentGenerator generator = new MarkdownDocumentGenerator();
        BlueprintInput input = new BlueprintInput();
        input.setTargetDomain("顧客管理");
        input.setSystemTypes(List.of("CRM"));
        input.setPrimaryDomain("顧客管理");
        input.setRelatedDomains(List.of("問い合わせ管理"));
        input.setUserTypes("営業担当");
        input.setRequiredOperations("検索");

        BlueprintResult result = new BlueprintResult();
        result.setApiDesignSummary("summary");
        result.setControllerSkeleton(new ControllerSkeleton("CustomerController", "@RestController"));

        String markdown = generator.generate(input, result);

        assertThat(markdown).contains("## 3. 抽出された業務構造");
        assertThat(markdown).contains("## 4. AI支援・承認・監査の分類");
        assertThat(markdown).contains("## 5. 曖昧点・確認事項");
        assertThat(markdown).contains("## 6. REST API候補");
        assertThat(markdown).contains("## 7. MCP tools候補");
        assertThat(markdown).contains("## 10. API/MCP対応表");
        assertThat(markdown).contains("## 11. セキュリティ・承認・監査設計");
    }

    @Test
    void usesInputDrivenOverviewInsteadOfApimInternalMvpText() {
        MarkdownDocumentGenerator generator = new MarkdownDocumentGenerator();
        BlueprintInput input = new BlueprintInput();
        input.setTargetDomain("顧客管理");
        input.setSystemTypes(List.of("CRM"));
        input.setPrimaryDomain("顧客管理");
        input.setRelatedDomains(List.of("問い合わせ管理"));
        input.setUserTypes("営業担当");
        input.setRequiredOperations("顧客検索");

        BlueprintResult result = new BlueprintResult();
        result.setControllerSkeleton(new ControllerSkeleton("CustomerController", "@RestController"));

        String markdown = generator.generate(input, result);

        assertThat(markdown)
                .contains("## 1. 設計対象概要")
                .contains("入力要件に基づき")
                .contains("CRMの顧客管理（関連ドメイン: 問い合わせ管理）")
                .contains("必要な操作として顧客検索を想定し")
                .contains("業務構造、曖昧点、安全分類、API、MCP tools、resources、prompts、権限・承認・監査設計を整理した設計成果物である。")
                .doesNotContain("APIM for Spring の初期MVP向け設計成果物。");
    }

    @Test
    void includesDomainBoundarySummaryFromNormalizedInput() {
        MarkdownDocumentGenerator generator = new MarkdownDocumentGenerator();
        BlueprintInput input = new BlueprintInput();
        input.setTargetDomain("顧客管理 / 問い合わせ管理");
        input.setSystemTypes(List.of("顧客対応CRM", "保守サポート管理"));
        input.setPrimaryDomain("顧客管理");
        input.setRelatedDomains(List.of("顧客管理", "問い合わせ管理"));
        input.setUserTypes("営業担当");
        input.setRequiredOperations("検索");

        BlueprintResult result = new BlueprintResult();
        result.setControllerSkeleton(new ControllerSkeleton("CustomerController", "@RestController"));

        String markdown = generator.generate(input, result);

        assertThat(markdown)
                .contains("- 対象システム種別: 顧客対応CRM / 保守サポート管理")
                .contains("- 主ドメイン: 顧客管理")
                .contains("- 関連ドメイン: 問い合わせ管理")
                .contains("- 正規化後ドメイン一覧: 顧客管理 / 問い合わせ管理");
    }

    @Test
    void keepsLegacyTargetDomainOnlyInputReadable() {
        MarkdownDocumentGenerator generator = new MarkdownDocumentGenerator();
        BlueprintInput input = new BlueprintInput();
        input.setTargetDomain("注文管理");
        input.setUserTypes("業務担当");
        input.setRequiredOperations("注文検索");

        BlueprintResult result = new BlueprintResult();
        result.setControllerSkeleton(new ControllerSkeleton("OrderController", "@RestController"));

        String markdown = generator.generate(input, result);

        assertThat(markdown)
                .contains("- 主ドメイン: 注文管理")
                .contains("- 関連ドメイン: なし")
                .contains("- 正規化後ドメイン一覧: 注文管理");
    }


    @Test
    void includesV2ConfirmationDetailsInMarkdownSummary() {
        MarkdownDocumentGenerator generator = new MarkdownDocumentGenerator();
        BlueprintInput input = new BlueprintInput();
        input.setTargetDomain("営業案件管理 / 契約請求管理");
        input.setSystemTypes(List.of("営業案件管理", "契約請求管理"));
        input.setPrimaryDomain("営業案件管理");
        input.setRelatedDomains(List.of("営業案件管理", "契約請求管理"));
        input.setUserTypes("- 営業担当\n- 契約担当者\n- 承認者");
        input.setRequiredOperations("- 顧客検索\n- 請求確定依頼");
        input.setAllowedAiOperations("- 顧客検索\n- 商談履歴要約");
        input.setReadOnlyOperations("- 顧客検索\n- 請求状況確認");
        input.setWriteOperations("- 請求確定依頼");
        input.setApprovalRequiredOperations("- 請求確定依頼");
        input.setAuditLogRequiredOperations("- 請求確定依頼");
        input.setAuthenticationMethod("authenticated_user");
        input.setTargetUsers("営業担当、契約担当者、承認者");

        BlueprintResult result = new BlueprintResult();
        result.setControllerSkeleton(new ControllerSkeleton("SalesBillingController", "@RestController"));

        String markdown = generator.generate(input, result);

        assertThat(markdown)
                .contains("## 4. AI支援・承認・監査の分類")
                .contains("### 4.1 AI支援可能操作")
                .contains("顧客検索")
                .contains("商談履歴要約")
                .contains("### 4.2 人間承認必須操作")
                .contains("請求確定依頼")
                .contains("### 4.3 監査ログ必須操作")
                .contains("- 想定認証方式: authenticated_user")
                .contains("- 想定利用者: 営業担当、契約担当者、承認者");
    }

    @Test
    void structuresV2MarkdownBeforeApiCandidatesAndKeepsSafetyColumns() {
        MarkdownDocumentGenerator generator = new MarkdownDocumentGenerator();
        BlueprintInput input = new BlueprintInput();
        input.setTargetDomain("営業案件管理 / 契約請求管理");
        input.setSystemTypes(List.of("営業案件管理", "契約請求管理"));
        input.setPrimaryDomain("営業案件管理");
        input.setRelatedDomains(List.of("営業案件管理", "契約請求管理"));
        input.setBusinessRequirements("営業担当と契約担当者が、顧客・商談・見積・契約・請求・入金を確認する。");
        input.setUserTypes("- 営業担当\n- 契約担当者");
        input.setRequiredOperations("- 商談履歴要約\n- 見積金額変更\n- 請求確定\n- 入金消込");
        input.setAllowedAiOperations("- 商談履歴要約\n- 失注リスク候補提示\n- フォローアップ文案作成");
        input.setApprovalRequiredOperations("- 見積金額変更\n- 請求確定\n- 入金消込");
        input.setAuditLogRequiredOperations("- 見積金額変更\n- 請求確定\n- 入金消込");
        input.setV2Domains(List.of(
                new BlueprintInput.V2Domain("sales", "営業案件管理", "primary", "顧客、商談、見積を扱う。"),
                new BlueprintInput.V2Domain("contract_billing", "契約請求管理", "primary", "契約、請求、入金を扱う。")
        ));
        input.setV2BusinessObjects(List.of(
                new BlueprintInput.V2BusinessObject("customer", "顧客", "sales", "confidential", List.of("customer_related")),
                new BlueprintInput.V2BusinessObject("opportunity", "商談", "sales", "confidential", List.of("sales")),
                new BlueprintInput.V2BusinessObject("quote", "見積", "sales", "confidential", List.of("financial")),
                new BlueprintInput.V2BusinessObject("contract", "契約", "contract_billing", "restricted", List.of("contract")),
                new BlueprintInput.V2BusinessObject("invoice", "請求", "contract_billing", "restricted", List.of("billing")),
                new BlueprintInput.V2BusinessObject("payment", "入金", "contract_billing", "restricted", List.of("payment"))
        ));
        input.setV2Actors(List.of(
                new BlueprintInput.V2Actor("sales_rep", "営業担当", "human_user"),
                new BlueprintInput.V2Actor("contract_staff", "契約担当者", "human_user")
        ));
        input.setV2Operations(List.of(
                new BlueprintInput.V2Operation("summarize_opportunity_history", "商談履歴要約", "",
                        List.of("sales_rep"), List.of("opportunity"), "ai_summary", "ai_assisted", "allowed",
                        false, "recommended", "medium", false, false, "summary"),
                new BlueprintInput.V2Operation("suggest_loss_risk", "失注リスク候補提示", "",
                        List.of("sales_rep"), List.of("opportunity"), "ai_analysis", "ai_assisted", "allowed",
                        false, "recommended", "medium", false, false, "candidate_list"),
                new BlueprintInput.V2Operation("draft_followup_message", "フォローアップ文案作成", "",
                        List.of("sales_rep"), List.of("customer", "opportunity"), "ai_draft", "draft_only", "allowed",
                        false, "recommended", "medium", false, false, "draft_text"),
                new BlueprintInput.V2Operation("request_quote_amount_change", "見積金額変更", "",
                        List.of("sales_rep"), List.of("quote"), "approval_request", "human_approved_write",
                        "not_allowed_directly", true, "required", "high", false, true, "approval_request"),
                new BlueprintInput.V2Operation("request_invoice_confirmation", "請求確定", "",
                        List.of("contract_staff"), List.of("invoice"), "approval_request", "human_approved_write",
                        "not_allowed_directly", true, "required", "high", false, true, "approval_request"),
                new BlueprintInput.V2Operation("request_payment_reconciliation", "入金消込", "",
                        List.of("contract_staff"), List.of("payment"), "approval_request", "human_approved_write",
                        "not_allowed_directly", true, "required", "high", false, true, "approval_request")
        ));
        input.setV2Relationships(List.of(
                new BlueprintInput.V2Relationship("invoice_has_payments", "invoice", "payment",
                        "has_many", "1請求は複数入金に紐づく。")
        ));
        input.setV2Ambiguities(List.of(
                new BlueprintInput.V2Ambiguity("approval_actor_unspecified", "missing_approval_actor",
                        "承認者ロールが明示されていません。", List.of("request_invoice_confirmation"),
                        "具体ロールは後続設計で確定する。", "medium")
        ));

        BlueprintResult result = new BlueprintResult();
        result.setApiDesignSummary("summary");
        result.setApiEndpoints(List.of(new com.example.apim.model.ApiEndpointCandidate(
                "POST", "/api/invoices/{id}/confirmation-requests", "請求確定依頼作成",
                "契約担当者", "InvoiceChangeRequest", "ApprovalResponse", "承認依頼権限",
                "必須", "必須", "業務オブジェクトAPI", "請求")));
        result.setMcpTools(List.of(new McpToolCandidate("requestInvoiceConfirmationApproval",
                "請求確定承認依頼", "approvalRequest", "ApprovalResponse",
                "/api/invoices/{id}/confirmation-requests", "approval-request",
                "AI直接実行不可（承認依頼まで）", "必須", "必須")));
        result.setControllerSkeleton(new ControllerSkeleton("SalesBillingController", "@RestController"));

        String markdown = generator.generate(input, result);

        assertThat(markdown).contains(
                "営業案件管理", "契約請求管理", "顧客", "商談", "見積", "契約", "請求", "入金",
                "営業担当", "契約担当者", "商談履歴要約", "失注リスク候補提示", "フォローアップ文案作成",
                "見積金額変更", "請求確定", "入金消込", "承認者ロールが明示されていません。");
        assertThat(markdown.indexOf("## 3. 抽出された業務構造")).isLessThan(markdown.indexOf("## 6. REST API候補"));
        assertThat(markdown.indexOf("## 4. AI支援・承認・監査の分類")).isLessThan(markdown.indexOf("## 6. REST API候補"));
        assertThat(markdown.indexOf("## 5. 曖昧点・確認事項")).isLessThan(markdown.indexOf("## 6. REST API候補"));
        assertThat(markdown.indexOf("## 6. REST API候補")).isLessThan(markdown.indexOf("## 7. MCP tools候補"));
        assertThat(markdown)
                .contains("### 4.1 AI支援可能操作")
                .contains("### 4.2 人間承認必須操作")
                .contains("### 4.3 監査ログ必須操作")
                .contains("### 4.4 AI直接実行不可操作")
                .contains("AI直接実行不可")
                .contains("承認依頼権限")
                .contains("InvoiceChangeRequest")
                .contains("ApprovalResponse")
                .contains("AI Execution Policy")
                .contains("Human Confirmation Boundary");
    }

    @Test
    void highAndCriticalAllowedOperationsAreNotDuplicatedAsAiSupported() {
        MarkdownDocumentGenerator generator = new MarkdownDocumentGenerator();
        BlueprintInput input = new BlueprintInput();
        input.setTargetDomain("審査管理");
        input.setPrimaryDomain("審査管理");
        input.setUserTypes("審査担当");
        input.setRequiredOperations("- 高リスク候補判定\n- 重大リスク候補判定");
        input.setAllowedAiOperations("- 高リスク候補判定\n- 重大リスク候補判定");
        input.setV2Operations(List.of(
                new BlueprintInput.V2Operation("detect_high_risk_candidate", "高リスク候補判定", "",
                        List.of("reviewer"), List.of("review_case"), "ai_analysis", "ai_assisted",
                        "allowed", false, "recommended", "high", false, false, "candidate_list"),
                new BlueprintInput.V2Operation("detect_critical_risk_candidate", "重大リスク候補判定", "",
                        List.of("reviewer"), List.of("review_case"), "ai_analysis", "ai_assisted",
                        "allowed", false, "recommended", "critical", false, false, "candidate_list")
        ));

        BlueprintResult result = new BlueprintResult();
        result.setControllerSkeleton(new ControllerSkeleton("ReviewController", "@RestController"));

        String markdown = generator.generate(input, result);
        String aiSupportedSection = section(markdown, "### 4.1 AI支援可能操作", "### 4.2 人間承認必須操作");
        String aiForbiddenSection = section(markdown, "### 4.4 AI直接実行不可操作", "## 5. 曖昧点・確認事項");

        assertThat(aiSupportedSection)
                .doesNotContain("高リスク候補判定")
                .doesNotContain("重大リスク候補判定");
        assertThat(aiForbiddenSection)
                .contains("高リスク候補判定")
                .contains("重大リスク候補判定")
                .contains("AI直接実行不可")
                .contains("high")
                .contains("critical");
    }


    @Test
    void outOfScopeSectionIsExpressedAsLaterPhaseDecisions() {
        MarkdownDocumentGenerator generator = new MarkdownDocumentGenerator();
        BlueprintInput input = new BlueprintInput();
        input.setTargetDomain("注文管理");
        input.setUserTypes("業務担当");
        input.setRequiredOperations("注文検索");

        BlueprintResult result = new BlueprintResult();
        result.setControllerSkeleton(new ControllerSkeleton("OrderController", "@RestController"));

        String markdown = generator.generate(input, result);

        assertThat(markdown)
                .contains("### 12.3 後続フェーズで具体化する事項")
                .contains("後続フェーズで要件、運用条件、セキュリティ方針に応じて具体化する。")
                .contains("実装禁止事項ではなく、追加設計・実装判断が必要な事項として扱う。")
                .contains("- MCPサーバーとしての実行形態")
                .contains("- 外部LLM API連携")
                .contains("- DB永続化")
                .contains("- 認証認可方式")
                .contains("- OpenAPI定義の生成・公開範囲")
                .doesNotContain("## 14. 初期MVPで実装しないこと")
                .doesNotContain("- 完全動作するMCPサーバー")
                .doesNotContain("- 認証認可の本格実装")
                .doesNotContain("- OpenAPI完全生成");
    }

    private String section(String markdown, String startHeading, String endHeading) {
        int start = markdown.indexOf(startHeading);
        int end = markdown.indexOf(endHeading);
        assertThat(start).isGreaterThanOrEqualTo(0);
        assertThat(end).isGreaterThan(start);
        return markdown.substring(start, end);
    }
}

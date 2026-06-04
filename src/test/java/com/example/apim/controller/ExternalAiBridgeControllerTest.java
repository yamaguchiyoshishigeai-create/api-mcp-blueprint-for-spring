package com.example.apim.controller;

import com.example.apim.model.BlueprintInput;
import com.example.apim.model.ExternalAiImportResult;
import com.example.apim.service.ExternalAiPromptBridgeService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest(ExternalAiBridgeController.class)
class ExternalAiBridgeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ExternalAiPromptBridgeService bridgeService;

    @Test
    void bridgePageShowsSaasStyleManualExternalAiFlow() throws Exception {
        mockMvc.perform(get("/external-ai-bridge"))
                .andExpect(status().isOk())
                .andExpect(view().name("external-ai-bridge"))
                .andExpect(content().string(containsString("API + MCP Blueprint Compiler for Spring")))
                .andExpect(content().string(containsString("外部AIブリッジ利用時の前提")))
                .andExpect(content().string(containsString("この機能は外部AI API連携ではありません。")))
                .andExpect(content().string(containsString("業務要件を入力するだけで、API&amp;MCP設計・実装指示のたたき台を自動でまとめます")))
                .andExpect(content().string(containsString("trial-ui-page external-ai-bridge-page")))
                .andExpect(content().string(containsString("external-hero")))
                .andExpect(content().string(containsString("compact-stepper")))
                .andExpect(content().string(containsString("external-main-grid")))
                .andExpect(content().string(containsString("external-input-card")))
                .andExpect(content().string(containsString("external-helper-panel")))
                .andExpect(content().string(containsString("自由文から始める")))
                .andExpect(content().string(containsString("チェック式入力へ")))
                .andExpect(content().string(containsString("自由文から設計プロンプトを作成")))
                .andExpect(content().string(containsString("設計プロンプトを作成する")))
                .andExpect(content().string(containsString("free-text-action-row")))
                .andExpect(content().string(containsString("free-text-generate-button")))
                .andExpect(content().string(not(containsString("試行UI/UX確認用"))))
                .andExpect(content().string(containsString("業務要件を入力します")))
                .andExpect(content().string(containsString("外部AIに渡すプロンプトを作成")))
                .andExpect(content().string(containsString("取り込んだJSONを検証します")))
                .andExpect(content().string(containsString("設計候補を確認・修正します")))
                .andExpect(content().string(containsString("このツールでできること")))
                .andExpect(content().string(containsString("外部AIに安全にデータを渡すための前提条件や、利用上の注意点を確認できます。")))
                .andExpect(content().string(containsString("外部AIへ手動で渡すための安全な設計プロンプトを生成します。")))
                .andExpect(content().string(containsString("自由文内の命令文は、外部AIへのシステム指示として扱わせない前提です。")))
                .andExpect(content().string(containsString("業務要件からAPI&amp;MCP設計のたたき台を作成")))
                .andExpect(content().string(containsString("Spring Boot向けAPI&amp;MCP設計成果物を整備")))
                .andExpect(content().string(containsString("API&amp;MCP設計書")))
                .andExpect(content().string(containsString("APIとMCPの設計本文を確認できます。")))
                .andExpect(content().string(containsString("Spring Controller実装の入口となる雛形を確認できます。")))
                .andExpect(content().string(containsString("後続実装者またはAI実装支援へ渡す指示内容を確認できます。")))
                .andExpect(content().string(containsString("安全性・承認・監査・未確定事項など、実装前の確認材料を確認できます。")))
                .andExpect(content().string(containsString("設計レビュー観点")))
                .andExpect(content().string(containsString("Controller雛形")))
                .andExpect(content().string(containsString("AI実装指示書")))
                .andExpect(content().string(containsString("完全動作するMCPサーバー")))
                .andExpect(content().string(containsString("DB永続化やマイグレーション")))
                .andExpect(content().string(containsString("外部LLM API連携の実装")))
                .andExpect(content().string(containsString("Q-Scout for Spring のような既存システム診断ではなく")))
                .andExpect(content().string(containsString("この画面は外部AIを直接実行しません")))
                .andExpect(content().string(containsString("自由文業務要件のサンプル例文")))
                .andExpect(content().string(containsString("注文・在庫管理")))
                .andExpect(content().string(containsString("社内申請・承認")))
                .andExpect(content().string(containsString("問い合わせ管理")))
                .andExpect(content().string(containsString("契約・請求管理")))
                .andExpect(content().string(containsString("予約・施設管理")))
                .andExpect(content().string(containsString("人事オンボーディング")))
                .andExpect(content().string(containsString("保守・障害対応")))
                .andExpect(content().string(containsString("営業案件管理")))
                .andExpect(content().string(containsString("ナレッジ検索")))
                .andExpect(content().string(containsString("insertFreeTextSample")))
                .andExpect(content().string(containsString("currentText.includes(nextText)")))
                .andExpect(content().string(containsString("また、")))
                .andExpect(content().string(containsString("clearFreeText")))
                .andExpect(content().string(containsString("data-sample")));
    }


    @Test
    void bridgePageDoesNotKeepDetailedOfficialAiLinkList() throws Exception {
        MvcResult result = mockMvc.perform(get("/external-ai-bridge"))
                .andExpect(status().isOk())
                .andExpect(view().name("external-ai-bridge"))
                .andReturn();

        String html = result.getResponse().getContentAsString(StandardCharsets.UTF_8);
        assertTrue(html.contains("この画面は外部AIを直接実行しません"));
        assertFalse(html.contains("ChatGPT公式サイトを別タブで開く"));
        assertFalse(html.contains("Claude公式サイトを別タブで開く"));
        assertFalse(html.contains("Gemini公式サイトを別タブで開く"));
        assertFalse(html.contains("Microsoft Copilot公式サイトを別タブで開く"));
    }


    @Test
    void promptGenerationDisplaysGeneratedPrompt() throws Exception {
        when(bridgeService.generatePrompt(anyString())).thenReturn("generated markdown prompt");

        mockMvc.perform(post("/external-ai-bridge/prompt")
                        .param("freeText", "顧客検索を行いたい"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/external-ai-bridge/prompt"));

        mockMvc.perform(get("/external-ai-bridge/prompt")
                        .sessionAttr("externalAiPrompt", "generated markdown prompt"))
                .andExpect(status().isOk())
                .andExpect(view().name("external-ai-prompt"))
                .andExpect(content().string(containsString("Markdownダウンロード")))
                .andExpect(content().string(containsString("自由文入力トップへ戻る")))
                .andExpect(content().string(containsString("生成プロンプト")))
                .andExpect(content().string(containsString("externalAiPromptText")))
                .andExpect(content().string(containsString("Step 2: 外部AI投入用プロンプト")))
                .andExpect(content().string(containsString("app-hero")))
                .andExpect(content().string(containsString("split-layout")))
                .andExpect(content().string(containsString("prompt-side-panel")))
                .andExpect(content().string(containsString("feature-grid external-ai-links")))
                .andExpect(content().string(containsString("json-import-card")))
                .andExpect(content().string(containsString("prompt-copy-toolbar")))
                .andExpect(content().string(containsString("copy-feedback-bubble")))
                .andExpect(content().string(containsString("コピーされました")))
                .andExpect(content().string(containsString("window.setTimeout")))
                .andExpect(content().string(containsString("生成プロンプトを一括コピー")))
                .andExpect(content().string(containsString("copyGeneratedPrompt")))
                .andExpect(content().string(containsString("navigator.clipboard.writeText")))
                .andExpect(content().string(containsString("promptText.value || promptText.textContent || promptText.innerText")))
                .andExpect(content().string(containsString("document.execCommand")))
                .andExpect(content().string(containsString("外部AI公式リンク集")))
                .andExpect(content().string(containsString("ChatGPT公式サイトを別タブで開く")))
                .andExpect(content().string(containsString("Claude公式サイトを別タブで開く")))
                .andExpect(content().string(containsString("Gemini公式サイトを別タブで開く")))
                .andExpect(content().string(containsString("Microsoft Copilot公式サイトを別タブで開く")))
                .andExpect(content().string(containsString("target=\"_blank\"")))
                .andExpect(content().string(containsString("rel=\"noopener noreferrer\"")))
                .andExpect(content().string(containsString("2. APIM取り込み用JSONを読み込む")))
                .andExpect(content().string(containsString("ファイルアップロードで取り込む")))
                .andExpect(content().string(containsString("貼り付けで取り込む")))
                .andExpect(content().string(containsString("switchJsonInputMode")))
                .andExpect(content().string(containsString("DOMContentLoaded")))
                .andExpect(content().string(containsString("jsonFilePanel")))
                .andExpect(content().string(containsString("jsonPastePanel")))
                .andExpect(content().string(containsString("bottom-action-bar prompt-json-actions")))
                .andExpect(content().string(containsString("トップページへ戻る")))
                .andExpect(content().string(not(containsString("Step2の最終アクション"))))
                .andExpect(content().string(containsString("generated markdown prompt")));

        verify(bridgeService).generatePrompt("顧客検索を行いたい");
    }


    @Test
    void promptGenerationRedirectDoesNotExposePromptInLocationQuery() throws Exception {
        String sensitivePrompt = "generated markdown prompt with 業務要件 and confidential-customer-search";
        when(bridgeService.generatePrompt(anyString())).thenReturn(sensitivePrompt);

        MvcResult result = mockMvc.perform(post("/external-ai-bridge/prompt")
                        .param("freeText", "顧客検索を行いたい confidential-customer-search"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/external-ai-bridge/prompt"))
                .andReturn();

        assertRedirectLocationDoesNotExpose(result.getResponse().getHeader(HttpHeaders.LOCATION),
                "externalAiPrompt", "generated markdown prompt", "confidential-customer-search", "業務要件");
    }

    @Test
    void promptGenerationRedirectPreservesServletContextPath() throws Exception {
        when(bridgeService.generatePrompt(anyString())).thenReturn("generated prompt");

        MvcResult result = mockMvc.perform(post("/apim/external-ai-bridge/prompt")
                        .contextPath("/apim")
                        .param("freeText", "顧客検索を行いたい"))
                .andExpect(status().is3xxRedirection())
                .andReturn();

        String location = result.getResponse().getHeader(HttpHeaders.LOCATION);
        assertTrue(location != null && location.equals("/apim/external-ai-bridge/prompt"),
                "Redirect Location must preserve servlet context path: " + location);
        assertRedirectLocationDoesNotExpose(location, "externalAiPrompt", "generated prompt");
    }

    @Test
    void importTextRedirectDoesNotExposeJsonOrModelAttributesInLocationQuery() throws Exception {
        String sensitiveRequirement = "secret-requirement-for-contract-billing";
        when(bridgeService.importJson(anyString()))
                .thenReturn(ExternalAiImportResult.canGenerate(validBlueprintInput(), "ok", List.of()));

        MvcResult result = mockMvc.perform(post("/external-ai-bridge/import-text")
                        .param("jsonText", "{\"businessRequirements\":\"" + sensitiveRequirement + "\"}"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/external-ai-bridge/import-result"))
                .andReturn();

        assertRedirectLocationDoesNotExpose(result.getResponse().getHeader(HttpHeaders.LOCATION),
                "jsonText", "importResult", "blueprintInput", sensitiveRequirement, "businessRequirements");
    }

    @Test
    void importFileRedirectDoesNotExposeJsonOrModelAttributesInLocationQuery() throws Exception {
        String sensitiveRequirement = "secret-requirement-from-uploaded-json";
        MockMultipartFile file = new MockMultipartFile(
                "jsonFile", "blueprint.json", "application/json",
                ("{\"businessRequirements\":\"" + sensitiveRequirement + "\"}").getBytes(StandardCharsets.UTF_8));
        when(bridgeService.importJson(anyString()))
                .thenReturn(ExternalAiImportResult.canGenerate(validBlueprintInput(), "ok", List.of()));

        MvcResult result = mockMvc.perform(multipart("/external-ai-bridge/import-file").file(file))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/external-ai-bridge/import-result"))
                .andReturn();

        assertRedirectLocationDoesNotExpose(result.getResponse().getHeader(HttpHeaders.LOCATION),
                "jsonFile", "importResult", "blueprintInput", sensitiveRequirement, "businessRequirements");
    }

    @Test
    void promptDownloadReturnsMarkdownAttachmentWhenPromptIsStored() throws Exception {
        mockMvc.perform(get("/external-ai-bridge/prompt/download")
                        .sessionAttr("externalAiPrompt", "# prompt"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION,
                        containsString("apim-external-ai-prompt.md")))
                .andExpect(content().string("# prompt"));
    }

    @Test
    void promptPageRedirectsWhenPromptIsNotStored() throws Exception {
        mockMvc.perform(get("/external-ai-bridge/prompt"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/external-ai-bridge"));
    }

    @Test
    void importTextRedirectsToImportResultAfterStoringResult() throws Exception {
        when(bridgeService.importJson("{valid}"))
                .thenReturn(ExternalAiImportResult.canGenerate(validBlueprintInput(), "ok", List.of()));

        mockMvc.perform(post("/external-ai-bridge/import-text")
                        .param("jsonText", "{valid}"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/external-ai-bridge/import-result"));
    }

    @Test
    void importResultRedirectsWhenResultIsNotStored() throws Exception {
        mockMvc.perform(get("/external-ai-bridge/import-result"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/external-ai-bridge/prompt"));
    }

    @Test
    void importTextWithValidV2JsonShowsExtractionSummary() throws Exception {
        BlueprintInput input = new BlueprintInput();
        input.setBusinessRequirements("営業案件管理と契約請求管理を横断して支援する。");
        input.setTargetDomain("営業案件管理 / 契約請求管理");
        input.setPrimaryDomain("営業案件管理");
        input.setRelatedDomains(List.of("営業案件管理", "契約請求管理"));
        input.setSystemTypes(List.of("営業案件管理", "契約請求管理"));
        input.setUserTypes("- 営業担当\n- 契約担当者");
        input.setRequiredOperations("- 顧客検索\n- 請求確定依頼");
        input.setAllowedAiOperations("- 顧客検索");
        input.setApprovalRequiredOperations("- 請求確定依頼");
        input.setAuditLogRequiredOperations("- 請求確定依頼");

        ExternalAiImportResult.ExtractionSummary summary = new ExternalAiImportResult.ExtractionSummary(
                List.of("営業案件管理", "契約請求管理"),
                List.of("顧客（domainId=sales / sensitivity=confidential）",
                        "請求（domainId=contract_billing / sensitivity=restricted）"),
                List.of("営業担当 (human_user)", "契約担当者 (human_user)", "承認者 (approver)"),
                List.of("顧客検索（search / direct_read / low）",
                        "請求確定依頼（approval_request / human_approved_write / high）"),
                List.of("opportunity -> invoice（references）"),
                List.of("請求確定依頼"),
                List.of("請求確定依頼"),
                List.of("請求確定の承認者ロールが未確定です。（medium）")
        );

        ExternalAiImportResult importResult = ExternalAiImportResult.canGenerate(input, "ok", List.of(), summary);
        when(bridgeService.importJson("{v2}")).thenReturn(importResult);

        mockMvc.perform(get("/external-ai-bridge/import-result")
                        .sessionAttr("importResult", importResult)
                        .sessionAttr("blueprintInput", input))
                .andExpect(status().isOk())
                .andExpect(view().name("external-ai-import-result"))
                .andExpect(content().string(containsString("抽出結果の確認")))
                .andExpect(content().string(not(containsString("v2抽出結果の確認"))))
                .andExpect(content().string(containsString(
                        "<a class=\"button-link hero-primary-action\" href=\"#generate\">設計候補生成へ進む</a>")))
                .andExpect(content().string(containsString(
                        "<a class=\"button-link secondary\" href=\"#review-form\">確認項目を修正する</a>")))
                .andExpect(content().string(containsString(
                        "<a class=\"button-link secondary\" href=\"#summary\">抽出結果を見る</a>")))
                .andExpect(content().string(containsString("JSONの検証と反映準備が完了しました")))
                .andExpect(content().string(containsString("step3-import-result-page")))
                .andExpect(content().string(containsString("import-progress-stepper")))
                .andExpect(content().string(containsString("import-result-dashboard")))
                .andExpect(content().string(containsString("step3-status-card")))
                .andExpect(content().string(containsString("step3-review-card")))
                .andExpect(content().string(containsString("step3-side-stack")))
                .andExpect(content().string(containsString("summary-card-grid")))
                .andExpect(content().string(containsString("summary-card")))
                .andExpect(content().string(containsString("業務領域")))
                .andExpect(content().string(containsString("業務オブジェクト")))
                .andExpect(content().string(containsString("利用者・ロール")))
                .andExpect(content().string(containsString("操作")))
                .andExpect(content().string(containsString("関係")))
                .andExpect(content().string(containsString("承認必須操作")))
                .andExpect(content().string(containsString("監査ログ必須操作")))
                .andExpect(content().string(containsString("曖昧点・確認事項")))
                .andExpect(content().string(containsString("営業案件管理")))
                .andExpect(content().string(containsString("顧客")))
                .andExpect(content().string(containsString("請求確定依頼")))
                .andExpect(content().string(containsString("請求確定の承認者ロールが未確定です。")));
    }


    @Test
    void importTextWithValidJsonShowsConfirmationForm() throws Exception {
        BlueprintInput input = new BlueprintInput();
        input.setBusinessRequirements("営業担当が顧客情報を検索する。");
        input.setTargetDomain("顧客管理");
        input.setPrimaryDomain("顧客管理");
        input.setRelatedDomains(List.of("顧客管理"));
        input.setUserTypes("- 営業担当\n- AIアシスタント");
        input.setRequiredOperations("- 顧客検索");
        input.setAllowedAiOperations("- 顧客検索");
        ExternalAiImportResult importResult = ExternalAiImportResult.canGenerate(input, "ok", List.of());
        when(bridgeService.importJson("{valid}")).thenReturn(importResult);

        MvcResult result = mockMvc.perform(get("/external-ai-bridge/import-result")
                        .sessionAttr("importResult", importResult)
                        .sessionAttr("blueprintInput", input))
                .andExpect(status().isOk())
                .andExpect(view().name("external-ai-import-result"))
                .andExpect(model().attributeExists("blueprintInput"))
                .andExpect(content().string(containsString("反映内容の確認・修正")))
                .andExpect(content().string(containsString("confirmation-form-layout")))
                .andExpect(content().string(containsString("step3-reflection-card")))
                .andExpect(content().string(containsString("step3-form-grid")))
                .andExpect(content().string(containsString("必須項目")))
                .andExpect(content().string(containsString("任意・安全確認項目")))
                .andExpect(content().string(containsString("営業担当が顧客情報を検索する。")))
                .andExpect(content().string(containsString("step3-action-bar")))
                .andExpect(content().string(containsString("step3-action-primary-group")))
                .andExpect(content().string(containsString("step3-action-nav-group")))
                .andExpect(content().string(containsString("href=\"/external-ai-bridge/prompt\"")))
                .andExpect(content().string(containsString("Step2へ戻る")))
                .andExpect(content().string(containsString("トップページへ戻る")))
                .andExpect(content().string(not(containsString("step3-secondary-links"))))
                .andExpect(content().string(containsString("確認した内容で設計候補を生成する")))
                .andExpect(content().string(containsString("step3-action-bar")))
                .andExpect(content().string(containsString("step3-action-primary-group")))
                .andExpect(content().string(containsString("step3-action-nav-group")))
                .andReturn();


    }

    @Test
    void importTextWithValidJsonShowsSuccessBeforeWarnings() throws Exception {
        BlueprintInput input = validBlueprintInput();
        ExternalAiImportResult importResult = ExternalAiImportResult.canGenerate(
                        input,
                        "ok",
                        List.of("承認必須操作は人間確認が必要です。"));
        when(bridgeService.importJson("{valid-warning}")).thenReturn(importResult);

        MvcResult result = mockMvc.perform(get("/external-ai-bridge/import-result")
                        .sessionAttr("importResult", importResult)
                        .sessionAttr("blueprintInput", input))
                .andExpect(status().isOk())
                .andExpect(view().name("external-ai-import-result"))
                .andExpect(model().attributeExists("blueprintInput"))
                .andExpect(content().string(containsString("JSONの検証と反映準備が完了しました")))
                .andExpect(content().string(containsString("step3-import-result-page")))
                .andExpect(content().string(containsString("import-progress-stepper")))
                .andExpect(content().string(containsString("import-result-dashboard")))
                .andExpect(content().string(containsString("step3-status-card")))
                .andExpect(content().string(containsString("step3-side-stack")))
                .andExpect(content().string(containsString("反映内容の確認・修正")))
                .andExpect(content().string(containsString("設計候補生成前の確認事項")))
                .andExpect(content().string(containsString("確認した内容で設計候補を生成する")))
                .andReturn();

        String html = result.getResponse().getContentAsString(StandardCharsets.UTF_8);
        assertAppearsInOrder(
                html,
                "JSONの検証と反映準備が完了しました",
                "設計候補生成前の確認事項");
    }

    @Test
    void importTextWithValidJsonShowsWarningsImmediatelyBeforeGenerateButton() throws Exception {
        BlueprintInput input = validBlueprintInput();
        ExternalAiImportResult importResult = ExternalAiImportResult.canGenerate(
                        input,
                        "ok",
                        List.of("監査ログ対象を確認してください。"));
        when(bridgeService.importJson("{valid-warning-order}")).thenReturn(importResult);

        MvcResult result = mockMvc.perform(get("/external-ai-bridge/import-result")
                        .sessionAttr("importResult", importResult)
                        .sessionAttr("blueprintInput", input))
                .andExpect(status().isOk())
                .andExpect(view().name("external-ai-import-result"))
                .andReturn();

        String html = result.getResponse().getContentAsString(StandardCharsets.UTF_8);
        assertAppearsInOrder(
                html,
                "反映内容の確認・修正",
                "設計候補生成前の確認事項",
                "確認した内容で設計候補を生成する");
    }

    @Test
    void importTextWithCanGenerateFalseShowsReason() throws Exception {
        ExternalAiImportResult importResult = ExternalAiImportResult.cannotGenerate(
                "対象ドメインが不足しています。", List.of("対象ドメイン"), List.of());
        when(bridgeService.importJson("{cannot}")).thenReturn(importResult);

        mockMvc.perform(get("/external-ai-bridge/import-result")
                        .sessionAttr("importResult", importResult))
                .andExpect(status().isOk())
                .andExpect(view().name("external-ai-import-result"))
                .andExpect(content().string(containsString("生成不可")))
                .andExpect(content().string(containsString("対象ドメインが不足しています。")))
                .andExpect(content().string(containsString("対象ドメイン")));
    }

    @Test
    void importTextWithInvalidJsonShowsErrorEscaped() throws Exception {
        ExternalAiImportResult importResult =
                ExternalAiImportResult.invalid(List.of("<script>alert(1)</script>"), List.of());
        when(bridgeService.importJson("<script>alert(1)</script>")).thenReturn(importResult);

        mockMvc.perform(get("/external-ai-bridge/import-result")
                        .sessionAttr("importResult", importResult))
                .andExpect(status().isOk())
                .andExpect(view().name("external-ai-import-result"))
                .andExpect(content().string(containsString("エラー")))
                .andExpect(content().string(containsString("<h2>エラー</h2>")))
                .andExpect(content().string(containsString("&lt;script&gt;alert(1)&lt;/script&gt;")))
                .andExpect(content().string(not(containsString("JSONの検証と反映準備が完了しました"))))
                .andExpect(content().string(not(containsString("確認した内容で設計候補を生成する"))))
                .andExpect(content().string(not(containsString("反映内容の確認・修正"))));
    }


    private void assertRedirectLocationDoesNotExpose(String location, String... forbiddenFragments) {
        assertTrue(location != null && !location.isBlank());
        String decodedLocation = URLDecoder.decode(location, StandardCharsets.UTF_8);
        assertFalse(decodedLocation.contains("?"), "Redirect Location must not contain a query string: " + decodedLocation);
        for (String forbiddenFragment : forbiddenFragments) {
            assertFalse(decodedLocation.contains(forbiddenFragment),
                    "Redirect Location leaked forbidden fragment [" + forbiddenFragment + "]: " + decodedLocation);
        }
    }

    private static BlueprintInput validBlueprintInput() {
        BlueprintInput input = new BlueprintInput();
        input.setBusinessRequirements("営業担当が顧客情報を検索する。");
        input.setTargetDomain("顧客管理");
        input.setPrimaryDomain("顧客管理");
        input.setRelatedDomains(List.of("顧客管理"));
        input.setUserTypes("- 営業担当\n- AIアシスタント");
        input.setRequiredOperations("- 顧客検索");
        input.setAllowedAiOperations("- 顧客検索");
        return input;
    }

    private static void assertAppearsInOrder(String html, String... texts) {
        int previousIndex = -1;
        for (String text : texts) {
            int currentIndex = html.indexOf(text);
            assertTrue(currentIndex >= 0, () -> "Expected to find text: " + text);
            assertTrue(currentIndex > previousIndex, () -> "Expected text to appear later: " + text);
            previousIndex = currentIndex;
        }
    }

    @Test
    void importFileRejectsNonJsonExtension() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "jsonFile", "apim-blueprint-input.txt", "text/plain", "{}".getBytes());

        mockMvc.perform(multipart("/external-ai-bridge/import-file").file(file))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/external-ai-bridge/import-result"));

        mockMvc.perform(get("/external-ai-bridge/import-result")
                        .sessionAttr("importResult", ExternalAiImportResult.invalid(
                                List.of(".json ファイルのみアップロードできます。"), List.of())))
                .andExpect(status().isOk())
                .andExpect(view().name("external-ai-import-result"))
                .andExpect(content().string(containsString(".json ファイルのみアップロードできます。")));
    }

    @Test
    void importFileAcceptsJsonExtension() throws Exception {
        ExternalAiImportResult importResult =
                ExternalAiImportResult.invalid(List.of("judgement が存在しません。"), List.of());
        when(bridgeService.importJson("{\"schemaVersion\":\"apim-blueprint-input/v1\"}")).thenReturn(importResult);
        MockMultipartFile file = new MockMultipartFile(
                "jsonFile",
                "apim-blueprint-input.json",
                "application/json",
                "{\"schemaVersion\":\"apim-blueprint-input/v1\"}".getBytes());

        mockMvc.perform(multipart("/external-ai-bridge/import-file").file(file))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/external-ai-bridge/import-result"));

        mockMvc.perform(get("/external-ai-bridge/import-result")
                        .sessionAttr("importResult", importResult))
                .andExpect(status().isOk())
                .andExpect(view().name("external-ai-import-result"))
                .andExpect(content().string(containsString("judgement が存在しません。")));
    }
    @Test
    void step3ActionBarPlacesProgressBeforeNavigation() throws Exception {
        BlueprintInput input = validBlueprintInput();
        when(bridgeService.importJson("{step3-action-order}"))
                .thenReturn(ExternalAiImportResult.canGenerate(input, "ok", List.of()));

        MvcResult result = mockMvc.perform(post("/external-ai-bridge/import-text")
                        .param("jsonText", "{step3-action-order}"))
                .andExpect(status().is3xxRedirection())
                .andReturn();

        String redirectUrl = result.getResponse().getRedirectedUrl();
        MvcResult page = mockMvc.perform(get(redirectUrl)
                        .session((org.springframework.mock.web.MockHttpSession) result.getRequest().getSession(false)))
                .andExpect(status().isOk())
                .andExpect(view().name("external-ai-import-result"))
                .andExpect(content().string(containsString("step3-action-bar")))
                .andExpect(content().string(containsString("step3-action-primary-group")))
                .andExpect(content().string(containsString("step3-action-nav-group")))
                .andReturn();

        String html = page.getResponse().getContentAsString(StandardCharsets.UTF_8);
        assertAppearsInOrder(
                html,
                "step3-action-primary-group",
                "確認した内容で設計候補を生成する",
                "step3-action-nav-group",
                "Step2へ戻る",
                "トップページへ戻る");
    }

}

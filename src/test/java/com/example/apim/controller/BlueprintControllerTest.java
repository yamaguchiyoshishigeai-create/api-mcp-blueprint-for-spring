package com.example.apim.controller;

import com.example.apim.model.BlueprintInput;
import java.util.List;
import com.example.apim.model.BlueprintResult;
import com.example.apim.service.BlueprintGenerationService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest(BlueprintController.class)
class BlueprintControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BlueprintGenerationService generationService;

    @Test
    void getRootRedirectsToExternalAiBridge() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/external-ai-bridge"));
    }

    @Test
    void checklistFormShowsFirstVisitorGuideAndSampleRoute() throws Exception {
        mockMvc.perform(get("/blueprint/form"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("container trial-ui-page check-input-page")))
                .andExpect(content().string(containsString("check-input-hero")))
                .andExpect(content().string(containsString("業務要件からAPI/MCP設計を作るチェック式入力フォーム")))
                .andExpect(content().string(containsString("既存コード診断ではなく")))
                .andExpect(content().string(containsString("API設計書、MCP設計書、Controller雛形、AI実装指示書")))
                .andExpect(content().string(containsString("対象利用者")))
                .andExpect(content().string(containsString("業務要件からAPI設計・MCP設計のたたき台を作りたい利用者")))
                .andExpect(content().string(containsString("ユースケース、エンドポイント候補、データ項目、制約")))
                .andExpect(content().string(containsString("チェック項目を編集する")))
                .andExpect(content().string(containsString("トップページへ戻る")))
                .andExpect(content().string(containsString("check-workflow-steps")))
                .andExpect(content().string(containsString("Step 1")))
                .andExpect(content().string(containsString("Step 4")))
                .andExpect(content().string(containsString("check-overview-layout")))
                .andExpect(content().string(containsString("check-form-layout")))
                .andExpect(content().string(containsString("check-sticky-panel")))
                .andExpect(content().string(containsString("注文・在庫管理サンプル")))
                .andExpect(content().string(containsString("社内申請・承認ワークフローサンプル")))
                .andExpect(content().string(containsString("問い合わせ・サポート管理サンプル")))
                .andExpect(content().string(containsString("契約・請求管理サンプル")))
                .andExpect(content().string(containsString("fillSample('order-inventory')")))
                .andExpect(content().string(containsString("fillSample('internal-approval')")))
                .andExpect(content().string(containsString("fillSample('support-inquiry')")))
                .andExpect(content().string(containsString("fillSample('contract-billing')")))
                .andExpect(content().string(containsString("restoreCheckedItemsFromBlueprintInput")))
                .andExpect(content().string(containsString("設計候補を生成する")));
    }

    @Test
    void blueprintRelatedPagesExposeTopPageLink() throws Exception {
        BlueprintResult mockResult = new BlueprintResult();
        mockResult.setInputSummary("summary");
        mockResult.setApiDesignSummary("api summary");
        mockResult.setBlueprintMarkdown("# markdown");
        mockResult.setImplementationInstructions("instructions");

        MockHttpSession session = new MockHttpSession();
        session.setAttribute("blueprintResult", mockResult);

        mockMvc.perform(get("/blueprint/preview").session(session))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("トップページへ戻る")))
                .andExpect(content().string(containsString("/external-ai-bridge")));

        mockMvc.perform(get("/blueprint/implementation-instructions").session(session))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("トップページへ戻る")))
                .andExpect(content().string(containsString("/external-ai-bridge")));
    }

    @Test
    void getHelpReturns200() throws Exception {
        mockMvc.perform(get("/help"))
                .andExpect(status().isOk())
                .andExpect(view().name("help"));
    }

    @Test
    void postGenerateWithMissingRequiredFieldsReturnsForm() throws Exception {
        mockMvc.perform(post("/blueprint/generate")
                        .param("targetDomain", "")
                        .param("businessRequirements", "")
                        .param("userTypes", "")
                        .param("requiredOperations", "")
                        .param("allowedAiOperations", ""))
                .andExpect(status().isOk())
                .andExpect(view().name("index"))
                .andExpect(model().attributeHasFieldErrors("blueprintInput",
                        "businessRequirements", "targetDomain", "userTypes", "requiredOperations", "allowedAiOperations"));
    }

    @Test
    void postGenerateWithValidInputReturnsResult() throws Exception {
        BlueprintResult mockResult = new BlueprintResult();
        mockResult.setInputSummary("summary");
        when(generationService.generate(any())).thenReturn(mockResult);

        mockMvc.perform(post("/blueprint/generate")
                        .param("businessRequirements", "顧客検索を行う")
                        .param("targetDomain", "顧客管理")
                        .param("userTypes", "営業担当")
                        .param("requiredOperations", "顧客検索")
                        .param("allowedAiOperations", "顧客検索"))
                .andExpect(status().isOk())
                .andExpect(view().name("result"))
                .andExpect(model().attributeExists("blueprintResult"));

        ArgumentCaptor<BlueprintInput> captor = ArgumentCaptor.forClass(BlueprintInput.class);
        verify(generationService).generate(captor.capture());
        assertThat(captor.getValue().getBusinessRequirements()).isEqualTo("顧客検索を行う");
        assertThat(captor.getValue().getTargetDomain()).isEqualTo("顧客管理");
        assertThat(captor.getValue().getRequiredOperations()).isEqualTo("顧客検索");
    }

    @Test
    void postGenerateWithFirstVisitorSampleInputReturnsResult() throws Exception {
        BlueprintResult mockResult = new BlueprintResult();
        mockResult.setInputSummary("summary");
        when(generationService.generate(any())).thenReturn(mockResult);

        mockMvc.perform(post("/blueprint/generate")
                        .param("businessRequirements", """
                                EC / 販売管理として、注文管理を主ドメインにし、在庫管理と商品管理を関連ドメインにする。
                                注文ステータス更新、注文キャンセル、返金処理、外部通知送信は人間承認後に実行する。
                                """)
                        .param("targetDomain", "注文管理 / 在庫管理 / 商品管理")
                        .param("systemTypes", "sales-commerce")
                        .param("primaryDomain", "注文管理")
                        .param("relatedDomains", "注文管理", "在庫管理", "商品管理")
                        .param("userTypes", "- 管理者\n- AIアシスタント\n- EC運営担当\n- 倉庫担当")
                        .param("requiredOperations", """
                                - 注文検索
                                - 注文詳細取得
                                - 注文ステータス更新
                                - 在庫確認
                                - 商品情報参照
                                - 出荷前チェック
                                - 承認依頼
                                """)
                        .param("allowedAiOperations", """
                                - 注文検索
                                - 注文詳細参照
                                - 在庫確認
                                - 商品情報参照
                                - 出荷前チェック結果の要約
                                - 注文変更案の作成
                                """)
                        .param("approvalRequiredOperations", """
                                - 注文ステータス更新
                                - 注文キャンセル
                                - 返金処理
                                - 外部通知送信
                                """)
                        .param("auditLogRequiredOperations", """
                                - 注文ステータス更新
                                - 注文キャンセル
                                - 返金処理
                                - AIによる変更案作成
                                - 承認依頼
                                """)
                        .param("authenticationMethod", "Spring Security + セッション認証")
                        .param("targetUsers", "EC運営担当、倉庫担当、管理者、AIアシスタント")
                        .param("outputLanguage", "日本語"))
                .andExpect(status().isOk())
                .andExpect(view().name("result"))
                .andExpect(model().attributeExists("blueprintResult"));

        ArgumentCaptor<BlueprintInput> captor = ArgumentCaptor.forClass(BlueprintInput.class);
        verify(generationService).generate(captor.capture());
        BlueprintInput input = captor.getValue();
        assertThat(input.getTargetDomain()).isEqualTo("注文管理 / 在庫管理 / 商品管理");
        assertThat(input.getSystemTypes()).containsExactly("sales-commerce");
        assertThat(input.getPrimaryDomain()).isEqualTo("注文管理");
        assertThat(input.getRelatedDomains()).containsExactly("注文管理", "在庫管理", "商品管理");
        assertThat(input.getRequiredOperations()).contains("注文ステータス更新", "出荷前チェック");
        assertThat(input.getAllowedAiOperations()).contains("注文変更案の作成");
        assertThat(input.getApprovalRequiredOperations()).contains("返金処理", "外部通知送信");
        assertThat(input.getAuditLogRequiredOperations()).contains("AIによる変更案作成");
    }

    @Test
    void postGenerateWithSupportSampleInputReturnsResult() throws Exception {
        BlueprintResult mockResult = new BlueprintResult();
        mockResult.setInputSummary("summary");
        when(generationService.generate(any())).thenReturn(mockResult);

        mockMvc.perform(post("/blueprint/generate")
                        .param("businessRequirements", """
                                問い合わせ・サポート管理として、問い合わせ受付、分類、FAQ検索、AI要約、返信下書き作成を扱う。
                                回答確定送信と重要問い合わせの状態変更は人間確認後に実行する。
                                """)
                        .param("targetDomain", "問い合わせ管理 / FAQ管理 / ナレッジ検索・要約 / 顧客管理 / 通知管理")
                        .param("systemTypes", "support-management", "knowledge-platform")
                        .param("primaryDomain", "問い合わせ管理")
                        .param("relatedDomains", "問い合わせ管理", "FAQ管理", "ナレッジ検索・要約", "顧客管理", "通知管理")
                        .param("userTypes", "- 管理者\n- AIアシスタント\n- サポート担当\n- 品質管理担当")
                        .param("requiredOperations", """
                                - 問い合わせ検索
                                - 問い合わせ詳細取得
                                - FAQ検索
                                - 問い合わせ要約
                                - 返信下書き作成
                                - 回答確定通知
                                """)
                        .param("allowedAiOperations", """
                                - 問い合わせ検索
                                - 問い合わせ詳細参照
                                - FAQ検索
                                - 問い合わせ要約
                                - 返信下書き作成
                                """)
                        .param("approvalRequiredOperations", """
                                - 回答確定送信
                                - 重要問い合わせの状態変更
                                - 顧客情報更新
                                """)
                        .param("auditLogRequiredOperations", """
                                - AIによる問い合わせ要約
                                - 返信下書き作成
                                - 回答確定送信
                                """)
                        .param("authenticationMethod", "OAuth2 / OIDC")
                        .param("targetUsers", "サポート担当、品質管理担当、管理者、AIアシスタント")
                        .param("outputLanguage", "日本語"))
                .andExpect(status().isOk())
                .andExpect(view().name("result"))
                .andExpect(model().attributeExists("blueprintResult"));

        ArgumentCaptor<BlueprintInput> captor = ArgumentCaptor.forClass(BlueprintInput.class);
        verify(generationService).generate(captor.capture());
        BlueprintInput input = captor.getValue();
        assertThat(input.getSystemTypes()).containsExactly("support-management", "knowledge-platform");
        assertThat(input.getPrimaryDomain()).isEqualTo("問い合わせ管理");
        assertThat(input.getRelatedDomains()).containsExactly("問い合わせ管理", "FAQ管理", "ナレッジ検索・要約", "顧客管理", "通知管理");
        assertThat(input.getRequiredOperations()).contains("FAQ検索", "返信下書き作成");
        assertThat(input.getAllowedAiOperations()).contains("問い合わせ要約");
        assertThat(input.getApprovalRequiredOperations()).contains("回答確定送信");
        assertThat(input.getAuditLogRequiredOperations()).contains("AIによる問い合わせ要約");
    }

    @Test
    void postGenerateResultExplainsHowToUseOutputsAndMvpExclusions() throws Exception {
        BlueprintResult mockResult = new BlueprintResult();
        mockResult.setInputSummary("summary");
        when(generationService.generate(any())).thenReturn(mockResult);

        mockMvc.perform(post("/blueprint/generate")
                        .param("businessRequirements", "顧客検索を行う")
                        .param("targetDomain", "顧客管理")
                        .param("userTypes", "営業担当")
                        .param("requiredOperations", "顧客検索")
                        .param("allowedAiOperations", "顧客検索"))
                .andExpect(status().isOk())
                .andExpect(view().name("result"))
                .andExpect(content().string(containsString("API設計書は、人間による設計レビューやdocs転記に使います")))
                .andExpect(content().string(containsString("MCP tools/resources/promptsは、AIエージェントへ公開する操作・参照範囲・定型指示の設計候補")))
                .andExpect(content().string(containsString("API/MCP対応表は、人間向けAPIとAI向け操作入口の対応を確認するための表")))
                .andExpect(content().string(containsString("AI実装指示書は、APIM for Springを実装するためではなく")))
                .andExpect(content().string(containsString("生成された設計候補をCodex等の実装支援AIへ渡し")))
                .andExpect(content().string(containsString("対象業務アプリケーションの実装につなげるための成果物")))
                .andExpect(content().string(containsString("おすすめの読み順")))
                .andExpect(content().string(containsString("まず入力サマリーで、どの業務要件から生成されたかを確認します")))
                .andExpect(content().string(containsString("RESTエンドポイント一覧で、人間向けAPI候補を確認します")))
                .andExpect(content().string(containsString("MCP tools/resources/promptsで、AIエージェントに公開する操作候補を確認します")))
                .andExpect(content().string(containsString("Markdown設計書やAI実装指示書を、レビューやCodex等への実装依頼に使います")))
                .andExpect(content().string(containsString("完全動作するMCPサーバー")))
                .andExpect(content().string(containsString("DB永続化やマイグレーション")))
                .andExpect(content().string(containsString("step4-result-page")))
                .andExpect(content().string(containsString("step4-result-hero")))
                .andExpect(content().string(containsString("Step 4: 設計候補生成結果")))
                .andExpect(content().string(containsString("API / MCP設計候補を生成しました")))
                .andExpect(content().string(containsString("step4-progress-stepper")))
                .andExpect(content().string(containsString("step4-status-card")))
                .andExpect(content().string(containsString("生成成果物の全体像")))
                .andExpect(content().string(containsString("step4-overview-card")))
                .andExpect(content().string(containsString("step4-detail-card")));
    }

    @Test
    void postGenerateBindsMultiSystemAndDomainFields() throws Exception {
        BlueprintResult mockResult = new BlueprintResult();
        mockResult.setInputSummary("summary");
        when(generationService.generate(any())).thenReturn(mockResult);

        mockMvc.perform(post("/blueprint/generate")
                        .param("businessRequirements", "顧客検索を行う")
                        .param("targetDomain", "顧客管理 / 問い合わせ管理")
                        .param("systemTypes", "customer-crm", "support-management")
                        .param("primaryDomain", "顧客管理")
                        .param("relatedDomains", "顧客管理", "問い合わせ管理")
                        .param("userTypes", "営業担当")
                        .param("requiredOperations", "顧客検索")
                        .param("allowedAiOperations", "顧客検索"))
                .andExpect(status().isOk())
                .andExpect(view().name("result"))
                .andExpect(model().attributeExists("blueprintResult"));

        ArgumentCaptor<BlueprintInput> captor = ArgumentCaptor.forClass(BlueprintInput.class);
        verify(generationService).generate(captor.capture());
        assertThat(captor.getValue().getTargetDomain()).isEqualTo("顧客管理 / 問い合わせ管理");
        assertThat(captor.getValue().getPrimaryDomain()).isEqualTo("顧客管理");
        assertThat(captor.getValue().getSystemTypes()).containsExactly("customer-crm", "support-management");
        assertThat(captor.getValue().getRelatedDomains()).containsExactly("顧客管理", "問い合わせ管理");
    }

    @Test
    void previewRedirectsWhenNoResultIsStored() throws Exception {
        mockMvc.perform(get("/blueprint/preview"))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/"));
    }

    @Test
    void previewShowsDownloadAndReturnToStoredInputLinks() throws Exception {
        BlueprintResult mockResult = new BlueprintResult();
        mockResult.setBlueprintMarkdown("# API MCP Blueprint");

        mockMvc.perform(get("/blueprint/preview")
                        .sessionAttr("blueprintResult", mockResult))
                .andExpect(status().isOk())
                .andExpect(view().name("blueprint-preview"))
                .andExpect(content().string(containsString("Markdown設計書ダウンロード")))
                .andExpect(content().string(containsString("/blueprint/download")))
                .andExpect(content().string(containsString("設定を修正して再生成")))
                .andExpect(content().string(containsString("/blueprint/edit")));
    }

    @Test
    void editFormKeepsManualInputAfterPreviewFlow() throws Exception {
        BlueprintResult mockResult = new BlueprintResult();
        mockResult.setInputSummary("summary");
        mockResult.setBlueprintMarkdown("# API MCP Blueprint");
        when(generationService.generate(any())).thenReturn(mockResult);

        MockHttpSession session = new MockHttpSession();
        mockMvc.perform(post("/blueprint/generate")
                        .session(session)
                        .param("businessRequirements", "顧客検索と問い合わせ回答を行う")
                        .param("targetDomain", "顧客管理 / 問い合わせ管理")
                        .param("systemTypes", "customer-crm")
                        .param("primaryDomain", "顧客管理")
                        .param("relatedDomains", "顧客管理", "問い合わせ管理")
                        .param("userTypes", "- 営業担当\n- AIアシスタント")
                        .param("requiredOperations", "- 顧客検索\n- 問い合わせ詳細取得")
                        .param("allowedAiOperations", "- 顧客検索\n- 問い合わせ要約")
                        .param("approvalRequiredOperations", "- 更新")
                        .param("auditLogRequiredOperations", "- 更新")
                        .param("authenticationMethod", "OAuth2 / OIDC")
                        .param("targetUsers", "営業担当、AIアシスタント")
                        .param("outputLanguage", "English"))
                .andExpect(status().isOk())
                .andExpect(view().name("result"));

        mockMvc.perform(get("/blueprint/preview").session(session))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("設定を修正して再生成")));

        mockMvc.perform(get("/blueprint/edit").session(session))
                .andExpect(status().isOk())
                .andExpect(view().name("index"))
                .andExpect(content().string(containsString("顧客検索と問い合わせ回答を行う")))
                .andExpect(content().string(containsString("顧客管理 / 問い合わせ管理")))
                .andExpect(content().string(containsString("customer-crm")))
                .andExpect(content().string(containsString("問い合わせ管理")))
                .andExpect(content().string(containsString("顧客検索")))
                .andExpect(content().string(containsString("問い合わせ要約")))
                .andExpect(content().string(containsString("English")));
    }

    @Test
    void editFormPreservesFreeFormOperationsOnResubmission() throws Exception {
        BlueprintResult firstResult = new BlueprintResult();
        firstResult.setInputSummary("summary");
        firstResult.setBlueprintMarkdown("# API MCP Blueprint");
        when(generationService.generate(any())).thenReturn(firstResult);

        MockHttpSession session = new MockHttpSession();
        mockMvc.perform(post("/blueprint/generate")
                        .session(session)
                        .param("businessRequirements", "顧客検索と問い合わせ回答を行う")
                        .param("targetDomain", "顧客管理 / 問い合わせ管理")
                        .param("userTypes", "- 営業担当\n- AIアシスタント")
                        .param("requiredOperations", "- 検索\n- 顧客検索\n- 問い合わせ詳細取得")
                        .param("allowedAiOperations", "- 詳細参照\n- 問い合わせ詳細取得\n- 問い合わせ要約"))
                .andExpect(status().isOk())
                .andExpect(view().name("result"));

        mockMvc.perform(get("/blueprint/edit").session(session))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("function syncOperationTextareaPreservingFreeForm(target)")))
                .andExpect(content().string(containsString("syncOperationTextareaPreservingFreeForm('requiredOperations')")))
                .andExpect(content().string(containsString("syncOperationTextareaPreservingFreeForm('allowedAiOperations')")))
                .andExpect(content().string(containsString("顧客検索")))
                .andExpect(content().string(containsString("問い合わせ詳細取得")))
                .andExpect(content().string(containsString("問い合わせ要約")));

        mockMvc.perform(post("/blueprint/generate")
                        .session(session)
                        .param("businessRequirements", "顧客検索と問い合わせ回答を行う")
                        .param("targetDomain", "顧客管理 / 問い合わせ管理")
                        .param("userTypes", "- 営業担当\n- AIアシスタント")
                        .param("requiredOperations", "- 検索\n- 顧客検索\n- 問い合わせ詳細取得")
                        .param("allowedAiOperations", "- 詳細参照\n- 問い合わせ詳細取得\n- 問い合わせ要約"))
                .andExpect(status().isOk())
                .andExpect(view().name("result"));

        ArgumentCaptor<BlueprintInput> captor = ArgumentCaptor.forClass(BlueprintInput.class);
        verify(generationService, org.mockito.Mockito.times(2)).generate(captor.capture());
        BlueprintInput resubmittedInput = captor.getAllValues().get(1);
        assertThat(resubmittedInput.getRequiredOperations())
                .contains("顧客検索")
                .contains("問い合わせ詳細取得");
        assertThat(resubmittedInput.getAllowedAiOperations())
                .contains("問い合わせ詳細取得")
                .contains("問い合わせ要約");
    }

    @Test
    void implementationPreviewRedirectsWhenNoResultIsStored() throws Exception {
        mockMvc.perform(get("/blueprint/implementation-instructions"))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/"));
    }

    @Test
    void blueprintDownloadReturnsMarkdownAttachmentWhenResultIsStored() throws Exception {
        BlueprintResult mockResult = new BlueprintResult();
        mockResult.setBlueprintMarkdown("# API MCP Blueprint");

        mockMvc.perform(get("/blueprint/download")
                        .sessionAttr("blueprintResult", mockResult))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION,
                        containsString("api-mcp-blueprint.md")))
                .andExpect(content().string("# API MCP Blueprint"));
    }

    @Test
    void blueprintDownloadReturns404WhenNoResultIsStored() throws Exception {
        mockMvc.perform(get("/blueprint/download"))
                .andExpect(status().isNotFound());
    }

    @Test
    void implementationInstructionsDownloadReturnsMarkdownAttachmentWhenResultIsStored() throws Exception {
        BlueprintResult mockResult = new BlueprintResult();
        mockResult.setImplementationInstructions("# Implementation Instructions");

        mockMvc.perform(get("/blueprint/implementation-instructions/download")
                        .sessionAttr("blueprintResult", mockResult))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION,
                        containsString("implementation-instructions.md")))
                .andExpect(content().string("# Implementation Instructions"));
    }

    @Test
    void implementationInstructionsDownloadReturns404WhenNoResultIsStored() throws Exception {
        mockMvc.perform(get("/blueprint/implementation-instructions/download"))
                .andExpect(status().isNotFound());
    }
    @Test
    void editFormRestoresCheckedItemsFromGeneratedBlueprintInput() throws Exception {
        BlueprintInput input = new BlueprintInput();
        input.setTargetDomain("注文管理");
        input.setPrimaryDomain("注文管理");
        input.setRelatedDomains(List.of("在庫管理"));
        input.setSystemTypes(List.of("sales-commerce"));
        input.setUserTypes("管理者、AIアシスタント");
        input.setRequiredOperations("検索、更新、通知");
        input.setAllowedAiOperations("検索、要約");
        input.setBusinessRequirements("AIは外部送信を直接実行しない");
        input.setReadOnlyOperations("検索、要約");
        input.setWriteOperations("更新、通知");
        input.setApprovalRequiredOperations("更新、外部送信");
        input.setAuditLogRequiredOperations("更新、通知");

        MockHttpSession session = new MockHttpSession();
        session.setAttribute("blueprintInput", input);

        mockMvc.perform(get("/blueprint/edit").session(session))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("restoreCheckedItemsFromBlueprintInput")))
                .andExpect(content().string(containsString("restoreCheckboxGroupFromCurrentValues")))
                .andExpect(content().string(containsString("systemTypePresets")))
                .andExpect(content().string(containsString("domainCatalog")))
                .andExpect(content().string(containsString("approvalRequiredOperations")))
                .andExpect(content().string(containsString("sales-commerce")))
                .andExpect(content().string(containsString("在庫管理")))
                .andExpect(content().string(containsString("AIは外部送信を直接実行しない")));
    }

    @Test
    void editFormRestoresGeneratedDetailedOperationsToChecklistCandidates() throws Exception {
        BlueprintInput input = new BlueprintInput();
        input.setTargetDomain("注文管理 / 在庫管理 / 商品管理");
        input.setPrimaryDomain("注文管理");
        input.setRelatedDomains(List.of("在庫管理", "商品管理"));
        input.setSystemTypes(List.of("sales-commerce"));
        input.setUserTypes("- 管理者\n- AIアシスタント");
        input.setRequiredOperations("- 注文検索\n- 注文詳細取得\n- 注文ステータス更新\n- 承認依頼");
        input.setAllowedAiOperations("- 注文検索\n- 注文詳細参照\n- 出荷前チェック結果の要約\n- 注文変更案の作成");
        input.setBusinessRequirements("AIは外部送信を直接実行しない");
        input.setReadOnlyOperations("- 注文検索\n- 注文詳細取得\n- 要約");
        input.setWriteOperations("- 注文ステータス更新\n- 出荷通知送信");
        input.setApprovalRequiredOperations("- 注文ステータス更新\n- 注文キャンセル\n- 返金処理\n- 外部通知送信");
        input.setAuditLogRequiredOperations("- 注文ステータス更新\n- AIによる変更案作成\n- 承認依頼");

        MockHttpSession session = new MockHttpSession();
        session.setAttribute("blueprintInput", input);

        mockMvc.perform(get("/blueprint/edit").session(session))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("normalizeRestoreText")))
                .andExpect(content().string(containsString("isRestoredCheckboxValueMatch")))
                .andExpect(content().string(containsString("normalizedRestored.includes('外部')")))
                .andExpect(content().string(containsString("normalizedCandidate === '詳細参照'")))
                .andExpect(content().string(containsString("注文ステータス更新")))
                .andExpect(content().string(containsString("外部通知送信")))
                .andExpect(content().string(containsString("restoreCheckedItemsFromBlueprintInput();")));
    }

}

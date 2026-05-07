package com.example.apim.controller;

import com.example.apim.model.BlueprintInput;
import com.example.apim.model.BlueprintResult;
import com.example.apim.service.BlueprintGenerationService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
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
    void getRootReturns200() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"));
    }

    @Test
    void getRootShowsFirstVisitorGuideAndSampleRoute() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("業務要件からAPI設計とMCP設計候補を同時に作る設計支援ツール")))
                .andExpect(content().string(containsString("まだコードを書く前に、必要なAPI、AIエージェント向け操作、承認・監査観点")))
                .andExpect(content().string(containsString("どのような入力からAPI設計書、MCP設計候補、API/MCP対応表、AI実装指示書が得られるか")))
                .andExpect(content().string(containsString("はじめて試す場合の3ステップ")))
                .andExpect(content().string(containsString("注文管理 / 在庫管理 / 商品管理を題材にしたサンプル業務要件をフォームへ自動入力")))
                .andExpect(content().string(containsString("サンプル投入だけでは生成は実行されません")))
                .andExpect(content().string(containsString("設計候補を生成する」を押すと生成結果へ進みます")))
                .andExpect(content().string(containsString("開発前に、どのAPIが必要かを整理できます")))
                .andExpect(content().string(containsString("AIアシスタントに許可する操作と禁止する操作を分けて考えられます")))
                .andExpect(content().string(containsString("生成されたAI実装指示書をCodex等へ渡し、実装着手の材料にできます")))
                .andExpect(content().string(containsString("注文管理サンプルを挿入")))
                .andExpect(content().string(containsString("自由入力へ戻す")))
                .andExpect(content().string(containsString("完全動作するMCPサーバー")))
                .andExpect(content().string(containsString("DB永続化やマイグレーション")));
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
                .andExpect(content().string(containsString("AI実装指示書は、Codex等の実装支援AIへ渡す入力")))
                .andExpect(content().string(containsString("おすすめの読み順")))
                .andExpect(content().string(containsString("まず入力サマリーで、どの業務要件から生成されたかを確認します")))
                .andExpect(content().string(containsString("RESTエンドポイント一覧で、人間向けAPI候補を確認します")))
                .andExpect(content().string(containsString("MCP tools/resources/promptsで、AIエージェントに公開する操作候補を確認します")))
                .andExpect(content().string(containsString("Markdown設計書やAI実装指示書を、レビューやCodex等への実装依頼に使います")))
                .andExpect(content().string(containsString("完全動作するMCPサーバー")))
                .andExpect(content().string(containsString("DB永続化やマイグレーション")));
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
}

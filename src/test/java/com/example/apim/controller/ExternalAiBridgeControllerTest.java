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

import java.util.List;

import static org.hamcrest.Matchers.containsString;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest(ExternalAiBridgeController.class)
class ExternalAiBridgeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ExternalAiPromptBridgeService bridgeService;

    @Test
    void bridgePageShowsManualExternalAiFlow() throws Exception {
        mockMvc.perform(get("/external-ai-bridge"))
                .andExpect(status().isOk())
                .andExpect(view().name("external-ai-bridge"))
                .andExpect(content().string(containsString("API + MCP Blueprint Compiler for Spring")))
                .andExpect(content().string(containsString("業務要件から、Spring REST API設計とMCP tools/resources/prompts設計候補を同時生成するWeb MVP")))
                .andExpect(content().string(containsString("業務要件からAPI設計とMCP設計候補を同時に作る設計支援ツール")))
                .andExpect(content().string(containsString("API設計書 / REST API設計候補")))
                .andExpect(content().string(containsString("MCP tools / resources / prompts 候補")))
                .andExpect(content().string(containsString("完全動作するMCPサーバー")))
                .andExpect(content().string(containsString("外部LLM API連携")))
                .andExpect(content().string(containsString("自由文から設計候補を作る3ステップ")))
                .andExpect(content().string(containsString("生成されたプロンプトをChatGPT等の外部AIへ手動投入")))
                .andExpect(content().string(containsString("貼り付けとアップロードはどちらか一方で構いません")))
                .andExpect(content().string(containsString("外部AI公式リンク集")))
                .andExpect(content().string(containsString("当サイトは各サービス提供元と提携・公認関係にあるものではありません")))
                .andExpect(content().string(containsString("ChatGPT公式サイトを別タブで開く")))
                .andExpect(content().string(containsString("Claude公式サイトを別タブで開く")))
                .andExpect(content().string(containsString("Gemini公式サイトを別タブで開く")))
                .andExpect(content().string(containsString("Microsoft Copilot公式サイトを別タブで開く")))
                .andExpect(content().string(containsString("target=\"_blank\"")))
                .andExpect(content().string(containsString("rel=\"noopener noreferrer\"")))
                .andExpect(content().string(containsString("APIM側から左右分割表示を強制することはできません")))
                .andExpect(content().string(containsString("外部AI API連携ではありません")))
                .andExpect(content().string(containsString("解析対象データ")))
                .andExpect(content().string(containsString("APIM取り込み用JSON")))
                .andExpect(content().string(containsString("チェック式入力へ進む")))
                .andExpect(content().string(containsString("value=\"file\" checked")))
                .andExpect(content().string(containsString("onchange=\"switchJsonInputMode(\'file\')\"")))
                .andExpect(content().string(containsString("value=\"paste\" onchange")))
                .andExpect(content().string(containsString("onchange=\"switchJsonInputMode(\'paste\')\"")))
                .andExpect(content().string(containsString("DOMContentLoaded")))
                .andExpect(content().string(containsString("aria-hidden")))
                .andExpect(content().string(containsString("id=\"jsonFilePanel\" class=\"json-input-panel\"")))
                .andExpect(content().string(containsString("id=\"jsonPastePanel\" class=\"json-input-panel\" hidden")))
                .andExpect(content().string(containsString("ファイルアップロードで取り込む")))
                .andExpect(content().string(containsString("貼り付けで取り込む")))
                .andExpect(content().string(containsString("両方を順番に行う必要はありません")))
                .andExpect(content().string(containsString("代表的なサンプル例文を入力できます")))
                .andExpect(content().string(containsString("注文・在庫管理")))
                .andExpect(content().string(containsString("社内申請・承認")))
                .andExpect(content().string(containsString("問い合わせ管理")))
                .andExpect(content().string(containsString("契約・請求管理")))
                .andExpect(content().string(containsString("予約・施設管理")))
                .andExpect(content().string(containsString("人事オンボーディング")))
                .andExpect(content().string(containsString("保守・障害対応")))
                .andExpect(content().string(containsString("営業案件管理")))
                .andExpect(content().string(containsString("ナレッジ検索")))
                .andExpect(content().string(containsString("購買・稟議管理")))
                .andExpect(content().string(containsString("insertFreeTextSample")))
                .andExpect(content().string(containsString("data-sample")))
                .andExpect(content().string(containsString("64KB以下")));
    }

    @Test
    void promptGenerationDisplaysGeneratedPrompt() throws Exception {
        when(bridgeService.generatePrompt(anyString())).thenReturn("generated markdown prompt");

        mockMvc.perform(post("/external-ai-bridge/prompt")
                        .param("freeText", "顧客検索を行いたい"))
                .andExpect(status().isOk())
                .andExpect(view().name("external-ai-prompt"))
                .andExpect(model().attribute("externalAiPrompt", "generated markdown prompt"))
                .andExpect(content().string(containsString("Markdownダウンロード")))
                .andExpect(content().string(containsString("generated markdown prompt")));

        verify(bridgeService).generatePrompt("顧客検索を行いたい");
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
    void importTextWithValidJsonShowsConfirmationForm() throws Exception {
        BlueprintInput input = new BlueprintInput();
        input.setBusinessRequirements("営業担当が顧客情報を検索する。");
        input.setTargetDomain("顧客管理");
        input.setPrimaryDomain("顧客管理");
        input.setRelatedDomains(List.of("顧客管理"));
        input.setUserTypes("- 営業担当\n- AIアシスタント");
        input.setRequiredOperations("- 顧客検索");
        input.setAllowedAiOperations("- 顧客検索");
        when(bridgeService.importJson("{valid}"))
                .thenReturn(ExternalAiImportResult.canGenerate(input, "ok", List.of()));

        mockMvc.perform(post("/external-ai-bridge/import-text")
                        .param("jsonText", "{valid}"))
                .andExpect(status().isOk())
                .andExpect(view().name("external-ai-import-result"))
                .andExpect(model().attributeExists("blueprintInput"))
                .andExpect(content().string(containsString("反映内容の確認・修正")))
                .andExpect(content().string(containsString("営業担当が顧客情報を検索する。")))
                .andExpect(content().string(containsString("確認した内容で設計候補を生成する")));
    }

    @Test
    void importTextWithCanGenerateFalseShowsReason() throws Exception {
        when(bridgeService.importJson("{cannot}")).thenReturn(ExternalAiImportResult.cannotGenerate(
                "対象ドメインが不足しています。", List.of("対象ドメイン"), List.of()));

        mockMvc.perform(post("/external-ai-bridge/import-text")
                        .param("jsonText", "{cannot}"))
                .andExpect(status().isOk())
                .andExpect(view().name("external-ai-import-result"))
                .andExpect(content().string(containsString("生成不可")))
                .andExpect(content().string(containsString("対象ドメインが不足しています。")))
                .andExpect(content().string(containsString("対象ドメイン")));
    }

    @Test
    void importTextWithInvalidJsonShowsErrorEscaped() throws Exception {
        when(bridgeService.importJson("<script>alert(1)</script>"))
                .thenReturn(ExternalAiImportResult.invalid(List.of("<script>alert(1)</script>"), List.of()));

        mockMvc.perform(post("/external-ai-bridge/import-text")
                        .param("jsonText", "<script>alert(1)</script>"))
                .andExpect(status().isOk())
                .andExpect(view().name("external-ai-import-result"))
                .andExpect(content().string(containsString("&lt;script&gt;alert(1)&lt;/script&gt;")));
    }

    @Test
    void importFileRejectsNonJsonExtension() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "jsonFile", "apim-blueprint-input.txt", "text/plain", "{}".getBytes());

        mockMvc.perform(multipart("/external-ai-bridge/import-file").file(file))
                .andExpect(status().isOk())
                .andExpect(view().name("external-ai-import-result"))
                .andExpect(content().string(containsString(".json ファイルのみアップロードできます。")));
    }

    @Test
    void importFileAcceptsJsonExtension() throws Exception {
        when(bridgeService.importJson("{\"schemaVersion\":\"apim-blueprint-input/v1\"}"))
                .thenReturn(ExternalAiImportResult.invalid(List.of("judgement が存在しません。"), List.of()));
        MockMultipartFile file = new MockMultipartFile(
                "jsonFile",
                "apim-blueprint-input.json",
                "application/json",
                "{\"schemaVersion\":\"apim-blueprint-input/v1\"}".getBytes());

        mockMvc.perform(multipart("/external-ai-bridge/import-file").file(file))
                .andExpect(status().isOk())
                .andExpect(view().name("external-ai-import-result"))
                .andExpect(content().string(containsString("judgement が存在しません。")));
    }
}

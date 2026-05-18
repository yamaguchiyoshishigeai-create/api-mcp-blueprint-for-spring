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
                .andExpect(content().string(containsString("外部AI API連携ではありません")))
                .andExpect(content().string(containsString("解析対象データ")))
                .andExpect(content().string(containsString("APIM取り込み用JSON")))
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

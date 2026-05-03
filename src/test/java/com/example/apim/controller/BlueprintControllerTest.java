package com.example.apim.controller;

import com.example.apim.model.BlueprintResult;
import com.example.apim.service.BlueprintGenerationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
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

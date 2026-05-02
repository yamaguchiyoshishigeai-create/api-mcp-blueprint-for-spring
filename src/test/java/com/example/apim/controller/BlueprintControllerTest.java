package com.example.apim.controller;

import com.example.apim.model.BlueprintResult;
import com.example.apim.service.BlueprintGenerationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
}

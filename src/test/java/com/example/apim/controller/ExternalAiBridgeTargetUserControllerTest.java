package com.example.apim.controller;

import com.example.apim.service.ExternalAiPromptBridgeService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest(ExternalAiBridgeController.class)
class ExternalAiBridgeTargetUserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ExternalAiPromptBridgeService bridgeService;

    @Test
    void bridgePageShowsTargetUserCopyNearHero() throws Exception {
        mockMvc.perform(get("/external-ai-bridge"))
                .andExpect(status().isOk())
                .andExpect(view().name("external-ai-bridge"))
                .andExpect(content().string(containsString("target-user-strip")))
                .andExpect(content().string(containsString("対象利用者")))
                .andExpect(content().string(containsString("Spring BootでAPI設計に悩む初学者・若手エンジニア")))
                .andExpect(content().string(containsString("業務要件からAPI&amp;MCP設計のたたき台を作りたい利用者")));
    }
}

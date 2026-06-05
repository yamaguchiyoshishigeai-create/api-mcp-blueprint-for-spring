package com.example.apim.controller;

import com.example.apim.model.BlueprintResult;
import com.example.apim.service.BlueprintGenerationService;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest(BlueprintController.class)
class BlueprintResultSummaryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BlueprintGenerationService generationService;

    @Test
    void resultPageShowsPostHeroSummaryBeforeDetailedResults() throws Exception {
        BlueprintResult result = new BlueprintResult();
        result.setInputSummary("summary");

        MvcResult page = mockMvc.perform(get("/blueprint/result")
                        .sessionAttr("blueprintResult", result))
                .andExpect(status().isOk())
                .andExpect(view().name("result"))
                .andExpect(content().string(containsString("/css/result-summary.css")))
                .andExpect(content().string(containsString("tsk073-result-summary")))
                .andExpect(content().string(containsString("結果サマリー")))
                .andExpect(content().string(containsString("生成成果物の確認ポイント")))
                .andExpect(content().string(containsString("API設計")))
                .andExpect(content().string(containsString("MCP設計")))
                .andExpect(content().string(containsString("セキュリティ・監査")))
                .andExpect(content().string(containsString("実装引き継ぎ")))
                .andExpect(content().string(containsString("API&amp;MCP設計書を確認")))
                .andExpect(content().string(containsString("AI実装指示書を確認")))
                .andExpect(content().string(containsString("生成結果詳細を見る")))
                .andReturn();

        String html = page.getResponse().getContentAsString(StandardCharsets.UTF_8);
        assertAppearsInOrder(
                html,
                "step4-result-hero",
                "tsk073-result-summary",
                "step4-progress-stepper",
                "step4-main-grid");
    }

    @Test
    void resultSummaryCssIsServedAsExternalStaticAsset() throws Exception {
        mockMvc.perform(get("/css/result-summary.css"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("/* BEGIN TSK-073_RESULT_SUMMARY */")))
                .andExpect(content().string(containsString(".tsk073-result-summary")))
                .andExpect(content().string(containsString(".tsk073-result-summary__grid")))
                .andExpect(content().string(containsString("@media (max-width: 640px)")));
    }

    private static void assertAppearsInOrder(String html, String... markers) {
        int previousIndex = -1;
        for (String marker : markers) {
            int currentIndex = html.indexOf(marker);
            assertThat(currentIndex).as("Expected to find marker: " + marker).isGreaterThanOrEqualTo(0);
            assertThat(currentIndex).as("Expected marker to appear later: " + marker).isGreaterThan(previousIndex);
            previousIndex = currentIndex;
        }
    }
}

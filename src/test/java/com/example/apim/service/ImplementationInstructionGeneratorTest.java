package com.example.apim.service;

import com.example.apim.model.BlueprintInput;
import com.example.apim.model.BlueprintResult;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ImplementationInstructionGeneratorTest {

    @Test
    void includesOutOfScopeMcpServerStatement() {
        ImplementationInstructionGenerator generator = new ImplementationInstructionGenerator();
        BlueprintInput input = new BlueprintInput();
        input.setTargetDomain("顧客管理");
        input.setRequiredOperations("顧客検索");
        BlueprintResult result = new BlueprintResult();

        String markdown = generator.generate(input, result);

        assertThat(markdown).contains("完全動作するMCPサーバーは実装しない");
    }
}

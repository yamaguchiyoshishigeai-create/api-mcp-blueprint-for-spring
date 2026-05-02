package com.example.apim.service;

import com.example.apim.model.BlueprintInput;
import com.example.apim.support.OperationType;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class SecurityNotesGeneratorTest {

    @Test
    void includesDeleteSafetyNotes() {
        SecurityNotesGenerator generator = new SecurityNotesGenerator();
        BlueprintInput input = new BlueprintInput();
        input.setBusinessRequirements("顧客削除を実施する");
        input.setRequiredOperations("削除");

        var notes = generator.generate(input, Set.of(OperationType.DELETE));

        assertThat(notes).anyMatch(n -> n.message().contains("削除操作"));
        assertThat(notes).anyMatch(n -> n.message().contains("監査ログ"));
    }
}

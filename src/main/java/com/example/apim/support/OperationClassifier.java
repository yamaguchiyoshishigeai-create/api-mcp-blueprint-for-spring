package com.example.apim.support;

import com.example.apim.model.BlueprintInput;
import org.springframework.stereotype.Component;

import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

@Component
public class OperationClassifier {

    private static final Map<OperationType, String[]> KEYWORDS = Map.of(
            OperationType.SEARCH, new String[]{"検索", "一覧", "探す"},
            OperationType.READ, new String[]{"取得", "参照", "詳細", "確認"},
            OperationType.CREATE, new String[]{"登録", "作成", "追加"},
            OperationType.UPDATE, new String[]{"更新", "変更", "編集"},
            OperationType.DELETE, new String[]{"削除", "廃止", "取消"},
            OperationType.APPROVAL, new String[]{"承認", "却下", "申請"},
            OperationType.SUMMARY, new String[]{"要約", "整理"},
            OperationType.NOTIFICATION, new String[]{"通知", "送信", "外部送信", "外部公開", "公開"},
            OperationType.PERMISSION, new String[]{"権限", "ロール", "許可", "権限変更"}
    );

    public Set<OperationType> classify(BlueprintInput input) {
        Set<OperationType> types = new LinkedHashSet<>();
        detect(types, input.getBusinessRequirements());
        detect(types, input.getRequiredOperations());
        detect(types, input.getAllowedAiOperations());
        detect(types, input.getWriteOperations());
        detect(types, input.getApprovalRequiredOperations());
        detect(types, input.getAuditLogRequiredOperations());
        return types;
    }

    private void detect(Set<OperationType> result, String text) {
        if (text == null || text.isBlank()) {
            return;
        }
        String lower = text.toLowerCase(Locale.ROOT);
        for (Map.Entry<OperationType, String[]> entry : KEYWORDS.entrySet()) {
            for (String keyword : entry.getValue()) {
                if (lower.contains(keyword.toLowerCase(Locale.ROOT))) {
                    result.add(entry.getKey());
                    break;
                }
            }
        }
    }
}

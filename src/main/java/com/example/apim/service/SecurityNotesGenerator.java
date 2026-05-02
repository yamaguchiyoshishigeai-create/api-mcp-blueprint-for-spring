package com.example.apim.service;

import com.example.apim.model.BlueprintInput;
import com.example.apim.model.SecurityNote;
import com.example.apim.support.OperationType;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
public class SecurityNotesGenerator {

    public List<SecurityNote> generate(BlueprintInput input, Set<OperationType> operations) {
        List<SecurityNote> notes = new ArrayList<>();
        notes.add(new SecurityNote("認証", "認証方式を検討すること。"));
        notes.add(new SecurityNote("認可", "ロール別認可を設計すること。"));
        notes.add(new SecurityNote("承認", "書き込み系操作には承認要否を設定すること。"));
        notes.add(new SecurityNote("監査ログ", "AI操作は人間操作と区別して監査ログに残すこと。"));
        notes.add(new SecurityNote("禁止操作", "削除・外部送信・権限変更はAIに直接許可しないこと。"));
        notes.add(new SecurityNote("情報保護", "個人情報・機密情報の出力範囲を最小化すること。"));

        if (operations.contains(OperationType.DELETE)) {
            notes.add(new SecurityNote("削除操作", "削除操作は承認必須とし、実行ログを必須化する。"));
        }
        if (operations.contains(OperationType.UPDATE)) {
            notes.add(new SecurityNote("更新操作", "更新操作は提案と実行を分離し、人間確認を挟む。"));
        }
        if (operations.contains(OperationType.PERMISSION)) {
            notes.add(new SecurityNote("権限変更", "権限変更操作はAI実行不可とし、管理者承認必須とする。"));
        }
        if (operations.contains(OperationType.NOTIFICATION)) {
            notes.add(new SecurityNote("外部送信", "外部送信・外部公開は承認必須とし、AIの直接実行を禁止する。"));
        }

        String br = lower(input.getBusinessRequirements()) + " " + lower(input.getRequiredOperations());
        if (containsAny(br, "個人情報", "機密", "顧客", "契約", "金銭")) {
            notes.add(new SecurityNote("機微情報", "機微情報の取り扱いは表示マスキングとアクセス制御を前提にする。"));
        }
        return notes;
    }

    private String lower(String value) {
        return value == null ? "" : value.toLowerCase(Locale.ROOT);
    }

    private boolean containsAny(String text, String... terms) {
        for (String term : terms) {
            if (text.contains(term)) {
                return true;
            }
        }
        return false;
    }
}

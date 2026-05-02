package com.example.apim.testsupport;

import com.example.apim.model.BlueprintInput;

public final class BlueprintInputFixtures {

    private BlueprintInputFixtures() {
    }

    public static BlueprintInput equipmentLoanManagement() {
        BlueprintInput input = baseInput("備品貸出管理");
        input.setBusinessRequirements("""
                備品を検索し、詳細参照、登録、更新、備品廃棄を扱う。
                管理者権限変更と外部業者への修理依頼送信は承認必須にしたい。
                予約や修理依頼の対応履歴をAIで要約する。
                """);
        input.setRequiredOperations("""
                - 備品検索
                - 備品詳細取得
                - 備品登録
                - 備品更新
                - 備品廃棄
                - 備品削除
                - 管理者権限変更
                - 外部業者への修理依頼送信
                - 予約要約
                """);
        input.setAllowedAiOperations("""
                - 備品検索
                - 備品詳細参照
                - 備品更新案の作成
                - 予約要約
                """);
        input.setApprovalRequiredOperations("""
                - 備品廃棄
                - 備品削除
                - 管理者権限変更
                - 外部業者への修理依頼送信
                """);
        input.setAuditLogRequiredOperations("""
                - 備品廃棄
                - 管理者権限変更
                - 外部業者への修理依頼送信
                """);
        return input;
    }

    public static BlueprintInput internalApplicationWorkflow() {
        BlueprintInput input = baseInput("社内申請ワークフロー");
        input.setBusinessRequirements("""
                社内申請の検索、詳細参照、作成、更新、承認、却下を扱う。
                経費精算の支払処理対象化と代理承認者変更は承認必須にしたい。
                AIは申請内容の要約と更新案作成までを補助する。
                """);
        input.setRequiredOperations("""
                - 申請検索
                - 申請詳細取得
                - 申請作成
                - 申請更新
                - 承認
                - 却下
                - 支払処理対象化
                - 代理承認者変更
                - 申請要約
                """);
        input.setAllowedAiOperations("""
                - 申請検索
                - 申請詳細参照
                - 申請要約
                - 申請更新案の作成
                """);
        input.setApprovalRequiredOperations("""
                - 承認
                - 却下
                - 支払処理対象化
                - 代理承認者変更
                """);
        input.setAuditLogRequiredOperations("""
                - 承認
                - 却下
                - 支払処理対象化
                - 代理承認者変更
                """);
        return input;
    }

    public static BlueprintInput knowledgeSearchAndSummary() {
        BlueprintInput input = baseInput("ナレッジ検索・要約");
        input.setBusinessRequirements("""
                ナレッジ記事の検索、詳細参照、作成、更新、要約、記事公開を扱う。
                未公開記事参照、外部共有リンク発行、問い合わせ回答確定送信は承認または警告対象にしたい。
                AIは検索、詳細参照、要約、更新案作成までを補助する。
                """);
        input.setRequiredOperations("""
                - ナレッジ検索
                - ナレッジ詳細取得
                - ナレッジ登録
                - ナレッジ更新
                - ナレッジ要約
                - 未公開記事参照
                - 記事公開
                - 外部共有リンク発行
                - 問い合わせ回答確定送信
                """);
        input.setAllowedAiOperations("""
                - ナレッジ検索
                - ナレッジ詳細参照
                - ナレッジ要約
                - ナレッジ更新案の作成
                """);
        input.setApprovalRequiredOperations("""
                - 未公開記事参照
                - 記事公開
                - 外部共有リンク発行
                - 問い合わせ回答確定送信
                """);
        input.setAuditLogRequiredOperations("""
                - 未公開記事参照
                - 記事公開
                - 外部共有リンク発行
                - 問い合わせ回答確定送信
                """);
        return input;
    }

    private static BlueprintInput baseInput(String targetDomain) {
        BlueprintInput input = new BlueprintInput();
        input.setTargetDomain(targetDomain);
        input.setUserTypes("- 担当者\n- 管理者\n- AIアシスタント");
        input.setOutputLanguage("日本語");
        return input;
    }
}

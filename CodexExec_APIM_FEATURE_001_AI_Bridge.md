# CodexExec_APIM_FEATURE_001_AI_Bridge 実装指示書

## 1. 作業目的

APIM for Spring に「外部AIプロンプトブリッジ機能」の要件定義書を保存し、必要に応じて改善タスクとして登録する。

本作業は要件定義ドキュメント保存を主目的とする。Java実装、Controller追加、Service追加、Thymeleaf画面追加、テスト追加はこの作業の対象外とする。

## 2. 対象リポジトリ

`yamaguchiyoshishigeai-create/api-mcp-blueprint-for-spring`

## 3. 作業ブランチ

`docs/apim-feature-001-ai-bridge-requirements`

## 4. 実施内容

1. 作業開始前に `chatgpt-ops-rules` の現行正本または対象リポジトリ内の参照規程を確認し、現行運用規程に従う。
2. 要件定義書を以下へ追加する。
   - `docs/20_要件定義/APIM-FEATURE-001_外部AIプロンプトブリッジ機能_要件定義書.md`
3. 改善タスク一覧が存在する場合、未解決タスクとして本機能の実装準備タスクを登録する。
   - 既存最大TSK番号の次番号を使用する。
   - 既に同主題のTSKが存在する場合は重複登録しない。
4. `git diff --check` を実行する。
5. 可能であればPRを作成する。

## 5. 実装対象外

- Java実装
- Controller追加
- Service追加
- Thymeleaf画面追加
- JSON取り込み実装
- テスト追加
- Render設定変更
- mainへのmerge
- 公開環境操作

## 6. 仕様不変ルール

安全チェックやツール制約を回避する目的で、要求仕様、文言、テスト期待値を弱めたり曖昧化したりしないこと。

指定どおりに反映できない場合は、代替表現で済ませず、未反映として報告すること。

## 7. PR作成方針

PRタイトル案:

`APIM-FEATURE-001: 外部AIプロンプトブリッジ機能の要件定義を追加する`

PR本文には以下を含める。

- Summary
- Scope
- Validation
- Out of scope
- Result note

## 8. 失敗時報告形式

以下を結果に含める。

- JOB_ID
- TERMINAL_CLASS
- STATUS_DETAIL
- FAILED_STEP
- ERROR_SUMMARY
- CREATED_BRANCH
- CHANGED_FILES
- VALIDATION_RESULT
- PR_URL

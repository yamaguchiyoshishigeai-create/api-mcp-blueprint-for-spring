# APIM-015 生成品質回帰テスト自動化 Codex実装指示書 v0.1

## 1. 作業名

APIM-015「生成品質回帰テスト自動化」

## 2. 対象リポジトリ

- Repository: `yamaguchiyoshishigeai-create/api-mcp-blueprint-for-spring`
- Local repository root: `C:\academia\src\api-mcp-blueprint-for-spring`
- Base branch: `main`
- 作業ブランチ案: `feature/apim-015-generation-regression-tests`
- PRタイトル案: `APIM-015 生成品質回帰テスト自動化を整備する`

## 3. 作業目的

APIM-013で手動確認した3サンプルを、自動回帰テストとして整備する。

対象サンプル:

- 備品貸出管理
- 社内申請ワークフロー
- ナレッジ検索・要約

これにより、今後の命名改善、生成ロジック改善、画面変更時に、ユーザーが毎回ブラウザへ手入力してスクリーンショットを貼る運用を原則不要にする。

## 4. 重要な事前条件

作業開始前に以下を確認すること。

- `main` が最新であること
- `git status --short --untracked-files=all` が空であること
- APIMのローカルSpring Bootサーバーが起動中でないこと
- `target/`、`.class`、`.jar`、`.log`、`.result` をGit登録しないこと

確認コマンド例:

    cd C:\academia\src\api-mcp-blueprint-for-spring
    git checkout main
    git pull origin main
    git status --short --untracked-files=all
    Get-Process java -ErrorAction SilentlyContinue

JavaプロセスがAPIMのjarを掴んでいる場合は、削除やpackageに失敗するため、起動中サーバーを停止してから作業すること。

## 5. TSK番号方針

APIM-015では `TSK-011` を使用する。

- ID: TSK-011
- 件名: 生成品質回帰テスト自動化を整備する
- 状態: 確認待ち
- 優先度: High
- 主担当候補: Codex

APIM-015の実装PRでは、`TSK-011` を未解決から確認待ちへ更新すること。

## 6. 実装方針

最小実装優先で、まずJUnitベースの生成ロジック回帰テストを整備する。

優先順位:

1. 3サンプル入力をテストフィクスチャ化する
2. `BlueprintGenerationService` などサービス層を直接呼び出す生成品質回帰テストを追加する
3. 既存のAPIM-014テストと重複しすぎないよう、再利用性と可読性を高める
4. 画面E2EテストやPlaywright/Selenium導入は今回対象外とする

## 7. 追加候補ファイル

候補:

- `src/test/java/com/example/apim/testsupport/BlueprintInputFixtures.java`
- `src/test/java/com/example/apim/service/BlueprintGenerationRegressionTest.java`

既に類似ファイルや類似テストがある場合は、重複作成せず既存テストを拡張すること。

## 8. 必須テスト観点

### 8.1 備品貸出管理

- API path に `equipment` が含まれる
- DTO名に `Equipment` が含まれる
- MCP tool名に `Equipment` が含まれる
- 主要生成名に `domain-items` / `DomainItem` が含まれない
- 備品廃棄、管理者権限変更、外部送信系操作が承認必須または警告対象として維持される

### 8.2 社内申請ワークフロー

- API path に `applications` が含まれる
- DTO名に `Application` が含まれる
- MCP tool名に `Application` または `Applications` が含まれる
- 主要生成名に `domain-items` / `DomainItem` が含まれない
- 承認、却下、支払処理、代理承認者変更が承認必須または警告対象として維持される

### 8.3 ナレッジ検索・要約

- API path に `knowledge-articles` が含まれる
- DTO名に `KnowledgeArticle` が含まれる
- MCP tool名に `KnowledgeArticle` または `KnowledgeArticles` が含まれる
- 主要生成名に `domain-items` / `DomainItem` が含まれない
- 未公開記事参照、記事公開、外部共有リンク発行、問い合わせ回答確定送信が承認必須または警告対象として維持される

### 8.4 文書生成

- Markdown設計書に主要章が含まれる
- AI実装指示書に主要章が含まれる
- 初期MVP対象外の範囲を実装しない旨が維持される

## 9. 対象外

今回のAPIM-015では以下を行わない。

- Playwright/Selenium等の実ブラウザE2E導入
- 外部LLM API連携
- MCPサーバー実装
- DB永続化
- Spring Security本格実装
- OpenAPI完全生成
- UIレイアウト大規模変更

## 10. AI_REPO_RESULT追記

`AI_REPO_RESULT.md` に以下を追記すること。

- APIM-015の目的
- 追加・更新したテストファイル
- テスト観点
- Maven Wrapper test結果
- GitHub Actions結果
- 今後の手動ブラウザ確認を限定する運用方針
- PR URL

## 11. 改善タスク更新

APIM-015実装PRでは以下を行うこと。

- 改善タスク一覧の `TSK-011` を `確認待ち` に更新
- `docs/00_プロジェクト管理/02_改善タスク管理/未解決/TSK-011.md` を削除
- `docs/00_プロジェクト管理/02_改善タスク管理/確認待ち/TSK-011.md` を追加
- 個票に実装内容と確認結果を追記

## 12. 検証

最低限、以下を実行すること。

    .\mvnw.cmd test

またはLinux/macOS環境では:

    ./mvnw test

成功条件:

- 既存テストを含めて全テストがPASS
- 追加した回帰テストがPASS
- GitHub ActionsがPASS

## 13. 禁止事項

- `git push origin main`
- `main` への直接commit
- `main` への直接merge
- `target/` やjar/class/log/resultファイルのGit登録
- APIM-015対象外機能の実装

## 14. 最終報告形式

Codexは作業完了後、以下形式で報告すること。

- 作業ブランチ:
- コミット:
- PR URL:
- 変更ファイル:
- 追加・更新したテスト:
- 検証結果:
  - Maven Wrapper test:
  - GitHub Actions:
- 自動化された確認観点:
- TSK-011状態:
- mainへのmerge未実施確認:
- 補足・未解決事項:

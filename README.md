# API + MCP Blueprint Compiler for Spring

## 製品概要

API + MCP Blueprint Compiler for Spring（APIM for Spring）は、自然言語の業務要件から、Spring向けREST API設計とMCP設計を同時生成する設計支援Webツールです。

## このリポジトリの目的

本リポジトリは、APIM for Spring の設計・運用文書を正本管理し、MVPの要件整理と設計生成フローを段階的に具体化するための基盤です。

APIM-005で、初期Web MVP（Spring Boot + Thymeleaf）を実装済みです。

APIM-008で Maven Wrapper を追加済みです。ローカルPCにMaven本体が未導入でも、Windowsでは `mvnw.cmd`、macOS/Linuxでは `mvnw` からテスト・packageを実行できます。

## 初期MVP方針

- 初期MVPは設計生成に限定する
- 完全動作するMCPサーバーは生成しない
- 完全動作するSpring Bootアプリケーションは生成しない
- 生成対象は以下とする
  - API設計書
  - MCP設計書
  - API/MCP対応表
  - Spring Controller雛形
  - AI実装指示書

## 出力予定成果物

- `api-mcp-blueprint.md`（人向け設計成果物）
- `implementation-instructions.md`（AI実装入力）
- 要件・設計・運用ドキュメント群（`docs/`）

## APIMとQ-Scoutの違い

- APIM for Spring: 新規業務要件から設計を生成する設計生成系
- Q-Scout for Spring: 既存Springシステムの診断・評価を行う診断系

本リポジトリはQ-Scoutの診断ルール、スコアリング、解析ロジックを扱いません。

## 現時点で実装しないこと

- 完全動作するMCPサーバー実装
- 外部LLM API連携
- DB永続化
- 認証認可の本格実装
- OpenAPI完全生成
- 本番運用レベルの監視・アラート設計

## docs参照導線

- docs全体: `docs/README.md`
- 企画正本: `docs/10_企画/APIM-001-API-MCP-Blueprint-Compiler-for-Spring-立ち上げ方針書-v0.1.md`
- 要件定義ガイド: `docs/20_要件定義/20_要件定義ガイド.md`
- 基本設計: `docs/30_基本設計/基本設計.md`
- 詳細設計: `docs/40_詳細設計/詳細設計.md`
- 改善タスク一覧: `docs/00_プロジェクト管理/02_改善タスク管理/改善タスク課題一覧.md`
- Render公開準備: `docs/50_実装/Render公開準備.md`
- Render公開前チェックリスト: `docs/50_実装/Render公開前チェックリスト.md`
- Render公開後確認記録: `docs/50_実装/Render公開後確認記録.md`

## 開発運用基盤

- 改善タスク管理: `docs/00_プロジェクト管理/02_改善タスク管理/`
- 横断運用規程: `docs/00_プロジェクト管理/05_横断運用規程/`
- PRテンプレート: `.github/PULL_REQUEST_TEMPLATE.md`
- 最低限チェック: `.github/workflows/template-checks.yml`, `scripts/*.py`
- Maven Wrapper: `mvnw`, `mvnw.cmd`, `.mvn/wrapper/maven-wrapper.properties`
- Render公開準備: `Dockerfile`, `render.yaml`, `src/main/resources/application.properties`

## 作業分担

- 指示書作成: ChatGPT（会話内）
- 初期リポジトリ生成: Codex
- 通常PRのPR確認・CI確認・merge判断: ChatGPT(リポジトリ編集)
- 外部サービス操作・公開判断: ユーザー(人)

## Web MVP 起動方法

Windows PowerShellでは、以下のいずれかの方式で起動できます。

### 方式1: spring-boot:run

```powershell
.\mvnw.cmd spring-boot:run
```

正常起動すると、Spring Boot Webサーバーが前面で継続実行されます。停止する場合は `Ctrl+C` を使用します。

### 方式2: package 後の jar 起動

1. `mvnw.cmd package` を実行します。
2. 生成された `target/apim-0.0.1-SNAPSHOT.jar` をJavaで起動します。
3. ブラウザで `http://localhost:8080/` を開きます。

macOS/Linuxでも同様に、`./mvnw spring-boot:run` または `./mvnw package` 後のjar起動を利用できます。

### TSK-008 再検証結果

旧環境では、Windowsローカル環境で `mvnw.cmd spring-boot:run` 実行時に main class を検出できない事象を確認していました。

2026-05-03に、リポジトリを `C:\academia\src\api-mcp-blueprint-for-spring` 配下へ移動した状態で再検証した結果、`mvnw.cmd spring-boot:run`、main class明示版、fork=false指定版のいずれも `Tomcat started on port 8080` / `Started ApimApplication` まで到達しました。

このため、TSK-008は現行環境では再現しないものとして解決済みです。旧事象は、旧OneDrive配下パス、当時のPowerShell引数指定、または当時のローカル環境条件に起因していた可能性があります。

## Render公開準備

APIM for Spring は Render の Docker Web Service として公開できるように、以下の設定を用意しています。

| ファイル | 役割 |
|---|---|
| `Dockerfile` | Maven Wrapperでjarをbuildし、Java 17 runtimeでSpring Bootを起動する |
| `render.yaml` | Render BlueprintからWeb Serviceを作成する |
| `src/main/resources/application.properties` | `server.port=${PORT:8080}` によりRenderの `PORT` 環境変数へ追従する |

Render Blueprintを利用する場合は、Render Dashboardから本リポジトリを接続し、`render.yaml` をもとにWeb Serviceを作成します。

主な設定値は以下です。

- Runtime: Docker
- Dockerfile Path: `./Dockerfile`
- Health Check Path: `/`
- Auto Deploy: off
- Java: 17
- Build: Maven Wrapper

公開前は `docs/50_実装/Render公開前チェックリスト.md` を確認し、公開後は `docs/50_実装/Render公開後確認記録.md` に確認結果を記録します。

公開後は、Renderが発行したURLで以下を確認します。

1. `/` で初回体験ルートの入力画面が表示されること。
2. サンプル業務パターンから設計生成結果画面へ遷移できること。
3. Markdown設計書プレビューを開けること。
4. AI実装指示書プレビューを開けること。
5. 設計書とAI実装指示書をダウンロードできること。
6. `/help` を開けること。

詳細は `docs/50_実装/Render公開準備.md` を参照してください。

## テスト実行

Windows PowerShell:

`mvnw.cmd test`

macOS/Linux:

`mvnw test`

Maven本体をインストール済みの場合:

`mvn test`

## 初期MVPの実装範囲（APIM-005）

- 業務要件入力フォーム
- API設計候補生成
- MCP設計候補生成
- API/MCP対応表生成
- Markdown設計書生成
- AI実装指示書生成
- セキュリティ・承認・監査ログ注意点生成

## APIM-007 手動確認結果

Windowsローカル環境で以下を確認済みです。

- `mvnw.cmd test`: PASS
- `mvnw.cmd package`: PASS
- jar起動: PASS
- `http://localhost:8080/` 入力画面表示: PASS
- 顧客管理サンプル生成: PASS
- Markdown設計書プレビュー: PASS
- AI実装指示書プレビュー: PASS
- ヘルプ画面: PASS

## リポジトリ情報

- Repository: `yamaguchiyoshishigeai-create/api-mcp-blueprint-for-spring`
- Default Branch: `main`
- Product Category: Spring向け API / MCP 設計生成支援ツール
- Target Framework: Spring Boot / Spring Framework
- Primary Runtime: Java 17
- Build Tool: Maven / Maven Wrapper

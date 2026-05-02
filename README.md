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
- Docker / 本番デプロイ構成

## docs参照導線

- docs全体: `docs/README.md`
- 企画正本: `docs/10_企画/APIM-001-API-MCP-Blueprint-Compiler-for-Spring-立ち上げ方針書-v0.1.md`
- 要件定義ガイド: `docs/20_要件定義/20_要件定義ガイド.md`
- 基本設計: `docs/30_基本設計/基本設計.md`
- 詳細設計: `docs/40_詳細設計/詳細設計.md`
- 改善タスク一覧: `docs/00_プロジェクト管理/02_改善タスク管理/改善タスク課題一覧.md`

## 開発運用基盤

- 改善タスク管理: `docs/00_プロジェクト管理/02_改善タスク管理/`
- 横断運用規程: `docs/00_プロジェクト管理/05_横断運用規程/`
- PRテンプレート: `.github/PULL_REQUEST_TEMPLATE.md`
- 最低限チェック: `.github/workflows/template-checks.yml`, `scripts/*.py`
- Maven Wrapper: `mvnw`, `mvnw.cmd`, `.mvn/wrapper/maven-wrapper.properties`

## 作業分担

- 指示書作成: ChatGPT（会話内）
- 初期リポジトリ生成: Codex
- PR確認・merge判断: ユーザー

## Web MVP 起動方法

Windows PowerShellでは、APIM-007の手動確認結果に基づき、jar起動方式を推奨します。

1. `mvnw.cmd package` を実行します。
2. 生成された `target/apim-0.0.1-SNAPSHOT.jar` をJavaで起動します。
3. ブラウザで `http://localhost:8080/` を開きます。

macOS/Linuxでも同様に、`mvnw package` 実行後に生成されたjarをJavaで起動します。

### 既知課題

Windowsローカル環境では、`mvnw.cmd spring-boot:run` 実行時に main class を検出できない事象を確認しています。

当面は package 後の jar起動を正式なローカル起動手順とします。この原因調査は TSK-008 で扱います。

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

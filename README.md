# API + MCP Blueprint Compiler for Spring

## 製品概要

API + MCP Blueprint Compiler for Spring（APIM for Spring）は、自然言語の業務要件から、Spring向けREST API設計とMCP設計を同時生成する設計支援Webツールです。

## このリポジトリの目的

本リポジトリは、APIM for Spring の設計・運用文書を正本管理し、MVPの要件整理と設計生成フローを段階的に具体化するための基盤です。

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

## 作業分担

- 指示書作成: ChatGPT（会話内）
- 初期リポジトリ生成: Codex
- PR確認・merge判断: ユーザー

## Web MVP 起動方法

```bash
mvn spring-boot:run
```

アクセスURL:

`http://localhost:8080/`

## テスト実行

```bash
mvn test
```

## 初期MVPの実装範囲（APIM-005）

- 業務要件入力フォーム
- API設計候補生成
- MCP設計候補生成
- API/MCP対応表生成
- Markdown設計書生成
- AI実装指示書生成
- セキュリティ・承認・監査ログ注意点生成

## リポジトリ情報

- Repository: `yamaguchiyoshishigeai-create/api-mcp-blueprint-for-spring`
- Default Branch: `main`
- Product Category: Spring向け API / MCP 設計生成支援ツール
- Target Framework: Spring Boot / Spring Framework
- Primary Runtime: Java 17
- Build Tool: Maven

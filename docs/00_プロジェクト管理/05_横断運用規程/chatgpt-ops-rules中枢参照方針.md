# chatgpt-ops-rules 中枢参照方針

## 1. 目的

本書は、APIM for Spring における共通運用ルールの参照先を明確化するための文書である。

共通運用ルールの正本は、`yamaguchiyoshishigeai-create/chatgpt-ops-rules` とする。

本リポジトリには、APIM固有の業務仕様、実装仕様、環境依存手順、公開準備、設計生成系としての製品前提を残す。

## 2. 正本の分離

| 区分 | 管理場所 |
|---|---|
| 共通運用ルール | `yamaguchiyoshishigeai-create/chatgpt-ops-rules` |
| APIM固有の業務仕様 | `api-mcp-blueprint-for-spring` |
| APIM固有の実装仕様 | `api-mcp-blueprint-for-spring` |
| APIM固有の環境依存手順 | `api-mcp-blueprint-for-spring` |
| Render公開準備・公開前後確認 | `api-mcp-blueprint-for-spring` |
| 改善タスク個票 | 対象作業が属するリポジトリ |

## 3. 作業開始時の参照順

APIM for Spring の作業開始時は、以下の順に確認する。

1. Memory上のプロジェクト運用ルール。
2. `chatgpt-ops-rules` の `PROJECT_START_PROMPT.md`。
3. `chatgpt-ops-rules` の横断運用規程入口。
4. APIM側の `PROJECT_START_PROMPT.md`。
5. APIM側の `README.md`。
6. APIM側の `docs/README.md`。
7. APIM側の改善タスク課題一覧。
8. APIM固有の要件定義、設計、実装、公開準備文書。

## 4. 衝突時の優先順位

共通運用ルールとAPIM側文書に矛盾がある場合は、原則として `chatgpt-ops-rules` を優先する。

ただし、APIM固有の業務仕様、設計生成系としての製品前提、Render公開準備、ローカル起動手順などはAPIM側文書を正とする。

## 5. APIM固有として残す事項

以下はAPIM側に残す。

- APIMは診断系ではなく設計生成系であるという製品前提。
- API設計書、MCP設計書、API/MCP対応表、Spring Controller雛形、AI実装指示書の生成方針。
- 初回訪問者向け体験ルート、サンプル業務パターン、生成結果プレビューの仕様。
- Render公開準備、公開前チェックリスト、公開後確認記録。
- Maven Wrapper、Dockerfile、render.yaml、ローカル起動・テスト手順。
- APIM側改善タスク課題一覧と個票。

## 6. APIM側に重複保持しない事項

以下の共通運用ルール本文は、APIM側へ重複コピーしない。

- ChatExec / ChatExec2方式の一般規程。
- 通常PR自動merge方針。
- 個票先行main反映ゲート。
- FullFlow bat一括実行方針。
- Codex投入前ハンドオフゲート。
- 安全チェック発生時の仕様不変切替方針。
- 発生・残存課題の個票化最優先ルール。
- 実行計画と実行指示の分離方針。

必要な場合は、APIM側に本文を再掲せず、`chatgpt-ops-rules` への参照として扱う。

## 7. 関連文書

- `PROJECT_START_PROMPT.md`
- `README.md`
- `docs/README.md`
- `docs/00_プロジェクト管理/02_改善タスク管理/改善タスク課題一覧.md`
- `docs/50_実装/Render公開準備.md`
- `docs/50_実装/Render公開前チェックリスト.md`
- `docs/50_実装/Render公開後確認記録.md`

# Render公開前チェックリスト

## 1. 目的

本チェックリストは、APIM for Spring を Render へ公開する前に確認すべき事項を整理するための文書である。

Render上での実サービス作成、URL確定、公開判断はユーザー(人)が行う。ChatGPTは、リポジトリ状態、文書整合、CI状態、確認観点の整理を支援する。

## 2. 公開判断の前提

| 項目 | 確認内容 | 結果 | 備考 |
|---|---|---|---|
| 公開判断主体 | Render公開判断はユーザー(人)が行う | 未確認 | 外部サービス操作・公開判断のため |
| 対象リポジトリ | `yamaguchiyoshishigeai-create/api-mcp-blueprint-for-spring` | 未確認 |  |
| 対象ブランチ | `main` | 未確認 |  |
| 対象commit | 公開対象commit SHAを記録する | 未確認 |  |
| Open PR | APIM側にOpen PRがない | 未確認 |  |
| 課題管理状態 | 公開前に残すべき未解決TSKがない、または未解決理由が明確 | 未確認 |  |
| CI | 対象commitのCIが終了条件まで `success` | 未確認 | queued / in_progress / failure は不可 |
| local sync | ローカルmainが公開対象commitへ同期済み | 未確認 |  |
| worktree | ローカルworktreeがclean | 未確認 |  |

## 3. Render設定確認

| 項目 | 確認内容 | 結果 | 備考 |
|---|---|---|---|
| Runtime | Docker Web Serviceとして作成する | 未確認 |  |
| Dockerfile | `Dockerfile` が存在する | 未確認 |  |
| render.yaml | `render.yaml` が存在する | 未確認 |  |
| Dockerfile Path | `./Dockerfile` | 未確認 |  |
| Docker Context | `.` | 未確認 |  |
| Java | Java 17想定 | 未確認 |  |
| Build | Maven Wrapperでjarをbuildする | 未確認 |  |
| Start | DockerfileのENTRYPOINTでSpring Boot jarを起動する | 未確認 |  |
| Port | `server.port=${PORT:8080}` によりRenderのPORTへ追従する | 未確認 |  |
| Health Check Path | `/` | 未確認 |  |
| Auto Deploy | 初回公開時は意図しない自動公開を避ける | 未確認 | 必要に応じてoff |

## 4. 製品説明・範囲確認

| 項目 | 確認内容 | 結果 | 備考 |
|---|---|---|---|
| 製品分類 | APIM for Springは設計生成支援ツールである | 未確認 | 診断系ではない |
| Q-Scoutとの差分 | Q-Scoutの診断・評価・スコアリングを扱わない | 未確認 |  |
| MVP範囲 | API設計書、MCP設計書、API/MCP対応表、Controller雛形、AI実装指示書を生成する | 未確認 |  |
| 対象外明示 | 完全動作するMCPサーバーを生成しない | 未確認 |  |
| 対象外明示 | 完全動作するSpring Bootアプリケーションを生成しない | 未確認 |  |
| 対象外明示 | 外部LLM API連携、DB永続化、本格認証認可は対象外 | 未確認 |  |

## 5. 画面・動線確認

公開前のローカル確認、または公開後のRender URL確認で使用する。

| 項目 | 確認内容 | 結果 | 備考 |
|---|---|---|---|
| `/` | 初回体験ルートの入力画面が表示される | 未確認 |  |
| サンプル業務パターン | 複数サンプルから設計生成へ進める | 未確認 |  |
| 入力フォーム | 業務要件を入力できる | 未確認 |  |
| 設計生成結果 | API/MCP設計候補が表示される | 未確認 |  |
| Markdownプレビュー | Markdown設計書プレビューを開ける | 未確認 |  |
| AI実装指示書プレビュー | AI実装指示書プレビューを開ける | 未確認 |  |
| ダウンロード | 設計書とAI実装指示書をダウンロードできる | 未確認 |  |
| 入力状態復元 | 生成結果から入力状態復元導線を利用できる | 未確認 |  |
| `/help` | help画面を開ける | 未確認 |  |

## 6. 回帰・禁止語確認

| 項目 | 確認内容 | 結果 | 備考 |
|---|---|---|---|
| 禁止語リスト | `src/test/resources/regression/forbidden-output-phrases.txt` が存在する | 未確認 |  |
| 生成物横断ガード | 生成物に禁止語が含まれないことを検査するテストが存在する | 未確認 |  |
| メタガード | 禁止語リストや重要証跡の削除・空化を検出するテストが存在する | 未確認 |  |
| Regression Evidence Matrix | `RegressionEvidenceMatrix.md` にTSK-044証跡がある | 未確認 |  |
| 旧注意文言 | `APIM for Spring本体の改修指示ではない` がユーザー向け生成物へ出ない | 未確認 |  |

## 7. 公開可否判断欄

| 項目 | 記入欄 |
|---|---|
| 公開判断 | 未判断 / 公開可 / 公開延期 |
| 判断者 |  |
| 判断日 |  |
| 公開対象commit |  |
| 公開前に残す未解決事項 |  |
| 公開延期理由 |  |
| 備考 |  |

## 8. 関連文書

- `docs/50_実装/Render公開準備.md`
- `docs/50_実装/Render公開後確認記録.md`
- `README.md`
- `docs/00_プロジェクト管理/06_品質管理/RegressionEvidenceMatrix.md`

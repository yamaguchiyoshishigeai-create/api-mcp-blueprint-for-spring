# APIM自由文構造化v2 Schema設計案 v0.1

## 提案名

APIM v2業務構造Schema案

## 対応Issue

- Parent: #160
- Child: #161

## 1. 設計目的

`apim-blueprint-input/v2` は、自由文から抽出した業務要件を、API/MCP Generator が安全に処理できる中間構造として定義する。

v1のように `targetDomain` と操作一覧へ単純正規化するのではなく、以下を構造として保持する。

- 業務領域
- 業務オブジェクト
- 利用者・ロール
- 操作インスタンス
- AI実行可否
- 承認要否
- 監査ログ要否
- オブジェクト間関係
- 操作グループ
- 曖昧点
- 安全側デフォルト

これにより、営業、契約、請求、入金、注文、在庫、配送、決済、監査、通知などが1つの自由文に混在しても、単一ドメインへ潰さずに扱えるようにする。

## 2. トップレベル構造

    {
      "schemaVersion": "apim-blueprint-input/v2",
      "judgement": {},
      "businessContext": {},
      "domains": [],
      "businessObjects": [],
      "actors": [],
      "operations": [],
      "operationGroups": [],
      "relationships": [],
      "securityPolicy": {},
      "generationHints": {},
      "ambiguities": []
    }

## 3. 必須 / 任意

| フィールド | 必須 | 役割 |
|---|---:|---|
| `schemaVersion` | 必須 | `apim-blueprint-input/v2` 固定 |
| `judgement` | 必須 | 生成可否・信頼度・警告 |
| `businessContext` | 必須 | システム全体目的・言語・業務概要 |
| `domains` | 必須 | 業務領域一覧 |
| `businessObjects` | 必須 | 業務対象リソース一覧 |
| `actors` | 必須 | 利用者・ロール一覧 |
| `operations` | 必須 | 業務操作一覧 |
| `operationGroups` | 任意 | 操作のまとまり |
| `relationships` | 任意 | オブジェクト間関係 |
| `securityPolicy` | 必須 | 全体安全方針 |
| `generationHints` | 任意 | API/MCP/Markdown生成ヒント |
| `ambiguities` | 任意 | 曖昧点・確認事項 |

## 4. judgement

`judgement` は外部AIの判定状態、生成可否、信頼度、不足情報、警告を保持する。

### state

| 値 | 意味 |
|---|---|
| `invalid` | 無効入力。JSON生成対象外 |
| `needs_clarification` | 情報不足。外部AIが追加質問すべき |
| `ready_to_generate` | JSON生成可能 |

`invalid` と `needs_clarification` は、原則としてAPIM取込用JSONを出さない段階である。APIMへ渡されるJSONは原則 `ready_to_generate` のみとする。ただしSchema上はログ、テスト、将来拡張のために状態値を定義する。

## 5. businessContext

`businessContext` は自由文全体から見たシステム目的、要約、言語、入力種別を保持する。

| フィールド | 必須 | 説明 |
|---|---:|---|
| `systemPurpose` | 必須 | システムの目的 |
| `summary` | 必須 | 入力要件の短い要約 |
| `language` | 必須 | `ja` / `en` 等 |
| `sourceInputType` | 必須 | `free_text` / `manual_form` / `imported_json` |
| `sourceInputSummary` | 任意 | 元自由文の要約 |

## 6. domains

`domains[]` は業務領域を表す。複数の `primary` を許容する。

### role

| 値 | 意味 |
|---|---|
| `primary` | 主要業務領域 |
| `supporting` | 補助業務領域 |
| `cross_cutting` | 監査、通知、承認など横断領域 |
| `external` | 外部システム・外部サービス |

## 7. businessObjects

`businessObjects[]` は業務対象リソースを表す。各オブジェクトは `domainId` により業務領域へ紐づく。

### sensitivity

| 値 | 意味 |
|---|---|
| `public` | 公開情報 |
| `internal` | 社内情報 |
| `confidential` | 機密情報 |
| `restricted` | 強い制限が必要な情報 |

### dataCategories 例

- `customer_related`
- `contract`
- `billing`
- `payment`
- `personal_data`
- `financial`
- `audit`
- `authentication`
- `authorization`

## 8. actors

`actors[]` は利用者・ロール・AI・外部システムを表す。

### actorType

| 値 | 意味 |
|---|---|
| `human_user` | 人間ユーザー |
| `approver` | 承認者 |
| `admin` | 管理者 |
| `ai_agent` | AIエージェント |
| `external_system` | 外部システム |

## 9. operations

`operations[]` はv2の中核である。個々の業務操作インスタンスを表す。

### intent

| 値 | 意味 |
|---|---|
| `read` | 参照 |
| `search` | 検索 |
| `ai_summary` | AI要約 |
| `ai_analysis` | AI分析・候補提示 |
| `ai_draft` | AI文案・下書き作成 |
| `proposal` | 変更提案 |
| `approval_request` | 承認依頼 |
| `write` | 更新 |
| `state_transition` | 状態変更 |
| `delete` | 削除 |
| `external_action` | 外部送信・外部実行 |
| `admin` | 管理操作 |

### executionMode

| 値 | 意味 |
|---|---|
| `direct_read` | 直接参照可 |
| `ai_assisted` | AI支援可 |
| `draft_only` | 下書き作成のみ |
| `proposal_only` | 提案のみ |
| `human_approved_write` | 人間承認後に更新 |
| `human_only` | 人間のみ実行可 |
| `system_only` | システム内部のみ |

### aiPermission

| 値 | 意味 |
|---|---|
| `allowed` | AI実行可 |
| `allowed_with_review` | AI支援可だが人間確認推奨 |
| `not_allowed_directly` | AI直接実行不可 |
| `human_only` | 人間のみ |
| `unknown` | 未確定 |

### auditLogRequired

| 値 | 意味 |
|---|---|
| `none` | 不要 |
| `recommended` | 推奨 |
| `required` | 必須 |

### riskLevel

| 値 | 意味 |
|---|---|
| `low` | 低リスク |
| `medium` | 中リスク |
| `high` | 高リスク |
| `critical` | 重大リスク |

## 10. operationGroups

`operationGroups[]` は操作のまとまりを表す。API/MCP/Markdownで章やグループを作るために使う。v0.1では任意項目とする。

## 11. relationships

`relationships[]` は業務オブジェクト間の関係を表す。v0.1では任意項目とする。

### type

| 値 | 意味 |
|---|---|
| `has_one` | 1対1 |
| `has_many` | 1対多 |
| `belongs_to` | 所属 |
| `references` | 参照 |
| `depends_on` | 依存 |
| `triggers` | トリガー |
| `approves` | 承認関係 |
| `audits` | 監査関係 |

## 12. securityPolicy

`securityPolicy` は全体安全方針を表す。

安全方針:

- 金銭影響あり → 承認必須
- 契約影響あり → 承認必須
- 入金・消込 → 承認必須
- 権限変更 → 承認必須
- 外部送信 → 承認必須
- 削除 → 承認必須
- AI文案作成 → 送信とは分離
- AI変更提案 → 実更新とは分離

## 13. generationHints

`generationHints` はGeneratorに対するヒントである。安全条件は `operations[]` と `securityPolicy` を優先する。

## 14. ambiguities

`ambiguities[]` は自由文から確定できない事項を記録する。

### type

| 値 | 意味 |
|---|---|
| `missing_actor` | 利用者不明 |
| `missing_object` | 対象情報不明 |
| `missing_approval_actor` | 承認者不明 |
| `draft_vs_send_boundary` | 文案作成と送信の境界不明 |
| `proposal_vs_write_boundary` | 提案と更新の境界不明 |
| `external_action_unclear` | 外部送信・外部実行が不明 |
| `audit_requirement_unclear` | 監査要否不明 |
| `authorization_unclear` | 権限境界不明 |
| `data_sensitivity_unclear` | データ感度不明 |

### severity

| 値 | 意味 |
|---|---|
| `low` | 軽微 |
| `medium` | 要確認 |
| `high` | 設計前確認推奨 |
| `critical` | 生成停止または承認必須 |

## 15. v1互換方針

### 提案名

v1/v2併存移行案

| 入力 | 扱い |
|---|---|
| `schemaVersion = apim-blueprint-input/v1` | 既存処理へ流す |
| `schemaVersion = apim-blueprint-input/v2` | 新v2処理へ流す |
| 未指定 | エラー |
| 未知バージョン | エラー |

初期段階ではv1からv2への自動変換は必須にしない。v1は情報粒度が粗いため、自動v2化すると推測が多くなるためである。

## 16. サンプルJSON

v0.1では、成功例だけでなく、情報不足・無効入力の参照例も含めて10本を配置する。

- `samples/sales-contract-billing.v2.json`
- `samples/order-inventory-delivery-billing-payment.v2.json`
- `samples/hr-onboarding-access-approval.v2.json`
- `samples/inquiry-knowledge-customer-notification.v2.json`
- `samples/procurement-approval-invoice.v2.json`
- `samples/maintenance-incident-inventory.v2.json`
- `samples/facility-reservation-billing.v2.json`
- `samples/needs-clarification-vague-business.v2.reference.json`
- `samples/needs-clarification-approval-boundary.v2.reference.json`
- `samples/invalid-meaningless-input.v2.reference.json`

`.reference.json` は、実運用でAPIMへ取り込む通常JSONではなく、外部AIプロンプト制御・Schema設計・回帰テスト用の参照サンプルである。

## 17. v0.1時点の設計判断

### 採用

- `targetDomain` はv2正本から外す。
- 複数 `primary` domainを許容する。
- `businessObjects[]` をGeneratorの中心にする。
- `operations[]` をGeneratorの中心にする。
- AI文案作成と外部送信を分離する。
- 変更依頼と実更新を分離する。
- 曖昧点は `ambiguities[]` に保持する。
- 金銭・契約・入金・権限・削除・外部送信は安全側に倒す。
- 成功例だけでなく `needs_clarification` / `invalid` の参照サンプルも保持する。

### 保留

- `operationGroups[]` を必須にするか任意にするか。v0.1では任意。
- `relationships[]` を必須にするか任意にするか。v0.1では任意。
- `securityPolicy` の詳細化。v0.1では全体方針に留め、operation単位の属性を優先する。

## 18. 次フェーズ

#162以降で以下を段階実装する。

- 外部AI投入用プロンプトをv2業務構造抽出型へ改修する。
- v2 JSON取込・検証を実装する。
- 抽出結果確認画面を実装する。
- API GeneratorをbusinessObjects/operationsベースへ再設計する。
- MCP Generatorを安全レベル別tool生成へ再設計する。
- Markdown設計書を構造確認優先へ再編する。
- 複合自由文回帰テストを整備する。

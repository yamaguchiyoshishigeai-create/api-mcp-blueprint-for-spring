# APIM-001 API + MCP Blueprint Compiler for Spring 立ち上げ方針書 v0.1

## 1. 文書概要

### 1.1 文書目的

本書は、新規ポートフォリオ開発候補である **API + MCP Blueprint Compiler for Spring** の立ち上げ方針を定義する文書である。

本プロダクトは、自然言語で記述された業務要件から、Spring Boot / Spring Framework 向けの REST API 設計と、AIエージェント / LLM アプリケーション向けの MCP 設計を同時に生成する **設計支援Webツール** として立ち上げる。

初期MVPでは、完全動作する Spring アプリケーションや MCP サーバーを自動生成するのではなく、以下の成果物を生成する **設計コンパイラ** として開始する。

- API設計書
- RESTエンドポイント一覧
- Request / Response DTO候補
- Spring Controller雛形
- MCP tools / resources / prompts 定義案
- API / MCP 対応表
- セキュリティ・承認・監査ログ設計メモ
- AI実装指示書

---

## 2. 製品コンセプト

### 2.1 製品名

第一候補:

    API + MCP Blueprint Compiler for Spring

第二候補:

    Spring API/MCP Blueprint Builder

第三候補:

    API MCP Compiler for Spring

### 2.2 コンセプト

**API + MCP Blueprint Compiler for Spring** は、業務要件を入力すると、Spring向けの REST API 設計と MCP 設計を同時に生成する設計支援ツールである。

従来、API設計は人間・フロントエンド・他システム連携を主対象として整理されてきた。一方、AIエージェントやLLMアプリケーションの普及により、業務機能・業務データ・操作手順をAIから安全に利用可能にするための MCP 設計も重要になっている。

本プロダクトは、以下の問いに答える。

- この業務要件から、どの REST API を設計すべきか
- その API はどの Spring Controller / DTO / Service 境界で表現すべきか
- 同じ業務機能を AI エージェントに公開する場合、どの MCP tool / resource / prompt として設計すべきか
- API と MCP tool はどのように対応するか
- 書き込み系操作にはどのような承認・監査・人間確認が必要か
- Codex 等の実装AIに渡す場合、どのような実装指示書にすべきか

### 2.3 提案名

    APIM-001 API + MCP Blueprint Compiler for Spring 立ち上げ方針

### 2.4 製品カテゴリ

    Spring向け API / MCP 設計生成支援ツール

### 2.5 提供価値

本プロダクトの提供価値は以下である。

1. 業務要件から API 設計への落とし込みを高速化する
2. API と MCP を別々に設計することによる不整合を防ぐ
3. AIエージェントに許可する操作範囲を設計段階で明確化する
4. 承認必須操作、監査ログ、権限設計を初期段階から組み込む
5. Spring 実装へ進む前の設計品質を底上げする
6. Codex 等に渡せる実装指示書を生成し、実装フェーズへの接続を容易にする

---

## 3. Q-Scout for Spring との差別化

### 3.1 Q-Scout for Spring の位置づけ

Q-Scout for Spring は、既存の Spring Boot / Spring Framework プロジェクトを対象に、設計健全性や実装品質を診断するプロダクトである。

位置づけは以下である。

    既存Springシステムを診断するツール

主な価値は以下である。

- 既存コードの品質診断
- 設計逸脱の検出
- スコアリング
- 改善ヒント提示
- 人間向け / AI向け Markdown 成果物生成

### 3.2 API + MCP Blueprint Compiler for Spring の位置づけ

本プロダクトは、既存コードを診断するのではなく、これから作る API / MCP 境界を設計する。

位置づけは以下である。

    新規Springシステムの API / MCP 設計を生成するツール

主な価値は以下である。

- 業務要件から API 設計を生成
- 業務要件から MCP 設計を生成
- API / MCP 対応表を生成
- Spring Controller 雛形を生成
- AI実装指示書を生成

### 3.3 差別化の整理

| 観点 | Q-Scout for Spring | API + MCP Blueprint Compiler for Spring |
|---|---|---|
| 主対象 | 既存Springコード | 新規業務要件 |
| 主要目的 | 診断・評価・改善 | 設計生成・実装準備 |
| 入力 | SpringプロジェクトZIP / コード | 自然言語の業務要件 |
| 出力 | 診断レポート / AI入力Markdown | API設計書 / MCP設計書 / 実装指示書 |
| タイミング | 実装後・レビュー時 | 実装前・設計時 |
| 価値軸 | 品質可視化 | 設計高速化 |
| AI連携 | AIに改善相談しやすい成果物 | AIに実装させやすい指示書 |

### 3.4 ポートフォリオ上の関係

本プロダクトと Q-Scout は、競合ではなく連続した開発支援フローを形成する。

    設計する
      → API + MCP Blueprint Compiler for Spring

    実装する
      → Codex / Spring-Tool-Development-Template / 手動実装

    診断する
      → Q-Scout for Spring

この構成により、ポートフォリオ上は以下のストーリーを提示できる。

- 業務要件から API / MCP 設計を作る
- 生成された設計書をもとに実装する
- 実装された Spring プロジェクトを Q-Scout で診断する
- 設計から診断までを一連の Spring 開発支援ラインとして見せる

---

## 4. MCPの位置づけ

### 4.1 APIとMCPは競合しない

本プロダクトでは、API と MCP を競合関係として扱わない。

両者の役割は以下のように分ける。

| 区分 | API | MCP |
|---|---|---|
| 主な利用者 | 人間、フロントエンド、他システム | AIエージェント、LLMアプリケーション |
| 主な入口 | RESTエンドポイント | tools / resources / prompts |
| 主な関心 | 業務機能のHTTP公開 | AIが安全に業務機能・データへアクセスする方法 |
| 設計観点 | URL、HTTPメソッド、DTO、ステータスコード | tool名、引数、戻り値、許可範囲、承認要否 |
| リスク | 認証・認可漏れ、過剰公開 | AIによる過剰操作、誤操作、自動実行リスク |

### 4.2 MCPの役割

MCP は、AIエージェントが業務システムの機能・データ・操作へアクセスするための設計入口として位置づける。

本プロダクトでは、MCPを以下の3要素に分解して扱う。

1. tools
   - AIが実行できる業務操作
   - 例: 顧客検索、注文作成、在庫確認、申請承認依頼

2. resources
   - AIが参照できる業務データまたは文書
   - 例: 顧客情報、商品情報、注文履歴、業務ルール文書

3. prompts
   - AIが特定業務を遂行するための定型指示
   - 例: 顧客問い合わせ対応、注文変更支援、承認前チェック

### 4.3 初期MVPにおけるMCPの扱い

初期MVPでは、完全動作する MCP サーバーを生成しない。

初期MVPで生成するのは以下である。

- MCP tools 定義案
- MCP resources 定義案
- MCP prompts 定義案
- API / MCP tool 対応表
- 権限・承認・監査ログ観点
- 将来のMCPサーバー実装に向けた実装指示書

### 4.4 MCP設計で重視する原則

MCP設計では、以下を必須観点とする。

1. 読み取り専用操作と書き込み操作を分ける
2. 書き込み操作には承認要否を明示する
3. 危険操作には人間確認を必須とする
4. AIが直接実行してよい操作と、提案までに留める操作を分ける
5. 監査ログに残すべき項目を定義する
6. tool の引数と戻り値を明確化する
7. 業務データの過剰公開を避ける

---

## 5. 初期MVP範囲

### 5.1 初期MVPの基本方針

初期MVPは、設計生成に特化する。

実装生成や完全動作サーバー生成に踏み込みすぎると、API設計、MCP仕様、Spring実装、セキュリティ、権限、テスト、デプロイまで範囲が膨らみ、MVPが重くなる。

そのため、初期MVPでは以下に絞る。

    業務要件から API / MCP の設計成果物を生成する

### 5.2 MVPで実施すること

初期MVPで実施することは以下である。

1. 業務要件入力フォームを提供する
2. 入力内容から API 設計サマリーを生成する
3. RESTエンドポイント一覧を生成する
4. Request / Response DTO候補を生成する
5. Spring Controller 雛形を生成する
6. MCP tools 一覧を生成する
7. MCP resources 一覧を生成する
8. MCP prompts 一覧を生成する
9. API / MCP tool 対応表を生成する
10. セキュリティ・承認・監査ログ注意点を生成する
11. Markdown設計書を生成する
12. AI実装指示書を生成する

### 5.3 MVPで実施しないこと

初期MVPで実施しないことは以下である。

1. 完全動作する Spring Boot アプリケーションの生成
2. 完全動作する MCP サーバーの生成
3. DBマイグレーションの自動生成
4. Entity / Repository / Service の完全実装
5. 認証・認可コードの自動実装
6. OpenAPI YAML の完全準拠生成
7. MCP仕様への完全準拠検証
8. 生成コードのビルド保証
9. GitHubリポジトリへの自動PR作成
10. 本番デプロイ

### 5.4 MVP成立条件

初期MVPの成立条件は以下である。

1. 入力フォームから業務要件を登録できる
2. API設計とMCP設計を同時に生成できる
3. API / MCP の対応関係が表形式で確認できる
4. Spring Controller 雛形が実装の起点として利用できる
5. Markdown設計書として保存・コピーできる
6. Codex等に渡せる実装指示書が出力できる
7. セキュリティ・承認・監査ログの注意点が必ず出力される

---

## 6. 入力仕様

### 6.1 入力項目一覧

初期MVPの入力項目は以下とする。

| 項目 | 必須 | 形式 | 内容 |
|---|---:|---|---|
| 業務要件 | 必須 | 複数行テキスト | 実現したい業務内容 |
| 対象ドメイン | 必須 | テキスト | 例: 顧客管理、注文管理、在庫管理 |
| ユーザー種別 | 必須 | 複数行テキスト | 管理者、一般ユーザー、営業担当、AIアシスタント等 |
| 必要な操作 | 必須 | 複数行テキスト | 検索、登録、更新、削除、承認、通知等 |
| AIアシスタントに許可したい操作 | 必須 | 複数行テキスト | AIに実行させたい操作 |
| 読み取り専用操作 | 任意 | 複数行テキスト | 参照のみ許可する操作 |
| 書き込み許可操作 | 任意 | 複数行テキスト | 作成・更新・削除を許可する操作 |
| 承認必須操作 | 任意 | 複数行テキスト | 人間承認が必要な操作 |
| 監査ログが必要な操作 | 任意 | 複数行テキスト | 証跡を残すべき操作 |
| 想定認証方式 | 任意 | 選択 / テキスト | session、JWT、OAuth2等 |
| 想定利用者 | 任意 | テキスト | 社内、顧客、管理者、外部連携等 |
| 出力言語 | 任意 | 選択 | 日本語 / 英語 |

### 6.2 入力例

    業務要件:
    社内の営業担当が顧客情報を検索し、問い合わせ履歴を確認できる。
    管理者は顧客情報を登録・更新できる。
    AIアシスタントには顧客検索と問い合わせ履歴の要約を許可したい。
    顧客情報の更新はAIが直接実行せず、変更案を作成して人間承認後に反映する。

    対象ドメイン:
    顧客管理

    ユーザー種別:
    - 営業担当
    - 管理者
    - AIアシスタント

    必要な操作:
    - 顧客検索
    - 顧客詳細取得
    - 顧客登録
    - 顧客更新
    - 問い合わせ履歴取得
    - 問い合わせ履歴要約

    AIアシスタントに許可したい操作:
    - 顧客検索
    - 顧客詳細参照
    - 問い合わせ履歴要約
    - 顧客更新案の作成

    承認必須操作:
    - 顧客情報更新
    - 顧客削除

### 6.3 入力バリデーション

初期MVPでは、以下のバリデーションを行う。

1. 業務要件が空でないこと
2. 対象ドメインが空でないこと
3. ユーザー種別が空でないこと
4. 必要な操作が空でないこと
5. AIアシスタントに許可したい操作が空でないこと
6. 入力文字数が上限を超えないこと
7. 書き込み操作が存在する場合、承認要否を確認すること

### 6.4 入力上限

初期MVPでは、以下の上限を想定する。

| 項目 | 上限 |
|---|---:|
| 業務要件 | 10,000文字 |
| 対象ドメイン | 100文字 |
| ユーザー種別 | 2,000文字 |
| 必要な操作 | 3,000文字 |
| AIアシスタントに許可したい操作 | 3,000文字 |
| その他補足 | 5,000文字 |

---

## 7. 出力仕様

### 7.1 出力成果物一覧

初期MVPの出力成果物は以下とする。

1. API設計サマリー
2. RESTエンドポイント一覧
3. Request / Response DTO候補
4. Spring Controller雛形
5. MCP tools一覧
6. MCP resources一覧
7. MCP prompts一覧
8. API / MCP tool 対応表
9. 権限・承認・監査ログの注意点
10. Markdown設計書
11. AI実装指示書

### 7.2 API設計サマリー

API設計サマリーには以下を含める。

- 対象ドメイン
- 主なアクター
- API設計方針
- 読み取り系API
- 書き込み系API
- 承認が必要なAPI
- 監査ログが必要なAPI
- 想定Controller構成

### 7.3 RESTエンドポイント一覧

RESTエンドポイント一覧には以下を含める。

| 項目 | 内容 |
|---|---|
| HTTPメソッド | GET / POST / PUT / PATCH / DELETE |
| パス | `/api/customers/{id}` 等 |
| 用途 | 顧客詳細取得等 |
| 主な利用者 | 管理者、営業担当等 |
| Request DTO | `CustomerUpdateRequest` 等 |
| Response DTO | `CustomerResponse` 等 |
| 認可 | ADMIN、SALES等 |
| 承認要否 | 不要 / 必須 |
| 監査ログ | 不要 / 必須 |

### 7.4 Request / Response DTO候補

DTO候補には以下を含める。

- DTO名
- 用途
- フィールド名
- 型
- 必須 / 任意
- バリデーション候補
- センシティブ項目かどうか

### 7.5 Spring Controller雛形

Spring Controller雛形は、完全実装ではなく、実装開始点として利用できる粒度とする。

含めるもの:

- `@RestController`
- `@RequestMapping`
- エンドポイントメソッド
- Request DTO / Response DTO 参照
- Service 呼び出し位置
- 認可コメント
- 承認チェックコメント
- 監査ログコメント

含めないもの:

- Service 完全実装
- Repository 実装
- Entity 実装
- 認証認可の実コード
- DBアクセスコード

### 7.6 MCP tools一覧

MCP tools一覧には以下を含める。

| 項目 | 内容 |
|---|---|
| tool名 | `searchCustomers` 等 |
| 目的 | 顧客検索 |
| 入力引数 | `keyword`, `status` 等 |
| 戻り値 | 顧客候補一覧 |
| 対応API | `GET /api/customers` |
| 操作区分 | read / write |
| AI実行可否 | 可 / 提案のみ / 承認後可 |
| 承認要否 | 不要 / 必須 |
| 監査ログ | 不要 / 必須 |

### 7.7 MCP resources一覧

MCP resources一覧には以下を含める。

- resource名
- 対象データ
- 説明
- 参照権限
- 更新可否
- センシティブ情報の有無
- 対応API
- 公開範囲

### 7.8 MCP prompts一覧

MCP prompts一覧には以下を含める。

- prompt名
- 利用場面
- 入力
- 出力
- 使用するtools
- 使用するresources
- 禁止事項
- 人間確認が必要な条件

### 7.9 API / MCP tool 対応表

API / MCP tool 対応表には以下を含める。

| API | MCP tool | 操作区分 | AI実行可否 | 承認要否 | 監査ログ |
|---|---|---|---|---|---|
| GET /api/customers | searchCustomers | read | 可 | 不要 | 任意 |
| PUT /api/customers/{id} | proposeCustomerUpdate | write | 提案のみ | 必須 | 必須 |

### 7.10 AI実装指示書

AI実装指示書には以下を含める。

- 実装目的
- 実装対象
- 実装しないこと
- Controller 実装方針
- DTO 実装方針
- Service 境界
- 認可方針
- 承認フロー方針
- 監査ログ方針
- MCP設計の実装展開方針
- テスト観点
- 成果物一覧
- 検証手順

---

## 8. 画面構成案

### 8.1 初期MVP画面一覧

初期MVPでは、以下の画面を想定する。

1. トップ / 入力画面
2. 生成結果画面
3. Markdown設計書プレビュー画面
4. AI実装指示書プレビュー画面
5. ヘルプ画面

### 8.2 トップ / 入力画面

入力画面には以下を配置する。

- 製品概要
- 入力フォーム
- サンプル入力
- 生成ボタン
- 注意事項
- 出力される成果物の説明

主なフォーム項目:

- 業務要件
- 対象ドメイン
- ユーザー種別
- 必要な操作
- AIアシスタントに許可したい操作
- 読み取り専用操作
- 書き込み許可操作
- 承認必須操作
- 監査ログが必要な操作
- 出力言語

### 8.3 生成結果画面

生成結果画面には以下を配置する。

- API設計サマリー
- RESTエンドポイント一覧
- MCP tools一覧
- MCP resources一覧
- MCP prompts一覧
- API / MCP 対応表
- セキュリティ注意点
- 承認・監査ログ注意点
- Markdown設計書コピー / ダウンロード導線
- AI実装指示書コピー / ダウンロード導線

### 8.4 Markdown設計書プレビュー画面

Markdown設計書プレビュー画面には、生成された設計書全体を表示する。

主な用途:

- 人間によるレビュー
- 設計書として保存
- GitHub docs への転記
- Codex 実装前レビュー

### 8.5 AI実装指示書プレビュー画面

AI実装指示書プレビュー画面には、Codex 等へ渡すための実装指示書を表示する。

主な用途:

- 実装AIへの投入前確認
- 実装対象 / 非対象の確認
- セキュリティ観点の確認
- テスト観点の確認

### 8.6 ヘルプ画面

ヘルプ画面には以下を含める。

- APIとMCPの違い
- 本ツールの使い方
- 入力例
- 出力例
- 初期MVPの制約
- 生成物の扱い
- セキュリティ注意事項
- AI実装指示書を使う際の注意点

---

## 9. API設計生成範囲

### 9.1 生成対象

API設計生成では、以下を生成対象とする。

1. Controller構成案
2. RESTエンドポイント一覧
3. Request DTO候補
4. Response DTO候補
5. バリデーション候補
6. HTTPステータス方針
7. 認可方針
8. 承認要否
9. 監査ログ要否
10. Spring Controller雛形

### 9.2 Controller設計方針

Controllerは、対象ドメイン単位で分割する。

例:

- `CustomerController`
- `OrderController`
- `InventoryController`
- `ApprovalController`

Controllerには、業務ロジックを直接書かず、Service層へ委譲する前提で雛形を生成する。

### 9.3 エンドポイント設計方針

エンドポイントは、RESTの一般的な設計方針に従う。

例:

- 一覧取得: `GET /api/customers`
- 詳細取得: `GET /api/customers/{id}`
- 新規作成: `POST /api/customers`
- 更新: `PUT /api/customers/{id}`
- 部分更新: `PATCH /api/customers/{id}`
- 削除: `DELETE /api/customers/{id}`
- 承認依頼: `POST /api/customers/{id}/approval-requests`
- 承認実行: `POST /api/approval-requests/{id}/approve`

### 9.4 DTO設計方針

DTOは、Entityとは分離する前提で生成する。

生成するDTO候補:

- `SearchRequest`
- `CreateRequest`
- `UpdateRequest`
- `Response`
- `SummaryResponse`
- `ApprovalRequest`
- `ApprovalResponse`

センシティブ情報を含む項目は、DTO設計時に明示する。

### 9.5 API設計で必ず含める注意点

API設計では、以下を必ず出力する。

- 認証が必要なエンドポイント
- 管理者権限が必要なエンドポイント
- AIから直接実行させるべきでないエンドポイント
- 承認フローを挟むべきエンドポイント
- 監査ログが必要なエンドポイント
- 個人情報・機密情報を含む可能性があるレスポンス
- DELETE系操作の扱い

---

## 10. MCP設計生成範囲

### 10.1 生成対象

MCP設計生成では、以下を生成対象とする。

1. MCP tools一覧
2. MCP resources一覧
3. MCP prompts一覧
4. API / MCP tool 対応表
5. AI実行可否分類
6. 承認要否分類
7. 監査ログ要否分類
8. 禁止操作一覧
9. 将来のMCPサーバー実装メモ

### 10.2 tools設計方針

tools は、AIが実行可能な業務操作として設計する。

tool名は、動詞 + 対象名で命名する。

例:

- `searchCustomers`
- `getCustomerDetail`
- `summarizeCustomerInteractions`
- `createOrderDraft`
- `proposeCustomerUpdate`
- `requestApprovalForCustomerUpdate`

### 10.3 resources設計方針

resources は、AIが参照可能な業務データまたは文書として設計する。

例:

- `customerProfile`
- `customerInteractionHistory`
- `productCatalog`
- `orderSummary`
- `businessRuleGuide`

resources は、原則として読み取り専用とし、更新は tools 経由で扱う。

### 10.4 prompts設計方針

prompts は、AIが特定業務を安全に遂行するための定型指示として設計する。

例:

- `customerSupportAssistantPrompt`
- `orderChangeReviewPrompt`
- `approvalPreparationPrompt`
- `sensitiveDataHandlingPrompt`

prompts には、禁止事項と人間確認条件を含める。

### 10.5 AI実行可否分類

MCP tool には、以下の実行可否分類を付与する。

| 分類 | 意味 |
|---|---|
| AI実行可 | AIが直接実行してよい |
| 提案のみ | AIは変更案の作成まで行い、実行は人間が行う |
| 承認後実行可 | 人間承認後に実行してよい |
| AI実行不可 | AIからは利用させない |

### 10.6 MCP設計で必ず含める注意点

MCP設計では、以下を必ず出力する。

- AIに許可する操作
- AIに許可しない操作
- 読み取り専用操作
- 書き込み操作
- 承認必須操作
- 監査ログ必須操作
- センシティブ情報を扱うresources
- 人間確認が必要な条件
- toolの誤用リスク

---

## 11. セキュリティ・承認・監査ログ設計

### 11.1 基本方針

本プロダクトは、API / MCP 設計を生成する段階から、セキュリティ・承認・監査ログ観点を組み込む。

特に MCP は AIエージェントが業務操作を実行する入口になり得るため、通常のAPI設計よりも、誤操作・過剰操作・権限逸脱への注意が必要である。

### 11.2 セキュリティ設計観点

出力には、以下の観点を含める。

1. 認証方式
2. ロールベース認可
3. エンドポイント別権限
4. MCP tool別権限
5. センシティブ情報の扱い
6. 入力バリデーション
7. 出力フィルタリング
8. レート制限
9. CSRF / CORS 等のWeb観点
10. AIによる誤操作対策

### 11.3 承認設計観点

承認設計では、以下を分類する。

| 操作区分 | 承認要否 |
|---|---|
| 読み取り | 原則不要 |
| 検索 | 原則不要 |
| 要約 | 原則不要。ただし機密情報を含む場合は注意 |
| 新規作成 | 業務影響により判断 |
| 更新 | 原則として承認検討 |
| 削除 | 原則承認必須 |
| 外部送信 | 原則承認必須 |
| 権限変更 | 承認必須 |
| 金銭・契約・顧客影響操作 | 承認必須 |

### 11.4 監査ログ設計観点

監査ログには、以下を残す設計を推奨する。

- 操作者
- 操作主体が人間かAIか
- 実行されたAPI
- 実行されたMCP tool
- 入力パラメータ概要
- 対象リソースID
- 実行結果
- 承認者
- 承認日時
- 実行日時
- エラー内容
- AIが生成した提案内容への参照

### 11.5 AI操作に関する追加注意

AI操作では、以下を明記する。

- AIに直接削除を許可しない
- AIに権限変更を直接許可しない
- AIによる外部送信は人間確認を挟む
- 個人情報や機密情報を含むresourceは最小化する
- AIが取得できるデータ範囲を業務上必要な範囲に限定する
- AI操作は人間操作と区別してログに残す

---

## 12. Spring-Tool-Development-Template 適用方針

### 12.1 適用前提

本プロダクトは、Spring-Tool-Development-Template を新規リポジトリの開発運用基盤として適用する前提で立ち上げる。

同テンプレートは、Spring系ツール開発に共通する以下を提供する。

- README
- PROJECT_START_PROMPT_TEMPLATE
- docs階層
- 改善タスク管理
- AI作業証跡管理
- Codex連携運用ルール
- GitHub Actions による初期チェック
- PRテンプレート

### 12.2 適用範囲

初期適用では、以下を利用する。

1. ルートREADME
2. PROJECT_START_PROMPT_TEMPLATE
3. docs/README.md
4. docs/00_プロジェクト管理
5. docs/10_企画
6. docs/20_要件定義
7. docs/30_基本設計
8. docs/40_詳細設計
9. 改善タスク管理
10. AI_REPO_RESULT 運用
11. Codex連携運用ルール
12. PRテンプレート

### 12.3 置換変数方針

テンプレート適用時の主な変数は以下とする。

| 変数 | 値 |
|---|---|
| `{{PROJECT_NAME}}` | API MCP Blueprint for Spring |
| `{{PRODUCT_NAME}}` | API + MCP Blueprint Compiler for Spring |
| `{{REPOSITORY_FULL_NAME}}` | yamaguchiyoshishigeai-create/api-mcp-blueprint-for-spring |
| `{{DEFAULT_BRANCH}}` | main |
| `{{PRODUCT_CATEGORY}}` | Spring向け API / MCP 設計生成支援ツール |
| `{{TOOL_DESCRIPTION}}` | 業務要件からREST API設計とMCP設計を同時生成するWebツール |
| `{{TARGET_FRAMEWORK}}` | Spring Boot / Spring Framework |
| `{{PRIMARY_RUNTIME}}` | Java 17 |
| `{{BUILD_TOOL}}` | Maven |
| `{{MAIN_APPLICATION_CLASS}}` | 後続基本設計で確定 |
| `{{CLI_MAIN_CLASS}}` | 初期MVPでは未使用 |
| `{{HUMAN_REPORT_FILE}}` | api-mcp-blueprint.md |
| `{{AI_INPUT_FILE}}` | implementation-instructions.md |

### 12.4 テンプレート適用時の注意

本プロダクトは設計生成系であり、Q-Scout のような診断・スコアリング系とは異なる。

そのため、テンプレート適用時には以下を明確化する。

- 診断ルールという概念は使わない
- スコアリングという概念は使わない
- 入力はコードZIPではなく業務要件テキストである
- 出力は診断レポートではなく設計書である
- AI向け成果物は「改善相談用」ではなく「実装指示用」である

---

## 13. 新規リポジトリ名案

### 13.1 候補一覧

#### 提案名: APIM-001-RN01 api-mcp-blueprint-for-spring

    api-mcp-blueprint-for-spring

評価:

- API / MCP / Spring がすべて含まれる
- 設計書生成ツールであることが分かりやすい
- 長すぎない
- 製品コンセプトと一致する

#### 提案名: APIM-001-RN02 spring-api-mcp-blueprint-builder

    spring-api-mcp-blueprint-builder

評価:

- Spring起点で分かりやすい
- Builder という語により生成支援ツール感がある
- やや長い

#### 提案名: APIM-001-RN03 api-mcp-compiler-for-spring

    api-mcp-compiler-for-spring

評価:

- Compiler という製品名と整合する
- 技術的で印象に残る
- 設計書生成という意味がやや伝わりにくい可能性がある

### 13.2 推奨リポジトリ名

推奨は以下とする。

    api-mcp-blueprint-for-spring

理由:

1. API / MCP / Spring の3要素が自然に含まれる
2. Blueprint により「設計図生成」ツールであることが伝わる
3. Compiler よりも初見で分かりやすい
4. MVP段階の「設計書生成」に適している
5. 将来、実装生成やOpenAPI出力へ広げても違和感が少ない

### 13.3 推奨製品名

推奨製品名は以下とする。

    API + MCP Blueprint Compiler for Spring

理由:

- ポートフォリオ上の見栄えがよい
- API と MCP を同時に扱う特徴が明確
- Compiler により「自然言語要件を構造化設計へ変換する」印象を出せる
- リポジトリ名よりも製品名として訴求力が高い

---

## 14. ChatGPT / Codex / ユーザー作業分担

### 14.1 ChatGPT(会話内)

担当:

- 立ち上げ方針書の作成
- 製品コンセプト整理
- Q-Scoutとの差別化整理
- MVP範囲定義
- 入力仕様・出力仕様の整理
- 画面構成案作成
- セキュリティ・承認・監査ログ観点整理
- Codex向け指示書作成
- ユーザー判断事項の整理

理由:

- 文書設計、方針整理、比較検討、プロンプト作成が中心であり、会話内で完結しやすいため。

### 14.2 ChatGPT(リポジトリ編集)

担当候補:

- 新規リポジトリ作成後の小規模 docs 追加
- README の軽微な修正
- 方針書の配置
- 改善タスク登録
- テンプレート置換後の軽微な整合修正

理由:

- 小〜中規模の文書編集であり、対象ファイルを完全取得できる場合は ChatGPT 側で対応可能なため。

### 14.3 Codex

担当候補:

- Spring-Tool-Development-Template の新規リポジトリ適用
- 多数ファイルの生成
- 変数置換
- docs階層の作成
- 初期README作成
- GitHub Actions / scripts の整合確認
- Spring Boot Web MVP の実装
- 生成ロジックの実装
- Controller / Service / template の複数ファイル実装
- テスト実行
- PR作成

理由:

- 複数ファイル生成、実装、テスト、PR作成を伴うため、Codex が適している。

### 14.4 ユーザー(人)

担当:

- 製品名の最終決定
- リポジトリ名の最終決定
- Public / Private の最終判断
- GitHub上での新規リポジトリ作成
- main への merge 判断
- MVP範囲の最終承認
- ポートフォリオ上の表示方針決定

理由:

- 命名、公開範囲、事業判断、最終mergeは人間判断が必要なため。

---

## 15. 初期フェーズ計画

### Phase 1: 立ち上げ方針確定

作業主体:

    ChatGPT(会話内)

成果物:

    APIM-001 API + MCP Blueprint Compiler for Spring 立ち上げ方針書 v0.1

完了条件:

- 製品コンセプトが整理されている
- Q-Scoutとの差別化が整理されている
- MVP範囲が定義されている
- 入出力仕様が定義されている
- 作業分担が定義されている
- 次のユーザー作業が明確である

### Phase 2: 新規リポジトリ作成判断

作業主体:

    ユーザー(人)

実施内容:

- リポジトリ名を決定する
- Public / Private を決定する
- GitHub上に新規リポジトリを作成する

推奨:

    repository: api-mcp-blueprint-for-spring
    visibility: Private start recommended

理由:

- 初期段階では設計・プロンプト・生成ロジックの試行錯誤が多いため
- 後続で公開用READMEやサンプルを整備してからPublic化判断する方が安全なため

### Phase 3: テンプレート適用指示書作成

作業主体:

    ChatGPT(会話内)

成果物:

    APIM-002 Codex向け API + MCP Blueprint Compiler for Spring 初期リポジトリ生成指示書 v0.1

内容:

- Spring-Tool-Development-Template の適用
- 変数置換一覧
- 初期docs構成
- APIM-001 方針書配置
- 初期改善タスク登録
- 実装コード非対象の明記
- PR作成方針

### Phase 4: 初期リポジトリ生成

作業主体:

    Codex

実施内容:

- テンプレート適用
- docs配置
- README作成
- PROJECT_START_PROMPT作成
- 改善タスク初期化
- GitHub Actions / scripts 確認
- PR作成

### Phase 5: 要件定義・基本設計

作業主体:

    ChatGPT(会話内) / Codex

成果物候補:

- APIM-003 MVP最小要件定義書 v0.1
- APIM-004 全体設計方針書 v0.1
- APIM-005 基本設計書 v0.1
- APIM-006 Codex向け Web MVP 実装指示書 v0.1

---

## 16. 初期リスクと対策

### 16.1 MCP範囲が膨らむリスク

リスク:

- MCPサーバー実装、認証、tool実行、LLM接続まで含めるとMVPが重くなる。

対策:

- 初期MVPでは MCP 設計書生成に限定する。
- 完全動作するMCPサーバーは対象外と明記する。
- tools / resources / prompts 定義案までに絞る。

### 16.2 API生成が実装生成へ膨らむリスク

リスク:

- Controller、Service、DTO、Entity、Repository、テスト、OpenAPIまで完全生成しようとして範囲が膨らむ。

対策:

- 初期MVPでは Controller雛形とDTO候補に留める。
- Service / Repository / Entity 完全実装は対象外とする。
- 実装指示書生成を主成果物にする。

### 16.3 セキュリティ上危険な設計を生成するリスク

リスク:

- AIに書き込み・削除・外部送信を過剰に許可する設計が生成される。

対策:

- 書き込み操作には承認要否を必ず付与する。
- 削除・権限変更・外部送信は原則承認必須にする。
- AI実行可否分類を必須出力にする。
- 監査ログ要否を必須出力にする。

### 16.4 Q-Scoutと似た診断系に寄ってしまうリスク

リスク:

- 設計生成系ではなく、また診断・解析系プロダクトになってしまう。

対策:

- 入力を既存コードではなく業務要件に固定する。
- 出力を診断レポートではなく設計書に固定する。
- スコアリングやルール検出を初期MVPから除外する。

### 16.5 テンプレート適用後に文書が重くなるリスク

リスク:

- Spring-Tool-Development-Template のフルセットが初期MVPに対して重く感じられる。

対策:

- 初期は 00 / 10 / 20 / 30 / 40 の主要文書に集中する。
- 50 / 60 / 70 はプレースホルダとして扱う。
- 改善タスクは最小件数から開始する。

---

## 17. 成功条件

APIM-001 の成功条件は以下である。

1. 製品コンセプトが明確である
2. Q-Scout for Spring との差別化が明確である
3. API と MCP の役割分担が明確である
4. 初期MVP範囲が過大でない
5. 入力仕様が具体化されている
6. 出力仕様が具体化されている
7. 画面構成案が定義されている
8. API設計生成範囲が定義されている
9. MCP設計生成範囲が定義されている
10. セキュリティ・承認・監査ログ観点が必須化されている
11. Spring-Tool-Development-Template 適用方針が明確である
12. 新規リポジトリ名の推奨案が明確である
13. ChatGPT / Codex / ユーザーの作業分担が明確である
14. 次のユーザー作業が明確である

---

## 18. 次のユーザー作業

### 18.1 最優先の判断事項

次にユーザーが判断する事項は以下である。

1. 製品名を以下で進めるか

       API + MCP Blueprint Compiler for Spring

2. リポジトリ名を以下で進めるか

       api-mcp-blueprint-for-spring

3. 新規リポジトリを Private で作成するか

4. Spring-Tool-Development-Template を開発運用基盤として適用するか

### 18.2 推奨判断

現時点の推奨は以下である。

| 項目 | 推奨 |
|---|---|
| 製品名 | API + MCP Blueprint Compiler for Spring |
| リポジトリ名 | api-mcp-blueprint-for-spring |
| 公開範囲 | Private start |
| 開発基盤 | Spring-Tool-Development-Template を適用 |
| 次成果物 | APIM-002 Codex向け初期リポジトリ生成指示書 |

### 18.3 ユーザー作業後に進める内容

ユーザーが新規リポジトリを作成した後、次に作成すべき文書は以下である。

    APIM-002 Codex向け API + MCP Blueprint Compiler for Spring 初期リポジトリ生成指示書 v0.1

APIM-002 では、以下を定義する。

- 対象リポジトリ
- 対象ブランチ
- Spring-Tool-Development-Template 適用方法
- 変数置換一覧
- 初期ファイル配置
- APIM-001 方針書の配置先
- 初期改善タスク
- Codexで実施すること
- Codexで実施しないこと
- 検証手順
- PR作成方針
- AI_REPO_RESULT 出力形式

---

## 19. 最終結論

**API + MCP Blueprint Compiler for Spring** は、Q-Scout for Spring と競合しない新規ポートフォリオ候補として有力である。

Q-Scout が「既存Springシステムを診断する」プロダクトであるのに対し、本プロダクトは「新規業務要件から API / MCP 設計を生成する」プロダクトである。

両者を組み合わせることで、以下のポートフォリオ導線を形成できる。

    設計する
      → API + MCP Blueprint Compiler for Spring

    実装する
      → Codex / Spring-Tool-Development-Template / Spring Boot

    診断する
      → Q-Scout for Spring

初期MVPでは、完全動作する MCP サーバーや Spring Boot アプリの自動生成には踏み込まず、API設計書、MCP設計書、API / MCP 対応表、Spring Controller雛形、AI実装指示書を生成する設計コンパイラとして開始する。

また、セキュリティ、認可、承認、人間確認、監査ログの観点を必須出力に含めることで、単なるAPI雛形生成ツールではなく、AI時代の業務システム設計支援ツールとして差別化できる。

新規リポジトリ名は `api-mcp-blueprint-for-spring` を推奨する。

次フェーズでは、ユーザーが新規リポジトリ作成方針を決定したうえで、`APIM-002 Codex向け API + MCP Blueprint Compiler for Spring 初期リポジトリ生成指示書 v0.1` を作成する。

以上を、APIM-001 API + MCP Blueprint Compiler for Spring 立ち上げ方針書 v0.1 とする。


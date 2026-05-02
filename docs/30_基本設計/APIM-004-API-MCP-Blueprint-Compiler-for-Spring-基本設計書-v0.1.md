# APIM-004 API + MCP Blueprint Compiler for Spring 基本設計書 v0.1

## 1. 文書概要

### 1.1 文書目的

本書は、**API + MCP Blueprint Compiler for Spring** の初期Web MVPを実装するための基本設計を定義する文書である。

APIM-003「MVP最小要件定義書 v0.1」では、初期MVPを「自然言語の業務要件から、API設計とMCP設計を同時に生成する設計支援Webツール」として定義した。

本書では、その要件を受けて、以下を基本設計として具体化する。

- 全体アーキテクチャ
- 画面構成
- Spring MVC構成
- パッケージ構成
- Controller / Service / DTO / View の責務
- API設計生成ロジック
- MCP設計生成ロジック
- Markdown設計書生成ロジック
- AI実装指示書生成ロジック
- 入力バリデーション
- エラー処理
- セキュリティ・承認・監査ログ観点
- テスト方針
- Codex実装時の境界条件

### 1.2 文書位置づけ

| 項目 | 内容 |
|---|---|
| 文書ID | APIM-004 |
| 文書名 | API + MCP Blueprint Compiler for Spring 基本設計書 v0.1 |
| 上位文書 | APIM-001 立ち上げ方針書 / APIM-003 MVP最小要件定義書 |
| 後続文書 | APIM-005 Codex向け Web MVP 実装指示書 |
| 配置候補 | `docs/30_基本設計/APIM-004-API-MCP-Blueprint-Compiler-for-Spring-基本設計書-v0.1.md` |
| 主担当 | ChatGPT(会話内) |
| 実装担当候補 | Codex |

### 1.3 基本前提

APIM for Spring 自体は **Spring Boot Webアプリケーション** として実装する。  
ただし、本ツールが生成する成果物としては、完全動作するSpring Bootアプリケーションや完全動作するMCPサーバーを生成しない。

初期MVPの生成対象は以下に限定する。

- API設計サマリー
- RESTエンドポイント一覧
- Request / Response DTO候補
- Spring Controller雛形
- MCP tools / resources / prompts 定義案
- API / MCP tool 対応表
- セキュリティ・承認・監査ログ注意点
- Markdown設計書
- AI実装指示書

---

## 2. 基本方針

### 2.1 設計方針

初期MVPでは、以下を基本方針とする。

1. Spring Boot Webアプリとして実装する。
2. サーバーサイドテンプレートによる画面表示を基本とする。
3. 入力値からルールベース / テンプレートベースで設計成果物を生成する。
4. 外部LLM API連携は初期必須としない。
5. DB永続化は初期対象外とする。
6. 生成結果はリクエストまたはセッションスコープで扱う。
7. 生成成果物はMarkdown文字列として構成する。
8. Controller雛形は実装開始点としてのコードブロックに留める。
9. MCP出力はtools / resources / prompts の設計案に留める。
10. セキュリティ・承認・監査ログ注意点を必ず生成する。

### 2.2 初期アーキテクチャ

    Browser
      ↓
    Spring MVC Controller
      ↓
    BlueprintGenerationService
      ↓
    Generator Services
      ├─ ApiDesignGenerator
      ├─ DtoCandidateGenerator
      ├─ ControllerSkeletonGenerator
      ├─ McpDesignGenerator
      ├─ SecurityNotesGenerator
      ├─ MarkdownDocumentGenerator
      └─ ImplementationInstructionGenerator
      ↓
    Thymeleaf Views

### 2.3 初期MVPで採用しない構成

| 対象 | 理由 |
|---|---|
| SPAフロントエンド | MVP範囲を抑えるため |
| DB永続化 | 入力内容に機密情報が含まれる可能性があるため |
| 外部LLM API必須構成 | APIキー管理・コスト・再現性の課題を避けるため |
| MCPサーバー実装 | 初期MVPでは設計案生成に限定するため |
| OpenAPI完全生成 | MVPではエンドポイント一覧に限定するため |
| 認証認可の本格実装 | 初期MVPではローカル・設計検証用途を優先するため |

---

## 3. システム構成

### 3.1 実行形態

| 項目 | 内容 |
|---|---|
| 実行形式 | Spring Boot Webアプリ |
| 想定実行環境 | ローカル開発環境 |
| Java | Java 17 |
| ビルド | Maven |
| DB | 使用しない |
| 外部API | 初期必須ではない |
| 画面 | サーバーサイドテンプレート |
| 生成方式 | ルールベース + テンプレートベース |

### 3.2 技術スタック候補

| 区分 | 候補 |
|---|---|
| 言語 | Java 17 |
| フレームワーク | Spring Boot |
| Web | Spring MVC |
| View | Thymeleaf |
| Validation | Jakarta Bean Validation |
| Build | Maven |
| Test | JUnit 5 / Spring Boot Test |
| Static resource | CSS / JavaScript最小限 |
| Markdown | 文字列生成 / テンプレート生成 |

### 3.3 初期ディレクトリ構成案

    src/main/java/com/example/apim/
      ApimApplication.java
      controller/
        BlueprintController.java
      service/
        BlueprintGenerationService.java
        ApiDesignGenerator.java
        DtoCandidateGenerator.java
        ControllerSkeletonGenerator.java
        McpDesignGenerator.java
        SecurityNotesGenerator.java
        MarkdownDocumentGenerator.java
        ImplementationInstructionGenerator.java
      model/
        BlueprintInput.java
        BlueprintResult.java
        ApiEndpointCandidate.java
        DtoCandidate.java
        DtoFieldCandidate.java
        ControllerSkeleton.java
        McpToolCandidate.java
        McpResourceCandidate.java
        McpPromptCandidate.java
        ApiMcpMapping.java
        SecurityNote.java
      support/
        DomainNameNormalizer.java
        OperationClassifier.java
        NamingSupport.java

    src/main/resources/
      templates/
        index.html
        result.html
        blueprint-preview.html
        implementation-instructions-preview.html
        help.html
      static/
        css/
          app.css

    src/test/java/com/example/apim/
      service/
        BlueprintGenerationServiceTest.java
        ApiDesignGeneratorTest.java
        McpDesignGeneratorTest.java
        SecurityNotesGeneratorTest.java
        MarkdownDocumentGeneratorTest.java

---

## 4. 画面設計

### 4.1 画面一覧

| 画面ID | 画面名 | URL候補 | View |
|---|---|---|---|
| UI-001 | トップ / 入力画面 | `/` | `index.html` |
| UI-002 | 生成結果画面 | `/blueprint/generate` | `result.html` |
| UI-003 | Markdown設計書プレビュー | `/blueprint/preview` | `blueprint-preview.html` |
| UI-004 | AI実装指示書プレビュー | `/blueprint/implementation-instructions` | `implementation-instructions-preview.html` |
| UI-005 | ヘルプ画面 | `/help` | `help.html` |

### 4.2 UI-001 トップ / 入力画面

目的は、業務要件、対象ドメイン、ユーザー種別、必要な操作、AIアシスタントに許可したい操作を入力することである。

表示要素:

- 製品名
- 製品概要
- 初期MVPで生成するもの
- 初期MVPで生成しないもの
- 入力フォーム
- サンプル入力ボタン
- 生成ボタン
- ヘルプ画面へのリンク

入力項目:

| 項目 | name候補 | 必須 | UI部品 |
|---|---|---:|---|
| 業務要件 | `businessRequirements` | 必須 | textarea |
| 対象ドメイン | `targetDomain` | 必須 | input text |
| ユーザー種別 | `userTypes` | 必須 | textarea |
| 必要な操作 | `requiredOperations` | 必須 | textarea |
| AIアシスタントに許可したい操作 | `allowedAiOperations` | 必須 | textarea |
| 読み取り専用操作 | `readOnlyOperations` | 任意 | textarea |
| 書き込み許可操作 | `writeOperations` | 任意 | textarea |
| 承認必須操作 | `approvalRequiredOperations` | 任意 | textarea |
| 監査ログが必要な操作 | `auditLogRequiredOperations` | 任意 | textarea |
| 想定認証方式 | `authenticationMethod` | 任意 | input text |
| 想定利用者 | `targetUsers` | 任意 | input text |
| 出力言語 | `outputLanguage` | 任意 | select |

### 4.3 UI-002 生成結果画面

目的は、生成されたAPI設計、MCP設計、対応表、注意点、Controller雛形を確認することである。

表示領域:

- 入力要約
- API設計サマリー
- RESTエンドポイント一覧
- DTO候補
- Spring Controller雛形
- MCP tools一覧
- MCP resources一覧
- MCP prompts一覧
- API / MCP tool 対応表
- セキュリティ・承認・監査ログ注意点
- Markdown設計書プレビュー導線
- AI実装指示書プレビュー導線
- コピー / ダウンロード導線

### 4.4 UI-003 Markdown設計書プレビュー

生成された `api-mcp-blueprint.md` 相当のMarkdown設計書を確認・コピー・ダウンロードする画面である。

### 4.5 UI-004 AI実装指示書プレビュー

生成された `implementation-instructions.md` 相当のAI実装指示書を確認・コピー・ダウンロードする画面である。

### 4.6 UI-005 ヘルプ画面

APIM for Spring の利用方法、APIとMCPの違い、初期MVPの制約を説明する画面である。

---

## 5. Controller設計

### 5.1 Controller一覧

| Controller | 役割 |
|---|---|
| `BlueprintController` | 入力画面、生成処理、結果表示、プレビュー表示を扱う |

初期MVPではControllerを1つに集約する。  
画面・機能が増えた段階で、`HelpController` や `DownloadController` に分離する。

### 5.2 BlueprintController

担当:

- 入力画面表示
- 入力バリデーション
- 生成処理呼び出し
- 生成結果画面表示
- Markdown設計書プレビュー表示
- AI実装指示書プレビュー表示
- ヘルプ画面表示

エンドポイント案:

| HTTP | Path | 用途 |
|---|---|---|
| GET | `/` | 入力画面表示 |
| POST | `/blueprint/generate` | 生成処理実行 |
| GET | `/blueprint/preview` | Markdown設計書プレビュー |
| GET | `/blueprint/implementation-instructions` | AI実装指示書プレビュー |
| GET | `/help` | ヘルプ表示 |

POST `/blueprint/generate` の処理フロー:

1. `BlueprintInput` を受け取る。
2. Bean Validation を実行する。
3. 入力エラーがあれば `index.html` に戻す。
4. `BlueprintGenerationService` を呼び出す。
5. `BlueprintResult` を取得する。
6. 生成結果をModelまたはSessionへ格納する。
7. `result.html` を返す。

---

## 6. Model設計

### 6.1 BlueprintInput

| フィールド | 型 | 必須 | 説明 |
|---|---|---:|---|
| `businessRequirements` | String | 必須 | 業務要件 |
| `targetDomain` | String | 必須 | 対象ドメイン |
| `userTypes` | String | 必須 | ユーザー種別 |
| `requiredOperations` | String | 必須 | 必要な操作 |
| `allowedAiOperations` | String | 必須 | AIに許可する操作 |
| `readOnlyOperations` | String | 任意 | 読み取り専用操作 |
| `writeOperations` | String | 任意 | 書き込み許可操作 |
| `approvalRequiredOperations` | String | 任意 | 承認必須操作 |
| `auditLogRequiredOperations` | String | 任意 | 監査ログ必要操作 |
| `authenticationMethod` | String | 任意 | 想定認証方式 |
| `targetUsers` | String | 任意 | 想定利用者 |
| `outputLanguage` | String | 任意 | 出力言語 |

### 6.2 BlueprintResult

| フィールド | 型 | 説明 |
|---|---|---|
| `inputSummary` | String | 入力内容の要約 |
| `apiDesignSummary` | String | API設計サマリー |
| `apiEndpoints` | List<ApiEndpointCandidate> | RESTエンドポイント候補 |
| `dtoCandidates` | List<DtoCandidate> | DTO候補 |
| `controllerSkeleton` | ControllerSkeleton | Controller雛形 |
| `mcpTools` | List<McpToolCandidate> | MCP tools候補 |
| `mcpResources` | List<McpResourceCandidate> | MCP resources候補 |
| `mcpPrompts` | List<McpPromptCandidate> | MCP prompts候補 |
| `apiMcpMappings` | List<ApiMcpMapping> | API/MCP対応表 |
| `securityNotes` | List<SecurityNote> | セキュリティ・承認・監査ログ注意点 |
| `blueprintMarkdown` | String | Markdown設計書 |
| `implementationInstructions` | String | AI実装指示書 |

### 6.3 ApiEndpointCandidate

| フィールド | 型 | 説明 |
|---|---|---|
| `httpMethod` | String | GET / POST / PUT / PATCH / DELETE |
| `path` | String | APIパス |
| `purpose` | String | 用途 |
| `actors` | String | 主な利用者 |
| `requestDto` | String | Request DTO候補 |
| `responseDto` | String | Response DTO候補 |
| `authorization` | String | 認可方針 |
| `approvalRequired` | String | 承認要否 |
| `auditLogRequired` | String | 監査ログ要否 |

### 6.4 DtoCandidate / DtoFieldCandidate

`DtoCandidate` は DTO名、用途、フィールド候補を保持する。  
`DtoFieldCandidate` はフィールド名、Java型候補、必須可否、バリデーション候補、センシティブ項目可否を保持する。

### 6.5 McpToolCandidate

| フィールド | 型 | 説明 |
|---|---|---|
| `name` | String | tool名 |
| `purpose` | String | 目的 |
| `arguments` | String | 入力引数 |
| `returnValue` | String | 戻り値 |
| `relatedApi` | String | 対応API |
| `operationType` | String | read / write |
| `aiExecutionPolicy` | String | AI実行可 / 提案のみ / 承認後可 / 不可 |
| `approvalRequired` | String | 承認要否 |
| `auditLogRequired` | String | 監査ログ要否 |

### 6.6 McpResourceCandidate / McpPromptCandidate

`McpResourceCandidate` はAIが参照する業務データ候補を表す。  
`McpPromptCandidate` はAIが特定業務を遂行するための定型プロンプト候補を表す。

---

## 7. Service設計

### 7.1 Service一覧

| Service | 役割 |
|---|---|
| `BlueprintGenerationService` | 生成処理全体を統括する |
| `ApiDesignGenerator` | API設計サマリーとエンドポイント候補を生成する |
| `DtoCandidateGenerator` | DTO候補を生成する |
| `ControllerSkeletonGenerator` | Spring Controller雛形を生成する |
| `McpDesignGenerator` | MCP tools / resources / prompts を生成する |
| `SecurityNotesGenerator` | セキュリティ・承認・監査ログ注意点を生成する |
| `MarkdownDocumentGenerator` | Markdown設計書を生成する |
| `ImplementationInstructionGenerator` | AI実装指示書を生成する |

### 7.2 BlueprintGenerationService

生成処理全体のオーケストレーションを担当する。

処理順序:

1. 入力の正規化
2. 操作語の抽出
3. 操作分類
4. API設計生成
5. DTO候補生成
6. Controller雛形生成
7. MCP設計生成
8. セキュリティ注意点生成
9. Markdown設計書生成
10. AI実装指示書生成
11. `BlueprintResult` 組み立て

疑似コード:

    public BlueprintResult generate(BlueprintInput input) {
        NormalizedInput normalized = normalize(input);
        List<Operation> operations = operationClassifier.classify(normalized);

        List<ApiEndpointCandidate> endpoints = apiDesignGenerator.generate(normalized, operations);
        List<DtoCandidate> dtos = dtoCandidateGenerator.generate(normalized, endpoints);
        ControllerSkeleton controller = controllerSkeletonGenerator.generate(normalized, endpoints, dtos);

        McpDesignResult mcp = mcpDesignGenerator.generate(normalized, endpoints, operations);
        List<SecurityNote> securityNotes = securityNotesGenerator.generate(normalized, endpoints, mcp);

        String blueprintMarkdown = markdownDocumentGenerator.generate(normalized, endpoints, dtos, controller, mcp, securityNotes);
        String implementationInstructions = implementationInstructionGenerator.generate(normalized, endpoints, dtos, controller, mcp, securityNotes);

        return new BlueprintResult(...);
    }

---

## 8. 生成ロジック設計

### 8.1 API候補生成ルール

| 操作語 | HTTPメソッド | パス候補 |
|---|---|---|
| 検索 / 一覧 | GET | `/api/{domain}` |
| 詳細取得 / 参照 | GET | `/api/{domain}/{id}` |
| 登録 / 作成 | POST | `/api/{domain}` |
| 更新 | PUT / PATCH | `/api/{domain}/{id}` |
| 削除 | DELETE | `/api/{domain}/{id}` |
| 承認依頼 | POST | `/api/{domain}/{id}/approval-requests` |
| 承認 | POST | `/api/approval-requests/{id}/approve` |
| 却下 | POST | `/api/approval-requests/{id}/reject` |

### 8.2 DTO命名方針

| 用途 | DTO名候補 |
|---|---|
| 検索 | `{Domain}SearchRequest` |
| 一覧レスポンス | `{Domain}SummaryResponse` |
| 詳細レスポンス | `{Domain}Response` |
| 作成 | `{Domain}CreateRequest` |
| 更新 | `{Domain}UpdateRequest` |
| 承認依頼 | `{Domain}ApprovalRequest` |
| 承認結果 | `ApprovalResponse` |

### 8.3 Controller雛形生成方針

Controller雛形には以下を含める。

- package宣言候補
- import候補
- `@RestController`
- `@RequestMapping`
- APIごとのメソッド
- Request DTO / Response DTO参照
- Service呼び出し位置
- 認可コメント
- 承認チェックコメント
- 監査ログコメント

完全なコンパイル成功は必須としない。

### 8.4 MCP tool命名方針

| 操作 | tool名候補 |
|---|---|
| 検索 | `search{Domain}` |
| 詳細取得 | `get{Domain}Detail` |
| 要約 | `summarize{Domain}` |
| 登録 | `create{Domain}Draft` |
| 更新 | `propose{Domain}Update` |
| 削除 | `request{Domain}DeletionApproval` |
| 承認依頼 | `requestApprovalFor{Domain}` |

### 8.5 AI実行可否分類

| 操作区分 | 初期判定 |
|---|---|
| 読み取り | AI実行可 |
| 検索 | AI実行可 |
| 要約 | AI実行可。ただし機密情報注意 |
| 登録 | 下書き作成または承認後実行 |
| 更新 | 提案のみまたは承認後実行 |
| 削除 | 原則AI実行不可 / 承認必須 |
| 外部送信 | 原則承認必須 |
| 権限変更 | AI実行不可 / 承認必須 |

### 8.6 SecurityNotesGenerator

以下の操作を検出した場合、セキュリティ・承認・監査ログ注意点を出力する。

- 削除
- 更新
- 外部送信
- 権限変更
- 個人情報参照
- 機密情報参照
- 金銭・契約・顧客影響操作
- AIによる自動実行

---

## 9. Markdown / AI実装指示書生成設計

### 9.1 Markdown設計書

出力ファイル名:

    api-mcp-blueprint.md

章構成:

1. 設計対象概要
2. 入力要件サマリー
3. 想定ユーザー・ロール
4. API設計サマリー
5. RESTエンドポイント一覧
6. Request / Response DTO候補
7. Spring Controller雛形
8. MCP tools一覧
9. MCP resources一覧
10. MCP prompts一覧
11. API / MCP tool 対応表
12. 権限・承認・監査ログ設計
13. セキュリティ注意点
14. 初期MVPで実装しないこと
15. 次の実装ステップ

### 9.2 AI実装指示書

出力ファイル名:

    implementation-instructions.md

章構成:

1. 実装目的
2. 実装対象
3. 実装しないこと
4. 想定技術スタック
5. Controller実装方針
6. DTO実装方針
7. Service境界
8. 認可・承認・監査ログ方針
9. MCP設計の将来実装方針
10. テスト観点
11. 成果物一覧
12. 検証手順
13. 注意事項

### 9.3 生成方式

初期MVPでは、Javaの文字列組み立てまたは章単位テンプレートメソッドによるMarkdown生成を採用する。

`MarkdownDocumentGenerator` では、章ごとに以下のようなメソッドへ分割する。

- `appendTitle`
- `appendInputSummary`
- `appendApiDesignSummary`
- `appendEndpoints`
- `appendDtoCandidates`
- `appendControllerSkeleton`
- `appendMcpTools`
- `appendMcpResources`
- `appendMcpPrompts`
- `appendApiMcpMappings`
- `appendSecurityNotes`
- `appendOutOfScope`
- `appendNextSteps`

---

## 10. 入力バリデーション設計

### 10.1 Bean Validation

| フィールド | 制約 |
|---|---|
| `businessRequirements` | `@NotBlank`, `@Size(max = 10000)` |
| `targetDomain` | `@NotBlank`, `@Size(max = 100)` |
| `userTypes` | `@NotBlank`, `@Size(max = 2000)` |
| `requiredOperations` | `@NotBlank`, `@Size(max = 3000)` |
| `allowedAiOperations` | `@NotBlank`, `@Size(max = 3000)` |
| `readOnlyOperations` | `@Size(max = 2000)` |
| `writeOperations` | `@Size(max = 2000)` |
| `approvalRequiredOperations` | `@Size(max = 2000)` |
| `auditLogRequiredOperations` | `@Size(max = 2000)` |

### 10.2 業務バリデーション

| ID | 条件 | 挙動 |
|---|---|---|
| BV-001 | 書き込み操作があるが承認必須操作が空 | 警告を表示する |
| BV-002 | AI許可操作に削除が含まれる | 強い警告を表示する |
| BV-003 | AI許可操作に権限変更が含まれる | 強い警告を表示する |
| BV-004 | AI許可操作に外部送信が含まれる | 強い警告を表示する |
| BV-005 | 想定認証方式が空 | 設計結果側に認証方式検討メモを出力する |

### 10.3 エスケープ方針

入力値は画面表示時にHTMLエスケープする。  
生成Markdown内でも、ユーザー入力由来の文字列はコード実行されない前提で扱う。

---

## 11. エラー処理設計

### 11.1 エラー分類

| エラー種別 | 内容 | 表示先 |
|---|---|---|
| 入力エラー | 必須未入力、文字数超過 | 入力画面 |
| 警告 | 書き込み操作に承認指定なし等 | 入力画面 / 結果画面 |
| 生成エラー | 生成処理中の例外 | エラー表示領域 |
| 想定外エラー | 未分類例外 | 共通エラー表示 |

### 11.2 入力エラー時の挙動

- 入力値を保持して入力画面へ戻す。
- 項目ごとにエラーメッセージを表示する。
- 生成処理は実行しない。

### 11.3 警告時の挙動

- 警告は生成処理を止めない。
- 生成結果内にも注意点として反映する。
- 削除・外部送信・権限変更は強い警告とする。

### 11.4 想定外エラー時の挙動

- スタックトレースを画面表示しない。
- 開発ログに原因を記録する。
- ユーザーには簡潔なメッセージを表示する。
- 入力内容に機密情報が含まれる可能性を考慮し、ログ出力は最小化する。

---

## 12. セキュリティ設計

### 12.1 初期MVPの前提

初期MVPはローカル利用または限定公開を前提とし、本格的な認証認可実装は対象外とする。

ただし、以下は設計上の必須事項とする。

- 入力値表示時のHTMLエスケープ
- 入力値の過剰ログ出力禁止
- secret / API key のリポジトリ混入禁止
- 外部LLM API連携を初期必須にしない
- 生成成果物内で危険操作を明示する

### 12.2 AI操作リスク出力

生成成果物には、以下のリスクを明示する。

- AIによる削除操作
- AIによる権限変更
- AIによる外部送信
- AIによる個人情報参照
- AIによる契約・金銭影響操作
- AIによる承認なし更新

### 12.3 監査ログ観点

監査ログ設計メモには、以下を出力する。

- 操作者
- 操作主体が人間かAIか
- 実行API
- MCP tool
- 対象リソースID
- 実行結果
- 承認者
- 承認日時
- 実行日時
- AI生成提案への参照

---

## 13. データ管理設計

### 13.1 永続化方針

初期MVPでは以下を永続化しない。

- 業務要件入力
- ユーザー種別入力
- AI許可操作入力
- 生成結果
- Markdown設計書
- AI実装指示書

理由:

- 機密情報を含む可能性がある。
- DB設計をMVP範囲外にして実装を軽量化する。
- 初期目的は設計生成フローの検証である。

### 13.2 一時保持

プレビュー画面で生成結果を参照するため、セッションに `BlueprintResult` を一時保持する方式を候補とする。

注意点:

- セッション保存は永続保存ではない。
- 長文データをセッションに保持しすぎない。
- ダウンロード処理はPOST直後の結果から生成する方式も検討する。

---

## 14. テスト設計

### 14.1 テスト方針

初期MVPでは、生成ロジックの単体テストを重視する。

| テスト種別 | 対象 |
|---|---|
| Unit Test | Generator / Classifier / NamingSupport |
| MVC Test | Controller入力・結果表示 |
| Integration Test | 主要生成フロー |
| Snapshot的確認 | Markdown出力の主要章確認 |

### 14.2 主要テストケース

| ID | テスト対象 | 内容 |
|---|---|---|
| TC-001 | 入力バリデーション | 必須未入力でエラーになる |
| TC-002 | API生成 | 顧客検索からGET `/api/customers` が生成される |
| TC-003 | API生成 | 顧客更新からPUT/PATCH候補が生成される |
| TC-004 | MCP生成 | 顧客検索から `searchCustomers` が生成される |
| TC-005 | MCP生成 | 顧客更新から `proposeCustomerUpdate` が生成される |
| TC-006 | セキュリティ生成 | 削除操作に承認必須注意が出力される |
| TC-007 | Markdown生成 | 主要章が出力される |
| TC-008 | AI実装指示書生成 | 実装しないことが明記される |
| TC-009 | Controller雛形 | `@RestController` と `@RequestMapping` が含まれる |
| TC-010 | 非対象確認 | MCPサーバー実装コードが生成されない |

---

## 15. ログ設計

### 15.1 ログ方針

初期MVPでは、以下のみログ出力する。

- アプリ起動
- 生成処理開始
- 生成処理完了
- 入力バリデーション失敗
- 生成処理例外

### 15.2 ログに出さないもの

- 業務要件全文
- 個人情報
- 機密情報
- API key
- secret
- 生成Markdown全文

---

## 16. 実装順序案

### 16.1 Step 1 最小Spring Boot Web骨格

- `pom.xml`
- `ApimApplication`
- `BlueprintController`
- `index.html`
- `result.html`

### 16.2 Step 2 入力モデルとバリデーション

- `BlueprintInput`
- Bean Validation
- 入力エラー表示

### 16.3 Step 3 生成サービス骨格

- `BlueprintGenerationService`
- `BlueprintResult`
- ダミー生成結果

### 16.4 Step 4 ルールベースAPI生成

- `OperationClassifier`
- `DomainNameNormalizer`
- `ApiDesignGenerator`
- `DtoCandidateGenerator`

### 16.5 Step 5 MCP設計生成

- `McpDesignGenerator`
- `McpToolCandidate`
- `McpResourceCandidate`
- `McpPromptCandidate`

### 16.6 Step 6 Markdown / AI実装指示書生成

- `MarkdownDocumentGenerator`
- `ImplementationInstructionGenerator`
- プレビュー画面

### 16.7 Step 7 セキュリティ注意点とテスト

- `SecurityNotesGenerator`
- Unit Test
- MVC Test
- README更新

---

## 17. Codex実装時の注意事項

### 17.1 実装してよいもの

APIM-005以降でCodexに実装させてよいものは以下である。

- Spring Boot Webアプリの最小構成
- 入力フォーム
- 生成結果画面
- DTO / model
- 生成Service
- ルールベース生成ロジック
- Markdown生成
- AI実装指示書生成
- 単体テスト
- MVCテスト

### 17.2 実装してはいけないもの

初期Web MVPでは以下を実装しない。

- 完全動作するMCPサーバー
- 外部LLM API連携必須化
- DB永続化
- 認証認可の本格実装
- GitHub自動PR作成
- OpenAPI完全生成
- Docker / 本番デプロイ構成
- Q-Scout診断ロジック

### 17.3 Codex作業単位

| 作業 | PR単位候補 |
|---|---|
| Spring Boot骨格作成 | APIM-005 |
| 入力フォーム・結果画面 | APIM-006 |
| API設計生成ロジック | APIM-007 |
| MCP設計生成ロジック | APIM-008 |
| Markdown / AI実装指示書生成 | APIM-009 |
| テスト整備 | APIM-010 |

初回実装PRで全機能を詰め込みすぎない。

---

## 18. 受け入れ条件

APIM-004の受け入れ条件は以下である。

1. APIM-003の要件を満たす基本設計になっている。
2. Spring Boot Web MVPとしての構成が示されている。
3. Controller / Service / Model / View の責務が定義されている。
4. API設計生成ロジックが定義されている。
5. MCP設計生成ロジックが定義されている。
6. Markdown設計書生成ロジックが定義されている。
7. AI実装指示書生成ロジックが定義されている。
8. セキュリティ・承認・監査ログ注意点生成が設計に含まれている。
9. 初期MVPで実装しないことが明確である。
10. Codex実装時の境界条件が明確である。

---

## 19. 次フェーズ

APIM-004完了後、次に作成する文書は以下とする。

    APIM-005 Codex向け API + MCP Blueprint Compiler for Spring Web MVP 実装指示書 v0.1

APIM-005では、以下を定義する。

- 実装対象
- 実装しないこと
- 作業ブランチ
- ファイル構成
- 実装順序
- 検証コマンド
- テスト方針
- PR本文
- 完了条件

---

## 20. 最終結論

APIM for Spring の初期Web MVPは、Spring Boot Webアプリとして、ユーザー入力からAPI設計とMCP設計を同時生成する構成とする。

初期MVPの中核は、外部LLM APIではなく、ルールベース / テンプレートベースの設計成果物生成である。

本設計では、以下を明確に分離する。

- 入力受付
- 入力バリデーション
- 操作分類
- API設計生成
- DTO候補生成
- Controller雛形生成
- MCP設計生成
- セキュリティ注意点生成
- Markdown設計書生成
- AI実装指示書生成

また、初期MVPでは、完全動作するMCPサーバー、完全動作する生成先Springアプリ、DB永続化、認証認可コード自動生成、外部LLM API必須化は行わない。

以上を、APIM-004 API + MCP Blueprint Compiler for Spring 基本設計書 v0.1 とする。



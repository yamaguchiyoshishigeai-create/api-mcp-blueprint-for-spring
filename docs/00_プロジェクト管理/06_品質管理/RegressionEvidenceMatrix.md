# Regression Evidence Matrix

## TSK-044

| 項目 | 内容 |
|---|---|
| TSK ID | TSK-044 |
| 完了条件 | AI実装指示書に旧注意文言を再出力しない |
| 対応テストクラス | ImplementationInstructionGeneratorTest / BlueprintGenerationRegressionTest / GeneratedOutputForbiddenPhraseGuardTest |
| 対応テストメソッド | `generatesTargetApplicationInstructionsWithoutApimInternalImplementationScope` / `assertDocumentsKeepRequiredSectionsAndOutOfScope` / `generatedBlueprintAndImplementationInstructionsDoNotContainAnyForbiddenPhrase` |
| 正例条件 | 対象業務アプリケーション向けの実装指示と必須セクションが出力される |
| 否定条件 | `APIM for Spring本体の改修指示ではない` を含む禁止語が blueprint markdown / implementation instructions に含まれない |
| 手動確認 | Render公開前確認ではプレビュー画面でも禁止語再発がないことを確認する |

## TSK-064

| 項目 | 内容 |
|---|---|
| TSK ID | TSK-064 |
| 完了条件 | Markdown設計書にAPIM本体MVP制約が混入しない |
| 対応テストクラス | MarkdownDocumentGeneratorTest / GeneratedOutputForbiddenPhraseGuardTest |
| 対応テストメソッド | `usesInputDrivenOverviewInsteadOfApimInternalMvpText` / `outOfScopeSectionIsExpressedAsLaterPhaseDecisions` / `generatedBlueprintAndImplementationInstructionsDoNotContainAnyForbiddenPhrase` |
| 正例条件 | 設計対象概要が入力要件ベースで出力され、`## 14. 後続フェーズで具体化する事項` として後続判断事項が出力される |
| 否定条件 | `APIM for Spring の初期MVP向け設計成果物。` と `初期MVPで実装しないこと` が blueprint markdown / implementation instructions に含まれない |
| 手動確認 | Render公開前確認では blueprint preview の `## 1` と `## 14` が対象システム基準の文脈で出力されることを確認する |

## TSK-065

| 項目 | 内容 |
|---|---|
| TSK ID | TSK-065 |
| 完了条件 | AI実装指示書に固定的な「実装しないこと」を出力しない |
| 対応テストクラス | ImplementationInstructionGeneratorTest / BlueprintGenerationRegressionTest / GeneratedOutputForbiddenPhraseGuardTest |
| 対応テストメソッド | `generatesTargetApplicationInstructionsWithoutApimInternalImplementationScope` / `assertDocumentsKeepRequiredSectionsAndOutOfScope` / `generatedBlueprintAndImplementationInstructionsDoNotContainAnyForbiddenPhrase` |
| 正例条件 | AI実装指示書で `## 10. 後続フェーズで具体化する事項` が出力され、後続AIにも実装禁止ではなく追加設計・実装判断事項として扱う旨が出力される |
| 否定条件 | `## 10. 実装しないこと` と旧固定禁止文言が blueprint markdown / implementation instructions に含まれない |
| 手動確認 | Render公開前確認では implementation instructions preview の `## 10` が後続判断事項として出力されることを確認する |

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

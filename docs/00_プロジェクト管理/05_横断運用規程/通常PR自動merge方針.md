# 通常PR自動merge方針

## 1. 目的

本方針は、通常PRにおいて、ChatGPT(リポジトリ編集)がCI確認後にmergeまで自動実行できる条件と、ユーザー(人)判断へ戻す条件を定義する。

従来の運用文書には、ユーザー(人)の担当範囲に `merge判断` を無条件に含める記述と、PR確認・CI確認・merge判断をChatGPT(リポジトリ編集)で実施する記述が併存していた。

この曖昧さにより、CI success、mergeable true、scope内、未反映事項なしを確認済みの通常PRでも、ChatGPTがmerge前に停止する問題が発生した。

本方針では、通常PRのmergeをChatGPT(リポジトリ編集)の自動実行対象に含めつつ、外部影響、破壊的変更、主観判断、ユーザー明示確認が必要な場合はユーザー(人)判断へ戻す。

## 2. 基本原則

1. `success` 以外のCI状態をmerge可能状態として扱わない。
2. `queued`、`in_progress`、一部job success、status未検出をmerge可能状態として扱わない。
3. `failure`、`cancelled`、`timed_out`、`action_required` をsuccess相当に読み替えない。
4. 要求仕様、ユーザー指定文言、実装方針、テスト期待値、運用規則を弱めてmerge条件を満たしたことにしない。
5. 未反映事項、CI未確認、PR未作成、main未反映を完了扱いしない。
6. merge後cleanup、remote branch削除、local sync、prune確認を省略しない。

## 3. ChatGPT自動mergeを許可する条件

以下をすべて満たす通常PRは、ChatGPT(リポジトリ編集)がmergeまで実行できる。

| 条件 | 内容 |
|---|---|
| CI終了条件 | 対象PRまたはhead commitの必要CIが終了条件まで `success` である。 |
| merge可能性 | PRが `mergeable` であり、merge conflictがない。 |
| scope整合 | 変更内容が事前合意スコープ内である。 |
| 仕様不変 | 要求仕様、指定文言、実装方針、テスト期待値、運用規則を弱めていない。 |
| 未反映事項なし | 未反映事項、未確認事項、未解決レビュー、未処理のblocking commentがない。 |
| 外部影響なし | 外部サービス作成、公開判断、費用影響、秘密情報、破壊的変更、主観判断を伴わない。 |
| ユーザー明示制約なし | ユーザーがmerge前確認を明示要求していない。 |

上記条件を満たす場合、ChatGPTは「mergeはユーザー(人)の最終判断」として停止せず、squash merge等の既定merge方式でmain反映まで進める。

## 4. ユーザー(人)判断へ戻す条件

以下のいずれかに該当する場合、ChatGPTは自動mergeせず、ユーザー(人)判断へ戻す。

- Public公開、Render等の外部サービス作成、URL公開、費用発生、アカウント設定変更を伴う。
- 秘密情報、権限、認証認可、本番運用、データ削除、破壊的変更を伴う。
- 要求仕様や文言の解釈に主観判断が必要である。
- CIが `success` ではない。
- CIが未検出、queued、in_progress、cancelled、timed_out、action_required、failureである。
- merge conflict、未解決レビュー、未反映事項、scope逸脱がある。
- ユーザーが明示的にmerge前確認を要求している。
- tool権限やGitHub側制約により、ChatGPTが安全にmergeできない。

## 5. merge後の必須処理

ChatGPTがPRをmergeした後は、以下を完了状態の一部として扱う。

1. merge commit SHAを確認する。
2. 必要な課題管理整理PRを作成し、CI success確認後にmainへmergeする。
3. リモート作業ブランチを削除する。
4. ローカル環境ではChatExec2方式等により `git fetch origin --prune` を実行する。
5. ローカル `main` が期待するmerge後SHAへ同期されていることを確認する。
6. `git branch -r` で削除済み作業ブランチが残っていないことを確認する。
7. worktree cleanを確認する。

上記が未完了の場合、作業全体を `DONE` と扱わない。

## 6. 既存方針との関係

本方針は以下の既存方針と接続する。

- `CIポーリング終了条件固定方針` 相当の運用: CIは終了条件まで確認し、success以外を完了扱いしない。
- `Merge後cleanup・local sync自動統合方針` 相当の運用: merge後cleanupとlocal syncを完了状態に含める。
- `Codex投入前ハンドオフゲート方針`: Codexへmerge、課題管理整理、merge後ブランチ削除を混ぜず、Codex完了後にChatGPTがPR確認・CI確認・merge判断を扱う。
- `安全チェック発生時の仕様不変切替方針`: merge条件を満たすために仕様やテスト期待値を弱めない。

## 7. 禁止事項

以下を禁止する。

- すべてのPRを無条件に自動mergeすること。
- CI未完了またはin_progress状態でmergeすること。
- failure、cancelled、timed_out、action_requiredをsuccess相当として扱うこと。
- 要求仕様やテスト期待値を弱めてmerge条件を満たしたことにすること。
- 未解決レビュー、未反映事項、scope逸脱を無視してmergeすること。
- merge後cleanup、local sync、prune確認を省略してDONE扱いすること。

## 8. 関連規程

- `00_運用ルール索引と実行ゲート.md`
- `Codex投入前ハンドオフゲート方針.md`
- `安全チェック発生時の仕様不変切替方針.md`
- `ChatExec2方式.md`

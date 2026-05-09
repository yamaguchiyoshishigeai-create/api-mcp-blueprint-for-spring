# Merge後cleanup・local sync自動統合方針

## 1. 目的

本書は、PR squash merge後に発生するremote branch cleanup、local prune、local main fast-forward syncを、必要な範囲までChatGPTが自動準備し、ユーザー操作を最小化するための横断運用規程である。

既存の順次タスクmergeゲートでは、merge後のremote作業ブランチ削除、`git fetch origin --prune`、ローカル追跡参照整理を必須工程として扱っている。しかし、ChatGPTがどこまで自動で進め、どの時点でユーザー実行へ切り替えるか、またcleanupとlocal syncを1本のChatExec2スクリプトに統合すべきかが明文化されていなかった。

その結果、必須後続処理であるにもかかわらず、ユーザーへ「進めてよいか」を確認したり、cleanup用スクリプトとlocal sync用スクリプトを分けて提示したりする余地があった。

## 2. 基本原則

PR squash merge後のremote branch cleanup、local prune、local main syncが運用上必須工程として確定している場合、ChatGPTは実施可否の確認をユーザーに求めない。

ChatGPTは、以下を自動で進める。

1. PR merge結果確認。
2. remote作業ブランチ残存確認。
3. cleanup対象branchの特定。
4. local mainがbehindになる可能性の確認。
5. ChatExec2スクリプト作成。
6. ユーザーに実行対象 `.cmd` または単一コマンドを提示。

ユーザーへ依頼するのは、ローカル環境で実行が必要な段階に限定する。

## 3. 原則1スクリプト化

merge後cleanupとlocal main syncが連続する必須後続処理である場合、ChatGPTは原則として以下を1本のChatExec2スクリプトに統合して提示する。

1. `gh` CLI / 認証確認。
2. 対象リポジトリパス確認。
3. remote作業ブランチ存在確認。
4. remote作業ブランチ削除。
5. remote作業ブランチ不存在確認。
6. `git fetch origin --prune`。
7. 作業ツリーclean確認。
8. `git pull --ff-only origin main`。
9. 最終 `git status --short --branch` 確認。
10. 必要に応じたPRコメントへのサニタイズ済みsummary投稿。

成功時のユーザー操作は、1回のダブルクリックまたは単一コマンド実行で完了することを目標とする。

## 4. 失敗時の停止方針

統合スクリプトは、途中工程で失敗した場合、その時点で処理を中断し、`[FAIL]` を表示する。

結果ファイルには、以下を明記する。

- 失敗Step。
- 失敗理由。
- 完了済み工程。
- 未実施工程。
- remote branch削除状態。
- local prune状態。
- local sync状態。
- 次に必要な判断または再実行方針。

失敗した工程より後続の破壊的または状態変更を伴う処理へ進んではならない。

## 5. 分割実行を許容する例外

cleanupとlocal syncを分割して提示してよいのは、以下の場合に限定する。

| 例外 | 理由 |
|---|---|
| sync実行可否に判断が必要 | local worktree dirty、ローカル作業中branch、未保存変更などが想定されるため |
| 対象branchが不明 | 誤削除防止のため |
| 対象repositoryが不明 | 誤操作防止のため |
| 複数候補branchがある | 削除対象の特定が必要なため |
| PRが未mergeまたはmerge状態不明 | branch削除の安全性を確認できないため |
| ユーザーが分割実行を明示 | ユーザー意図を優先するため |
| local syncを行わない理由が明確 | 作業ブランチ継続中、別作業中、検証用にbehind維持が必要など |

これらに該当しない場合、cleanupとlocal syncは1本化する。

## 6. 不要確認の禁止

以下を禁止する。

- merge済みPRの作業branchが残っているだけの状態で「cleanupしますか」と確認して停止すること。
- 必須後続処理であるにもかかわらず、cleanupスクリプト作成前にユーザー承認を求めること。
- cleanup用スクリプトとlocal sync用スクリプトを理由なく分割し、成功時のユーザー操作を複数回に増やすこと。
- `queued` / `in_progress` のCI完了待ちと同様に、明確な終了条件なしで待ち戻しを発生させること。

## 7. ChatExec2スクリプト仕様

統合スクリプトは、以下を満たす。

1. `JOB_ID` を持つ。
2. Windows向け `.cmd` ではダブルクリック実行を前提とする。
3. 既知のリポジトリパスは固定値として埋め込む。
4. 既知パスがある場合、フォルダ選択ダイアログを出さない。
5. 詳細結果は `%TEMP%/<JOB_ID>/` または `.chatexec2/<JOB_ID>/` に出力する。
6. 標準出力には `[OK]` / `[FAIL]` と結果末尾を表示する。
7. 実行後にウィンドウを保持し、ユーザーが結果を視認できるようにする。
8. 秘密情報、トークン、環境変数全文、個人情報、不要な絶対パスをPRコメントに含めない。
9. GitHubコメントにはサニタイズ済みsummaryのみを投稿する。

## 8. 標準結果分類

統合スクリプトの結果分類は以下を標準とする。

| 分類 | 意味 |
|---|---|
| `SUCCESS` | remote cleanup、local prune、local syncが完了した |
| `REMOTE_CLEANED_LOCAL_SYNC_FAILED` | remote cleanupは完了したがlocal syncに失敗した |
| `REMOTE_ALREADY_ABSENT_LOCAL_SYNC_DONE` | branchは既に無く、local syncは完了した |
| `REMOTE_DELETE_FAILED` | remote branch削除に失敗した |
| `LOCAL_DIRTY_STOPPED` | worktree dirtyのためpullせず停止した |
| `NOT_MAIN_STOPPED` | local branchがmainではないためpullせず停止した |
| `FAILED` | その他の失敗 |

## 9. 他規程との関係

本方針は、以下の規程を補強する。

- `順次タスクmergeゲート方針`
- `squash merge後ブランチ整理方針`
- `ChatExec2方式.md`
- `ローカル一時ファイルとルート直下整理方針.md`
- `CIポーリング終了条件固定方針.md`

重複がある場合は、ユーザー操作を最小化し、必須後続処理を自動準備する方針を優先する。ただし、削除対象やsync可否が不明な場合は安全側に倒し、停止して状況を明示する。

## 10. 完了条件

本方針の適用完了条件は以下である。

1. merge後cleanupが必須工程である場合、確認待ちにせずChatExec2スクリプト作成まで自動で進めることが明文化されている。
2. remote branch cleanup、local prune、local main fast-forward syncを原則1本のChatExec2スクリプトに統合することが明文化されている。
3. 失敗時の停止条件と報告内容が明文化されている。
4. 分割実行を許容する例外条件が限定されている。
5. ユーザー操作は成功時1回を目標とすることが明文化されている。

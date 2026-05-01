from __future__ import annotations

from pathlib import Path
import sys


REQUIRED_RESULT_HEADERS = [
    "## 作業概要",
    "## 作業ブランチ",
    "## 作成・更新ファイル",
    "## 実施した検証",
    "## 検証結果",
    "## 未実施の検証と理由",
    "## 残課題",
    "## PR",
    "## main反映",
]

REQUIRED_PR_HEADERS = [
    "## 概要",
    "## 対応内容",
    "## 実施しなかったこと",
    "## 検証",
]


def check_contains(path: Path, headers: list[str]) -> list[str]:
    text = path.read_text(encoding="utf-8")
    return [h for h in headers if h not in text]


def main() -> int:
    if len(sys.argv) != 2:
        print("usage: python scripts/check_ai_repo_result.py <pr_template_path>")
        return 1

    pr_path = Path(sys.argv[1])
    result_path = Path("AI_REPO_RESULT.md")

    if not pr_path.exists():
        print(f"NG: PR template not found: {pr_path}")
        return 1
    if not result_path.exists():
        print(f"NG: result file not found: {result_path}")
        return 1

    missing_pr = check_contains(pr_path, REQUIRED_PR_HEADERS)
    missing_result = check_contains(result_path, REQUIRED_RESULT_HEADERS)

    if missing_pr or missing_result:
        print("NG: required sections are missing")
        for h in missing_pr:
            print(f"- missing in PR template: {h}")
        for h in missing_result:
            print(f"- missing in result file: {h}")
        return 1

    print("PASS: AI evidence files include required sections")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())

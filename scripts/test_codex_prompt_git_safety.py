from __future__ import annotations

from pathlib import Path
import sys


PROMPT_PATH = Path("PROJECT_START_PROMPT.md")
REQUIRED = [
    "main",
    "直接push",
    "merge",
    "認証",
    "認可",
    "承認",
    "人間確認",
    "監査ログ",
]


def main() -> int:
    if not PROMPT_PATH.exists():
        print(f"NG: not found: {PROMPT_PATH}")
        return 1

    text = PROMPT_PATH.read_text(encoding="utf-8")
    missing = [token for token in REQUIRED if token not in text]
    if missing:
        print("NG: missing safety tokens")
        for token in missing:
            print(f"- {token}")
        return 1

    print("PASS: PROJECT_START_PROMPT contains safety essentials")
    return 0


if __name__ == "__main__":
    sys.exit(main())

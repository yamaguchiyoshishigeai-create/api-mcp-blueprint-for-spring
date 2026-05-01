from __future__ import annotations

from pathlib import Path
import re
import sys


INDEX = Path("docs/00_プロジェクト管理/02_改善タスク管理/改善タスク課題一覧.md")
TASK_ROOT = Path("docs/00_プロジェクト管理/02_改善タスク管理")

STATUS_TO_DIR = {
    "未解決": "未解決",
    "解決中": "解決中",
    "確認待ち": "確認待ち",
    "解決済み": "解決済み",
}


def parse_index() -> list[tuple[str, str]]:
    if not INDEX.exists():
        raise FileNotFoundError(f"index not found: {INDEX}")

    rows: list[tuple[str, str]] = []
    line_re = re.compile(r"^\|\s*(TSK-\d{3})\s*\|.*\|\s*(未解決|解決中|確認待ち|解決済み)\s*\|")
    for line in INDEX.read_text(encoding="utf-8").splitlines():
        m = line_re.match(line)
        if m:
            rows.append((m.group(1), m.group(2)))
    return rows


def main() -> int:
    rows = parse_index()
    if not rows:
        print("NG: no TSK rows found in index")
        return 1

    missing = []
    for tsk_id, status in rows:
        folder = STATUS_TO_DIR[status]
        path = TASK_ROOT / folder / f"{tsk_id}.md"
        if not path.exists():
            missing.append(str(path))

    if missing:
        print("NG: missing TSK files")
        for p in missing:
            print(f"- {p}")
        return 1

    print(f"PASS: {len(rows)} tasks are consistent with index")
    return 0


if __name__ == "__main__":
    sys.exit(main())

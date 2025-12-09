#!/usr/bin/env python3
# -*- coding: utf-8 -*-

"""Extract BDIC timetable courses for 2025-2026 (both semesters) into a unified table.

Input:
  - csvkebiao     : 2025-2026-1 学期课表（真实 2025-2026 上学期）
  - csvkebiaoXia  : 2023-2024-2 课表（作为 2025-2026-2 模板）

Output (in project root):
  - timetable_courses_2025_2026.csv
  - timetable_courses_2025_2026.json

Fields:
  - academic_year  : "2025-2026"  （两学期统一按 25-26 记）
  - term           : 1 或 2
  - major          : SE / EIE / IOT / FIN
  - grade_level    : 1–4 （大一–大四）
  - course_code
  - course_name_cn
  - course_name_en (如果没有英文名则为空字符串)

NOTE:
  - 对于下学期，我们用 2023-2024-2 作为模板，只关心课程与年级（大一–大四），
    所以只是把 2020–2023 级映射成 grade_level 4–1，统一视作 25-26 学年的下学期。
"""

import csv
import json
import os
import re
from typing import Dict, List, Tuple

ROOT_DIR = os.path.dirname(os.path.abspath(__file__))

# 输入目录
CSV_KEBIAO_DIR = os.path.join(ROOT_DIR, "csvkebiao")       # 上学期 2025-2026-1
CSV_KEBIAO_XIA_DIR = os.path.join(ROOT_DIR, "csvkebiaoXia")  # 下学期模板 2023-2024-2

# 统一学年（逻辑上都视作 2025-2026）
FIRST_SEM_ACADEMIC_YEAR = "2025-2026"
SECOND_SEM_ACADEMIC_YEAR = "2025-2026"

FIRST_SEM_TERM = 1
SECOND_SEM_TERM = 2

# 计算年级用的参考“开学年份”
FIRST_SEM_REFERENCE_START_YEAR = 2025  # 2025-2026-1：2022–2025 级分别是大四–大一
SECOND_SEM_TEMPLATE_START_YEAR = 2023  # 2023-2024-2：2020–2023 级分别是大四–大一

MAJOR_KEYWORDS = {
    "软件工程": "SE",
    "电子信息工程": "EIE",
    "物联网工程": "IOT",
    "金融学": "FIN",
}

# 课程代码：形如 BDIC1036J / EEEN3019J / COMP1002J / ARCH1001J 或 0009287-2 / 0009919 等
COURSE_CODE_PATTERN = re.compile(r"/\s*([A-Z]{3,5}\d{3,4}J|\d{6,8}(?:-\d+)?)\s*/")


def infer_major_from_filename(filename: str) -> str:
    for zh, code in MAJOR_KEYWORDS.items():
        if zh in filename:
            return code
    raise ValueError(f"无法从文件名推断专业: {filename}")


def infer_entry_year_first_sem(filename: str) -> int:
    """从 2025-2026-1 学期文件名中抽取入学年份（2022–2025）。"""
    m = re.search(r"(20\d{2})级", filename)
    if not m:
        raise ValueError(f"未在文件名中找到 'XXXX级': {filename}")
    return int(m.group(1))


def infer_entry_year_second_sem(filename: str) -> int:
    """从 2023-2024-2-XXXX... 文件名中抽取 XXXX（2020–2023）。"""
    m = re.match(r"2023-2024-2-(\d{4})", filename)
    if not m:
        raise ValueError(f"未在文件名中找到模板入学年份: {filename}")
    return int(m.group(1))


def grade_level_first_sem(entry_year: int) -> int:
    level = FIRST_SEM_REFERENCE_START_YEAR - entry_year + 1
    if level < 1 or level > 4:
        raise ValueError(f"首学期入学年份 {entry_year} 计算得到非法年级 {level}")
    return level


def grade_level_second_sem(entry_year: int) -> int:
    # 模板学年 2023-2024-2：2020–2023 级 -> 大四–大一
    level = SECOND_SEM_TEMPLATE_START_YEAR - entry_year + 1
    if level < 1 or level > 4:
        raise ValueError(f"下学期模板入学年份 {entry_year} 计算得到非法年级 {level}")
    return level


def split_cn_en_name(raw: str) -> Tuple[str, str]:
    """拆分课程名中的中文和英文部分。

    例：
      - "学术英语-阅读 College English 3" -> ("学术英语-阅读", "College English 3")
      - "新中国史History of New China"   -> ("新中国史", "History of New China")
      - "形势与政策7"                     -> ("形势与政策7", "")
    """
    s = raw.strip()
    ascii_idx = None
    for i, ch in enumerate(s):
        if ("A" <= ch <= "Z") or ("a" <= ch <= "z"):
            ascii_idx = i
            break
    if ascii_idx is None:
        return s, ""

    cn = s[:ascii_idx].strip()
    en = s[ascii_idx:].strip()
    if not cn:
        # 基本不会出现只有英文名的情况，但做个保护
        return "", s
    return cn, en


def extract_courses_from_cell(text: str) -> List[Tuple[str, str, str]]:
    """从一个单元格文本中提取所有课程（三元组：cn, en, code）。"""
    if not text:
        return []

    courses: List[Tuple[str, str, str]] = []

    # 单元格中可能用换行分成多门课
    for part in re.split(r"[\r\n]+", text):
        part = part.strip()
        if not part:
            continue

        m = COURSE_CODE_PATTERN.search(part)
        if not m:
            # 既没有 BDIC/EEEN/COMP 等代码，也没有通识课代码，跳过
            continue

        code = m.group(1).strip()
        name_part = part[: m.start()].strip()
        if not name_part:
            # 理论上不会发生
            continue

        cn, en = split_cn_en_name(name_part)
        courses.append((cn, en, code))

    return courses


def extract_courses_from_csv_file(csv_path: str) -> List[Tuple[str, str, str]]:
    """从单个课表 CSV 文件中提取所有课程（三元组列表）。"""
    all_courses: List[Tuple[str, str, str]] = []

    with open(csv_path, "r", encoding="utf-8-sig", newline="") as f:
        reader = csv.reader(f)
        for row_idx, row in enumerate(reader):
            # 一般第 1 行是标题，第 2 行是表头，从第 3 行开始才有课
            if row_idx < 2:
                continue
            if len(row) <= 2:
                continue

            # 0: 节次/上午/下午；1: 时间；2-6: 周一到周五
            for cell in row[2:]:
                if not cell or not cell.strip():
                    continue
                for cn, en, code in extract_courses_from_cell(cell):
                    all_courses.append((cn, en, code))

    return all_courses


def collect_courses() -> List[Dict[str, str]]:
    """遍历两个目录，汇总并去重课程。"""
    # key: (academic_year, term, major, grade_level, course_code)
    courses: Dict[Tuple[str, int, str, int, str], Dict[str, str]] = {}

    # 上学期：csvkebiao
    if os.path.isdir(CSV_KEBIAO_DIR):
        for fname in sorted(os.listdir(CSV_KEBIAO_DIR)):
            if not fname.endswith(".csv"):
                continue
            full_path = os.path.join(CSV_KEBIAO_DIR, fname)

            major = infer_major_from_filename(fname)
            entry_year = infer_entry_year_first_sem(fname)
            grade_level = grade_level_first_sem(entry_year)

            for cn, en, code in extract_courses_from_csv_file(full_path):
                key = (
                    FIRST_SEM_ACADEMIC_YEAR,
                    FIRST_SEM_TERM,
                    major,
                    grade_level,
                    code,
                )
                value = {"course_name_cn": cn, "course_name_en": en}

                if key in courses:
                    old = courses[key]
                    # 如果同一 key 出现多次，优先保留已有记录，
                    # 如果中英文名不一致，仅在控制台打印一下提醒。
                    if (
                        old["course_name_cn"] != value["course_name_cn"]
                        or old["course_name_en"] != value["course_name_en"]
                    ):
                        print(
                            "WARNING: 同一课程键但名称不一致:",
                            key,
                            "旧=",
                            old,
                            "新=",
                            value,
                        )
                    continue

                courses[key] = value

    # 下学期（模板）：csvkebiaoxia
    if os.path.isdir(CSV_KEBIAO_XIA_DIR):
        for fname in sorted(os.listdir(CSV_KEBIAO_XIA_DIR)):
            if not fname.endswith(".csv"):
                continue
            full_path = os.path.join(CSV_KEBIAO_XIA_DIR, fname)

            major = infer_major_from_filename(fname)
            entry_year = infer_entry_year_second_sem(fname)
            grade_level = grade_level_second_sem(entry_year)

            for cn, en, code in extract_courses_from_csv_file(full_path):
                key = (
                    SECOND_SEM_ACADEMIC_YEAR,
                    SECOND_SEM_TERM,
                    major,
                    grade_level,
                    code,
                )
                value = {"course_name_cn": cn, "course_name_en": en}

                if key in courses:
                    old = courses[key]
                    if (
                        old["course_name_cn"] != value["course_name_cn"]
                        or old["course_name_en"] != value["course_name_en"]
                    ):
                        print(
                            "WARNING: 同一课程键但名称不一致(下学期):",
                            key,
                            "旧=",
                            old,
                            "新=",
                            value,
                        )
                    continue

                courses[key] = value

    # 转成列表并排序（方便人工检查）
    rows: List[Dict[str, str]] = []
    for (academic_year, term, major, grade_level, code), names in sorted(
        courses.items(),
        key=lambda x: (
            x[0][0],  # academic_year
            x[0][1],  # term
            x[0][3],  # grade_level
            x[0][2],  # major
            x[0][4],  # course_code
        ),
    ):
        rows.append(
            {
                "academic_year": academic_year,
                "term": term,
                "major": major,
                "grade_level": grade_level,
                "course_code": code,
                "course_name_cn": names["course_name_cn"],
                "course_name_en": names["course_name_en"],
            }
        )

    return rows


def write_outputs(rows: List[Dict[str, str]]) -> None:
    csv_path = os.path.join(ROOT_DIR, "timetable_courses_2025_2026.csv")
    json_path = os.path.join(ROOT_DIR, "timetable_courses_2025_2026.json")

    fieldnames = [
        "academic_year",
        "term",
        "major",
        "grade_level",
        "course_code",
        "course_name_cn",
        "course_name_en",
    ]

    with open(csv_path, "w", encoding="utf-8", newline="") as f:
        writer = csv.DictWriter(f, fieldnames=fieldnames)
        writer.writeheader()
        writer.writerows(rows)

    with open(json_path, "w", encoding="utf-8") as f:
        json.dump(rows, f, ensure_ascii=False, indent=2)

    print(f"已写出 {len(rows)} 条课程记录到:")
    print(f"  CSV : {csv_path}")
    print(f"  JSON: {json_path}")


def main() -> None:
    rows = collect_courses()
    write_outputs(rows)


if __name__ == "__main__":
    main()

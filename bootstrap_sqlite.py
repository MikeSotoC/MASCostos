#!/usr/bin/env python3
"""Crea SQLDelphin_basica.sqlite ejecutando SQLDelphin_basica.sql."""

from __future__ import annotations

import argparse
import sqlite3
from pathlib import Path


def parse_statements(sql_text: str) -> list[str]:
    statements: list[str] = []
    buffer: list[str] = []

    for raw_line in sql_text.splitlines():
        line = raw_line.strip()
        if not line or line.startswith("--"):
            continue
        buffer.append(raw_line)
        if line.endswith(";"):
            stmt = "\n".join(buffer).strip().rstrip(";").strip()
            if stmt:
                statements.append(stmt)
            buffer.clear()

    if buffer:
        stmt = "\n".join(buffer).strip().rstrip(";").strip()
        if stmt:
            statements.append(stmt)

    return statements


def build_sqlite(sql_file: Path, sqlite_file: Path, force: bool) -> int:
    if not sql_file.exists():
        print(f"ERROR: no existe el archivo SQL: {sql_file}")
        return 1

    if sqlite_file.exists() and not force:
        print(f"ERROR: ya existe {sqlite_file}. Usa --force para recrearlo.")
        return 1

    if sqlite_file.exists() and force:
        sqlite_file.unlink()

    sqlite_file.parent.mkdir(parents=True, exist_ok=True)

    sql_text = sql_file.read_text(encoding="utf-8")
    statements = parse_statements(sql_text)

    conn = sqlite3.connect(str(sqlite_file))
    try:
        cur = conn.cursor()
        for stmt in statements:
            cur.execute(stmt)
        conn.commit()
    finally:
        conn.close()

    print(f"OK: creado {sqlite_file} desde {sql_file} con {len(statements)} sentencias")
    return 0


def main() -> int:
    parser = argparse.ArgumentParser(description="Bootstrap SQLDelphin sqlite desde .sql")
    parser.add_argument("--sql", default="SQLDelphin_basica.sql", help="ruta al SQL fuente")
    parser.add_argument("--out", default="SQLDelphin_basica.sqlite", help="ruta del sqlite destino")
    parser.add_argument("--force", action="store_true", help="recrear sqlite si ya existe")
    args = parser.parse_args()

    return build_sqlite(Path(args.sql), Path(args.out), args.force)


if __name__ == "__main__":
    raise SystemExit(main())

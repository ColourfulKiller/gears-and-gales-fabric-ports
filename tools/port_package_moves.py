"""Fix imports of vanilla classes that moved package between 1.21.1 and 1.21.11, in place.

    python -I ports/tools/port_package_moves.py <src/main/java dir> <decompiled mc source dir>

Every `import net.minecraft.a.b.X;` (also nested `X.Y` and static imports) whose class is not at that path in the
1.21.11 sources is pointed at the unique top-level class X found elsewhere in them. Ambiguous or missing names are
reported, not changed.
"""
import re
import sys
from collections import defaultdict
from pathlib import Path

SRC = Path(sys.argv[1])
MC = Path(sys.argv[2])

by_name = defaultdict(list)
for f in MC.rglob("*.java"):
    rel = f.relative_to(MC).with_suffix("")
    by_name[f.stem].append(".".join(rel.parts))
existing = {fq for fqs in by_name.values() for fq in fqs}

IMPORT = re.compile(r"^import (static )?(net\.minecraft\.[\w.]+?)\.([A-Z]\w*)((?:\.[\w*]+)*);", re.M)
missing, ambiguous = defaultdict(int), defaultdict(set)


def fix(m):
    static, pkg, cls, rest = m.groups()
    fq = f"{pkg}.{cls}"
    if fq in existing:
        return m.group(0)
    candidates = by_name.get(cls, [])
    if len(candidates) == 1:
        return f"import {static or ''}{candidates[0]}{rest};"
    if not candidates:
        missing[fq] += 1
    else:
        ambiguous[fq] |= set(candidates)
    return m.group(0)


changed = 0
for f in SRC.rglob("*.java"):
    t = f.read_text(encoding="utf-8")
    out = IMPORT.sub(fix, t)
    # fully qualified uses of a moved package (e.g. net.minecraft.advancements.critereon.X in code)
    if out != t:
        f.write_text(out, encoding="utf-8")
        changed += 1
print(f"{changed} files changed")
for fq, n in sorted(missing.items(), key=lambda kv: -kv[1]):
    print(f"  gone      {n:3} {fq}")
for fq, c in sorted(ambiguous.items()):
    print(f"  ambiguous     {fq} -> {sorted(c)}")

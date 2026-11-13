"""Rename methods only where javac says they no longer exist, in place.

    python -I ports/tools/port_line_renames.py <javac error log> old=new [old=new ...]

For every "cannot find symbol ... symbol: method old(...)" error, `.old(` becomes `.new(` on that source line
(e.g. location=identifier: ResourceKey#location is gone but TagKey#location / SoundEvent#location are not, so a global
rename would be wrong). `new` may contain a leading receiver change such as `getItem().getDescriptionId`.
"""
import re
import sys
from collections import defaultdict
from pathlib import Path

lines = Path(sys.argv[1]).read_text(encoding="utf-8-sig", errors="replace").splitlines()
renames = dict(arg.split("=", 1) for arg in sys.argv[2:])
targets = defaultdict(lambda: defaultdict(set))  # file -> line -> {old}
for i, line in enumerate(lines):
    m = re.match(r"^(.*\.java):(\d+): error: cannot find symbol", line)
    if not m:
        continue
    for k in range(i + 1, min(i + 6, len(lines))):
        s = re.search(r"symbol:\s+method (\w+)\(", lines[k])
        if s:
            if s.group(1) in renames:
                targets[m.group(1)][int(m.group(2))].add(s.group(1))
            break
changed = 0
for path, by_line in targets.items():
    f = Path(path)
    if not f.exists():  # parked / deleted since the compile
        continue
    text = f.read_text(encoding="utf-8").split("\n")
    for ln, olds in by_line.items():
        before = text[ln - 1]
        for old in olds:
            if f".{renames[old]}(" in text[ln - 1]:  # already rewritten (rerun)
                continue
            text[ln - 1] = re.sub(rf"\.{old}\(", f".{renames[old]}(", text[ln - 1])
        if text[ln - 1] != before:
            changed += 1
    f.write_text("\n".join(text), encoding="utf-8")
print(f"{changed} lines changed")

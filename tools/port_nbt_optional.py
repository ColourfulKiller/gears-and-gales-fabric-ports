"""1.21.5 NBT getters return Optional; rewrite only the lines javac flags.

    python -I ports/tools/port_nbt_optional.py <src/main/java dir> <javac error log>

On each line with "Optional<X> cannot be converted", "getList ... cannot be applied", "getAllKeys" or
"getAsString" errors:
  .getInt(k) -> .getIntOr(k, 0)   (same for Short/Byte/Long/Float/Double -> 0, Boolean -> false, String -> "")
  .getCompound(k) -> .getCompoundOrEmpty(k)     .getList(k, type) -> .getListOrEmpty(k)
  .getIntArray(k) -> .getIntArray(k).orElse(new int[0])  (same for Long/Byte arrays)
  .getAllKeys() -> .keySet()                    tag.getAsString() -> tag.asString().orElse("")
Prints the lines it could not change so they can be fixed by hand.
"""
import re
import sys
from collections import defaultdict
from pathlib import Path

ROOT = Path(sys.argv[1])
LOG = Path(sys.argv[2]).read_text(encoding="utf-8-sig", errors="replace").splitlines()

DEFAULTS = {"Int": "0", "Short": "(short) 0", "Byte": "(byte) 0", "Long": "0L", "Float": "0F", "Double": "0D",
            "Boolean": "false", "String": '""'}
ARRAYS = {"IntArray": "new int[0]", "LongArray": "new long[0]", "ByteArray": "new byte[0]"}


def find_close(s, open_idx):
    depth = 0
    for i in range(open_idx, len(s)):
        depth += s[i] == "("
        depth -= s[i] == ")"
        if depth == 0:
            return i
    return -1


def fix_line(line):
    out, i = "", 0
    pat = re.compile(r"\.get(Int|Short|Byte|Long|Float|Double|Boolean|String|Compound|List|IntArray|LongArray|ByteArray)\(")
    while True:
        m = pat.search(line, i)
        if not m:
            return out + line[i:]
        kind = m.group(1)
        open_idx = m.end() - 1
        close = find_close(line, open_idx)
        if close < 0:
            return out + line[i:]
        args = line[open_idx + 1:close]
        if not args.strip():  # zero-arg getters (e.g. Component.getString()) are not NBT
            out += line[i:close + 1]
            i = close + 1
            continue
        if kind in DEFAULTS:
            new = f".get{kind}Or({args}, {DEFAULTS[kind]})"
        elif kind == "Compound":
            new = f".getCompoundOrEmpty({args})"
        elif kind == "List":
            first = args.split(",")[0].strip()
            new = f".getListOrEmpty({first})"
        else:
            new = f".get{kind}({args}).orElse({ARRAYS[kind]})"
        out += line[i:m.start()] + new
        i = close + 1


def main():
    targets = defaultdict(set)
    for raw in LOG:
        m = re.search(r"^(.*\.java):(\d+): error: (.*)", raw.strip())
        if not m:
            continue
        path, ln, msg = m.group(1), int(m.group(2)), m.group(3)
        idx = LOG.index(raw)
        detail = " ".join(LOG[idx:idx + 3])
        # wider window: "cannot find symbol" / "bad operand" details put the Optional type a few lines down
        wide = " ".join(LOG[idx:idx + 7]).split(".java:")[0:2]
        wide = ".java:".join(wide)
        if re.search(r"Optional<\w+(\[\])?> cannot be converted|getList in class CompoundTag|getAllKeys|getAsString|"
                     r"max\(Optional|bad type in conditional", detail) or re.search(
                     r"Optional<(Integer|Long|Short|Byte|Float|Double|Boolean|String|CompoundTag|ListTag)>", wide):
            targets[path].add(ln)
    changed = 0
    for path, lines in targets.items():
        f = Path(path)
        text = f.read_text(encoding="utf-8").split("\n")
        for ln in sorted(lines):
            old = text[ln - 1]
            new = fix_line(old).replace(".getAllKeys()", ".keySet()").replace(".getAsString()", ".asString().orElse(\"\")")
            if new != old:
                text[ln - 1] = new
                changed += 1
            else:
                print(f"unchanged {f.name}:{ln}: {old.strip()}")
        f.write_text("\n".join(text), encoding="utf-8")
    print(f"{changed} lines rewritten")


if __name__ == "__main__":
    main()

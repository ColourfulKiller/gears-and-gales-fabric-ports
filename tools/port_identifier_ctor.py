"""1.21: `new ResourceLocation(...)` (now Identifier, whose constructor is private) -> factory methods, in place.

    python -I ports/tools/port_identifier_ctor.py <src/main/java dir>

  new Identifier(ns, path) -> Identifier.fromNamespaceAndPath(ns, path)
  new Identifier(id)       -> Identifier.parse(id)
Run after port_mechanical.py (which renames ResourceLocation -> Identifier).
"""
import sys
from pathlib import Path


def split_args(s):
    out, depth, cur, quote = [], 0, "", None
    for ch in s:
        if quote:
            cur += ch
            if ch == quote and not cur.endswith("\\" + quote):
                quote = None
            continue
        if ch in "\"'":
            quote = ch
        depth += ch in "([{"
        depth -= ch in ")]}"
        if ch == "," and depth == 0:
            out.append(cur.strip())
            cur = ""
        else:
            cur += ch
    out.append(cur.strip())
    return out


def port(t):
    out, pos, n = "", 0, 0
    opener = "new Identifier("
    while True:
        s = t.find(opener, pos)
        if s < 0:
            return out + t[pos:], n
        open_idx = s + len(opener) - 1
        depth, i, quote = 0, open_idx, None
        while True:
            ch = t[i]
            if quote:
                if ch == quote and t[i - 1] != "\\":
                    quote = None
            elif ch in "\"'":
                quote = ch
            else:
                depth += ch == "("
                depth -= ch == ")"
                if depth == 0:
                    break
            i += 1
        args = split_args(t[open_idx + 1:i])
        if len(args) == 2:
            new = f"Identifier.fromNamespaceAndPath({args[0]}, {args[1]})"
        elif len(args) == 1:
            new = f"Identifier.parse({args[0]})"
        else:
            new = t[s:i + 1]
        out += t[pos:s] + new
        pos = i + 1
        n += 1


def main():
    total = files = 0
    for f in Path(sys.argv[1]).rglob("*.java"):
        src = f.read_text(encoding="utf-8")
        if "new Identifier(" not in src:
            continue
        out, n = port(src)
        if out != src:
            f.write_text(out, encoding="utf-8")
            total += n
            files += 1
    print(f"{total} constructors in {files} files")


if __name__ == "__main__":
    main()

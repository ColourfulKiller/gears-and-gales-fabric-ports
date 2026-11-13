"""Convert a NeoForge accesstransformer.cfg into Fabric access widener lines, checked against the 1.21.11 jar.

    python -I ports/tools/at_to_aw.py <accesstransformer.cfg> <named minecraft jar> <javap executable> [decompiled mc source dir]

Entries whose class / member no longer exists are listed as skipped (they need porting by hand). Field descriptors
come from javap. `public-f` becomes accessible + mutable.
"""
import re
import subprocess
import sys
from functools import lru_cache

cfg, jar, javap = sys.argv[1:4]
MC_SRC = sys.argv[4] if len(sys.argv) > 4 else None


def relocate(cls):
    """Moved class: find the unique top-level class with that simple name in the decompiled sources."""
    if not MC_SRC:
        return None
    from pathlib import Path
    outer, _, nested = cls.partition("$")
    hits = list(Path(MC_SRC).rglob(outer.split(".")[-1] + ".java"))
    if len(hits) != 1:
        return None
    moved = ".".join(hits[0].relative_to(MC_SRC).with_suffix("").parts)
    return moved + ("$" + nested if nested else "")


@lru_cache(maxsize=None)
def members(cls):
    r = subprocess.run([javap, "-p", "-s", "-cp", jar, cls], capture_output=True, text=True)
    if r.returncode != 0:
        return None
    out, fields, methods = r.stdout.splitlines(), {}, set()
    for i, line in enumerate(out):
        d = re.match(r"\s*descriptor: (\S+)", line)
        if not d:
            continue
        decl = out[i - 1]
        name = re.search(r"([\w$<>]+)(\(|;|$)", decl.split("=")[0].strip().rstrip(";").split(" ")[-1] + ("(" if "(" in decl else ";"))
        if "(" in decl:
            mname = re.search(r"([\w$]+)\(", decl)
            if mname:
                n = mname.group(1)
                methods.add((n if n != cls.split(".")[-1].split("$")[-1] else "<init>", d.group(1)))
        else:
            fname = decl.strip().rstrip(";").split(" ")[-1]
            fields[fname] = d.group(1)
    return fields, methods


out, skipped = [], []
for raw in open(cfg, encoding="utf-8"):
    line = raw.split("#")[0].strip()
    if not line:
        continue
    parts = line.split()
    mod, cls = parts[0], parts[1]
    member = parts[2] if len(parts) > 2 else None
    info = members(cls)
    if info is None and relocate(cls):
        cls = relocate(cls)
        info = members(cls)
    if info is None:
        skipped.append(f"class gone: {line}")
        continue
    fields, methods = info
    internal = cls.replace(".", "/")
    final = mod.endswith("-f")
    if member is None:
        out.append(f"accessible class {internal}")
        continue
    if "(" in member:
        name, desc = member[:member.index("(")], member[member.index("("):]
        if (name, desc) not in methods:
            skipped.append(f"method gone: {line}")
            continue
        out.append(f"accessible method {internal} {name} {desc}")
        if final:
            out.append(f"extendable method {internal} {name} {desc}")
    else:
        if member not in fields:
            skipped.append(f"field gone: {line}")
            continue
        out.append(f"accessible field {internal} {member} {fields[member]}")
        if final:
            out.append(f"mutable field {internal} {member} {fields[member]}")
for l in dict.fromkeys(out):
    print(l)
for s in skipped:
    print("# SKIPPED " + s, file=sys.stderr)

"""1.21.2: Entity#hurt(DamageSource, float) became final; subclasses override hurtServer(ServerLevel, DamageSource, float).

    python -I ports/tools/port_hurt_server.py <src/main/java dir>

Rewrites `boolean hurt(<annotations> [final] DamageSource a, [final] float b)` declarations and the `super.hurt(`
calls in the same file. Idempotent.
"""
import re
import sys
from pathlib import Path

DECL = re.compile(r"(public\s+boolean\s+)hurt\(((?:@\w+\s+)*(?:final\s+)?(?:@\w+\s+)*DamageSource\s+\w+\s*,\s*(?:final\s+)?float\s+\w+)\)")
n = 0
for f in Path(sys.argv[1]).rglob("*.java"):
    t = f.read_text(encoding="utf-8")
    if not DECL.search(t):
        continue
    t = DECL.sub(r"\1hurtServer(final net.minecraft.server.level.ServerLevel hurtLevel, \2)", t)
    t = t.replace("super.hurt(", "super.hurtServer(hurtLevel, ")
    f.write_text(t, encoding="utf-8")
    n += 1
print(n, "files")

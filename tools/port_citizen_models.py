"""MineColonies 1.21.11: citizen job models animate from CitizenRenderState instead of the entity.

    python -I ports/tools/port_citizen_models.py <src/main/java dir>

- CitizenModel<...> / CitizenModel<> -> CitizenModel (no longer generic), everywhere
- in classes extending CitizenModel: setupAnim(AbstractEntityCitizen e, 5 floats) -> setupAnim(CitizenRenderState e),
  super.setupAnim(e, ...) -> super.setupAnim(e), getActualRotation(AbstractEntityCitizen) -> (CitizenRenderState)
Idempotent.
"""
import re
import sys
from pathlib import Path

IMPORT = "import com.minecolonies.api.client.render.modeltype.CitizenRenderState;"
n = 0
for f in Path(sys.argv[1]).rglob("*.java"):
    t = f.read_text(encoding="utf-8")
    o = t
    t = re.sub(r"\bCitizenModel<[\w<>?, ]*>", "CitizenModel", t)
    if re.search(r"extends CitizenModel\b", t) and f.name != "CitizenModel.java":
        t = re.sub(r"setupAnim\((?:@NotNull )?(?:final )?AbstractEntityCitizen (\w+)(?:, (?:final )?float \w+){5}\)",
                   r"setupAnim(@NotNull final CitizenRenderState \1)", t)
        t = re.sub(r"super\.setupAnim\((\w+)(?:, \w+){5}\)", r"super.setupAnim(\1)", t)
        t = re.sub(r"getActualRotation\((@NotNull )?(final )?AbstractEntityCitizen (\w+)\)", r"getActualRotation(\1\2CitizenRenderState \3)", t)
        t = re.sub(r"(isWorking|displayHat)\((final )?AbstractEntityCitizen (\w+)\)", r"\1(\2CitizenRenderState \3)", t)
        if IMPORT not in t and "package com.minecolonies.api.client.render.modeltype;" not in t:
            t = re.sub(r"(package [\w.]+;\r?\n)", r"\1\n" + IMPORT + "\n", t, count=1)
    if t != o:
        f.write_text(t, encoding="utf-8")
        n += 1
print(n, "files")

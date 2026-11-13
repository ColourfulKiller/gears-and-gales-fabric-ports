"""Common 1.21.1 -> 1.21.11 API renames for the LDT Team ports, applied in place.

    python -I ports/tools/port_api_fixes.py <src/main/java dir>

- Screen.hasShiftDown/hasControlDown/hasAltDown moved to Minecraft (instance methods), incl. method references
- RenderSystem blend / depth toggles are gone (the GUI pipelines carry that state): standalone calls are deleted
- RenderSystem.setShaderColor -> UiRenderMacros.setTint (BlockUI's per-blit tint)
- BuiltInRegistries.X.get(id) returned the value; it now returns Optional<Holder.Reference>, getValue(id) is the old one
- adds a missing org.joml.Matrix3x2fStack import
"""
import re
import sys
from pathlib import Path

DELETE_LINES = re.compile(
    r"^[ \t]*RenderSystem\.(enableBlend|disableBlend|defaultBlendFunc|enableDepthTest|disableDepthTest)\(\);[ \t]*\n", re.M)


def add_import(src, imp):
    if f"import {imp};" in src:
        return src
    return re.sub(r"(^package [\w.]+;\n)", rf"\1\nimport {imp};", src, count=1, flags=re.M)


def main():
    root = Path(sys.argv[1])
    changed = 0
    for f in sorted(root.rglob("*.java")):
        if "com/ldtteam/fabric" in f.as_posix():
            continue
        src = f.read_text(encoding="utf-8")
        out = src
        for key in ("Shift", "Control", "Alt"):
            out = out.replace(f"Screen::has{key}Down", f"() -> Minecraft.getInstance().has{key}Down()")
            out = out.replace(f"Screen.has{key}Down()", f"Minecraft.getInstance().has{key}Down()")
        out = DELETE_LINES.sub("", out)
        out = re.sub(r"\bRenderSystem\.setShaderColor\(", "UiRenderMacros.setTint(", out)
        out = re.sub(r"\b(BuiltInRegistries\.\w+)\.get\(", r"\1.getValue(", out)
        if "Minecraft.getInstance().has" in out:
            out = add_import(out, "net.minecraft.client.Minecraft")
        if "UiRenderMacros.setTint" in out and "package com.ldtteam.blockui;" not in out:
            out = add_import(out, "com.ldtteam.blockui.UiRenderMacros")
        if re.search(r"\bMatrix3x2fStack\b", out):
            out = add_import(out, "org.joml.Matrix3x2fStack")
        if out != src:
            f.write_text(out, encoding="utf-8")
            changed += 1
    print(f"{changed} files changed")


if __name__ == "__main__":
    main()

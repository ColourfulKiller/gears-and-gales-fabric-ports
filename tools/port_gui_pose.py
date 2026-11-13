"""BlockUI GUI code: PoseStack (3D) -> Matrix3x2fStack (2D), for Minecraft 1.21.6+ GUI rendering.

    python -I ports/tools/port_gui_pose.py <src/main/java dir> [--skip <path fragment> ...]

In GUI code that gets its stack from target.pose():
  PoseStack -> Matrix3x2fStack, pushPose/popPose -> pushMatrix/popMatrix,
  translate(x, y, z) -> translate(x, y), scale(x, y, z) -> scale(x, y),
  UiRenderMacros calls with (ms, ...) or (target.pose(), ...) -> (target, ...).
Files that render in 3D (world hooks) are passed with --skip and done by hand.
Prints every z argument it drops so a human can check the ones that weren't 0 / 1.
"""
import re
import sys
from pathlib import Path

MACROS = ("drawLineRect", "drawLineRectGradient", "fill", "fillGradient", "hLine", "vLine", "line", "blit",
          "blitSprite", "blitRepeatable", "drawEntity")


def split_args(s):
    """Split a call's argument string on top-level commas."""
    out, depth, cur = [], 0, ""
    for ch in s:
        if ch in "([{":
            depth += 1
        elif ch in ")]}":
            depth -= 1
        if ch == "," and depth == 0:
            out.append(cur)
            cur = ""
        else:
            cur += ch
    out.append(cur)
    return out


def call_end(src, open_idx):
    """Index of the parenthesis closing the one at open_idx."""
    depth = 0
    for i in range(open_idx, len(src)):
        if src[i] == "(":
            depth += 1
        elif src[i] == ")":
            depth -= 1
            if depth == 0:
                return i
    raise ValueError("unbalanced")


def drop_third_arg(src, method, receivers, dropped, fname):
    pat = re.compile(r"\b(" + "|".join(receivers) + r")\." + method + r"\(")
    pos = 0
    while True:
        m = pat.search(src, pos)
        if not m:
            return src
        open_idx = m.end() - 1
        close = call_end(src, open_idx)
        args = split_args(src[open_idx + 1:close])
        if len(args) == 3:
            dropped.append(f"{fname}: {method} z={args[2].strip()}")
            new = ",".join(args[:2])
            src = src[:open_idx + 1] + new + src[close:]
            pos = open_idx + 1 + len(new)
        else:
            pos = close


def main():
    root = Path(sys.argv[1])
    skips = [sys.argv[i + 1] for i, a in enumerate(sys.argv) if a == "--skip"]
    dropped, changed = [], 0
    for f in sorted(root.rglob("*.java")):
        rel = f.relative_to(root).as_posix()
        if any(s in rel for s in skips):
            continue
        src = f.read_text(encoding="utf-8")
        out = src
        # receivers: variables assigned from target.pose() / graphics.pose(), plus pose() itself
        receivers = set(re.findall(r"PoseStack\s+(\w+)\s*=\s*\w+\.pose\(\)", out)) | {r"\w+\.pose\(\)", r"pose\(\)"}
        out = re.sub(r"\bPoseStack(\s+\w+\s*=\s*\w+\.pose\(\))", r"Matrix3x2fStack\1", out)
        recv = sorted(receivers, key=len, reverse=True)
        for r in recv:
            out = re.sub(r"\b" + r + r"\.pushPose\(\)", lambda m: m.group(0).replace("pushPose", "pushMatrix"), out)
            out = re.sub(r"\b" + r + r"\.popPose\(\)", lambda m: m.group(0).replace("popPose", "popMatrix"), out)
        out = drop_third_arg(out, "translate", recv, dropped, rel)
        out = drop_third_arg(out, "scale", recv, dropped, rel)
        out = re.sub(r"\b(" + "|".join(MACROS) + r")\((?:ms|ps|poseStack|matrixStack|\w+\.pose\(\))\s*,",
                     r"\1(target,", out)
        if "Matrix3x2fStack" in out and "import org.joml.Matrix3x2fStack;" not in out:
            out = re.sub(r"(^package [\w.]+;\n)", r"\1\nimport org.joml.Matrix3x2fStack;", out, count=1, flags=re.M)
        if "PoseStack" not in out.replace("Matrix3x2fStack", ""):
            out = out.replace("import com.mojang.blaze3d.vertex.PoseStack;\n", "")
        if out != src:
            f.write_text(out, encoding="utf-8")
            changed += 1
    for d in dropped:
        print("  dropped " + d)
    print(f"{changed} files changed")


if __name__ == "__main__":
    main()

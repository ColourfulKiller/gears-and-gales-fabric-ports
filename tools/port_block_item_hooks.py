"""NeoForge block/item extension hooks -> Fabric 1.21.11 shim, applied in place.

    python -I ports/tools/port_block_item_hooks.py <src/main/java dir>

- super::getSoundType / super::getExplosionResistance (NeoForge IBlockExtension defaults)
    -> PositionalBlockHooks::defaultSoundType / ::defaultExplosionResistance
- PropertyDispatch.QuadFunction (removed in 1.21.11) -> PositionalBlockHooks.QuadFunction
- Item.appendHoverText(stack, ctx, List<Component> tooltip, flag)
    -> appendHoverText(stack, ctx, TooltipDisplay display, Consumer<Component> tooltipAdder, flag) with
       `List<Component> tooltip = TooltipList.of(tooltipAdder);` as the first statement, so the body is unchanged.
Classes still have to implement PositionalBlockHooks (directly or through an interface) for their overrides to apply.
"""
import re
import sys
from pathlib import Path

HOOKS = "com.ldtteam.fabric.block.PositionalBlockHooks"

SIG = re.compile(
    r"(public\s+void\s+appendHoverText\(\s*)"
    r"((?:@\w+\s+)*(?:final\s+)?(?:@\w+\s+)*ItemStack\s+\w+)\s*,\s*"
    r"((?:@\w+\s+)*(?:final\s+)?(?:@\w+\s+)*(?:Item\.)?TooltipContext\s+\w+)\s*,\s*"
    r"(?:@\w+\s+)*(?:final\s+)?(?:@\w+\s+)*List<Component>\s+(\w+)\s*,\s*"
    r"((?:@\w+\s+)*(?:final\s+)?(?:@\w+\s+)*TooltipFlag\s+\w+)\s*\)\s*\{")


# 1.21.2: updateShape(state, dir, neighborState, LevelAccessor level, pos, neighborPos)
#      -> updateShape(state, LevelReader level, ScheduledTickAccess ticks, pos, dir, neighborPos, neighborState, RandomSource random)
UPDATE_SHAPE = re.compile(
    r"(public|protected)\s+BlockState\s+updateShape\(\s*"
    r"(?:@\w+\s+)*(?:final\s+)?(?:@\w+\s+)*BlockState\s+(\w+)\s*,\s*"
    r"(?:@\w+\s+)*(?:final\s+)?(?:@\w+\s+)*Direction\s+(\w+)\s*,\s*"
    r"(?:@\w+\s+)*(?:final\s+)?(?:@\w+\s+)*BlockState\s+(\w+)\s*,\s*"
    r"(?:@\w+\s+)*(?:final\s+)?(?:@\w+\s+)*LevelAccessor\s+(\w+)\s*,\s*"
    r"(?:@\w+\s+)*(?:final\s+)?(?:@\w+\s+)*BlockPos\s+(\w+)\s*,\s*"
    r"(?:@\w+\s+)*(?:final\s+)?(?:@\w+\s+)*BlockPos\s+(\w+)\s*\)")


def port_update_shape(src):
    m = UPDATE_SHAPE.search(src)
    if not m:
        return src
    vis, state, direction, neighbor, level, pos, npos = m.groups()
    header = (f"{vis} BlockState updateShape(final BlockState {state}, final net.minecraft.world.level.LevelReader {level}, "
              f"final net.minecraft.world.level.ScheduledTickAccess scheduledTicks, final BlockPos {pos}, final Direction {direction}, "
              f"final BlockPos {npos}, final BlockState {neighbor}, final net.minecraft.util.RandomSource random)")
    # body: the method's braces
    start = src.index("{", m.end())
    depth, i = 0, start
    while True:
        if src[i] == "{":
            depth += 1
        elif src[i] == "}":
            depth -= 1
            if depth == 0:
                break
        i += 1
    body = src[start:i + 1]
    body = re.sub(rf"\b{level}\.scheduleTick\(", "scheduledTicks.scheduleTick(", body)
    body = re.sub(r"super\.updateShape\(\s*(\w+)\s*,\s*(\w+)\s*,\s*(\w+)\s*,\s*(\w+)\s*,\s*(\w+)\s*,\s*(\w+)\s*\)",
                  r"super.updateShape(\1, \4, scheduledTicks, \5, \2, \6, \3, random)", body)
    return src[:m.start()] + header + "\n    " + body + port_update_shape(src[i + 1:])


def add_import(src, imp):
    if f"import {imp};" in src:
        return src
    return re.sub(r"(^package [\w.]+;\n)", rf"\1\nimport {imp};", src, count=1, flags=re.M)


def main():
    root = Path(sys.argv[1])
    changed = 0
    for f in sorted(root.rglob("*.java")):
        src = f.read_text(encoding="utf-8")
        out = src
        out = out.replace("super::getSoundType", "PositionalBlockHooks::defaultSoundType")
        out = out.replace("super::getExplosionResistance", "PositionalBlockHooks::defaultExplosionResistance")
        out = out.replace("PropertyDispatch.QuadFunction", "PositionalBlockHooks.QuadFunction")
        out = re.sub(r"import net\.minecraft\.data\.models\.blockstates\.PropertyDispatch;\n", "", out)
        out = port_update_shape(out)
        # 1.21.2+: DirectionProperty is gone (EnumProperty<Direction>); wall side properties renamed; isClientSide is a method
        out = re.sub(r"\bDirectionProperty\b", "EnumProperty<Direction>", out)
        out = out.replace("import net.minecraft.world.level.block.state.properties.EnumProperty<Direction>;",
                          "import net.minecraft.world.level.block.state.properties.EnumProperty;")
        out = re.sub(r"\bWallBlock\.(NORTH|EAST|SOUTH|WEST)_WALL\b", r"WallBlock.\1", out)
        out = re.sub(r"\.isClientSide\b(?!\s*\()", ".isClientSide()", out)
        out = out.replace(".noCollission()", ".noCollision()")
        if "EnumProperty<Direction>" in out:
            out = add_import(out, "net.minecraft.world.level.block.state.properties.EnumProperty")
            out = add_import(out, "net.minecraft.core.Direction")

        def sig(m):
            list_name = m.group(4)
            return (f"{m.group(1)}{m.group(2)}, {m.group(3)}, final net.minecraft.world.item.component.TooltipDisplay tooltipDisplay, "
                    f"final java.util.function.Consumer<Component> {list_name}Adder, {m.group(5)})\n    {{\n"
                    f"        final List<Component> {list_name} = com.ldtteam.fabric.TooltipList.of({list_name}Adder);")
        out, n = SIG.subn(sig, out)
        if n:
            # super.appendHoverText(stack, ctx, tooltip, flag) -> new signature
            out = re.sub(r"super\.appendHoverText\(\s*(\w+)\s*,\s*(\w+)\s*,\s*(\w+)\s*,\s*(\w+)\s*\)",
                         r"super.appendHoverText(\1, \2, tooltipDisplay, \3Adder, \4)", out)
        if "PositionalBlockHooks" in out and "package com.ldtteam.fabric.block;" not in out:
            out = add_import(out, HOOKS)
        if out != src:
            f.write_text(out, encoding="utf-8")
            changed += 1
    print(f"{changed} files changed")


if __name__ == "__main__":
    main()

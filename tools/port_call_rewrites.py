"""Rewrite method calls whose shape changed (receiver / argument moves), in place.

    python -I ports/tools/port_call_rewrites.py <src/main/java dir> <javac error log>

Each rule: (method, arg count, template, scope). The template uses {recv} (the receiver expression) and {a0}, {a1}...
Scope "global" rewrites every matching call; "errline" only calls on lines where javac reported `method <name>`
missing (for names that still exist on other types). Rules live in RULES below; add to them as the ports need.
"""
import re
import sys
from collections import defaultdict
from pathlib import Path

H = "com.ldtteam.fabric"
RULES = [
    # 1.21.5 CompoundTag UUID helpers -> codecs
    ("putUUID", 2, "{recv}.store({a0}, net.minecraft.core.UUIDUtil.CODEC, {a1})", "global"),
    ("getUUID", 1, "{recv}.read({a0}, net.minecraft.core.UUIDUtil.CODEC).orElse(null)", "global"),
    ("hasUUID", 1, "{recv}.read({a0}, net.minecraft.core.UUIDUtil.CODEC).isPresent()", "global"),
    # 1.21.2 level height
    ("getMaxBuildHeight", 0, "({recv}.getMaxY() + 1)", "global"),
    # NeoForge entity / block / item extensions
    ("isAddedToLevel", 0, H + ".PlayerHooks.isAddedToLevel({recv})", "global"),
    ("isBed", 4, H + ".block.PositionalBlockHooks.isBed({a0}, {a1}, {a2}, {a3})", "global"),
    ("isBed", 3, H + ".block.PositionalBlockHooks.isBed({recv}, {a0}, {a1}, {a2})", "global"),
    ("isLadder", 4, H + ".block.PositionalBlockHooks.isLadder({a0}, {a1}, {a2}, {a3})", "global"),
    ("isLadder", 3, H + ".block.PositionalBlockHooks.isLadder({recv}, {a0}, {a1}, {a2})", "global"),
    ("getFoodProperties", 2, "{a0}.get(net.minecraft.core.component.DataComponents.FOOD)", "global"),
    ("getFoodProperties", 1, "{recv}.get(net.minecraft.core.component.DataComponents.FOOD)", "global"),
    ("getCraftingRemainingItem", 0, "{recv}.getItem().getRecipeRemainder({recv})", "global"),
    # vanilla removals
    ("hasPermission", 1, H + ".PlayerHooks.hasPermission({recv}, {a0})", "errline"),
    ("getServer", 0, "{recv}.level().getServer()", "errline"),  # errline rules: run once per fresh compile log
    ("getItems", 0, H + ".IngredientHooks.getItems({recv})", "errline"),
    ("setUnsaved", 1, "{recv}.markUnsaved()", "errline"),
    ("serverLevel", 0, "{recv}.level()", "global"),
    # 1.21.2 recipe API removals -> com.ldtteam.fabric.crafting.RecipeHooks
    ("getResultItem", 1, H + ".crafting.RecipeHooks.getResultItem({recv}, {a0})", "global"),
    ("getAllRecipesFor", 1, H + ".crafting.RecipeHooks.getAllRecipesFor({recv}, {a0})", "global"),
    ("canCraftInDimensions", 2, H + ".crafting.RecipeHooks.canCraftInDimensions({recv}, {a0}, {a1})", "global"),
    ("getIngredients", 0, H + ".crafting.RecipeHooks.getIngredients({recv})", "errline"),
    # 1.21.2 LootContext#getParamOrNull -> getOptionalParameter
    ("getParamOrNull", 1, "{recv}.getOptionalParameter({a0})", "global"),
    # 1.21.2 / 1.21.5 signature changes, keyed on javac's message for the line
    ("getSoundType", 4, "{a0}.getSoundType()", "msg:method getSoundType in class"),
    ("isSolidRender", 2, "{recv}.isSolidRender()", "msg:method isSolidRender in class"),
    ("spawnAtLocation", 2, "{recv}.spawnAtLocation((net.minecraft.server.level.ServerLevel) {recv}.level(), {a0}, {a1})", "msg:no suitable method found for spawnAtLocation"),
    ("spawnAtLocation", 1, "{recv}.spawnAtLocation((net.minecraft.server.level.ServerLevel) {recv}.level(), {a0})", "msg:no suitable method found for spawnAtLocation"),
    ("hurtAndBreak", 4, "{recv}.hurtAndBreak({a0}, {a1}, null, {a3})", "msg:cannot be converted to ServerPlay"),
    ("getTagEnchantments", 0, "{recv}.getEnchantments()", "global"),
    ("getTagEnchantmentLevel", 2, "{recv}.getItemEnchantmentLevel({a0}, {a1})", "global"),
    ("getPickedResult", 1, "{recv}.getPickResult()", "global"),
    ("saveOptional", 1, H + ".nbt.NbtIO.saveOptional({recv}, {a0})", "msg:method saveOptional\\("),
    ("contains", 2, "{recv}.contains({a0})", "msg:method contains in class CompoundTag"),
    ("isAreaLoaded", 2, "{recv}.isLoaded({a0})", "msg:method isAreaLoaded\\("),
    ("getMaxDamage", 1, "{a0}.getMaxDamage()", "msg:method getMaxDamage\\(ItemStack\\)"),
    ("getHolder", 1, "{recv}.get({a0})", "msg:method getHolder\\("),
    ("getTag", 1, "{recv}.get({a0})", "msg:method getTag\\(TagKey"),
    ("saveWithId", 1, H + ".nbt.NbtIO.saveWithId(this, {a0})", "msg:Provider cannot be converted to ValueOutput"),
    ("hasPermissions", 1, H + ".PlayerHooks.hasPermission({recv}, {a0})", "msg:method hasPermissions\\("),
    ("isInput", 1, "{a0}.has(net.minecraft.core.component.DataComponents.POTION_CONTENTS)", "msg:method isInput\\("),
    ("getSoundType", 3, "{recv}.getSoundType()", "msg:method getSoundType in class BlockStateBase"),
    ("getEnchantmentLevel", 1, "net.minecraft.world.item.enchantment.EnchantmentHelper.getItemEnchantmentLevel({a0}, {recv})", "msg:method getEnchantmentLevel\\("),
    ("restrictTo", 2, "{recv}.setHomeTo({a0}, {a1})", "global"),
    ("clearRestriction", 0, "{recv}.clearHome()", "global"),
    ("getLightEmission", 2, "{recv}.getLightEmission()", "msg:method getLightEmission in class BlockStateBase"),
    ("supportsEnchantment", 1, "{a0}.value().isSupportedItem({recv})", "global"),
    ("applyBonemeal", 4, "{recv}.growCrop({a0}, {a1}, {a2})", "msg:method applyBonemeal\\(ItemStack"),
    ("getCloneItemStack", 3, "{recv}.getCloneItemStack({a0}, {a1}, {a2}, false)", "msg:method getCloneItemStack in class"),
    ("getOrCreateTag", 1, "{recv}.getOrThrow({a0})", "global"),
    ("holderOrThrow", 1, "{recv}.getOrThrow({a0})", "global"),
    ("save", 1, H + ".nbt.NbtIO.save({recv}, {a0})", "msg:method save\\(Provider\\)"),
    # NeoForge menus with extra data -> com.ldtteam.fabric.MenuHooks (Fabric extended menu types)
    ("openMenu", 2, H + ".MenuHooks.openMenu({recv}, {a0}, {a1})", "global"),
]

IDENT = re.compile(r"[\w$]")


def receiver_start(t, dot):
    """Index where the receiver expression ending right before `dot` starts."""
    i = dot - 1
    while True:
        while i >= 0 and t[i].isspace():
            i -= 1
        if i < 0:
            return 0
        if t[i] == ")" or t[i] == "]":
            close, open_ = t[i], "(" if t[i] == ")" else "["
            depth = 0
            while i >= 0:
                depth += t[i] == close
                depth -= t[i] == open_
                if depth == 0:
                    break
                i -= 1
            i -= 1
            # a call: include the method name before the paren
            while i >= 0 and IDENT.match(t[i]):
                i -= 1
        elif IDENT.match(t[i]):
            while i >= 0 and IDENT.match(t[i]):
                i -= 1
        else:
            return i + 1
        # continue through a.b.c chains
        j = i
        while j >= 0 and t[j].isspace():
            j -= 1
        if j >= 0 and t[j] == ".":
            i = j - 1
            continue
        return i + 1


def split_args(s):
    out, depth, cur, quote = [], 0, "", None
    for ch in s:
        if quote:
            cur += ch
            if ch == quote:
                quote = None
            continue
        if ch in "\"'":
            quote = ch
        depth += ch in "([{<" if ch != "<" else 0
        depth -= ch in ")]}"
        if ch == "," and depth == 0:
            out.append(cur.strip())
            cur = ""
        else:
            cur += ch
    if cur.strip():
        out.append(cur.strip())
    return out


def rewrite(t, method, nargs, template, allowed_lines):
    pos = 0
    out = []
    pat = re.compile(rf"\.{method}\(")
    while True:
        m = pat.search(t, pos)
        if not m:
            out.append(t[pos:])
            return "".join(out)
        line_no = t.count("\n", 0, m.start()) + 1
        open_idx = m.end() - 1
        depth, i = 0, open_idx
        while i < len(t):
            depth += t[i] == "("
            depth -= t[i] == ")"
            if depth == 0:
                break
            i += 1
        args = split_args(t[open_idx + 1:i])
        start = receiver_start(t, m.start())
        recv = t[start:m.start()].strip()
        ok = len(args) == nargs and recv and (allowed_lines is None or line_no in allowed_lines) and "fabric." not in recv
        if not ok:
            out.append(t[pos:m.end()])
            pos = m.end()
            continue
        fields = {"recv": recv, **{f"a{k}": a for k, a in enumerate(args)}}
        out.append(t[pos:start] + template.format(**fields))
        pos = i + 1


def main():
    root = Path(sys.argv[1])
    log = Path(sys.argv[2]).read_text(encoding="utf-8-sig", errors="replace").splitlines()
    err = defaultdict(lambda: defaultdict(set))  # method -> file -> lines
    details = defaultdict(lambda: defaultdict(str))  # file -> line -> error text (for msg: scopes)
    for k, line in enumerate(log):
        e = re.match(r"^(.*\.java):(\d+): error: ", line)
        if e:
            details[str(Path(e.group(1)).resolve())][int(e.group(2))] += " ".join(log[k:k + 6]) + "\n"
        m = re.match(r"^(.*\.java):(\d+): error: cannot find symbol", line)
        if not m:
            continue
        for j in range(k + 1, min(k + 6, len(log))):
            s = re.search(r"symbol:\s+method (\w+)\(", log[j])
            if s:
                err[s.group(1)][str(Path(m.group(1)).resolve())].add(int(m.group(2)))
                break
    changed = 0
    for f in root.rglob("*.java"):
        t = f.read_text(encoding="utf-8")
        new = t
        for method, nargs, template, scope in RULES:
            if f".{method}(" not in new:
                continue
            if scope == "global":
                allowed = None
            elif scope.startswith("msg:"):
                allowed = {ln for ln, txt in details[str(f.resolve())].items() if re.search(scope[4:], txt)}
            else:
                allowed = err[method].get(str(f.resolve()), set())
            if allowed is not None and not allowed:
                continue
            new = rewrite(new, method, nargs, template, allowed)
        if new != t:
            f.write_text(new, encoding="utf-8")
            changed += 1
    print(f"{changed} files changed")


if __name__ == "__main__":
    main()

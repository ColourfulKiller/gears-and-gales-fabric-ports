"""1.21.2: InteractionResultHolder<T> is gone; Item#use returns InteractionResult. Applied in place.

    python -I ports/tools/port_interaction_result.py <src/main/java dir>

  InteractionResultHolder<ItemStack>            -> InteractionResult
  new InteractionResultHolder<>(result, stack)  -> result
  InteractionResultHolder.success/consume/pass/fail(stack) -> InteractionResult.SUCCESS/CONSUME/PASS/FAIL
  holder.getResult() -> holder (callers that unwrapped it)
A changed held stack (the holder's second value) is not carried over; uses that relied on it need a manual
player.setItemInHand. The script prints those (non-trivial second arguments).
"""
import re
import sys
from pathlib import Path


def split_args(s):
    out, depth, cur = [], 0, ""
    for ch in s:
        depth += ch in "([{<"
        depth -= ch in ")]}>"
        if ch == "," and depth == 0:
            out.append(cur.strip())
            cur = ""
        else:
            cur += ch
    out.append(cur.strip())
    return out


def replace_calls(t, opener, build, report):
    out, pos = "", 0
    while True:
        s = t.find(opener, pos)
        if s < 0:
            return out + t[pos:]
        open_idx = s + len(opener) - 1
        depth, i = 0, open_idx
        while True:
            depth += t[i] == "("
            depth -= t[i] == ")"
            if depth == 0:
                break
            i += 1
        args = split_args(t[open_idx + 1:i])
        out += t[pos:s] + build(args, report)
        pos = i + 1


def main():
    for f in sorted(Path(sys.argv[1]).rglob("*.java")):
        t = f.read_text(encoding="utf-8")
        if "InteractionResultHolder" not in t:
            continue
        notes = []

        def ctor(args, report):
            if len(args) == 2 and not re.fullmatch(r"\w+", args[1]):
                report.append(f"held stack expression dropped: {args[1][:60]}")
            return args[0]

        t = replace_calls(t, "new InteractionResultHolder<>(", ctor, notes)
        for kind, const in (("success", "SUCCESS"), ("consume", "CONSUME"), ("pass", "PASS"), ("fail", "FAIL"),
                            ("sidedSuccess", "SUCCESS")):
            t = replace_calls(t, f"InteractionResultHolder.{kind}(", lambda a, r, c=const: f"InteractionResult.{c}", notes)
        t = re.sub(r"InteractionResultHolder<[\w.]+>", "InteractionResult", t)
        t = t.replace("import net.minecraft.world.InteractionResultHolder;\n", "")
        if "import net.minecraft.world.InteractionResult;" not in t:
            t = re.sub(r"(^package [\w.]+;\n)", r"\1\nimport net.minecraft.world.InteractionResult;", t, count=1, flags=re.M)
        f.write_text(t, encoding="utf-8")
        print(f"{f.name}: converted" + "".join(f"\n    NOTE {n}" for n in notes))


if __name__ == "__main__":
    main()

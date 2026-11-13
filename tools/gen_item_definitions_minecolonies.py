"""Generate MineColonies' 1.21.4+ item model definitions (assets/minecolonies/items/*.json).

    python -I ports/tools/gen_item_definitions_minecolonies.py <minecolonies 1.21.1 release jar> <out items dir>

Reads the ORIGINAL item models from the release jar (the ported resources no longer carry loaders / overrides):
- plain item models -> {"type": "minecraft:model"}; models whose parent chain uses Domum's materially_textured
  loader -> {"type": "domum_ornamentum:materially_textured"} (the Domum Fabric port retextures from the stack)
- build_goggles ("disabled" override) -> condition minecolonies:goggles_disabled
- spear (separate perspectives + "throwing" override) -> select on display context; gui/ground/fixed use the flat
  sprite, everything else the "minecolonies:spear" special renderer, held up while being thrown (using_item).
"""
import json
import sys
import zipfile
from pathlib import Path

JAR = zipfile.ZipFile(sys.argv[1])
OUT = Path(sys.argv[2])
PREFIX = "assets/minecolonies/models/"


def load(ref):
    ns, path = (ref if ":" in ref else "minecraft:" + ref).split(":", 1)
    if ns != "minecolonies":
        return None
    try:
        return json.loads(JAR.read(PREFIX + path + ".json"))
    except KeyError:
        return None


def retexturable(ref):
    seen = set()
    m = load(ref)
    while m is not None and ref not in seen:
        seen.add(ref)
        if m.get("loader", "").endswith(":materially_textured"):
            return True
        ref = m.get("parent")
        m = load(ref) if ref else None
    return False


def model(ref):
    return {"type": "domum_ornamentum:materially_textured" if retexturable(ref) else "minecraft:model", "model": ref}


SPECIAL = {
    "build_goggles": {"type": "minecraft:condition", "property": "minecolonies:goggles_disabled",
                      "on_true": {"type": "minecraft:model", "model": "minecolonies:item/build_goggles_disabled"},
                      "on_false": {"type": "minecraft:model", "model": "minecolonies:item/build_goggles"}},
    "spear": {"type": "minecraft:select", "property": "minecraft:display_context",
              "cases": [{"when": ["gui", "ground", "fixed"], "model": {"type": "minecraft:model", "model": "minecolonies:item/spear"}}],
              "fallback": {"type": "minecraft:condition", "property": "minecraft:using_item",
                           "on_true": {"type": "minecraft:special", "base": "minecolonies:item/spear_throwing",
                                       "model": {"type": "minecolonies:spear"}},
                           "on_false": {"type": "minecraft:special", "base": "minecolonies:item/spear_in_hand",
                                        "model": {"type": "minecolonies:spear"}}}},
}
SKIP = {"build_goggles_disabled", "spear_in_hand", "spear_throwing"}


def main():
    OUT.mkdir(parents=True, exist_ok=True)
    count = 0
    for name in sorted(JAR.namelist()):
        if not (name.startswith(PREFIX + "item/") and name.endswith(".json")) or name.count("/") != 4:
            continue
        item = name.rsplit("/", 1)[1][:-5]
        if item in SKIP or item.endswith("_spec"):
            continue
        definition = SPECIAL.get(item) or model(f"minecolonies:item/{item}")
        (OUT / f"{item}.json").write_text(json.dumps({"model": definition}, indent=2) + "\n", encoding="utf-8")
        count += 1
    print(count, "item definitions")


if __name__ == "__main__":
    main()

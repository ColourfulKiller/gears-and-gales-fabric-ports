"""Generate 1.21.4+ item model definitions (assets/<modid>/items/*.json) from 1.21.1 NeoForge item models.

    python -I ports/tools/gen_item_definitions.py <assets/<modid> dir (original, un-flattened models)> <out items dir>

Run on the ORIGINAL models (before port_neoforge_models.py drops "loader"/"overrides").
- An item model whose chain uses the <modid>:materially_textured loader -> {"type": "<modid>:materially_textured"}
  (retextured from the stack's texture data by the Fabric port), otherwise a plain "minecraft:model".
- NeoForge "overrides" on <modid>:*_type predicates (index into the block's TYPE enum, read from the stack's
  block_state component) -> "minecraft:select" on block_state "type"; the case value is parsed from the override
  model name (<kind>_<serialized type>_spec).
"""
import json
import re
import sys
from pathlib import Path

ASSETS = Path(sys.argv[1])
OUT = Path(sys.argv[2])
MODID = ASSETS.name
MODELS = ASSETS / "models"
TYPE_RE = re.compile(r"^(?:door|trapdoor|panel|post)_(.+)_spec$")


def load(ref):
    ns, path = (ref if ":" in ref else "minecraft:" + ref).split(":", 1)
    f = MODELS / (path + ".json")
    return json.loads(f.read_text(encoding="utf-8")) if ns == MODID and f.exists() else None


def chain(ref):
    m = load(ref)
    while m is not None:
        yield m
        for child in m.get("children", {}).values():
            yield child
            if "parent" in child:
                yield from chain(child["parent"])
        m = load(m["parent"]) if "parent" in m else None


def retexturable(ref):
    return any(m.get("loader") == f"{MODID}:materially_textured" for m in chain(ref))


def leaf(ref, force_retexture=False):
    kind = f"{MODID}:materially_textured" if force_retexture or retexturable(ref) else "minecraft:model"
    return {"type": kind, "model": ref}


def main():
    OUT.mkdir(parents=True, exist_ok=True)
    count = selects = 0
    for f in sorted((MODELS / "item").glob("*.json")):
        name = f.stem
        if name.endswith("_spec"):
            continue  # override holders, referenced as parents
        ref = f"{MODID}:item/{name}"
        overrides = next((m["overrides"] for m in chain(ref) if "overrides" in m), None)
        if overrides:
            cases = []
            for o in sorted(overrides, key=lambda o: next(iter(o["predicate"].values()))):
                m = TYPE_RE.match(o["model"].rsplit("/", 1)[1])
                if not m:
                    raise ValueError(f"{name}: can't read type from {o['model']}")
                # the loader sat on the item model holding the overrides, not on the override targets
                cases.append({"when": m.group(1), "model": leaf(o["model"], retexturable(ref))})
            model = {"type": "minecraft:select", "property": "minecraft:block_state", "block_state_property": "type",
                     "cases": cases, "fallback": cases[0]["model"]}
            selects += 1
        else:
            model = leaf(ref)
        (OUT / f"{name}.json").write_text(json.dumps({"model": model}, indent=2) + "\n", encoding="utf-8")
        count += 1
    print(f"{count} item definitions ({selects} select)")


if __name__ == "__main__":
    main()

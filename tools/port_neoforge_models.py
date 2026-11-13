"""Rewrite NeoForge-only block/item model JSON into plain vanilla 1.21.11 models, in place.

    python -I ports/tools/port_neoforge_models.py <assets/<modid> dir> <minecraft-client.jar>

- "loader": "<mod>:materially_textured" (a marker that wrapped the parent's baked model) is dropped; the Fabric port
  retextures at the BlockStateModel / ItemModel level instead, so the plain parent model is all that's needed.
- "loader": "neoforge:composite" is flattened: every child is resolved through its parent chain (mod models first,
  then the vanilla client jar), its texture variables get a per-child prefix, and the children's elements are
  concatenated into one model. Per-child "render_type" is dropped (vanilla sets the layer per block).
- Item-model "overrides" (removed in 1.21.4) are dropped; item definitions are generated separately.
"""
import json
import re
import sys
import zipfile
from pathlib import Path

ASSETS = Path(sys.argv[1])
MODID = ASSETS.name
JAR = zipfile.ZipFile(sys.argv[2])
MODELS = ASSETS / "models"
DROP_KEYS = {"loader", "render_type", "overrides", "children", "visibility", "item_render_order", "neoforge_data"}


def norm(ref):
    return ref if ":" in ref else "minecraft:" + ref


def load_model(ref):
    ns, path = norm(ref).split(":", 1)
    if ns == MODID:
        f = MODELS / (path + ".json")
        if f.exists():
            return json.loads(f.read_text(encoding="utf-8"))
    if ns == "minecraft":
        name = f"assets/minecraft/models/{path}.json"
        if name in JAR.namelist():
            return json.loads(JAR.read(name))
    raise FileNotFoundError(ref)


def resolve(model):
    """Return (elements, textures, extras) for a model dict, walking its parent chain."""
    textures, elements, extras = {}, None, {}
    m = model
    while m is not None:
        for k, v in m.get("textures", {}).items():
            textures.setdefault(k, v)
        if elements is None and "elements" in m:
            elements = m["elements"]
        for k in ("display", "ambientocclusion", "gui_light"):
            if k in m and k not in extras:
                extras[k] = m[k]
        if m.get("loader") and m.get("loader") != f"{MODID}:materially_textured":
            raise ValueError(f"nested loader {m['loader']}")
        m = load_model(m["parent"]) if "parent" in m else None
    return elements or [], textures, extras


def prefix_ref(value, prefix):
    return "#" + prefix + value[1:] if isinstance(value, str) and value.startswith("#") else value


def flatten_composite(model):
    out_textures, out_elements, extras = {}, [], {}
    for i, (name, child) in enumerate(model["children"].items()):
        elements, textures, child_extras = resolve(child)
        prefix = f"c{i}_{name}_"
        for k, v in textures.items():
            out_textures[prefix + k] = prefix_ref(v, prefix)
        for el in elements:
            el = json.loads(json.dumps(el))
            el.pop("neoforge_data", None)
            el.pop("render_type", None)
            for face in el.get("faces", {}).values():
                face["texture"] = prefix_ref(face["texture"], prefix)
            out_elements.append(el)
        for k, v in child_extras.items():
            extras.setdefault(k, v)
        if i == 0 and "particle" in textures:
            out_textures.setdefault("particle", prefix_ref(textures["particle"], prefix))
    # the composite's own keys win (display, textures such as particle)
    _, own_textures, own_extras = resolve({k: v for k, v in model.items() if k not in ("children", "loader")})
    out_textures.update(own_textures)
    extras.update(own_extras)
    result = dict(extras)
    result["textures"] = out_textures
    result["elements"] = out_elements
    return result


def main():
    stats = {"composite": 0, "marker": 0, "overrides": 0, "render_type": 0}
    for f in sorted(MODELS.rglob("*.json")):
        model = json.loads(f.read_text(encoding="utf-8"))
        loader = model.get("loader")
        if loader == "neoforge:composite":
            model = flatten_composite(model)
            stats["composite"] += 1
        elif loader and loader.endswith(":materially_textured"):  # Domum's marker, also used by dependent mods
            del model["loader"]
            stats["marker"] += 1
        elif loader:
            raise ValueError(f"{f}: unknown loader {loader}")
        for k in ("overrides", "render_type"):
            if k in model:
                del model[k]
                stats[k] += 1
        leftover = DROP_KEYS & model.keys()
        if leftover:
            raise ValueError(f"{f}: leftover {leftover}")
        f.write_text(json.dumps(model, indent=2) + "\n", encoding="utf-8")
    print(stats)


if __name__ == "__main__":
    main()

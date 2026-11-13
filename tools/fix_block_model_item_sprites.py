"""1.21.11 rejects block models that use item-atlas textures. Copy those textures into the mod's block textures and
point the models at the copies, in place.

    python -I ports/tools/fix_block_model_item_sprites.py <assets/<modid> dir> <minecraft-client.jar>

Every texture reference in models/block/**.json of the form "[ns:]item/..." becomes
"<modid>:block/item_sprites/<ns>/<path>"; the png (and .mcmeta) is copied from the mod's own textures or the client jar.
Idempotent.
"""
import json
import sys
import zipfile
from pathlib import Path

ASSETS = Path(sys.argv[1])
MODID = ASSETS.name
JAR = zipfile.ZipFile(sys.argv[2])
ASSETS_ROOT = ASSETS.parent


def read_texture(ns, path, suffix):
    local = ASSETS_ROOT / ns / "textures" / (path + suffix)
    if local.exists():
        return local.read_bytes()
    try:
        return JAR.read(f"assets/{ns}/textures/{path}{suffix}")
    except KeyError:
        return None


def main():
    models = changed = copied = 0
    for f in (ASSETS / "models" / "block").rglob("*.json"):
        model = json.loads(f.read_text(encoding="utf-8"))
        textures = model.get("textures", {})
        dirty = False
        for key, ref in list(textures.items()):
            if not isinstance(ref, str) or ref.startswith("#"):
                continue
            ns, path = ref.split(":", 1) if ":" in ref else ("minecraft", ref)
            if not path.startswith("item/"):
                continue
            new_path = f"block/item_sprites/{ns}/{path[len('item/'):]}"
            dest = ASSETS / "textures" / (new_path + ".png")
            if not dest.exists():
                png = read_texture(ns, path, ".png")
                if png is None:
                    print(f"missing texture {ref} for {f.name}")
                    continue
                dest.parent.mkdir(parents=True, exist_ok=True)
                dest.write_bytes(png)
                meta = read_texture(ns, path, ".png.mcmeta")
                if meta is not None:
                    (dest.parent / (dest.name + ".mcmeta")).write_bytes(meta)
                copied += 1
            textures[key] = f"{MODID}:{new_path}"
            dirty = True
        models += 1
        if dirty:
            f.write_text(json.dumps(model, indent=2) + "\n", encoding="utf-8")
            changed += 1
    print(f"{models} block models, {changed} rewritten, {copied} textures copied")


if __name__ == "__main__":
    main()

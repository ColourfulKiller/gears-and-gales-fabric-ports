"""1.21.2 recipe ingredient format, applied in place to a data dir's */recipe/**.json.

    python -I ports/tools/port_recipes_1_21_2.py <src/main/resources/data>

{"item": "x"} -> "x", {"tag": "t"} -> "#t", a list of {"item": ..} -> ["a", "b"] (a list can't mix in tags in 1.21.2+).
Applies to crafting "key" values and "ingredients" entries, and to "ingredient"/"base"/"addition"/"template" fields.
"""
import json
import sys
from pathlib import Path


def ingredient(value):
    if isinstance(value, dict):
        if value.get("data", 0) == 0:  # pre-1.13 metadata, meaningless since the flattening
            value = {k: v for k, v in value.items() if k != "data"}
        if set(value) == {"item"}:
            return value["item"]
        if set(value) == {"tag"}:
            return "#" + value["tag"]
        raise ValueError(f"unsupported ingredient {value}")
    if isinstance(value, list):
        converted = [ingredient(v) for v in value]
        if len(converted) == 1:
            return converted[0]
        if any(c.startswith("#") for c in converted):
            raise ValueError(f"list mixes tags: {value}")
        return converted
    return value


def main():
    changed = 0
    for f in sorted(Path(sys.argv[1]).glob("*/recipe/**/*.json")):
        recipe = json.loads(f.read_text(encoding="utf-8"))
        before = json.dumps(recipe, sort_keys=True)
        if isinstance(recipe.get("key"), dict):
            recipe["key"] = {k: ingredient(v) for k, v in recipe["key"].items()}
        if isinstance(recipe.get("ingredients"), list):
            recipe["ingredients"] = [ingredient(v) for v in recipe["ingredients"]]
        for field in ("ingredient", "base", "addition", "template"):
            if field in recipe:
                recipe[field] = ingredient(recipe[field])
        if json.dumps(recipe, sort_keys=True) != before:
            f.write_text(json.dumps(recipe, indent=2) + "\n", encoding="utf-8")
            changed += 1
    print(f"{changed} recipes converted")


if __name__ == "__main__":
    main()

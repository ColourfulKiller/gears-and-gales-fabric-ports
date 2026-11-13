"""1.21.6 block entity NBT (CompoundTag + Provider) -> ValueInput / ValueOutput, applied in place.

    python -I ports/tools/port_be_valueio.py <src/main/java dir>

Method headers are rewritten and the old parameter names are re-declared from com.ldtteam.fabric.nbt.NbtIO, so the
bodies keep their CompoundTag code (and the on-disk format stays the same):
  loadAdditional(CompoundTag c, Provider p)  -> loadAdditional(ValueInput valueInput)   c = NbtIO.tag(valueInput), p = valueInput.lookup()
  saveAdditional(CompoundTag c, Provider p)  -> saveAdditional(ValueOutput valueOutput) c = NbtIO.tag(valueOutput), p = NbtIO.registries()
  removeComponentsFromTag(CompoundTag c)     -> removeComponentsFromTag(ValueOutput valueOutput)
  applyImplicitComponents(BlockEntity.DataComponentInput x) -> applyImplicitComponents(DataComponentGetter x)
  super.loadAdditional(c, p) / super.saveAdditional(c, p) -> super.xxxAdditional(valueInput / valueOutput)
  be.saveWithId(p) -> NbtIO.saveWithId(be, p);  be.loadWithComponents(tag, p) -> NbtIO.loadWithComponents(be, tag, p)
"""
import re
import sys
from pathlib import Path

NBT = "com.ldtteam.fabric.nbt.NbtIO"
P = r"(?:@[\w.]+\s+)*(?:final\s+)?(?:@[\w.]+\s+)*"

LOAD = re.compile(rf"(public|protected)\s+void\s+loadAdditional\(\s*{P}CompoundTag\s+(\w+)\s*,\s*{P}(?:HolderLookup\.)?Provider\s+(\w+)\s*\)\s*\{{")
SAVE = re.compile(rf"(public|protected)\s+void\s+saveAdditional\(\s*{P}CompoundTag\s+(\w+)\s*,\s*{P}(?:HolderLookup\.)?Provider\s+(\w+)\s*\)\s*\{{")
ENT_SAVE = re.compile(rf"(public|protected)\s+void\s+addAdditionalSaveData\(\s*{P}CompoundTag\s+(\w+)\s*\)\s*\{{")
ENT_LOAD = re.compile(rf"(public|protected)\s+void\s+readAdditionalSaveData\(\s*{P}CompoundTag\s+(\w+)\s*\)\s*\{{")
REMOVE = re.compile(rf"public\s+void\s+removeComponentsFromTag\(\s*{P}CompoundTag\s+(\w+)\s*\)\s*\{{")


def port(src):
    def load(m):
        vis, tag, prov = m.groups()
        return (f"{vis} void loadAdditional(final net.minecraft.world.level.storage.ValueInput valueInput)\n    {{\n"
                f"        final CompoundTag {tag} = {NBT}.tag(valueInput);\n"
                f"        final net.minecraft.core.HolderLookup.Provider {prov} = valueInput.lookup();")

    def save(m):
        vis, tag, prov = m.groups()
        return (f"{vis} void saveAdditional(final net.minecraft.world.level.storage.ValueOutput valueOutput)\n    {{\n"
                f"        final CompoundTag {tag} = {NBT}.tag(valueOutput);\n"
                f"        final net.minecraft.core.HolderLookup.Provider {prov} = {NBT}.registries();")

    def remove(m):
        tag = m.group(1)
        return (f"public void removeComponentsFromTag(final net.minecraft.world.level.storage.ValueOutput valueOutput)\n    {{\n"
                f"        final CompoundTag {tag} = {NBT}.tag(valueOutput);")

    out = LOAD.sub(load, src)
    out = SAVE.sub(save, out)
    out = REMOVE.sub(remove, out)

    # entities (1.21.6): same bridge; entity data is always tag-backed too
    def ent_save(m):
        vis, tag = m.groups()
        return (f"{vis} void addAdditionalSaveData(final net.minecraft.world.level.storage.ValueOutput valueOutput)\n    {{\n"
                f"        final CompoundTag {tag} = {NBT}.tag(valueOutput);")

    def ent_load(m):
        vis, tag = m.groups()
        return (f"{vis} void readAdditionalSaveData(final net.minecraft.world.level.storage.ValueInput valueInput)\n    {{\n"
                f"        final CompoundTag {tag} = {NBT}.tag(valueInput);")

    out = ENT_SAVE.sub(ent_save, out)
    out = ENT_LOAD.sub(ent_load, out)
    out = re.sub(r"super\.addAdditionalSaveData\(\s*\w+\s*\)", "super.addAdditionalSaveData(valueOutput)", out)
    out = re.sub(r"super\.readAdditionalSaveData\(\s*\w+\s*\)", "super.readAdditionalSaveData(valueInput)", out)
    out = re.sub(r"super\.loadAdditional\(\s*\w+\s*,\s*\w+\s*\)", "super.loadAdditional(valueInput)", out)
    out = re.sub(r"super\.saveAdditional\(\s*\w+\s*,\s*\w+\s*\)", "super.saveAdditional(valueOutput)", out)
    out = re.sub(r"super\.removeComponentsFromTag\(\s*\w+\s*\)", "super.removeComponentsFromTag(valueOutput)", out)
    out = re.sub(r"BlockEntity\.DataComponentInput\b|\bDataComponentInput\b", "net.minecraft.core.component.DataComponentGetter", out)
    out = re.sub(r"\b(\w+(?:\.\w+\(\))*)\.saveWithId\((\w+(?:\.\w+\(\))?)\)", rf"{NBT}.saveWithId(\1, \2)", out)
    out = re.sub(r"\b(\w+(?:\.\w+\(\))*)\.loadWithComponents\(([^;]*?),\s*([\w.()]+)\);", rf"{NBT}.loadWithComponents(\1, \2, \3);", out)
    return out


def main():
    changed = 0
    for f in sorted(Path(sys.argv[1]).rglob("*.java")):
        src = f.read_text(encoding="utf-8")
        out = port(src)
        if out != src:
            f.write_text(out, encoding="utf-8")
            changed += 1
            print(f.name)
    print(f"{changed} files changed")


if __name__ == "__main__":
    main()

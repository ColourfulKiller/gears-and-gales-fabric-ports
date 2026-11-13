"""Regex rewrites applied only on lines javac flagged with a matching message.

    python -I ports/tools/port_errline_fix.py <src/main/java dir> <javac error log>

RULES: (message regex, line regex, replacement). The message text searched is the error line plus the next few
detail lines (symbol / location / required / found). Each rule replaces every match on the flagged line, so keep
the line regexes narrow and idempotent (negative lookaheads against the already-rewritten form).
Run once per fresh compile log.
"""
import re
import sys
from collections import defaultdict
from pathlib import Path

RULES = [
    # 1.21.2 Registry#get(Identifier|ResourceKey) -> Optional<Reference>; getValue returns the entry
    (r"Optional<Reference<", r"((?:[Rr]egistry\(\)|getInstance\(\)|REGISTRY|Registries\.\w+|[Rr]egistry)\s*)\.get\(", r"\1.getValue("),
    # 1.21.2 loot tables are Optional on blocks / entity types
    (r"Optional<ResourceKey<LootTable>>", r"\.(getLootTable|getDefaultLootTable)\(\)(?!\.orElse)", r".\1().orElse(null)"),
    # sound events that became Holder<SoundEvent>
    (r"Reference<SoundEvent>", r"\b(SoundEvents\.[A-Z_]+)\b(?!\.value\(\))", r"\1.value()"),
    # DeferredHolder of a data component / attribute passed where vanilla wants the object / holder
    (r"DeferredHolder<DataComponentType", r"\b(ModDataComponents\.[A-Z_]+)\b(?!\.get\(\))", r"\1.get()"),
    (r"DeferredHolder<Attribute", r"\b([A-Z][A-Z_]+)\b(?=\s*[),])", r"\1.getDelegate()"),
    # 1.21.2 Direction#getNormal -> getUnitVec3i
    (r"method getNormal\(", r"\.getNormal\(\)", r".getUnitVec3i()"),
    # 1.21.5 absMoveTo / moveTo -> absSnapTo / snapTo
    (r"method absMoveTo\(", r"\.absMoveTo\(", r".absSnapTo("),
    (r"method moveTo\(", r"\.moveTo\(", r".snapTo("),
    # 1.21.2 EntityType#create(Level) needs a spawn reason
    (r"no suitable method found for create\((@\S+ )?(Server)?Level\)", r"\.create\(([^()]+(?:\([^()]*\))?)\)", r".create(\1, net.minecraft.world.entity.EntitySpawnReason.EVENT)"),
    # MobEffects renames (1.21.5)
    (r"variable MOVEMENT_SPEED", r"MobEffects\.MOVEMENT_SPEED", "MobEffects.SPEED"),
    (r"variable MOVEMENT_SLOWDOWN", r"MobEffects\.MOVEMENT_SLOWDOWN", "MobEffects.SLOWNESS"),
    (r"variable DIG_SPEED", r"MobEffects\.DIG_SPEED", "MobEffects.HASTE"),
    (r"variable DIG_SLOWDOWN", r"MobEffects\.DIG_SLOWDOWN", "MobEffects.MINING_FATIGUE"),
    (r"variable DAMAGE_BOOST", r"MobEffects\.DAMAGE_BOOST", "MobEffects.STRENGTH"),
    (r"variable HEAL\b", r"MobEffects\.HEAL\b", "MobEffects.INSTANT_HEALTH"),
    (r"variable HARM\b", r"MobEffects\.HARM\b", "MobEffects.INSTANT_DAMAGE"),
    (r"variable JUMP\b", r"MobEffects\.JUMP\b", "MobEffects.JUMP_BOOST"),
    (r"variable CONFUSION", r"MobEffects\.CONFUSION", "MobEffects.NAUSEA"),
    (r"variable DAMAGE_RESISTANCE", r"MobEffects\.DAMAGE_RESISTANCE", "MobEffects.RESISTANCE"),
    # 1.21.2 ServerChunkCache#broadcast -> sendToTrackingPlayers
    (r"method broadcast\(", r"\.broadcast\(", ".sendToTrackingPlayers("),
    # 1.21.2 Entity#kill() -> kill(ServerLevel)
    (r"method kill in class Entity", r"\bthis\.kill\(\)|(?<![.\w])kill\(\)", "kill((net.minecraft.server.level.ServerLevel) this.level())"),
    # Entity#hurt is void since 1.21.2; callers that use the result -> hurtOrSimulate
    (r"void cannot be converted to boolean|bad operand type void|.void. type not allowed here", r"\.hurt\(", ".hurtOrSimulate("),
    # 1.21.11 startRiding(entity, force, sendGameEvent)
    (r"no suitable method found for startRiding\(\w+,boolean\)", r"\.startRiding\(([^(),]+),\s*(true|false)\)", r".startRiding(\1, \2, true)"),
    # registry access lookups (1.21.2): registry(key).get() -> lookupOrThrow(key)
    (r"method registry\(", r"\.registry\(([^()]+)\)\.get\(\)", r".lookupOrThrow(\1)"),
    (r"method registryOrThrow\(", r"\.registryOrThrow\(", ".lookupOrThrow("),
    # GameProfile getters became record accessors (1.21.9)
    (r"method getId\(\).*GameProfile|location: class GameProfile.*getId", r"(getGameProfile\(\)|[Pp]rofile)\.getId\(\)", r"\1.id()"),
    (r"method getName\(\).*GameProfile|location: class GameProfile.*getName", r"(getGameProfile\(\)|[Pp]rofile)\.getName\(\)", r"\1.name()"),
]


def main():
    root = Path(sys.argv[1])
    log = Path(sys.argv[2]).read_text(encoding="utf-8-sig", errors="replace").splitlines()
    targets = defaultdict(lambda: defaultdict(list))  # file -> line -> rule indices
    for k, line in enumerate(log):
        m = re.match(r"^(.*\.java):(\d+): error: (.*)$", line)
        if not m:
            continue
        detail = [line]
        for j in range(k + 1, min(k + 7, len(log))):
            if re.match(r"^.*\.java:\d+: error", log[j]):
                break
            detail.append(log[j])
        text = " ".join(detail)
        for idx, (msg, _, _) in enumerate(RULES):
            if re.search(msg, text):
                targets[str(Path(m.group(1)).resolve())][int(m.group(2))].append(idx)
    changed = 0
    for path, lines in targets.items():
        f = Path(path)
        src = f.read_text(encoding="utf-8").split("\n")
        for ln, rules in lines.items():
            old = src[ln - 1]
            new = old
            for idx in dict.fromkeys(rules):
                _, pat, rep = RULES[idx]
                new = re.sub(pat, rep, new)
            if new != old:
                src[ln - 1] = new
                changed += 1
        f.write_text("\n".join(src), encoding="utf-8")
    print(f"{changed} lines rewritten")


if __name__ == "__main__":
    main()

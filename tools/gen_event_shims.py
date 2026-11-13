"""Generate com.ldtteam.fabric.event: NeoForge-shaped event classes (same names, getters and cancellation) so LDT event
handlers port by changing imports only. Fired by com.ldtteam.fabric.event.EventBridge.

    python -I ports/tools/gen_event_shims.py <blockui src/main/java>
"""
import sys
from pathlib import Path

ROOT = Path(sys.argv[1]) / "com" / "ldtteam" / "fabric" / "event"
BASE = "com.ldtteam.fabric.event"

# class spec: (subpackage, outer class, extends, cancellable, [(type, name)], [nested specs], extra body, imports)
# nested spec: (name, extends(outer-relative), cancellable, fields, extra body)


def field_code(fields):
    decl = "".join(f"    private final {t} {n};\n" for t, n in fields)
    getters = "".join(f"\n    public {t} get{n[0].upper() + n[1:]}()\n    {{\n        return {n};\n    }}\n" for t, n in fields)
    return decl, getters


def ctor(name, fields, super_fields):
    params = ", ".join(f"final {t} {n}" for t, n in super_fields + fields)
    sup = f"        super({', '.join(n for _, n in super_fields)});\n" if super_fields else ""
    assigns = "".join(f"        this.{n} = {n};\n" for _, n in fields)
    return f"\n    public {name}({params})\n    {{\n{sup}{assigns}    }}\n"


CLASSES = []


def cls(pkg, name, extends, super_fields, cancellable, fields, nested=(), extra="", imports=(), abstract=False):
    CLASSES.append(dict(pkg=pkg, name=name, extends=extends, super_fields=super_fields, cancellable=cancellable,
                        fields=fields, nested=nested, extra=extra, imports=imports, abstract=abstract))


def render(c):
    pkg = f"{BASE}.{c['pkg']}" if c["pkg"] else BASE
    imports = sorted(set(c["imports"]) | {f"{BASE}.bus.Event", f"{BASE}.bus.ICancellableEvent"})
    out = [f"package {pkg};\n\n"] + [f"import {i};\n" for i in imports if not i.startswith(pkg + ".") or i.count(".") > pkg.count(".") + 1]
    out.append("\n/** Fabric port of NeoForge's event of the same name (see com.ldtteam.fabric.event.EventBridge for where it fires). */\n")
    impl = " implements ICancellableEvent" if c["cancellable"] else ""
    out.append(f"public {'abstract ' if c['abstract'] else ''}class {c['name']} extends {c['extends']}{impl}\n{{\n")
    decl, getters = field_code(c["fields"])
    out.append(decl)
    out.append(ctor(c["name"], c["fields"], c["super_fields"]).replace("public", "protected" if c["abstract"] else "public", 1))
    out.append(getters)
    out.append(c["extra"])
    for n in c["nested"]:
        nname, next_, ncancel, nfields, nextra = n
        nsuper = c["super_fields"] + c["fields"]
        nimpl = " implements ICancellableEvent" if ncancel else ""
        ndecl, ngetters = field_code(nfields)
        body = ndecl + ctor(nname, nfields, nsuper) + ngetters + nextra
        body = "\n".join("    " + l if l else l for l in body.split("\n"))
        out.append(f"\n    public static class {nname} extends {next_ or c['name']}{nimpl}\n    {{\n{body}\n    }}\n")
    out.append("}\n")
    return pkg, "".join(out)


L = "net.minecraft.world.level.Level"
LA = "net.minecraft.world.level.LevelAccessor"
POS = "net.minecraft.core.BlockPos"
STATE = "net.minecraft.world.level.block.state.BlockState"
ENT = "net.minecraft.world.entity.Entity"
LIV = "net.minecraft.world.entity.LivingEntity"
PL = "net.minecraft.world.entity.player.Player"
SRC = "net.minecraft.world.damagesource.DamageSource"

# --- level
cls("level", "BlockEvent", "Event", [], False, [(LA, "level"), (POS, "pos"), (STATE, "state")], nested=[
    ("BreakEvent", None, True, [(PL, "player")], ""),
    ("EntityPlaceEvent", None, True, [(ENT, "entity"), (STATE, "placedAgainst")],
     "\npublic BlockState getPlacedBlock()\n{\n    return getState();\n}\n"),
    ("FarmlandTrampleEvent", None, True, [(ENT, "entity"), ("float", "fallDistance")], ""),
], imports=[STATE])
cls("level", "ChunkEvent", "Event", [], False, [(LA, "level"), ("net.minecraft.world.level.chunk.ChunkAccess", "chunk")], nested=[
    ("Load", None, False, [("boolean", "newChunk")], "\npublic boolean isNewChunk()\n{\n    return newChunk;\n}\n"),
    ("Unload", None, False, [], ""),
])
cls("level", "LevelEvent", "Event", [], False, [(LA, "level")], nested=[("Load", None, False, [], ""), ("Unload", None, False, [], "")])
cls("level", "ExplosionEvent", "Event", [], False, [(L, "level"), ("net.minecraft.world.level.Explosion", "explosion")], nested=[
    ("Start", None, True, [], ""),
    ("Detonate", None, False, [("java.util.List<" + POS + ">", "affectedBlocks"), ("java.util.List<" + ENT + ">", "affectedEntities")], ""),
])
cls("level", "NoteBlockEvent", "Event", [], False, [],
    extra="\n    /** NeoForge's note names, in note-block order (0 = F#3 ... 24 = F#5). */\n"
          "    public enum Note\n    {\n        F_SHARP, G, G_SHARP, A, A_SHARP, B, C, C_SHARP, D, D_SHARP, E, F;\n\n"
          "        private static final Note[] VALUES = values();\n\n"
          "        public static Note fromId(final int id)\n        {\n            return VALUES[id % 12];\n        }\n    }\n")

# --- entity
cls("entity", "EntityEvent", "Event", [], False, [(ENT, "entity")])
cls("entity", "EntityJoinLevelEvent", "EntityEvent", [(ENT, "entity")], True, [(L, "level"), ("boolean", "loadedFromDisk")],
    extra="\n    public boolean loadedFromDisk()\n    {\n        return loadedFromDisk;\n    }\n")
cls("entity", "EntityLeaveLevelEvent", "EntityEvent", [(ENT, "entity")], False, [(L, "level")])
cls("entity", "EntityTravelToDimensionEvent", "EntityEvent", [(ENT, "entity")], True,
    [("net.minecraft.resources.ResourceKey<" + L + ">", "dimension")])
cls("entity.item", "ItemTossEvent", "com.ldtteam.fabric.event.entity.EntityEvent", [(ENT, "entity")], True, [(PL, "player")],
    extra="\n    @Override\n    public net.minecraft.world.entity.item.ItemEntity getEntity()\n    {\n        return (net.minecraft.world.entity.item.ItemEntity) super.getEntity();\n    }\n")

# --- living
LIVBASE = "com.ldtteam.fabric.event.entity.EntityEvent"
LIVGET = f"\n    @Override\n    public {LIV} getEntity()\n    {{\n        return ({LIV}) super.getEntity();\n    }}\n"
cls("entity.living", "LivingEvent", LIVBASE, [(ENT, "entity")], False, [], extra=LIVGET)
cls("entity.living", "LivingDeathEvent", "LivingEvent", [(ENT, "entity")], True, [(SRC, "source")])
cls("entity.living", "LivingIncomingDamageEvent", "LivingEvent", [(ENT, "entity")], True, [(SRC, "source")],
    extra="\n    private float amount;\n\n    public float getAmount()\n    {\n        return amount;\n    }\n\n"
          "    public void setAmount(final float amount)\n    {\n        this.amount = amount;\n    }\n")
cls("entity.living", "LivingDamageEvent", "LivingEvent", [(ENT, "entity")], False, [(SRC, "source")], nested=[
    ("Pre", None, False, [("float", "originalDamage")],
     "\nprivate float newDamage = Float.NaN;\n\npublic float getNewDamage()\n{\n    return Float.isNaN(newDamage) ? getOriginalDamage() : newDamage;\n}\n\n"
     "public void setNewDamage(final float newDamage)\n{\n    this.newDamage = newDamage;\n}\n"),
])
cls("entity.living", "LivingConversionEvent", "LivingEvent", [(ENT, "entity")], False, [], nested=[
    ("Pre", None, True, [("net.minecraft.world.entity.EntityType<? extends " + LIV + ">", "outcome")], ""),
])
cls("entity.living", "LivingShieldBlockEvent", "LivingEvent", [(ENT, "entity")], True, [(SRC, "damageSource"), ("float", "blockedDamage")])
cls("entity.living", "MobSpawnEvent", "LivingEvent", [(ENT, "entity")], False, [("net.minecraft.world.level.ServerLevelAccessor", "level"), ("double", "x"), ("double", "y"), ("double", "z")], nested=[
    ("PositionCheck", None, False, [("net.minecraft.world.entity.EntitySpawnReason", "spawnType")],
     "\npublic enum Result\n{\n    SUCCEED, DEFAULT, FAIL\n}\n\nprivate Result result = Result.DEFAULT;\n\n"
     "public void setResult(final Result result)\n{\n    this.result = result;\n}\n\npublic Result getResult()\n{\n    return result;\n}\n"),
], extra=f"\n    @Override\n    public net.minecraft.world.entity.Mob getEntity()\n    {{\n        return (net.minecraft.world.entity.Mob) super.getEntity();\n    }}\n")

# --- player
PLGET = f"\n    @Override\n    public {PL} getEntity()\n    {{\n        return ({PL}) super.getEntity();\n    }}\n"
cls("entity.player", "PlayerEvent", "com.ldtteam.fabric.event.entity.living.LivingEvent", [(ENT, "entity")], False, [], nested=[
    ("PlayerLoggedInEvent", None, False, [], ""),
    ("PlayerLoggedOutEvent", None, False, [], ""),
    ("PlayerChangedDimensionEvent", None, False, [("net.minecraft.resources.ResourceKey<" + L + ">", "from"), ("net.minecraft.resources.ResourceKey<" + L + ">", "to")], ""),
], extra=PLGET)
cls("entity.player", "PlayerInteractEvent", "PlayerEvent", [(ENT, "entity")], False,
    [("net.minecraft.world.InteractionHand", "hand"), (POS, "pos"), ("net.minecraft.core.Direction", "face")], nested=[
    ("RightClickBlock", None, True, [("net.minecraft.world.phys.BlockHitResult", "hitVec")], ""),
    ("RightClickItem", None, True, [],
     "\nprivate net.minecraft.world.InteractionResult cancellationResult = net.minecraft.world.InteractionResult.PASS;\n\n"
     "public net.minecraft.world.InteractionResult getCancellationResult()\n{\n    return cancellationResult;\n}\n\n"
     "public void setCancellationResult(final net.minecraft.world.InteractionResult result)\n{\n    this.cancellationResult = result;\n}\n"),
    ("LeftClickBlock", None, True, [], ""),
    ("EntityInteract", None, True, [(ENT, "target")], ""),
    ("EntityInteractSpecific", None, True, [(ENT, "target"), ("net.minecraft.world.phys.Vec3", "localPos")], ""),
], extra="\n    public net.minecraft.world.level.Level getLevel()\n    {\n        return getEntity().level();\n    }\n\n"
         "    public net.minecraft.world.item.ItemStack getItemStack()\n    {\n        return getEntity().getItemInHand(hand);\n    }\n")
cls("entity.player", "AttackEntityEvent", "PlayerEvent", [(ENT, "entity")], True, [(ENT, "target")])
cls("entity.player", "ArrowLooseEvent", "PlayerEvent", [(ENT, "entity")], True, [("net.minecraft.world.item.ItemStack", "bow"), ("int", "charge")])
cls("entity.player", "ItemEntityPickupEvent", "Event", [], False, [(PL, "player"), ("net.minecraft.world.entity.item.ItemEntity", "itemEntity")], nested=[
    ("Pre", None, False, [],
     "\nprivate net.fabricmc.fabric.api.util.TriState canPickup = net.fabricmc.fabric.api.util.TriState.DEFAULT;\n\n"
     "public void setCanPickup(final net.fabricmc.fabric.api.util.TriState canPickup)\n{\n    this.canPickup = canPickup;\n}\n\n"
     "public net.fabricmc.fabric.api.util.TriState canPickup()\n{\n    return canPickup;\n}\n"),
])
cls("entity.player", "ItemTooltipEvent", "PlayerEvent", [(ENT, "entity")], False,
    [("net.minecraft.world.item.ItemStack", "itemStack"), ("java.util.List<net.minecraft.network.chat.Component>", "toolTip"), ("net.minecraft.world.item.TooltipFlag", "flags")])

# --- ticks
cls("tick", "LevelTickEvent", "Event", [], False, [(L, "level")], nested=[("Pre", None, False, [], ""), ("Post", None, False, [], "")])
cls("tick", "ServerTickEvent", "Event", [], False, [("net.minecraft.server.MinecraftServer", "server")], nested=[("Pre", None, False, [], ""), ("Post", None, False, [], "")])
cls("tick", "PlayerTickEvent", "com.ldtteam.fabric.event.entity.player.PlayerEvent", [(ENT, "entity")], False, [], nested=[("Pre", None, False, [], ""), ("Post", None, False, [], "")])

# --- server / misc
cls("server", "ServerLifecycleEvent", "Event", [], False, [("net.minecraft.server.MinecraftServer", "server")])
for n in ("ServerAboutToStartEvent", "ServerStartingEvent", "ServerStartedEvent", "ServerStoppingEvent", "ServerStoppedEvent"):
    cls("server", n, "ServerLifecycleEvent", [("net.minecraft.server.MinecraftServer", "server")], False, [])
cls("", "LootTableLoadEvent", "Event", [], False,
    [("net.minecraft.resources.Identifier", "name"), ("TableAccess", "table")],
    extra="\n    /** What handlers used of NeoForge's mutable LootTable: adding pools (to Fabric's builder). */\n"
          "    public record TableAccess(net.minecraft.world.level.storage.loot.LootTable.Builder builder)\n    {\n"
          "        public void addPool(final net.minecraft.world.level.storage.loot.LootPool pool)\n        {\n"
          "            builder.pool(pool);\n        }\n    }\n")
cls("", "OnDatapackSyncEvent", "Event", [], False,
    [("net.minecraft.server.players.PlayerList", "playerList"), ("net.minecraft.server.level.ServerPlayer", "player")])
cls("", "VanillaGameEvent", "Event", [], True,
    [(L, "level"), ("net.minecraft.core.Holder<net.minecraft.world.level.gameevent.GameEvent>", "vanillaEvent"),
     ("net.minecraft.world.phys.Vec3", "eventPosition"), ("net.minecraft.world.level.gameevent.GameEvent.Context", "context")])
cls("", "TagsUpdatedEvent", "Event", [], False, [("net.minecraft.core.RegistryAccess", "registryAccess"), ("boolean", "client")])


BUS = {
    "Event.java": """package com.ldtteam.fabric.event.bus;

/** Base of the NeoForge-shaped events (com.ldtteam.fabric.event). */
public abstract class Event
{
    private boolean canceled;

    /** Only meaningful for {@link ICancellableEvent}s. */
    public void setCanceled(final boolean canceled)
    {
        this.canceled = canceled;
    }

    public boolean isCanceled()
    {
        return canceled;
    }
}
""",
    "ICancellableEvent.java": """package com.ldtteam.fabric.event.bus;

/** Marks events whose cancellation the bridge honours. */
public interface ICancellableEvent
{
    void setCanceled(boolean canceled);

    boolean isCanceled();
}
""",
    "EventPriority.java": """package com.ldtteam.fabric.event.bus;

/** Handler order, highest first (as in NeoForge). */
public enum EventPriority
{
    HIGHEST, HIGH, NORMAL, LOW, LOWEST
}
""",
    "SubscribeEvent.java": """package com.ldtteam.fabric.event.bus;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** Marks event handler methods for {@link EventBus#register}. */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface SubscribeEvent
{
    EventPriority priority() default EventPriority.NORMAL;

    boolean receiveCanceled() default false;
}
""",
}


def main():
    for c in CLASSES:
        pkg, code = render(c)
        d = ROOT.joinpath(*c["pkg"].split(".")) if c["pkg"] else ROOT
        d.mkdir(parents=True, exist_ok=True)
        (d / f"{c['name']}.java").write_text(code, encoding="utf-8")
    bus = ROOT / "bus"
    bus.mkdir(parents=True, exist_ok=True)
    for name, code in BUS.items():
        (bus / name).write_text(code, encoding="utf-8")
    print(f"{len(CLASSES)} event classes + {len(BUS)} bus classes")


if __name__ == "__main__":
    main()

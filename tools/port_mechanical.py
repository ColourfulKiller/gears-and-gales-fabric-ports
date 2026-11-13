"""Mechanical NeoForge 1.21.1 -> Fabric 1.21.11 rewrites, applied to a source tree in place.

    python -I ports/tools/port_mechanical.py <src/main/java dir>

Only rewrites that are safe everywhere: renamed vanilla classes and the Env shim (com.ldtteam.fabric.Env).
Semantic changes (rendering, networking, events) are done by hand. Prints a count per rule.
"""
import re
import sys
from pathlib import Path

RULES = [
    # 1.21.11 renamed ResourceLocation to Identifier (same package, same static factories)
    (r"\bnet\.minecraft\.resources\.ResourceLocation\b", "net.minecraft.resources.Identifier"),
    (r"\bResourceLocation\b", "Identifier"),
    # FMLEnvironment -> Env shim
    (r"^import net\.neoforged\.fml\.loading\.FMLEnvironment;", "import com.ldtteam.fabric.Env;"),
    (r"\bFMLEnvironment\.production\b", "Env.PRODUCTION"),
    (r"\bFMLEnvironment\.dist\.isClient\(\)", "Env.isClient()"),
    (r"\bFMLEnvironment\.dist\.isDedicatedServer\(\)", "!Env.isClient()"),
    # networking -> com.ldtteam.fabric.network shim
    (r"^import net\.neoforged\.neoforge\.network\.handling\.IPayloadContext;", "import com.ldtteam.fabric.network.PayloadContext;"),
    (r"\bIPayloadContext\b", "PayloadContext"),
    (r"^import net\.neoforged\.neoforge\.network\.PacketDistributor;", "import com.ldtteam.fabric.network.Networking;"),
    (r"\bPacketDistributor\.(sendTo\w+)\(", r"Networking.\1("),
    # registries -> com.ldtteam.fabric.registry shim
    (r"^import net\.neoforged\.neoforge\.registries\.(DeferredRegister|DeferredHolder|DeferredBlock|DeferredItem);",
     r"import com.ldtteam.fabric.registry.\1;"),
    (r"^import net\.neoforged\.neoforge\.common\.util\.Lazy;", "import com.ldtteam.fabric.Lazy;"),
    # side annotations: Fabric's equivalent
    (r"^import net\.neoforged\.api\.distmarker\.Dist;", "import net.fabricmc.api.EnvType;"),
    (r"^import net\.neoforged\.api\.distmarker\.OnlyIn;", "import net.fabricmc.api.Environment;"),
    (r"@OnlyIn\(Dist\.CLIENT\)", "@Environment(EnvType.CLIENT)"),
    (r"@OnlyIn\(Dist\.DEDICATED_SERVER\)", "@Environment(EnvType.SERVER)"),
    # 1.21.11: ResourceKey.location() -> identifier() (only on obvious ResourceKey expressions)
    (r"(unwrapKey\(\)\.orElseThrow\(\)|\.getKey\(\)|\.dimension\(\))\.location\(\)", r"\1.identifier()"),
    # item handlers -> com.ldtteam.fabric.items shim
    (r"^import net\.neoforged\.neoforge\.items\.(IItemHandler|IItemHandlerModifiable|ItemStackHandler|ItemHandlerHelper);",
     r"import com.ldtteam.fabric.items.\1;"),
    (r"^import net\.neoforged\.neoforge\.items\.wrapper\.InvWrapper;", "import com.ldtteam.fabric.items.InvWrapper;"),
    (r"^import net\.neoforged\.neoforge\.common\.util\.TriPredicate;", "import com.ldtteam.fabric.util.TriPredicate;"),
    # removed in 1.21.2+: Player#playNotifySound, Entity#getCommandSenderWorld, Inventory#getSelected
    (r"(?<![\w.])(?!com\.ldtteam\.fabric\.PlayerHooks\b)((?:\w+\.)*\w+)\.playNotifySound\(", r"com.ldtteam.fabric.PlayerHooks.playNotifySound(\1, "),
    (r"\.getCommandSenderWorld\(\)", ".level()"),
    (r"\.getInventory\(\)\.getSelected\(\)", ".getInventory().getSelectedItem()"),
    # 1.21.2: level height accessors
    (r"\.getMinBuildHeight\(\)", ".getMinY()"),
    # 1.21.9: SharedConstants version accessors are records
    (r"SharedConstants\.getCurrentVersion\(\)\.getDataVersion\(\)\.getVersion\(\)", "SharedConstants.getCurrentVersion().dataVersion().version()"),
    # 1.21.2: registries are HolderGetters themselves
    (r"(BuiltInRegistries\.\w+)\.asLookup\(\)", r"\1"),
    # 1.21.5: Entity#moveTo -> snapTo
    (r"\.moveTo\((?=[^)]*,[^)]*,[^)]*,[^)]*,)", ".snapTo("),
    # NeoForge FriendlyByteBuf helpers renamed with ResourceLocation -> Identifier
    (r"\bResourceLocationException\b", "IdentifierException"),
    (r"\.readResourceLocation\(\)", ".readIdentifier()"),
    (r"\.writeResourceLocation\(", ".writeIdentifier("),
    # NeoForge common APIs -> com.ldtteam.fabric shims / Fabric equivalents
    (r"^import net\.neoforged\.neoforge\.common\.Tags;", "import com.ldtteam.fabric.Tags;"),
    (r"\bnet\.neoforged\.neoforge\.common\.Tags\b", "com.ldtteam.fabric.Tags"),
    (r"^import net\.neoforged\.neoforge\.common\.util\.INBTSerializable;", "import com.ldtteam.fabric.nbt.INBTSerializable;"),
    (r"^import net\.neoforged\.neoforge\.items\.SlotItemHandler;", "import com.ldtteam.fabric.items.SlotItemHandler;"),
    (r"^import net\.neoforged\.neoforge\.common\.util\.FakePlayer;", "import net.fabricmc.fabric.api.entity.FakePlayer;"),
    (r"^import net\.neoforged\.neoforge\.common\.util\.FakePlayerFactory;\n", ""),
    (r"\bnew FakePlayer\(", "FakePlayer.get("),
    (r"\bFakePlayerFactory\.get\(", "FakePlayer.get("),
    (r"\bFakePlayerFactory\.getMinecraft\(", "FakePlayer.get("),
    (r"^import net\.neoforged\.neoforge\.common\.ItemAbilities;", "import com.ldtteam.fabric.ItemAbilities;"),
    (r"^import net\.neoforged\.neoforge\.common\.ItemAbility;", "import com.ldtteam.fabric.ItemAbilities.ItemAbility;"),
    (r"\bnet\.neoforged\.neoforge\.common\.ItemAbilities\b", "com.ldtteam.fabric.ItemAbilities"),
    (r"(?<![\w.])(?!(?:com\.ldtteam\.fabric\.)?ItemAbilities\b)((?:\w+\.)*\w+(?:\(\))?)\.canPerformAction\(", r"com.ldtteam.fabric.ItemAbilities.canPerformAction(\1, "),
    (r"^import net\.neoforged\.neoforge\.server\.ServerLifecycleHooks;\n", ""),
    (r"\b(?:net\.neoforged\.neoforge\.server\.)?ServerLifecycleHooks\.getCurrentServer\(\)", "com.ldtteam.fabric.network.Networking.server()"),
    (r"^import net\.neoforged\.fml\.ModList;\n", ""),
    (r"\bModList\.get\(\)\.isLoaded\(", "net.fabricmc.loader.api.FabricLoader.getInstance().isModLoaded("),
    (r"!ModList\.get\(\)\.getModContainerById\(([^;]*?)\)\.isPresent\(\)", r"!net.fabricmc.loader.api.FabricLoader.getInstance().isModLoaded(\1)"),
    (r"\bModList\.get\(\)\.getModContainerById\(([^;]*?)\)\.isPresent\(\)", r"net.fabricmc.loader.api.FabricLoader.getInstance().isModLoaded(\1)"),
    # 1.21.x vanilla package / type renames that also appear fully qualified in code
    (r"\bnet\.minecraft\.advancements\.critereon\b", "net.minecraft.advancements.criterion"),
    (r"\bnet\.minecraft\.world\.entity\.animal\.horse\b", "net.minecraft.world.entity.animal.equine"),
    (r"^import net\.minecraft\.world\.entity\.MobSpawnType;", "import net.minecraft.world.entity.EntitySpawnReason;"),
    (r"\bMobSpawnType\b", "EntitySpawnReason"),
    (r"^import net\.minecraft\.world\.item\.UseAnim;", "import net.minecraft.world.item.ItemUseAnimation;"),
    (r"\bUseAnim\b", "ItemUseAnimation"),
    (r"^import net\.minecraft\.util\.FastColor;", "import net.minecraft.util.ARGB;"),
    (r"\bFastColor\.ARGB32\.", "ARGB."),
    (r"\bFastColor\.", "ARGB."),
    (r"^import net\.minecraft\.world\.level\.portal\.DimensionTransition;", "import net.minecraft.world.level.portal.TeleportTransition;"),
    (r"\bDimensionTransition\b", "TeleportTransition"),
    (r"^import net\.minecraft\.util\.random\.WeightedRandomList;", "import net.minecraft.util.random.WeightedList;"),
    (r"\bWeightedRandomList\b", "WeightedList"),
    (r"^import net\.minecraft\.world\.level\.storage\.loot\.parameters\.LootContextParamSet;", "import net.minecraft.util.context.ContextKeySet;"),
    (r"\bLootContextParamSet\b", "ContextKeySet"),
    # NeoForge event bus + events -> com.ldtteam.fabric.event (NeoForge-shaped, fired by EventBridge)
    (r"\bnet\.neoforged\.bus\.api\.(Event|EventPriority|ICancellableEvent|IEventBus|SubscribeEvent)\b", r"com.ldtteam.fabric.event.bus.\1"),
    (r"\bnet\.neoforged\.neoforge\.common\.NeoForge\b", "com.ldtteam.fabric.event.NeoForge"),
    (r"\bnet\.neoforged\.neoforge\.common\.util\.TriState\b", "net.fabricmc.fabric.api.util.TriState"),
    (r"\bnet\.neoforged\.neoforge\.event\.("
     r"level\.(?:BlockEvent|ChunkEvent|LevelEvent|ExplosionEvent|NoteBlockEvent)"
     r"|entity\.(?:EntityJoinLevelEvent|EntityLeaveLevelEvent|EntityTravelToDimensionEvent|EntityEvent)"
     r"|entity\.item\.ItemTossEvent"
     r"|entity\.living\.(?:LivingEvent|LivingDeathEvent|LivingDamageEvent|LivingIncomingDamageEvent|LivingConversionEvent|LivingShieldBlockEvent|MobSpawnEvent)"
     r"|entity\.player\.(?:PlayerEvent|PlayerInteractEvent|AttackEntityEvent|ArrowLooseEvent|ItemEntityPickupEvent|ItemTooltipEvent)"
     r"|tick\.\w+|server\.\w+|LootTableLoadEvent|OnDatapackSyncEvent|VanillaGameEvent|TagsUpdatedEvent)\b", r"com.ldtteam.fabric.event.\1"),
    (r"^import net\.neoforged\.neoforge\.event\.entity\.player\.\*;", "import com.ldtteam.fabric.event.entity.player.*;"),
    (r"^import net\.neoforged\.fml\.common\.Mod;\n", ""),
    (r"^import net\.neoforged\.neoforge\.event\.entity\.living\.MobDespawnEvent;\n", ""),
    # mod-bus / registration events -> com.ldtteam.fabric.event shims (delegate to Fabric registries)
    (r"\bnet\.neoforged\.neoforge\.registries\.RegisterEvent\b", "com.ldtteam.fabric.event.registry.RegisterEvent"),
    (r"\bnet\.neoforged\.neoforge\.event\.entity\.EntityAttributeCreationEvent\b", "com.ldtteam.fabric.event.entity.EntityAttributeCreationEvent"),
    (r"\bnet\.neoforged\.fml\.event\.lifecycle\.(FMLCommonSetupEvent|FMLLoadCompleteEvent)\b", r"com.ldtteam.fabric.event.lifecycle.\1"),
    (r"\bnet\.neoforged\.neoforge\.event\.(AddReloadListenerEvent|RegisterCommandsEvent|EventHooks)\b", r"com.ldtteam.fabric.event.\1"),
    (r"\bnet\.neoforged\.neoforge\.client\.event\.(RegisterMenuScreensEvent|RegisterParticleProvidersEvent|RegisterClientReloadListenersEvent"
     r"|EntityRenderersEvent|RegisterKeyMappingsEvent|ClientTickEvent|ClientPlayerNetworkEvent)\b", r"com.ldtteam.fabric.event.client.\1"),
    # NeoForge auto-registration: the Fabric entrypoints register these classes explicitly
    (r"^@EventBusSubscriber\([^)]*\)\n", ""),
    (r"^import (static )?net\.neoforged\.fml\.common\.EventBusSubscriber[\w.]*;\n", ""),
    # 1.21.2: JSON reload listeners take a codec + file converter (apply(Map<Identifier, JsonElement>, ...) unchanged)
    (r"\bextends SimpleJsonResourceReloadListener\b(?!<)", "extends SimpleJsonResourceReloadListener<com.google.gson.JsonElement>"),
    (r"\bsuper\(GSON, ([^;]+)\);", r"super(net.minecraft.util.ExtraCodecs.JSON, net.minecraft.resources.FileToIdConverter.json(\1));"),
    # NeoForge custom ingredients -> com.ldtteam.fabric.crafting (Fabric custom ingredient API)
    (r"\bnet\.neoforged\.neoforge\.common\.crafting\.(ICustomIngredient|IngredientType)\b", r"com.ldtteam.fabric.crafting.\1"),
    (r"\bIngredient\.CODEC_NONEMPTY\b", "Ingredient.CODEC"),
    (r"\bchild == Ingredient\.EMPTY\b", "child.isEmpty()"),
    # NeoForge item handler capabilities -> com.ldtteam.fabric.items.ItemHandlers
    (r"^import net\.neoforged\.neoforge\.capabilities\.Capabilities(\.ItemHandler)?;\n", ""),
    (r"\b(?:Capabilities\.)?ItemHandler\.ENTITY(?:_AUTOMATION)?\.getCapability\(", "com.ldtteam.fabric.items.ItemHandlers.forEntity("),
    (r"\b(?:Capabilities\.)?ItemHandler\.BLOCK\.getCapability\(", "com.ldtteam.fabric.items.ItemHandlers.forBlock("),
    (r"\b(?:Capabilities\.)?ItemHandler\.ITEM\.getCapability\(([^,()]+(?:\([^()]*\))?), null\)", r"com.ldtteam.fabric.items.ItemHandlers.forItem(\1)"),
    (r"([\w.]+(?:\(\))?)\.getCapability\(Capabilities\.ItemHandler\.BLOCK, ", r"com.ldtteam.fabric.items.ItemHandlers.forBlock(\1, "),
    # NeoForge menu types with extra data -> MenuHooks
    (r"^import net\.neoforged\.neoforge\.common\.extensions\.IMenuTypeExtension;\n", ""),
    (r"\bIMenuTypeExtension\.create\(", "com.ldtteam.fabric.MenuHooks.create("),
    # NeoForge tag-key helpers / item property extras
    (r"\bItemTags\.create\(", "net.minecraft.tags.TagKey.create(net.minecraft.core.registries.Registries.ITEM, "),
    (r"\bBlockTags\.create\(", "net.minecraft.tags.TagKey.create(net.minecraft.core.registries.Registries.BLOCK, "),
    (r"\bEntityTypeTags\.create\(", "net.minecraft.tags.TagKey.create(net.minecraft.core.registries.Registries.ENTITY_TYPE, "),
    (r"\bBiomeTags\.create\(", "net.minecraft.tags.TagKey.create(net.minecraft.core.registries.Registries.BIOME, "),
    (r"\.setNoRepair\(\)", ""),
    (r"\bWeightedList\.create\(", "WeightedList.of("),
    # 1.21.9: net.minecraft.Util moved
    (r"^import net\.minecraft\.Util;", "import net.minecraft.util.Util;"),
    (r"\bnet\.minecraft\.Util\.", "net.minecraft.util.Util."),
    (r"\bMobDespawnEvent\.PositionCheck\b", "MobSpawnEvent.PositionCheck"),
    # 1.21.6: ItemStack NBT helpers moved to codecs -> NbtIO
    (r"(?<![\w.])(?!(?:com\.ldtteam\.fabric\.nbt\.)?NbtIO\b)((?:\w+\.)*\w+(?:\(\))?)\.saveOptional\(", r"com.ldtteam.fabric.nbt.NbtIO.saveOptional(\1, "),
    (r"\bItemStack\.parseOptional\(", "com.ldtteam.fabric.nbt.NbtIO.parseOptional("),
    # 1.21.2: EntityType.Builder setters lost their "set" prefix
    (r"\.setUpdateInterval\(", ".updateInterval("),
    (r"\.setTrackingRange\(", ".clientTrackingRange("),
    (r"\.setShouldReceiveVelocityUpdates\([^)]*\)", ""),
    # 1.21.2: ItemInteractionResult folded into InteractionResult
    (r"^import net\.minecraft\.world\.ItemInteractionResult;\n", ""),
    (r"\bItemInteractionResult\.PASS_TO_DEFAULT_BLOCK_INTERACTION\b", "InteractionResult.TRY_WITH_EMPTY_HAND"),
    (r"\bItemInteractionResult\.SKIP_DEFAULT_BLOCK_INTERACTION\b", "InteractionResult.PASS"),
    (r"\bItemInteractionResult\.sidedSuccess\([^)]*\)", "InteractionResult.SUCCESS"),
    (r"\bItemInteractionResult\b", "InteractionResult"),
]


def main():
    root = Path(sys.argv[1])
    counts = [0] * len(RULES)
    changed = 0
    for f in root.rglob("*.java"):
        if "com/ldtteam/fabric" in f.as_posix():  # the compatibility shim itself
            continue
        src = f.read_text(encoding="utf-8")
        out = src
        for i, (pat, rep) in enumerate(RULES):
            out, n = re.subn(pat, rep, out, flags=re.MULTILINE)
            counts[i] += n
        if out != src:
            f.write_text(out, encoding="utf-8")
            changed += 1
    for (pat, _), n in zip(RULES, counts):
        print(f"{n:5}  {pat}")
    print(f"{changed} files changed")


if __name__ == "__main__":
    main()

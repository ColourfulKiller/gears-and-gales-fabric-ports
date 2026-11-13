package com.ldtteam.fabric.data;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.fabricmc.fabric.api.loot.v3.LootTableEvents;
import net.fabricmc.fabric.api.resource.v1.ResourceLoader;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * NeoForge global loot modifiers on Fabric, for the "neoforge:add_table" modifier type (all the LDT mods use).
 * <p>
 * Modifiers load from data/&lt;ns&gt;/loot_modifiers/*.json, listed by data/neoforge/loot_modifiers/global_loot_modifiers.json
 * (merged across packs, honouring "replace"). When any loot table rolls, each modifier whose conditions pass rolls its
 * table into the same drops (Fabric LootTableEvents.MODIFY_DROPS). NeoForge's "neoforge:loot_table_id" condition maps to
 * "ldtteam_fabric:loot_table_id", which checks the table being rolled.
 */
public final class GlobalLootModifiers
{
    private static final Logger LOGGER = LogUtils.getLogger();

    /** The table currently being modified (the loot_table_id condition reads it). */
    private static final ThreadLocal<Identifier> CURRENT_TABLE = new ThreadLocal<>();

    private static volatile List<Modifier> modifiers = List.of();
    private static boolean initialized = false;

    private record Modifier(Identifier id, List<LootItemCondition> conditions, ResourceKey<LootTable> table) {}

    /** NeoForge's loot_table_id condition. */
    public record LootTableIdCondition(Identifier lootTableId) implements LootItemCondition
    {
        public static final MapCodec<LootTableIdCondition> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            Identifier.CODEC.fieldOf("loot_table_id").forGetter(LootTableIdCondition::lootTableId)).apply(i, LootTableIdCondition::new));
        public static final LootItemConditionType TYPE = new LootItemConditionType(CODEC);

        @Override
        public LootItemConditionType getType()
        {
            return TYPE;
        }

        @Override
        public boolean test(final LootContext context)
        {
            return lootTableId.equals(CURRENT_TABLE.get());
        }
    }

    private GlobalLootModifiers()
    {
    }

    /** Registers the condition type, the reload listener and the drop hook. Safe to call more than once. */
    public static synchronized void init()
    {
        if (initialized)
        {
            return;
        }
        initialized = true;
        Registry.register(BuiltInRegistries.LOOT_CONDITION_TYPE, Identifier.fromNamespaceAndPath("ldtteam_fabric", "loot_table_id"), LootTableIdCondition.TYPE);
        ResourceLoader.get(PackType.SERVER_DATA).registerReloader(Identifier.fromNamespaceAndPath("ldtteam_fabric", "global_loot_modifiers"), new Listener());
        LootTableEvents.MODIFY_DROPS.register(GlobalLootModifiers::apply);
    }

    private static void apply(final Holder<LootTable> table, final LootContext context, final List<ItemStack> drops)
    {
        final List<Modifier> active = modifiers;
        if (active.isEmpty())
        {
            return;
        }
        final Identifier tableId = table.unwrapKey().map(ResourceKey::identifier).orElse(null);
        final Identifier previous = CURRENT_TABLE.get();
        CURRENT_TABLE.set(tableId);
        try
        {
            for (final Modifier modifier : active)
            {
                if (modifier.table().identifier().equals(tableId))
                {
                    continue; // never roll a table into itself
                }
                boolean pass = true;
                for (final LootItemCondition condition : modifier.conditions())
                {
                    if (!condition.test(context))
                    {
                        pass = false;
                        break;
                    }
                }
                if (pass)
                {
                    context.getResolver().get(modifier.table()).ifPresent(extra -> extra.value().getRandomItemsRaw(context, LootTable.createStackSplitter(context.getLevel(), drops::add)));
                }
            }
        }
        finally
        {
            CURRENT_TABLE.set(previous);
        }
    }

    /** Rewrites NeoForge condition ids to their Fabric replacements, in place. */
    private static void rewriteConditions(final JsonElement element)
    {
        if (element instanceof final JsonObject object)
        {
            if (object.has("condition") && "neoforge:loot_table_id".equals(object.get("condition").getAsString()))
            {
                object.addProperty("condition", "ldtteam_fabric:loot_table_id");
            }
            for (final Map.Entry<String, JsonElement> entry : object.entrySet())
            {
                rewriteConditions(entry.getValue());
            }
        }
        else if (element instanceof final JsonArray array)
        {
            array.forEach(GlobalLootModifiers::rewriteConditions);
        }
    }

    private static final class Listener extends RegistryAwareJsonListener
    {
        private Listener()
        {
            super(ExtraCodecs.JSON, net.minecraft.resources.FileToIdConverter.json("loot_modifiers"));
        }

        @Override
        protected void apply(final Map<Identifier, JsonElement> files, final ResourceManager resourceManager, final ProfilerFiller profiler)
        {
            final Set<Identifier> entries = new LinkedHashSet<>();
            // the merged list: every pack's global_loot_modifiers.json, in pack order, "replace" drops earlier entries
            for (final var resource : resourceManager.getResourceStack(Identifier.fromNamespaceAndPath("neoforge", "loot_modifiers/global_loot_modifiers.json")))
            {
                try (var reader = resource.openAsReader())
                {
                    final JsonObject list = com.google.gson.JsonParser.parseReader(reader).getAsJsonObject();
                    if (list.has("replace") && list.get("replace").getAsBoolean())
                    {
                        entries.clear();
                    }
                    for (final JsonElement entry : list.getAsJsonArray("entries"))
                    {
                        entries.add(Identifier.parse(entry.getAsString()));
                    }
                }
                catch (final Exception e)
                {
                    LOGGER.error("Could not read global loot modifier list from {}", resource.sourcePackId(), e);
                }
            }

            final RegistryOps<JsonElement> ops = getRegistryLookup().createSerializationContext(JsonOps.INSTANCE);
            final List<Modifier> loaded = new ArrayList<>();
            for (final Identifier id : entries)
            {
                final JsonElement json = files.get(id);
                if (!(json instanceof final JsonObject object))
                {
                    LOGGER.warn("Global loot modifier {} is listed but missing", id);
                    continue;
                }
                if (!JsonConditions.test(object))
                {
                    continue;
                }
                final String type = object.has("type") ? object.get("type").getAsString() : "";
                if (!"neoforge:add_table".equals(type))
                {
                    LOGGER.warn("Global loot modifier {} has unsupported type {}", id, type);
                    continue;
                }
                final JsonElement conditions = object.has("conditions") ? object.get("conditions").deepCopy() : new JsonArray();
                rewriteConditions(conditions);
                final var parsed = LootItemCondition.DIRECT_CODEC.listOf().parse(ops, conditions);
                if (parsed.error().isPresent())
                {
                    LOGGER.error("Global loot modifier {}: bad conditions: {}", id, parsed.error().get().message());
                    continue;
                }
                final ResourceKey<LootTable> table = ResourceKey.create(Registries.LOOT_TABLE, Identifier.parse(object.get("table").getAsString()));
                loaded.add(new Modifier(id, List.copyOf(parsed.result().orElseThrow()), table));
            }
            modifiers = List.copyOf(loaded);
            LOGGER.info("Loaded {} global loot modifiers", loaded.size());
        }
    }
}

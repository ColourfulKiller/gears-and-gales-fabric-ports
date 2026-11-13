package com.ldtteam.fabric.data;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.logging.LogUtils;
import net.fabricmc.fabric.api.registry.CompostingChanceRegistry;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import org.slf4j.Logger;

import java.io.Reader;
import java.nio.file.Files;
import java.util.Map;

/**
 * NeoForge's "compostables" item data map, read from a mod's bundled data/neoforge/data_maps/item/compostables.json
 * and registered with Fabric's composting registry. Call after the mod's items are registered.
 */
public final class Compostables
{
    private static final Logger LOGGER = LogUtils.getLogger();

    private Compostables()
    {
    }

    public static void registerBundled(final String modId)
    {
        FabricLoader.getInstance().getModContainer(modId).flatMap(c -> c.findPath("data/neoforge/data_maps/item/compostables.json")).ifPresent(path -> {
            try (Reader reader = Files.newBufferedReader(path))
            {
                final JsonObject values = JsonParser.parseReader(reader).getAsJsonObject().getAsJsonObject("values");
                int count = 0;
                for (final Map.Entry<String, JsonElement> entry : values.entrySet())
                {
                    final Identifier id = Identifier.tryParse(entry.getKey());
                    if (id == null || !BuiltInRegistries.ITEM.containsKey(id))
                    {
                        continue;
                    }
                    final float chance = entry.getValue().getAsJsonObject().get("chance").getAsFloat();
                    CompostingChanceRegistry.INSTANCE.add(BuiltInRegistries.ITEM.getValue(id), chance);
                    count++;
                }
                LOGGER.info("Registered {} compostables for {}", count, modId);
            }
            catch (final Exception e)
            {
                LOGGER.error("Could not read compostables for {}", modId, e);
            }
        });
    }
}
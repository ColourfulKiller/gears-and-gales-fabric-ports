package com.minecolonies.core.datalistener;

import com.google.gson.*;
import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.api.util.Log;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * Loads and listens to get custom nbt matching rules.
 */
public class ItemNbtListener extends com.ldtteam.fabric.data.RegistryAwareJsonListener
{
    /**
     * Gson instance
     */
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    /**
     * Create a new listener.
     */
    public ItemNbtListener()
    {
        super(net.minecraft.util.ExtraCodecs.JSON, net.minecraft.resources.FileToIdConverter.json("compatibility"));
    }

    @Override
    protected void apply(final Map<Identifier, JsonElement> jsonElementMap, final @NotNull ResourceManager resourceManager, final @NotNull ProfilerFiller profiler)
    {
        ItemStackUtils.CHECKED_NBT_KEYS.clear();
        for (final Map.Entry<Identifier, JsonElement> entry : jsonElementMap.entrySet())
        {
            tryParse(this.getRegistryLookup(), entry);
        }
    }

    /**
     * Tries to parse the entry
     *
     * @param entry
     */
    private void tryParse(@NotNull final HolderLookup.Provider provider, final Map.Entry<Identifier, JsonElement> entry)
    {
        for (final JsonElement element : entry.getValue().getAsJsonArray())
        {
            try
            {
                final JsonObject jsonObj = element.getAsJsonObject();
                final Identifier itemLoc = Identifier.parse(jsonObj.get("item").getAsString());
                if (!BuiltInRegistries.ITEM.containsKey(itemLoc))
                {
                    continue;
                }
                if (jsonObj.has("checkednbtkeys"))
                {
                    final HashSet<DataComponentType<?>> set = new HashSet<>();
                    final JsonArray jsonArray = jsonObj.getAsJsonArray("checkednbtkeys");
                    for (final JsonElement subElement : jsonArray)
                    {
                        final Identifier componentId = Identifier.parse(subElement.getAsString());
                        final DataComponentType<?> componentType = BuiltInRegistries.DATA_COMPONENT_TYPE.getValue(componentId);
                        if (componentType == null)
                        {
                            // Unknown (renamed or removed) component: skip it, a null key breaks the login sync packet.
                            Log.getLogger().warn("Unknown data component '" + componentId + "' in " + entry.getKey() + " for " + itemLoc + ", ignoring");
                            continue;
                        }
                        set.add(componentType);
                    }

                    ItemStackUtils.CHECKED_NBT_KEYS.put(BuiltInRegistries.ITEM.getValue(itemLoc), set);
                }
                else
                {
                    ItemStackUtils.CHECKED_NBT_KEYS.put(BuiltInRegistries.ITEM.getValue(itemLoc), new HashSet<>());
                }
            }
            catch (Exception e)
            {
                Log.getLogger().warn("Could not nbt comparator for:" + entry.getKey(), e);
            }
        }
        Log.getLogger().warn("Read " + ItemStackUtils.CHECKED_NBT_KEYS.size() + " items with their nbt keys for compatibility.");
    }
}

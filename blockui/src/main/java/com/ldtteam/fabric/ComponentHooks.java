package com.ldtteam.fabric;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import org.jetbrains.annotations.Nullable;

/** 1.21.5 removed Component.Serializer; same JSON string format through ComponentSerialization.CODEC. */
public final class ComponentHooks
{
    private ComponentHooks()
    {
    }

    /** Was {@code Component.Serializer.toJson(component, registries)}. */
    public static String toJson(final Component component, final HolderLookup.Provider registries)
    {
        return ComponentSerialization.CODEC.encodeStart(registries.createSerializationContext(JsonOps.INSTANCE), component)
            .map(JsonElement::toString).result().orElse("\"\"");
    }

    /** Was {@code Component.Serializer.fromJson(json, registries)}. */
    public static @Nullable Component fromJson(final String json, final HolderLookup.Provider registries)
    {
        if (json == null || json.isEmpty())
        {
            return null;
        }
        try
        {
            return ComponentSerialization.CODEC.parse(registries.createSerializationContext(JsonOps.INSTANCE), JsonParser.parseString(json)).result().orElse(null);
        }
        catch (final RuntimeException e)
        {
            return Component.literal(json);
        }
    }
}
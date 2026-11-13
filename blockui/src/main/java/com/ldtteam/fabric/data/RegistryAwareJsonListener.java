package com.ldtteam.fabric.data;

import com.google.gson.JsonElement;
import com.ldtteam.fabric.nbt.NbtIO;
import com.mojang.serialization.Codec;
import net.fabricmc.fabric.api.resource.v1.ResourceLoader;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import org.jetbrains.annotations.Nullable;

/**
 * A JSON reload listener that knows the registries being loaded (NeoForge's ContextAwareReloadListener#getRegistryLookup),
 * taken from Fabric's reload shared state.
 */
public abstract class RegistryAwareJsonListener extends SimpleJsonResourceReloadListener<JsonElement>
{
    private @Nullable HolderLookup.Provider registryLookup;

    protected RegistryAwareJsonListener(final Codec<JsonElement> codec, final FileToIdConverter converter)
    {
        super(codec, converter);
    }

    @Override
    public void prepareSharedState(final SharedState sharedState)
    {
        try
        {
            registryLookup = sharedState.get(ResourceLoader.RELOADER_REGISTRY_LOOKUP_KEY);
        }
        catch (final NullPointerException e)
        {
            registryLookup = null;
        }
    }

    /** The registries of the reload in progress (or the running server's / client's when unknown). */
    protected HolderLookup.Provider getRegistryLookup()
    {
        return registryLookup != null ? registryLookup : NbtIO.registries();
    }
}

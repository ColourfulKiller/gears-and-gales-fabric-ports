package com.ldtteam.fabric.event;

import com.ldtteam.fabric.event.bus.Event;
import com.ldtteam.fabric.event.bus.ICancellableEvent;

/** Fabric port of NeoForge's event of the same name (see com.ldtteam.fabric.event.EventBridge for where it fires). */
public class TagsUpdatedEvent extends Event
{
    private final net.minecraft.core.RegistryAccess registryAccess;
    private final boolean client;

    public TagsUpdatedEvent(final net.minecraft.core.RegistryAccess registryAccess, final boolean client)
    {
        this.registryAccess = registryAccess;
        this.client = client;
    }

    public net.minecraft.core.RegistryAccess getRegistryAccess()
    {
        return registryAccess;
    }

    public boolean getClient()
    {
        return client;
    }
}

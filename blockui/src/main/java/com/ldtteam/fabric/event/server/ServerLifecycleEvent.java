package com.ldtteam.fabric.event.server;

import com.ldtteam.fabric.event.bus.Event;
import com.ldtteam.fabric.event.bus.ICancellableEvent;

/** Fabric port of NeoForge's event of the same name (see com.ldtteam.fabric.event.EventBridge for where it fires). */
public class ServerLifecycleEvent extends Event
{
    private final net.minecraft.server.MinecraftServer server;

    public ServerLifecycleEvent(final net.minecraft.server.MinecraftServer server)
    {
        this.server = server;
    }

    public net.minecraft.server.MinecraftServer getServer()
    {
        return server;
    }
}

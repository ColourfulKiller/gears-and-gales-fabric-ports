package com.ldtteam.fabric.event.server;

import com.ldtteam.fabric.event.bus.Event;
import com.ldtteam.fabric.event.bus.ICancellableEvent;

/** Fabric port of NeoForge's event of the same name (see com.ldtteam.fabric.event.EventBridge for where it fires). */
public class ServerAboutToStartEvent extends ServerLifecycleEvent
{

    public ServerAboutToStartEvent(final net.minecraft.server.MinecraftServer server)
    {
        super(server);
    }
}

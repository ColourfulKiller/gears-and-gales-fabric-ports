package com.ldtteam.fabric.event.tick;

import com.ldtteam.fabric.event.bus.Event;
import com.ldtteam.fabric.event.bus.ICancellableEvent;

/** Fabric port of NeoForge's event of the same name (see com.ldtteam.fabric.event.EventBridge for where it fires). */
public class ServerTickEvent extends Event
{
    private final net.minecraft.server.MinecraftServer server;

    public ServerTickEvent(final net.minecraft.server.MinecraftServer server)
    {
        this.server = server;
    }

    public net.minecraft.server.MinecraftServer getServer()
    {
        return server;
    }

    public static class Pre extends ServerTickEvent
    {

        public Pre(final net.minecraft.server.MinecraftServer server)
        {
            super(server);
        }

    }

    public static class Post extends ServerTickEvent
    {

        public Post(final net.minecraft.server.MinecraftServer server)
        {
            super(server);
        }

    }
}

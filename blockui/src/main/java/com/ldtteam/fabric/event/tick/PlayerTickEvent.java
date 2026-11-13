package com.ldtteam.fabric.event.tick;

import com.ldtteam.fabric.event.bus.Event;
import com.ldtteam.fabric.event.bus.ICancellableEvent;

/** Fabric port of NeoForge's event of the same name (see com.ldtteam.fabric.event.EventBridge for where it fires). */
public class PlayerTickEvent extends com.ldtteam.fabric.event.entity.player.PlayerEvent
{

    public PlayerTickEvent(final net.minecraft.world.entity.Entity entity)
    {
        super(entity);
    }

    public static class Pre extends PlayerTickEvent
    {

        public Pre(final net.minecraft.world.entity.Entity entity)
        {
            super(entity);
        }

    }

    public static class Post extends PlayerTickEvent
    {

        public Post(final net.minecraft.world.entity.Entity entity)
        {
            super(entity);
        }

    }
}

package com.ldtteam.fabric.event.entity.player;

import com.ldtteam.fabric.event.bus.Event;
import com.ldtteam.fabric.event.bus.ICancellableEvent;

/** Fabric port of NeoForge's event of the same name (see com.ldtteam.fabric.event.EventBridge for where it fires). */
public class PlayerEvent extends com.ldtteam.fabric.event.entity.living.LivingEvent
{

    public PlayerEvent(final net.minecraft.world.entity.Entity entity)
    {
        super(entity);
    }

    @Override
    public net.minecraft.world.entity.player.Player getEntity()
    {
        return (net.minecraft.world.entity.player.Player) super.getEntity();
    }

    public static class PlayerLoggedInEvent extends PlayerEvent
    {

        public PlayerLoggedInEvent(final net.minecraft.world.entity.Entity entity)
        {
            super(entity);
        }

    }

    public static class PlayerLoggedOutEvent extends PlayerEvent
    {

        public PlayerLoggedOutEvent(final net.minecraft.world.entity.Entity entity)
        {
            super(entity);
        }

    }

    public static class PlayerChangedDimensionEvent extends PlayerEvent
    {
        private final net.minecraft.resources.ResourceKey<net.minecraft.world.level.Level> from;
        private final net.minecraft.resources.ResourceKey<net.minecraft.world.level.Level> to;

        public PlayerChangedDimensionEvent(final net.minecraft.world.entity.Entity entity, final net.minecraft.resources.ResourceKey<net.minecraft.world.level.Level> from, final net.minecraft.resources.ResourceKey<net.minecraft.world.level.Level> to)
        {
            super(entity);
            this.from = from;
            this.to = to;
        }

        public net.minecraft.resources.ResourceKey<net.minecraft.world.level.Level> getFrom()
        {
            return from;
        }

        public net.minecraft.resources.ResourceKey<net.minecraft.world.level.Level> getTo()
        {
            return to;
        }

    }
}

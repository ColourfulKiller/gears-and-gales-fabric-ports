package com.ldtteam.fabric.event.entity;

import com.ldtteam.fabric.event.bus.Event;
import com.ldtteam.fabric.event.bus.ICancellableEvent;

/** Fabric port of NeoForge's event of the same name (see com.ldtteam.fabric.event.EventBridge for where it fires). */
public class EntityTravelToDimensionEvent extends EntityEvent implements ICancellableEvent
{
    private final net.minecraft.resources.ResourceKey<net.minecraft.world.level.Level> dimension;

    public EntityTravelToDimensionEvent(final net.minecraft.world.entity.Entity entity, final net.minecraft.resources.ResourceKey<net.minecraft.world.level.Level> dimension)
    {
        super(entity);
        this.dimension = dimension;
    }

    public net.minecraft.resources.ResourceKey<net.minecraft.world.level.Level> getDimension()
    {
        return dimension;
    }
}

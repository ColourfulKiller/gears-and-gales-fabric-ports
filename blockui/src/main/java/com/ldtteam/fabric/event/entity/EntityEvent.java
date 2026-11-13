package com.ldtteam.fabric.event.entity;

import com.ldtteam.fabric.event.bus.Event;
import com.ldtteam.fabric.event.bus.ICancellableEvent;

/** Fabric port of NeoForge's event of the same name (see com.ldtteam.fabric.event.EventBridge for where it fires). */
public class EntityEvent extends Event
{
    private final net.minecraft.world.entity.Entity entity;

    public EntityEvent(final net.minecraft.world.entity.Entity entity)
    {
        this.entity = entity;
    }

    public net.minecraft.world.entity.Entity getEntity()
    {
        return entity;
    }
}

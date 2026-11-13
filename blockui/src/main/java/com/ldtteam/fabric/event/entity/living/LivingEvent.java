package com.ldtteam.fabric.event.entity.living;

import com.ldtteam.fabric.event.bus.Event;
import com.ldtteam.fabric.event.bus.ICancellableEvent;

/** Fabric port of NeoForge's event of the same name (see com.ldtteam.fabric.event.EventBridge for where it fires). */
public class LivingEvent extends com.ldtteam.fabric.event.entity.EntityEvent
{

    public LivingEvent(final net.minecraft.world.entity.Entity entity)
    {
        super(entity);
    }

    @Override
    public net.minecraft.world.entity.LivingEntity getEntity()
    {
        return (net.minecraft.world.entity.LivingEntity) super.getEntity();
    }
}

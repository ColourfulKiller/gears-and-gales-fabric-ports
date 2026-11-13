package com.ldtteam.fabric.event.entity.player;

import com.ldtteam.fabric.event.bus.Event;
import com.ldtteam.fabric.event.bus.ICancellableEvent;

/** Fabric port of NeoForge's event of the same name (see com.ldtteam.fabric.event.EventBridge for where it fires). */
public class AttackEntityEvent extends PlayerEvent implements ICancellableEvent
{
    private final net.minecraft.world.entity.Entity target;

    public AttackEntityEvent(final net.minecraft.world.entity.Entity entity, final net.minecraft.world.entity.Entity target)
    {
        super(entity);
        this.target = target;
    }

    public net.minecraft.world.entity.Entity getTarget()
    {
        return target;
    }
}

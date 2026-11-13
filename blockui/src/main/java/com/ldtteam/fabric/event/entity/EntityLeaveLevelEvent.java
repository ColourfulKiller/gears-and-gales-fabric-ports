package com.ldtteam.fabric.event.entity;

import com.ldtteam.fabric.event.bus.Event;
import com.ldtteam.fabric.event.bus.ICancellableEvent;

/** Fabric port of NeoForge's event of the same name (see com.ldtteam.fabric.event.EventBridge for where it fires). */
public class EntityLeaveLevelEvent extends EntityEvent
{
    private final net.minecraft.world.level.Level level;

    public EntityLeaveLevelEvent(final net.minecraft.world.entity.Entity entity, final net.minecraft.world.level.Level level)
    {
        super(entity);
        this.level = level;
    }

    public net.minecraft.world.level.Level getLevel()
    {
        return level;
    }
}

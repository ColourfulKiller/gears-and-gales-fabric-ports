package com.ldtteam.fabric.event.entity;

import com.ldtteam.fabric.event.bus.Event;
import com.ldtteam.fabric.event.bus.ICancellableEvent;

/** Fabric port of NeoForge's event of the same name (see com.ldtteam.fabric.event.EventBridge for where it fires). */
public class EntityJoinLevelEvent extends EntityEvent implements ICancellableEvent
{
    private final net.minecraft.world.level.Level level;
    private final boolean loadedFromDisk;

    public EntityJoinLevelEvent(final net.minecraft.world.entity.Entity entity, final net.minecraft.world.level.Level level, final boolean loadedFromDisk)
    {
        super(entity);
        this.level = level;
        this.loadedFromDisk = loadedFromDisk;
    }

    public net.minecraft.world.level.Level getLevel()
    {
        return level;
    }

    public boolean getLoadedFromDisk()
    {
        return loadedFromDisk;
    }

    public boolean loadedFromDisk()
    {
        return loadedFromDisk;
    }
}

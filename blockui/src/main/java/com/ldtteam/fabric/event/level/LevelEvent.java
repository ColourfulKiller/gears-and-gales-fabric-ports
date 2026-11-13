package com.ldtteam.fabric.event.level;

import com.ldtteam.fabric.event.bus.Event;
import com.ldtteam.fabric.event.bus.ICancellableEvent;

/** Fabric port of NeoForge's event of the same name (see com.ldtteam.fabric.event.EventBridge for where it fires). */
public class LevelEvent extends Event
{
    private final net.minecraft.world.level.LevelAccessor level;

    public LevelEvent(final net.minecraft.world.level.LevelAccessor level)
    {
        this.level = level;
    }

    public net.minecraft.world.level.LevelAccessor getLevel()
    {
        return level;
    }

    public static class Load extends LevelEvent
    {

        public Load(final net.minecraft.world.level.LevelAccessor level)
        {
            super(level);
        }

    }

    public static class Unload extends LevelEvent
    {

        public Unload(final net.minecraft.world.level.LevelAccessor level)
        {
            super(level);
        }

    }
}

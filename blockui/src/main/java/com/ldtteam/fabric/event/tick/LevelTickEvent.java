package com.ldtteam.fabric.event.tick;

import com.ldtteam.fabric.event.bus.Event;
import com.ldtteam.fabric.event.bus.ICancellableEvent;

/** Fabric port of NeoForge's event of the same name (see com.ldtteam.fabric.event.EventBridge for where it fires). */
public class LevelTickEvent extends Event
{
    private final net.minecraft.world.level.Level level;

    public LevelTickEvent(final net.minecraft.world.level.Level level)
    {
        this.level = level;
    }

    public net.minecraft.world.level.Level getLevel()
    {
        return level;
    }

    public static class Pre extends LevelTickEvent
    {

        public Pre(final net.minecraft.world.level.Level level)
        {
            super(level);
        }

    }

    public static class Post extends LevelTickEvent
    {

        public Post(final net.minecraft.world.level.Level level)
        {
            super(level);
        }

    }
}

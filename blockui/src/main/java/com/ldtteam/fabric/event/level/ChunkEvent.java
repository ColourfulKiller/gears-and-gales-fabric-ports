package com.ldtteam.fabric.event.level;

import com.ldtteam.fabric.event.bus.Event;
import com.ldtteam.fabric.event.bus.ICancellableEvent;

/** Fabric port of NeoForge's event of the same name (see com.ldtteam.fabric.event.EventBridge for where it fires). */
public class ChunkEvent extends Event
{
    private final net.minecraft.world.level.LevelAccessor level;
    private final net.minecraft.world.level.chunk.ChunkAccess chunk;

    public ChunkEvent(final net.minecraft.world.level.LevelAccessor level, final net.minecraft.world.level.chunk.ChunkAccess chunk)
    {
        this.level = level;
        this.chunk = chunk;
    }

    public net.minecraft.world.level.LevelAccessor getLevel()
    {
        return level;
    }

    public net.minecraft.world.level.chunk.ChunkAccess getChunk()
    {
        return chunk;
    }

    public static class Load extends ChunkEvent
    {
        private final boolean newChunk;

        public Load(final net.minecraft.world.level.LevelAccessor level, final net.minecraft.world.level.chunk.ChunkAccess chunk, final boolean newChunk)
        {
            super(level, chunk);
            this.newChunk = newChunk;
        }

        public boolean getNewChunk()
        {
            return newChunk;
        }

    public boolean isNewChunk()
    {
        return newChunk;
    }

    }

    public static class Unload extends ChunkEvent
    {

        public Unload(final net.minecraft.world.level.LevelAccessor level, final net.minecraft.world.level.chunk.ChunkAccess chunk)
        {
            super(level, chunk);
        }

    }
}

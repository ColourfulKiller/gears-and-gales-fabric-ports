package com.ldtteam.fabric.event.level;

import com.ldtteam.fabric.event.bus.Event;
import com.ldtteam.fabric.event.bus.ICancellableEvent;

/** Fabric port of NeoForge's event of the same name (see com.ldtteam.fabric.event.EventBridge for where it fires). */
public class ExplosionEvent extends Event
{
    private final net.minecraft.world.level.Level level;
    private final net.minecraft.world.level.Explosion explosion;

    public ExplosionEvent(final net.minecraft.world.level.Level level, final net.minecraft.world.level.Explosion explosion)
    {
        this.level = level;
        this.explosion = explosion;
    }

    public net.minecraft.world.level.Level getLevel()
    {
        return level;
    }

    public net.minecraft.world.level.Explosion getExplosion()
    {
        return explosion;
    }

    public static class Start extends ExplosionEvent implements ICancellableEvent
    {

        public Start(final net.minecraft.world.level.Level level, final net.minecraft.world.level.Explosion explosion)
        {
            super(level, explosion);
        }

    }

    public static class Detonate extends ExplosionEvent
    {
        private final java.util.List<net.minecraft.core.BlockPos> affectedBlocks;
        private final java.util.List<net.minecraft.world.entity.Entity> affectedEntities;

        public Detonate(final net.minecraft.world.level.Level level, final net.minecraft.world.level.Explosion explosion, final java.util.List<net.minecraft.core.BlockPos> affectedBlocks, final java.util.List<net.minecraft.world.entity.Entity> affectedEntities)
        {
            super(level, explosion);
            this.affectedBlocks = affectedBlocks;
            this.affectedEntities = affectedEntities;
        }

        public java.util.List<net.minecraft.core.BlockPos> getAffectedBlocks()
        {
            return affectedBlocks;
        }

        public java.util.List<net.minecraft.world.entity.Entity> getAffectedEntities()
        {
            return affectedEntities;
        }

    }
}

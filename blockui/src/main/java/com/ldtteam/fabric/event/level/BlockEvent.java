package com.ldtteam.fabric.event.level;

import com.ldtteam.fabric.event.bus.Event;
import com.ldtteam.fabric.event.bus.ICancellableEvent;
import net.minecraft.world.level.block.state.BlockState;

/** Fabric port of NeoForge's event of the same name (see com.ldtteam.fabric.event.EventBridge for where it fires). */
public class BlockEvent extends Event
{
    private final net.minecraft.world.level.LevelAccessor level;
    private final net.minecraft.core.BlockPos pos;
    private final net.minecraft.world.level.block.state.BlockState state;

    public BlockEvent(final net.minecraft.world.level.LevelAccessor level, final net.minecraft.core.BlockPos pos, final net.minecraft.world.level.block.state.BlockState state)
    {
        this.level = level;
        this.pos = pos;
        this.state = state;
    }

    public net.minecraft.world.level.LevelAccessor getLevel()
    {
        return level;
    }

    public net.minecraft.core.BlockPos getPos()
    {
        return pos;
    }

    public net.minecraft.world.level.block.state.BlockState getState()
    {
        return state;
    }

    public static class BreakEvent extends BlockEvent implements ICancellableEvent
    {
        private final net.minecraft.world.entity.player.Player player;

        public BreakEvent(final net.minecraft.world.level.LevelAccessor level, final net.minecraft.core.BlockPos pos, final net.minecraft.world.level.block.state.BlockState state, final net.minecraft.world.entity.player.Player player)
        {
            super(level, pos, state);
            this.player = player;
        }

        public net.minecraft.world.entity.player.Player getPlayer()
        {
            return player;
        }

    }

    public static class EntityPlaceEvent extends BlockEvent implements ICancellableEvent
    {
        private final net.minecraft.world.entity.Entity entity;
        private final net.minecraft.world.level.block.state.BlockState placedAgainst;

        public EntityPlaceEvent(final net.minecraft.world.level.LevelAccessor level, final net.minecraft.core.BlockPos pos, final net.minecraft.world.level.block.state.BlockState state, final net.minecraft.world.entity.Entity entity, final net.minecraft.world.level.block.state.BlockState placedAgainst)
        {
            super(level, pos, state);
            this.entity = entity;
            this.placedAgainst = placedAgainst;
        }

        public net.minecraft.world.entity.Entity getEntity()
        {
            return entity;
        }

        public net.minecraft.world.level.block.state.BlockState getPlacedAgainst()
        {
            return placedAgainst;
        }

    public BlockState getPlacedBlock()
    {
        return getState();
    }

    }

    public static class FarmlandTrampleEvent extends BlockEvent implements ICancellableEvent
    {
        private final net.minecraft.world.entity.Entity entity;
        private final float fallDistance;

        public FarmlandTrampleEvent(final net.minecraft.world.level.LevelAccessor level, final net.minecraft.core.BlockPos pos, final net.minecraft.world.level.block.state.BlockState state, final net.minecraft.world.entity.Entity entity, final float fallDistance)
        {
            super(level, pos, state);
            this.entity = entity;
            this.fallDistance = fallDistance;
        }

        public net.minecraft.world.entity.Entity getEntity()
        {
            return entity;
        }

        public float getFallDistance()
        {
            return fallDistance;
        }

    }
}

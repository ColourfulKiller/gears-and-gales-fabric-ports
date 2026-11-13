package com.ldtteam.fabric.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

/**
 * 1.21.1's {@code Block#onRemove}: called on the server whenever the block's state at a position is replaced, while the
 * old block entity still exists (1.21.5 split this into BlockEntity#preRemoveSideEffects and
 * Block#affectNeighborsAfterRemoval). Called from com.ldtteam.fabric.event.mixin.LevelChunkRemovalMixin.
 */
public interface RemovalHook
{
    void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston);
}
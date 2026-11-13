package com.ldtteam.fabric.event.mixin;

import com.ldtteam.fabric.block.RemovalHook;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Calls {@link RemovalHook#onRemove} before a block state is replaced on the server (block entity still present). */
@Mixin(LevelChunk.class)
public abstract class LevelChunkRemovalMixin
{
    @Shadow
    @Final
    Level level;

    @Inject(method = "setBlockState", at = @At("HEAD"))
    private void ldtteam$onRemove(final BlockPos pos, final BlockState newState, final int flags, final CallbackInfoReturnable<BlockState> cir)
    {
        if (level.isClientSide())
        {
            return;
        }
        final BlockState old = ((LevelChunk) (Object) this).getBlockState(pos);
        if (old != newState && old.getBlock() instanceof final RemovalHook hook)
        {
            hook.onRemove(old, level, pos, newState, (flags & 64) != 0);
        }
    }
}
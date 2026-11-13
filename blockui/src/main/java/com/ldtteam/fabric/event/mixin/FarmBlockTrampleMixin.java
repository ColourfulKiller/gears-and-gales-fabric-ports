package com.ldtteam.fabric.event.mixin;

import com.ldtteam.fabric.event.EventBridge;
import com.ldtteam.fabric.event.level.BlockEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.FarmBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** BlockEvent.FarmlandTrampleEvent: a cancel keeps the farmland (fall damage still applies via the vanilla super call skipped). */
@Mixin(FarmBlock.class)
public abstract class FarmBlockTrampleMixin
{
    @Inject(method = "fallOn", at = @At("HEAD"), cancellable = true)
    private void ldtteam$trample(final Level level, final BlockState state, final BlockPos pos, final Entity entity, final double fallDistance, final CallbackInfo ci)
    {
        if (!level.isClientSide() && EventBridge.wants(BlockEvent.FarmlandTrampleEvent.class)
              && EventBridge.post(new BlockEvent.FarmlandTrampleEvent(level, pos, state, entity, (float) fallDistance)).isCanceled())
        {
            entity.causeFallDamage(fallDistance, 1.0F, entity.damageSources().fall());
            ci.cancel();
        }
    }
}
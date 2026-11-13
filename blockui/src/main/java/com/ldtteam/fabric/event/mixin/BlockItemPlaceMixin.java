package com.ldtteam.fabric.event.mixin;

import com.ldtteam.fabric.event.EventBridge;
import com.ldtteam.fabric.event.level.BlockEvent;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** BlockEvent.EntityPlaceEvent: before a block item places its block; a cancel refuses the placement. */
@Mixin(BlockItem.class)
public abstract class BlockItemPlaceMixin
{
    @Inject(method = "place", at = @At("HEAD"), cancellable = true)
    private void ldtteam$placeEvent(final BlockPlaceContext context, final CallbackInfoReturnable<InteractionResult> cir)
    {
        if (!EventBridge.wants(BlockEvent.EntityPlaceEvent.class) || !context.canPlace())
        {
            return;
        }
        final BlockState placed = ((BlockItem) (Object) this).getBlock().getStateForPlacement(context);
        if (placed == null)
        {
            return;
        }
        final BlockState against = context.getLevel().getBlockState(context.getClickedPos().relative(context.getClickedFace().getOpposite()));
        if (EventBridge.post(new BlockEvent.EntityPlaceEvent(context.getLevel(), context.getClickedPos(), placed, context.getPlayer(), against)).isCanceled())
        {
            cir.setReturnValue(InteractionResult.FAIL);
        }
    }
}
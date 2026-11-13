package com.ldtteam.blockui.mixin;

import com.ldtteam.fabric.block.PositionalBlockHooks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Calls {@link PositionalBlockHooks#getCloneItemStack} on pick-block (NeoForge's position-aware clone stack).
 */
@Mixin(BlockBehaviour.BlockStateBase.class)
public abstract class CloneItemStackMixin
{
    @Inject(method = "getCloneItemStack(Lnet/minecraft/world/level/LevelReader;Lnet/minecraft/core/BlockPos;Z)Lnet/minecraft/world/item/ItemStack;",
        at = @At("HEAD"), cancellable = true)
    private void ldtfabric$positionalClone(final LevelReader level, final BlockPos pos, final boolean includeData, final CallbackInfoReturnable<ItemStack> cir)
    {
        final BlockState state = (BlockState) (Object) this;
        if (state.getBlock() instanceof final PositionalBlockHooks hooks)
        {
            final ItemStack stack = hooks.getCloneItemStack(state, null, level, pos, null);
            if (stack != null)
            {
                cir.setReturnValue(stack);
            }
        }
    }
}

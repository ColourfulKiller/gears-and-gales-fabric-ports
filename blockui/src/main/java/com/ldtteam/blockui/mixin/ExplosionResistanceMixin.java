package com.ldtteam.blockui.mixin;

import com.ldtteam.fabric.block.PositionalBlockHooks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.ExplosionDamageCalculator;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Calls {@link PositionalBlockHooks#getExplosionResistance} (NeoForge's position-aware explosion resistance).
 */
@Mixin(ExplosionDamageCalculator.class)
public abstract class ExplosionResistanceMixin
{
    @Redirect(method = "getBlockExplosionResistance",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/Block;getExplosionResistance()F"))
    private float ldtfabric$positionalResistance(final Block block,
        final Explosion explosion,
        final BlockGetter level,
        final BlockPos pos,
        final BlockState state,
        final FluidState fluid)
    {
        return block instanceof final PositionalBlockHooks hooks ? hooks.getExplosionResistance(state, level, pos, explosion) :
            block.getExplosionResistance();
    }
}

package com.ldtteam.fabric.block;

import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.HitResult;
import org.jetbrains.annotations.Nullable;

/**
 * The position-aware block hooks of NeoForge's IBlockExtension that the LDT Team mods override. Vanilla only has the
 * position-less versions.
 * <ul>
 * <li>{@link #getExplosionResistance} is called by ExplosionDamageCalculator (mixin com.ldtteam.blockui.mixin.ExplosionResistanceMixin)</li>
 * <li>{@link #getCloneItemStack} is called on pick-block (mixin com.ldtteam.blockui.mixin.CloneItemStackMixin); hitResult and
 * player are null there, because 1.21.4+ pick-block doesn't pass them</li>
 * <li>{@link #getSoundType} is NOT called by the game: vanilla reads sounds in dozens of places without a position, so
 * blocks use their default sound type</li>
 * </ul>
 */
public interface PositionalBlockHooks
{
    default SoundType getSoundType(final BlockState state, final LevelReader level, final BlockPos pos, @Nullable final Entity entity)
    {
        return defaultSoundType(state, level, pos, entity);
    }

    default float getExplosionResistance(final BlockState state, final BlockGetter level, final BlockPos pos, final Explosion explosion)
    {
        return defaultExplosionResistance(state, level, pos, explosion);
    }

    /**
     * @return the stack, or null to use the vanilla pick-block result
     */
    default @Nullable ItemStack getCloneItemStack(final BlockState state,
        @Nullable final HitResult target,
        final LevelReader level,
        final BlockPos pos,
        @Nullable final Player player)
    {
        return null;
    }

    /**
     * NeoForge's position-aware rotate (vanilla only has {@code BlockState.rotate(Rotation)}). Called by blueprint
     * placement through {@link #rotateAt(BlockState, LevelAccessor, BlockPos, Rotation)}.
     */
    default BlockState rotate(final BlockState state, final LevelAccessor level, final BlockPos pos, final Rotation rotation)
    {
        return state.rotate(rotation);
    }

    /** Was {@code blockState.rotate(level, pos, rotation)}: uses the block's hook when it has one. */
    static BlockState rotateAt(final BlockState state, final LevelAccessor level, final BlockPos pos, final Rotation rotation)
    {
        return state.getBlock() instanceof final PositionalBlockHooks hooks ? hooks.rotate(state, level, pos, rotation) : state.rotate(rotation);
    }

    /** What NeoForge's IBlockExtension default did; use where 1.21.1 code wrote super::getSoundType. */
    static SoundType defaultSoundType(final BlockState state, final LevelReader level, final BlockPos pos, @Nullable final Entity entity)
    {
        return state.getSoundType();
    }

    /** What NeoForge's IBlockExtension default did; use where 1.21.1 code wrote super::getExplosionResistance. */
    static float defaultExplosionResistance(final BlockState state, final BlockGetter level, final BlockPos pos, final Explosion explosion)
    {
        return state.getBlock().getExplosionResistance();
    }

    /** Replaces PropertyDispatch.QuadFunction, removed in 1.21.11. */
    @FunctionalInterface
    interface QuadFunction<A, B, C, D, R>
    {
        R apply(A a, B b, C c, D d);
    }

    /** Was NeoForge's {@code state.isBed(level, pos, sleeper)}. */
    static boolean isBed(final BlockState state, final BlockGetter level, final BlockPos pos, @Nullable final Entity sleeper)
    {
        return state.getBlock() instanceof net.minecraft.world.level.block.BedBlock || state.is(net.minecraft.tags.BlockTags.BEDS);
    }

    /** Was NeoForge's {@code state.isLadder(level, pos, entity)}. */
    static boolean isLadder(final BlockState state, final LevelReader level, final BlockPos pos, @Nullable final Entity entity)
    {
        return state.is(net.minecraft.tags.BlockTags.CLIMBABLE);
    }
}
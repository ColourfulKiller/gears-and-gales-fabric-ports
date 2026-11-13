package com.ldtteam.blockui.util;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

/**
 * Same shape as vanilla's ItemPropertyFunction, which 1.21.4 removed. Kept so ItemIconWithProperties' API still compiles.
 */
@FunctionalInterface
public interface ItemPropertyFunction
{
    float call(ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity entity, int seed);
}

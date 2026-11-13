package com.ldtteam.fabric.items;

import net.minecraft.world.item.ItemStack;

/**
 * Same contract as NeoForge's IItemHandler (which the LDT mods are built on): slot-based inventory access where
 * insert/extract return what's left over / what was taken and {@code simulate} makes no changes.
 * Stacks returned by {@link #getStackInSlot} must not be modified.
 */
public interface IItemHandler
{
    int getSlots();

    ItemStack getStackInSlot(int slot);

    /**
     * @return the remainder that could not be inserted (empty when all of it fit)
     */
    ItemStack insertItem(int slot, ItemStack stack, boolean simulate);

    /**
     * @return the extracted stack (empty when nothing could be taken)
     */
    ItemStack extractItem(int slot, int amount, boolean simulate);

    int getSlotLimit(int slot);

    boolean isItemValid(int slot, ItemStack stack);
}
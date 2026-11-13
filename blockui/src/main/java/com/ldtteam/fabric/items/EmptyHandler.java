package com.ldtteam.fabric.items;

import net.minecraft.world.item.ItemStack;

/** NeoForge's EmptyItemHandler. */
public final class EmptyHandler implements IItemHandlerModifiable
{
    public static final EmptyHandler INSTANCE = new EmptyHandler();

    private EmptyHandler()
    {
    }

    @Override
    public void setStackInSlot(final int slot, final ItemStack stack)
    {
    }

    @Override
    public int getSlots()
    {
        return 0;
    }

    @Override
    public ItemStack getStackInSlot(final int slot)
    {
        return ItemStack.EMPTY;
    }

    @Override
    public ItemStack insertItem(final int slot, final ItemStack stack, final boolean simulate)
    {
        return stack;
    }

    @Override
    public ItemStack extractItem(final int slot, final int amount, final boolean simulate)
    {
        return ItemStack.EMPTY;
    }

    @Override
    public int getSlotLimit(final int slot)
    {
        return 0;
    }

    @Override
    public boolean isItemValid(final int slot, final ItemStack stack)
    {
        return false;
    }
}

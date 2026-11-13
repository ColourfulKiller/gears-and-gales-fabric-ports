package com.ldtteam.fabric.items;

import net.minecraft.world.item.ItemStack;

/** NeoForge's RangedWrapper: a contiguous slot range of another handler. */
public class RangedWrapper implements IItemHandlerModifiable
{
    private final IItemHandlerModifiable compose;
    private final int minSlot;
    private final int maxSlot;

    public RangedWrapper(final IItemHandlerModifiable compose, final int minSlot, final int maxSlotExclusive)
    {
        this.compose = compose;
        this.minSlot = minSlot;
        this.maxSlot = maxSlotExclusive;
    }

    private boolean inRange(final int slot)
    {
        return slot >= 0 && slot < maxSlot - minSlot;
    }

    @Override
    public int getSlots()
    {
        return maxSlot - minSlot;
    }

    @Override
    public ItemStack getStackInSlot(final int slot)
    {
        return inRange(slot) ? compose.getStackInSlot(slot + minSlot) : ItemStack.EMPTY;
    }

    @Override
    public ItemStack insertItem(final int slot, final ItemStack stack, final boolean simulate)
    {
        return inRange(slot) ? compose.insertItem(slot + minSlot, stack, simulate) : stack;
    }

    @Override
    public ItemStack extractItem(final int slot, final int amount, final boolean simulate)
    {
        return inRange(slot) ? compose.extractItem(slot + minSlot, amount, simulate) : ItemStack.EMPTY;
    }

    @Override
    public void setStackInSlot(final int slot, final ItemStack stack)
    {
        if (inRange(slot))
        {
            compose.setStackInSlot(slot + minSlot, stack);
        }
    }

    @Override
    public int getSlotLimit(final int slot)
    {
        return inRange(slot) ? compose.getSlotLimit(slot + minSlot) : 0;
    }

    @Override
    public boolean isItemValid(final int slot, final ItemStack stack)
    {
        return inRange(slot) && compose.isItemValid(slot + minSlot, stack);
    }
}

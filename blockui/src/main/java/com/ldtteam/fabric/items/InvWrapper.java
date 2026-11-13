package com.ldtteam.fabric.items;

import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;

/**
 * An {@link IItemHandlerModifiable} over a vanilla {@link Container} (NeoForge's InvWrapper).
 */
public class InvWrapper implements IItemHandlerModifiable
{
    private final Container inv;

    public InvWrapper(final Container inv)
    {
        this.inv = inv;
    }

    public Container getInv()
    {
        return inv;
    }

    @Override
    public int getSlots()
    {
        return inv.getContainerSize();
    }

    @Override
    public ItemStack getStackInSlot(final int slot)
    {
        return inv.getItem(slot);
    }

    @Override
    public ItemStack insertItem(final int slot, final ItemStack stack, final boolean simulate)
    {
        if (stack.isEmpty() || !inv.canPlaceItem(slot, stack))
        {
            return stack;
        }
        final ItemStack existing = inv.getItem(slot);
        final int limit = Math.min(getSlotLimit(slot), stack.getMaxStackSize());
        if (!existing.isEmpty())
        {
            if (!ItemHandlerHelper.canItemStacksStack(stack, existing))
            {
                return stack;
            }
            final int space = Math.min(limit, existing.getMaxStackSize()) - existing.getCount();
            if (space <= 0)
            {
                return stack;
            }
            final int moved = Math.min(space, stack.getCount());
            if (!simulate)
            {
                final ItemStack grown = existing.copy();
                grown.grow(moved);
                inv.setItem(slot, grown);
                inv.setChanged();
            }
            return stack.getCount() == moved ? ItemStack.EMPTY : stack.copyWithCount(stack.getCount() - moved);
        }
        final int moved = Math.min(limit, stack.getCount());
        if (!simulate)
        {
            inv.setItem(slot, stack.copyWithCount(moved));
            inv.setChanged();
        }
        return stack.getCount() == moved ? ItemStack.EMPTY : stack.copyWithCount(stack.getCount() - moved);
    }

    @Override
    public ItemStack extractItem(final int slot, final int amount, final boolean simulate)
    {
        if (amount <= 0)
        {
            return ItemStack.EMPTY;
        }
        final ItemStack existing = inv.getItem(slot);
        if (existing.isEmpty())
        {
            return ItemStack.EMPTY;
        }
        final int taken = Math.min(amount, existing.getCount());
        if (!simulate)
        {
            final ItemStack result = inv.removeItem(slot, taken);
            inv.setChanged();
            return result;
        }
        return existing.copyWithCount(taken);
    }

    @Override
    public void setStackInSlot(final int slot, final ItemStack stack)
    {
        inv.setItem(slot, stack);
    }

    @Override
    public int getSlotLimit(final int slot)
    {
        return inv.getMaxStackSize();
    }

    @Override
    public boolean isItemValid(final int slot, final ItemStack stack)
    {
        return inv.canPlaceItem(slot, stack);
    }

    @Override
    public boolean equals(final Object o)
    {
        return o instanceof final InvWrapper other && other.inv == inv;
    }

    @Override
    public int hashCode()
    {
        return System.identityHashCode(inv);
    }
}
package com.ldtteam.fabric.items;

import net.minecraft.world.item.ItemStack;

/** NeoForge's CombinedInvWrapper: several handlers presented as one, slots in order. */
public class CombinedInvWrapper implements IItemHandlerModifiable
{
    protected final IItemHandlerModifiable[] itemHandler;
    protected final int[] baseIndex;
    protected final int slotCount;

    public CombinedInvWrapper(final IItemHandlerModifiable... itemHandler)
    {
        this.itemHandler = itemHandler;
        this.baseIndex = new int[itemHandler.length];
        int index = 0;
        for (int i = 0; i < itemHandler.length; i++)
        {
            index += itemHandler[i].getSlots();
            baseIndex[i] = index;
        }
        this.slotCount = index;
    }

    protected int getIndexForSlot(final int slot)
    {
        if (slot < 0)
        {
            return -1;
        }
        for (int i = 0; i < baseIndex.length; i++)
        {
            if (slot - baseIndex[i] < 0)
            {
                return i;
            }
        }
        return -1;
    }

    protected IItemHandlerModifiable getHandlerFromIndex(final int index)
    {
        return index < 0 || index >= itemHandler.length ? EmptyHandler.INSTANCE : itemHandler[index];
    }

    protected int getSlotFromIndex(final int slot, final int index)
    {
        return index <= 0 || index >= baseIndex.length ? slot : slot - baseIndex[index - 1];
    }

    @Override
    public void setStackInSlot(final int slot, final ItemStack stack)
    {
        final int index = getIndexForSlot(slot);
        getHandlerFromIndex(index).setStackInSlot(getSlotFromIndex(slot, index), stack);
    }

    @Override
    public int getSlots()
    {
        return slotCount;
    }

    @Override
    public ItemStack getStackInSlot(final int slot)
    {
        final int index = getIndexForSlot(slot);
        return getHandlerFromIndex(index).getStackInSlot(getSlotFromIndex(slot, index));
    }

    @Override
    public ItemStack insertItem(final int slot, final ItemStack stack, final boolean simulate)
    {
        final int index = getIndexForSlot(slot);
        return getHandlerFromIndex(index).insertItem(getSlotFromIndex(slot, index), stack, simulate);
    }

    @Override
    public ItemStack extractItem(final int slot, final int amount, final boolean simulate)
    {
        final int index = getIndexForSlot(slot);
        return getHandlerFromIndex(index).extractItem(getSlotFromIndex(slot, index), amount, simulate);
    }

    @Override
    public int getSlotLimit(final int slot)
    {
        final int index = getIndexForSlot(slot);
        return getHandlerFromIndex(index).getSlotLimit(getSlotFromIndex(slot, index));
    }

    @Override
    public boolean isItemValid(final int slot, final ItemStack stack)
    {
        final int index = getIndexForSlot(slot);
        return getHandlerFromIndex(index).isItemValid(getSlotFromIndex(slot, index), stack);
    }
}

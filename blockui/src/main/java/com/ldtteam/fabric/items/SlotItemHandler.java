package com.ldtteam.fabric.items;

import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/**
 * NeoForge's SlotItemHandler: a menu slot backed by an {@link IItemHandler} slot instead of a vanilla Container.
 */
public class SlotItemHandler extends Slot
{
    private static final Container EMPTY = new SimpleContainer(0);

    private final IItemHandler itemHandler;
    private final int index;

    public SlotItemHandler(final IItemHandler itemHandler, final int index, final int xPosition, final int yPosition)
    {
        super(EMPTY, index, xPosition, yPosition);
        this.itemHandler = itemHandler;
        this.index = index;
    }

    @Override
    public boolean mayPlace(final ItemStack stack)
    {
        return !stack.isEmpty() && itemHandler.isItemValid(index, stack);
    }

    @Override
    public ItemStack getItem()
    {
        return this.getItemHandler().getStackInSlot(index);
    }

    @Override
    public void set(final ItemStack stack)
    {
        ((IItemHandlerModifiable) this.getItemHandler()).setStackInSlot(index, stack);
        this.setChanged();
    }

    @Override
    public void setByPlayer(final ItemStack newStack, final ItemStack oldStack)
    {
        set(newStack);
    }

    @Override
    public void setChanged()
    {
    }

    @Override
    public int getMaxStackSize()
    {
        return this.itemHandler.getSlotLimit(this.index);
    }

    @Override
    public int getMaxStackSize(final ItemStack stack)
    {
        final ItemStack maxAdd = stack.copyWithCount(stack.getMaxStackSize());
        final IItemHandler handler = this.getItemHandler();
        final ItemStack currentStack = handler.getStackInSlot(index);
        if (handler instanceof final IItemHandlerModifiable handlerModifiable)
        {
            handlerModifiable.setStackInSlot(index, ItemStack.EMPTY);
            final ItemStack remainder = handlerModifiable.insertItem(index, maxAdd, true);
            handlerModifiable.setStackInSlot(index, currentStack);
            return maxAdd.getCount() - remainder.getCount();
        }
        final ItemStack remainder = handler.insertItem(index, maxAdd, true);
        final int current = currentStack.getCount();
        final int added = maxAdd.getCount() - remainder.getCount();
        return current + added;
    }

    @Override
    public boolean mayPickup(final Player playerIn)
    {
        return !this.getItemHandler().extractItem(index, 1, true).isEmpty();
    }

    @Override
    public ItemStack remove(final int amount)
    {
        return this.getItemHandler().extractItem(index, amount, false);
    }

    public IItemHandler getItemHandler()
    {
        return itemHandler;
    }
}
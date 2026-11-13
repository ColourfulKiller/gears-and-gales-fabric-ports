package com.ldtteam.fabric.items;

import net.minecraft.world.item.ItemStack;

/** The parts of NeoForge's ItemHandlerHelper the LDT mods use. */
public final class ItemHandlerHelper
{
    private ItemHandlerHelper()
    {
    }

    /** Same item and components (count ignored), and the item stacks at all. */
    public static boolean canItemStacksStack(final ItemStack a, final ItemStack b)
    {
        return !a.isEmpty() && a.isStackable() && ItemStack.isSameItemSameComponents(a, b);
    }

    /** Same item and components, count ignored. */
    public static boolean canItemStacksStackRelaxed(final ItemStack a, final ItemStack b)
    {
        return !a.isEmpty() && !b.isEmpty() && ItemStack.isSameItemSameComponents(a, b);
    }

    public static ItemStack copyStackWithSize(final ItemStack stack, final int size)
    {
        return size == 0 ? ItemStack.EMPTY : stack.copyWithCount(size);
    }

    /**
     * Inserts into the first slots that take it.
     *
     * @return the remainder
     */
    public static ItemStack insertItem(final IItemHandler dest, ItemStack stack, final boolean simulate)
    {
        if (dest == null || stack.isEmpty())
        {
            return stack;
        }
        for (int i = 0; i < dest.getSlots() && !stack.isEmpty(); i++)
        {
            stack = dest.insertItem(i, stack, simulate);
        }
        return stack;
    }

    /**
     * Inserts into slots already holding the same item first, then empty ones.
     *
     * @return the remainder
     */
    public static ItemStack insertItemStacked(final IItemHandler inventory, ItemStack stack, final boolean simulate)
    {
        if (inventory == null || stack.isEmpty())
        {
            return stack;
        }
        if (!stack.isStackable())
        {
            return insertItem(inventory, stack, simulate);
        }
        for (int i = 0; i < inventory.getSlots() && !stack.isEmpty(); i++)
        {
            if (canItemStacksStackRelaxed(inventory.getStackInSlot(i), stack))
            {
                stack = inventory.insertItem(i, stack, simulate);
            }
        }
        for (int i = 0; i < inventory.getSlots() && !stack.isEmpty(); i++)
        {
            if (inventory.getStackInSlot(i).isEmpty())
            {
                stack = inventory.insertItem(i, stack, simulate);
            }
        }
        return stack;
    }
}
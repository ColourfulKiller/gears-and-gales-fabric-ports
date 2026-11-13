package com.ldtteam.fabric.items;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

/** NeoForge's EntityEquipmentInvWrapper: a living entity's hands and armor slots as an item handler. */
public class EquipmentWrapper implements IItemHandlerModifiable
{
    private static final EquipmentSlot[] SLOTS = {EquipmentSlot.MAINHAND, EquipmentSlot.OFFHAND, EquipmentSlot.FEET, EquipmentSlot.LEGS, EquipmentSlot.CHEST, EquipmentSlot.HEAD};

    private final LivingEntity entity;

    public EquipmentWrapper(final LivingEntity entity)
    {
        this.entity = entity;
    }

    @Override
    public int getSlots()
    {
        return SLOTS.length;
    }

    @Override
    public ItemStack getStackInSlot(final int slot)
    {
        return entity.getItemBySlot(SLOTS[slot]);
    }

    @Override
    public void setStackInSlot(final int slot, final ItemStack stack)
    {
        entity.setItemSlot(SLOTS[slot], stack);
    }

    @Override
    public ItemStack insertItem(final int slot, final ItemStack stack, final boolean simulate)
    {
        if (stack.isEmpty())
        {
            return ItemStack.EMPTY;
        }
        final ItemStack existing = getStackInSlot(slot);
        final int limit = Math.min(getSlotLimit(slot), stack.getMaxStackSize());
        if (!existing.isEmpty() && !ItemHandlerHelper.canItemStacksStack(stack, existing))
        {
            return stack;
        }
        final int space = limit - existing.getCount();
        if (space <= 0)
        {
            return stack;
        }
        final int moved = Math.min(space, stack.getCount());
        if (!simulate)
        {
            setStackInSlot(slot, stack.copyWithCount(existing.getCount() + moved));
        }
        return moved == stack.getCount() ? ItemStack.EMPTY : stack.copyWithCount(stack.getCount() - moved);
    }

    @Override
    public ItemStack extractItem(final int slot, final int amount, final boolean simulate)
    {
        final ItemStack existing = getStackInSlot(slot);
        if (amount <= 0 || existing.isEmpty())
        {
            return ItemStack.EMPTY;
        }
        final int taken = Math.min(amount, existing.getCount());
        if (!simulate)
        {
            setStackInSlot(slot, taken == existing.getCount() ? ItemStack.EMPTY : existing.copyWithCount(existing.getCount() - taken));
        }
        return existing.copyWithCount(taken);
    }

    @Override
    public int getSlotLimit(final int slot)
    {
        return SLOTS[slot].getType() == EquipmentSlot.Type.HUMANOID_ARMOR ? 1 : 64;
    }

    @Override
    public boolean isItemValid(final int slot, final ItemStack stack)
    {
        return true;
    }
}